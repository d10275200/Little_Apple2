package com.nol.ivan.little_apple2.Fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.nol.ivan.little_apple2.Activity_Game;
import com.nol.ivan.little_apple2.R;
import com.nol.ivan.little_apple2.adapter.DataItemAdapter;
import com.nol.ivan.little_apple2.file.sqlite.DataListItem;
import com.nol.ivan.little_apple2.file.sqlite.StrokeListItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Ivan on 2017/2/7.
 */

public class HistoryFragment extends Fragment {

    private ArrayList<String> GetCountOfStrokeType = new ArrayList<String>();

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    //Stroke
    private ListView His_GameData;
    private DataItemAdapter Adapter;
    private List<DataListItem.DataItem> stroke_dataset = new ArrayList<>();
    private TextView TextDate_his;
    private Button btn_date;

    private View view;

    String format;
    String format1;

    public final static String EXTRA_ID = "DataListPage.EXTRA_ID";
    public final static String EXTRA_PATH = "DataListPage.EXTRA_PATH";
    public final static String EXTRA_OFFSET = "DataListPage.EXTRA_OFFSET";

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();

        view = inflater.inflate(R.layout.fragment_history, container, false);

        setDate();

        return view;
    }


    private void setDate(){
        //textview
        T_netplay = (TextView) view.findViewById(R.id.netplay_ball_his);
        T_lob = (TextView) view.findViewById(R.id.lob_ball_his);
        T_long = (TextView) view.findViewById(R.id.long_ball_his);
        T_drive = (TextView) view.findViewById(R.id.drive_ball_his);
        T_drop = (TextView) view.findViewById(R.id.drop_ball_his);
        T_smash = (TextView) view.findViewById(R.id.smash_ball_his);
//        T_net_kill = (TextView) view.findViewById(R.id.net_kill_ball_his);
//        T_flat = (TextView) view.findViewById(R.id.flat_ball_his);

        //init Date
        Calendar calendar = Calendar.getInstance();
        int Year = calendar.get(Calendar.YEAR);
        int Month = calendar.get(Calendar.MONTH);
        int Day = calendar.get(Calendar.DAY_OF_MONTH);

        format1 =  setDateFormat(Year, Month, Day);

        TextDate_his = (TextView) view.findViewById(R.id.Date_View_His);
        btn_date = (Button) view.findViewById(R.id.DateButton);

        His_GameData = (ListView) view.findViewById(R.id.history_listview);

        stroke_dataset = SQLiteGetAllLoggingRecord(format1);
        Adapter = new DataItemAdapter(getActivity(),stroke_dataset);
        His_GameData.setAdapter(Adapter);
        //短按顯示資料
        His_GameData.setOnItemClickListener(ListClickListener);

        //長按刪資料
        His_GameData.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View view,
                                           final int position, long id) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("want to delele?")
                        .setMessage("Want to delete this item?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DataListItem.DataItem d_item = stroke_dataset.get(position);
                                Log.e("TAG", String.valueOf(d_item.path));

                                //刪list
                                Adapter.removeItem(position);
                                Adapter.notifyDataSetChanged();
                                Log.e("TAG", "Delete ID = " + DataListItem.Date_ID_List.get(position).toString());

                                //刪SQLite
                                DataListItem list = new DataListItem(getActivity());
                                boolean flag;
                                flag = list.delete(Long.valueOf(DataListItem.Date_ID_List.get(position)));
                                Log.e("TAG", "Delete Flag = " + String.valueOf(flag));

                                //刪local檔案
                                String path[] = d_item.path.split("/");
                                Log.e("TAG", path[4]);
                                Log.e("TAG", path[5]);
                                File dir = new File("/sdcard/" + path[4] + "/" + path[5]);
//                                Log.e("TAG", "/sdcard/" + path[4] + "/" + path[5]);
                                deleteDirectory(dir);

                                DataListItem.Date_ID_List.remove(position);
                                Log.e("TAG", "exist ID = " + DataListItem.Date_ID_List.toString());
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                return false;
            }
        });

        btn_date.setOnClickListener(DateButtonListener);
        TextDate_his.setText(format1);

        //initail ball_count
        T_drive.setText(String.valueOf(drive_count));
        T_drop.setText(String.valueOf(drop_count));
        T_lob.setText(String.valueOf(lob_count));
        T_long.setText(String.valueOf(long_count));
        T_netplay.setText(String.valueOf(netplay_count));
        T_smash.setText(String.valueOf(smash_count));
