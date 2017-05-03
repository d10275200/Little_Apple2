package com.nol.ivan.little_apple2.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.nol.ivan.little_apple2.R;
import com.nol.ivan.little_apple2.MainActivity;
import com.nol.ivan.little_apple2.adapter.StrokeItemAdapter;
import com.nol.ivan.little_apple2.algo.FrequencyBandModel;
import com.nol.ivan.little_apple2.algo.ScoreComputing;
import com.nol.ivan.little_apple2.algo.StrokeClassifier;
import com.nol.ivan.little_apple2.algo.StrokeDetector;
import com.nol.ivan.little_apple2.bt.devices.SoundWaveHandler;
import com.nol.ivan.little_apple2.file.LogFileWriter;
import com.nol.ivan.little_apple2.file.SystemParameters;
import com.nol.ivan.little_apple2.file.sqlite.DataListItem;
import com.nol.ivan.little_apple2.file.sqlite.MainFreqListItem;
import com.nol.ivan.little_apple2.file.sqlite.StrokeItem;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Ivan on 2016/12/23.
 */

public class MainFragment extends DialogFragment {

    /* Connect Related */
    public static Activity mAct;
//    private static SoundWaveHandler sw;
//    private static BeaconHandler bh;
    private final static String PAGETYPE_KEY = "LoggingFragment.PAGE_TYPE";
    /* Algorithm Related */
    private static FrequencyBandModel fbm = null;
    private static ScoreComputing SC = null;
    private static LogFileWriter ReadmeWriter;
    private static Button btTesting;
    private static Boolean isTesting = false;

    private PopupWindow popupWindow; // for select model
    private Spinner dropdown; // for select model
    public final static int TESTING_TYPE = 2;
    /* Stroke List */
    private static List<StrokeItem> strokelist_dataset = new ArrayList<>();
    private static ListView lv_StrokeData;
    private static StrokeItemAdapter Adapter;


    //球種Count Flag
    private int smash_count = 0;
    private int lob_count = 0;
    private int drive_count = 0;
    private int drop_count = 0;
    private int long_count = 0;
    private int netplay_count = 0;
    private int all_count = 0;
    private int net_kill_count = 0;
    private int flat_count = 0;

    //Type
    private TextView T_netplay;
    private TextView T_drive;
    private TextView T_lob;
    private TextView T_drop;
    private TextView T_smash;
    private TextView T_long;
    private TextView All;
    private TextView T_net_kill;
    private TextView T_flat;

    private final static String TAG = "MainFragment";
    public final static int KOALA_SCAN_PAGE_RESULT = 11;
    /* BT related */
    private BluetoothAdapter mBluetoothAdapter = null;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_OPEN_BTSETTING = 2;


    /* Sound Wave Related */
    public static Button btMicConnect;

    /* Beacon Connect Related */
    public static Button btKoalaConnect;
    private ProgressDialog WaitConnectDialog = null;
    private View view;


    Button btn_date;
    int year_x,month_x,day_x;
    static final int DILOG_ID = 0;

    public static TextView dateText;
    public static Button start_btn;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private int register_flag = 0;

//    public static MainFragment newInstance(Activity act, SoundWaveHandler s, BeaconHandler b, int type){
//        MainFragment lf = new MainFragment();
//
//        Bundle args = new Bundle();
//        args.putInt(PAGETYPE_KEY, type);
//        lf.setArguments(args);
//        mAct = act;
//        sw = s;
//        bh = b;
//
//        return lf;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);

        mAct = getActivity();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Main");

        //View & Event Initial
        initialViewandEvent();

