package com.sparc.frjvcapp;

import static com.sparc.frjvcapp.DbHelper.DATABASE_NAME;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.gson.Gson;
import com.sparc.frjvcapp.pojo.Response1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.HttpsURLConnection;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserProfileActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    public static final String userlogin = "coltlogin";
    public static final String token = "userlogin";
    private static final int WRITE_REQUEST_CODE = 300;
    private static final String TAG = UserProfileActivity.class.getSimpleName();
    TextView name, design, email, circle, division, totdiv, totrange, totfb, totfp;
    DbHelper dbHelper;
    String divid, userid, path,_token;
    JSONArray jsonArray;
    JSONObject fp_data;
    ProgressDialog progressDialog1,ProgreesDialog2;
    boolean doubleBackToExitPressedOnce = false;
    ImageView logout;
    private Button record, sync,back_up_image;
    private DbHelper.DatabaseHelper mDbHelper;
    SharedPreferences shared,_shareToken;
    public static final String userloginn = "userlogin";
    SQLiteDatabase db,db1, db2, db3, db4;
    String div_id, user_id,closeTimeFlag="",backup_path="";
    Handler handler=new Handler();
    Handler handler_new = new Handler();
    Runnable runnable;
    private int delayTime=400;
    int backupFileStatus = 0;
    private static int BUFFER_SIZE = 6 * 1024;
    LinearLayout loading_ll,path_ll;
    TextView loading_text,path_txt;
    Button ok_btn;
    ProgressBar progressB;
    int imageCount=0,pillar_reg_count=0,shifting_pillar_count=0;
    File tempBackupDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        dbHelper = new DbHelper(this);
        design = findViewById(R.id.design);
        name = findViewById(R.id.user_profile_name);
        record = findViewById(R.id.record);
        email = findViewById(R.id.email);
        circle = findViewById(R.id.circle);
        division = findViewById(R.id.div);
        //view = findViewById(R.id.view);
        sync = findViewById(R.id.sync);
        back_up_image = findViewById(R.id.back_up_image);
        totdiv = findViewById(R.id.totdiv);
        totrange = findViewById(R.id.totrange);
        totfb = findViewById(R.id.totfb);
        totfp = findViewById(R.id.totfp);
        loading_ll = findViewById(R.id.loading_ll);
        loading_text = findViewById(R.id.loading_text);
        path_ll = findViewById(R.id.path_ll);
        path_txt = findViewById(R.id.path_txt);
        ok_btn = findViewById(R.id.ok_btn);
        progressB = findViewById(R.id.progressB);


        SharedPreferences shared = getSharedPreferences(userlogin, MODE_PRIVATE);
        name.setText(shared.getString("uname", "0"));
        design.setText(shared.getString("upos", "0"));
        email.setText(shared.getString("uid", "0"));
        circle.setText(shared.getString("ucir", "0"));
        division.setText(shared.getString("udivname", "0"));
        divid = shared.getString("udivid", "0");
        userid = shared.getString("uemail", "0");

        _shareToken = getSharedPreferences(token, MODE_PRIVATE);
        _token=_shareToken.getString("token","0");

        checkForDialog();//for loader showing
        new imgaeSync().execute("");//for image sync JV

        deleteAllZipFile(userid);

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchMapFile.class);
                startActivity(intent);
                finish();
            }
        });
        sync.setOnClickListener(new View.OnClickListener() {
            Cursor cursor,cursor1,cursor2;
            @Override
            public void onClick(View v) {
                db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                try {
                    cursor = db.rawQuery("select * from m_pillar_reg where uid='" + userid + "' and d_id='" + divid + "' and p_sts ='" + 0 + "' order by point_no", null);
                    cursor1 = db.rawQuery("select * from m_survey_pillar_reg where uid='" + userid + "' and d_id='" + divid + "' and p_sts ='" + 0 + "' order by point_no", null);
                    cursor2 = db.rawQuery("select * from m_shifting_pillar_reg where uid='" + userid + "' and sync_status ='" + 0 + "'", null);
                    if (cursor.getCount() > 0) {
                        getDataforSync(divid, userid);
                    } else {
                        Toast.makeText(getApplicationContext(), "You dont have any data for Synchronization", Toast.LENGTH_LONG).show();
                    }
                    if (cursor1.getCount() > 0) {

                        getResurveyedPillarData(divid, userid);
                    } else {
                        Toast.makeText(getApplicationContext(), "You dont have any data for Synchronization", Toast.LENGTH_LONG).show();
                    }
                    if (cursor2.getCount() > 0) {
                        getShiftinhDataforSync(userid);
                    } else {
                        Toast.makeText(getApplicationContext(), "You dont have any data for Synchronization", Toast.LENGTH_LONG).show();
                    }
                }catch (Exception ee)
                {
                    ee.printStackTrace();
                }finally {
                    cursor.close();
                    cursor1.close();
                    cursor2.close();
                    db.close();

                }
            }
        });
        try {
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            Cursor cursor = db.rawQuery("select count(id) as totufb  from m_pillar_reg where uid='" + shared.getString("uemail", "0") + "' and p_sts='1' and img_status='1'", null);
            // Cursor cursor4 = db.rawQuery("select * from m_pillar_reg where uid='" + shared.getString("uemail", "0") + "'", null);
            Cursor cursor1 = db.rawQuery("select count(distinct r_id) as totrange from m_pillar_reg where uid='" + shared.getString("uemail", "0") + "'", null);
            Cursor cursor2 = db.rawQuery("select count(distinct fb_id) as totfb  from m_pillar_reg where uid='" + shared.getString("uemail", "0") + "'", null);
            Cursor cursor3 = db.rawQuery("select count(id) as totfp from m_pillar_reg where uid='" + shared.getString("uemail", "0") + "' and delete_status='0'", null);
            cursor.moveToFirst();
            cursor1.moveToFirst();
            cursor2.moveToFirst();
            cursor3.moveToFirst();
            if (cursor.moveToFirst()) {
                do {
                    totdiv.setText(cursor.getString(cursor.getColumnIndex("totufb")));
                } while (cursor.moveToNext());
            }
            if (cursor1.moveToFirst()) {
                do {
                    totrange.setText(cursor1.getString(cursor1.getColumnIndex("totrange")));
                } while (cursor1.moveToNext());
            }
            if (cursor2.moveToFirst()) {
                do {
                    totfb.setText(cursor2.getString(cursor2.getColumnIndex("totfb")));
                } while (cursor2.moveToNext());
            }
            if (cursor3.moveToFirst()) {
                do {
                    totfp.setText(cursor3.getString(cursor3.getColumnIndex("totfp")));
                } while (cursor3.moveToNext());
            }
            cursor.close();
            cursor1.close();
            cursor2.close();
            cursor3.close();
            db.close();
        }catch (Exception ee)
        {
            ee.printStackTrace();
        }
