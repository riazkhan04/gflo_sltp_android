package com.sparc.frjvcapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DGPSSyncMenuActivity extends AppCompatActivity {
    public static final String data = "data";
    public static final String userlogin = "userlogin";
    private SQLiteDatabase db;
    String sharediv, sharerange, sharefb, sharefbtype, sharefbname, userid, jobid, div_name, range_name, fb_name, _token, observPillNo;
    SharedPreferences shared, _shareToken;
    Button sync, backup;
    DbHelper dbHelper;
    private ProgressDialog progressDialog;
    JSONArray jsonArray;
    JSONObject fp_data;
    ProgressDialog progressDialog1, progressDialog2;
    TextView totpoint, syncpoint, totpic, syncpic, totsign, syncattendance, totfolder, syncfolder, dgpsfbName, totjxl, syncjxl;
    int totPoint, syncPoint, totPic, syncPic, totStatic, syncStatic, totRtx, syncRtx, totJxl, syncJxl;

    private RetrofitInterface jsonPlaceHolderApi;
    static File d_static, d_rtx, d_jxl;
    private TextView visDate, visTotObser, visObserv, visVirtual, visMisTagStatic, visTagRTX, visTagJXL, visReqRe;
    int backupFileStatus = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dgpssync_menu);

        sync = findViewById(R.id.sync);
        totpoint = findViewById(R.id.totpoint);
        syncpoint = findViewById(R.id.syncpoint);
        totpic = findViewById(R.id.totpic);
        syncpic = findViewById(R.id.syncpic);
        totsign = findViewById(R.id.totsign);
        backup = findViewById(R.id.backup);
        syncattendance = findViewById(R.id.syncattendance);
        totfolder = findViewById(R.id.totfolder);
        syncfolder = findViewById(R.id.syncfolder);
        dgpsfbName = findViewById(R.id.dgpsfbName);
        totjxl = findViewById(R.id.totjxl);
        syncjxl = findViewById(R.id.syncjxl);
        shared = getApplicationContext().getSharedPreferences(data, MODE_PRIVATE);
        sharediv = shared.getString("fbdivcode", "0");
        sharerange = shared.getString("fbrangecode", "0");
        sharefb = shared.getString("fbcode", "0");
        sharefbtype = shared.getString("fbtype", "0");
        sharefbname = shared.getString("fbname", "0");
        jobid = shared.getString("jobid", "0");
        userid = shared.getString("userid", "0");
        div_name = shared.getString("div_name", "0");
        range_name = shared.getString("range_name", "0");
        fb_name = shared.getString("fb_name", "0");

        _shareToken = getApplicationContext().getSharedPreferences(userlogin, MODE_PRIVATE);
        _token = _shareToken.getString("token", "0");

        dgpsfbName.setText(fb_name);
        deleteAllZipFile(userid);

        getDataforDisplay(userid);
        deleteTempStaticFile(userid);
        deleteTempRTXFile(userid);
        deleteTempJXLFile(userid);

        //OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.MINUTES)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(900, TimeUnit.SECONDS);
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Request request = chain.request().newBuilder().addHeader("Authorization", "Bearer " + _token).build();
                return chain.proceed(request);
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        jsonPlaceHolderApi = retrofit.create(RetrofitInterface.class);

        /*Data Sync*/
        AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(this);
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (totPoint == syncPoint && totPic == syncPic && totStatic == syncStatic && totRtx == syncRtx && totJxl == syncJxl) {
                    Toast.makeText(DGPSSyncMenuActivity.this, "You don't have anything for Sync...", Toast.LENGTH_SHORT).show();
                } else {
                    //Visualization before Sync
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DGPSSyncMenuActivity.this);
                    //alertDialogBuilder.setMessage("Are you sure to save this pillar data?");
                    final View customLayout = getLayoutInflater().inflate(R.layout.visualization_before_sync, null);
                    alertDialogBuilder.setView(customLayout);
                    visDate = customLayout.findViewById(R.id.surveyedDate);
                    visTotObser = customLayout.findViewById(R.id.totObservPill);
                    visObserv = customLayout.findViewById(R.id.oservPill);
                    visVirtual = customLayout.findViewById(R.id.virtualPill);
                    visMisTagStatic = customLayout.findViewById(R.id.missTagStatic);
                    visTagRTX = customLayout.findViewById(R.id.tagRTX);
                    visTagJXL = customLayout.findViewById(R.id.tagJXL);
                    visReqRe = customLayout.findViewById(R.id.reqResurvey);


                    //Query for visualization
                    String minmaxDate = " and survey_time>=(SELECT min(survey_time) from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "' and (sync_status!=2 or (pillar_jfile_status!='2' or pillar_jfile_status='') or (pillar_rfile_status!='2' or pillar_rfile_status='') or (pillar_sfile_status!='2' or pillar_sfile_status='')) ) and  survey_time<=(SELECT max(survey_time) from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "' and (sync_status!=2 or (pillar_jfile_status!='2' or pillar_jfile_status='') or (pillar_rfile_status!='2' or pillar_rfile_status='') or (pillar_sfile_status!='2' or pillar_sfile_status='')))";
                    db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                    Cursor cursor = db.rawQuery("SELECT substr(min(survey_time),0,11) as minDate ,substr(max(survey_time),0,11) as maxDate from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "' and (sync_status!=2 or (pillar_jfile_status!='2' or pillar_jfile_status='') or (pillar_rfile_status!='2' or pillar_rfile_status='') or (pillar_sfile_status!='2' or pillar_sfile_status=''))", null);
                    cursor.moveToFirst();
                    if (cursor.moveToFirst()) {
                        if (cursor.getString(cursor.getColumnIndex("minDate")).equals(cursor.getString(cursor.getColumnIndex("maxDate")))) {
                            visDate.setText(cursor.getString(cursor.getColumnIndex("minDate")));
                        } else {
                            visDate.setText(cursor.getString(cursor.getColumnIndex("minDate")) + " - " + cursor.getString(cursor.getColumnIndex("maxDate")));
                        }
                    }
                    cursor.close();
                    //total observ query
                    Cursor cursor2 = db.rawQuery("select count(*) as totobserv from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "'" + minmaxDate, null);
                    cursor2.moveToFirst();
                    if (cursor2.moveToFirst()) {
                        visTotObser.setText("Total Observed Pillar : " + cursor2.getString(cursor2.getColumnIndex("totobserv")) + "");
                    }
                    cursor2.close();
                    //observation pill query
                    Cursor cursor3 = db.rawQuery("select count(*) as observ from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "' and survey_status=1" + minmaxDate, null);
                    cursor3.moveToFirst();
                    if (cursor3.moveToFirst()) {
                        visObserv.setText("Observation Pillars : " + cursor3.getString(cursor3.getColumnIndex("observ")) + "");
                        observPillNo = cursor3.getString(cursor3.getColumnIndex("observ"));
                    }
                    cursor3.close();
                    //virtual query
                    Cursor cursor4 = db.rawQuery("select count(*) as virtualPill from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "' and survey_status='0'" + minmaxDate, null);
                    cursor4.moveToFirst();
                    if (cursor4.moveToFirst()) {
                        visVirtual.setText("Inaccessiable Pillars : " + cursor4.getString(cursor4.getColumnIndex("virtualPill")) + "");
                    }
                    cursor4.close();
                    //missing tag static query
                    Cursor cursor5 = db.rawQuery("select count(*) as missStatic from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "' and sync_status!=2 and pillar_sfile_status='0' and survey_status!='0'" + minmaxDate, null);
                    cursor5.moveToFirst();
                    if (cursor5.moveToFirst()) {
                        visMisTagStatic.setText("Missing Tag Static Files : " + cursor5.getString(cursor5.getColumnIndex("missStatic")) + "");
                    }
                    cursor5.close();
                    //missing rtx query
                    Cursor cursor6 = db.rawQuery("select count(*) as missRTX from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "' and sync_status!=2 and pillar_rfile_status='1' and survey_status!='0'" + minmaxDate, null);
                    cursor6.moveToFirst();
                    if (cursor6.moveToFirst()) {

                        if (observPillNo.equals(cursor6.getString(cursor6.getColumnIndex("missRTX")))) {
                            visTagRTX.setText("Missing RTX Files : NO");
                        } else {
                            visTagRTX.setText("Missing RTX Files : YES");
                        }
                    }
                    cursor6.close();
                    //missing jxl query
                    Cursor cursor7 = db.rawQuery("select count(*) as missJXL from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "' and sync_status!=2 and pillar_jfile_status='1' and survey_status!='0'" + minmaxDate, null);
                    cursor7.moveToFirst();
                    if (cursor7.moveToFirst()) {

                        if (observPillNo.equals(cursor7.getString(cursor7.getColumnIndex("missJXL")))) {
                            visTagJXL.setText("Missing JXL Files : NO");
                        } else {
                            visTagJXL.setText("Missing JXL Files : YES");
                        }
                    }
                    cursor7.close();


                    //resurvey query
                    String resurveyPill = "";
                    int count = 0;
                    //Cursor cursor8 = db.rawQuery("select * from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "' and sync_status!=2 and ((pill_solution_type='RTX' and (pill_hor_precision>0.75 or pill_verti_precision>0.85)) or ((pill_solution_type!='RTX' and pill_solution_type!='') and rtx_survey_min<15 ) or (pill_solution_type='' and pill_hor_precision='' and pill_verti_precision=''))", null);
                    Cursor cursor8 = db.rawQuery("select * from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "' and sync_status!=2 and pill_solution_type is NULL", null);
                    cursor8.moveToFirst();
                    if (cursor8.moveToFirst()) {

                        if (cursor8.getCount() > 0) {
                            do {
                                if (count == 0) {
                                    if (cursor8.getString(cursor8.getColumnIndex("pndjv_pill_no")).equals("0")) {
                                        resurveyPill = cursor8.getString(cursor8.getColumnIndex("pill_no"));
                                    } else {
                                        resurveyPill = cursor8.getString(cursor8.getColumnIndex("pill_no")) + "_" + cursor8.getString(cursor8.getColumnIndex("pndjv_pill_no"));
                                    }
                                } else {
                                    if (cursor8.getString(cursor8.getColumnIndex("pndjv_pill_no")).equals("0")) {
                                        resurveyPill = resurveyPill + "," + cursor8.getString(cursor8.getColumnIndex("pill_no"));
                                    } else {
                                        resurveyPill = resurveyPill + "," + cursor8.getString(cursor8.getColumnIndex("pill_no")) + "_" + cursor8.getString(cursor8.getColumnIndex("pndjv_pill_no"));
                                    }

                                }
                                count++;
                            }
                            while (cursor8.moveToNext());
                            visReqRe.setText("Required Re-Survey : " + resurveyPill + "");
                        }
                    } else {
                        visReqRe.setText("*** Re-Survey not Required ***");
                    }
                    cursor8.close();
                    db.close();


                    alertDialogBuilder.setPositiveButton("Proceed to Sync",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    syncProcess();
                                }
                            });

                    alertDialogBuilder.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {

                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });



        /*Data Backup*/
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (totPoint == syncPoint && totPic == syncPic && totStatic == syncStatic && totRtx == syncRtx && totJxl == syncJxl) {
                    backupProcess();
                } else {
                    final View customLayout = getLayoutInflater().inflate(R.layout.pupup_backup_confirm, null);
                    alertDialogBuilder.setView(customLayout);
                    alertDialogBuilder.setPositiveButton("Re- Sync",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    try {
                                        syncProcess();
                                    } catch (Exception ee) {
                                        ee.printStackTrace();
                                    }
                                }
                            });
                    alertDialogBuilder.setNegativeButton("Backup",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    backupProcess();
                                    // Toast.makeText(getApplicationContext(), "You canceled the request...please try again", Toast.LENGTH_LONG).show();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });
    }


    private void syncProcess() {
        ///delete all zip file before sync in same page
        deleteAllZipFile(userid);
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
            if (CheckDataTagging()) {
                if (checkDataForSynchronization()) {
                    BindDGPSData(sharediv, userid);
                } else {
                    Toast.makeText(getApplicationContext(), "There is no attribute data for Synchronization.", Toast.LENGTH_LONG).show();
                    ///new concept
                    staticFileSync();
                }
            } else {
                Toast.makeText(getApplicationContext(), "One or more pillars are not tagged", Toast.LENGTH_LONG).show();
                // rtxjxlstaticProcess();

            }
        } else {
            Toast.makeText(getApplicationContext(), "Internet is not available", Toast.LENGTH_LONG).show();
        }
    }

    private void staticFileSync() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
            if (totStatic == syncStatic) {
                Toast.makeText(this, "There is no Static File for Synchronization.", Toast.LENGTH_SHORT).show();
                rtxFileSync();
            } else {
                boolean s_status = checkFileStatus(userid, sharediv, sharefb);
                if (s_status == true) {
                    String sfile = Environment.getExternalStorageDirectory().toString();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String spath = "/StaticData/StaticData";
                    String zipPath = "/" + sharediv + "_" + sharerange + "_" + sharefb + "_StaticFile" + "_" + userid + "_" + timeStamp + ".zip";
                    String sfinalpath = sfile + spath;
                    String dfinalpath = sfile + zipPath;
                    if (zipFileAtPath(sfinalpath, dfinalpath)) {
                        new DGPSSyncMenuActivity.SyncFiles().execute(dfinalpath + "," + "S");
                    } else {
                        Toast.makeText(this, "Could not zip the Static file...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "There is no Static file for Synchronization", Toast.LENGTH_LONG).show();
                }

            }
        } else {
            Toast.makeText(getApplicationContext(), "Internet is not available", Toast.LENGTH_LONG).show();
        }

    }

    private void rtxFileSync() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
            if (totRtx == syncRtx) {
                Toast.makeText(this, "There is no RTX File for Synchronization.", Toast.LENGTH_SHORT).show();
                jxlFileSync();
            } else {
                boolean r_status = checkRTXFileStatus(userid, sharediv, sharefb);
                if (r_status == true) {
                    String sfile = Environment.getExternalStorageDirectory().toString();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String spath = "/RTXData/RTXData";
                    String zipPath = "/" + sharediv + "_" + sharerange + "_" + sharefb + "_RtxData" + "_" + userid + "_" + timeStamp + ".zip";
                    String sfinalpath = sfile + spath;
                    String dfinalpath = sfile + zipPath;
                    if (zipFileAtPath(sfinalpath, dfinalpath)) {
                        new DGPSSyncMenuActivity.SyncFiles().execute(dfinalpath + "," + "R");
                    } else {
                        Toast.makeText(this, "Could not zip the RTX file...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "There is no RTX file for Synchronization", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Internet is not available", Toast.LENGTH_LONG).show();
        }
    }

    private void jxlFileSync() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
            if (totJxl == syncJxl) {
                Toast.makeText(this, "There is no JXL File for Synchronization.", Toast.LENGTH_SHORT).show();

            } else {
                boolean j_status = checkJXLFileStatus(userid, sharediv, sharefb);
                if (j_status == true) {
                    String sfile = Environment.getExternalStorageDirectory().toString();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String spath = "/JXLFile/JXLFile";
                    String zipPath = "/" + sharediv + "_" + sharerange + "_" + sharefb + "_JXLData" + "_" + userid + "_" + timeStamp + ".zip";
                    String sfinalpath = sfile + spath;
                    String dfinalpath = sfile + zipPath;
                    if (zipFileAtPath(sfinalpath, dfinalpath)) {
                        new DGPSSyncMenuActivity.SyncFiles().execute(dfinalpath + "," + "J");
                    } else {
                        Toast.makeText(this, "Could not zip the JXL file...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "There is no JXL file for Synchronization", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Internet is not available", Toast.LENGTH_LONG).show();
        }
    }

    private void rtxjxlstaticProcess() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
            boolean s_status = checkFileStatus(userid, sharediv, sharefb);
            boolean r_status = checkRTXFileStatus(userid, sharediv, sharefb);
            boolean j_status = checkJXLFileStatus(userid, sharediv, sharefb);
            if (CheckDataTagging()) {
                if (s_status == true) {
                    String sfile = Environment.getExternalStorageDirectory().toString();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String spath = "/StaticData/StaticData";
                    String zipPath = "/" + sharediv + "_" + sharerange + "_" + sharefb + "_StaticFile" + "_" + userid + "_" + timeStamp + ".zip";
                    String sfinalpath = sfile + spath;
                    String dfinalpath = sfile + zipPath;
                    Log.d(dfinalpath, "static file");
                    zipFileAtPath(sfinalpath, dfinalpath);
                    new DGPSSyncMenuActivity.SyncFiles().execute(dfinalpath + "," + "S");
                } else {
                    Toast.makeText(getApplicationContext(), "There is no Static file for Synchronization", Toast.LENGTH_LONG).show();
                }
                if (r_status == true) {
                    String sfile = Environment.getExternalStorageDirectory().toString();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String spath = "/RTXData/RTXData";
                    String zipPath = "/" + sharediv + "_" + sharerange + "_" + sharefb + "_RtxData" + "_" + userid + "_" + timeStamp + ".zip";
                    String sfinalpath = sfile + spath;
                    String dfinalpath = sfile + zipPath;
                    zipFileAtPath(sfinalpath, dfinalpath);
                    new DGPSSyncMenuActivity.SyncFiles().execute(dfinalpath + "," + "R");
                } else {
                    Toast.makeText(getApplicationContext(), "There is no RTX file for Synchronization", Toast.LENGTH_LONG).show();
                }
                if (j_status == true) {
                    String sfile = Environment.getExternalStorageDirectory().toString();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String spath = "/JXLFile/JXLFile";
                    String zipPath = "/" + sharediv + "_" + sharerange + "_" + sharefb + "_JXLData" + "_" + userid + "_" + timeStamp + ".zip";
                    String sfinalpath = sfile + spath;
                    String dfinalpath = sfile + zipPath;
                    zipFileAtPath(sfinalpath, dfinalpath);
                    new DGPSSyncMenuActivity.SyncFiles().execute(dfinalpath + "," + "J");
                } else {
                    Toast.makeText(getApplicationContext(), "There is no JXL file for Synchronization", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "One or more pillars are not tagged.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Internet is not available", Toast.LENGTH_LONG).show();
        }
    }

    private void backupProcess() {
        try {
            progressDialog2 = new ProgressDialog(DGPSSyncMenuActivity.this, R.style.MyAlertDialogStyle);
            progressDialog2.setMessage("Wait we are backing up your files...");
            progressDialog2.show();
            for (int a = 0; a < 4; a++) {
                if (a == 0) {
                    String sfile = Environment.getExternalStorageDirectory().toString();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
                    String spath = "/StaticData";
                    String zipPath = "/TempBackup/" + userid + "_" + timeStamp;
                    String sfinalpath = sfile + spath;
                    String dfinalpath = sfile + zipPath;
                    File src = new File(sfinalpath);
                    File[] Files = src.listFiles();
                    if (Files.length > 0) {
                        backupFileStatus = 1;
                        backUpFile(sfinalpath, dfinalpath);
                    } else {
                        Toast.makeText(getApplicationContext(), "You don't have Static for backup", Toast.LENGTH_LONG).show();
                    }

                } else if (a == 1) {
                    String sfile = Environment.getExternalStorageDirectory().toString();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
                    String spath = "/RTXData";
                    String zipPath = "/TempBackup/" + userid + "_" + timeStamp;
                    String sfinalpath = sfile + spath;
                    String dfinalpath = sfile + zipPath;
                    File src = new File(sfinalpath);
                    File[] Files = src.listFiles();
                    if (Files.length > 0) {
                        backupFileStatus = 1;
                        backUpFile(sfinalpath, dfinalpath);
                    } else {
                        Toast.makeText(getApplicationContext(), "You don't have RTX for backup", Toast.LENGTH_LONG).show();
                    }

                } else if (a == 2) {
                    String sfile = Environment.getExternalStorageDirectory().toString();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
                    String spath = "/JXLFile";
                    String zipPath = "/TempBackup/" + userid + "_" + timeStamp;
                    String sfinalpath = sfile + spath;
                    String dfinalpath = sfile + zipPath;
                    File src = new File(sfinalpath);
                    File[] Files = src.listFiles();
                    if (Files.length > 0) {
                        backupFileStatus = 1;
                        backUpFile(sfinalpath, dfinalpath);
                    } else {
                        Toast.makeText(getApplicationContext(), "You don't have JXL for backup", Toast.LENGTH_LONG).show();
                    }
                } else if (a == 3) {
                    db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                    // Cursor cursor = db.rawQuery("select * from m_fb_dgps_survey_pill_data  order by pill_no", null);
                    Cursor cursor = db.rawQuery("select * from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "' order by pill_no", null);
                    if (cursor.getCount() > 0) {
                        String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
                        String timeStamp1 = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String fileName = sharefbname + userid + timeStamp + ".xls";
                        String sfile = Environment.getExternalStorageDirectory().toString();
                        String zipPath = "/TempBackup/" + fileName;
                        String dfinalpath = sfile + zipPath;

                        try {
                            File file = new File(dfinalpath);
                            WorkbookSettings wbSettings = new WorkbookSettings();
                            wbSettings.setLocale(new Locale("en", "EN"));
                            WritableWorkbook workbook;
                            workbook = Workbook.createWorkbook(file, wbSettings);
                            //Excel sheet name. 0 represents first sheet
                            WritableSheet sheet = workbook.createSheet("PillarList", 0);
                            sheet.addCell(new Label(0, 0, "Boundary Type"));
                            sheet.addCell(new Label(1, 0, "Division ID"));
                            sheet.addCell(new Label(2, 0, "Range ID"));
                            sheet.addCell(new Label(3, 0, "FB ID"));
                            sheet.addCell(new Label(4, 0, "Old Pillar ID"));
                            sheet.addCell(new Label(5, 0, "Job Id"));
                            sheet.addCell(new Label(6, 0, "User Id"));
                            sheet.addCell(new Label(7, 0, "Survey Duration"));
                            sheet.addCell(new Label(8, 0, "Forward Status"));
                            sheet.addCell(new Label(9, 0, "Back Status"));
                            sheet.addCell(new Label(10, 0, "Inwrd Status"));
                            sheet.addCell(new Label(11, 0, "Outward Status"));
                            sheet.addCell(new Label(12, 0, "Device pic Sts"));
                            sheet.addCell(new Label(13, 0, "Patch No"));
                            sheet.addCell(new Label(14, 0, "Ring No"));
                            sheet.addCell(new Label(15, 0, "Forest Person"));
                            sheet.addCell(new Label(16, 0, "Surveyor Name"));
                            sheet.addCell(new Label(17, 0, "Sync Status "));
                            sheet.addCell(new Label(18, 0, "Ack Status"));
                            sheet.addCell(new Label(19, 0, "Delete Status"));
                            sheet.addCell(new Label(20, 0, "Survey Segment"));
                            sheet.addCell(new Label(21, 0, "Completion Status"));
                            sheet.addCell(new Label(22, 0, "Forward Pic Name"));
                            sheet.addCell(new Label(23, 0, "Backward Pic Name"));
                            sheet.addCell(new Label(24, 0, "Inward Pic Name"));
                            sheet.addCell(new Label(25, 0, "Outward Pic Name"));
                            sheet.addCell(new Label(26, 0, "Device Pic Name"));
                            sheet.addCell(new Label(27, 0, "Imei No"));
                            sheet.addCell(new Label(28, 0, "Pill Sfile path"));
                            sheet.addCell(new Label(29, 0, "Pill Sfile Sts"));
                            sheet.addCell(new Label(30, 0, "Frjvc Lat"));
                            sheet.addCell(new Label(31, 0, "Frjvc Lon"));
                            sheet.addCell(new Label(32, 0, "Dgps Pill No"));
                            sheet.addCell(new Label(33, 0, "Survey Date"));
                            sheet.addCell(new Label(34, 0, "Approved Status"));
                            sheet.addCell(new Label(35, 0, "Pillar No."));
                            sheet.addCell(new Label(36, 0, "Pill Rfile Path"));
                            sheet.addCell(new Label(37, 0, "Pill Rfile Sts"));
                            sheet.addCell(new Label(38, 0, "Pill RTX Accuracy"));
                            sheet.addCell(new Label(39, 0, "Reason"));
                            sheet.addCell(new Label(40, 0, "Remark"));
                            sheet.addCell(new Label(41, 0, "Rtx Survey Min"));
                            sheet.addCell(new Label(42, 0, "Rtx Survey Sec"));
                            sheet.addCell(new Label(43, 0, "Survey Status"));
                            sheet.addCell(new Label(44, 0, "Pill Jfile Path"));
                            sheet.addCell(new Label(45, 0, "Pill Jfile Sts"));
                            sheet.addCell(new Label(46, 0, "Pill Seq No."));
                            sheet.addCell(new Label(47, 0, "New Pill Name"));
                            sheet.addCell(new Label(48, 0, "CDLTP Rvst Sts"));
                            sheet.addCell(new Label(49, 0, "Del Sts Web"));
                            //cursor.moveToFirst();
                            if (cursor.moveToFirst()) {
                                do {
                                    int i = cursor.getPosition() + 1;
                                    sheet.addCell(new Label(0, i, cursor.getString(cursor.getColumnIndex("pillar_type_code"))));
                                    sheet.addCell(new Label(1, i, cursor.getString(cursor.getColumnIndex("d_id"))));
                                    sheet.addCell(new Label(2, i, cursor.getString(cursor.getColumnIndex("r_id"))));
                                    sheet.addCell(new Label(3, i, cursor.getString(cursor.getColumnIndex("fb_id"))));
                                    sheet.addCell(new Label(4, i, cursor.getString(cursor.getColumnIndex("d_old_id"))));
                                    sheet.addCell(new Label(5, i, cursor.getString(cursor.getColumnIndex("job_id"))));
                                    sheet.addCell(new Label(6, i, cursor.getString(cursor.getColumnIndex("u_id"))));
                                    sheet.addCell(new Label(7, i, cursor.getString(cursor.getColumnIndex("survey_durn"))));
                                    sheet.addCell(new Label(8, i, cursor.getString(cursor.getColumnIndex("f_pic_status"))));
                                    sheet.addCell(new Label(9, i, cursor.getString(cursor.getColumnIndex("b_pic_status"))));
                                    sheet.addCell(new Label(10, i, cursor.getString(cursor.getColumnIndex("i_pic_status"))));
                                    sheet.addCell(new Label(11, i, cursor.getString(cursor.getColumnIndex("o_pic_status"))));
                                    sheet.addCell(new Label(12, i, cursor.getString(cursor.getColumnIndex("div_pic_status"))));
                                    sheet.addCell(new Label(13, i, cursor.getString(cursor.getColumnIndex("patch_no"))));
                                    sheet.addCell(new Label(14, i, cursor.getString(cursor.getColumnIndex("ring_no"))));
                                    sheet.addCell(new Label(15, i, cursor.getString(cursor.getColumnIndex("forest_person"))));
                                    sheet.addCell(new Label(16, i, cursor.getString(cursor.getColumnIndex("surveyor_name"))));
                                    sheet.addCell(new Label(17, i, cursor.getString(cursor.getColumnIndex("sync_status"))));
                                    sheet.addCell(new Label(18, i, cursor.getString(cursor.getColumnIndex("ack_status"))));
                                    sheet.addCell(new Label(19, i, cursor.getString(cursor.getColumnIndex("delete_status"))));
                                    sheet.addCell(new Label(20, i, cursor.getString(cursor.getColumnIndex("survey_segment"))));
                                    sheet.addCell(new Label(21, i, cursor.getString(cursor.getColumnIndex("completion_status"))));
                                    sheet.addCell(new Label(22, i, cursor.getString(cursor.getColumnIndex("f_pic_name"))));
                                    sheet.addCell(new Label(23, i, cursor.getString(cursor.getColumnIndex("b_pic_name"))));
                                    sheet.addCell(new Label(24, i, cursor.getString(cursor.getColumnIndex("i_pic_name"))));
                                    sheet.addCell(new Label(25, i, cursor.getString(cursor.getColumnIndex("o_pic_name"))));
                                    sheet.addCell(new Label(26, i, cursor.getString(cursor.getColumnIndex("div_pic_name"))));
                                    sheet.addCell(new Label(27, i, cursor.getString(cursor.getColumnIndex("device_imei_no"))));
                                    sheet.addCell(new Label(28, i, cursor.getString(cursor.getColumnIndex("pillar_sfile_path"))));
                                    sheet.addCell(new Label(29, i, cursor.getString(cursor.getColumnIndex("pillar_sfile_status"))));
                                    sheet.addCell(new Label(30, i, cursor.getString(cursor.getColumnIndex("frjvc_lat"))));
                                    sheet.addCell(new Label(31, i, cursor.getString(cursor.getColumnIndex("frjvc_long"))));
                                    sheet.addCell(new Label(32, i, cursor.getString(cursor.getColumnIndex("d_pill_no"))));
                                    sheet.addCell(new Label(33, i, cursor.getString(cursor.getColumnIndex("survey_time"))));
                                    sheet.addCell(new Label(34, i, "0"));
                                    sheet.addCell(new Label(35, i, cursor.getString(cursor.getColumnIndex("pill_no"))));
                                    sheet.addCell(new Label(36, i, cursor.getString(cursor.getColumnIndex("pillar_rfile_path"))));
                                    sheet.addCell(new Label(37, i, cursor.getString(cursor.getColumnIndex("pillar_rfile_status"))));
                                    sheet.addCell(new Label(38, i, "0"));
                                    sheet.addCell(new Label(39, i, cursor.getString(cursor.getColumnIndex("reason"))));
                                    sheet.addCell(new Label(40, i, cursor.getString(cursor.getColumnIndex("remark"))));
                                    sheet.addCell(new Label(41, i, cursor.getString(cursor.getColumnIndex("rtx_survey_min"))));
                                    sheet.addCell(new Label(42, i, cursor.getString(cursor.getColumnIndex("rtx_survey_second"))));
                                    sheet.addCell(new Label(43, i, cursor.getString(cursor.getColumnIndex("survey_status"))));
                                    sheet.addCell(new Label(44, i, cursor.getString(cursor.getColumnIndex("pillar_jfile_path"))));
                                    sheet.addCell(new Label(45, i, cursor.getString(cursor.getColumnIndex("pillar_jfile_status"))));
                                    sheet.addCell(new Label(46, i, "0"));
                                    sheet.addCell(new Label(47, i, cursor.getString(cursor.getColumnIndex("pndjv_pill_no"))));
                                    sheet.addCell(new Label(48, i, "0"));
                                    sheet.addCell(new Label(49, i, "0"));
                                } while (cursor.moveToNext());
                                cursor.close();
                                workbook.write();
                                workbook.close();
                                Toast.makeText(getApplicationContext(), "Data exported in excel sheet", Toast.LENGTH_SHORT).show();

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (RowsExceededException e) {
                            e.printStackTrace();
                        } catch (WriteException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        String backupPath = sfile + "/TempBackup/" + userid + "_" + timeStamp + "/";
                        backUpFile(dfinalpath, backupPath);
                        String zipBackupath = "/Backup/" + userid + "_" + timeStamp1 + ".zip";

                        String zipfinalpath = sfile + zipBackupath;
                        File dir = new File(sfile + "/Backup/");
                        try {
                            if (dir.mkdir()) {
                                System.out.println("Directory created");
                            } else {
                                System.out.println("Directory is not created");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (backupFileStatus == 1) {
                            if (zipFileAtPath(backupPath, zipfinalpath)) {
                                File deleteTemp = new File(sfile + "/TempBackup/");
                                FileUtils.deleteDirectory(deleteTemp);
                                backupFileStatus = 0;
                            }
                        } else {
                            AlertDialog.Builder connectionerrorBuilder = new AlertDialog.Builder(DGPSSyncMenuActivity.this);
                            connectionerrorBuilder.setTitle("Alert !!!");
                            connectionerrorBuilder.setMessage("You have already taken Today's Backup.");
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
                    } else {
                        Toast.makeText(getApplicationContext(), "You don't have any data for Backup", Toast.LENGTH_LONG).show();
                    }
                    cursor.close();
                    db.close();
                }
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            progressDialog2.dismiss();
        }
    }


    private void deleteAllZipFile(String userid) {
        String sfile = Environment.getExternalStorageDirectory().toString();
        File f = new File(sfile);
        File[] files = f.listFiles();
        if (files != null)
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (getFileExtension(file).equals(".zip")) {
                    file.delete();
                }
            }
    }

    private void deleteTempStaticFile(String userid) {
        String sfile = Environment.getExternalStorageDirectory().toString() + "/StaticData/StaticData";
        File f = new File(sfile);
        File[] files = f.listFiles();
        if (files != null)
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                file.delete();
            }

    }

    private void deleteTempRTXFile(String userid) {
        String sfile = Environment.getExternalStorageDirectory().toString() + "/RTXData/RTXData";
        File f = new File(sfile);
        File[] files = f.listFiles();
        if (files != null)
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                file.delete();
            }

    }

    private void deleteTempJXLFile(String userid) {
        String sfile = Environment.getExternalStorageDirectory().toString() + "/JXLFile/JXLFile";
        File f = new File(sfile);
        File[] files = f.listFiles();
        if (files != null)
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                file.delete();
            }

    }

    private static String getFileExtension(File _file) {
        String extension = "";

        try {
            if (_file != null && _file.exists()) {
                String name = _file.getName();
                extension = name.substring(name.lastIndexOf("."));
            }
        } catch (Exception e) {
            extension = "";
        }

        return extension;

    }

    private boolean checkFileStatus(String userid, String sharediv, String sharefb) {
        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
        boolean b = false;
        // String temp = Environment.getExternalStorageDirectory().toString();
        //  String tempStatic = "/StaticData/StaticData";
        String dfinalpath = Environment.getExternalStorageDirectory().toString() + "/StaticData/StaticData";

        d_static = new File(dfinalpath);
        if (!d_static.exists()) {
            d_static.mkdirs();
        }
        Cursor cursor = db.rawQuery("select * from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and d_id='" + sharediv + "' and fb_id='" + sharefb + "' and pillar_sfile_path is not null and (pillar_sfile_status='1') order by pill_no", null);
        try {
            if (cursor.getCount() > 0) {
                b = true;
                if (cursor.moveToFirst()) {
                    do {
                        String a = cursor.getString(cursor.getColumnIndex("pillar_sfile_path"));
                        //copy to temporary static file respective fb
                        File sourceLocation = new File(a);
                        if (sourceLocation.exists()) {
                            String fileName = a.substring(a.lastIndexOf('/') + 1);
                            InputStream in = new FileInputStream(sourceLocation);
                            OutputStream out = new FileOutputStream(dfinalpath + "/" + fileName);

                            // Copy the bits from instream to outstream
                            byte[] buf = new byte[1024];
                            int len;

                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }

                            in.close();
                            out.close();

                            Log.v("", "Copy file successful.");

                        } else {
                            Log.v("", "Copy file failed. Source file missing.");
                        }
                    } while (cursor.moveToNext());
                }
            } else {
                b = false;
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }
        return b;
    }

    private boolean checkRTXFileStatus(String userid, String sharediv, String sharefb) {
        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
        boolean b = false;
        String temp = Environment.getExternalStorageDirectory().toString();
        String tempStatic = "/RTXData/RTXData";
        String dfinalpath = temp + tempStatic;
        d_rtx = new File(dfinalpath);
        if (!d_rtx.exists()) {
            d_rtx.mkdirs();
        }
        Cursor cursor = db.rawQuery("select * from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and d_id='" + sharediv + "' and fb_id='" + sharefb + "' and pillar_rfile_path is not null and (pillar_rfile_status='1')", null);
        try {
            if (cursor.getCount() > 0) {
                b = true;
                if (cursor.moveToFirst()) {
                    do {
                        String a = cursor.getString(cursor.getColumnIndex("pillar_rfile_path"));
                        //copy to temporary rtx file respective fb
                        File sourceLocation = new File(a);
                        if (sourceLocation.exists()) {
                            String fileName = a.substring(a.lastIndexOf('/') + 1);
                            InputStream in = new FileInputStream(sourceLocation);
                            OutputStream out = new FileOutputStream(dfinalpath + "/" + fileName);

                            // Copy the bits from instream to outstream
                            byte[] buf = new byte[1024];
                            int len;

                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }

                            in.close();
                            out.close();

                            Log.v("rtxspecific", "Copy file successful.");

                        } else {
                            Log.v("rtxspecific", "Copy file failed. Source file missing.");
                        }
                    } while (cursor.moveToNext());
                }
            } else {
                b = false;
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }
        return b;
    }

    private boolean checkJXLFileStatus(String userid, String sharediv, String sharefb) {
        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
        boolean b = false;
        String temp = Environment.getExternalStorageDirectory().toString();
        String tempStatic = "/JXLFile/JXLFile";
        String dfinalpath = temp + tempStatic;
        d_jxl = new File(dfinalpath);
        if (!d_jxl.exists()) {
            d_jxl.mkdirs();
        }
        Cursor cursor = db.rawQuery("select * from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and d_id='" + sharediv + "' and fb_id='" + sharefb + "' and pillar_jfile_path is not null and (pillar_jfile_status='1')", null);
        try {
            if (cursor.getCount() > 0) {
                b = true;
                if (cursor.moveToFirst()) {
                    do {
                        String a = cursor.getString(cursor.getColumnIndex("pillar_jfile_path"));
                        //copy to temporary rtx file respective fb
                        File sourceLocation = new File(a);
                        if (sourceLocation.exists()) {
                            String fileName = a.substring(a.lastIndexOf('/') + 1);
                            InputStream in = new FileInputStream(sourceLocation);
                            OutputStream out = new FileOutputStream(dfinalpath + "/" + fileName);

                            // Copy the bits from instream to outstream
                            byte[] buf = new byte[1024];
                            int len;

                            while ((len = in.read(buf)) > 0) {
                                out.write(buf, 0, len);
                            }

                            in.close();
                            out.close();

                            Log.v("jxlspecific", "Copy file successful.");

                        } else {
                            Log.v("jxlspecific", "Copy file failed. Source file missing.");
                        }
                    } while (cursor.moveToNext());
                }
            } else {
                b = false;
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }
        return b;
    }

    private void ZipRTXFolder(File file) {
       /* if(file.isDirectory())
        {
            file.delete();
        }
*/
        progressDialog1 = new ProgressDialog(this, R.style.MyAlertDialogStyle);
        progressDialog1.setMessage("Synchronizing RTX data to server.....");
        progressDialog1.show();
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        Call<Object> responseBodyCall = jsonPlaceHolderApi.sendRTXDataWithFile(Integer.parseInt(sharefb), multipartBody);
        responseBodyCall.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, retrofit2.Response<Object> response) {
                if (response.isSuccessful()) {
                    if (new Gson().toJson(response.body()) == null) {
                        if (response.code() == 409) {
                            Toast.makeText(getApplicationContext(), "File already exist", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Internal server error", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        try {
                            String j_array = new Gson().toJson(response.body());
                            JsonObject jobj = (JsonObject) new JsonParser().parse(j_array);
                            JsonArray arr = (JsonArray) jobj.get("fileStatus");
                            for (int i = 0; i < arr.size(); i++) {
                                JsonObject obj = (JsonObject) arr.get(i);
                                try {
                                    db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                                    String path = "%" + obj.get("chrv_statusName").getAsString();
                                    Cursor c = db.rawQuery("update m_fb_dgps_survey_pill_data set pillar_rfile_status='2' where pillar_rfile_path like '" + path + "'", null);
                                    if (c.getCount() >= 0) {
                                        if (file.delete()) {
                                            progressDialog1.dismiss();
                                            Toast.makeText(DGPSSyncMenuActivity.this, "Data Synchronization successfully completed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    c.close();
                                    db.close();
                                } catch (Exception ee) {
                                    ee.printStackTrace();
                                } finally {
                                    progressDialog1.dismiss();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            progressDialog1.dismiss();
                        }
                    }
                } else {
                    progressDialog1.dismiss();
                    Toast.makeText(getApplicationContext(), response.errorBody().toString(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                progressDialog1.dismiss();
                Toast.makeText(getApplicationContext(), "No response from server", Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean zipFileAtPath(String sourcePath, String toLocation) {
        final int BUFFER = 2048;
        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length() + 1);
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                entry.setTime(sourceFile.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }

    private void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    private boolean CheckDataTagging() {
        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
        boolean b = false;
        //Cursor cursor = db.rawQuery("select * from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and d_id='" + sharediv + "' and (pillar_sfile_status='1' or pillar_sfile_status='2') and (pillar_rfile_status='1' or pillar_rfile_status='2') and (pillar_jfile_status='1' or pillar_jfile_status='2') order by pill_no", null);//and (pillar_jfile_status='1' or pillar_jfile_status='2')
        Cursor cursor = db.rawQuery("select * from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and d_id='" + sharediv + "' and fb_id='" + sharefb + "'and survey_status='1' and (pillar_sfile_status='0' or pillar_rfile_status='0' or pillar_jfile_status='0') order by pill_no", null);
        try {
            if (cursor.getCount() > 0) {
                b = false;
            } else {
                b = true;
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }
        return b;
    }

    private void getDataforDisplay(String userid) {
        try {
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            Cursor cursor = db.rawQuery("select count(id) as totpoint from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and fb_id='" + sharefb + "'", null);
            Cursor cursor1 = db.rawQuery("select count(id) as totsyncpoint from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and fb_id='" + sharefb + "' and sync_status='1' ", null);

            Cursor cursor2 = db.rawQuery("select count(id) as totpic from m_fb_dgps_survey_pill_pic where u_id='" + userid + "' and fb_id='" + sharefb + "'", null);
            Cursor cursor3 = db.rawQuery("select count(id) as totsyncpic from m_fb_dgps_survey_pill_pic where u_id='" + userid + "' and fb_id='" + sharefb + "' and pic_status='1'", null);

            Cursor cursor4 = db.rawQuery("select count(pillar_sfile_path) as totfile from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and fb_id='" + sharefb + "' and survey_status='1' and pillar_sfile_status!='0'", null);
            Cursor cursor5 = db.rawQuery("select count(pillar_sfile_path) as totsyncfile from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and fb_id='" + sharefb + "' and pillar_sfile_status='2'", null);

            Cursor cursor6 = db.rawQuery("select count(distinct pillar_rfile_path) as totrfile from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and fb_id='" + sharefb + "' and d_id='" + sharediv + "'and pillar_rfile_path!= ''", null);
            Cursor cursor7 = db.rawQuery("select count(distinct pillar_rfile_path) as totrsyncfile from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and fb_id='" + sharefb + "' and d_id='" + sharediv + "' and pillar_rfile_path is not null and pillar_rfile_status='2'", null);

            Cursor cursor8 = db.rawQuery("select count(distinct pillar_jfile_path) as totjfile from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and fb_id='" + sharefb + "' and d_id='" + sharediv + "'and pillar_jfile_path!= ''", null);
            Cursor cursor9 = db.rawQuery("select count(distinct pillar_jfile_path) as totjsyncfile from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and fb_id='" + sharefb + "' and d_id='" + sharediv + "' and pillar_jfile_path is not null and pillar_jfile_status='2'", null);


            cursor.moveToFirst();
            cursor1.moveToFirst();
            cursor2.moveToFirst();
            cursor3.moveToFirst();
            cursor4.moveToFirst();
            cursor5.moveToFirst();
            cursor6.moveToFirst();
            cursor7.moveToFirst();
            cursor8.moveToFirst();
            cursor9.moveToFirst();

            if (cursor.moveToFirst()) {
                do {
                    totpoint.setText(cursor.getString(cursor.getColumnIndex("totpoint")));
                    totPoint = Integer.parseInt(cursor.getString(cursor.getColumnIndex("totpoint")));
                } while (cursor.moveToNext());
            }
            if (cursor1.moveToFirst()) {
                do {
                    syncpoint.setText(cursor1.getString(cursor1.getColumnIndex("totsyncpoint")));
                    syncPoint = Integer.parseInt(cursor1.getString(cursor1.getColumnIndex("totsyncpoint")));
                } while (cursor1.moveToNext());
            }
            if (cursor2.moveToFirst()) {
                do {
                    totpic.setText(cursor2.getString(cursor2.getColumnIndex("totpic")));
                    totPic = Integer.parseInt(cursor2.getString(cursor2.getColumnIndex("totpic")));
                } while (cursor2.moveToNext());
            }
            if (cursor3.moveToFirst()) {
                do {
                    syncpic.setText(cursor3.getString(cursor3.getColumnIndex("totsyncpic")));
                    syncPic = Integer.parseInt(cursor3.getString(cursor3.getColumnIndex("totsyncpic")));
                } while (cursor3.moveToNext());
            }
            if (cursor4.moveToFirst()) {
                do {
                    totfolder.setText(cursor4.getString(cursor4.getColumnIndex("totfile")));
                    totStatic = Integer.parseInt(cursor4.getString(cursor4.getColumnIndex("totfile")));

                } while (cursor4.moveToNext());
            }
            if (cursor5.moveToFirst()) {
                do {
                    syncfolder.setText(cursor5.getString(cursor5.getColumnIndex("totsyncfile")));
                    syncStatic = Integer.parseInt(cursor5.getString(cursor5.getColumnIndex("totsyncfile")));
                } while (cursor5.moveToNext());
            }
            if (cursor6.moveToFirst()) {
                do {
                    totsign.setText(cursor6.getString(cursor6.getColumnIndex("totrfile")));
                    totRtx = Integer.parseInt(cursor6.getString(cursor6.getColumnIndex("totrfile")));
                } while (cursor6.moveToNext());
            }
            if (cursor7.moveToFirst()) {
                do {
                    syncattendance.setText(cursor7.getString(cursor7.getColumnIndex("totrsyncfile")));
                    syncRtx = Integer.parseInt(cursor7.getString(cursor7.getColumnIndex("totrsyncfile")));
                } while (cursor7.moveToNext());
            }
            if (cursor8.moveToFirst()) {
                do {
                    totjxl.setText(cursor8.getString(cursor8.getColumnIndex("totjfile")));
                    totJxl = Integer.parseInt(cursor8.getString(cursor8.getColumnIndex("totjfile")));
                } while (cursor8.moveToNext());
            }
            if (cursor9.moveToFirst()) {
                do {
                    syncjxl.setText(cursor9.getString(cursor9.getColumnIndex("totjsyncfile")));
                    syncJxl = Integer.parseInt(cursor9.getString(cursor9.getColumnIndex("totjsyncfile")));
                } while (cursor9.moveToNext());
            }
            cursor.close();
            cursor1.close();
            cursor2.close();
            cursor3.close();
            cursor4.close();
            cursor5.close();
            cursor6.close();
            cursor7.close();
            cursor8.close();
            cursor9.close();
            db.close();
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            db.close();
        }
    }

    private void BindDGPSData(String divid, String userid) {
        try {
            jsonArray = new JSONArray();
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            Cursor cursor = db.rawQuery("select * from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and d_id='" + divid + "' and fb_id='" + sharefb + "' and sync_status ='" + 0 + "' order by pill_no", null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                if (cursor.moveToFirst()) {
                    do {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("id", cursor.getString(cursor.getColumnIndex("pillar_type_code")));
                            json.put("d_id", cursor.getString(cursor.getColumnIndex("d_id")));
                            json.put("r_id", cursor.getString(cursor.getColumnIndex("r_id")));
                            json.put("fb_id", cursor.getString(cursor.getColumnIndex("fb_id")));
                            json.put("pill_no", cursor.getString(cursor.getColumnIndex("pill_no")));
                            json.put("job_id", cursor.getString(cursor.getColumnIndex("job_id")));
                            json.put("u_id", cursor.getString(cursor.getColumnIndex("u_id")));
                            json.put("survey_durn", cursor.getString(cursor.getColumnIndex("survey_durn")));
                            json.put("f_pic_status", cursor.getString(cursor.getColumnIndex("f_pic_status")));
                            json.put("b_pic_status", cursor.getString(cursor.getColumnIndex("b_pic_status")));
                            json.put("i_pic_status", cursor.getString(cursor.getColumnIndex("i_pic_status")));
                            json.put("o_pic_status", cursor.getString(cursor.getColumnIndex("o_pic_status")));
                            json.put("div_pic_status", cursor.getString(cursor.getColumnIndex("div_pic_status")));
                            json.put("patch_no", cursor.getString(cursor.getColumnIndex("patch_no")));
                            json.put("ring_no", cursor.getString(cursor.getColumnIndex("ring_no")));
                            json.put("forest_person", cursor.getString(cursor.getColumnIndex("forest_person")));
                            json.put("surveyor_name", cursor.getString(cursor.getColumnIndex("surveyor_name")));
                            json.put("survey_time", cursor.getString(cursor.getColumnIndex("survey_time")));
                            json.put("div_name", cursor.getString(cursor.getColumnIndex("div_name")));
                            json.put("range_name", cursor.getString(cursor.getColumnIndex("range_name")));
                            json.put("fb_name", cursor.getString(cursor.getColumnIndex("fb_name")));
                            json.put("sync_status", cursor.getString(cursor.getColumnIndex("sync_status")));
                            json.put("ack_status", cursor.getString(cursor.getColumnIndex("ack_status")));
                            json.put("delete_status", cursor.getString(cursor.getColumnIndex("delete_status")));
                            json.put("survey_segment", cursor.getString(cursor.getColumnIndex("survey_segment")));
                            json.put("completion_sts", cursor.getString(cursor.getColumnIndex("completion_sts")));
                            json.put("f_pic_name", cursor.getString(cursor.getColumnIndex("f_pic_name")));
                            json.put("b_pic_name", cursor.getString(cursor.getColumnIndex("b_pic_name")));
                            json.put("i_pic_name", cursor.getString(cursor.getColumnIndex("i_pic_name")));
                            json.put("o_pic_name", cursor.getString(cursor.getColumnIndex("o_pic_name")));
                            json.put("div_pic_name", cursor.getString(cursor.getColumnIndex("div_pic_name")));
                            json.put("device_imei_no", cursor.getString(cursor.getColumnIndex("device_imei_no")));
                            json.put("pillar_sfile_path", cursor.getString(cursor.getColumnIndex("pillar_sfile_path")));
                            json.put("pillar_sfile_status", cursor.getString(cursor.getColumnIndex("pillar_sfile_status")));
                            json.put("frjvc_lat", cursor.getString(cursor.getColumnIndex("frjvc_lat")));
                            json.put("frjvc_long", cursor.getString(cursor.getColumnIndex("frjvc_long")));
                            json.put("d_pill_no", cursor.getString(cursor.getColumnIndex("d_pill_no")));
                            json.put("d_old_id", cursor.getString(cursor.getColumnIndex("d_old_id")));
                            json.put("pillar_rfile_path", cursor.getString(cursor.getColumnIndex("pillar_rfile_path")));
                            json.put("pillar_rfile_status", cursor.getString(cursor.getColumnIndex("pillar_rfile_status")));
                            json.put("completion_status", cursor.getString(cursor.getColumnIndex("completion_status")));
                            json.put("rtx_survey_min", cursor.getString(cursor.getColumnIndex("rtx_survey_min")));
                            json.put("rtx_survey_second", cursor.getString(cursor.getColumnIndex("rtx_survey_second")));
                            json.put("survey_status", cursor.getString(cursor.getColumnIndex("survey_status")));
                            json.put("reason", cursor.getString(cursor.getColumnIndex("reason")));
                            json.put("remark", cursor.getString(cursor.getColumnIndex("remark")));
                            json.put("pillar_jfile_path", cursor.getString(cursor.getColumnIndex("pillar_jfile_path")));
                            json.put("pillar_jfile_status", cursor.getString(cursor.getColumnIndex("pillar_jfile_status")));
                            json.put("pndjv_pill_no", cursor.getString(cursor.getColumnIndex("pndjv_pill_no")));
                            jsonArray.put(json);
                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }

                    } while (cursor.moveToNext());
                }
                sendDatatoServer(jsonArray);
            } else {
                Toast.makeText(getApplicationContext(), "You dont have any data for Synchronization", Toast.LENGTH_LONG).show();
            }
            cursor.close();
            db.close();
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            db.close();
        }
    }

    private void sendDatatoServer(JSONArray jsonArray) {
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
                try {
                    progressDialog1 = new ProgressDialog(this, R.style.MyAlertDialogStyle);
                    progressDialog1.setMessage("Uploading DGPS pillar data to server.....");
                    progressDialog1.show();
                    fp_data = new JSONObject();
                    fp_data.put("fpdata", jsonArray);
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    String URL = BuildConfig.DGPS_D_FB_PILL_DATA_API;
                    requestQueue.getCache().remove(URL);
                    final String requestBody = fp_data.toString();
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.equals("200")) {
                                try {
                                    db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                                    Cursor c = db.rawQuery("update m_fb_dgps_survey_pill_data set sync_status='1' where u_id='" + userid + "' and d_id='" + sharediv + "' and fb_id='" + sharefb + "'", null);
                                    if (c.getCount() >= 0) {
                                        progressDialog1.dismiss();
                                        Toast.makeText(DGPSSyncMenuActivity.this, "Data Synchronization successfully completed", Toast.LENGTH_SHORT).show();
                                    }
                                    rtxjxlstaticProcess();
                                    c.close();
                                    db.close();
                                } catch (Exception ee) {
                                    ee.printStackTrace();
                                } finally {
                                    progressDialog1.dismiss();
                                    recreate();
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog1.dismiss();

                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Content-Type", "application/json; charset=UTF-8");
                            params.put("Authorization", "Bearer " + _token);
                            return params;
                        }

                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public byte[] getBody() throws AuthFailureError {
                            try {
                                return requestBody == null ? null : requestBody.getBytes("utf-8");
                            } catch (UnsupportedEncodingException uee) {
                                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                                return null;
                            }
                        }

                        @Override
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            String responseString = "";
                            if (response != null) {
                                responseString = String.valueOf(response.statusCode);
                                // can get more details such as response.headers
                            }
                            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                        }
                    };
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                            30000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(DGPSSyncMenuActivity.this, "You do not have Internet Connection", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    private boolean checkDataForSynchronization() {
        ///modification
        ///add fb id in query where clause
        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
        boolean b = false;
        //db.rawQuery("update m_fb_dgps_survey_pill_data set sync_status='0' where u_id='" + userid + "' and d_id='" + sharediv + "'",null);
        Cursor cursor = db.rawQuery("select * from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and d_id='" + sharediv + "' and sync_status ='" + 0 + "'  and pillar_sfile_path is not null order by pill_no", null);
        try {
            if (cursor.getCount() > 0) {
                b = true;
            } else {
                b = false;
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            cursor.close();
            db.close();
        }
        return b;
    }

    public void backUpFile(String sourcePath, String toLocation) {
        try {
            File src = new File(sourcePath);
            File dst = new File(toLocation, src.getName());

            if (src.isDirectory()) {

                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    backUpFile(src1, dst1);
                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
            if (sourceFile.exists()) {
                sourceFile.delete();
            }
        }
    }

    private class SyncFiles extends AsyncTask<String, String, String> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = ProgressDialog.show(DGPSSyncMenuActivity.this, "", "Please wait...We are processing and syncing the files", false);
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... strings) {
            String files[] = strings[0].split(",");
            File f = new File(files[0]);
            Call<Object> responseBodyCall = null;
            if (files[1].equals("S")) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
                MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("file", f.getName(), requestFile);
                responseBodyCall = jsonPlaceHolderApi.sendDataWithFile(Integer.parseInt(sharefb), multipartBody);
            } else if (files[1].equals("R")) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
                MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("file", f.getName(), requestFile);
                responseBodyCall = jsonPlaceHolderApi.sendRTXDataWithFile(Integer.parseInt(sharefb), multipartBody);
            } else {
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
                MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("file", f.getName(), requestFile);
                responseBodyCall = jsonPlaceHolderApi.sendJXLDataWithFile(Integer.parseInt(sharefb), multipartBody);
            }
            responseBodyCall.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, retrofit2.Response<Object> response) {
                    try {
                        if (response.isSuccessful()) {
                            if (new Gson().toJson(response.body()) == null) {
                                if (response.code() == 409) {
                                    Toast.makeText(getApplicationContext(), "File already exist", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Internal server error", Toast.LENGTH_LONG).show();
                                }
                            } else if (response.code() == 409) {
                                Toast.makeText(getApplicationContext(), "File already exist", Toast.LENGTH_LONG).show();
                            } else {
                                try {
                                    String j_array = new Gson().toJson(response.body());
                                    JsonObject jobj = (JsonObject) new JsonParser().parse(j_array);
                                    JsonArray arr = (JsonArray) jobj.get("fileStatus");///till now it is execute and data send to server success but not update in phone
                                    for (int i = 0; i < arr.size(); i++) {
                                        JsonObject obj = (JsonObject) arr.get(i);
                                        try {
                                            int count = 0;
                                            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                                            String path = "%" + obj.get("chrv_statusName").getAsString();
                                            Cursor c = null;
                                            if (files[1].equals("S")) {
                                                c = db.rawQuery("update m_fb_dgps_survey_pill_data set pillar_sfile_status='2' where pillar_sfile_path like '" + path + "'", null);
                                                if (c.getCount() >= 0) {
                                                    Log.d("staticresponse", "done");
                                                }
                                            }
                                            if (files[1].equals("R")) {
                                                c = db.rawQuery("update m_fb_dgps_survey_pill_data set pillar_rfile_status='2' where pillar_rfile_path like '" + path + "'", null);
                                                if (c.getCount() >= 0) {
                                                    Log.d("rtxresponse", "done");
                                                }
                                            }
                                            if (files[1].equals("J")) {
                                                c = db.rawQuery("update m_fb_dgps_survey_pill_data set pillar_jfile_status='2' where pillar_jfile_path like '" + path + "'", null);
                                                if (c.getCount() >= 0) {
                                                    Log.d("jxlresponse", "done");
                                                }
                                            }
                                            if (i == arr.size() - 1) {
                                                if (files[1].equals("S")) {
                                                    if (f.delete()) {
                                                        Toast.makeText(DGPSSyncMenuActivity.this, "Static Data Synchronization successfully completed", Toast.LENGTH_SHORT).show();
                                                    }
                                                    rtxFileSync();
                                                } else if (files[1].equals("R")) {
                                                    if (f.delete()) {
                                                        Toast.makeText(DGPSSyncMenuActivity.this, "RTX Data Synchronization successfully completed", Toast.LENGTH_SHORT).show();
                                                    }
                                                    jxlFileSync();
                                                } else if (files[1].equals("J")) {
                                                    if (f.delete()) {
                                                        Toast.makeText(DGPSSyncMenuActivity.this, "JXL Data Synchronization successfully completed", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                                c.close();
                                                db.close();
                                                recreate();//has to be tested
                                            }
                                        } catch (Exception ee) {
                                            Toast.makeText(DGPSSyncMenuActivity.this, ee.toString(), Toast.LENGTH_SHORT).show();
                                            ee.printStackTrace();
                                        } finally {
                                           /* if (arr.size() > 0 && i == arr.size() - 1) {
                                                progressDialog1.dismiss();
                                            }*/
                                        }
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(DGPSSyncMenuActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                } finally {
                                    //progressDialog1.dismiss();
                                }
                            }
                        } else {
                            if (response.code() == 409) {
                                if (files[1].equals("S")) {
                                    Toast.makeText(getApplicationContext(), "Static File already Synced", Toast.LENGTH_LONG).show();
                                } else if (files[1].equals("R")) {
                                    Toast.makeText(getApplicationContext(), "RTX File already Synced", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "JXL File already Synced", Toast.LENGTH_LONG).show();
                                }


                            }
                            //progressDialog1.dismiss();
                           /* String a = response.errorBody().toString();
                            Toast.makeText(getApplicationContext(), response.errorBody().toString(), Toast.LENGTH_SHORT).show();*/
                        }
                    } catch (Exception ee) {
                        ee.printStackTrace();
                        Toast.makeText(DGPSSyncMenuActivity.this, ee.toString(), Toast.LENGTH_SHORT).show();
                    } finally {
                        //progressDialog1.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    //progressDialog1.dismiss();
                    Toast.makeText(getApplicationContext(), "No response from server..Please Sync Again.", Toast.LENGTH_LONG).show();
                }
            });
            return null;

        }

        protected void onProgressUpdate(String... progress) {
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String message) {
            this.progressDialog.dismiss();
        }
    }

    public void callSyncDialog(Context context){
        try {
            Dialog dialog=new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.sync_dialog);

            ImageView close_img;
            close_img=dialog.findViewById(R.id.close_img);

            close_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });


            dialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