        return view;
    }


    @Override
    public void onDestroy(){
//        if(register_flag == 1){
            mAct.unregisterReceiver(mStrokeTypeResultReceiver);
//        }
//        register_flag = 0;
        super.onDestroy();
    }


    private void initialViewandEvent(){
        //textview
        T_netplay = (TextView) view.findViewById(R.id.netplay_ball);
        T_lob = (TextView) view.findViewById(R.id.lob_ball);
        T_long = (TextView) view.findViewById(R.id.long_ball);
        T_drive = (TextView) view.findViewById(R.id.drive_ball);
        T_drop = (TextView) view.findViewById(R.id.drop_ball);
        T_smash = (TextView) view.findViewById(R.id.smash_ball);
        All = (TextView) view.findViewById(R.id.all_count);
        T_net_kill = (TextView) view.findViewById(R.id.net_kill_ball);
        T_flat = (TextView) view.findViewById(R.id.flat_ball);

        //Button
        btMicConnect = (Button) view.findViewById(R.id.bt_micconnect);
        btMicConnect.setOnClickListener(MicConnectListener);

        btTesting = (Button) view.findViewById(R.id.StartBtn);
        btTesting.setOnClickListener(BtnStartListener);

        btKoalaConnect = (Button) view.findViewById(R.id.bt_koalaconnect);
        btKoalaConnect.setOnClickListener(KoalaConnectListener);

        if(MainActivity.koala_con_flag == 1){
            btKoalaConnect.setBackground(getResources().getDrawable(R.drawable.koala_connect));
        }else if(MainActivity.koala_con_flag == 0){
            btKoalaConnect.setBackground(getResources().getDrawable(R.drawable.koala_disconnect));
        }

        if(MainActivity.mic_con_flag == 0){
            btMicConnect.setBackground(getResources().getDrawable(R.drawable.headset_disconnect));
        }else if (MainActivity.mic_con_flag == 1){
            btMicConnect.setBackground(getResources().getDrawable(R.drawable.headset_connect));
        }

//        if(MainActivity.test_flag == 1) {
            mAct.registerReceiver(mStrokeTypeResultReceiver, makeStrokeTypeResultIntentFilter());
//            register_flag = 1;
//        }


            T_drive.setText(String.valueOf(drive_count));
            T_drop.setText(String.valueOf(drop_count));
            T_lob.setText(String.valueOf(lob_count));
            T_long.setText(String.valueOf(long_count));
            T_netplay.setText(String.valueOf(netplay_count));
            T_smash.setText(String.valueOf(smash_count));
            All.setText(String.valueOf(all_count));
            T_net_kill.setText(String.valueOf(net_kill_count));
            T_flat.setText(String.valueOf(flat_count));

//            int type = getArguments().getInt(PAGETYPE_KEY);
//            if(type == TESTING_TYPE){
                lv_StrokeData = (ListView) view.findViewById(R.id.lv_realtime_showstroke);
                Adapter = new StrokeItemAdapter(mAct.getApplicationContext(), strokelist_dataset);
                lv_StrokeData.setAdapter(Adapter);
//            }
//            testing();
//        }
        //init Date
        Calendar calendar = Calendar.getInstance();
        int Year = calendar.get(Calendar.YEAR);
        int Month = calendar.get(Calendar.MONTH);
        int Day = calendar.get(Calendar.DAY_OF_MONTH);

        String format1 =  setDateFormat(Year, Month, Day);
//        dateText.setText(format1);
//        dateText.setTextSize(25);

//        editor.putInt("Year", Year);
//        editor.putInt("Month", Month);
//        editor.putInt("Day", Day);
//        editor.commit();

    }




    private String setDateFormat(int year,int monthOfYear,int dayOfMonth){
        return String.valueOf(year) + "-"
                + String.valueOf(monthOfYear + 1) + "-"
                + String.valueOf(dayOfMonth);
    }

    /**********************/
    /**    Date Button   **/
    /**********************/

