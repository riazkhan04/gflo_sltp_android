package com.sparc.frjvcapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;

import com.sparc.frjvcapp.Adapter.HelpAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Help extends AppCompatActivity {
    //Todo declaration of XML views
    private RelativeLayout tabbar, rel_main;
    private ExpandableListView getting_strated_list, getting_strated_list1;

    private List<String> listDataHeader, listDataHeader1;
    private HashMap<String, List<String>> listDataChild, listDataChild1;
    private HelpAdapter helpAdapter;
    private int lastExpandedPosition = -1;
    private int lastExpandedPosition1 = -1;
    public static final String data = "data";
    String fb_id, fb_name, utmZone;
    SQLiteDatabase db;
    Button phtBackup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            phtBackup = findViewById(R.id.phtBackup);
            setContentView(R.layout.activity_help);

            init();

            set_list_dat();
            set_list_dat1();
            phtBackup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Todo methods to initialize XML views
    private void init() {
        try {

            getting_strated_list = findViewById(R.id.faqlist);
            getting_strated_list1 = findViewById(R.id.faqlist1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Todo set dat in expandable list
    private void set_list_dat() {

        prepareListData();

        helpAdapter = new HelpAdapter(Help.this, listDataHeader, listDataChild);

        // setting list adapter
        getting_strated_list.setAdapter(helpAdapter);

        getting_strated_list.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {


                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    getting_strated_list.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;

            }
        });

    }

    //Todo set data in arraylist
    private void prepareListData() {

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        listDataHeader.add("Steps of DGPS Survey");
        listDataHeader.add("How to take DGPS survey using Mobile App");
        listDataHeader.add("Data Tagging Issue");
        listDataHeader.add("Data Synchronization issues");
        listDataHeader.add("How to recover surveyed data");

        // Adding child data

        List<String> sub_question1 = new ArrayList<String>();
        sub_question1.add("Steps of DGPS Survey");
        sub_question1.add("Steps of DGPS Survey");
        sub_question1.add("Steps of DGPS Survey");
        sub_question1.add("Steps of DGPS Survey");
        sub_question1.add("Steps of DGPS Survey");

        List<String> sub_question2 = new ArrayList<String>();
        sub_question2.add("How to take DGPS survey using Mobile App");
        sub_question2.add("How to take DGPS survey using Mobile App");
        sub_question2.add("How to take DGPS survey using Mobile App");
        sub_question2.add("How to take DGPS survey using Mobile App");
        sub_question2.add("How to take DGPS survey using Mobile App");

        List<String> sub_question3 = new ArrayList<String>();
        sub_question3.add("Data Tagging Issue");
        sub_question3.add("Data Tagging Issue");
        sub_question3.add("Data Tagging Issue");

        List<String> sub_question4 = new ArrayList<String>();
        sub_question4.add("Data Synchronization issues");
        sub_question4.add("Data Synchronization issues");
        sub_question4.add("Data Synchronization issues");

        List<String> sub_question5 = new ArrayList<String>();
        sub_question5.add("How to recover surveyed data");
        sub_question5.add("How to recover surveyed data");
        sub_question5.add("How to recover surveyed data");


        listDataChild.put(listDataHeader.get(0), sub_question1); // Header, Child data
        listDataChild.put(listDataHeader.get(1), sub_question2);
        listDataChild.put(listDataHeader.get(2), sub_question3);
        listDataChild.put(listDataHeader.get(3), sub_question4);
        listDataChild.put(listDataHeader.get(4), sub_question5);


    }

    //Todo set data in arraylist
    private void prepareListData1() {
        SharedPreferences shared = getApplicationContext().getSharedPreferences(data, MODE_PRIVATE);
        fb_id = shared.getString("fbcode", "0");
        fb_name = shared.getString("fb_name", "0");

        try {
            db = openOrCreateDatabase("sltp.db", MODE_PRIVATE, null);
            Cursor cursor = db.rawQuery("select substr(((min(m_p_long)+max(m_p_long))/2),0,3) as result from m_dgps_Survey_pill_data where m_fb_id='" + fb_id + "'", null);
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


        listDataHeader1 = new ArrayList<String>();
        listDataChild1 = new HashMap<String, List<String>>();

        listDataHeader1.add("Know Your UTM/Zone");
        List<String> sub_question1 = new ArrayList<String>();
        sub_question1.add(fb_name + " : " + utmZone);


        listDataChild1.put(listDataHeader1.get(0), sub_question1); // Header, Child data

    }

    //Todo set dat in expandable list
    private void set_list_dat1() {

        prepareListData1();

        helpAdapter = new HelpAdapter(Help.this, listDataHeader1, listDataChild1);

        // setting list adapter
        getting_strated_list1.setAdapter(helpAdapter);

        getting_strated_list1.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {


                if (lastExpandedPosition1 != -1
                        && groupPosition != lastExpandedPosition1) {
                    getting_strated_list.collapseGroup(lastExpandedPosition1);
                }
                lastExpandedPosition1 = groupPosition;

            }
        });

    }
}
