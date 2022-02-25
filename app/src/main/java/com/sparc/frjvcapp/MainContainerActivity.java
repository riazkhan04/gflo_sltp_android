package com.sparc.frjvcapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.Task;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainContainerActivity extends AppCompatActivity {
    public static final String userlogin = "userlogin";
    public static final String coltlogin = "coltlogin";
    ImageView DataCollector, MapViewer, DGPSSurvey;


    ArrayList<Integer> headlist = new ArrayList<>();
    ArrayList<Integer> subheadlist = new ArrayList<>();
    int[] head = new int[]{1, 2, 3, 12, 4};
    int[] subhead = new int[]{5, 6, 7, 10, 11};
    SimpleDateFormat _endTime;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
    Date startDate, endDate = null;

    SQLiteDatabase db, db1, db2, db3;
    TextView logout, versionCode, rateus;
    SharedPreferences shared;
    public static final String PREVIOUS_TIME = "previousPref";
    long _min, _second;
    ReviewManager reviewManager;
    ReviewInfo reviewInfo;
    PowerManager pm;
    BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            if (level < 30) {
                AlertDialog.Builder connectionerrorBuilder = new AlertDialog.Builder(MainContainerActivity.this);
                connectionerrorBuilder.setTitle("Battery Low !!!");
                connectionerrorBuilder.setIcon(R.drawable.battery_low);
                connectionerrorBuilder.setMessage("You have " + level + "% battery. Please connect to Charger.");
                connectionerrorBuilder.setCancelable(false);
                connectionerrorBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.cancel();
                            }
                        });
                AlertDialog alertDialog1 = connectionerrorBuilder.create();
                alertDialog1.show();
            }
        }
    };

    //final String pos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);
        DataCollector = findViewById(R.id.mis_imageView);
        MapViewer = findViewById(R.id.gis_imageView);
        DGPSSurvey = findViewById(R.id.dgps_imageview);
        versionCode = findViewById(R.id.versionCode);
        rateus = findViewById(R.id.rateus);
        reviewManager = ReviewManagerFactory.create(getApplicationContext());
        logout = findViewById(R.id.name_textView);

        try {
            Util.scheduleJob(getApplicationContext());
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        for (int id : head) {
            headlist.add(id);
        }
        for (int id : subhead) {
            subheadlist.add(id);
        }

        try {
            versionCode.setText("Version : " + Util.getVersion_Code_Name(getApplicationContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        rateus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRateApp();
            }
        });
       /* SharedPreferences shared1 = getApplicationContext().getSharedPreferences(PREVIOUS_TIME, MODE_PRIVATE);
        if (shared1.getString("previousTime", "0").equals("1")) {
            try {
                Calendar c = Calendar.getInstance();
                SimpleDateFormat _endDateFormat = new SimpleDateFormat("kk:mm:ss");
                String _endTime = _endDateFormat.format(c.getTime());
                String _startTime = shared1.getString("exactTime", "0");
                // java.text.DateFormat df = new java.text.SimpleDateFormat("kk:mm:ss");
                SimpleDateFormat df = new SimpleDateFormat("kk:mm:ss");
                Date date11 = df.parse(df.format(new Date(_startTime)));
                java.util.Date date2 = df.parse(_endTime);
                long diff = date2.getTime() - date11.getTime();
                _min = diff / (60 * 1000) % 60;
                _second = diff / 1000 % 60;
                if ((_min >= 4 && _second >= 30) || (_min >= 14 && _second >= 30)) {
                    _min += 1;
                }
                String PillNo = shared1.getString("d_frjvc_pill_no", "0");
                db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                Cursor cursor;
                if (PillNo.contains("_")) {
                    String[] pillSplit = PillNo.split("_");
                    cursor = db.rawQuery("update m_fb_dgps_survey_pill_data set rtx_survey_min='" + _min +
                            "',rtx_survey_second='" + _second + "' where u_id='" + shared1.getString("userid", "0") +
                            "' and pill_no='" +
                            pillSplit[0] + "'  and pndjv_pill_no='" + pillSplit[1] + "' and frjvc_lat='" + shared1.getString("d_frjvc_lat", "0") +
                            "' and frjvc_long='" + shared1.getString("d_frjvc_long", "0") +
                            "'", null);
                } else {
                    cursor = db.rawQuery("update m_fb_dgps_survey_pill_data set rtx_survey_min='" + _min +
                            "',rtx_survey_second='" + _second + "' where u_id='" + shared1.getString("userid", "0") +
                            "' and pill_no='" +
                            PillNo + "' and frjvc_lat='" + shared1.getString("d_frjvc_lat", "0") +
                            "' and frjvc_long='" + shared1.getString("d_frjvc_long", "0") +
                            "'", null);
                }
                if (cursor.getCount() >= 0) {
                    AlertDialog.Builder connectionerrorBuilder = new AlertDialog.Builder(MainContainerActivity.this);
                    connectionerrorBuilder.setTitle("Pillar Observation");
                    connectionerrorBuilder.setMessage("Your Previous Pillar Observation Time : " + _min + " : " + _second);
                    connectionerrorBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.cancel();
                                }
                            });
                    AlertDialog alertDialog1 = connectionerrorBuilder.create();
                    alertDialog1.show();
                }
                cursor.close();
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            SharedPreferences.Editor editor = shared1.edit();
            editor.clear();
            editor.apply();
        }*/

        shared = getSharedPreferences(userlogin, MODE_PRIVATE);
        String _startTime = shared.getString("logintime", "0");
        _endTime = new SimpleDateFormat("dd/M/yyyy hh:mm:ss");
        String currentDateandTime = _endTime.format(new Date());
        try {
            startDate = sdf.parse(_startTime);
            endDate = sdf.parse(currentDateandTime);
            long difference_In_Time
                    = endDate.getTime() - startDate.getTime();
            long difference_In_Days
                    = (difference_In_Time
                    / (1000 * 60 * 60 * 24))
                    % 365;
            if (difference_In_Days < 7 && difference_In_Days > 2) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                if (difference_In_Days > 7) {
                    alertDialogBuilder.setTitle("Login Expired...");
                } else {
                    alertDialogBuilder.setTitle((7 - difference_In_Days) + " days left...");
                }
                alertDialogBuilder.setMessage("Keep Practice to Logout in 7 days...");
                alertDialogBuilder.setPositiveButton("Logout",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (isNetworkConnected().equals("Wifi enabled") || isNetworkConnected().equals("Mobile data enabled")) {
                                    Logout();

                                } else if (isNetworkConnected().equals("No internet is available")) {
                                    AlertDialog.Builder connectionerrorBuilder = new AlertDialog.Builder(MainContainerActivity.this);
                                    connectionerrorBuilder.setTitle("No Internet Connection");
                                    connectionerrorBuilder.setMessage("You need to have Mobile data or WIFI to access this. Press ok to Exit.");
                                    connectionerrorBuilder.setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface arg0, int arg1) {
                                                    arg0.cancel();
                                                }
                                            });
                                    AlertDialog alertDialog1 = connectionerrorBuilder.create();
                                    alertDialog1.show();
                                }
                            }
                        });
                alertDialogBuilder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            } else if (difference_In_Days >= 7) {
                Toast.makeText(this, "Login Expired...", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DataCollector.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (subheadlist.contains(Integer.parseInt(shared.getString("userdivid", "0")))) {
                    Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    SharedPreferences sharedPreferences = getSharedPreferences(coltlogin, 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.putString("uemail", shared.getString("uemail", "0"));
                    editor.putString("upass", shared.getString("upass", "0"));
                    editor.putString("uname", shared.getString("uname", "0"));
                    editor.putString("upos", shared.getString("upos", "0"));
                    editor.putString("ucir", shared.getString("ucir", "0"));
                    editor.putString("uid", shared.getString("uid", "0"));
                    editor.putString("udivid", shared.getString("udivid", "0"));
                    editor.putString("udivname", shared.getString("udivname", "0"));
                    //editor.putString("userid", jsonobject.getString("div_id"));
                    editor.commit();
                    startActivity(intent);
                } else {
                    Toast.makeText(MainContainerActivity.this, "Sorry.You are not authorized for this module", Toast.LENGTH_LONG).show();
                }
            }
        });

        MapViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*Intent intent = new Intent(getApplicationContext(), MapViewerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                SharedPreferences sharedPreferences = getSharedPreferences(coltlogin, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putString("uemail", shared.getString("uemail", "0"));
                editor.putString("upass", shared.getString("upass", "0"));
                editor.putString("uname", shared.getString("uname", "0"));
                editor.putString("upos", shared.getString("upos", "0"));
                editor.putString("ucir", shared.getString("ucir", "0"));
                editor.putString("uid", shared.getString("uid", "0"));
                editor.putString("udivid", shared.getString("udivid", "0"));
                editor.putString("udivname", shared.getString("udivname", "0"));
                //editor.putString("userid", jsonobject.getString("div_id"));
                editor.commit();
                startActivity(intent);*/
                Toast.makeText(MainContainerActivity.this, "Module is Under Development", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        DGPSSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View customLayout = getLayoutInflater().inflate(R.layout.pupup_check_file_availability, null);
                alertDialogBuilder.setView(customLayout);

                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent = new Intent(getApplicationContext(), SelectFBForDGPSActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                SharedPreferences sharedPreferences = getSharedPreferences(coltlogin, 0);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.clear();
                                editor.putString("uemail", shared.getString("uemail", "0"));
                                editor.putString("upass", shared.getString("upass", "0"));
                                editor.putString("uname", shared.getString("uname", "0"));
                                editor.putString("upos", shared.getString("upos", "0"));
                                editor.putString("ucir", shared.getString("ucir", "0"));
                                editor.putString("uid", shared.getString("uid", "0"));
                                editor.putString("udivid", shared.getString("udivid", "0"));
                                editor.putString("udivname", shared.getString("udivname", "0"));
                                //editor.putString("userid", jsonobject.getString("div_id"));
                                editor.commit();
                                startActivity(intent);
                            }
                        });
                alertDialogBuilder.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // Toast.makeText(getApplicationContext(), "You canceled the request...please try again", Toast.LENGTH_LONG).show();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                //Toast.makeText(MainContainerActivity.this, "Module is Under Development", Toast.LENGTH_SHORT).show();

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkConnected().equals("Wifi enabled") || isNetworkConnected().equals("Mobile data enabled")) {
                    Logout();

                } else if (isNetworkConnected().equals("No internet is available")) {
                    AlertDialog.Builder connectionerrorBuilder = new AlertDialog.Builder(MainContainerActivity.this);
                    connectionerrorBuilder.setTitle("No Internet Connection");
                    connectionerrorBuilder.setMessage("You need to have Mobile data or WIFI to access this. Press ok to Exit.");
                    connectionerrorBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.cancel();
                                }
                            });
                    AlertDialog alertDialog1 = connectionerrorBuilder.create();
                    alertDialog1.show();
                }

            }
        });
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isIgnoringBatteryOptimizations = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(getPackageName());
        }
        if (!isIgnoringBatteryOptimizations) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 2);
        }
    }

    private void runService() {
        String divid, userid;
        final SharedPreferences shared = getSharedPreferences(userlogin, MODE_PRIVATE);
        divid = shared.getString("udivid", "0");
        userid = shared.getString("uemail", "0");

        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);

        Cursor c = db.rawQuery("select * from m_pillar_reg where d_id='" + divid + "' and uid='" + userid + "' and img_status='0' and p_pic is not null", null);
        int count = c.getCount();
        if (c.getCount() >= 1) {
            if (c.moveToFirst()) {
                try {
                    Util.scheduleJob(getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            c.close();
            db.close();
        }

        Cursor c1 = db1.rawQuery("select * from m_shifting_pillar_reg where uid='" + userid + "' and simg_status='0' and s_pic is not null", null);
        int count1 = c.getCount();
        if (c1.getCount() >= 1) {
            if (c1.moveToFirst()) {
                try {
                    Util.scheduleJob(getApplicationContext());
                    ///uploadImage(Utility.getByeArr(Utility.setPic(c1.getString(c1.getColumnIndex("s_pic")))), c1.getString(c1.getColumnIndex("s_pic")), "2");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            c1.close();
            db1.close();
        }

        Cursor c2 = db1.rawQuery("select * from m_fb_dgps_survey_pill_pic where u_id='" + userid + "' and pic_status='0' and pic_name is not null", null);
        int count2 = c.getCount();
        if (c2.getCount() >= 1) {
            if (c2.moveToFirst()) {
                try {
                    Util.scheduleJob(getApplicationContext());
                    //uploadDGPSImage(Utility.getByeArr(Utility.setPic(c2.getString(c2.getColumnIndex("pic_name")))), c2.getString(c2.getColumnIndex("pic_name")), "1", c2.getString(c2.getColumnIndex("pic_view")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            c2.close();
            db2.close();
        } else {

        }
    }

    private void Logout() {
        if (subheadlist.contains(Integer.parseInt(shared.getString("userdivid", "0")))) {
            SQLiteDatabase db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            db.execSQL("DELETE from m_fb");
            db.delete("m_range", null, null);
            db.close();

            SharedPreferences sharedPreferences = getSharedPreferences(userlogin, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            i.setFlags(i.FLAG_ACTIVITY_NEW_TASK | i.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();

        } else {
            SharedPreferences sharedPreferences = getSharedPreferences(userlogin, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            i.setFlags(i.FLAG_ACTIVITY_NEW_TASK | i.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
    }

    private String isNetworkConnected() {
        String status = null;
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                status = "Wifi enabled";
                return status;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                status = "Mobile data enabled";
                return status;
            }
        } else {
            status = "No internet is available";
            return status;
        }
        return status;

    }

    private void showRateApp() {
        try {
            reviewManager.requestReviewFlow().addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
                @Override
                public void onComplete(@NonNull Task<ReviewInfo> task) {

                    if (task.isSuccessful()) {
                        reviewInfo = task.getResult();
                        reviewManager.launchReviewFlow(MainContainerActivity.this, reviewInfo).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                final String appPackageName = getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                                Log.d("rating", "fail");
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("rating", "success");
                            }
                        });


                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {

                    Toast.makeText(MainContainerActivity.this, "In-App Request Failed", Toast.LENGTH_SHORT).show();

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            this.unregisterReceiver(this.mBatInfoReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            this.unregisterReceiver(this.mBatInfoReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isIgnoringBatteryOptimizations = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(getPackageName());

                Log.d("saver", " " + isIgnoringBatteryOptimizations);
            }
            if (isIgnoringBatteryOptimizations) {
                Log.d("saver", " " + isIgnoringBatteryOptimizations);
                // Ignoring battery optimization
            } else {
                Log.d("saver", " " + isIgnoringBatteryOptimizations);
                // Not ignoring battery optimization
            }
        }
    }
}