//    private Button.OnClickListener DateButtonListener = new Button.OnClickListener(){
//        @Override
//        public void onClick(View view) {
////            PickDialog pickDialog = new PickDialog();
////            pickDialog.show(getActivity().getSupportFragmentManager(), "date_picker");
//
////            int Year = sharedPreferences.getInt("Year", 0);
////            int Month = sharedPreferences.getInt("Month", 0);
////            int Day = sharedPreferences.getInt("Day", 0);
////
////
////            Log.e(TAG, Year + "/" + (Month+1) + "/" + Day);
////            Log.e(TAG, "AAA" );
//
//
//            Calendar calendar = Calendar.getInstance();
//            int Year = calendar.get(Calendar.YEAR);
//            int Month = calendar.get(Calendar.MONTH);
//            int Day = calendar.get(Calendar.DAY_OF_MONTH);
//
//
//
//            new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
//                @Override
//                public void onDateSet(DatePicker view, int year, int month, int day) {
//                    String format = setDateFormat(year, month, day);
//                    dateText.setText(format);
////                    editor.putInt("Year", year);
////                    editor.putInt("Month", month);
////                    editor.putInt("Day", day);
////                    editor.commit();
//
//                }
//
//            }, Year, Month, Day).show();
//
//        }
//        private String setDateFormat(int year,int monthOfYear,int dayOfMonth){
//            return String.valueOf(year) + "-"
//                    + String.valueOf(monthOfYear + 1) + "-"
//                    + String.valueOf(dayOfMonth);
//        }
//    };


    /********************/
    /** Connecting Event **/
    /********************/
    private Button.OnClickListener MicConnectListener = new Button.OnClickListener() {
        public void onClick(View v) {
            MainActivity.MicScan(getActivity());

        }
    };

    private Button.OnClickListener KoalaConnectListener = new Button.OnClickListener() {
        public void onClick(View v) {
            MainActivity.KoalaScan(getActivity());

        }
    };


    private Button.OnClickListener BtnStartListener = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
