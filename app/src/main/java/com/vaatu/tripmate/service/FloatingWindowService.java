package com.vaatu.tripmate.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.vaatu.tripmate.R;
import com.vaatu.tripmate.ui.home.UpcomingTripsActivity;
import com.vaatu.tripmate.utils.TripModel;
import com.vaatu.tripmate.utils.alarmManagerReciever.MyDialogActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class FloatingWindowService extends Service {
    private final IBinder localBinder = new MyBinder();

    TripModel mTripModel;
    WindowManager wm;
    LinearLayout ll;
    private boolean listOn = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {


        return localBinder;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);
        ll.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setLayoutParams(layoutParams);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = 0;

        ImageView openapp = new ImageView(this);
        openapp.setImageResource(R.drawable.bubble);
        ViewGroup.LayoutParams butnparams = new ViewGroup.LayoutParams(
                200, 200);
        openapp.setLayoutParams(butnparams);

        ll.addView(openapp);
        wm.addView(ll, params);

        openapp.setOnTouchListener(new View.OnTouchListener() {
            WindowManager.LayoutParams updatepar = params;
            double x;
            double y;
            double px;
            double py;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        x = updatepar.x;
                        y = updatepar.y;

                        px = motionEvent.getRawX();
                        py = motionEvent.getRawY();

                        break;

                    case MotionEvent.ACTION_MOVE:

                        updatepar.x = (int) (x + (motionEvent.getRawX() - px));
                        updatepar.y = (int) (y + (motionEvent.getRawY() - py));

                        wm.updateViewLayout(ll, updatepar);

                    default:
                        break;
                }

                return false;

            }
        });
        openapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListView listview = new ListView(FloatingWindowService.this);
                String aarray[] = {"View notes from Home"};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(FloatingWindowService.this, android.R.layout.simple_list_item_1, aarray);
                listview.setAdapter(adapter);

                if (!listOn) {
                    ll.addView(listview);
                } else {
                    ll.removeView(listview);
                }

            }
        });
        openapp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent i = new Intent(FloatingWindowService.this, UpcomingTripsActivity.class);
                i.setFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                wm.removeView(ll);
                return false;
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        wm.removeView(ll);
    }

    public class MyBinder extends Binder {
        public FloatingWindowService getService() {
            return FloatingWindowService.this;
        }

    }

    public void setTripModel(TripModel tm) {
        this.mTripModel = tm;
    }

}

