package com.example.administrator.safetylens;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.List;

/**
 * Custom ListAdapter for pop up alert in map activity
 */
public class PolygonOptionsListAdapter extends RecyclerView.Adapter<PolygonOptionsListAdapter.MyViewHolder> {

    List<String> list;
    GoogleMap map;
    List<PolylineOptions[]> polylines;

    public PolygonOptionsListAdapter(List<String> timestamps, GoogleMap mMap, List<PolylineOptions[]> polylineOptionsList){

        list = timestamps;
        map = mMap;
        polylines = polylineOptionsList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycled_layout, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.textView.setText(list.get(position));
        /*final CheckBox checkBox = holder.checkBox;
        final int p = position;
        checkBox.setText(list.get(p));
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked())
                    for(PolylineOptions polylineOptions:polylines.get(p))
                        map.addPolyline(polylineOptions);
                else{
                    Polyline polyline;
                    for(PolylineOptions polylineOptions:polylines.get(p)) {
                        polyline = map.addPolyline(polylineOptions);
                        polyline.remove();
                    }
                }

            }
        });*/
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public MyViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.recyclerText);
        }
    }
}
