package com.sparc.frjvcapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sparc.frjvcapp.OnTapListener;
import com.sparc.frjvcapp.R;
import com.sparc.frjvcapp.pojo.DGPSRTXVIewModel;
import com.sparc.frjvcapp.setDGPSViewHolder;

import java.util.List;

public class DGPSRTXViewAdapter extends RecyclerView.Adapter<setDGPSViewHolder> {
    List<DGPSRTXVIewModel> dataitems;
    private Context context;
    private OnTapListener onTapListener;

    public DGPSRTXViewAdapter(Context ctx, List<DGPSRTXVIewModel> dataitems) {
        this.context = ctx;
        this.dataitems = dataitems;
    }


    @NonNull
    @Override
    public setDGPSViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_dgpsdata_recycle, viewGroup, false);
        return new setDGPSViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull setDGPSViewHolder holder, int i) {
        String s[]=dataitems.get(holder.getAdapterPosition()).getR_file_path().split("/");
        holder.pill_no.setText(String.valueOf(i+1));
        holder.pill_name.setText(s[s.length-1]);
        if (dataitems.get(holder.getAdapterPosition()).getR_status().equals("2")) {
            holder.pill_sts.setBackgroundResource(R.drawable.ic_sync_black_24dp);

        }else {
            holder.pill_sts.setBackgroundResource(R.drawable.ic_sync_problem_black_24dp);

        }
    }

    @Override
    public int getItemCount() {
        return dataitems.size();
    }
}