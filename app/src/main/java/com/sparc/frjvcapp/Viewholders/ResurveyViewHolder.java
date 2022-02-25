package com.sparc.frjvcapp.Viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sparc.frjvcapp.R;

public class ResurveyViewHolder extends RecyclerView.ViewHolder {
    public TextView pillNo, reason;

    public ResurveyViewHolder(@NonNull View itemView) {
        super(itemView);
        pillNo = itemView.findViewById(R.id.pillNo);
        reason = itemView.findViewById(R.id.resurvReason);
    }
}
