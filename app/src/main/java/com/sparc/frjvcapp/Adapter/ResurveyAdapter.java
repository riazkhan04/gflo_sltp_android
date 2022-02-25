package com.sparc.frjvcapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sparc.frjvcapp.OnTapListener;
import com.sparc.frjvcapp.R;
import com.sparc.frjvcapp.Viewholders.DGPSDataViewHolder;
import com.sparc.frjvcapp.Viewholders.ResurveyViewHolder;
import com.sparc.frjvcapp.pojo.ResurveyModel;

import java.util.List;

public class ResurveyAdapter extends RecyclerView.Adapter<ResurveyViewHolder> {
    List<ResurveyModel> dataitems;
    private Context context;
    private OnTapListener onTapListener;

    public ResurveyAdapter(Context ctx, List<ResurveyModel> dataitems) {
        this.context = ctx;
        this.dataitems = dataitems;
    }

    @NonNull
    @Override
    public ResurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.resurvey_cardview, parent, false);
        return new ResurveyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ResurveyViewHolder holder, int position) {
        holder.pillNo.setText(dataitems.get(holder.getAdapterPosition()).getPillNo());
        holder.reason.setText(dataitems.get(holder.getAdapterPosition()).getReason());

    }

    @Override
    public int getItemCount() {
        return dataitems.size();
    }
}
