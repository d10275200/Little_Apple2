package com.nol.ivan.little_apple2;

/**
 * Created by Ivan on 2017/1/3.
 */


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import cc.nctu1210.api.koala6x.KoalaDevice;
import com.nol.ivan.little_apple2.bt.devices.BeaconHandler;


public class KoalaScan extends Activity{
    private String TAG = "KoalaScan";
    // Scan Related
    private BeaconHandler bh = null;
    private Button btScan;
    private ListView listkoala;
    private ArrayAdapter<String> Adapter;
    private ArrayList<String> koala= new ArrayList<String>();
    // Pass Result Related
    public static final String macAddress = "KoalaScan.MacAddress";
    public static final String deviceName = "KoalaScan.DeviceName";
    //ProgressDialog
    private ProgressDialog dialog;

    public static final int REQUEST_COARSE_LOCATION = 0x01 << 1;
    public static final int REQUEST_EXTERNAL_STORAGE = 0x01 << 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.koalascan);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "定位服務權限未取得", Toast.LENGTH_LONG).show();
            //return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //verifyStoragePermissions(this);
            verifyCoaseLocationPermissions(this);
        }
        bh = new BeaconHandler(KoalaScan.this);


        initialViewandEvent();
    }

    @Override
    protected void onResume(){
        super.onResume();

        btScan.performClick();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "定位服務權限未取得", Toast.LENGTH_LONG).show();
            //return;
        } else {
                Toast.makeText(this, "請開啟定位服務", Toast.LENGTH_LONG).show();
        }


    }


//    public static void verifyStoragePermissions(Activity activity) {
//        // Check if we have write permission
//        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            // We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(
//                    activity,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    REQUEST_EXTERNAL_STORAGE
//            );
//        }
//    }

    public static void verifyCoaseLocationPermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "coarse location permission granted");
                    } else {
                        Log.e("TAG", grantResults[0]+"\n");
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }
                } else {
                    Log.w(TAG, "no permission granted!!");
                }
                break;
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "coarse location permission granted");
                    } else {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Functionality limited");
                        builder.setMessage("Since storage access has not been granted, this app will not be able to store any images.");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                            }

                        });
                        builder.show();
                    }
                } else {
                    Log.w(TAG, "no permission granted!!");
                }
                break;
        }
    }


    private void initialViewandEvent(){
        //ListView
        listkoala = (ListView) findViewById(R.id.list_device);
        listkoala.setOnItemClickListener(ListClickListener);
        Adapter = new ArrayAdapter<String>(KoalaScan.this, android.R.layout.simple_list_item_1, koala);
        listkoala.setAdapter(Adapter);

        //Button
        btScan = (Button) findViewById(R.id.bt_scan);
        btScan.setOnClickListener(KoalaScanListener);

    }

    private Button.OnClickListener KoalaScanListener = new Button.OnClickListener() {
        @Override
        public void onClick(View arg0) {
            koala.clear();
            bh.scanLeDevice();
            dialog = ProgressDialog.show(KoalaScan.this, "請稍後", "藍芽設備搜尋中", true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(bh.SCAN_PERIOD);
                        for (int i = 0, size = bh.getScanedDevices().size(); i < size; i++) {
                            KoalaDevice d = bh.getScanedDevices().get(i);
                            koala.add(d.getDevice().getName() + " " + d.getDevice().getAddress());
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Adapter.notifyDataSetChanged();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        dialog.dismiss();
                    }
                }
            }).start();
        }
    };

    private ListView.OnItemClickListener ListClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            String sel =arg0.getItemAtPosition(arg2).toString();
            Intent intent = new Intent();
            Bundle b = new Bundle();
            b.putString(deviceName, sel.split(" ")[0]);
            b.putString(macAddress, sel.split(" ")[1]);
            intent.putExtras(b);
            KoalaScan.this.setResult(RESULT_OK, intent);
            finish();
        }
    };

}
