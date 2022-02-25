package com.sparc.frjvcapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Environment;

import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.sparc.frjvcapp.Adapter.DGPSJXLViewAdapter;
import com.sparc.frjvcapp.pojo.DGPSJXLViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DGPSJXLDataExportActivity extends AppCompatActivity {
    public static final String data = "data";
    public HashMap<String, String> fbKey;
    ArrayAdapter<String> dataAdapter = null;
    ImageView img_download;
    TextView txtStsFileName;
    private SQLiteDatabase db;
    private ArrayList<String> arrayList;
    private ArrayList<DGPSJXLViewModel> arrayList1;
    private TextInputEditText pill_no;
    String spill_no;
    Cursor c;
    String sfinalpath, dfinalpath;
    static File d;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    public DGPSJXLViewAdapter adapter;
    String sharediv, sharerange, sharefb, sharefbtype, sharefbname, userid, jobid, div_name, range_name, fb_name, frjvc_long, txtFb, jxlFilename;
    SharedPreferences shared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dgpsjxldata_export);
        recyclerView = findViewById(R.id.jxlRecycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        img_download = findViewById(R.id.img_download);
        txtStsFileName = findViewById(R.id.txtStsFileName);
        arrayList = new ArrayList<String>();
        arrayList1 = new ArrayList<DGPSJXLViewModel>();

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

//        String sfile = Environment.getExternalStorageDirectory().toString();
//        String path = "/SurveyMobile.Droid/Export";
//        sfinalpath = sfile + path;
//
//        String dfile = Environment.getExternalStorageDirectory().toString();
//        String dpath = "/JXLFile";
//        dfinalpath = dfile + dpath;

        try {
            String sfile = Environment.getExternalStorageDirectory().toString();
//                String path = "/SurveyMobile.Droid/Download";
            String path = "/SurveyMobile.Droid";
            File downloadFile=new File(sfile+path+"/Export");
//                sfinalpath = sfile + path;
            sfinalpath = downloadFile.getPath();

            String dfile = Environment.getExternalStorageDirectory().toString();
            File staticFile=new File(dfile+"/JXLFile");
//                String dpath = "/StaticData";
//                dfinalpath = dfile + dpath;
            dfinalpath = staticFile.getPath();

        }catch (Exception e){
            e.printStackTrace();
        }

        File s = new File(sfinalpath);
        d = new File(dfinalpath);
        if (!d.exists()) {
            d.mkdirs();
        }
        File[] listOfFiles = s.listFiles();
        if (listOfFiles.length > 0) {
            for (int i = 0; i < listOfFiles.length; ) {
                if (getFileExtension(listOfFiles[i]).equals(".jxl")) {
                    if (listOfFiles[i].isFile()) {
                        img_download.setVisibility(View.VISIBLE);
                        txtStsFileName.setText(listOfFiles[i].getName());
                        jxlFilename = listOfFiles[i].getName();

                        break;
                    } else {
                        txtStsFileName.setText("No file available for download");
                        Typeface face = Typeface.createFromAsset(getAssets(),
                                "open_sans.ttf");
                        txtStsFileName.setTypeface(face);
                        img_download.setVisibility(View.GONE);
                        break;
                    }
                } else {
                    i++;
                    if (i == 1) {
                        txtStsFileName.setText("No file available for download");
                        img_download.setVisibility(View.GONE);
                        String delFile = Environment.getExternalStorageDirectory().toString() + "/SurveyMobile.Droid/Export";
                        File f = new File(delFile);
                        File[] files = f.listFiles();
                        if (files != null)
                            for (int d = 0; d < files.length; d++) {
                                File file = files[d];
                                if (!getFileExtension(file).equals(".jxl") && !getFileExtension(file).equals(".csv")) {
                                    file.delete();
                                }
                            }
                    }
                }
            }
        } else {
            txtStsFileName.setText("No file available for download");
            txtStsFileName.setTextSize(15);
/*            Typeface face = Typeface.createFromAsset(getAssets(),
                    "open_sans.ttf");*/
            //txtStsFileName.setTypeface(face);
            txtStsFileName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            txtStsFileName.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            txtStsFileName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
            img_download.setVisibility(View.GONE);
        }
        getJXLView();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        img_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDGPSForestBlockData();
                final View customLayout = getLayoutInflater().inflate(R.layout.assign_fb_to_rtx, null);
                alertDialogBuilder.setView(customLayout);
                pill_no = customLayout.findViewById(R.id.fb_name);
                pill_no.setText(txtFb);
                alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                String d_f_name[] = txtStsFileName.getText().toString().split("\\.");//.split(".")
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                String f_file_name = sharediv + "_" + sharerange + "_" + sharefb + "_" + userid + "_" + timeStamp + "." + d_f_name[1];
                                new DGPSJXLDataExportActivity.ExportPointJXLData().execute(sfinalpath + "/" + txtStsFileName.getText().toString(), sharefb, dfinalpath + "/" + f_file_name);
                               // recreate();
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
        adapter = new DGPSJXLViewAdapter(this, arrayList1);
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

    private class ExportPointJXLData extends AsyncTask<String, String, String> {

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
            // dismiss the dialog after the file was downloaded
            //this.progressDialog.dismiss();
            try {
                db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                c = db.rawQuery("update m_fb_dgps_survey_pill_data set pillar_jfile_status='1',pillar_jfile_path='" + arr[1] + "' where fb_id='" + arr[0] + "' and (pillar_jfile_status='0' or pillar_jfile_path is null) ", null);
                if (c.getCount() >= 0) {
                    try {
                        File file = new File(arr[2]);
                        if (file.delete()) {
                            //if (GetTaggingTableforDGPS(arr[0])) {
                            // UpdateTaggibngPillarTable(arr[0]);
                            Toast.makeText(getApplicationContext(), "Your data taging is successfully Completed", Toast.LENGTH_LONG);
                            Intent i = new Intent(getApplicationContext(), DGPSJXLDataExportActivity.class);
                            startActivity(i);
//                              }

                        } else {
                            Toast.makeText(getApplicationContext(), "Your data tagging is unsuccessfully", Toast.LENGTH_LONG);
                            recreate();
                        }
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    } finally {

                    }
                }
                c.close();
                db.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    private void getDGPSForestBlockData() {
        List<String> fbName = new ArrayList<String>();
        //rangeName.add("Select Range");
        fbKey = new HashMap<>();
        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("select distinct fb_name,fb_id from m_fb_dgps_survey_pill_data where d_id='" + sharediv + "' and fb_id='" + sharefb + "' and r_id ='" + sharerange + "' order by fb_name", null);
        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            txtFb = cursor.getString(cursor.getColumnIndex("fb_name"));
        }
        /*if (cursor.moveToFirst()) {
            do {
                fbName.add(cursor.getString(cursor.getColumnIndex("fb_name")));
                fbKey.put(cursor.getString(cursor.getColumnIndex("fb_name")), cursor.getString(cursor.getColumnIndex("fb_id")));
            } while (cursor.moveToNext());
        }*/
        cursor.close();
        db.close();
        //  dataAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_row, fbName);
    }

    private void getJXLView() {
        try {
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            arrayList1.clear();
            c = db.rawQuery("SELECT * from m_fb_dgps_survey_pill_data where delete_status='0' and pillar_jfile_path!='' and fb_id='" + sharefb + "' group by pillar_jfile_path", null);
            int count = c.getCount();
            if (count >= 1) {
                if (c.moveToFirst()) {
                    do {
                        DGPSJXLViewModel dgpsjxlViewModel = new DGPSJXLViewModel();
                        dgpsjxlViewModel.setFbname(c.getString(c.getColumnIndex("fb_name")));
                        dgpsjxlViewModel.setJ_file_path(c.getString(c.getColumnIndex("pillar_jfile_path")));
                        dgpsjxlViewModel.setJ_status(c.getString(c.getColumnIndex("pillar_jfile_status")));
                        arrayList1.add(dgpsjxlViewModel);
                    }
                    while (c.moveToNext());
                }
            }
            adapter = new DGPSJXLViewAdapter(this, arrayList1);
            recyclerView.setAdapter(adapter);
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            c.close();
            db.close();
        }
    }
}
