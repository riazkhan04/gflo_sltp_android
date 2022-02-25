package com.sparc.frjvcapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;


public class MyNotificationReceiver extends BroadcastReceiver {
    Context context_;
    String type = "";
    String pillNo = "";
    int count;
    String counter;
    SharedPreferences sharedPreferences;
    public static final String MyPREFERENCES = "MyPrefs";
    public static Handler handler;
    public static Runnable r;
    SharedPreferences.Editor editor;
    String counterStatus;
    String checkCount;

    @Override
    public void onReceive(Context context, Intent intent) {

        context_ = context;
        type = intent.getStringExtra("time");
        pillNo = intent.getStringExtra("pillNo");

        assert type != null;
        switch (type) {

            case "6":
                Log.d("onReceive", type);
                sharedPreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String counterStatus = (sharedPreferences.getString("counter", ""));

                if (counterStatus.equalsIgnoreCase("true")) {
                    if (handler != null && r != null) {
                        handler.removeCallbacks(r);
                    }
                }
                editor.clear();
                editor.putString("counter", "true");
                editor.apply();
                checkCount = "6";
                new NotifyClass(context).addNotification("Notification", "Notification is coming", "00:06:00"
                        , checkCount, pillNo);
                count = 6 * 60 * 1000;
                countTime(context);
                break;
            case "16":
                Log.d("onReceive", type);
                sharedPreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                editor = sharedPreferences.edit();
                counterStatus = (sharedPreferences.getString("counter", ""));

                if (counterStatus.equalsIgnoreCase("true")) {
                    if (handler != null && r != null) {
                        handler.removeCallbacks(r);
                    }
                }
                editor.clear();
                editor.putString("counter", "true");
                editor.apply();
                checkCount = "16";
                new NotifyClass(context).addNotification("Notification",
                        "Notification is coming", "00:16:00", checkCount, pillNo);
                count = 16 * 60 * 1000;
                countTime(context);
                break;
            case "31":
                Log.d("onReceive", type);
                sharedPreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                editor = sharedPreferences.edit();
                counterStatus = (sharedPreferences.getString("counter", ""));

                if (counterStatus.equalsIgnoreCase("true")) {
                    if (handler != null && r != null) {
                        handler.removeCallbacks(r);
                    }
                }
                editor.clear();
                editor.putString("counter", "true");
                editor.apply();
                checkCount = "31";
                new NotifyClass(context).addNotification("Notification",
                        "Notification is coming", "00:31:00", checkCount, pillNo);
                count = 31 * 60 * 1000;
                countTime(context);
                break;

        }


    }


    public void countTime(final Context context) {

        handler = new Handler();

        handler.postDelayed(r = new Runnable() {
            @Override
            public void run() {
                if (count >= 1000) {
                    count = count - 1000;
                    NumberFormat f = new DecimalFormat("00");
                    long hour = (count / 3600000) % 24;
                    long min = (count / 60000) % 60;
                    long sec = (count / 1000) % 60;
                    counter = f.format(hour) + ":" + f.format(min) + ":" + f.format(sec);
                    new NotifyClass(context_).addNotification("Notification", "Notification is coming", counter, checkCount,pillNo);
                    countTime(context);
                }
            }
        }, 1000);
    }
}

