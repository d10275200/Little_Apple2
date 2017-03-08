package com.example.ivan.little_apple2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.result.UserProfile;
import com.example.ivan.little_apple2.Fragment.HistoryFragment;
import com.example.ivan.little_apple2.Fragment.MainFragment;
import com.example.ivan.little_apple2.Fragment.ProfileFragment;
import com.example.ivan.little_apple2.Fragment.ReadmeFragment;
import com.example.ivan.little_apple2.Fragment.TabFragment;
import com.example.ivan.little_apple2.Fragment.TaskFragment;
import com.example.ivan.little_apple2.Network.VolleyController;
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
import com.example.ivan.little_apple2.util.CredentialsManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private TextView contentView;
    private ArrayList<String> menuLists;
    private ArrayAdapter<String> adapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    ActionBar actionBar;
    private int navItemId;

    private static Activity activity_one;

    private UserProfile mUserProfile;
    private Auth0 mAuth0;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private final static String TAG = "MainActivity";
    public final static int KOALA_SCAN_PAGE_RESULT = 11;

    /* BT related */
    private BluetoothAdapter mBluetoothAdapter = null;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_OPEN_BTSETTING = 2;

    /* Sound Wave Related */
    private static SoundWaveHandler sw = null;
    private Button btMicConnect;
    public static int mic_con_flag = 0;

    /* Beacon Connect Related */
    private static BeaconHandler bh = null;
    public static int koala_con_flag = 0;
    private Button btKoalaConnect;
    private ProgressDialog WaitConnectDialog = null;

    static final int DILOG_ID = 0;

    private int Calibration_flag = 0;
    private TypeFragment curFragment = null;
    private MainFragment main = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPreferences = this.getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        editor = sharedPreferences.edit();

        activity_one = MainActivity.this;

        //開啟全螢幕
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mAuth0 = new Auth0(getString(R.string.auth0_client_id), getString(R.string.auth0_domain));
        // The process to reclaim an UserProfile is preceded by an Authentication call.
        AuthenticationAPIClient aClient = new AuthenticationAPIClient(mAuth0);
        aClient.tokenInfo(VolleyController.getInstance().getUserCredentials().getIdToken())
                .start(new BaseCallback<UserProfile, AuthenticationException>() {
                    @Override
                    public void onSuccess(final UserProfile payload) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                mUserProfile = payload;
                                Log.d("TAG",payload.getId());
                                editor.putString("ID", payload.getId());
                                editor.commit();
                                //Toast.makeText(MainActivity.this, payload.getId(), Toast.LENGTH_SHORT).show();

                                RequestQueue mQueue = Volley.newRequestQueue(MainActivity.this);
                                StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://140.113.203.226/~Ivan/GetLittleAppleUser.php",  new Response.Listener<String>() {

                                    @Override
                                    public void onResponse(String response) {
                                        if(response.equals("fail")){

                                            Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                                            create_write();
                                            //Create_User(payload.getId());
                                            //  Toast.makeText(MainActivity.this, "create user ", Toast.LENGTH_SHORT).show();
                                        }else{
                                            //  Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                                            //  Toast.makeText(MainActivity.this, "user already", Toast.LENGTH_SHORT).show();
                                            String r[] = response.split("_");
                                            Log.v("TAG", r[0]);
                                            editor.putString("Name", r[0]);
                                            editor.putString("Weight", r[1]);
                                            editor.putString("Height", r[2]);
                                            editor.putString("Age", r[3]);
                                            editor.commit();

                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("TAG", error.getMessage(), error);
                                    }
                                }) {
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        Map<String, String> map = new HashMap<String, String>();
                                        map.put("ID", payload.getId());
                                        return map;
                                    }
                                };
                                mQueue.add(stringRequest);
                            }
                        });
                    }

                    @Override
                    public void onFailure(AuthenticationException error) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, "Profile Request Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_test);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //contentView = (TextView) findViewById(R.id.content_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);

        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
