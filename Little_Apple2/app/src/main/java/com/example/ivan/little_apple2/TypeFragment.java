package com.example.ivan.little_apple2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;


import com.example.ivan.little_apple2.Fragment.MainFragment;
import com.example.ivan.little_apple2.adapter.StrokeItemAdapter;
import com.example.ivan.little_apple2.algo.FrequencyBandModel;
import com.example.ivan.little_apple2.algo.ScoreComputing;
import com.example.ivan.little_apple2.algo.StrokeClassifier;
import com.example.ivan.little_apple2.algo.StrokeDetector;
import com.example.ivan.little_apple2.algo.TrainingWindowFinder;
import com.example.ivan.little_apple2.bt.devices.BeaconHandler;
import com.example.ivan.little_apple2.bt.devices.SoundWaveHandler;
import com.example.ivan.little_apple2.file.LogFileWriter;
import com.example.ivan.little_apple2.file.SystemParameters;
import com.example.ivan.little_apple2.file.sqlite.DataListItem;
import com.example.ivan.little_apple2.file.sqlite.MainFreqListItem;
import com.example.ivan.little_apple2.file.sqlite.StrokeItem;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Ivan on 2017/2/23.
 */

public class TypeFragment extends Fragment{
    private final static String TAG = "LoggingFragment";

    /* Connect Related */
    private static Activity mAct;
    private static SoundWaveHandler sw;
    private static BeaconHandler bh;

    /* Fragment Relate */
    private View v;
    public final static int TRAINING_TYPE = 1;
    public final static int TESTING_TYPE = 2;
    private final static String PAGETYPE_KEY = "LoggingFragment.PAGE_TYPE";

    /* Algorithm Related */
    public static FrequencyBandModel fbm = null;
    private static ScoreComputing SC = null;

    /* Log Related */
    private static LogFileWriter ReadmeWriter;
    private static Button btTraining;
    private static Button btTesting;
    private static Boolean isTraining = false;
    private static Boolean isTesting = false;
    private PopupWindow popupWindow; // for select model
    private Spinner dropdown; // for select model

    /* Stroke List */
    private static List<StrokeItem> strokelist_dataset = new ArrayList<>();
    private static ListView lv_StrokeData;
    private static StrokeItemAdapter Adapter;

    /* Interrupt Logging */
    public static final int DEVICE_HEADSET = 1;
    public static final int DEVICE_KOALA = 2;

    public static TypeFragment newInstance(Activity act, SoundWaveHandler s, BeaconHandler b, int type){
        TypeFragment lf = new TypeFragment();

        Bundle args = new Bundle();
        args.putInt(PAGETYPE_KEY, type);
        lf.setArguments(args);
        mAct = act;
        sw = s;
        bh = b;
        return lf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initial StrokeClassifier
        mAct.registerReceiver(mStrokeTypeResultReceiver, makeStrokeTypeResultIntentFilter());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int type = getArguments().getInt(PAGETYPE_KEY);

        if(type == TRAINING_TYPE) {
            v = inflater.inflate(R.layout.fragment_training, container, false);
            btTraining = (Button) v.findViewById(R.id.bt_voicesampling);
            btTraining.setOnClickListener(TrainingStartClickListener);
        }else if(type == TESTING_TYPE){
            Testing();

            v = inflater.inflate(R.layout.fragment_main, container, false);
            btTesting = (Button) v.findViewById(R.id.StartBtn);
//            btTesting.setOnClickListener(TestingStartClickListener);

            lv_StrokeData = (ListView) v.findViewById(R.id.game_listview);
            Adapter = new StrokeItemAdapter(mAct.getApplicationContext(), strokelist_dataset);
            lv_StrokeData.setAdapter(Adapter);
        }
        return v;
    }

    @Override
    public void onDestroy(){
        mAct.unregisterReceiver(mStrokeTypeResultReceiver);
        super.onDestroy();
    }


    /********************/
    /** Logging Event **/
    /********************/
    private void Testing(){
        if(fbm != null && fbm.CheckModelHasTrained()){
            if(SystemParameters.IsBtHeadsetReady && SystemParameters.IsKoalaReady && !isTesting)
                ShowWindowForSelectModel();
            else if(isTesting)
                StopLogging();
            else
                Toast.makeText(mAct,"You have to connect bt headset and koala.",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(mAct, "You must train your racket first.", Toast.LENGTH_SHORT).show();
        }
    }

    private Button.OnClickListener TrainingStartClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            if(SystemParameters.IsBtHeadsetReady && !isTraining)
                ActiveLogging(LogFileWriter.TRAINING_TYPE);
            else if(isTraining)
                StopLogging();
            else
                Toast.makeText(mAct, "You have to connect bt headset.", Toast.LENGTH_SHORT).show();
        }
    };

