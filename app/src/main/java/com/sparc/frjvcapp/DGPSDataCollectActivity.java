package com.sparc.frjvcapp;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.sparc.frjvcapp.pojo.M_dgps_pill_pic;
import com.sparc.frjvcapp.pojo.M_dgps_pilldata;
import com.sparc.frjvcapp.pojo.M_dgpssurvey_pillar_data;

import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.ganfra.materialspinner.MaterialSpinner;

public class DGPSDataCollectActivity extends AppCompatActivity {
    public static final String data = "data";
    public static final String _staticTime = "statictime";
    private static final int ACTION_TAKE_PHOTO_B = 1;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String PNG_FILE_SUFFIX = ".jpg";
    private final static int REQUEST_CHECK_SETTINGS = 2000;
    private static final int ACTION_TAKE_GALLERY_PIC = 0;
    public String imgValue = "blank";
    String[] duration = {"Clockwise", "Anticlockwise"};
    String[] _survey_Possibility = {"Yes", "No"};
    String[] _surveyReason = {"Inaccessible", "Encroachment", "Other"};
    String[] _spinPillarType = {"Boundary point", "SCP",};
    MaterialSpinner bpduration, surveyPossibility, chooseReason, spinPillarType;
    EditText edttxtpillarno, remark, edttxtpatchno, edttxtringno, edtForestoffnm, edtJobID, edtdpillno, edtremark;
    ImageView setpicforward, takepicforward, setpicbackward, takepicbackward, setpicinward, takepicinward, setpicoutward, takepicoutward, setpictop, takepictop;
    TextView txtViewdiv, txtViewran, txtViewfb, fbname;
    LinearLayout lh1, lh2, l17;
    String sharediv, sharerange, sharefb, sharefbtype, sharefbname,
            userid, jobid, div_name, range_name, fb_name, spinner_duration, spinner_segment, id, d_frjvc_lat, d_frjvc_long, d_old_id,
            d_frjvc_pill_no, imagepath1_F, imagepath1_B, imagepath1_I, imagepath1_O, imagepath1_T,
            d_check_sts, _startTime, _endTime, _surveyPossibility, _reason, _remark, pillarTypeValue, newPillRemarkTExt, newPillarEdit;
    Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;
    Map<Character, String> image_name;
    String imei;
    int clickedStatus, timecheck = 0;
    DbHelper dbHelper;
    SharedPreferences shared, sh, sharedForestPersonName;
    SQLiteDatabase db;
    int point_no, pndjv_no;
    String kmlstatus, personName;
    private String mCurrentPhotoPath_F, mCurrentPhotoPath_B, mCurrentPhotoPath_I, mCurrentPhotoPath_O, mCurrentPhotoPath_T;
    private Character pic_status;
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    SimpleDateFormat _startDateFormat, _endDateFormat;
    long _min, _second;
    TelephonyManager telephonyManager;
    int pillarTypeCode;
    public static final String NotificationPREf = "notiPrefs";
    public static final String PREVIOUS_TIME = "previousPref";

    CheckBox newPillCheck;
    EditText newEditRemark;

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dgpsdata_collect);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
        dbHelper = new DbHelper(getApplicationContext());
        //Matterial Spinner

//        bpduration = findViewById(R.id.bpduration);
        surveyPossibility = findViewById(R.id.surveyPossibility);
        chooseReason = findViewById(R.id.chooseReason);
        spinPillarType = findViewById(R.id.scptype);

        //EditText
        edttxtpillarno = findViewById(R.id.edttxtpillarno);
        remark = findViewById(R.id.remark);
        edttxtpatchno = findViewById(R.id.edttxtpatchno);
        edttxtringno = findViewById(R.id.edttxtringno);
        edtJobID = findViewById(R.id.edtJobID);
        edtdpillno = findViewById(R.id.edtdpillno);
        edtForestoffnm = findViewById(R.id.edtForestoffnm);
        edtremark = findViewById(R.id.edtremark);

        //Image View
        setpicforward = findViewById(R.id.setpicforward);
        takepicforward = findViewById(R.id.takepicforward);
        setpicbackward = findViewById(R.id.setpicbackward);
        takepicbackward = findViewById(R.id.takepicbackward);
        setpicinward = findViewById(R.id.setpicinward);
        takepicinward = findViewById(R.id.takepicinward);
        setpicoutward = findViewById(R.id.setpicoutward);
        takepicoutward = findViewById(R.id.takepicoutward);
        setpictop = findViewById(R.id.setpictop);
        takepictop = findViewById(R.id.takepictop);

        //TextView
        txtViewdiv = findViewById(R.id.txtViewdiv);
        txtViewran = findViewById(R.id.txtViewran);
        txtViewfb = findViewById(R.id.txtViewfb);
        fbname = findViewById(R.id.fbname);

        lh1 = findViewById(R.id.lh1);
        lh2 = findViewById(R.id.lh2);
        l17 = findViewById(R.id.l17);

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

        fbname.setText(sharefbname + " " + sharefbtype);
        edttxtpatchno.setText("1");
        edttxtringno.setText("0");
        edtJobID.setText(jobid);

        sh = getApplicationContext().getSharedPreferences("key", MODE_PRIVATE);
        personName = sh.getString("personName", "");
