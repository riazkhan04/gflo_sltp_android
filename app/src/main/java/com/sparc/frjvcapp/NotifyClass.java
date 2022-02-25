package com.sparc.frjvcapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import android.widget.RemoteViews;


import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

public class NotifyClass {
    /* int count = 10000;
     String counter;*/
    NotificationManager notificationManager;
    public static int NOTIFICATION_ID = 234;
    Intent intent;
    PendingIntent pendingIntent;
    Context _context;
    public static final String MyPREFERENCES = "MyPrefs";

    NotifyClass(Context context) {
        _context = context;
    }

    public void addNotification(String title, String sub_title, String val, String checkedCount, String pillNo) {
        notificationManager = (NotificationManager) _context.getSystemService(NOTIFICATION_SERVICE);
        String CHANNEL_ID = "xxx";
        CharSequence name = "Riaz";
        String Description = "This is my channel";


        RemoteViews expandedView = new RemoteViews(_context.getPackageName(), R.layout.view_expanded_notification);
        // expandedView.setTextViewText(R.id.timestamp, DateUtils.formatDateTime(_context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
        expandedView.setTextViewText(R.id.countDowntxt, val);
        expandedView.setTextViewText(R.id.piilar_no, "Pillar No : " + pillNo);

        intent = create_intent("6", pillNo);
        pendingIntent = PendingIntent.getBroadcast(_context, (int) System.currentTimeMillis()/* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        expandedView.setOnClickPendingIntent(R.id.radioFive, pendingIntent);
//        if(checkedCount.contains("6")){
//            expandedView.setBoolean(R.id.radioFive,"setChecked",true);
//        }

        intent = create_intent("16", pillNo);
        pendingIntent = PendingIntent.getBroadcast(_context, (int) System.currentTimeMillis()/* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
//                PendingIntent.FLAG_ONE_SHOT);
        expandedView.setOnClickPendingIntent(R.id.radioTen, pendingIntent);
//        if(checkedCount.contains("16")){
//            expandedView.setBoolean(R.id.radioTen,"setChecked",true);
//        }

        intent = create_intent("31", pillNo);
        pendingIntent = PendingIntent.getBroadcast(_context, (int) System.currentTimeMillis()/* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
//                PendingIntent.FLAG_ONE_SHOT);
        expandedView.setOnClickPendingIntent(R.id.radioFifteen, pendingIntent);
//        if(checkedCount.contains("31")){
//            expandedView.setBoolean(R.id.radioFifteen,"setChecked",true);
//        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
          /*  Log.d("anup1", String.valueOf(count));
            Log.d("anupam1", String.valueOf(counter));*/

            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setVibrationPattern(null);
            mChannel.enableVibration(false);
            mChannel.setLightColor(Color.RED);
            mChannel.setShowBadge(true);


            if (notificationManager != null) {
                SharedPreferences shared = _context.getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
                String counterStatus = shared.contains("counter") ? (shared.getString("counter", "")) : "";
                if (counterStatus.equals("true") || counterStatus.equalsIgnoreCase("")) {
                    notificationManager.createNotificationChannel(mChannel);
                }
            }
        }
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(_context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(sub_title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Notice that the Notification is Coming"))
                .setSmallIcon(R.drawable.black_tree)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setVibrate(null)
                .setSound(defaultSoundUri)
                .setCustomBigContentView(expandedView)
                .setColor(_context.getResources().getColor(android.R.color.holo_red_dark));
        if (notificationManager != null) {
            SharedPreferences shared = _context.getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
            String counterStatus = shared.contains("counter") ? (shared.getString("counter", "")) : "";
            if (counterStatus.equals("true") || counterStatus.equals("")) {
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }

        }

    }

    private Intent create_intent(String okOrcancel, String pillNo) {

        Intent intent = new Intent(_context, MyNotificationReceiver.class);
        intent.putExtra("time", okOrcancel);
        intent.putExtra("pillNo", pillNo);
        return intent;

    }
}
