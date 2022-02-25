package com.sparc.frjvcapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.sparc.frjvcapp.Adapter.DGPSPillarViewAdapter;
import com.sparc.frjvcapp.Adapter.DGPSRTXViewAdapter;
import com.sparc.frjvcapp.pojo.DGPSPillarDataViewModel;
import com.sparc.frjvcapp.pojo.DGPSRTXVIewModel;

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

public class DFPSRTXDataExportActivity extends AppCompatActivity {
    public static final String data = "data";
    public HashMap<String, String> fbKey;
    ArrayAdapter<String> dataAdapter = null;
    ImageView img_download;
    TextView txtStsFileName;
    private SQLiteDatabase db;
    private ArrayList<String> arrayList;
    private ArrayList<DGPSRTXVIewModel> arrayList1;
    private TextInputEditText pill_no;
    Cursor c;
    String sfinalpath, dfinalpath;
    static File d;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    public DGPSRTXViewAdapter adapter;
    String sharediv, sharerange, sharefb, sharefbtype, sharefbname, userid, jobid, div_name, range_name, fb_name, frjvc_long, txtFb, rtxFileName;
    SharedPreferences shared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dfpsrtxdata_export);
        recyclerView = findViewById(R.id.rtxRecycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        img_download = findViewById(R.id.img_download);
        txtStsFileName = findViewById(R.id.txtStsFileName);
        arrayList = new ArrayList<String>();
        arrayList1 = new ArrayList<DGPSRTXVIewModel>();

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

        String sfile = Environment.getExternalStorageDirectory().toString();
        String path = "/SurveyMobile.Droid/Export";
        sfinalpath = sfile + path;

        String dfile = Environment.getExternalStorageDirectory().toString();
        String dpath = "/RTXData";
        dfinalpath = dfile + dpath;

        File s = new File(sfinalpath);
        d = new File(dfinalpath);
        if (!d.exists()) {
            d.mkdirs();
        }


        File[] listOfFiles = s.listFiles();
        if (listOfFiles.length > 0) {
            for (int i = 0; i < listOfFiles.length; ) {
                String a = getFileExtension(listOfFiles[i]);
                if (getFileExtension(listOfFiles[i]).equals(".csv")) {
                    if (listOfFiles[i].isFile()) {
                        img_download.setVisibility(View.VISIBLE);
                        txtStsFileName.setText(listOfFiles[i].getName());
                        rtxFileName = listOfFiles[i].getName();
                        break;
                    } else {
                        txtStsFileName.setText("No file available for download");
                        img_download.setVisibility(View.GONE);
                        break;
                    }
                } else {
                    i++;
                    if (1 == 1)
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
        } else {
            txtStsFileName.setText("No file available for download");
            txtStsFileName.setTextSize(15);
            txtStsFileName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            txtStsFileName.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            txtStsFileName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
            img_download.setVisibility(View.GONE);
        }
        getRTXView();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        img_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDGPSForestBlockData();

                //alertDialogBuilder.setMessage("Are you sure to save this pillar data?");
                final View customLayout = getLayoutInflater().inflate(R.layout.assign_fb_to_rtx, null);
                alertDialogBuilder.setView(customLayout);
                pill_no = customLayout.findViewById(R.id.fb_name);
                pill_no.setText(txtFb);
                alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                //check stakeout or rtx file
                                try {
                                    ArrayList<String> stakeoutcheckList = new ArrayList<>();
                                    ArrayList<String> stakeoutValue = new ArrayList<>();
                                    CSVReader reader = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().toString() + "/SurveyMobile.Droid/Export/" + rtxFileName + ""));
                                    String[] nextLine;
                                    String columnLength[] = reader.readNext();
                                    if (columnLength[0] != null) {
                                        stakeoutcheckList.clear();
                                        stakeoutValue.clear();
                                        while ((nextLine = reader.readNext()) != null) {

                                            stakeoutcheckList.add(nextLine[4]);
                                            if (nextLine[4].equals("100")) {
                                                stakeoutValue.add(nextLine[4]);
                                            }
                                            System.out.println(nextLine[0] + nextLine[1] + "etc...");
                                        }
                                        //stakeout file
                                        if (stakeoutcheckList.size() == stakeoutValue.size()) {
                                            AlertDialog.Builder connectionerrorBuilder = new AlertDialog.Builder(DFPSRTXDataExportActivity.this);
                                            connectionerrorBuilder.setTitle("Wrong File Tagged !!!");
                                            connectionerrorBuilder.setMessage("You are trying to tag Stakeout File. Please go to survey mobile app and open your Job : " + jobid + " and export the RTX File.");
                                            connectionerrorBuilder.setCancelable(false);
                                            connectionerrorBuilder.setPositiveButton("OK",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface arg0, int arg1) {
                                                            //delete stake out
                                                            String delFile = Environment.getExternalStorageDirectory().toString() + "/SurveyMobile.Droid/Export";
                                                            File f = new File(delFile);
                                                            File[] files = f.listFiles();
                                                            if (files != null)
                                                                for (int d = 0; d < files.length; d++) {
                                                                    File file = files[d];
                                                                    if (getFileExtension(file).equals(".csv")) {
                                                                        file.delete();
                                                                    }
                                                                }
                                                            recreate();
                                                            arg0.cancel();
                                                        }
                                                    });
                                            AlertDialog alertDialog1 = connectionerrorBuilder.create();
                                            alertDialog1.show();

                                        } else if (columnLength.length <= 11) {
                                            AlertDialog.Builder connectionerrorBuilder = new AlertDialog.Builder(DFPSRTXDataExportActivity.this);
                                            connectionerrorBuilder.setTitle("Wrong File Tagged !!!");
                                            connectionerrorBuilder.setMessage("You are trying to tag inappropriate RTX File. Please go to survey mobile app and export CSV File Type with Default template. ");
                                            connectionerrorBuilder.setCancelable(false);
                                            connectionerrorBuilder.setPositiveButton("OK",
                                                    new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface arg0, int arg1) {
                                                            //delete stake out
                                                            String delFile = Environment.getExternalStorageDirectory().toString() + "/SurveyMobile.Droid/Export";
                                                            File f = new File(delFile);
                                                            File[] files = f.listFiles();
                                                            if (files != null)
                                                                for (int d = 0; d < files.length; d++) {
                                                                    File file = files[d];
                                                                    if (getFileExtension(file).equals(".csv")) {
                                                                        file.delete();
                                                                    }
                                                                }
                                                            recreate();
                                                            arg0.cancel();
                                                        }
                                                    });
                                            AlertDialog alertDialog1 = connectionerrorBuilder.create();
                                            alertDialog1.show();
                                        }
                                        //rtx file
                                        else {

                                            //update precision and solution type to table
                                            CSVReader reader1 = new CSVReader(new FileReader(Environment.getExternalStorageDirectory().toString() + "/SurveyMobile.Droid/Export/" + rtxFileName + ""));
                                            String[] nextLine1;
                                            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                                            while ((nextLine1 = reader1.readNext()) != null) {
                                                Cursor cursor;
                                                if (nextLine1[0].contains("_")) {
                                                    String[] newpillar = nextLine1[0].split("_");
                                                    cursor = db.rawQuery("update m_fb_dgps_survey_pill_data set pill_hor_precision='" + nextLine1[8] + "',pill_verti_precision='" + nextLine1[9] + "',pill_solution_type='" + nextLine1[11] + "' where pill_no='" + newpillar[0] + "' and pndjv_pill_no='" + newpillar[1] + "' and fb_id='" + sharefb + "'", null);
                                                } else {
                                                    cursor = db.rawQuery("update m_fb_dgps_survey_pill_data set pill_hor_precision='" + nextLine1[8] + "',pill_verti_precision='" + nextLine1[9] + "',pill_solution_type='" + nextLine1[11] + "' where pill_no='" + nextLine1[0] + "' and fb_id='" + sharefb + "'", null);
                                                }
                                                if (cursor.getCount() >= 0) {
                                                    Log.d("precision", "updated");
                                                }
                                                cursor.close();
                                            }

                                            db.close();
                                            //////after update uncomment the below code
                                            String d_f_name[] = txtStsFileName.getText().toString().split("\\.");//.split(".")
                                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                            String f_file_name = sharediv + "_" + sharerange + "_" + sharefb + "_" + userid + "_" + timeStamp + "." + d_f_name[1];
                                            new DFPSRTXDataExportActivity.ExportPointRTXData().execute(sfinalpath + "/" + txtStsFileName.getText().toString(), sharefb, dfinalpath + "/" + f_file_name);
                                        }
                                    } else {
                                        AlertDialog.Builder connectionerrorBuilder = new AlertDialog.Builder(DFPSRTXDataExportActivity.this);
                                        connectionerrorBuilder.setTitle("Blank File !!!");
                                        connectionerrorBuilder.setMessage("You are trying to tag blank file. Please go to survey mobile app and export RTX File. ");
                                        connectionerrorBuilder.setCancelable(false);
                                        connectionerrorBuilder.setPositiveButton("OK",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface arg0, int arg1) {
                                                        //delete stake out
                                                        String delFile = Environment.getExternalStorageDirectory().toString() + "/SurveyMobile.Droid/Export";
                                                        File f = new File(delFile);
                                                        File[] files = f.listFiles();
                                                        if (files != null)
                                                            for (int d = 0; d < files.length; d++) {
                                                                File file = files[d];
                                                                if (getFileExtension(file).equals(".csv")) {
                                                                    file.delete();
                                                                }
                                                            }
                                                        recreate();
                                                        arg0.cancel();
                                                    }
                                                });
                                        AlertDialog alertDialog1 = connectionerrorBuilder.create();
                                        alertDialog1.show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
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
        adapter = new DGPSRTXViewAdapter(this, arrayList1);
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

    private class ExportPointRTXData extends AsyncTask<String, String, String> {

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
                c = db.rawQuery("update m_fb_dgps_survey_pill_data set pillar_rfile_status='1',pillar_rfile_path='" + arr[1] + "' where fb_id='" + arr[0] + "' and (pillar_rfile_path is null or pillar_rfile_status='0') ", null);
                if (c.getCount() >= 0) {
                    try {
                        File file = new File(arr[2]);
                        if (file.delete()) {
                            /*if (GetTaggingTableforDGPS(arr[0])) {
                                UpdateTaggingPillarTable(arr[0]);*/
                            Toast.makeText(getApplicationContext(), "Your data taging is successfully Completed", Toast.LENGTH_LONG);
                            Intent i = new Intent(getApplicationContext(), DFPSRTXDataExportActivity.class);
                            startActivity(i);
                            /*  }*/

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
        cursor.close();
        db.close();
        //   dataAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_row, fbName);
    }


    private void getRTXView() {
        try {
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            arrayList1.clear();
            c = db.rawQuery("SELECT * from m_fb_dgps_survey_pill_data where delete_status='0' and pillar_rfile_path!='' and fb_id='" + sharefb + "' group by pillar_rfile_path", null);
            int count = c.getCount();
            if (count >= 1) {
                if (c.moveToFirst()) {
                    do {
                        DGPSRTXVIewModel dgpsrtxvIewModel = new DGPSRTXVIewModel();
                        dgpsrtxvIewModel.setFbname(c.getString(c.getColumnIndex("fb_name")));
                        dgpsrtxvIewModel.setR_file_path(c.getString(c.getColumnIndex("pillar_rfile_path")));
                        dgpsrtxvIewModel.setR_status(c.getString(c.getColumnIndex("pillar_rfile_status")));
                        arrayList1.add(dgpsrtxvIewModel);
                    }
                    while (c.moveToNext());
                }
            }
            adapter = new DGPSRTXViewAdapter(this, arrayList1);
            recyclerView.setAdapter(adapter);
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            c.close();
            db.close();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), DGPSDataTaggMenuActivity.class);
        startActivity(intent);
    }
}