//edtForestoffnm.setText(personName);

        if (personName != "") {
            edtForestoffnm.setText(personName);
        } else {
            edtForestoffnm.setText(" ");
        }

        //point_no = getSLNO(sharefb);


        Intent i = getIntent();
        kmlstatus = i.getStringExtra("kml_status");
        id = i.getStringExtra("id");
        d_frjvc_lat = i.getStringExtra("lat");
        d_frjvc_long = i.getStringExtra("lon");
        d_frjvc_pill_no = i.getStringExtra("pill_no");
        newEditRemark = findViewById(R.id.editremarkNew);

        if (d_frjvc_pill_no.contains("_")) {
            String arr[] = d_frjvc_pill_no.split("_");
            d_old_id = "0";
            point_no = Integer.parseInt(arr[0]);
            pndjv_no = Integer.parseInt(arr[1]);
            edttxtpillarno.setEnabled(true);
            edttxtpillarno.setMaxLines(1);
            newPillCheck = (CheckBox) findViewById(R.id.checkBox);
            newPillCheck.setVisibility(View.VISIBLE);
            newPillCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                        @Override
                                                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                            if (newPillCheck.isChecked()) {
                                                                newEditRemark.setVisibility(View.VISIBLE);
                                                                newEditRemark.setText("Pillar Fixed as per field suitability.");
                                                            } else {
                                                                newEditRemark.setVisibility(View.GONE);
                                                            }
                                                        }
                                                    }
            );
        } else {
            point_no = Integer.parseInt(d_frjvc_pill_no);
            d_old_id = i.getStringExtra("old_id");
            pndjv_no = 0;
        }

        edttxtpillarno.setText(d_frjvc_pill_no);


        txtViewdiv.setText(div_name);
        txtViewran.setText(range_name);
        txtViewfb.setText(fb_name);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            imei = getUniqueIMEIId(this);
        } else {

        }

        (findViewById(R.id.button_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = null;
                i = new Intent(getApplicationContext(), DGPSMapViewActivity.class);
                i.putExtra("kml_status", kmlstatus);
                /* i.putExtra("check_sts", d_check_sts);*/
                startActivity(i);
            }
        });
        (findViewById(R.id.save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveData();
            }
        });

        takepicforward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    if (setpicforward.getDrawable() == null) {
                        SelectImage();
                    } else {
                        if (setpicforward.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_photo_camera_black_24dp).getConstantState()) {
                            setpicforward.setImageResource(0);
                            SelectImage();
                        } else {
                            SelectImage();
                        }
                    }
                }
            }
        });
        takepicbackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    if (setpicbackward.getDrawable() == null) {
                        SelectImageBackward();
                    } else {
                        if (setpicbackward.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_photo_camera_black_24dp).getConstantState()) {
                            setpicbackward.setImageResource(0);
                            SelectImageBackward();
                        } else {
                            SelectImageBackward();
                        }
                    }
                }
            }
        });
        takepicinward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    if (setpicinward.getDrawable() == null) {
                        SelectImageInward();
                    } else {
                        if (setpicinward.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_photo_camera_black_24dp).getConstantState()) {
                            setpicinward.setImageResource(0);
                            SelectImageInward();
                        } else {
                            SelectImageInward();
                        }
                    }
                }
            }
        });
        takepicoutward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    if (setpicoutward.getDrawable() == null) {
                        SelectImageOutward();
                    } else {
                        if (setpicoutward.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_photo_camera_black_24dp).getConstantState()) {
                            setpicoutward.setImageResource(0);
                            SelectImageOutward();
                        } else {
                            SelectImageOutward();
                        }
                    }
                }
            }
        });
        takepictop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                    if (setpictop.getDrawable() == null) {
                        SelectImageTop();
                    } else {
                        if (setpictop.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_photo_camera_black_24dp).getConstantState()) {
                            setpictop.setImageResource(0);
                            SelectImageTop();
                        } else {
                            SelectImageTop();
                        }
                    }
                }
            }
        });

        final ArrayAdapter<String> _surveyPossibilityadapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_row, _survey_Possibility);
        surveyPossibility.setAdapter(_surveyPossibilityadapter);
        surveyPossibility.setPaddingSafe(0, 0, 0, 0);
        surveyPossibility.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _surveyPossibility = (String) parent.getItemAtPosition(position);
                if (!_surveyPossibility.equals("Is DGPS Survey Possible for this Pillar?")) {
                    //CheckPillarStatus(locationtype);
                    if (_surveyPossibility.equals("Yes")) {
                        lh1.setVisibility(View.VISIBLE);
                        lh2.setVisibility(View.GONE);
                        l17.setVisibility(View.VISIBLE);
                    } else if (_surveyPossibility.equals("No")) {
                        lh1.setVisibility(View.GONE);
                        lh2.setVisibility(View.VISIBLE);
                        l17.setVisibility(View.VISIBLE);
                    } else {

                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final ArrayAdapter<String> _pillartypeadapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_row, _spinPillarType);
        spinPillarType.setAdapter(_pillartypeadapter);
        spinPillarType.setPaddingSafe(0, 0, 0, 0);
        spinPillarType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pillarTypeValue = (String) parent.getItemAtPosition(position);
                if (!pillarTypeValue.equals("Select Pillar Type?")) {
                    //CheckPillarStatus(locationtype);
                    if (pillarTypeValue.equals("Boundary point")) {
                        pillarTypeCode = 1;
                    } else if (pillarTypeValue.equals("SCP")) {
                        pillarTypeCode = 2;
                    } else {

                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final ArrayAdapter<String> _reasonofNosurvey = new ArrayAdapter<String>(getApplicationContext(), R.layout.spinner_row, _surveyReason);
        chooseReason.setAdapter(_reasonofNosurvey);
        chooseReason.setPaddingSafe(0, 0, 0, 0);
        chooseReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _reason = (String) parent.getItemAtPosition(position);
                if (!_reason.equals("Select Reason")) {
                    //CheckPillarStatus(locationtype);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Calculate and storage of RTX time for Report generation
        /*if (timecheck != 0) {
            try {
                SharedPreferences shared = getApplicationContext().getSharedPreferences(PREVIOUS_TIME, MODE_PRIVATE);
                if (shared.getString("previousTime", "0").equals("1")) {
                    SharedPreferences.Editor editor = shared.edit();
                    editor.clear();
                    editor.apply();
                }
                Calendar c = Calendar.getInstance();
                _endDateFormat = new SimpleDateFormat("kk:mm:ss");
                _endTime = _endDateFormat.format(c.getTime());
                java.text.DateFormat df = new java.text.SimpleDateFormat("kk:mm:ss");
                java.util.Date date1 = df.parse(_startTime);
                java.util.Date date2 = df.parse(_endTime);
                long diff = date2.getTime() - date1.getTime();
                _min = diff / (60 * 1000) % 60;
                _second = diff / 1000 % 60;
                if ((_min >= 4 && _second >= 30) || (_min >= 14 && _second >= 30)) {
                    _min += 1;

                }
                db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                Cursor cursor;
                if (d_frjvc_pill_no.contains("_")) {
                    String[] pillSplit = d_frjvc_pill_no.split("_");
                    cursor = db.rawQuery("update m_fb_dgps_survey_pill_data set rtx_survey_min='" + _min + "',rtx_survey_second='" + _second + "' where u_id='" + userid + "' and pill_no='" + pillSplit[0] + "' and pndjv_pill_no='" + pillSplit[1] + "' and frjvc_lat='" + d_frjvc_lat + "' and frjvc_long='" + d_frjvc_long + "'", null);

                } else {
                    cursor = db.rawQuery("update m_fb_dgps_survey_pill_data set rtx_survey_min='" + _min + "',rtx_survey_second='" + _second + "' where u_id='" + userid + "' and pill_no='" + d_frjvc_pill_no + "' and frjvc_lat='" + d_frjvc_lat + "' and frjvc_long='" + d_frjvc_long + "'", null);
                }
                if (cursor.getCount() >= 0) {
                    SharedPreferences previoustime = getSharedPreferences(PREVIOUS_TIME, 0);
                    SharedPreferences.Editor editor123 = previoustime.edit();
                    editor123.clear();
                    editor123.putString("mapobserv", "1");
                    editor123.putString("min", String.valueOf(_min));
                    editor123.putString("sec", String.valueOf(_second));
                    editor123.apply();
                    Log.d("observ", "done");
                    AlertDialog.Builder connectionerrorBuilder = new AlertDialog.Builder(DGPSDataCollectActivity.this);
                    connectionerrorBuilder.setTitle("Previous Pill Observation");
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
                    // Toast.makeText(this, "Observation time has been updated to this pillar..", Toast.LENGTH_SHORT).show();
                }
                cursor.close();
                db.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            } finally {
                timecheck = 0;
                Intent i = null;
                i = new Intent(getApplicationContext(), DGPSMapViewActivity.class);
                i.putExtra("kml_status", kmlstatus);
                startActivity(i);
            }}*/
        if (timecheck != 0) {
            timecheck = 0;
            Intent i = null;
            i = new Intent(getApplicationContext(), DGPSMapViewActivity.class);
            i.putExtra("kml_status", kmlstatus);
            startActivity(i);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), DGPSMapViewActivity.class);
        i.setFlags(i.FLAG_ACTIVITY_NEW_TASK | i.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);

    }

    private int getSLNO(String sharefb) {
        //List<String> fbName = new ArrayList<String>();
        //return dbHelper.getPillarData(sharefb);
        return dbHelper.getDGPSPillarData(sharefb);
    }

    private void SaveData() {
        sharedForestPersonName = getSharedPreferences("key", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedForestPersonName.edit();
        String forest_person = edtForestoffnm.getText().toString();
        editor.putString("personName", forest_person);
        editor.apply();

        if (d_frjvc_pill_no.contains("_")) {

            newPillarEdit = edttxtpillarno.getText().toString();
            String arr[] = newPillarEdit.split("_");
            point_no = Integer.parseInt(arr[0]);
            pndjv_no = Integer.parseInt(arr[1]);
            try {
                //Added blank string for error in constructor last for now //27/10/2020
                M_dgpssurvey_pillar_data m_fb = new M_dgpssurvey_pillar_data(d_frjvc_lat, d_frjvc_long, String.valueOf(point_no), "", sharefb, "0", "0", "0", "0", "0", String.valueOf(pndjv_no), "");//object.getString("point_path")
                dbHelper.open();
                dbHelper.insertdgpsSurveyedPointDataData(m_fb);
                dbHelper.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        if (!_surveyPossibility.equals("Is DGPS Survey Possible for this Pillar?")) {
            if (_surveyPossibility.equals("Yes")) {
                if (edttxtpillarno.getText().toString() == "") {
                    Toast.makeText(this, "Serial Number can not ne blank or Zero", Toast.LENGTH_LONG).show();
                } else if (pillarTypeValue.equals("Select Pillar Type?")) {
                    Toast.makeText(this, "Please Choose Pillar Type", Toast.LENGTH_SHORT).show();
                } else if (edtForestoffnm.getText().toString().isEmpty() || edtForestoffnm.getText().toString() == " ") {
                    Toast.makeText(this, "Please Provide the Forest official name", Toast.LENGTH_LONG).show();
                } else if (imagepath1_F == "" || imagepath1_F == null) {
                    Toast.makeText(this, "Front view of pillar is not available", Toast.LENGTH_LONG).show();
                } else if (imagepath1_B == "" || imagepath1_B == null) {
                    Toast.makeText(this, "Back view of pillar is not available", Toast.LENGTH_LONG).show();
                } else if (imagepath1_I == "" || imagepath1_I == null) {
                    Toast.makeText(this, "Inward view of pillar is not available", Toast.LENGTH_LONG).show();
                } else if (imagepath1_O == "" || imagepath1_O == null) {
                    Toast.makeText(this, "Outward view of pillar is not available", Toast.LENGTH_LONG).show();
                } else if (imagepath1_T == "" || imagepath1_T == null) {
                    Toast.makeText(this, "Withdevice view of pillar is not available", Toast.LENGTH_LONG).show();
                } else if (_surveyPossibility.equals("Is DGPS Survey Possible for this Pillar?")) {
                    Toast.makeText(this, "Please select survey possibility", Toast.LENGTH_LONG).show();
                } else {
                    db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                    Cursor c = db.rawQuery("select * from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and d_id='" + sharediv + "' and r_id='" + sharerange + "' and fb_id='" + sharefb + "' and frjvc_lat='" + d_frjvc_lat + "' and frjvc_long='" + d_frjvc_long + "'", null);
                    if (c.getCount() > 0) {
                        if (c.moveToFirst()) {
                            if (Integer.parseInt(c.getString(c.getColumnIndex("pillar_sfile_status"))) == 0) {
                                Toast.makeText(this, "This pillar is already registered.Please tag the pillar with its Static Observation data.", Toast.LENGTH_LONG).show();
                            } else if (Integer.parseInt(c.getString(c.getColumnIndex("pillar_sfile_status"))) == 1) {
                                Toast.makeText(this, "This pillar is already registered and Tagged.", Toast.LENGTH_LONG).show();
                            } else {

                            }
                        }
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        //alertDialogBuilder.setMessage("Are you sure to save this pillar data?");
                        final View customLayout = getLayoutInflater().inflate(R.layout.save_custome_dialod_register_pillar, null);
                        alertDialogBuilder.setView(customLayout);
                        alertDialogBuilder.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        //int sl = Integer.parseInt();
                                        String jobID = edtJobID.getText().toString();
                                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                        Date date = new Date();
                                        SharedPreferences previoustime = getSharedPreferences(PREVIOUS_TIME, 0);
                                        SharedPreferences.Editor editor123 = previoustime.edit();
                                        editor123.clear();
                                        editor123.putString("previousTime", "1");
                                        editor123.putString("exactTime", formatter.format(date));
                                        editor123.putString("userid", userid);
                                        if (d_frjvc_pill_no.contains("_")) {
                                            editor123.putString("d_frjvc_pill_no", newPillarEdit);
                                        } else {
                                            editor123.putString("d_frjvc_pill_no", d_frjvc_pill_no);
                                        }
                                        editor123.putString("d_frjvc_lat", d_frjvc_lat);
                                        editor123.putString("d_frjvc_long", d_frjvc_long);
                                        editor123.apply();
                                        if (newEditRemark.getText().length() > 0) {
                                            newPillRemarkTExt = newEditRemark.getText().toString();
                                        } else {
                                            newPillRemarkTExt = "";
                                        }
                                        M_dgps_pilldata mpr1 = new M_dgps_pilldata(sharediv, sharerange, sharefb, point_no, jobID, userid, "",
                                                "0", "0", "0", "0", "0",
                                                edttxtpatchno.getText().toString(), edttxtringno.getText().toString(), edtForestoffnm.getText().toString(),
                                                userid, formatter.format(date), txtViewdiv.getText().toString(), txtViewran.getText().toString(),
                                                txtViewfb.getText().toString(), "0", "0", "0", "",
                                                "", imagepath1_F, imagepath1_B, imagepath1_I, imagepath1_O, imagepath1_T,
                                                getMyMacAddress(), "", "0",
                                                d_frjvc_lat, d_frjvc_long, edtdpillno.getText().toString(), d_old_id,
                                                "", "0", "0", "0", "0",
                                                "1", "", newPillRemarkTExt, "", "0", String.valueOf(pndjv_no), String.valueOf(pillarTypeCode));//+"_"+pilshiftsts,surdir,accuracy
                                        try {
                                            dbHelper.open();
                                            long status = dbHelper.insertDGPSSurveyPillarData(mpr1);
                                            dbHelper.close();
                                            if (status >= 0) {
                                                if (checkDGPSDataAvalability(1)) {
                                                    image_name = new HashMap<Character, String>();
                                                    image_name.put('F', imagepath1_F);
                                                    image_name.put('B', imagepath1_B);
                                                    image_name.put('I', imagepath1_I);
                                                    image_name.put('O', imagepath1_O);
                                                    image_name.put('T', imagepath1_T);
                                                    long a = insertDGPSImage((HashMap<Character, String>) image_name, userid, point_no, String.valueOf(pndjv_no));
                                                    if (a == 5) {
                                                        ///Notification timer
                                                        SharedPreferences shared = getSharedPreferences(NotificationPREf, MODE_PRIVATE);
                                                        String counterStatus = shared.contains("counter") ? (shared.getString("counter", "")) : "";

                                                        if (!counterStatus.equalsIgnoreCase("")) {
                                                            SharedPreferences.Editor editor = shared.edit();
                                                            editor.putString("counter", "");
                                                            editor.apply();
                                                        }
                                                        new NotifyClass(DGPSDataCollectActivity.this).addNotification("Notification", "Notification is coming", "00:00:00", "0", d_frjvc_pill_no);
                                                        ///Notification timer end

                                                        ClipboardManager cm = (ClipboardManager) getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
                                                        ClipData clipData = ClipData.newPlainText("JobID", jobID);
                                                        cm.setPrimaryClip(clipData);
                                                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("SurveyMobile.Droid");
                                                        if (launchIntent != null) {
                                                            timecheck = 1;
                                                            Calendar c = Calendar.getInstance();
                                                            System.out.println("Current time =&gt; " + c.getTime());
                                                            _startDateFormat = new SimpleDateFormat("kk:mm:ss");
                                                            _startTime = _startDateFormat.format(c.getTime());
                                                            reset();
                                                            startActivity(launchIntent);
                                                        } else {
                                                            Toast.makeText(DGPSDataCollectActivity.this, "There is no package available in android", Toast.LENGTH_LONG).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), "Pillar pictures is not stored", Toast.LENGTH_LONG).show();
                                                    }
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "NoT Pillar registered Successfully", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        } catch (Exception ee) {
                                            ee.printStackTrace();
                                        } finally {
                                            if (dbHelper != null) {
                                                dbHelper.close();
                                            }
                                        }
                                    }
                                });

                        alertDialogBuilder.setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        Toast.makeText(getApplicationContext(), "You canceled the save request...please try again", Toast.LENGTH_LONG).show();
                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }

                }
            } else {
                db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
                Cursor c = db.rawQuery("select * from m_fb_dgps_survey_pill_data where u_id='" + userid + "' and d_id='" + sharediv + "' and r_id='" + sharerange + "' and fb_id='" + sharefb + "' and frjvc_lat='" + d_frjvc_lat + "' and frjvc_long='" + d_frjvc_long + "'", null);
                if (c.getCount() > 0) {
                    if (c.moveToFirst()) {
                        if (Integer.parseInt(c.getString(c.getColumnIndex("pillar_sfile_status"))) == 0) {
                            Toast.makeText(this, "This pillar is already registered.Please tag the pillar with its Static Observation data.", Toast.LENGTH_LONG).show();
                        } else if (Integer.parseInt(c.getString(c.getColumnIndex("pillar_sfile_status"))) == 1) {
                            Toast.makeText(this, "This pillar is already registered and Tagged.", Toast.LENGTH_LONG).show();
                        } else {
                        }
                    }
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    final View customLayout = getLayoutInflater().inflate(R.layout.save_custome_dialod_register_pillar, null);
                    alertDialogBuilder.setView(customLayout);
                    alertDialogBuilder.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    String jobID = edtJobID.getText().toString();
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                    Date date = new Date();
                                    M_dgps_pilldata mpr1 = new M_dgps_pilldata(sharediv, sharerange, sharefb, point_no, jobID, userid, "",
                                            "0", "0", "0", "0", "0",
                                            edttxtpatchno.getText().toString(), edttxtringno.getText().toString(), edtForestoffnm.getText().toString(),
                                            userid, formatter.format(date), txtViewdiv.getText().toString(), txtViewran.getText().toString(),
                                            txtViewfb.getText().toString(), "0", "0", "0", "",
                                            "", "", "", "", "", "",
                                            getMyMacAddress(), "", "0"
                                            , d_frjvc_lat, d_frjvc_long, edtdpillno.getText().toString(), d_old_id,
                                            "", "0", "0", "0", "0",
                                            "0", _reason, edtremark.getText().toString(), "", "0", String.valueOf(pndjv_no), "1");
                                    try {
                                        dbHelper.open();
                                        long status = dbHelper.insertDGPSSurveyPillarData(mpr1);
                                        dbHelper.close();
                                        if (status >= 0) {
                                            if (checkDGPSDataAvalability(2)) {
                                                Toast.makeText(getApplicationContext(), "Pillar registered successfully", Toast.LENGTH_LONG).show();
                                                Intent i = null;
                                                i = new Intent(getApplicationContext(), DGPSMapViewActivity.class);
                                                i.putExtra("kml_status", kmlstatus);
                                                /* i.putExtra("check_sts", d_check_sts);*/
                                                startActivity(i);
                                            }
                                        }
                                    } catch (Exception ee) {
                                        ee.printStackTrace();
                                    } finally {
                                        if (dbHelper != null) {
                                            dbHelper.close();
                                        }
                                    }
                                }
                            });

                    alertDialogBuilder.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    Toast.makeText(getApplicationContext(), "You canceled the save request...please try again", Toast.LENGTH_LONG).show();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

            }
        } else {
            Toast.makeText(this, "Please select survey possibility", Toast.LENGTH_LONG).show();
        }
    }

    private String getMyMacAddress() {
        String macAddress = "";
        telephonyManager = (TelephonyManager) getSystemService(this.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 101);
            macAddress = "";
        } else {
            macAddress = android.provider.Settings.Secure.getString(this.getApplicationContext().getContentResolver(), "android_id");
        }
        return macAddress;
    }

    private int insertDGPSImage(HashMap<Character, String> image_name, String userid, int sl, String pndjv_no) {
        long status = 0;
        int count = 0;
        try {
            for (Map.Entry entry : image_name.entrySet()) {
                M_dgps_pill_pic mpic = new M_dgps_pill_pic(sl, userid, "0", entry.getValue().toString(),
                        entry.getKey().toString(), pndjv_no, sharefb);
                dbHelper.open();
                status = dbHelper.insertDGPSSurveyPillarPic(mpic);
                if (status > 0) {
                    count += 1;
                }
                dbHelper.close();
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return count;
    }

    private boolean checkDGPSDataAvalability(int a) {
        boolean b = false;
        try {
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            Cursor c = db.rawQuery("update m_dgps_Survey_pill_data set m_dgps_surv_sts='1',m_survey_status='" + a + "' where m_p_lat='" + d_frjvc_lat + "' and m_p_long='" + d_frjvc_long + "'", null);
            if (c.getCount() >= 0) {
                b = true;
            }
            c.close();
            db.close();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return b;
    }

    public static String getUniqueIMEIId(Context context) {
        String imei = "";
       /* try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imei = telephonyManager.getImei();
                }else{
                    imei=telephonyManager.getDeviceId();
                }
            }
            *//*Log.e("imei", "=" + imei);
            if (imei != null && !imei.isEmpty()) {
                return imei;
            } else {
                return Build.SERIAL;
            }*//*
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String uniqueID = UUID.randomUUID().toString();
        return imei;
    }

    private void reset() {
        imagepath1_F = null;
        imagepath1_B = null;
        imagepath1_I = null;
        imagepath1_O = null;
        imagepath1_T = null;
        mCurrentPhotoPath_F = null;
        mCurrentPhotoPath_B = null;
        mCurrentPhotoPath_I = null;
        mCurrentPhotoPath_O = null;
        mCurrentPhotoPath_T = null;
        edtForestoffnm.setText("");
        edtdpillno.setText("");
        edtremark.setText("");

        /* dpSegment.setSelection(0);*/
//        bpduration.setSelection(0);
        surveyPossibility.setSelection(0);
        chooseReason.setSelection(0);
        setpicoutward.setImageResource(0);
        setpictop.setImageResource(0);
        setpicbackward.setImageResource(0);
        setpicforward.setImageResource(0);
        setpicinward.setImageResource(0);
    }

    private void SelectImage() {
        final CharSequence[] items = {"Camera", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image");

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    dispatchTakePictureIntent(1, 'F', edttxtpillarno.getText().toString());
                } else if (items[i].equals("Gallery")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private void SelectImageBackward() {
        final CharSequence[] items = {"Camera", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image");

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    dispatchTakePictureIntent(1, 'B', edttxtpillarno.getText().toString());
                } else if (items[i].equals("Gallery")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private void SelectImageInward() {
        final CharSequence[] items = {"Camera", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image");

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    dispatchTakePictureIntent(1, 'I', edttxtpillarno.getText().toString());
                } else if (items[i].equals("Gallery")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private void SelectImageOutward() {
        final CharSequence[] items = {"Camera", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image");

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    dispatchTakePictureIntent(1, 'O', edttxtpillarno.getText().toString());
                } else if (items[i].equals("Gallery")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private void SelectImageTop() {
        final CharSequence[] items = {"Camera", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image");

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    dispatchTakePictureIntent(1, 'T', edttxtpillarno.getText().toString());
                } else if (items[i].equals("Gallery")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private void dispatchTakePictureIntent(int actionCode, Character character, String pll_no) {
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());
        ///Intent takePictureIntent = getPackageManager().getLaunchIntentForPackage("com.psychos.gpsamera.MainActivity");
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        openBackCamera(""+character);

        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            if (Build.VERSION.SDK_INT >= 29) {
                //only api 29 above
                // Create an image file name
                String imageFileName = "D_JPG_" + timeStamp + "_";
                File storageDir = getExternalFilesDir(null);
                File cameraSampleFolder=new File(storageDir+"/CameraSample");
                if (!cameraSampleFolder.exists()){
                    cameraSampleFolder.mkdirs();
                }
                File file=null;
//                File file = File.createTempFile(
//                        imageFileName,  /* prefix */
//                        ".jpg",         /* suffix */
//                        cameraSampleFolder      /* directory */
//                );

                // Save a file: path for use with ACTION_VIEW intents
//                mCurrentPhotoPath = file.getAbsolutePath();
                //checking with type of image
                switch (actionCode) {
                    case ACTION_TAKE_PHOTO_B:
                        if (character == 'F') {
                            try {
                                file = setUpPhotoFile(character, pll_no,cameraSampleFolder);
                                pic_status = character;
                                mCurrentPhotoPath_F = file.getAbsolutePath();
                        /*Uri photoURI = FileProvider.getUriForFile(this,
                                "com.sparc.frjvcapp.provider", f);*/
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                            } catch (IOException e) {
                                e.printStackTrace();
                                file = null;
                                mCurrentPhotoPath_F = null;
                            }
                            break;
                        } else if (character == 'B') {
                            try {
                                file = setUpPhotoFile(character, pll_no,cameraSampleFolder);
                                pic_status = character;
                                mCurrentPhotoPath_B = file.getAbsolutePath();
                      /*  Uri photoURI = FileProvider.getUriForFile(this,
                                "com.sparc.frjvcapp.provider", f);*/
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                            } catch (IOException e) {
                                e.printStackTrace();
                                file = null;
                                mCurrentPhotoPath_B = null;
                            }
                            break;
                        } else if (character == 'I') {
                            try {
                                file = setUpPhotoFile(character, pll_no,cameraSampleFolder);
                                pic_status = character;
                                mCurrentPhotoPath_I = file.getAbsolutePath();
                       /* Uri photoURI = FileProvider.getUriForFile(this,
                                "com.sparc.frjvcapp.provider", f);*/
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                            } catch (IOException e) {
                                e.printStackTrace();
                                file = null;
                                mCurrentPhotoPath_I = null;
                            }
                            break;
                        } else if (character == 'O') {
                            try {
                                file = setUpPhotoFile(character, pll_no,cameraSampleFolder);
                                pic_status = character;
                                mCurrentPhotoPath_O = file.getAbsolutePath();
                                Uri photoURI = FileProvider.getUriForFile(this,
                                        "com.sparc.frjvcapp.provider", file);
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            } catch (IOException e) {
                                e.printStackTrace();
                                file = null;
                                mCurrentPhotoPath_O = null;
                            }
                            break;
                        } else if (character == 'T') {
                            try {
                                file = setUpPhotoFile(character, pll_no,cameraSampleFolder);
                                pic_status = character;
                                mCurrentPhotoPath_T = file.getAbsolutePath();
                       /* Uri photoURI = FileProvider.getUriForFile(this,
                                "com.sparc.frjvcapp.provider", f);*/
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                            } catch (IOException e) {
                                e.printStackTrace();
                                file = null;
                                mCurrentPhotoPath_T = null;
                            }
                            break;
                        }

                    default:
                        break;
                } // switch

                Uri outputFileUri = FileProvider.getUriForFile(DGPSDataCollectActivity.this,
                        getApplicationContext().getPackageName() + ".provider", file);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                //COMPATIBILITY
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, outputFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
                startActivityForResult(cameraIntent, REQUEST_CAMERA);


            } else {
                //only api 29 down
                String imageFileName = timeStamp + ".jpg";

                String rootPath = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/CameraSample/";
                File file=null;

                File root = new File(rootPath);
                if (!root.exists()) {
                    root.mkdirs();
                }

                switch (actionCode) {
                    case ACTION_TAKE_PHOTO_B:
                        if (character == 'F') {
                            try {
                                mCurrentPhotoPath_F = rootPath + imageFileName;
                                file = new File(mCurrentPhotoPath_F);
                                pic_status = character;
//                                mCurrentPhotoPath_F = file.getAbsolutePath();
//                                file = setUpPhotoFile(character, pll_no);
                                file = setUpPhotoFile(character, pll_no,root);
                        /*Uri photoURI = FileProvider.getUriForFile(this,
                                "com.sparc.frjvcapp.provider", f);*/
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                            } catch (IOException e) {
                                e.printStackTrace();
                                file = null;
                                mCurrentPhotoPath_F = null;
                            }
                            break;
                        } else if (character == 'B') {
                            try {
                                mCurrentPhotoPath_B = rootPath + imageFileName;
                                file = new File(mCurrentPhotoPath_B);
//                                file = setUpPhotoFile(character, pll_no);
                                file = setUpPhotoFile(character, pll_no,root);
                                pic_status = character;
//                                mCurrentPhotoPath_B = file.getAbsolutePath();
                      /*  Uri photoURI = FileProvider.getUriForFile(this,
                                "com.sparc.frjvcapp.provider", f);*/
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                            } catch (IOException e) {
                                e.printStackTrace();
                                file = null;
                                mCurrentPhotoPath_B = null;
                            }
                            break;
                        } else if (character == 'I') {
                            try {
                                mCurrentPhotoPath_I = rootPath + imageFileName;
                                file = new File(mCurrentPhotoPath_I);
//                                file = setUpPhotoFile(character, pll_no);
                                file = setUpPhotoFile(character, pll_no,root);
                                pic_status = character;
//                                mCurrentPhotoPath_I = file.getAbsolutePath();
                       /* Uri photoURI = FileProvider.getUriForFile(this,
                                "com.sparc.frjvcapp.provider", f);*/
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                            } catch (IOException e) {
                                e.printStackTrace();
                                file = null;
                                mCurrentPhotoPath_I = null;
                            }
                            break;
                        } else if (character == 'O') {
                            try {
                                mCurrentPhotoPath_O = rootPath + imageFileName;
                                file = new File(mCurrentPhotoPath_O);
//                                file = setUpPhotoFile(character, pll_no);
                                file = setUpPhotoFile(character, pll_no,root);
                                pic_status = character;
//                                mCurrentPhotoPath_O = file.getAbsolutePath();
                                Uri photoURI = FileProvider.getUriForFile(this,
                                        "com.sparc.frjvcapp.provider", file);
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            } catch (IOException e) {
                                e.printStackTrace();
                                file = null;
                                mCurrentPhotoPath_O = null;
                            }
                            break;
                        } else if (character == 'T') {
                            try {
                                mCurrentPhotoPath_T = rootPath + imageFileName;
                                file = new File(mCurrentPhotoPath_T);
//                                file = setUpPhotoFile(character, pll_no);
                                file = setUpPhotoFile(character, pll_no,root);
                                pic_status = character;
//                                mCurrentPhotoPath_T = file.getAbsolutePath();
                       /* Uri photoURI = FileProvider.getUriForFile(this,
                                "com.sparc.frjvcapp.provider", f);*/
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                            } catch (IOException e) {
                                e.printStackTrace();
                                file = null;
                                mCurrentPhotoPath_T = null;
                            }
                            break;
                        }

                    default:
                        break;
                } // switch



                Uri outputFileUri = FileProvider.getUriForFile(DGPSDataCollectActivity.this,
                        getApplicationContext().getPackageName() + ".provider", file);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                //COMPATIBILITY
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, outputFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            }

        }catch (Exception e){
            e.printStackTrace();
        }


//        startActivityForResult(takePictureIntent, actionCode);
    }

    private File setUpPhotoFile(Character character, String pill_no,File cameraSampleFolder) throws IOException {
        File f = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "D_JPG_" + timeStamp + "_";
            String imageNm = "D_JPG_" + timeStamp + "_" + sharefb + "_" + character + "_" + pill_no + "_";
            if (character == 'F') {
                f = File.createTempFile(
                        imageNm,  /* prefix */
                        ".jpg",         /* suffix */
                        cameraSampleFolder      /* directory */
                );
//            f = createImageFile(character, pill_no);
                mCurrentPhotoPath_F = f.getAbsolutePath();
            } else if (character == 'B') {
                f = File.createTempFile(
                        imageNm,  /* prefix */
                        ".jpg",         /* suffix */
                        cameraSampleFolder      /* directory */
                );
//            f = createImageFile(character, pill_no);
                mCurrentPhotoPath_B = f.getAbsolutePath();
            } else if (character == 'I') {
//            f = createImageFile(character, pill_no);
                f = File.createTempFile(
                        imageNm,  /* prefix */
                        ".jpg",         /* suffix */
                        cameraSampleFolder      /* directory */
                );
                mCurrentPhotoPath_I = f.getAbsolutePath();
            } else if (character == 'O') {
//            f = createImageFile(character, pill_no);
                f = File.createTempFile(
                        imageNm,  /* prefix */
                        ".jpg",         /* suffix */
                        cameraSampleFolder      /* directory */
                );
                mCurrentPhotoPath_O = f.getAbsolutePath();
            } else if (character == 'T') {
//            f = createImageFile(character, pill_no);
                f = File.createTempFile(
                        imageNm,  /* prefix */
                        ".jpg",         /* suffix */
                        cameraSampleFolder      /* directory */
                );
                mCurrentPhotoPath_T = f.getAbsolutePath();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return f;
    }

    private File createImageFile(Character character, String pill_no) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_" + sharefb + "_" + character + "_" + pill_no + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName + sharefb, PNG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private File getAlbumDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private String getAlbumName() {
        return "CameraSample";
    }

    //activity start result for camera new
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        // All required changes were successfully made
                        // requestLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }
//                break;
            case ACTION_TAKE_PHOTO_B: {
                if (resultCode == RESULT_OK) {
                    if (pic_status == 'F') {
                        handleBigCameraPhoto(pic_status);
                    } else if (pic_status == 'B') {
                        handleBigCameraPhoto(pic_status);
                    } else if (pic_status == 'I') {
                        handleBigCameraPhoto(pic_status);
                    } else if (pic_status == 'O') {
                        handleBigCameraPhoto(pic_status);
                    } else if (pic_status == 'T') {
                        handleBigCameraPhoto(pic_status);
                    }

                }
                break;
            } // ACTION_TAKE_PHOTO_B
            case ACTION_TAKE_GALLERY_PIC: {
                if (requestCode == SELECT_FILE) {
                    if (pic_status == 'F') {
                        Uri selectedImageUri = data.getData();
                        if (setpicforward.getDrawable() == null) {
                            setpicforward.setImageURI(selectedImageUri);
                        } else {
                            setpicforward.setImageURI(selectedImageUri);
                            // Toast.makeText(HomeScreen.this, "Cannot capture more photos", Toast.LENGTH_SHORT).show();
                        }
                        mCurrentPhotoPath_F = selectedImageUri.toString();
                        imagepath1_F = mCurrentPhotoPath_F;
                        clickedStatus = 1;
                        if (Build.VERSION.SDK_INT >= 19) {
                            mCurrentPhotoPath_F = UtilityGetPath.getRealPathFromURI_API19(getApplicationContext(), selectedImageUri);
                            imgValue = "captured";
                        } else {
                            mCurrentPhotoPath_F = UtilityGetPath.getRealPathFromURI_API11to18(getApplicationContext(), selectedImageUri);
                            imgValue = "captured";
                        }
                    } else if (pic_status == 'B') {
                        Uri selectedImageUri = data.getData();
                        if (setpicbackward.getDrawable() == null) {
                            setpicbackward.setImageURI(selectedImageUri);
                        } else {
                            setpicbackward.setImageURI(selectedImageUri);
                            // Toast.makeText(HomeScreen.this, "Cannot capture more photos", Toast.LENGTH_SHORT).show();
                        }
                        mCurrentPhotoPath_B = selectedImageUri.toString();
                        imagepath1_B = mCurrentPhotoPath_B;
                        clickedStatus = 1;
                        if (Build.VERSION.SDK_INT >= 19) {
                            mCurrentPhotoPath_B = UtilityGetPath.getRealPathFromURI_API19(getApplicationContext(), selectedImageUri);
                            imgValue = "captured";
                        } else {
                            mCurrentPhotoPath_B = UtilityGetPath.getRealPathFromURI_API11to18(getApplicationContext(), selectedImageUri);
                            imgValue = "captured";
                        }
                    } else if (pic_status == 'I') {
                        Uri selectedImageUri = data.getData();
                        if (setpicbackward.getDrawable() == null) {
                            setpicbackward.setImageURI(selectedImageUri);
                        } else {
                            setpicbackward.setImageURI(selectedImageUri);
                            // Toast.makeText(HomeScreen.this, "Cannot capture more photos", Toast.LENGTH_SHORT).show();
                        }
                        mCurrentPhotoPath_I = selectedImageUri.toString();
                        imagepath1_I = mCurrentPhotoPath_I;
                        clickedStatus = 1;
                        if (Build.VERSION.SDK_INT >= 19) {
                            mCurrentPhotoPath_I = UtilityGetPath.getRealPathFromURI_API19(getApplicationContext(), selectedImageUri);
                            imgValue = "captured";
                        } else {
                            mCurrentPhotoPath_I = UtilityGetPath.getRealPathFromURI_API11to18(getApplicationContext(), selectedImageUri);
                            imgValue = "captured";
                        }
                    } else if (pic_status == 'O') {
                        Uri selectedImageUri = data.getData();
                        if (setpicbackward.getDrawable() == null) {
                            setpicbackward.setImageURI(selectedImageUri);
                        } else {
                            setpicbackward.setImageURI(selectedImageUri);
                            // Toast.makeText(HomeScreen.this, "Cannot capture more photos", Toast.LENGTH_SHORT).show();
                        }
                        mCurrentPhotoPath_O = selectedImageUri.toString();
                        imagepath1_O = mCurrentPhotoPath_O;
                        clickedStatus = 1;
                        if (Build.VERSION.SDK_INT >= 19) {
                            mCurrentPhotoPath_O = UtilityGetPath.getRealPathFromURI_API19(getApplicationContext(), selectedImageUri);
                            imgValue = "captured";
                        } else {
                            mCurrentPhotoPath_O = UtilityGetPath.getRealPathFromURI_API11to18(getApplicationContext(), selectedImageUri);
                            imgValue = "captured";
                        }
                    } else if (pic_status == 'T') {
                        Uri selectedImageUri = data.getData();
                        if (setpicbackward.getDrawable() == null) {
                            setpicbackward.setImageURI(selectedImageUri);
                        } else {
                            setpicbackward.setImageURI(selectedImageUri);
                            // Toast.makeText(HomeScreen.this, "Cannot capture more photos", Toast.LENGTH_SHORT).show();
                        }
                        mCurrentPhotoPath_T = selectedImageUri.toString();
                        imagepath1_T = mCurrentPhotoPath_T;
                        clickedStatus = 1;
                        if (Build.VERSION.SDK_INT >= 19) {
                            mCurrentPhotoPath_T = UtilityGetPath.getRealPathFromURI_API19(getApplicationContext(), selectedImageUri);
                            imgValue = "captured";
                        } else {
                            mCurrentPhotoPath_T = UtilityGetPath.getRealPathFromURI_API11to18(getApplicationContext(), selectedImageUri);
                            imgValue = "captured";
                        }
                    }
                }
            }
        } // switch
    }

    private void handleBigCameraPhoto(Character character) {
        if (character == 'F') {
            if (mCurrentPhotoPath_F != null) {
                String path = compressImage(mCurrentPhotoPath_F, character);
                galleryAddPic(path);
            }
        } else if (character == 'B') {
            if (mCurrentPhotoPath_B != null) {
                String path = compressImage(mCurrentPhotoPath_B, character);
                galleryAddPic(path);
            }
        } else if (character == 'I') {
            if (mCurrentPhotoPath_I != null) {
                String path = compressImage(mCurrentPhotoPath_I, character);
                galleryAddPic(path);
            }
        } else if (character == 'O') {
            if (mCurrentPhotoPath_O != null) {
                String path = compressImage(mCurrentPhotoPath_O, character);
                galleryAddPic(path);
            }
        } else if (character == 'T') {
            if (mCurrentPhotoPath_T != null) {
                String path = compressImage(mCurrentPhotoPath_T, character);
                galleryAddPic(path);
            }
        }
        /*if (mCurrentPhotoPath != null) {
            String path = compressImage(mCurrentPhotoPath);
            galleryAddPic(path);
        }*/

    }

    public String compressImage(String imageUri, Character character) {
        String filename = "";
        if (character == 'F') {
            imagepath1_F = imageUri;
            String filePath = getRealPathFromURI(imageUri);
            Bitmap scaledBitmap = null;

            BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

            float maxHeight = 816.0f;
            float maxWidth = 612.0f;
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;

                }
            }

//      setting inSampleSize value allows to load a scaled down version of the original image

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            try {
//          load the bitmap from its path
                bmp = BitmapFactory.decodeFile(filePath, options);
                if (setpicforward.getDrawable() == null) {
                    setpicforward.setImageBitmap(bmp);
                } else {
                    setpicforward.setImageBitmap(bmp);
                }
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();

            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
            ExifInterface exif;
            try {
                exif = new ExifInterface(filePath);

                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 0);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                    Log.d("EXIF", "Exif: " + orientation);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                        true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream out = null;
            filename = getFilename(character);
            try {
                out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else if (character == 'B') {
            imagepath1_B = imageUri;
            String filePath = getRealPathFromURI(imageUri);
            Bitmap scaledBitmap = null;

            BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

            float maxHeight = 816.0f;
            float maxWidth = 612.0f;
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;

                }
            }

//      setting inSampleSize value allows to load a scaled down version of the original image

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            try {
//          load the bitmap from its path
                bmp = BitmapFactory.decodeFile(filePath, options);
                if (setpicbackward.getDrawable() == null) {
                    setpicbackward.setImageBitmap(bmp);
                } else {
                    setpicbackward.setImageBitmap(bmp);
                }
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();

            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
            ExifInterface exif;
            try {
                exif = new ExifInterface(filePath);

                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 0);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                    Log.d("EXIF", "Exif: " + orientation);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                        true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream out = null;
            filename = getFilename(character);
            try {
                out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (character == 'I') {
            imagepath1_I = imageUri;
            String filePath = getRealPathFromURI(imageUri);
            Bitmap scaledBitmap = null;

            BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

            float maxHeight = 816.0f;
            float maxWidth = 612.0f;
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;

                }
            }

//      setting inSampleSize value allows to load a scaled down version of the original image

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            try {
//          load the bitmap from its path
                bmp = BitmapFactory.decodeFile(filePath, options);
                if (setpicinward.getDrawable() == null) {
                    setpicinward.setImageBitmap(bmp);
                } else {
                    setpicinward.setImageBitmap(bmp);
                }
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();

            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
            ExifInterface exif;
            try {
                exif = new ExifInterface(filePath);

                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 0);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                    Log.d("EXIF", "Exif: " + orientation);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                        true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream out = null;
            filename = getFilename(character);
            try {
                out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else if (character == 'O') {
            imagepath1_O = imageUri;
            String filePath = getRealPathFromURI(imageUri);
            Bitmap scaledBitmap = null;

            BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

            float maxHeight = 816.0f;
            float maxWidth = 612.0f;
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;

                }
            }

//      setting inSampleSize value allows to load a scaled down version of the original image

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            try {
//          load the bitmap from its path
                bmp = BitmapFactory.decodeFile(filePath, options);
                if (setpicoutward.getDrawable() == null) {
                    setpicoutward.setImageBitmap(bmp);
                } else {
                    setpicoutward.setImageBitmap(bmp);
                }
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();

            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
            ExifInterface exif;
            try {
                exif = new ExifInterface(filePath);

                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 0);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                    Log.d("EXIF", "Exif: " + orientation);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                        true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream out = null;
            filename = getFilename(character);
            try {
                out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else if (character == 'T') {
            imagepath1_T = imageUri;
            String filePath = getRealPathFromURI(imageUri);
            Bitmap scaledBitmap = null;

            BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

            float maxHeight = 816.0f;
            float maxWidth = 612.0f;
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;

                }
            }

//      setting inSampleSize value allows to load a scaled down version of the original image

            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            try {
//          load the bitmap from its path
                bmp = BitmapFactory.decodeFile(filePath, options);
                if (setpictop.getDrawable() == null) {
                    setpictop.setImageBitmap(bmp);
                } else {
                    setpictop.setImageBitmap(bmp);
                }
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();

            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
            ExifInterface exif;
            try {
                exif = new ExifInterface(filePath);

                int orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 0);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                    Log.d("EXIF", "Exif: " + orientation);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                        true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream out = null;
            filename = getFilename(character);
            try {
                out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
        return filename;
    }

    public String getFilename(Character character) {
//        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
//        if (!file.exists()) {
//            file.mkdirs();
//        }
        String uriSting = "";
        if (character == 'F') {
            uriSting = mCurrentPhotoPath_F;

        } else if (character == 'B') {
            uriSting = mCurrentPhotoPath_B;

        } else if (character == 'I') {
            uriSting = mCurrentPhotoPath_I;

        } else if (character == 'O') {
            uriSting = mCurrentPhotoPath_O;

        } else if (character == 'T') {
            uriSting = mCurrentPhotoPath_T;

        }
        return uriSting;
    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private void galleryAddPic(String path) {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        //  mCurrentPhotoPath=Utility.getByeArr(Utility.setPic(mCurrentPhotoPath));
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getApplicationContext().sendBroadcast(mediaScanIntent);
    }

//    private void openBackCamera(String imageType) {
//        try {
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            if(Build.VERSION.SDK_INT >= 29) {
//                //only api 29 above
//                // Create an image file name
//                String imageFileName = "JPEG_" + timeStamp + "_";
//                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//                File file = File.createTempFile(
//                        imageFileName,  /* prefix */
//                        ".jpg",         /* suffix */
//                        storageDir      /* directory */
//                );
//
//                // Save a file: path for use with ACTION_VIEW intents
//                mCurrentPhotoPath = file.getAbsolutePath();
//
//                Uri outputFileUri = FileProvider.getUriForFile(DGPSDataCollectActivity.this,
//                        getApplicationContext().getPackageName() + ".provider", file);
//
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
//                //COMPATIBILITY
//                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP) {
//                    cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                } else {
//                    List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
//                    for (ResolveInfo resolveInfo : resInfoList) {
//                        String packageName = resolveInfo.activityInfo.packageName;
//                        grantUriPermission(packageName, outputFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    }
//                }
//                startActivityForResult(cameraIntent, REQUEST_CAMERA);
//
//
//            }else{
//                //only api 29 down
//                String imageFileName = timeStamp + ".jpg";
//
//                String rootPath = Environment.getExternalStorageDirectory()
//                        .getAbsolutePath() + "/WildLifeAppImages/";
//
//                File root = new File(rootPath);
//                if (!root.exists()) {
//                    root.mkdirs();
//                }
//
//                mCurrentPhotoPath=  rootPath + imageFileName;
//                File file = new File(mCurrentPhotoPath);
//                Uri outputFileUri = FileProvider.getUriForFile(DGPSDataCollectActivity.this,
//                        getApplicationContext().getPackageName() + ".provider", file);
//
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
//                //COMPATIBILITY
//                if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP) {
//                    cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                } else {
//                    List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
//                    for (ResolveInfo resolveInfo : resInfoList) {
//                        String packageName = resolveInfo.activityInfo.packageName;
//                        grantUriPermission(packageName, outputFileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    }
//                }
//                startActivityForResult(cameraIntent, REQUEST_CAMERA);
//            }
//
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }


}