//                Toast.makeText(MainActivity.this, menuItem.getTitle() + " pressed", Toast.LENGTH_LONG).show();

//                Toast.makeText(MainActivity.this, menuItem.getItemId() + " pressed", Toast.LENGTH_LONG).show();
                //contentView.setText(menuItem.getTitle());
                switch(menuItem.getItemId()){
                    case R.id.main:
                        menuItem.setChecked(true);
                        setFragment(6);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.task:
                        menuItem.setChecked(true);
                        setFragment(2);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.profile:
                        menuItem.setChecked(true);
                        setFragment(3);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.Sampling_Voice:
                        menuItem.setChecked(true);
                        setFragment(4);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.read_me:
                        menuItem.setChecked(true);
                        setFragment(5);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case  R.id.reset_calibration:
                        menuItem.setChecked(true);
                        if(Calibration_flag == 1){
                            SystemParameters.initializeSystemParameters();
                            for(int i = 0; i < 2; i++)
                                ActiveCalibration(i);
                        }else if (Calibration_flag == 0){
                            Toast.makeText(MainActivity.this, "Please connect Koala first", Toast.LENGTH_LONG).show();
                        }
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.logout:
                        menuItem.setChecked(true);
                        logout();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        return true;

                }


                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                return false;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle( this, mDrawerLayout, toolbar, R.string.drawer_open , R.string.drawer_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                super .onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super .onDrawerOpened(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if(null != savedInstanceState){
            navItemId = savedInstanceState.getInt(NAV_ITEM_ID, R.id.main);
        }
        else{
            navItemId = R.id.main;
        }
        navigateTo(view.getMenu().findItem(navItemId));

        setFragment(6);



        //Main
        //Bluetooth Initial
        initialBTManager();

        //View & Event Initial
        //initialViewandEvent();





    }


    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        if (sw != null){
            sw.deleteObject();
            sw = null;
        }

        if(bh != null){
            bh.deleteObject();
            bh = null;
        }

        unregisterReceiver(mSoundWaveHandlerStateUpdateReceiver);
        unregisterReceiver(mKoalaStateUpdateReceiver);
//        Intent intent = new Intent(MainActivity.this,LoginPage.class);
//        stopService(intent);

        //System.exit(0);
        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                Log.e(TAG,"Bluetooth is not enabled.");
                finish();
                return;
            }else if(resultCode == Activity.RESULT_OK){}
        }else if(requestCode == REQUEST_OPEN_BTSETTING){}

        else if(requestCode == KOALA_SCAN_PAGE_RESULT){
            if(resultCode == Activity.RESULT_OK && bh != null){
                final String clickedMacAddress = data.getExtras().getString(KoalaScan.macAddress);
                final String clickedDeviceName = data.getExtras().getString(KoalaScan.deviceName);
                //CurKoalaDevice = clickedDeviceName + "-" +clickedMacAddress;
                bh.ConnectToKoala(clickedMacAddress);

                WaitConnectDialog = ProgressDialog.show(this, "連線中", "請稍後...", true);
                WaitConnectDialog.show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        if (WaitConnectDialog.isShowing()) {
                            WaitConnectDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Connect fail, please retry.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 15000);  // 15 seconds
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void setFragment(int position) {
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;
        switch (position) {
            case 0:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                MainFragment mainFragment = new MainFragment();
                fragmentTransaction.replace(R.id.fragment, mainFragment);
                fragmentTransaction.commit();
                break;
//            case 1:
//                fragmentManager = getSupportFragmentManager();
//                fragmentTransaction = fragmentManager.beginTransaction();
//                HistoryFragment historyFragment = new HistoryFragment();
//                fragmentTransaction.replace(R.id.fragment, historyFragment);
//                fragmentTransaction.commit();
//                break;
            case 2:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                TaskFragment taskFragment = new TaskFragment();
                fragmentTransaction.replace(R.id.fragment, taskFragment);
                fragmentTransaction.commit();
                break;
            case 3:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                ProfileFragment profileFragment = new ProfileFragment();
                fragmentTransaction.replace(R.id.fragment, profileFragment);
                fragmentTransaction.commit();
                break;
            case 4:

//                fragmentManager = getSupportFragmentManager();
//                fragmentTransaction = fragmentManager.beginTransaction();
//                TypeFragment trainingFragment = new TypeFragment();
//                fragmentTransaction.replace(R.id.fragment, trainingFragment);
//                fragmentTransaction.commit();
                changeTrainFragment(TypeFragment.newInstance(MainActivity.this, sw, bh, TypeFragment.TRAINING_TYPE));
                break;
            case 5:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                ReadmeFragment readmeFragment = new ReadmeFragment();
                fragmentTransaction.replace(R.id.fragment, readmeFragment);
                fragmentTransaction.commit();
                break;
            case 6:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                TabFragment tabFragment = new TabFragment();
                fragmentTransaction.replace(R.id.fragment, tabFragment);
                fragmentTransaction.commit();
                break;
        }
    }

    //Training Or Testing
    private void changeTrainFragment(Fragment f) {
        curFragment = (TypeFragment)f;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, f);
        transaction.commitAllowingStateLoss();
    }
    private void changeTestFragment(Fragment f) {
        main = (MainFragment)f;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, f);
        transaction.commitAllowingStateLoss();
    }




    private void navigateTo(MenuItem menuItem){
//        contentView.setText(menuItem.getTitle());

        navItemId = menuItem.getItemId();
        menuItem.setChecked(true);
    }

    private static final String NAV_ITEM_ID = "nav_index";
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, navItemId);
    }


    private void create_write(){
        LayoutInflater inflater = LayoutInflater.from(this);
        final View v1 = inflater.inflate(R.layout.popup_layout_profile, null);

        new AlertDialog.Builder(this)
                .setTitle("Please key profile information")
                .setView(v1)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String Pop_name = ((EditText) v1.findViewById(R.id.pop_name)).getText().toString();
                        String Pop_height = ((EditText) v1.findViewById(R.id.pop_height)).getText().toString();
                        String Pop_weight = ((EditText) v1.findViewById(R.id.pop_weight)).getText().toString();
                        String Pop_age= ((EditText) v1.findViewById(R.id.pop_age)).getText().toString();

                        if ("".equals(Pop_name)) {
                            Toast.makeText(MainActivity.this, "Name can't be empty", Toast.LENGTH_LONG).show();
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else if ("".equals(Pop_height)) {
                            Toast.makeText(MainActivity.this, "height can't be empty", Toast.LENGTH_LONG).show();
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else if ("".equals(Pop_weight)) {
                            Toast.makeText(MainActivity.this, "weight can't be empty", Toast.LENGTH_LONG).show();
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else if ("".equals(Pop_age)) {
                            Toast.makeText(MainActivity.this, "age can't be empty", Toast.LENGTH_LONG).show();
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {

                            String t_id = sharedPreferences.getString("ID", null);
                            Create_User(t_id, Pop_name, Pop_height, Pop_weight, Pop_age);

                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                })
                .setOnKeyListener(new android.content.DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_BACK:
                                Log.v("Tag", "KEYCODE_BACK");
                                return true;
                        }
                        return false;
                    }
                })
                .show();
    }
    private String Create_User(final String User_id,final String Name, final String Height, final String Weight, final String Age){
        RequestQueue mQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://140.113.203.226/~andersen/InsertLittleAppleUser.php",  new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(MainActivity.this, String.valueOf(response), Toast.LENGTH_SHORT).show();
                if(response.equals("success")){
                    Toast.makeText(MainActivity.this, "Add Success!", Toast.LENGTH_SHORT).show();

                }else{
                    Toast.makeText(MainActivity.this, "Add Fail!", Toast.LENGTH_SHORT).show();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("ID", User_id );
                map.put("NAME", Name);
                map.put("WEIGHT", Weight );
                map.put("HEIGHT", Height );
                map.put("AGE", Age );
                return map;
            }
        };
        mQueue.add(stringRequest);

        return "";
    }




    private void initialBTManager() {
        Log.d(TAG, "Check if BT is enable");
        //Check BT Enabled
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Log.d(TAG, "Bind BT Service");

        //Initial SoundWave Handler
        sw = new SoundWaveHandler(MainActivity.this);
        registerReceiver(mSoundWaveHandlerStateUpdateReceiver, makeSoundWaveHandlerStateUpdateIntentFilter());

        //Initial Beacon Handler
        bh = new BeaconHandler(MainActivity.this);
        registerReceiver(mKoalaStateUpdateReceiver, makeKoalaStateUpdateIntentFilter());

//        changeFragment(TypeFragment.newInstance(MainActivity.this, sw, bh, TypeFragment.TRAINING_TYPE));
    }





    /**********************/
    /**    Broadcast Event	 **/
    /**********************/
    private final BroadcastReceiver mKoalaStateUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if( BeaconHandler.ACTION_BEACON_CONNECT_STATE.equals(action) ){
                MainFragment.btKoalaConnect.setBackground(getResources().getDrawable(R.drawable.koala_connect));
                koala_con_flag = 1;
            }else if( BeaconHandler.ACTION_BEACON_DISCONNECT_STATE.equals(action) ){
                MainFragment.btKoalaConnect.setBackground(getResources().getDrawable(R.drawable.koala_disconnect));
//                curFragment.InterruptLogging(TypeFragment.DEVICE_KOALA);
                koala_con_flag = 0;
            }else if( BeaconHandler.ACTION_BEACON_FIRST_DATA_RECEIVE.equals(action) ){
                WaitConnectDialog.dismiss();

                Calibration_flag = 1;
                // Active Calibration
                SystemParameters.initializeSystemParameters();
                for(int i = 0; i < 2; i++)
                    ActiveCalibration(i);
            }
        }
    };
    private static IntentFilter makeKoalaStateUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BeaconHandler.ACTION_BEACON_CONNECT_STATE);
        intentFilter.addAction(BeaconHandler.ACTION_BEACON_DISCONNECT_STATE);
        intentFilter.addAction(BeaconHandler.ACTION_BEACON_FIRST_DATA_RECEIVE);

        return intentFilter;
    }

    private final BroadcastReceiver mSoundWaveHandlerStateUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if( SoundWaveHandler.ACTION_SOUND_SERVICE_CONNECT_STATE.equals(action) ) {}
            else if( SoundWaveHandler.ACTION_SOUND_NOT_PREPARE_STATE.equals(action) ){
                MainFragment.btMicConnect.setBackground(getResources().getDrawable(R.drawable.headset_disconnect));
                MainFragment.btMicConnect.setEnabled(true);
                MainFragment.btKoalaConnect.setEnabled(true);
//                curFragment.InterruptLogging(TypeFragment.DEVICE_HEADSET);
                mic_con_flag = 0;
            }else if( SoundWaveHandler.ACTION_SOUND_PREPARING_STATE.equals(action) ){
                MainFragment.btMicConnect.setEnabled(false);
                MainFragment.btKoalaConnect.setEnabled(false);
            }else if( SoundWaveHandler.ACTION_SOUND_PREPARED_STATE.equals(action) ){
                MainFragment.btMicConnect.setBackground(getResources().getDrawable(R.drawable.headset_connect));
                MainFragment.btMicConnect.setEnabled(true);
                MainFragment.btKoalaConnect.setEnabled(true);
                mic_con_flag = 1;
            }
        }
    };

    private static IntentFilter makeSoundWaveHandlerStateUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(SoundWaveHandler.ACTION_SOUND_SERVICE_CONNECT_STATE);
        intentFilter.addAction(SoundWaveHandler.ACTION_SOUND_NOT_PREPARE_STATE);
        intentFilter.addAction(SoundWaveHandler.ACTION_SOUND_PREPARING_STATE);
        intentFilter.addAction(SoundWaveHandler.ACTION_SOUND_PREPARED_STATE);

        return intentFilter;
    }


    /********************/
    /** Connecting Event **/
    /********************/
