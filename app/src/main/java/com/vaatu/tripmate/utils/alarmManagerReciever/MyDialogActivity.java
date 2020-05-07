package com.vaatu.tripmate.utils.alarmManagerReciever;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.vaatu.tripmate.R;
import com.vaatu.tripmate.data.remote.network.FirebaseDB;
import com.vaatu.tripmate.service.DialognotificationService;
import com.vaatu.tripmate.service.FloatingWindowService;
import com.vaatu.tripmate.ui.splash.SplashActivity;
import com.vaatu.tripmate.utils.TripModel;

import static com.vaatu.tripmate.utils.alarmManagerReciever.AlarmEventReciever.RECEIVED_TRIP;
import static com.vaatu.tripmate.utils.alarmManagerReciever.AlarmEventReciever.RECEIVED_TRIP_SEND_SERIAL;

public class MyDialogActivity extends Activity {
    public static final String DIALOG_TO_BUBBLE = "DIALOG_TO_BUBBLE";
    DialognotificationService mService;
    AlertDialog alertDialog;
    android.app.AlertDialog alert;
    boolean started = false;
    FloatingWindowService fws;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_my_dialog);
        FirebaseDB firebaseDB = FirebaseDB.getInstance();

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);

        Intent i = getIntent();
        Bundle b = i.getBundleExtra(RECEIVED_TRIP);
        TripModel tm = (TripModel) b.getSerializable(RECEIVED_TRIP_SEND_SERIAL);
        if (tm != null) {
            startAlarmRingTone(r);
            AlertDialog.Builder Builder = new AlertDialog.Builder(this)
                    .setMessage("Your Trip: \" "+ tm.getTripname() +"\" is now on...")
                    .setTitle("Trip reminder")
                    .setIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setNegativeButton("Snooze", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MyDialogActivity.this, "Trip Snooze", Toast.LENGTH_SHORT).show();
                            stopAlarmRingTone(r);
                            startDialogService(tm);

                            finish();
                        }
                    })
                    .setPositiveButton("Start Trip", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MyDialogActivity.this, "Trip Will Start", Toast.LENGTH_SHORT).show();
                            tm.setStatus("Done!");
                            firebaseDB.addTripToHistory(tm);
                            firebaseDB.removeFromUpcoming(tm);
                            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + tm.getEndloc());
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            stopAlarmRingTone(r);
                            startActivity(mapIntent);
                            start_stop(tm);

                            if (isMyServiceRunning(FloatingWindowService.class)){
                                started = true;
                            }

                            finish();
                        }
                    }).setNeutralButton("Cancel Trip", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MyDialogActivity.this, "Trip Canceled", Toast.LENGTH_SHORT).show();
                            tm.setStatus("Canceled!");
                            firebaseDB.addTripToHistory(tm);
                            firebaseDB.removeFromUpcoming(tm);

                            stopAlarmRingTone(r);
                            alertDialog.dismiss();
                            finish();
                        }
                    });

            alertDialog = Builder.create();
            alertDialog.show();

        } else {
            Toast.makeText(this, "Smth went wrong !", Toast.LENGTH_SHORT).show();
        }

    }

    public void startDialogService(TripModel tm) {
        Intent service = new Intent(this, DialognotificationService.class);

        service.putExtra(RECEIVED_TRIP_SEND_SERIAL, tm);
        service.putExtra("test","MEMO");
        startService(service);
        bindService(service,mServiceConnection,BIND_ADJUST_WITH_ACTIVITY);
        alertDialog.dismiss();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = ((DialognotificationService.MyBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public void startAlarmRingTone(Ringtone r) {
        r.play();
    }

    public void stopAlarmRingTone(Ringtone r) {
        r.stop();
    }


    public void start_stop(TripModel tm) {
        if (checkPermission()) {
            if (started) {
                Intent i = new Intent(MyDialogActivity.this, FloatingWindowService.class);


                stopService(i);
                // start_stop.setText("Start");
                started = false;
            } else {
                Intent i = new Intent(MyDialogActivity.this, FloatingWindowService.class);

                startService(i);
                started = true;
            }
        }else {
            reqPermission();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK) {
            if (checkPermission()) {
            } else {
                reqPermission();
            }
        }
    }


    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                reqPermission();
                return false;
            }
            else {
                return true;
            }
        }else{
            return true;
        }

    }

    private void reqPermission(){
        final android.app.AlertDialog.Builder alertBuilder = new android.app.AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Screen overlay detected");
        alertBuilder.setMessage("Enable 'Draw over other apps' in your system setting.");
        alertBuilder.setPositiveButton("OPEN SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent,RESULT_OK);
            }
        });
        alert = alertBuilder.create();
        alert.show();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
