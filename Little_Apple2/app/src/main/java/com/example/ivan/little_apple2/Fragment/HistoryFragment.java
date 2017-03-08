package com.example.ivan.little_apple2.Fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ivan.little_apple2.R;
import com.example.ivan.little_apple2.adapter.StrokeItemAdapter;
import com.example.ivan.little_apple2.bt.devices.SoundWaveHandler;
import com.example.ivan.little_apple2.file.sqlite.StrokeItem;
import com.example.ivan.little_apple2.file.sqlite.StrokeListItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Ivan on 2017/2/7.
 */

public class HistoryFragment extends Fragment {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //Stroke
    ListView His_GameData;
    private List<StrokeItem> strokelist_dataset = new ArrayList<>();
    private StrokeItemAdapter Adapter;
    private List<StrokeInfo> StrokeTable = new ArrayList<>();

    TextView TextDate_his;
    Button btn_date;

    private View view;

    // Audio Data
    private short[] raw_value;
    private long offset;
    private final float deltaT = (1 / (float) SoundWaveHandler.SAMPLE_RATE) * 1000;
    private final static long SHOWRANGE = 1000; // show 1 sec data




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();

        view = inflater.inflate(R.layout.fragment_history, container, false);


        setDate();

        return view;
    }


    private void setDate(){

        TextDate_his = (TextView) view.findViewById(R.id.Date_View_His);
        btn_date = (Button) view.findViewById(R.id.DateButton);

        His_GameData = (ListView) view.findViewById(R.id.history_listview);
        Adapter = new StrokeItemAdapter(getActivity(), strokelist_dataset);
        His_GameData.setAdapter(Adapter);
        His_GameData.setOnItemClickListener(ListClickListener);

        btn_date.setOnClickListener(DateButtonListener);



        //init Date
        Calendar calendar = Calendar.getInstance();
        int Year = calendar.get(Calendar.YEAR);
        int Month = calendar.get(Calendar.MONTH);
        int Day = calendar.get(Calendar.DAY_OF_MONTH);

        String format1 =  setDateFormat(Year, Month, Day);
        TextDate_his.setText(format1);


    }

    private ListView.OnItemClickListener ListClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            StrokeItem slist_item = strokelist_dataset.get(arg2);
            StrokeInfo info = StrokeTable.get(arg2);

            //int len = info.right_idx-info.left_idx+1;
//            double[] audio_time = new double[len];
//            double[] audio_val = new double[len];
//            for(int i = 0; i < len; i++){
//                audio_time[i] = getAudioTime(info.left_idx+i);
//                audio_val[i] = getAudioValue(info.left_idx+i);
//            }

//            Intent i = new Intent(StrokeListPage.this, ShowTestingData.class);
//            i.putExtra(EXTRA_ID, DataID);
//            i.putExtra(EXTRA_STROKETIME, slist_item.stroke_time);
//            i.putExtra(EXTRA_AUDIODATA_TIME, audio_time);
//            i.putExtra(EXTRA_AUDIODATA_VALUE, audio_val);
//            i.putExtra(EXTRA_AUDIODATA_BLOCKSTARTIDX, info.block_start_idx);

            //startActivity(i);
        }
    };


    private String setDateFormat(int year,int monthOfYear,int dayOfMonth){
        return String.valueOf(year) + "-"
                + String.valueOf(monthOfYear + 1) + "-"
                + String.valueOf(dayOfMonth);
    }

    private Button.OnClickListener DateButtonListener = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
//            PickDialog pickDialog = new PickDialog();
//            pickDialog.show(getActivity().getSupportFragmentManager(), "date_picker");
//
//            int Year = sharedPreferences.getInt("Year", 0);
//            int Month = sharedPreferences.getInt("Month", 0);
//            int Day = sharedPreferences.getInt("Day", 0);
//
//
//            Log.e(TAG, Year + "/" + (Month+1) + "/" + Day);
//            Log.e(TAG, "AAA" );


            Calendar calendar = Calendar.getInstance();
            int Year = calendar.get(Calendar.YEAR);
            int Month = calendar.get(Calendar.MONTH);
            int Day = calendar.get(Calendar.DAY_OF_MONTH);



            new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    String format = setDateFormat(year, month, day);
                    TextDate_his.setText(format);
//                    editor.putInt("Year", year);
//                    editor.putInt("Month", month);
//                    editor.putInt("Day", day);
//                    editor.commit();

                }

            }, Year, Month, Day).show();

        }
        private String setDateFormat(int year,int monthOfYear,int dayOfMonth){
            return String.valueOf(year) + "-"
                    + String.valueOf(monthOfYear + 1) + "-"
                    + String.valueOf(dayOfMonth);
        }
    };


//    private final double getAudioTime(final int index){
//        return offset + deltaT * index;
//    }
//    private final double getAudioValue(final int index){
//        return (double)raw_value[index] / 32768;
//    }
//    private final int getAudioSize() {
//        return raw_value.length;
//    }

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