//    private Button.OnClickListener MicConnectListener = new Button.OnClickListener() {
//        public void onClick(View v) {
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
//        }
//    };

//    private Button.OnClickListener KoalaConnectListener = new Button.OnClickListener() {
//        public void onClick(View v) {
//            if(SystemParameters.IsKoalaReady)
//                bh.DisconnectToKoala();
//            else{
//                Intent i = new Intent(MainActivity.this, KoalaScan.class);
//                startActivityForResult(i, KOALA_SCAN_PAGE_RESULT);
//            }
//        }
//    };
//

    public static void MicScan(Activity activity){
        if( !SystemParameters.IsBtHeadsetReady ) {
            Intent BTSettingIntent = new Intent(Intent.ACTION_MAIN, null);
            BTSettingIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.android.settings",
                    "com.android.settings.bluetooth.BluetoothSettings");
            BTSettingIntent.setComponent(cn);
            activity.startActivityForResult(BTSettingIntent, REQUEST_OPEN_BTSETTING);
        }
        else
            sw.getService().DisconnectAllScoBTHeadset();
    }

    public static void KoalaScan(Activity activity){
        if(SystemParameters.IsKoalaReady)
            bh.DisconnectToKoala();
        else{
            Intent i = new Intent(activity_one, KoalaScan.class);
            activity.startActivityForResult(i, KOALA_SCAN_PAGE_RESULT);

        }
    }

    public static int test_flag = 0;
    public void BtnStart(Activity activity){

        changeTestFragment(MainFragment.newInstance(MainActivity.this, sw, bh, TypeFragment.TESTING_TYPE));
        test_flag = 1;

    }



    /************************
     *  Axis Calibration Related
     ***********************/
	/* Calibration UI */
    public void ActiveCalibration(int type) {
        String Title,Message;
        final int TypeTemp ;
        if(type == 0) {
            Title = "Calibration Z";
            Message = "請將拍子垂直朝下";
            TypeTemp = LogFileWriter.CALIBRATION_Z_TYPE;
        }
        else {
            Title = "Calibration Y";
            Message = "請將拍子水平放置";
            TypeTemp = LogFileWriter.CALIBRATION_Y_TYPE;
        }

        AlertDialog.Builder CalZDialog = new AlertDialog.Builder(MainActivity.this);
        CalZDialog.setTitle(Title)
                .setMessage(Message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        showAxisCalibrationProcessDialog(TypeTemp);
                    }
                }).show();
    }

    private void showAxisCalibrationProcessDialog(final int LogType) {
        final ProgressDialog Cal_dialog = ProgressDialog.show(MainActivity.this, "校正中", "計算校正軸，請稍後",true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bh.startRecording(LogType);
                    //Service Start
                    SystemParameters.SetMeasureStartTime();
                    SystemParameters.isServiceRunning.set(true);
                    Thread.sleep(bh.Correct_Corrdinate_Time);
                    bh.stopRecording();
                    SystemParameters.isServiceRunning.set(false);
                    while(bh.isWrittingSensorDataLog.get()); //wait logging
                    bh.StartAxisCalibration(LogType);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Cal_dialog.dismiss();
                        }
                    });
                }
            }
        }).start();
    }

    private void logout() {
        //Mic Koala diconnect
        sw.getService().DisconnectAllScoBTHeadset();
        bh.DisconnectToKoala();


        VolleyController.getInstance().deleteUserCredentials();
        startActivity(new Intent(MainActivity.this, LoginPage.class));
        finish();
    }
}