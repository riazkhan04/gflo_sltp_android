package com.sparc.frjvcapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sparc.frjvcapp.Adapter.ResurveyAdapter;
import com.sparc.frjvcapp.pojo.ResurveyModel;

import java.util.ArrayList;

public class Resurvey extends AppCompatActivity {
    private SQLiteDatabase db;
    public static final String data = "data";
    SharedPreferences shared;
    String sharefb = "";
    TextView fbname, notreq;
    LinearLayout header;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    public ResurveyAdapter adapter;
    private ArrayList<ResurveyModel> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resurvey);
        fbname = findViewById(R.id.dgpsfbName);
        notreq = findViewById(R.id.notreq);
        header = findViewById(R.id.header);
        shared = getApplicationContext().getSharedPreferences(data, MODE_PRIVATE);
        sharefb = shared.getString("fbcode", "0");
        fbname.setText(shared.getString("fbname", "0"));
        String resurveyPill = "";
        int count = 0;
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        arrayList = new ArrayList<ResurveyModel>();
        adapter = new ResurveyAdapter(getApplicationContext(), arrayList);
        db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
        arrayList.clear();

        //Cursor cursor8 = db.rawQuery("select * from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "' and sync_status!=2 and ((pill_solution_type='RTX' and (pill_hor_precision>0.75 or pill_verti_precision>0.85)) or ((pill_solution_type!='RTX' and pill_solution_type!='') and rtx_survey_min<15 ) or (pill_solution_type='' and pill_hor_precision='' and pill_verti_precision=''))", null);
        Cursor cursor8 = db.rawQuery("select * from m_fb_dgps_survey_pill_data where fb_id='" + sharefb + "' and sync_status!=2 and pill_solution_type is NULL", null);
        cursor8.moveToFirst();
        if (cursor8.moveToFirst()) {

            if (cursor8.getCount() > 0) {
                do {
                    ResurveyModel resurveyModel = new ResurveyModel();
                    if (cursor8.getString(cursor8.getColumnIndex("pndjv_pill_no")).equals("0")) {
                        resurveyModel.setPillNo(cursor8.getString(cursor8.getColumnIndex("pill_no")));
                        resurveyModel.setReason("RTX value Missing");
                    } else {
                        resurveyModel.setPillNo(cursor8.getString(cursor8.getColumnIndex("pill_no")) + "_" + cursor8.getString(cursor8.getColumnIndex("pndjv_pill_no")));
                        resurveyModel.setReason("RTX value Missing");
                    }
                    /*if (count == 0) {
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
                    count++;*/
                    arrayList.add(resurveyModel);
                }
                while (cursor8.moveToNext());
                /* pillNo.setText("Required Re-Survey : " + resurveyPill + "");
                 */
            }
        } else {
            notreq.setVisibility(View.VISIBLE);
            header.setVisibility(View.INVISIBLE);
            notreq.setText("*** Re-Survey not Required ***");
        }
        cursor8.close();
        db.close();

        adapter = new ResurveyAdapter(this, arrayList);
        recyclerView.setAdapter(adapter);
    }
}