//                ((MainActivity)getActivity()).BtnStart(getActivity());
            if(SQLiteGetFreqModelMax()){
                if(fbm != null && fbm.CheckModelHasTrained()){
                    if(SystemParameters.IsBtHeadsetReady && SystemParameters.IsKoalaReady && !isTesting){
                        if(SystemParameters.ModelName.equals(""))
                            Toast.makeText(mAct, "Select your model!!!", Toast.LENGTH_SHORT).show();
                        else{
                            smash_count = 0;
                            lob_count = 0;
                            drive_count = 0;
                            drop_count = 0;
                            long_count = 0;
                            netplay_count = 0;
                            all_count = 0;
                            net_kill_count = 0;
                            flat_count = 0;

                            T_drive.setText(String.valueOf(drive_count));
                            T_drop.setText(String.valueOf(drop_count));
                            T_lob.setText(String.valueOf(lob_count));
                            T_long.setText(String.valueOf(long_count));
                            T_netplay.setText(String.valueOf(netplay_count));
                            T_smash.setText(String.valueOf(smash_count));
                            All.setText(String.valueOf(all_count));
                            T_net_kill.setText(String.valueOf(net_kill_count));
                            T_flat.setText(String.valueOf(flat_count));

                            ActiveLogging(LogFileWriter.TESTING_TYPE);
                        }
                    }
                    else if(isTesting)
                        StopLogging();
                    else
                        Toast.makeText(mAct,"You have to connect bt headset and koala.",Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(mAct, "You must train your racket first..", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(mAct, "You must connect bt headset and koala, train your racket first.", Toast.LENGTH_SHORT).show();
            }

        }
    };

    private void ActiveLogging(final int LogType){
        //SystemParameters Initial
        SystemParameters.initializeSystemParameters();

        //UI Button Control
        if(LogType == LogFileWriter.TESTING_TYPE) {
            btTesting.setText(R.string.Testing_State);
            isTesting = true;
            ClearStrokeList();
        }
//        else{
//            btTraining.setText(R.string.Training_State);
//            isTraining = true;
//        }

        //Initial Log File
        ReadmeWriter = new LogFileWriter("Readme.txt", LogFileWriter.README_TYPE, LogType);

        new Thread(){
            @Override
            public void run() {
                // Trigger Sensor to Ready (wait isServiceRunning become true)
                MainActivity.sw.startRecording(LogType);
                if( isTesting ){
                    MainActivity.bh.startRecording(LogType);
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

    private void StartTestingAlgo(){
		/* 用來計算Window分數的模組 */
        SC = new ScoreComputing(MainActivity.sw);
        SC.StartComputingScore(fbm.getTopKMainFreqBandTable(), SoundWaveHandler.SAMPLE_RATE, FrequencyBandModel.FFT_LENGTH);
        SC.StartLogging();

		/* 用來偵測擊球的模組 */
        StrokeDetector SD = new StrokeDetector(mAct, SC);
        SD.StartStrokeDetector(MainActivity.bh);
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

                MainActivity.sw.stopRecording();
                if( isTesting )MainActivity.bh.stopRecording();


                //Wait log file write done
                if( MainActivity.sw != null ) while(MainActivity.sw.isWrittingAudioDataLog.get());
                if( SC != null ) while(SC.isWrittingWindowScore.get());
                if( MainActivity.bh != null ) while(MainActivity.bh.isWrittingSensorDataLog.get());

//                if(isTraining) StartTrainingAlgo(sw);

                //Show UI
                mAct.runOnUiThread(new Runnable() {
                    public void run() {
                        showLogInformationDialog();

                        //UI Button Control
                        if (isTesting) {
                            btTesting.setText(R.string.Not_Testing_State);
                            isTesting = false;
                        }
//                        else if (isTraining) {
//                            btTraining.setText(R.string.Not_Training_State);
//                            isTraining = false;
//                        }
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
   /**********************
     *  Local Database Related *
     ***********************/
    private long SQLiteInsertNewLoggingRecord(String date, String subject, int stroke_num, String path, boolean is_testing, long offset, long match_id){
        DataListItem dlistDB = new DataListItem(mAct);
        long id = dlistDB.insert(date, subject, stroke_num, path, is_testing, offset, match_id);
        dlistDB.close();
        return id;
    }

//    private void SQLiteInsertFreqModel(final List<HashMap.Entry<Float, Float>> freq_model, double threshold, long matching_training_id){
//        MainFreqListItem mflistDB = new MainFreqListItem(mAct);
//        mflistDB.insert(freq_model, threshold, matching_training_id);
//        mflistDB.close();
//    }

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

                Log.e(TAG,stroke_type);

                if(stroke_type.equals("netplay")){
                    netplay_count++;
                    T_netplay.setText(String.valueOf(netplay_count));
                }else if(stroke_type.equals("lob")){
                    lob_count++;
                    T_lob.setText(String.valueOf(lob_count));
                }else if(stroke_type.equals("drive")){
                    drive_count++;
                    T_drive.setText(String.valueOf(drive_count));
                }else if(stroke_type.equals("drop")){
                    drop_count++;
                    T_drop.setText(String.valueOf(drop_count));
                }else if(stroke_type.equals("long")){
                    long_count++;
                    T_long.setText(String.valueOf(long_count));
                }else if(stroke_type.equals("smash")){
                    smash_count++;
                    T_smash.setText(String.valueOf(smash_count));
                }else if(stroke_type.equals("net_kill")){
                    net_kill_count++;
                    T_net_kill.setText(String.valueOf(net_kill_count));
                }else if(stroke_type.equals("flat")){
                    flat_count++;
                    T_flat.setText(String.valueOf(flat_count));
                }

                all_count = netplay_count + lob_count + long_count + drive_count + drop_count + smash_count + flat_count + net_kill_count;
                All.setText(String.valueOf(all_count));

                AddStrokeData(stroke_time, stroke_type);
            }
        }
    };
    private static IntentFilter makeStrokeTypeResultIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StrokeClassifier.ACTION_OUTPUT_RESULT_STATE);
        return intentFilter;
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
                Log.e(TAG,dropdown.getSelectedItem().toString());
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

    private boolean SQLiteGetFreqModelMax() {
        MainFreqListItem mFreqList = new MainFreqListItem(getActivity());

        if(mFreqList.Check()){
            MainFreqListItem.FreqModel mFreqM;
            mFreqM = mFreqList.GetFreqModelMax();

            fbm = new FrequencyBandModel();
            HashMap<Float, Float> MainFreqMap = new HashMap<Float, Float>();
            for (int i = 0; i < 5; i++) {
                MainFreqMap.put((float)(mFreqM.freqs[i]), (float)(mFreqM.vals[i]));
                Log.e("TAG", String.valueOf(mFreqM.freqs[i]) + " " + String.valueOf(mFreqM.vals[i]));
            }
            Log.e("TAG", String.valueOf(MainFreqMap));
            fbm.setTopKMainFreqBandTableFromSQL(MainFreqMap);

            return true;
        }else{
            Log.e("TAG", " read fail");
            return false;
        }
    }
}
