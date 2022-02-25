package com.sparc.frjvcapp.Viewholders;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sparc.frjvcapp.R;

public class DGPSDataViewHolder extends RecyclerView.ViewHolder {
    public TextView txtPillarNo;
    public TextView txtjobid;
    public ImageView DeleteImg;
    public ImageView PillarImg;

    public DGPSDataViewHolder(@NonNull View itemView) {
        super(itemView);
        txtPillarNo= itemView.findViewById(R.id.pillarNo);
        txtjobid= itemView.findViewById(R.id.jobid);
        PillarImg= itemView.findViewById(R.id.pillarImg);
        DeleteImg= itemView.findViewById(R.id.delete);

    }
}
