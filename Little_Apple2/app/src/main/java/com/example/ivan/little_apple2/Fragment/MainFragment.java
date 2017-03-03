package com.example.ivan.little_apple2.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivan.little_apple2.R;
import com.example.ivan.little_apple2.MainActivity;
import com.example.ivan.little_apple2.TypeFragment;

import java.util.Calendar;


/**
 * Created by Ivan on 2016/12/23.
 */

public class MainFragment extends DialogFragment {


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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_main, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Fragment Main");

        //View & Event Initial
        initialViewandEvent();

        return view;
    }





    private void initialViewandEvent(){
        //Button
        btMicConnect = (Button) view.findViewById(R.id.bt_micconnect);
        btKoalaConnect = (Button) view.findViewById(R.id.bt_koalaconnect);
//        btn_date = (Button) view.findViewById(R.id.DateButton);
        start_btn = (Button) view.findViewById(R.id.StartBtn);
//        dateText = (TextView) view.findViewById(R.id.dateview);

        start_btn.setOnClickListener(BtnStartListener);
        btMicConnect.setOnClickListener(MicConnectListener);
        btKoalaConnect.setOnClickListener(KoalaConnectListener);
//        btn_date.setOnClickListener(DateButtonListener);

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

//            if( !SystemParameters.IsBtHeadsetReady ) {
//                Intent BTSettingIntent = new Intent(Intent.ACTION_MAIN, null);
//                BTSettingIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//                ComponentName cn = new ComponentName("com.android.settings",
//                        "com.android.settings.bluetooth.BluetoothSettings");
//                BTSettingIntent.setComponent(cn);
//                startActivityForResult(BTSettingIntent, REQUEST_OPEN_BTSETTING);
//            }
//            else
//                sw.getService().DisconnectAllScoBTHeadset();
        }
    };

    private Button.OnClickListener KoalaConnectListener = new Button.OnClickListener() {
        public void onClick(View v) {
            MainActivity.KoalaScan(getActivity());

//            if(SystemParameters.IsKoalaReady)
//                bh.DisconnectToKoala();
//            else{
//                Intent i = new Intent(MainActivity.this, KoalaScan.class);
//                startActivityForResult(i, KOALA_SCAN_PAGE_RESULT);
//            }
        }
    };

    //BUTTON START
    private Button.OnClickListener BtnStartListener = new Button.OnClickListener(){
        @Override
        public void onClick(View view) {
//            MainActivity.BtnStart(getActivity());

            ((MainActivity)getActivity()).BtnStart(getActivity());

        }
    };

}
