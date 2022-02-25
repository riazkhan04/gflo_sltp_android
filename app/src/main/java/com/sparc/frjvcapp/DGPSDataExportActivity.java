
package com.sparc.frjvcapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sparc.frjvcapp.Adapter.DGPSPillarViewAdapter;
import com.sparc.frjvcapp.pojo.DGPSPillarDataViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DGPSDataExportActivity extends AppCompatActivity {
    public static final String data = "data";
    ImageView img_download;
    TextView txtStsFileName;
    private SQLiteDatabase db, db1;
    private ArrayList<String> arrayList;
    private ArrayList<DGPSPillarDataViewModel> arrayList1;
    //    MaterialSpinner pill_no;
    private TextInputEditText pill_no;
    String txtPillNo;
    Cursor c;
    String sfinalpath, dfinalpath;
    static File d;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    public DGPSPillarViewAdapter adapter;
    String sharediv, sharerange, sharefb, sharefbtype, sharefbname, userid, jobid, div_name, range_name, fb_name, frjvc_lat, frjvc_long;
    SharedPreferences shared;
    public static final String PREVIOUS_TIME = "previousPref";
    long _min, _second;
    File s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dgpsdata_export);

        recyclerView = findViewById(R.id.dgpsrecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        img_download = findViewById(R.id.img_download);
        txtStsFileName = findViewById(R.id.txtStsFileName);
        arrayList = new ArrayList<String>();
        arrayList1 = new ArrayList<DGPSPillarDataViewModel>();

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

        try {
            //(not working for android 11 so we make the targetsdk to 29.(and not to publish in playstore
            // as it requrired sdk min 30))

            //for all files permission for android 11
//            if(Build.VERSION.SDK_INT >= 29) {
////                callForAllFilesPermissionAndroid11();
//                File main_file_path=getExternalFilesDir("");
//                String dpath = main_file_path+"/StaticData";
//                dfinalpath = dpath;
//            }

//            if(Build.VERSION.SDK_INT >= 29) {
//                File main_file_path=getExternalFilesDir("");
//                String sfile="";
//                String path = main_file_path+"/SurveyMobile.Droid/Download";
//                sfinalpath = sfile + path;
//
//                String dfile = "";
//                String dpath = main_file_path+"/StaticData";
//                dfinalpath = dfile + dpath;
//
//            }else {
                //less than 29 api level
            try {
                String sfile = Environment.getExternalStorageDirectory().toString();
//                String path = "/SurveyMobile.Droid/Download";
                String path = "/SurveyMobile.Droid";
                File downloadFile=new File(sfile+path+"/Download");
//                sfinalpath = sfile + path;
                sfinalpath = downloadFile.getPath();

                String dfile = Environment.getExternalStorageDirectory().toString();
                File staticFile=new File(dfile+"/StaticData");
//                String dpath = "/StaticData";
//                dfinalpath = dfile + dpath;
                dfinalpath = staticFile.getPath();

            }catch (Exception e){
                e.printStackTrace();
            }
//            }
            s = new File(sfinalpath);
            d = new File(dfinalpath);
            if (!d.exists()) {
                d.mkdirs();
            }
//            copyFileToInternalStorage(Uri.parse(s.getPath()),"StaticData");
//            openDirectoryAndroid11(Uri.parse(s.getPath()));
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);

            c = db.rawQuery("SELECT * from m_fb_dgps_survey_pill_data where pillar_sfile_status='0' and survey_status='1' and delete_status='0' and fb_id='" + sharefb + "' order by pill_no", null);
            int count = c.getCount();
            if (count > 0) {
                File[] listOfFiles = s.listFiles();
                if (listOfFiles!=null){
                    if (listOfFiles.length > 0) {
                        for (int i = 0; i < 1; i++) {
                            if (listOfFiles[i].isFile()) {
                                // Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
                                //int CurrentDayOfYear = localCalendar.get(Calendar.DAY_OF_YEAR);
                                String filename[] = listOfFiles[i].getName().toString().split("\\.");
                                if (filename[1].equals(dayoftheyear())) {
                                    txtStsFileName.setText(listOfFiles[i].getName());
                                } else {
                                    txtStsFileName.setText("No file available for download");
                                    img_download.setVisibility(View.GONE);
                                    String wrongFile = Environment.getExternalStorageDirectory().toString() + "/SurveyMobile.Droid/Download";
                                    File f = new File(wrongFile);
                                    File[] files = f.listFiles();
                                    if (files != null)
                                        for (int a = 0; a < files.length; a++) {
                                            File file = files[a];
                                            file.delete();
                                        }
                                }
                            } else {
                                txtStsFileName.setText("No file available for download");
                                img_download.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        txtStsFileName.setText("No file available for download");
                        txtStsFileName.setTextSize(15);
                        txtStsFileName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        txtStsFileName.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                        txtStsFileName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                        img_download.setVisibility(View.GONE);
                    }
                }
                else {
                    //array is null
                    txtStsFileName.setText("No file available for download !");
                    txtStsFileName.setTextSize(15);
                    txtStsFileName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    txtStsFileName.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
                    txtStsFileName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
                    img_download.setVisibility(View.GONE);
                }

            } else {
                txtStsFileName.setText("No file available for download");
                img_download.setVisibility(View.GONE);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        getDGPSDataFOrView();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        img_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDGPSPillData();
                //alertDialogBuilder.setMessage("Are you sure to save this pillar data?");
                final View customLayout = getLayoutInflater().inflate(R.layout.assign_pillar_to_static, null);
                alertDialogBuilder.setView(customLayout);
                pill_no = customLayout.findViewById(R.id.pillno);
                pill_no.setText(txtPillNo);
                alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                File fileTime = new File(Environment.getExternalStorageDirectory().toString() + "/SurveyMobile.Droid/Download");
                                Date lastModDate = new Date(fileTime.lastModified());
                                String aa = lastModDate.toString();
                                SharedPreferences shared1 = getApplicationContext().getSharedPreferences(PREVIOUS_TIME, MODE_PRIVATE);
                                if (shared1.getString("previousTime", "0").equals("1")) {
                                    try {
                                        String _startTime = shared1.getString("exactTime", "0");

                                        SimpleDateFormat df = new SimpleDateFormat("kk:mm:ss");
                                        Date date11 = df.parse(df.format(new Date(_startTime)));
                                        Date date2 = df.parse(df.format(new Date(aa)));
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
                                            Log.d("observ_time", "done");
                                           /* AlertDialog.Builder connectionerrorBuilder = new AlertDialog.Builder(MainContainerActivity.this);
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
                                            alertDialog1.show();*/
                                        }
                                        cursor.close();
                                        db.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    SharedPreferences.Editor editor = shared1.edit();
                                    editor.clear();
                                    editor.apply();
                                }
//observation time updated


                                String d_f_name[] = txtStsFileName.getText().toString().split("\\.");//.split(".")
                                String f_file_name = d_f_name[0] + "_" + "P" + txtPillNo + "." + d_f_name[1];

                                try {
                                   // Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
                                    //int CurrentDayOfYear = localCalendar.get(Calendar.DAY_OF_YEAR);

                                    if (d_f_name[1].equals(dayoftheyear())) {
                                        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);

                                        c = db.rawQuery("SELECT * from m_fb_dgps_survey_pill_data where pillar_sfile_status!='0' and delete_status='0' and fb_id='" + sharefb + "' order by pill_no", null);
                                        int count = c.getCount();
                                        int dExist = 0;
                                        if (count >= 1) {
                                            if (c.moveToFirst()) {
                                                do {
                                                    String staticFilePath = c.getString(c.getColumnIndex("pillar_sfile_path"));
                                                    String[] sfilename = staticFilePath.split("/");
                                                    String[] staticcheck = sfilename[sfilename.length - 1].split("_");
                                                    String finalvalue = staticcheck[0];
                                                    String[] databaseextension = sfilename[sfilename.length - 1].split("\\.");//.split(".")
                                                    String databaseextensionLast = databaseextension[1];
                                                    if (databaseextensionLast.equals(dayoftheyear())) {
                                                        dExist = dExist + 1;
                                                        if (d_f_name[0].equals(finalvalue) && d_f_name[1].equals(databaseextensionLast)) {
                                                            String sfile = Environment.getExternalStorageDirectory().toString() + "/SurveyMobile.Droid/Download";
                                                            File f = new File(sfile);
                                                            File[] files = f.listFiles();
                                                            if (files != null)
                                                                for (int i = 0; i < files.length; i++) {
                                                                    File file = files[i];
                                                                    file.delete();
                                                                }
                                                            //dialog message "File already tagged after delete"
                                                            AlertDialog.Builder connectionerrorBuilder = new AlertDialog.Builder(DGPSDataExportActivity.this);
                                                            connectionerrorBuilder.setTitle("Duplicate File Tagged !!!");
                                                            connectionerrorBuilder.setMessage("Please go to Survey Mobile App and download correct Static File.");
                                                            connectionerrorBuilder.setPositiveButton("OK",
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface arg0, int arg1) {
                                                                            recreate();
                                                                        }
                                                                    });
                                                            AlertDialog alertDialog1 = connectionerrorBuilder.create();
                                                            alertDialog1.show();
                                                            ///
                                                            break;
                                                        } else {
                                                            new DGPSDataExportActivity.ExportPointStaticData().execute(sfinalpath + "/" + txtStsFileName.getText().toString(), txtPillNo, dfinalpath + "/" + f_file_name);
                                                            //recreate();
                                                        }
                                                    }


                                                }
                                                while (c.moveToNext());
                                            }
                                            if (dExist == 0) {
                                                new DGPSDataExportActivity.ExportPointStaticData().execute(sfinalpath + "/" + txtStsFileName.getText().toString(), txtPillNo, dfinalpath + "/" + f_file_name);

                                            }
                                        } else {
                                            new DGPSDataExportActivity.ExportPointStaticData().execute(sfinalpath + "/" + txtStsFileName.getText().toString(), txtPillNo, dfinalpath + "/" + f_file_name);

                                        }
                                    } else {
                                        ///dialog message "File already tagged after delete"
                                        AlertDialog.Builder connectionerrorBuilder = new AlertDialog.Builder(DGPSDataExportActivity.this);
                                        connectionerrorBuilder.setTitle("Wrong File Tagged !!!");
                                        connectionerrorBuilder.setMessage("You are trying to tag previous day file.Please go to Survey Mobile App and download correct Static File");
                                        connectionerrorBuilder.setPositiveButton("OK",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface arg0, int arg1) {
                                                        recreate();
                                                    }
                                                });
                                        AlertDialog alertDialog1 = connectionerrorBuilder.create();
                                        alertDialog1.show();
                                        //delete static file from download
                                        String sfile = Environment.getExternalStorageDirectory().toString() + "/SurveyMobile.Droid/Download";
                                        File f = new File(sfile);
                                        File[] files = f.listFiles();
                                        if (files != null)
                                            for (int i = 0; i < files.length; i++) {
                                                File file = files[i];
                                                file.delete();
                                            }
                                        ///
                                    }


                                } catch (Exception e) {

                                }

                            }
                        });

                alertDialogBuilder.setNegativeButton("Cancel",
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
        adapter = new DGPSPillarViewAdapter(this, arrayList1);
    }

    public static String dayoftheyear(){
        Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
        int CurrentDayOfYear = localCalendar.get(Calendar.DAY_OF_YEAR);
        int tempdate = localCalendar.get(Calendar.DAY_OF_YEAR);
        String a ="";
        int cnt=0;
        while (tempdate != 0) {
            tempdate /= 10;
            ++cnt;
        }
        if (cnt==1){
            a="00"+CurrentDayOfYear;
        }else if(cnt==2){
            a="0"+CurrentDayOfYear;
        }else if(cnt==3){
            a=String.valueOf(CurrentDayOfYear);
        }
        return a;
    }


    private void getDGPSDataFOrView() {
        try {
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            arrayList1.clear();
            c = db.rawQuery("SELECT * from m_fb_dgps_survey_pill_data where survey_status='1'and pillar_sfile_status!='0' and delete_status='0' and fb_id='" + sharefb + "' order by pill_no", null);
            int count = c.getCount();
            if (count >= 1) {
                if (c.moveToFirst()) {
                    do {
                        DGPSPillarDataViewModel dataViewDetails = new DGPSPillarDataViewModel();
                        if (c.getString(c.getColumnIndex("pndjv_pill_no")).equals("0")) {
                            dataViewDetails.setPill_no(c.getString(c.getColumnIndex("pill_no")));
                        } else {
                            dataViewDetails.setPill_no(c.getString(c.getColumnIndex("pill_no")) + "_" + c.getString(c.getColumnIndex("pndjv_pill_no")));
                        }
                        dataViewDetails.setFilename(c.getString(c.getColumnIndex("pillar_sfile_path")));
                        dataViewDetails.setSync_status(c.getString(c.getColumnIndex("sync_status")));
                        arrayList1.add(dataViewDetails);
                    }
                    while (c.moveToNext());
                }
            }
            adapter = new DGPSPillarViewAdapter(this, arrayList1);
            recyclerView.setAdapter(adapter);
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            c.close();
            db.close();
        }
    }

    private void updatePillarData(String spill_no) {
        try {
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            arrayList.clear();
            c = db.rawQuery("update m_fb_dgps_survey_pill_data set pillar_sfile_status='1' and pillar_sfile_path='" + txtStsFileName.getText().toString() + "' where pill_no='" + spill_no + "'", null);
            if (c.getCount() >= 0) {
                Toast.makeText(this, "Your data tagged successfully", Toast.LENGTH_LONG);
            }

        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            c.close();
            db.close();
        }

    }

    private void getDGPSPillData() {
        try {
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            arrayList.clear();
            c = db.rawQuery("SELECT * from m_fb_dgps_survey_pill_data where pillar_sfile_status='0' and delete_status='0' and survey_status='1' and fb_id='" + sharefb + "' order by survey_time desc limit 1", null);
            int count = c.getCount();
            if (count >= 1) {
                if (c.moveToFirst()) {
                    //new
                    if (!c.getString(c.getColumnIndex("pndjv_pill_no")).equals("0")) {
                        txtPillNo = (c.getString(c.getColumnIndex("pill_no")) + "_" + c.getString(c.getColumnIndex("pndjv_pill_no")));
                    } else {
                        txtPillNo = (c.getString(c.getColumnIndex("pill_no")));
                    }
                    //new end
                    /*do {
                        String s = c.getString(c.getColumnIndex("pndjv_pill_no"));
                        if (!c.getString(c.getColumnIndex("pndjv_pill_no")).equals("0")) {
                            arrayList.add(c.getString(c.getColumnIndex("pill_no")) + "_" + c.getString(c.getColumnIndex("pndjv_pill_no")));
                        } else {
                            arrayList.add(c.getString(c.getColumnIndex("pill_no")));
                        }
                    }
                    while (c.moveToNext());*/
                }
            }

        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            c.close();
            db.close();
        }
    }

    private class ExportPointStaticData extends AsyncTask<String, String, String> {

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
            /*this.progressDialog = ProgressDialog.show(DGPSDataExportActivity.this, "", "Please wait...Your Point KML data is downloading", false);
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);*/
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = new FileInputStream(f_url[0]);
                    os = new FileOutputStream(f_url[2]);

                    // buffer size 1K
                    byte[] data = new byte[16384];

                    int bytesRead;
                    while ((bytesRead = is.read(data)) > 0) {
                        os.write(data, 0, bytesRead);
                    }
                    os.flush();
                } catch (Exception ee) {
                    ee.printStackTrace();
                } finally {
                    is.close();
                    os.flush();
                    os.close();
                }
                return f_url[1] + "&" + f_url[2] + "&" + f_url[0];

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return "Point KML files is missing in the server.Please contact your Admin";
            //return "CMV/MMV files is missing in the server";
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            //progressDialog.setProgress(Integer.parseInt(progress[0]));
        }


        @Override
        protected void onPostExecute(String message) {
            String arr[];
            arr = message.split("&");
            try {
                db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                //db1 = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                if (arr[0].contains("_")) {
                    String pillar_data[] = arr[0].split("_");
                    c = db.rawQuery("update m_fb_dgps_survey_pill_data set pillar_sfile_status='1',pillar_sfile_path='" + arr[1] + "' where pill_no='" + pillar_data[0] + "' and pndjv_pill_no='" + pillar_data[1] + "' and fb_id='" + sharefb + "'", null);
                } else {
                    c = db.rawQuery("update m_fb_dgps_survey_pill_data set pillar_sfile_status='1',pillar_sfile_path='" + arr[1] + "' where pill_no='" + arr[0] + "' and fb_id='" + sharefb + "'", null);
                }

                if (c.getCount() >= 0) {
                    try {
                        File file = new File(arr[2]);
                        if (file.delete()) {
                            if (GetTaggingTableforDGPS(arr[0])) {
                                UpdateTaggingPillarTable(arr[0]);
                                Toast.makeText(getApplicationContext(), "Your data tagging is successfully Completed", Toast.LENGTH_LONG);
                                Intent i = new Intent(getApplicationContext(), DGPSDataExportActivity.class);
                                startActivity(i);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Your data tagging is unsuccessfully", Toast.LENGTH_LONG);
                            recreate();
                        }
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    } finally {
                    }
                }

            } catch (Exception ee) {
                ee.printStackTrace();
            } finally {
                c.close();
                db.close();
            }
        }
    }

    private void UpdateTaggingPillarTable(String s) {
        Cursor c = null;
        try {
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            if (s.contains("_")) {
                String pillar_data[] = s.split("_");
                c = db.rawQuery("update m_dgps_Survey_pill_data set m_dgps_file_sts='1' where m_p_lat='" + frjvc_lat + "' and m_p_long='" + frjvc_long + "' and m_fb_pillar_no='" + pillar_data[0] + "' and m_pndjv_pill_no='" + pillar_data[1] + "'", null);
            } else {
                c = db.rawQuery("update m_dgps_Survey_pill_data set m_dgps_file_sts='1' where m_p_lat='" + frjvc_lat + "' and m_p_long='" + frjvc_long + "' and m_fb_pillar_no='" + s + "'", null);
            }
            if (c.getCount() >= 0) {
                Toast.makeText(this, "dd", Toast.LENGTH_LONG);
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            c.close();
            db.close();
        }
    }

    private boolean GetTaggingTableforDGPS(String s) {
        boolean status = false;
        try {
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            if (s.contains("_")) {
                String pillar_data[] = s.split("_");
                c = db.rawQuery("select * from m_fb_dgps_survey_pill_data where pillar_sfile_status='1' and delete_status='0' and fb_id='" + sharefb + "' and pill_no='" + pillar_data[0] + "' and pndjv_pill_no='" + pillar_data[1] + "'", null);
            } else {
                c = db.rawQuery("update m_dgps_Survey_pill_data set m_dgps_file_sts='1' where m_fb_pillar_no='" + s + "'", null);
            }
            int count = c.getCount();
            if (count >= 1) {
                if (c.moveToFirst()) {
                    frjvc_lat = c.getString(c.getColumnIndex("frjvc_lat"));
                    frjvc_long = c.getString(c.getColumnIndex("frjvc_long"));
                    status = true;
                }
            }

        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            c.close();
            db.close();
        }
        return status;
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
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                if (Environment.isExternalStorageManager()) {
////                    startActivity(new Intent(this, DGPSDataTaggMenuActivity.class));
//                } else { //request for the permission
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
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

    private String copyFileToInternalStorage(Uri uri, String newDirName) {
        Uri returnUri = uri;

        Cursor returnCursor = DGPSDataExportActivity.this.getContentResolver().query(returnUri, new String[]{
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
        }, null, null, null);


        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));

        File output;
        if (!newDirName.equals("")) {
            File dir = new File(DGPSDataExportActivity.this.getFilesDir() + "/" + newDirName);
            if (!dir.exists()) {
                dir.mkdir();
            }
            output = new File(DGPSDataExportActivity.this.getFilesDir() + "/" + newDirName + "/" + name);
        } else {
            output = new File(DGPSDataExportActivity.this.getFilesDir() + "/" + name);
        }
        try {
            InputStream inputStream = DGPSDataExportActivity.this.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(output);
            int read = 0;
            int bufferSize = 1024;
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }

            inputStream.close();
            outputStream.close();

        } catch (Exception e) {

            Log.e("Exception", e.getMessage());
        }

        return output.getPath();
    }

}