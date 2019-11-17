package com.example.googlemapsgoogleplaces;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<Address> {
    private static final String TAG = "CustomAdaptor";
    private Context mContext;
    int mResource;

    Activity activity;
    LayoutInflater inflater;
    ArrayList<String> address;


    public CustomAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Address> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String address = getItem(position).getAddress();
        Boolean isDone = getItem(position).isDone();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView textName = convertView.findViewById(R.id.textView);
        TextView textNumber = convertView.findViewById(R.id.textView2);
        ImageView imageView = convertView.findViewById(R.id.imageView);


        textName.setText(address);
        textNumber.setText((position+1) + ". ");
        if(isDone)
            imageView.setVisibility(View.VISIBLE);
        return convertView;
    }

}