//        back_up_image.setVisibility(View.GONE);
        back_up_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

//                    backUpProcess();
                    int all_data_excel_count=getType_dataSize("excel_all_data");
                    if (all_data_excel_count>0) {
                        loading_ll.setVisibility(View.VISIBLE);
                        loading_text.setText("Please wait for backup your data...!");
//                        path_txt.clearAnimation();
                    }
                    getDataforExcel(user_id);



                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loading_ll.setVisibility(View.GONE);

                loading_text.setVisibility(View.VISIBLE);
                progressB.setVisibility(View.VISIBLE);
                path_ll.setVisibility(View.GONE);
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    //Toast.makeText(addAlarm.this, "Permission denied to access your location.", Toast.LENGTH_SHORT).show();
                }
            }
        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, MainActivity.this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        //Download the file once permission is granted
        //url = editTextUrl.getText().toString();
        //new DownloadFile().execute(url);
        getCMVMMVData();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // Log.d(TAG, "Permission has been denied");
    }

    public void getCMVMMVData() {
        try {
            dbHelper.open();
            ArrayList<String> mfb = dbHelper.getCMVMMVFiles(divid);
            for (int i = 0; i < mfb.size(); i++) {

                new DownloadFile().execute(BuildConfig.F_KML_API + mfb.get(i));

            }
            dbHelper.close();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    private void getDataforSync(String divid, String userid) {
    try {
        jsonArray = new JSONArray();
        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("select * from m_pillar_reg where uid='" + userid + "' and d_id='" + divid + "' and p_sts ='" + 0 + "' order by point_no", null);
        if (cursor.getCount() > 0) {

            cursor.moveToFirst();
            if (cursor.moveToFirst()) {

                //progressDialog1 = ProgressDialog.show(UserProfileActivity.this, "", "Uploading files to server.....", false);
                do {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("id", cursor.getString(cursor.getColumnIndex("point_no")));
                        json.put("d_id", cursor.getString(cursor.getColumnIndex("d_id")));
                        json.put("r_id", cursor.getString(cursor.getColumnIndex("r_id")));
                        json.put("fb_id", cursor.getString(cursor.getColumnIndex("fb_id")));
                        json.put("p_sl_no", cursor.getString(cursor.getColumnIndex("p_sl_no")));
                        json.put("p_lat", cursor.getString(cursor.getColumnIndex("p_lat")));
                        json.put("p_long", cursor.getString(cursor.getColumnIndex("p_long")));
                        json.put("p_type", cursor.getString(cursor.getColumnIndex("p_type")));
                        json.put("p_cond", cursor.getString(cursor.getColumnIndex("p_cond")));
                        json.put("p_rmk", cursor.getString(cursor.getColumnIndex("p_rmk")));
                        json.put("p_pic", cursor.getString(cursor.getColumnIndex("p_pic")));
                        json.put("patch_no", cursor.getString(cursor.getColumnIndex("patch_no")));
                        json.put("ring_no", cursor.getString(cursor.getColumnIndex("ring_no")));
                        json.put("p_loc_type", cursor.getString(cursor.getColumnIndex("p_loc_type")));
                        json.put("p_no", cursor.getString(cursor.getColumnIndex("p_no")));
                        json.put("p_paint_status", cursor.getString(cursor.getColumnIndex("p_paint_status")));
                        json.put("fb_name", cursor.getString(cursor.getColumnIndex("fb_name")));
                        json.put("uid", cursor.getString(cursor.getColumnIndex("uid")));
                        json.put("img_status", cursor.getString(cursor.getColumnIndex("img_status")));
                        json.put("delete_status", cursor.getString(cursor.getColumnIndex("delete_status")));
                        json.put("shifting_status", cursor.getString(cursor.getColumnIndex("shifting_status")));
                        json.put("survey_dir", cursor.getString(cursor.getColumnIndex("surv_direction")));
                        json.put("accuracy", cursor.getString(cursor.getColumnIndex("p_accuracy")));
                        json.put("survey_dt", cursor.getString(cursor.getColumnIndex("survey_dt")));
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
    }catch (Exception ee)
    {
        ee.printStackTrace();
    }

    }
    private void getResurveyedPillarData(String divid, String userid) {
    try {
        jsonArray = new JSONArray();
        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("select * from m_survey_pillar_reg where uid='" + userid + "' and d_id='" + divid + "' and p_sts ='" + 0 + "' order by point_no", null);
        if (cursor.getCount() > 0) {

            cursor.moveToFirst();
            if (cursor.moveToFirst()) {
                /*progressDialog1 = new ProgressDialog(this, R.style.MyAlertDialogStyle);
                progressDialog1.setMessage("Uploading files to Server.....");
                progressDialog1.show();*/
                //progressDialog1 = ProgressDialog.show(UserProfileActivity.this, "", "Uploading files to server.....", false);
                do {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("id", cursor.getString(cursor.getColumnIndex("point_no")));
                        json.put("d_id", cursor.getString(cursor.getColumnIndex("d_id")));
                        json.put("r_id", cursor.getString(cursor.getColumnIndex("r_id")));
                        json.put("fb_id", cursor.getString(cursor.getColumnIndex("fb_id")));
                        json.put("p_sl_no", cursor.getString(cursor.getColumnIndex("p_sl_no")));
                        json.put("p_lat", cursor.getString(cursor.getColumnIndex("p_lat")));
                        json.put("p_long", cursor.getString(cursor.getColumnIndex("p_long")));
                        json.put("p_type", cursor.getString(cursor.getColumnIndex("p_type")));
                        json.put("p_cond", cursor.getString(cursor.getColumnIndex("p_cond")));
                        json.put("p_rmk", cursor.getString(cursor.getColumnIndex("p_rmk")));
                        json.put("p_pic", cursor.getString(cursor.getColumnIndex("p_pic")));
                        json.put("patch_no", cursor.getString(cursor.getColumnIndex("patch_no")));
                        json.put("ring_no", cursor.getString(cursor.getColumnIndex("ring_no")));
                        json.put("p_loc_type", cursor.getString(cursor.getColumnIndex("p_loc_type")));
                        json.put("p_no", cursor.getString(cursor.getColumnIndex("p_no")));
                        json.put("p_paint_status", cursor.getString(cursor.getColumnIndex("p_paint_status")));
                        json.put("fb_name", cursor.getString(cursor.getColumnIndex("fb_name")));
                        json.put("uid", cursor.getString(cursor.getColumnIndex("uid")));
                        json.put("img_status", cursor.getString(cursor.getColumnIndex("img_status")));
                        json.put("delete_status", cursor.getString(cursor.getColumnIndex("delete_status")));
                        json.put("shifting_status", cursor.getString(cursor.getColumnIndex("shifting_status")));
                        json.put("past_lat", cursor.getString(cursor.getColumnIndex("past_lat")));
                        json.put("past_long", cursor.getString(cursor.getColumnIndex("past_long")));
                        json.put("survey_dir", cursor.getString(cursor.getColumnIndex("surv_direction")));
                        json.put("accuracy", cursor.getString(cursor.getColumnIndex("p_accuracy")));
                        json.put("survey_dt", cursor.getString(cursor.getColumnIndex("survey_dt")));
                        jsonArray.put(json);
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }

                } while (cursor.moveToNext());

            }
            sendOtherSurveyDatatoServer(jsonArray);
        } else {
            Toast.makeText(getApplicationContext(), "You dont have any data for Synchronization", Toast.LENGTH_LONG).show();
        }
        cursor.close();
        db.close();

    }catch (Exception ee)
    {
        ee.printStackTrace();
    }

    }
    private void getShiftinhDataforSync(String userid) {
        try {
            jsonArray = new JSONArray();
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            Cursor cursor = db.rawQuery("select * from m_shifting_pillar_reg where uid='" + userid + "' and sync_status ='" + 0 + "'", null);
            if (cursor.getCount() > 0) {

                cursor.moveToFirst();
                if (cursor.moveToFirst()) {
               /* ProgreesDialog2 = new ProgressDialog(this , R.style.MyAlertDialogStyle);
                ProgreesDialog2.setMessage("Uploading files to Server.....");
                ProgreesDialog2.show();*/
                    //ProgreesDialog2 = ProgressDialog.show(UserProfileActivity.this, "", "Uploading files to server.....", false);
                    do {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("slat", cursor.getString(cursor.getColumnIndex("s_lat")));
                            json.put("slong", cursor.getString(cursor.getColumnIndex("s_long")));
                            json.put("sremark", cursor.getString(cursor.getColumnIndex("s_rmk")));
                            json.put("spic", cursor.getString(cursor.getColumnIndex("s_pic")));
                            json.put("spicstatus", cursor.getString(cursor.getColumnIndex("simg_status")));
                            json.put("sfbname", cursor.getString(cursor.getColumnIndex("fb_name")));
                            json.put("suid", cursor.getString(cursor.getColumnIndex("uid")));
                            json.put("sfbid", cursor.getString(cursor.getColumnIndex("fb_id")));
                            json.put("spno", cursor.getString(cursor.getColumnIndex("p_no")));
                            json.put("ssyncsts", cursor.getString(cursor.getColumnIndex("sync_status")));
                            json.put("sdelsts", cursor.getString(cursor.getColumnIndex("sdelete_status")));
                            jsonArray.put(json);
                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }

                    } while (cursor.moveToNext());

                }
            } else {
                Toast.makeText(getApplicationContext(), "You dont have any data for Synchronization", Toast.LENGTH_LONG).show();
            }
            cursor.close();
            db.close();
            sendShiftingDatatoServer(jsonArray);
        }catch (Exception ee)
        {
            ee.printStackTrace();
        }
    }
    public void sendDatatoServer(JSONArray jsonArray) {
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
                try {
                    progressDialog1 = new ProgressDialog(this, R.style.MyAlertDialogStyle);
                    progressDialog1.setMessage("Uploading files to Server.....");
                    progressDialog1.show();
                    fp_data = new JSONObject();
                    fp_data.put("fpdata", jsonArray);
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    String URL = BuildConfig.F_UDT_PILL_DATA;
                    requestQueue.getCache().remove(URL);
                    final String requestBody = fp_data.toString();
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // SendImageFile();
                            try {
                                db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                                Cursor c = db.rawQuery("update m_pillar_reg set p_sts='1' where uid='" + userid + "' and d_id='" + divid + "'", null);
                                if (c.getCount() >= 0) {
                                    progressDialog1.dismiss();
                                    Toast.makeText(UserProfileActivity.this, "Data Synchronization successfully completed", Toast.LENGTH_SHORT).show();
                                }
                                try {
                                    Handler handler=new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            UserProfileActivity.this.recreate();
                                        }
                                    },4000);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }

                                c.close();
                                db.close();
                            } catch (Exception ee) {
                                ee.printStackTrace();
                            }
                            finally {
                                progressDialog1.dismiss();
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
                            params.put("Authorization", "Bearer "+_token);
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
                Toast.makeText(UserProfileActivity.this, "You do not have Internet Connection", Toast.LENGTH_LONG).show();
            }
        }catch (Exception ee)
        {
            ee.printStackTrace();
        }
    }
    public void sendOtherSurveyDatatoServer(JSONArray jsonArray) {
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
                try {
                    progressDialog1 = new ProgressDialog(this, R.style.MyAlertDialogStyle);
                    progressDialog1.setMessage("Uploading files to Server.....");
                    progressDialog1.show();
                    fp_data = new JSONObject();
                    fp_data.put("fpdata", jsonArray);
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    String URL = BuildConfig.F_CFM_UDT_PILL_DATA;
                    requestQueue.getCache().remove(URL);
                    final String requestBody = fp_data.toString();
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // SendImageFile();
                            try {
                                db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                                Cursor c = db.rawQuery("update m_survey_pillar_reg set p_sts='1' where uid='" + userid + "' and d_id='" + divid + "'", null);
                                if (c.getCount() >= 0) {
                                    progressDialog1.dismiss();
                                    Toast.makeText(UserProfileActivity.this, "Data Synchronization successfully completed", Toast.LENGTH_SHORT).show();
                                }
                                c.close();
                                db.close();
                            } catch (Exception ee) {
                                ee.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Content-Type", "application/json; charset=UTF-8");
                            params.put("Authorization", "Bearer "+_token);
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
                Toast.makeText(UserProfileActivity.this, "You do not have Internet Connection", Toast.LENGTH_LONG).show();
            }
        }catch (Exception ee)
        {
            ee.printStackTrace();
        }
    }
    public void sendShiftingDatatoServer(JSONArray jsonArray) {
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
                try {
                    progressDialog1 = new ProgressDialog(this, R.style.MyAlertDialogStyle);
                    progressDialog1.setMessage("Uploading files to Server.....");
                    progressDialog1.show();
                    fp_data = new JSONObject();
                    fp_data.put("sfpdata", jsonArray);
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    String URL = BuildConfig.F_SFT_FRJVC_PILL_DATA;
                    requestQueue.getCache().remove(URL);
                    final String requestBody = fp_data.toString();
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // SendImageFile();
                            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                            Cursor c = db.rawQuery("update m_shifting_pillar_reg set sync_status='1' where uid='" + userid + "'", null);
                            if (c.getCount() >= 0) {
                                progressDialog1.dismiss();
                                Toast.makeText(UserProfileActivity.this, "Data Synchronization successfully completed", Toast.LENGTH_SHORT).show();
                            }
                            c.close();
                            db.close();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Content-Type", "application/json; charset=UTF-8");
                            params.put("Authorization", "Bearer "+_token);
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
                Toast.makeText(UserProfileActivity.this, "You do not have Internet Connection", Toast.LENGTH_LONG).show();
            }
        }catch (Exception ee)
        {
            ee.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainContainerActivity.class);
        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

   /* private void uploadImage(byte[] imageBytes, final String imgname) {
        progress = new progressdialog(userprofileactivity.this, r.style.mytheme);
        progress.setcancelable(false);
        progress.setprogressstyle(android.r.style.widget_holo_progressbar);
        progress.show();
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://203.129.207.130:5067/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
                MultipartBody.Part body = null;

                body = MultipartBody.Part.createFormData("image", imgname, requestFile);

                Call<Response1> call = retrofitInterface.uploadImage(body);
                // mProgressBar.setVisibility(View.VISIBLE);
                call.enqueue(new Callback<Response1>() {
                    @Override
                    public void onResponse(Call<Response1> call, retrofit2.Response<Response1> response) {
                        if (response.isSuccessful()) {
                            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                            Cursor c = db.rawQuery("update m_pillar_reg set img_status='1' where uid='" + userid + "' and d_id='" + divid + "' and p_pic='" + imgname + "'", null);
                            int aa = c.getCount();
                            if (c.getCount() >= 0) {
                                Response1 responseBody = response.body();
                                Toast.makeText(UserProfileActivity.this, responseBody.getPath(), Toast.LENGTH_SHORT).show();
                            }
                            c.close();
                            db.close();
                            //SendImageFile();
                        } else {
                            ResponseBody errorBody = response.errorBody();
                            Gson gson = new Gson();
                            try {
                                Response errorResponse = gson.fromJson(errorBody.string(), Response.class);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Response1> call, Throwable t) {

                    }

                });
            } else {
                Toast.makeText(UserProfileActivity.this, "Internet Connection is Not Available", Toast.LENGTH_LONG).show();
            }
        }catch (Exception ee)
        {
            ee.printStackTrace();
        }
    }*/
   /* public void SendImageFile() {
        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
        Cursor co = db.rawQuery("update m_pillar_reg set img_status='0' where uid='" + userid + "' and d_id='" + divid + "'", null);
        Cursor c = db.rawQuery("select * from m_pillar_reg where d_id='" + divid + "' and uid='" + userid + "' and img_status='0' ", null);
        int count = c.getCount();
        while (c.moveToNext()) {
            try {
                uploadImage(Utility.getByeArr(Utility.setPic(c.getString(c.getColumnIndex("p_pic")))), c.getString(c.getColumnIndex("p_pic")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        c.close();
        co.close();
        db.close();
    }*/

    private class DownloadFile extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String fileName;
        private String folder;
        private boolean isDownloaded;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(UserProfileActivity.this);
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();


                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                //String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

                //Extract file name from URL
                fileName = f_url[0].substring(f_url[0].lastIndexOf('/') + 1);
                File directory = getExternalFilesDir(null);
                String folder = directory.getAbsolutePath();

                if (!directory.exists()) {
                    directory.mkdirs();
                }
                OutputStream output = new FileOutputStream(folder + "/" + fileName);

                byte[] data = new byte[16384];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    Log.d(TAG, "Progress: " + (int) ((total * 100) / lengthOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
                return "Downloaded at: " + folder + fileName;

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return "Something went wrong";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }


        @Override
        protected void onPostExecute(String message) {
            // dismiss the dialog after the file was downloaded
            this.progressDialog.dismiss();

            // Display File path after downloading
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();
        }
    }

    private class imgaeSync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //inside image sync
            try {
                Images();

            }catch (Exception e){
                e.printStackTrace();
            }
            return "Executed";
        }
    }

    public void Images() {
        try {
            SharedPreferences shared = getSharedPreferences(userloginn, MODE_PRIVATE);
            div_id = shared.getString("udivid", "0");
            user_id = shared.getString("uemail", "0");
            db1 = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            db2 = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            db3 = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            //Cursor c1 = db.rawQuery("update m_pillar_reg set img_status='0' where uid='" + userid + "' and d_id='" + divid + "'", null);

            Cursor c = db1.rawQuery("select * from m_pillar_reg where d_id='" + div_id + "' and uid='" + user_id + "' and img_status='0' and p_pic is not null", null);
            int count = c.getCount();
            if (count >= 1) {
                if (c.moveToFirst()) {
                    try {
                        do {
                            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo nInfo = cm.getActiveNetworkInfo();
                            if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
                                uploadImage(Utility.getByeArr(Utility.setPic(c.getString(c.getColumnIndex("p_pic")))), c.getString(c.getColumnIndex("p_pic")), "1");
                            }else {
                                Toast.makeText(UserProfileActivity.this, "No internet connection...please connect !", Toast.LENGTH_LONG).show();
                            }
                        }while (c.moveToNext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                c.close();
                db1.close();
            }

            Cursor c1 = db2.rawQuery("select * from m_shifting_pillar_reg where uid='" + user_id + "' and simg_status='0' and s_pic is not null", null);
            int count1 = c1.getCount();
            if (count1 >= 1) {
                if (c1.moveToFirst()) {
                    try {
                        do {
                            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo nInfo = cm.getActiveNetworkInfo();
                            if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
                                uploadImage(Utility.getByeArr(Utility.setPic(c1.getString(c1.getColumnIndex("s_pic")))), c1.getString(c1.getColumnIndex("s_pic")), "2");
                            }else {
                                Toast.makeText(UserProfileActivity.this, "No internet connection...please connect !", Toast.LENGTH_LONG).show();
                            }

                        }while (c1.moveToNext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                c1.close();
                db2.close();
            }

            Cursor c2 = db3.rawQuery("select * from m_fb_dgps_survey_pill_pic where u_id='" + user_id + "' and pic_status='0' and pic_name is not null", null);
            int count2 = c2.getCount();
            if (count2 >= 1) {
                if (c2.moveToFirst()) {
                    try {
                        do {
                            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo nInfo = cm.getActiveNetworkInfo();
                            if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
                                uploadDGPSImage(Utility.getByeArr(Utility.setPic(c2.getString(c2.getColumnIndex("pic_name")))), c2.getString(c2.getColumnIndex("pic_name")), "1", c2.getString(c2.getColumnIndex("pic_view")));
                            }else {
                                Toast.makeText(UserProfileActivity.this, "No internet connection...please connect !", Toast.LENGTH_LONG).show();
                            }

                        }while (c2.moveToNext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                c2.close();
                db3.close();
            }
        }
        catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    private void uploadImage(byte[] imageBytes, final String imgname, String value) {
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BuildConfig.F_PILL_PIC_NODE_SERVICE)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
                MultipartBody.Part body = null;
                body = MultipartBody.Part.createFormData("image", imgname, requestFile);
                Call<Response1> call = retrofitInterface.uploadImage(body);
                call.enqueue(new Callback<Response1>() {
                    @Override
                    public void onResponse(Call<Response1> call, retrofit2.Response<Response1> response) {
                        if (response.isSuccessful()) {
                            try {
                                if (value.equals("1")) {
                                    db1 = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                                    Cursor c = db1.rawQuery("update m_pillar_reg set img_status='1' where uid='" + userid + "' and d_id='" + divid + "' and p_pic='" + imgname + "'", null);
                                    if (c.getCount() >= 0) {
                                        Response1 responseBody = response.body();
                                        Toast.makeText(UserProfileActivity.this, responseBody.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    c.close();
                                    db1.close();
                                } else {
                                    db1 = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                                    Cursor c = db1.rawQuery("update m_shifting_pillar_reg set simg_status='1' where uid='" + user_id + "' and s_pic='" + imgname + "'", null);
                                    if (c.getCount() >= 0) {
                                        Response1 responseBody = response.body();
                                        Toast.makeText(UserProfileActivity.this, responseBody.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    c.close();
                                    db1.close();
                                }
                            } catch (Exception ee) {
                                ee.printStackTrace();
                            }
                        } else {
                            ResponseBody errorBody = response.errorBody();
                            Gson gson = new Gson();
                            try {
                                Response errorResponse = gson.fromJson(errorBody.string(), Response.class);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Response1> call, Throwable t) {
                    }

                });
            } else {
                Toast.makeText(UserProfileActivity.this, "Internet Connection is Not Available", Toast.LENGTH_LONG).show();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void uploadDGPSImage(byte[] imageBytes, final String imgname, String value, String pic_view) {
       try {
           ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
           NetworkInfo nInfo = cm.getActiveNetworkInfo();
           if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
               Retrofit retrofit = new Retrofit.Builder()
                       .baseUrl(BuildConfig.DGPS_PILL_PIC_NODE_SERVICE)
                       .addConverterFactory(GsonConverterFactory.create())
                       .build();
               RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
               RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
               MultipartBody.Part body = null;
               body = MultipartBody.Part.createFormData("image", imgname, requestFile);
               Call<Response1> call = retrofitInterface.uploadDGPSImage(body);
               // mProgressBar.setVisibility(View.VISIBLE);
               call.enqueue(new Callback<Response1>() {
                   @Override
                   public void onResponse(Call<Response1> call, retrofit2.Response<Response1> response) {
                       if (response.isSuccessful()) {
                           if (value.equals("1")) {
                               try {
                                   db4 = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                                   Cursor c4 = db4.rawQuery("update m_fb_dgps_survey_pill_pic set pic_status='1' where u_id='" + user_id + "' and pic_name='" + imgname + "'", null);
                                   if (c4.getCount() >= 0) {
                                       SQLiteDatabase dbtemp = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                                       Cursor ctemp = null;
                                       try {
                                           if (pic_view.equals("F")) {
                                               ctemp = dbtemp.rawQuery("update m_fb_dgps_survey_pill_data set f_pic_status='1' where u_id='" + user_id + "' and f_pic_name='" + imgname + "'", null);
                                           } else if (pic_view.equals("B")) {
                                               ctemp = dbtemp.rawQuery("update m_fb_dgps_survey_pill_data set b_pic_status='1' where u_id='" + userid + "' and b_pic_name='" + imgname + "'", null);
                                           } else if (pic_view.equals("I")) {
                                               ctemp = dbtemp.rawQuery("update m_fb_dgps_survey_pill_data set i_pic_status='1' where u_id='" + userid + "' and i_pic_name='" + imgname + "'", null);
                                           } else if (pic_view.equals("O")) {
                                               ctemp = dbtemp.rawQuery("update m_fb_dgps_survey_pill_data set o_pic_status='1' where u_id='" + userid + "' and o_pic_name='" + imgname + "'", null);
                                           } else if (pic_view.equals("T")) {
                                               ctemp = dbtemp.rawQuery("update m_fb_dgps_survey_pill_data set div_pic_status='1' where u_id='" + userid + "' and div_pic_name='" + imgname + "'", null);
                                           } else {

                                           }
                                           if (ctemp.getCount() >= 0) {
//                                            Toast.makeText(UserProfileActivity.this, "Data Updated", Toast.LENGTH_SHORT).show();
                                           }
                                           ctemp.close();
                                           dbtemp.close();
                                       } catch (Exception ee) {
                                           ee.printStackTrace();
                                       } finally {
                                           Response1 responseBody = response.body();
                                           Toast.makeText(UserProfileActivity.this, responseBody.getPath(), Toast.LENGTH_SHORT).show();
                                       }
                                   }
                                   c4.close();
                                   db4.close();
                               } catch (Exception ee) {
                                   ee.printStackTrace();
                               }
                           } else {
                           }
                       } else {
                           ResponseBody errorBody = response.errorBody();
                           Gson gson = new Gson();
                           try {
                               Response errorResponse = gson.fromJson(errorBody.string(), Response.class);
                           } catch (IOException e) {
                               e.printStackTrace();
                           }
                       }
                   }
                   @Override
                   public void onFailure(Call<Response1> call, Throwable t) {
                   }
               });
           } else {
               Toast.makeText(UserProfileActivity.this, "Internet Connection is Not Available", Toast.LENGTH_LONG).show();
           }

       }catch (Exception e){
           e.printStackTrace();
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
                    if (closeTimeFlag.equalsIgnoreCase("close")){
                        dialog.dismiss();
                    }else {
                        Toast.makeText(UserProfileActivity.this, "Wait for image sync !", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    closeTimeFlag="close";
                    dialog.dismiss();
                }
            },6000);

            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void checkForDialog(){
        try {
            SharedPreferences shared = getSharedPreferences(userloginn, MODE_PRIVATE);
            div_id = shared.getString("udivid", "0");
            user_id = shared.getString("uemail", "0");
            db1 = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            db2 = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            db3 = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            //Cursor c1 = db.rawQuery("update m_pillar_reg set img_status='0' where uid='" + userid + "' and d_id='" + divid + "'", null);
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();

            pillar_reg_count = getType_dataSize("pillar_reg");
            shifting_pillar_count = getType_dataSize("shifting_pillar_reg");
            if (pillar_reg_count==0 && shifting_pillar_count==0){
            }else {
                loading_ll.setVisibility(View.VISIBLE);
            }

            handler_new.postDelayed(runnable=new Runnable() {
                @Override
                public void run() {
                    handler_new.postDelayed(runnable,delayTime);

                    pillar_reg_count = getType_dataSize("pillar_reg");
                    shifting_pillar_count = getType_dataSize("shifting_pillar_reg");

                    imageCount=pillar_reg_count+shifting_pillar_count;


                    Animation image_count_animation= AnimationUtils.loadAnimation(UserProfileActivity.this,R.anim.zoom_in_anim);
                    path_txt.startAnimation(image_count_animation);

                    if (pillar_reg_count==0 && shifting_pillar_count==0){
                        path_txt.clearAnimation();
                        handler_new.removeCallbacks(runnable);
                        loading_ll.setVisibility(View.GONE);
                    }else {
                        loading_text.setText("Please wait for image to be sync...!");
                        path_ll.setVisibility(View.VISIBLE);
                        path_txt.setText("Remaining image to be sync : " + imageCount);

                        if (imageCount==0){
                            loading_ll.setVisibility(View.GONE);

                            loading_text.setVisibility(View.VISIBLE);
                            progressB.setVisibility(View.VISIBLE);
                            path_ll.setVisibility(View.GONE);
                            path_txt.clearAnimation();
                            handler_new.removeCallbacks(runnable);
                        }
                    }

                }
            },0);

//            Cursor c = db1.rawQuery("select * from m_pillar_reg where d_id='" + div_id + "' and uid='" + user_id + "' and img_status='0' and p_pic is not null", null);
//            int count = c.getCount();
//            if (count >= 1) {
//                if (c.moveToFirst()) {
//                    try {
//                        do {
////                new imgaeSync().execute("");//for image sync JV
//                            if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
//                                imageCount = count;
////                    callSyncDialog(UserProfileActivity.this);//call dialog for image sync
////                                loading_ll.setVisibility(View.VISIBLE);
////                                loading_text.setText("Please wait for image to be sync...!");
////                                path_ll.setVisibility(View.VISIBLE);
////                                path_txt.setText("Remaining image to be sync : " + imageCount);
//                            }
//                            else {
//                                Toast.makeText(UserProfileActivity.this, "No internet connection...please connect !", Toast.LENGTH_LONG).show();
//                            }
//
//                        } while (c.moveToNext());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                c.close();
//                db1.close();
//            }
//
//            Cursor c1 = db2.rawQuery("select * from m_shifting_pillar_reg where uid='" + user_id + "' and simg_status='0' and s_pic is not null", null);
//            int count1 = c1.getCount();
//            if (count1 >= 1) {
//                if (c1.moveToFirst()) {
//                    try {
//                        do {
//                            if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
//                                if (count > 0) {
//                                    imageCount = count + count1;
//                                } else {
//                                    imageCount = count1;
//                                }
////                                loading_ll.setVisibility(View.VISIBLE);
////                                loading_text.setText("Please wait for image to be sync...!");
////                                path_ll.setVisibility(View.VISIBLE);
////                                path_txt.setText("Remaining image to be sync : " + imageCount);
////                    callSyncDialog(UserProfileActivity.this);//call dialog for image sync
//
//                            } else {
////                    Toast.makeText(UserProfileActivity.this, "No internet connection...please connect !", Toast.LENGTH_LONG).show();
//                            }
//                        } while (c1.moveToNext());
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                c1.close();
//                db2.close();
//            }

            Cursor c2 = db3.rawQuery("select * from m_fb_dgps_survey_pill_pic where u_id='" + user_id + "' and pic_status='0' and pic_name is not null", null);
            int count2 = c2.getCount();
            if (count2 >= 1) {
                if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
//                    callSyncDialog(UserProfileActivity.this);//call dialog for image sync

                }else {
//                    Toast.makeText(UserProfileActivity.this, "No internet connection...please connect !", Toast.LENGTH_LONG).show();
                }
                c2.close();
                db3.close();
            }
        }
        catch (Exception ee) {
            ee.printStackTrace();
        }
    }
    public void backUpProcess(){
        try {
            File src=null;
            File storageDir=null,backupDir=null;
            String sfinalpath ="";
            String dfinalpath="";
            String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
            if(Build.VERSION.SDK_INT >= 29) {
                //only api 29 above
                // Create an image file name
                storageDir = getExternalFilesDir(null); // /storage/emulated/0/Android/data/com.sparc.frjvcapp/files/CameraSample/
//                backupDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString()); // /storage/emulated/0/Android/data/com.sparc.frjvcapp/files/Backup/
                backup_path = storageDir+"/Backup"; // /storage/emulated/0/Android/data/com.sparc.frjvcapp/files/Documents/
                File cameraSampleFolder = new File(storageDir + "/CameraSample");
                if (!cameraSampleFolder.exists()) {
                    cameraSampleFolder.mkdirs();
                }


                path_ll.setVisibility(View.VISIBLE);
                path_txt.setText("BackUp Store path : "+ backup_path);

                String currentDateTime;
                Random r = new Random();
                int randomNumber = r.nextInt(10000);
                String uniqueNo=""+randomNumber;
                currentDateTime=Util.getCurrentDate("dd_MM_yyyy");

                tempBackupDir = new File(backup_path + "/JVBackup_"+currentDateTime+"_"+user_id+"_"+uniqueNo);
                if (!tempBackupDir.exists()) {
                    tempBackupDir.mkdirs();
                }
                String spath=cameraSampleFolder.getAbsolutePath();
                String zipPath =tempBackupDir.getAbsolutePath();
//                String zipPath =tempBackupDir.getAbsolutePath()+"/"+ userid + "_" + timeStamp;

                sfinalpath = spath;
                dfinalpath = zipPath;
                src = new File(sfinalpath);
                File[] Files = src.listFiles();
                if (Files.length > 0) {
                    backupFileStatus = 1;
                    String[] sfile=new String[1];
                    sfile[0]=sfinalpath;

//                    zip(sfile, dfinalpath);//for zip
                    backUpFile(sfinalpath, dfinalpath);


                } else {
                    Toast.makeText(getApplicationContext(), "You don't have data for backup", Toast.LENGTH_LONG).show();
                }
            }
            else {
                //below api level 29
                String sfile = Environment.getExternalStorageDirectory().toString();
                String spath = "/CameraSample";
                String zipPath = "/JVBackup_/" + userid + "_" + timeStamp;
                sfinalpath = sfile + spath;
                dfinalpath = sfile + spath + zipPath;
                backup_path= sfile;
                src = new File(sfinalpath);
                File[] Files = src.listFiles();
                if (Files.length > 0) {
                    backupFileStatus = 1;
                    backUpFile(sfinalpath, dfinalpath);
//                    if (zipFileAtPath(sfinalpath, dfinalpath)) {
////                        new DGPSSyncMenuActivity.SyncFiles().execute(dfinalpath + "," + "S");
//                    } else {
//                        Toast.makeText(this, "Could not zip the file...", Toast.LENGTH_SHORT).show();
//                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You don't have data for backup", Toast.LENGTH_LONG).show();
                }

            }
            Toast.makeText(UserProfileActivity.this, "Backup image successfull ." +
                    "please check zip file in '"+backup_path+"' ", Toast.LENGTH_SHORT).show();
            Handler handler =new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (loading_ll.getVisibility()==View.VISIBLE) {
//                        loading_ll.setVisibility(View.GONE);
                        loading_text.setVisibility(View.GONE);
                        progressB.setVisibility(View.GONE);
                    }
                }
            },5000);
//            openDirectoryAndroid11(Uri.fromFile(storageDir));
//            openFolder(storageDir.getAbsolutePath(),UserProfileActivity.this);

        }catch (Exception e){
            e.printStackTrace();
        }
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
                zipFileAtPath(sourcePath, toLocation+".zip");
//            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

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

    private void deleteAllZipFile(String userid) {
        try {
            if(Build.VERSION.SDK_INT >= 29) {
                //only api 29 above
                // Create an image file name
                File storageDir = getExternalFilesDir(null); // /storage/emulated/0/Android/data/com.sparc.frjvcapp/files/CameraSample/
                File cameraSampleFolder = new File(storageDir + "/CameraSample");
                if (!cameraSampleFolder.exists()) {
                    cameraSampleFolder.mkdirs();
                }

                String sfile = cameraSampleFolder.getAbsolutePath();
                File f = new File(sfile);
                File[] files = f.listFiles();
                if (files != null)
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        if (getFileExtension(file).equals(".zip")) {
                            file.delete();
                        }
                    }

            }else {
                //below api level 29
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
        }catch (Exception e){
            e.printStackTrace();
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
            deleteFiles(tempBackupDir,"tempFolderJV");
//            Toast.makeText(UserProfileActivity.this, tempBackupDir.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String getLastPathComponent(String filePath) {
        String[] segments ;
        String lastPathComponent="";
        try {
            segments= filePath.split("/");
            if (segments.length == 0)
                return "";
            lastPathComponent = segments[segments.length - 1];
        }catch (Exception e){
            e.printStackTrace();
        }

        return lastPathComponent;
    }

    private void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {
        try {

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
            File folderNm=getExternalFilesDir(null);
            String fileNm=folderNm.getAbsolutePath()+"/CameraSample";

            deleteFiles(new File(fileNm),"cameraSample");

//            deleteFiles(tempBackupDir,"tempFolderJV");


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void zip(String[] files, String zipFile) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            byte data[] = new byte[BUFFER_SIZE];

            for (int i = 0; i < files.length; i++) {
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    origin.close();
                }
            }
        } finally {
            out.close();
        }
    }

    private void getDataforExcel(String userid) {
        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("select * from m_pillar_reg where uid='" + userid + "' order by id", null);
//        File directoryy = getExternalFilesDir(null);
//        openDirectoryAndroid11(Uri.fromFile(directoryy));
//        openFolder(directoryy.getAbsolutePath(),UserProfileActivity.this);

        if (cursor.getCount() > 0) {
            String currentDateTime;
            Random r = new Random();
            int randomNumber = r.nextInt(1000);
            String uniqueNo=""+randomNumber;
            currentDateTime=Util.getCurrentDate("dd_MM_yyyy");
            String fileName = currentDateTime +"_"+ userid +"_" + uniqueNo + ".xls";
            File directory = getExternalFilesDir(null);
//            File directory = getExternalFilesDir(null);
            String folder = directory.getAbsolutePath()+"/CameraSample";
            if (!directory.exists()) {
                directory.mkdirs();
            }

            try {
                File file = new File(folder, fileName);
                WorkbookSettings wbSettings = new WorkbookSettings();
                wbSettings.setLocale(new Locale("en", "EN"));
                WritableWorkbook workbook;
                workbook = Workbook.createWorkbook(file, wbSettings);
                //Excel sheet name. 0 represents first sheet
                WritableSheet sheet = workbook.createSheet("PillarList", 0);
                sheet.addCell(new Label(0, 0, "ID"));
                sheet.addCell(new Label(1, 0, "Division ID"));
                sheet.addCell(new Label(2, 0, "Range ID"));
                sheet.addCell(new Label(3, 0, "ForestBlock ID"));
                sheet.addCell(new Label(4, 0, "Insc. Pillar No"));
                sheet.addCell(new Label(5, 0, "Lat"));
                sheet.addCell(new Label(6, 0, "Long"));
                sheet.addCell(new Label(7, 0, "Pillar Type"));
                sheet.addCell(new Label(8, 0, "Pillar Condition"));
                sheet.addCell(new Label(9, 0, "Remark"));
                sheet.addCell(new Label(10, 0, "Photo"));
                sheet.addCell(new Label(11, 0, "Patch No"));
                sheet.addCell(new Label(12, 0, "Ring No"));
                sheet.addCell(new Label(13, 0, "Location Type"));
                sheet.addCell(new Label(14, 0, "Pillar No"));
                sheet.addCell(new Label(15, 0, "Paint Status"));
                sheet.addCell(new Label(16, 0, "FB Name"));
                sheet.addCell(new Label(17, 0, "User ID"));
                sheet.addCell(new Label(18, 0, "Point No"));
                sheet.addCell(new Label(19, 0, "Image Status"));
                sheet.addCell(new Label(20, 0, "Delete Status"));
                sheet.addCell(new Label(21, 0, "Shifting Status"));
                sheet.addCell(new Label(22, 0, "Survey Duration"));
                sheet.addCell(new Label(23, 0, "Accuracy"));
                sheet.addCell(new Label(24, 0, "Date"));
                //cursor.moveToFirst();
                if (cursor.moveToFirst()) {
                    do {
                        int i = cursor.getPosition() + 1;
                        sheet.addCell(new Label(0, i, cursor.getString(cursor.getColumnIndex("point_no"))));
                        sheet.addCell(new Label(1, i, cursor.getString(cursor.getColumnIndex("d_id"))));
                        sheet.addCell(new Label(2, i, cursor.getString(cursor.getColumnIndex("r_id"))));
                        sheet.addCell(new Label(3, i, cursor.getString(cursor.getColumnIndex("fb_id"))));
                        sheet.addCell(new Label(4, i, cursor.getString(cursor.getColumnIndex("p_sl_no"))));
                        sheet.addCell(new Label(5, i, cursor.getString(cursor.getColumnIndex("p_lat"))));
                        sheet.addCell(new Label(6, i, cursor.getString(cursor.getColumnIndex("p_long"))));
                        sheet.addCell(new Label(7, i, cursor.getString(cursor.getColumnIndex("p_type"))));
                        sheet.addCell(new Label(8, i, cursor.getString(cursor.getColumnIndex("p_cond"))));
                        sheet.addCell(new Label(9, i, cursor.getString(cursor.getColumnIndex("p_rmk"))));
                        sheet.addCell(new Label(10, i, cursor.getString(cursor.getColumnIndex("p_pic"))));
                        sheet.addCell(new Label(11, i, cursor.getString(cursor.getColumnIndex("patch_no"))));
                        sheet.addCell(new Label(12, i, cursor.getString(cursor.getColumnIndex("ring_no"))));
                        sheet.addCell(new Label(13, i, cursor.getString(cursor.getColumnIndex("p_loc_type"))));
                        sheet.addCell(new Label(14, i, cursor.getString(cursor.getColumnIndex("p_no"))));
                        sheet.addCell(new Label(15, i, cursor.getString(cursor.getColumnIndex("p_paint_status"))));
                        sheet.addCell(new Label(16, i, cursor.getString(cursor.getColumnIndex("fb_name"))));
                        sheet.addCell(new Label(17, i, cursor.getString(cursor.getColumnIndex("uid"))));
                        sheet.addCell(new Label(18, i, cursor.getString(cursor.getColumnIndex("point_no"))));
                        sheet.addCell(new Label(19, i, cursor.getString(cursor.getColumnIndex("img_status"))));
                        sheet.addCell(new Label(20, i, cursor.getString(cursor.getColumnIndex("delete_status"))));
                        sheet.addCell(new Label(21, i, cursor.getString(cursor.getColumnIndex("shifting_status"))));
                        sheet.addCell(new Label(22, i, cursor.getString(cursor.getColumnIndex("surv_direction"))));
                        sheet.addCell(new Label(23, i, cursor.getString(cursor.getColumnIndex("p_accuracy"))));
                        sheet.addCell(new Label(24, i, cursor.getString(cursor.getColumnIndex("survey_dt"))));
                    } while (cursor.moveToNext());
                    cursor.close();
                    workbook.write();
                    workbook.close();
                    Toast.makeText(this, "Data exported in excel sheet", Toast.LENGTH_SHORT).show();

//                    openFolder(directory.getAbsolutePath(),UserProfileActivity.this);
                    backUpProcess();//zip the /CameraSample folder

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

        } else {
            Toast.makeText(getApplicationContext(), "You dont have any data for Export !", Toast.LENGTH_LONG).show();
        }
        cursor.close();
        db.close();
    }
    public static void openFolder(String folderNm,Context context){
        try {
            File file = new File(folderNm);
            MimeTypeMap map = MimeTypeMap.getSingleton();
            FileNameMap fileNameMap = URLConnection.getFileNameMap();

            String ext = fileNameMap.getContentTypeFor("file://"+file.getAbsolutePath());
//            String ext = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
            String type = map.getMimeTypeFromExtension(ext);
            if (type == null)
                type = "*/*";

            Intent intent = new Intent(Intent.ACTION_VIEW);
//            Uri data = Uri.fromFile(file);
            Uri data = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//without permission we cannot read file
            intent.setDataAndType(data, type);
            context.startActivity(Intent.createChooser(intent, "Open with"));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void openDirectoryAndroid11(Uri uriToLoad) {
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uriToLoad);

        startActivityForResult(intent, 11);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        int takeFlags=0;
        if (requestCode == 11
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            if (intent != null) {
                uri = intent.getData();
                // Perform operations on the document using its URI.

                takeFlags = intent.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                getContentResolver().takePersistableUriPermission(uri, takeFlags);

            }
        }
    }
//    public void callForAllFilesPermissionAndroid11(){
//        try {
//            if (Build.VERSION.SDK_INT >= 29) {
//                if (Environment.isExternalStorageManager()) {
////                    startActivity(new Intent(this, DGPSDataTaggMenuActivity.class));
//                } else { //request for the permission
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
//                    Uri uri = Uri.fromParts("package", getPackageName(), null);
//                    intent.setData(uri);
//                    startActivity(intent);
//                }
//            }
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }


    public int getType_dataSize(String type) {
        int count=0;
        Cursor c=null;
        try {
            db = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
            if (type.equalsIgnoreCase("pillar_reg")) {
//                db1 = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
                c = db.rawQuery("select * from m_pillar_reg where d_id='" + div_id + "' and uid='" + user_id + "' and img_status='0' and p_pic is not null", null);
                count = c.getCount();
            }
            else if (type.equalsIgnoreCase("shifting_pillar_reg")) {
//                db2 = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
                c = db.rawQuery("select * from m_shifting_pillar_reg where uid='" + user_id + "' and simg_status='0' and s_pic is not null", null);
                count = c.getCount();
            }
            else if (type.equalsIgnoreCase("excel_all_data")){
//                db = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
                c = db.rawQuery("select * from m_pillar_reg where uid='" + userid + "' order by id", null);
                count = c.getCount();
            }
            db.close();
//            db1.close();
//            db2.close();
            c.close();

        }catch (Exception e){
            Log.d("exception_onSize",e.getMessage());
        }
        return count;
    }
    public void deleteFiles(File fileOrDirectory,String type) {
        switch (type){
            case "cameraSample": {
                if (fileOrDirectory.isDirectory())
                    for (File child : fileOrDirectory.listFiles()) {
                        deleteFiles(child, "cameraSample");
                    }
                fileOrDirectory.delete();//delete the empty folder

                Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        callToCreateFolder();//create folder of /CameraSample after copy
                    }
                },4000);
                break;
            }
            case "tempFolderJV" :
            {
                if (fileOrDirectory.isDirectory())
//                    for (File child : fileOrDirectory.listFiles())
//                        deleteFiles(child,"tempFolderJV");

                fileOrDirectory.delete();//delete the empty folder
                break;
            }
        }
    }

    public void callToCreateFolder(){
        try {
            //create file
            File defaultFolder=getExternalFilesDir(null);
            String fileNm=defaultFolder.getAbsolutePath();
            File camerasampleFolder=new File(defaultFolder+"/CameraSample");
            if (!camerasampleFolder.exists()){
                camerasampleFolder.mkdirs();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}