//    private Button.OnClickListener TestingStartClickListener = new Button.OnClickListener() {
//        @Override
//        public void onClick(View arg0) {
//            if(fbm != null && fbm.CheckModelHasTrained()){
//                if(SystemParameters.IsBtHeadsetReady && SystemParameters.IsKoalaReady && !isTesting)
//                    ShowWindowForSelectModel();
//                else if(isTesting)
//                    StopLogging();
//                else
//                    Toast.makeText(mAct,"You have to connect bt headset and koala.",Toast.LENGTH_SHORT).show();
//            }else{
//                Toast.makeText(mAct, "You must train your racket first.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    };

    private void ActiveLogging(final int LogType){
        //SystemParameters Initial
        SystemParameters.initializeSystemParameters();

        //UI Button Control
        if(LogType == LogFileWriter.TESTING_TYPE) {
            btTesting.setText(R.string.Testing_State);
            isTesting = true;
            ClearStrokeList();
        }
        else{
            btTraining.setText(R.string.Training_State);
            isTraining = true;
        }

        //Initial Log File
        ReadmeWriter = new LogFileWriter("Readme.txt", LogFileWriter.README_TYPE, LogType);

        new Thread(){
            @Override
            public void run() {
                // Trigger Sensor to Ready (wait isServiceRunning become true)
                sw.startRecording(LogType);
                if( isTesting ){
                    bh.startRecording(LogType);
                    StartTestingAlgo();
                }

                SystemParameters.SetMeasureStartTime(); //設定開始時間
                SystemParameters.isServiceRunning.set(true);

                mAct.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(mAct, "Log Service is Start", Toast.LENGTH_SHORT).show();
                        //init UI
                        //tv_strokeCount.setText("0");
                        //tv_strokeType.setText("None");
                    }
                });

            }
        }.start();
    }

    private void StopLogging(){
        final ProgressDialog dialog = ProgressDialog.show(mAct,
                "寫檔中", "處理檔案中，請稍後",true);

        Toast.makeText(mAct, "Log Service is Stop", Toast.LENGTH_SHORT).show();
        SystemParameters.isServiceRunning.set(false);
        SystemParameters.Duration = (System.currentTimeMillis() - SystemParameters.StartTime)/1000.0;

        new Thread(){
            public void run(){
                if(!isTesting) {
                    // Local Database Handler
                    long id = SQLiteInsertNewLoggingRecord(
                            SystemParameters.StartDate,
                            "ghg070",
                            SystemParameters.StrokeCount,
                            SystemParameters.filePath,
                            isTesting,
                            SystemParameters.SoundStartTime - SystemParameters.StartTime,
                            -1);
                    SystemParameters.TrainingId = id;
                }else{
                    long id = SQLiteInsertNewLoggingRecord(
                            SystemParameters.StartDate,
                            "ghg070",
                            SystemParameters.StrokeCount,
                            SystemParameters.filePath,
                            isTesting, SystemParameters.SoundStartTime - SystemParameters.StartTime,
                            SystemParameters.TrainingId);
                    SystemParameters.TestingId = id;
                }

                sw.stopRecording();
                if( isTesting ) bh.stopRecording();


                //Wait log file write done
                if( sw != null ) while(sw.isWrittingAudioDataLog.get());
                if( SC != null ) while(SC.isWrittingWindowScore.get());
                if( bh != null ) while(bh.isWrittingSensorDataLog.get());

                if(isTraining) StartTrainingAlgo(sw);

                //Show UI
                mAct.runOnUiThread(new Runnable() {
                    public void run() {
                        showLogInformationDialog();

                        //UI Button Control
                        if (isTesting) {
                            btTesting.setText(R.string.Not_Testing_State);
                            isTesting = false;
                        } else if (isTraining) {
                            btTraining.setText(R.string.Not_Training_State);
                            isTraining = false;
                        }
                        dialog.dismiss();
                    }
                });
            }
        }.start();
    }


    private void showLogInformationDialog(){//and also write readme.txt
        try {
            if(ReadmeWriter != null){
                ReadmeWriter.writeReadMeFile();
                ReadmeWriter.closefile();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mAct);
        alertDialogBuilder.setTitle("Log Information")
                .setMessage("Duration: " + SystemParameters.Duration + "sec\n"
                        + 	"SoundFile: "+SystemParameters.AudioCount+" records\n"
                        +	"InertialFile: "+SystemParameters.SensorCount+" records\n"
                ).setPositiveButton("OK",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int id) {}
        }).show();
    }

    public void InterruptLogging(int DeviceType){
        if( SystemParameters.isServiceRunning.get() && isTraining && DeviceType == DEVICE_HEADSET )//if Logging is running
            btTraining.performClick();
        else if( SystemParameters.isServiceRunning.get() && isTesting)
            btTesting.performClick();

    }

    /************************
     *  Local Database Related
     ***********************/
    private long SQLiteInsertNewLoggingRecord(String date, String subject, int stroke_num, String path, boolean is_testing, long offset, long match_id){
        DataListItem dlistDB = new DataListItem(mAct);
        long id = dlistDB.insert(date, subject, stroke_num, path, is_testing, offset, match_id);
        dlistDB.close();
        return id;
    }

    private void SQLiteInsertFreqModel(final List<HashMap.Entry<Float, Float>> freq_model, double threshold, long matching_training_id){
        MainFreqListItem mflistDB = new MainFreqListItem(mAct);
        mflistDB.insert(freq_model, threshold, matching_training_id);
        mflistDB.close();
    }


    /***********************/
    /** Algorithm Related **/
    /***********************/
    private void StartTrainingAlgo(final SoundWaveHandler sw){
        //split time array and data array
        final LinkedBlockingQueue<SoundWaveHandler.AudioData> ads = sw.getSampleData();
        float times[] = new float[ads.size()],
                vals[] = new float[ads.size()];

        Iterator<SoundWaveHandler.AudioData> it = ads.iterator();
        int count = 0;
        while(it.hasNext()){
            SoundWaveHandler.AudioData ad = it.next();
            times[count] = (float)ad.time;
            vals[count] = ad.data;
            count++;
        }

        //Find Training Window
        TrainingWindowFinder twFinder = new TrainingWindowFinder(FrequencyBandModel.FFT_LENGTH);
        List<Integer> wPos = twFinder.findWindowIndex(times, vals);
        SystemParameters.StrokeCount = wPos.size();


        // Find top K freq band
        fbm = new FrequencyBandModel();
        Vector<FrequencyBandModel.MainFreqInOneWindow> AllMainFreqBands = fbm.FindSpectrumMainFreqs(wPos, vals, SoundWaveHandler.SAMPLE_RATE);
        fbm.setTopKFreqBandTable(AllMainFreqBands, wPos.size());
        List<HashMap.Entry<Float, Float>> TopKMainFreqs = fbm.getTopKMainFreqBandTable();

        // Count Stroke Detector's Threshold
        double threshold = StrokeDetector.ComputeScoreThreshold(TopKMainFreqs, vals, SoundWaveHandler.SAMPLE_RATE, FrequencyBandModel.FFT_LENGTH);

        // SQLite
        if(TopKMainFreqs.size() > 0)
            SQLiteInsertFreqModel(TopKMainFreqs, threshold, SystemParameters.TrainingId);

        // Test File for All Spectrum Main Freq Bands
        LogFileWriter AllSpectrumMainFreqsTestWriter = new LogFileWriter("AllSpectrumMainFreqs.csv", LogFileWriter.OTHER_TYPE, LogFileWriter.TRAINING_TYPE);
        for(int i = 0; i < AllMainFreqBands.size(); i++){
            FrequencyBandModel.MainFreqInOneWindow mf = AllMainFreqBands.get(i);

            float [] sortedFreq = new float[mf.freqbands.length];
            float [] sortedPower = new float[mf.freqbands.length];
            for(int j = 0; j < mf.freqbands.length; j++){
                sortedFreq[j] = mf.freqbands[j].Freq;
                sortedPower[j] = mf.freqbands[j].Power;
            }
            try {
                AllSpectrumMainFreqsTestWriter.writeFreqPeakIndexFile(mf.stroke_num, mf.window_num, sortedFreq, sortedPower);
            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
            }
        }
        AllSpectrumMainFreqsTestWriter.closefile();

        //Test File for Top K Freq Band Table
        LogFileWriter TopKMainFreqTableWriter = new LogFileWriter("TopKMainFreqTable.csv", LogFileWriter.OTHER_TYPE, LogFileWriter.TRAINING_TYPE);
        for(int i = 0; i < TopKMainFreqs.size(); i++){
            HashMap.Entry<Float, Float> entry = TopKMainFreqs.get(i);
            float freq = entry.getKey();
            float val = entry.getValue();
            try {
                TopKMainFreqTableWriter.writeMainFreqPower(freq, val);
            } catch (IOException e) {
                Log.e(TAG,e.getMessage());
            }
        }
        TopKMainFreqTableWriter.closefile();
    }

    private void StartTestingAlgo(){
		/* 用來計算Window分數的模組 */
        SC = new ScoreComputing(sw);
        SC.StartComputingScore(fbm.getTopKMainFreqBandTable(), SoundWaveHandler.SAMPLE_RATE, FrequencyBandModel.FFT_LENGTH);
        SC.StartLogging();

		/* 用來偵測擊球的模組 */
        StrokeDetector SD = new StrokeDetector(mAct, SC);
        SD.StartStrokeDetector(bh);
    }

    /*******************/
    /** ListView Related **/
    /*******************/
    private void AddStrokeData(long StrokeTime, String StrokeType){
        StrokeItem sItem = new StrokeItem();
        sItem.stroke_time = StrokeTime;
        sItem.stroke_type = StrokeType;

        strokelist_dataset.add(sItem);
        Adapter.notifyDataSetChanged();
        lv_StrokeData.setSelection(lv_StrokeData.getCount() - 1);
    }

    private void ClearStrokeList(){
        strokelist_dataset.clear();
        Adapter.notifyDataSetChanged();
    }

    /*******************/
    /** Broadcast Related **/
    /*******************/
    private final BroadcastReceiver mStrokeTypeResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if( StrokeClassifier.ACTION_OUTPUT_RESULT_STATE.equals(action) ){
                SystemParameters.StrokeCount++;
                long stroke_time = intent.getLongExtra(StrokeClassifier.EXTRA_TIME, 0);
                String stroke_type = intent.getStringExtra(StrokeClassifier.EXTRA_TYPE);

                AddStrokeData(stroke_time, stroke_type);
            }
        }
    };
    private static IntentFilter makeStrokeTypeResultIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StrokeClassifier.ACTION_OUTPUT_RESULT_STATE);
        return intentFilter;
    }



    /**********************
     *	Pop Window Related
     * ********************/
    private void ShowWindowForSelectModel(){
        LayoutInflater inflater = (LayoutInflater)mAct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.modelpage, null);
        View main = mAct.findViewById(android.R.id.content);
        popupWindow = new PopupWindow(view);
        popupWindow.setWidth(main.getWidth() - (int) getResources().getDimension(R.dimen.activity_horizontal_margin) * 2);
        popupWindow.setHeight(main.getHeight() / 2);
        popupWindow.showAtLocation(main, Gravity.CENTER, 0, 0);
        popupWindow.setOutsideTouchable(true);

        Button bt_ok = (Button)view.findViewById(R.id.bt_model_ok);
        Button bt_cancel = (Button)view.findViewById(R.id.bt_model_cancel);
        bt_ok.setOnClickListener(ConfirmModel);
        bt_cancel.setOnClickListener(CancelWindow);

        final String[] models = CheckModelInRaw();
        dropdown = (Spinner)view.findViewById(R.id.sp_model_select);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, models);
        dropdown.setAdapter(adapter);
    }

    private Button.OnClickListener ConfirmModel = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(popupWindow != null){
                //Log.e(TAG,dropdown.getSelectedItem().toString());
                SystemParameters.ModelName = dropdown.getSelectedItem().toString();
                popupWindow.dismiss();
                popupWindow = null;

                ActiveLogging(LogFileWriter.TESTING_TYPE);
            }
        }
    };

    private Button.OnClickListener CancelWindow = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(popupWindow != null){
                popupWindow.dismiss();
                popupWindow = null;
            }
        }
    };

    private String[] CheckModelInRaw(){
        Field[] fields = R.raw.class.getFields();
        String[] result = new String[fields.length];

        for(int i=0; i < fields.length; i++)
            result[i] = fields[i].getName();

        return result;
    }
}
