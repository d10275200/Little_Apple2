package com.nol.ivan.little_apple2;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.nol.ivan.little_apple2.Fragment.HistoryFragment;
import com.nol.ivan.little_apple2.adapter.StrokeItemAdapter;
import com.nol.ivan.little_apple2.bt.devices.SoundWaveHandler;
import com.nol.ivan.little_apple2.file.sqlite.StrokeItem;
import com.nol.ivan.little_apple2.file.sqlite.StrokeListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan on 2017/2/14.
 */

public class Activity_Game extends Activity {
    private final static String TAG = Activity_Game.class.getSimpleName();


    private long DataID = -1;
    private String DataPath;

    // Audio Data
    private short[] raw_value;
    private long offset;
    private final float deltaT = (1 / (float) SoundWaveHandler.SAMPLE_RATE) * 1000;
    private final static long SHOWRANGE = 1000; // show 1 sec data

    // Stroke
    private List<StrokeItem> strokelist_dataset = new ArrayList<>();
    private ListView lv_StrokeData;
    private StrokeItemAdapter Adapter;
    private List<StrokeInfo> StrokeTable = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.strokelist);

        Bundle extras = getIntent().getExtras();
        initialViewandEvent();
        if (extras != null) {
            DataID = extras.getLong(HistoryFragment.EXTRA_ID);
            DataPath = extras.getString(HistoryFragment.EXTRA_PATH);
            offset = extras.getLong(HistoryFragment.EXTRA_OFFSET);
            Prepare();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private void initialViewandEvent(){
        lv_StrokeData = (ListView) findViewById(R.id.list_each_stroke_info);
        Adapter = new StrokeItemAdapter(Activity_Game.this, strokelist_dataset);
        lv_StrokeData.setAdapter(Adapter);
//        lv_StrokeData.setOnItemClickListener(ListClickListener);
    }



    private void Prepare(){
//        final ProgressDialog dialog = ProgressDialog.show(Activity_Game.this, "請稍後", "讀取音訊資料中", true);
        new Thread() {
            @Override
            public void run(){
                strokelist_dataset.clear();
                strokelist_dataset.addAll(SQLiteGetStrokeById(DataID));
//                SetAudioSamplesByPath(DataPath);
//                BuildStrokeTable(strokelist_dataset);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Adapter.notifyDataSetChanged();
//                        dialog.dismiss();
//                    }
//                });
            }
        }.start();

    }

//    private final double getAudioTime(final int index){
//        return offset + deltaT * index;
//    }
//    private final double getAudioValue(final int index){
//        return (double)raw_value[index] / 32768;
//    }
//    private final int getAudioSize() {
//        return raw_value.length;
//    }

    /***************
     *    File Related
     * ***************/
//    private void SetAudioSamplesByPath(final String path) {
//        // Read Wav File, store data
//        try {
//            WavReader wr = new WavReader(new FileInputStream(path + "Sound.wav"));
//            raw_value = wr.getShortSamples();
//        } catch (FileNotFoundException e) {
//            Log.e(TAG, e.getMessage());
//        }
//    }



    /**********************
     *      SQLite Related
     ***********************/
    public List< StrokeItem> SQLiteGetStrokeById(long id){
        StrokeListItem slistDB = new StrokeListItem(Activity_Game.this);
        List< StrokeItem> result = slistDB.GetStrokesInOneTestingFile(id);
        slistDB.close();
        return result;
    }

    /******************
     *      Stroke Table
     * ******************/
    private class StrokeInfo{
        public int left_idx;
        public int right_idx;
        public int block_start_idx;
        public StrokeInfo(){
            left_idx = -1;
            right_idx = -1;
            block_start_idx = -1;
        }
    }

//    private void BuildStrokeTable(final List<StrokeItem> dataset){
//        int curStrokeCount = 0;
//        int left = -1, right;
//        for(int i = 0; i < getAudioSize(); i++){
//            if(curStrokeCount >= dataset.size())
//                break;
//
//            long curStrokeTime = dataset.get(curStrokeCount).stroke_time;
//            if(left == -1 && getAudioTime(i) >= curStrokeTime-SHOWRANGE/2) {
//                left = i;
//                right = left;
//                while(right < getAudioSize() &&  getAudioTime(right) < curStrokeTime+SHOWRANGE/2)
//                    right++;
//
//                StrokeInfo info = new StrokeInfo();
//                info.left_idx = left;
//                info.right_idx = right;
//
//                int difference = FrequencyBandModel.FFT_LENGTH - left%FrequencyBandModel.FFT_LENGTH;
//                info.block_start_idx = difference;
//
//                StrokeTable.add(info);
//
//                left = -1;
//                curStrokeCount++;
//            }
//        }
//    }
}
