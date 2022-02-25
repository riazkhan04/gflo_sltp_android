package com.sparc.frjvcapp.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.sparc.frjvcapp.R;

import java.util.HashMap;
import java.util.List;

public class HelpAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> expandableListTitle;
    private HashMap<String, List<String>> expandableListDetail;

    public HelpAdapter(Context context, List<String> expandableListTitle,
                                 HashMap<String, List<String>> expandableListDetail) {

        try {
            this.context = context;
            this.expandableListTitle = expandableListTitle;
            this.expandableListDetail = expandableListDetail;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        try {
            final String expandedListText = (String) getChild(listPosition, expandedListPosition);

            TextView expandedListTextView = null;
            convertView = null;

            if (convertView == null) {

                LayoutInflater layoutInflater = (LayoutInflater) this.context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = layoutInflater.inflate(R.layout.list_item, null);
                expandedListTextView = convertView.findViewById(R.id.expandedListItem);
                expandedListTextView.setText(expandedListText);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {

        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {

        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {

        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {

        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        try {
            String listTitle = (String) getGroup(listPosition);

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) this.context.
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.list_group, null);
            }

            TextView listTitleTextView = convertView.findViewById(R.id.listTitle);
            ImageView image = convertView.findViewById(R.id.groupicon);

            listTitleTextView.setTypeface(null, Typeface.BOLD);
            listTitleTextView.setText(listTitle);

            int imageResourceId = isExpanded ? R.mipmap.arrow_down : R.mipmap.right_arrow;
            image.setImageResource(imageResourceId);

            image.setVisibility(View.VISIBLE);

        }catch (Exception e){
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}