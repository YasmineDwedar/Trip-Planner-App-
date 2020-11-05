package com.vaatu.tripmate.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.vaatu.tripmate.R;
import com.vaatu.tripmate.utils.TripModel;
import com.vaatu.tripmate.utils.alarmManagerReciever.MyDialogActivity;

import static com.vaatu.tripmate.utils.alarmManagerReciever.AlarmEventReciever.RECEIVED_TRIP;
import static com.vaatu.tripmate.utils.alarmManagerReciever.AlarmEventReciever.RECEIVED_TRIP_SEND_SERIAL;

public class DialognotificationService extends Service {
    private static final String CHANNEL_ID = "MyDialogService";
    private TripModel tm;
    private final IBinder localBinder = new MyBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Create Notification Here !
        // TODO Try to remove createNotificationChannel(); call and check the notification Builder

        createNotificationChannel();
        createNotification();

        tm = (TripModel) intent.getSerializableExtra(RECEIVED_TRIP_SEND_SERIAL);
        if (tm != null) {
            Log.i("MyService: ", "Object Received " + tm.getTripname());
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

//        Bundle b = intent.getBundleExtra(RECEIVED_TRIP);
//        tm = (TripModel) b.getSerializable(RECEIVED_TRIP_SEND_SERIAL);

        return localBinder;  // ref from inerclass MyBinder
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel(
                    CHANNEL_ID,
                    "Trip Mate",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager man = (NotificationManager) getSystemService(this.NOTIFICATION_SERVICE);

            man.createNotificationChannel(nc);
        }

    }

    private void createNotification() {
        Intent notificationIntent = new Intent(this, MyDialogActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MyDialogActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        //TODO GET TRIP ! to show trip name
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Trip Mate")
                .setContentText("You have an upcoming trip").setTicker("Notification!")
                .setContentIntent(pendingIntent).setSmallIcon(R.drawable.ic_date_range_24px)
                .build();

        startForeground(12354, notification);

    }


    public class MyBinder extends Binder {
        public DialognotificationService getService() {  // ref ml service
            return DialognotificationService.this;
        }

    }

}
