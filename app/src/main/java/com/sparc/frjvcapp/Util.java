package com.sparc.frjvcapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.format.DateFormat;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.Date;


public class Util {

    private static int REMINDER_INTERVAL_SECONDS = 5;
    private static int SYNC_FLEXTIME_SECONDS = 5;
    private static String REMINDER_JOB_TAG = "aaa";

    public static void scheduleJob(Context context) {
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher firebaseJobDispatcher = new FirebaseJobDispatcher(driver);

        Job constraintReminderJob = firebaseJobDispatcher.newJobBuilder()
                .setService(ImageService.class)
                .setTag(REMINDER_JOB_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        REMINDER_INTERVAL_SECONDS,
                        REMINDER_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS
                ))
                .setReplaceCurrent(true)
                .build();
        firebaseJobDispatcher.schedule(constraintReminderJob);
    }

    public static String getVersion_Code_Name(Context context) {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int versionCode = info.versionCode;
        String versionName = info.versionName;
        return versionName;
    }
    public static String getCurrentDate(String format){
        String current_date="";
        try {
            Date d = new Date();
            current_date  = DateFormat.format(format, d.getTime()).toString();

        }catch (Exception e){
            e.printStackTrace();
        }
        return current_date;
    }
}
