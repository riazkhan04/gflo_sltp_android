package com.sparc.frjvcapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sparc.frjvcapp.pojo.M_dgpssurvey_pillar_data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChooseSurvetTypeActivity extends AppCompatActivity {
    SharedPreferences shared, _shareToken;
    String sharediv, sharerange, sharefb, sharefbtype, sharefbname, userid, jobid, div_name, range_name, fb_name, _token, utmZone;
    public static final String data = "data";
    public static final String userlogin = "userlogin";
    ImageView data_collect, data_view, data_export, data_sync, data_point_dwld, mapview, revisit, help, resurvey;
    TextView dgpsfbName;
    SQLiteDatabase db;
    DbHelper dbHelper;
    private ProgressDialog progressDialog;
    JSONArray jsonArray;
    JSONObject fp_data;
    ProgressDialog progressDialog1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_survet_type);


        /*data_collect=findViewById(R.id.datacollect);*/
        data_view = findViewById(R.id.dataview);
        data_export = findViewById(R.id.exporttofolder);
        data_sync = findViewById(R.id.synchronize);
        data_point_dwld = findViewById(R.id.data_point_dwld);
        mapview = findViewById(R.id.mapview);
        dgpsfbName = findViewById(R.id.dgpsfbName);
        revisit = findViewById(R.id.exporttoExcel);
        resurvey = findViewById(R.id.resurvey);
        help = findViewById(R.id.help);

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
        dbHelper = new DbHelper(this);

        dgpsfbName.setText(fb_name);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Help.class);
                startActivity(i);
            }
        });
        resurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), Resurvey.class);
                startActivity(i);
            }
        });

        data_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DGPSDataViewActivity.class);
                SharedPreferences sharedPreferences = getSharedPreferences(data, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putString("fbrangecode", sharerange);
                editor.putString("fbdivcode", sharediv);
                editor.putString("fbcode", sharefb);
                editor.putString("fbtype", sharefbtype);
                editor.putString("fbname", sharefbname);
                editor.putString("userid", userid);
                editor.putString("jobid", jobid);
                editor.putString("div_name", div_name);
                editor.putString("range_name", range_name);
                editor.putString("fb_name", fb_name);
                editor.apply();
                startActivity(i);
            }
        });
        data_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DGPSDataTaggMenuActivity.class);
                SharedPreferences sharedPreferences = getSharedPreferences(data, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putString("fbrangecode", sharerange);
                editor.putString("fbdivcode", sharediv);
                editor.putString("fbcode", sharefb);
                editor.putString("fbtype", sharefbtype);
                editor.putString("fbname", sharefbname);
                editor.putString("userid", userid);
                editor.putString("jobid", jobid);
                editor.putString("div_name", div_name);
                editor.putString("range_name", range_name);
                editor.putString("fb_name", fb_name);
                editor.apply();
                startActivity(i);
            }
        });
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        data_point_dwld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo nInfo = cm.getActiveNetworkInfo();
                final View customLayout = getLayoutInflater().inflate(R.layout.pupup_check_point_download, null);
                alertDialogBuilder.setView(customLayout);
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (nInfo != null && nInfo.isAvailable() && nInfo.isConnected()) {
                                    if (CheckDataAvalability(sharefb)) {
                                        getDataForSurveyPoints(sharefb);
                                    } else {

                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Check your internet connection.", Toast.LENGTH_SHORT).show();
                                }
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


            }
        });
        mapview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DGPSMapViewActivity.class);
                SharedPreferences sharedPreferences = getSharedPreferences(data, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putString("fbrangecode", sharerange);
                editor.putString("fbdivcode", sharediv);
                editor.putString("fbcode", sharefb);
                editor.putString("fbtype", sharefbtype);
                editor.putString("fbname", sharefbname);
                editor.putString("userid", userid);
                editor.putString("jobid", jobid);
                editor.putString("div_name", div_name);
                editor.putString("range_name", range_name);
                editor.putString("fb_name", fb_name);
                editor.apply();
                startActivity(i);
            }
        });
        data_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), DGPSSyncMenuActivity.class);
                SharedPreferences sharedPreferences = getSharedPreferences(data, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putString("fbrangecode", sharerange);
                editor.putString("fbdivcode", sharediv);
                editor.putString("fbcode", sharefb);
                editor.putString("fbtype", sharefbtype);
                editor.putString("fbname", sharefbname);
                editor.putString("userid", userid);
                editor.putString("jobid", jobid);
                editor.putString("div_name", div_name);
                editor.putString("range_name", range_name);
                editor.putString("fb_name", fb_name);
                editor.apply();
                startActivity(i);
               /*if(checkDataForSynchronization())
               {
                   BindDGPSData(sharediv,userid);

               }else{
                   Toast.makeText(getApplicationContext(), "You dont have any data for Synchronization", Toast.LENGTH_LONG).show();
               }*/
            }
        });
        revisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RevisitDGPSSurveyMenuActivity.class);
                SharedPreferences sharedPreferences = getSharedPreferences(data, 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putString("fbrangecode", sharerange);
                editor.putString("fbdivcode", sharediv);
                editor.putString("fbcode", sharefb);
                editor.putString("fbtype", sharefbtype);
                editor.putString("fbname", sharefbname);
                editor.putString("userid", userid);
                editor.putString("jobid", jobid);
                editor.putString("div_name", div_name);
                editor.putString("range_name", range_name);
                editor.putString("fb_name", fb_name);
                editor.apply();
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), SelectFBForDGPSActivity.class);
        i.setFlags(i.FLAG_ACTIVITY_NEW_TASK | i.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);

    }

    private boolean CheckDataAvalability(String sharefb) {
        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
        boolean b = false;
        Cursor cursor = db.rawQuery("select * from m_dgps_Survey_pill_data where m_fb_id='" + sharefb + "'", null);
        try {
            if (cursor.getCount() > 0) {
                db.execSQL("delete from m_dgps_Survey_pill_data where m_fb_id='" + sharefb + "'");
                b = true;
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

    private void getDataForSurveyPoints(String fbid) {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            String URL = BuildConfig.F_D_FB_PILL_DATA_API + fbid;
            progressDialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage("Please wait...Your Point data is downloading");
            progressDialog.show();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            M_dgpssurvey_pillar_data m_fb = new M_dgpssurvey_pillar_data(object.getString("latitude"), object.getString("longitude"), object.getString("pillar_no"), "", fbid, object.getString("status"), "0", "0", object.getString("id"), "0", "0", object.getString("commonPlrSts"));//object.getString("point_path")
                            dbHelper.open();
                            dbHelper.insertdgpsSurveyedPointDataData(m_fb);
                            dbHelper.close();

                        }
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "The Survey Point Data is downloaded.", Toast.LENGTH_SHORT).show();

                        try {
                            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                            Cursor cursor = db.rawQuery("select substr(((min(m_p_long)+max(m_p_long))/2),0,3) as result from m_dgps_Survey_pill_data where m_fb_id='" + sharefb + "'", null);
                            cursor.moveToFirst();
                            if (cursor.getCount() > 0) {

                                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex("result"))) >= 84) {
                                    utmZone = "45 North";
                                } else {
                                    utmZone = "44 North";
                                }
                            }
                            cursor.close();
                            db.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        AlertDialog.Builder connectionerrorBuilder = new AlertDialog.Builder(ChooseSurvetTypeActivity.this);
                        connectionerrorBuilder.setTitle("UTM/Zone : " + utmZone + "");
                        connectionerrorBuilder.setMessage("Set this UTM zone in survey mobile application.");
                        connectionerrorBuilder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        arg0.cancel();
                                    }
                                });
                        AlertDialog alertDialog1 = connectionerrorBuilder.create();
                        alertDialog1.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "You don't have any point.", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Data is not available in Server", Toast.LENGTH_SHORT).show();
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
            };

            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
