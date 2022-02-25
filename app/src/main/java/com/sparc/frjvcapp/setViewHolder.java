package com.sparc.frjvcapp;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class setViewHolder  extends RecyclerView.ViewHolder  {
    public TextView txtPillarNo;
    public TextView txtPillarLat;
    public TextView txtPillarLon;
    public ImageView PillarImg;
    public ImageView DeleteImg;

    public setViewHolder(@NonNull View itemView) {
        super(itemView);
        txtPillarNo= itemView.findViewById(R.id.pillarNo);
        txtPillarLat= itemView.findViewById(R.id.pillarLat);
        txtPillarLon= itemView.findViewById(R.id.pillarLon);
        PillarImg= itemView.findViewById(R.id.pillarImg);
        DeleteImg= itemView.findViewById(R.id.delete);

    }
}