//        All.setText(String.valueOf(all_count));
//        T_net_kill.setText(String.valueOf(net_kill_count));
//        T_flat.setText(String.valueOf(flat_count));


    }

    private ListView.OnItemClickListener ListClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            DataListItem.DataItem d_item = stroke_dataset.get(arg2);
            File dir = new File(d_item.path);
            if(dir.isDirectory()) {
                if (d_item.is_testing == 0) {
//                    Intent i = new Intent(getActivity(), ShowTrainingData.class);
//                    i.putExtra(EXTRA_ID, d_item.id);
//                    i.putExtra(EXTRA_PATH, d_item.path);
//                    i.putExtra(EXTRA_OFFSET, d_item.offset);
//                    startActivity(i);
                } else {
                    Intent i = new Intent(getActivity(), Activity_Game.class);
                    i.putExtra(EXTRA_ID, d_item.id);
                    i.putExtra(EXTRA_PATH, d_item.path);
                    i.putExtra(EXTRA_OFFSET, d_item.offset);
                    Log.e("TAG", String.valueOf(d_item.id));
                    Log.e("TAG", String.valueOf(d_item.subject));
                    Log.e("TAG", String.valueOf(d_item.path));
                    Log.e("TAG", String.valueOf(d_item.offset));
                    startActivity(i);
                }
            }else{
                new AlertDialog.Builder(getActivity())
                        .setTitle("檔案遺失")
                        .setMessage("無法進行資料分析")
                        .setNegativeButton("確認",null)
                        .show();
            }
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


            Calendar calendar = Calendar.getInstance();
            int Year = calendar.get(Calendar.YEAR);
            int Month = calendar.get(Calendar.MONTH);
            int Day = calendar.get(Calendar.DAY_OF_MONTH);


            new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    //initial
                    smash_count = 0;
                    lob_count = 0;
                    drive_count = 0;
                    drop_count = 0;
                    long_count = 0;
                    netplay_count = 0;
//                    all_count = 0;
//                    net_kill_count = 0;
//                    flat_count = 0;

                    T_drive.setText(String.valueOf(drive_count));
                    T_drop.setText(String.valueOf(drop_count));
                    T_lob.setText(String.valueOf(lob_count));
                    T_long.setText(String.valueOf(long_count));
                    T_netplay.setText(String.valueOf(netplay_count));
                    T_smash.setText(String.valueOf(smash_count));
//                    T_net_kill.setText(String.valueOf(net_kill_count));
//                    T_flat.setText(String.valueOf(flat_count));


                    DataListItem.Date_ID_List.clear();
                    format = setDateFormat(year, month, day);
                    TextDate_his.setText(format);
//                    editor.putInt("Year", year);
//                    editor.putInt("Month", month);
//                    editor.putInt("Day", day);
//                    editor.commit();

                    stroke_dataset = SQLiteGetAllLoggingRecord(format);
                    Adapter = new DataItemAdapter(getActivity(),stroke_dataset);
                    His_GameData.setAdapter(Adapter);
                    His_GameData.setOnItemClickListener(ListClickListener);

                    for(int i = 0 ; i < DataListItem.Date_ID_List.size() ; i++){
                        GetCountOfStrokeType = SQLiteGetStrokeById(Long.valueOf(DataListItem.Date_ID_List.get(i)));
                        for(int j = 0 ; j < GetCountOfStrokeType.size() ; j++){
                            if(GetCountOfStrokeType.get(j).equals("netplay")){
                                netplay_count++;
                                T_netplay.setText(String.valueOf(netplay_count));
                            }else if(GetCountOfStrokeType.get(j).equals("lob")){
                                lob_count++;
                                T_lob.setText(String.valueOf(lob_count));
                            }else if(GetCountOfStrokeType.get(j).equals("drive")){
                                drive_count++;
                                T_drive.setText(String.valueOf(drive_count));
                            }else if(GetCountOfStrokeType.get(j).equals("drop")){
                                drop_count++;
                                T_drop.setText(String.valueOf(drop_count));
                            }else if(GetCountOfStrokeType.get(j).equals("long")){
                                long_count++;
                                T_long.setText(String.valueOf(long_count));
                            }else if(GetCountOfStrokeType.get(j).equals("smash")){
                                smash_count++;
                                T_smash.setText(String.valueOf(smash_count));
                            }
//                            else if(GetCountOfStrokeType.get(j).equals("net_kill")){
//                                net_kill_count++;
//                                T_net_kill.setText(String.valueOf(net_kill_count));
//                            }else if(GetCountOfStrokeType.get(j).equals("flat")){
//                                flat_count++;
//                                T_flat.setText(String.valueOf(flat_count));
//                            }



                        }
                    }
                    GetCountOfStrokeType.clear();

                }

            }, Year, Month, Day).show();

        }
        private String setDateFormat(int year,int monthOfYear,int dayOfMonth){
            return String.valueOf(year) + "-"
                    + String.valueOf(monthOfYear + 1) + "-"
                    + String.valueOf(dayOfMonth);
        }
    };

    /*********************
     *    Local Database Related
     ***********************/
    private final List<DataListItem.DataItem> SQLiteGetAllLoggingRecord(String date){
        DataListItem dlistDB = new DataListItem(getActivity());
//        List<DataListItem.DataItem> list = dlistDB.getALLTestingData();
        List<DataListItem.DataItem> list = dlistDB.getDateData(date);
       // Log.e("TAG", DataListItem.Date_ID_List.get(1).toString());

        dlistDB.close();
        return list;
    }

    public ArrayList<String> SQLiteGetStrokeById(long id){
        StrokeListItem slistDB = new StrokeListItem(getActivity());
        ArrayList<String> result = new ArrayList<String>();
        result = slistDB.GetCountOfStrokeType(id);
        slistDB.close();
        return result;
    }


    /*********************
     *    Delete Folder
     ***********************/
    public static boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }
}
