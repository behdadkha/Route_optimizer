package com.example.googlemapsgoogleplaces;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.googlemapsgoogleplaces.MapActivity.currentCityCountry;
import static com.example.googlemapsgoogleplaces.MapActivity.orderedAdd;

public class orderedlist extends AppCompatActivity {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orderedlist);

        final TextView textView = findViewById(R.id.textView4);
        ListView listView = findViewById(R.id.listview);

        final ArrayList<Address> addresslist = new ArrayList<>();

        for (int i = 0; i < orderedAdd.size(); i++){
            Address address = new Address(orderedAdd.get(i), false);
            addresslist.add(address);
        }

        generateFile();

        final CustomAdapter CustomAdapter = new CustomAdapter(this, R.layout.listview_layout, addresslist);
        listView.setAdapter(CustomAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {


                textView.setVisibility(View.INVISIBLE);


                addresslist.get(position).setDone(true);
                CustomAdapter.notifyDataSetChanged();

                // Create a Uri from an intent string. Use the result to create an Intent.
                LatLng location_latlng = geoLocate(orderedAdd.get(position));
                String parseString = "google.navigation:q=" + location_latlng.latitude + "," + location_latlng.longitude;
                Uri gmmIntentUri = Uri.parse(parseString);


                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");

                // Attempt to start an activity that can handle the Intent
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);


                }
            }
        });
    }


    private LatLng geoLocate(String searchString) {

        Geocoder geocoder = new Geocoder(orderedlist.this);
        List<android.location.Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString+currentCityCountry, 1);
        } catch (IOException e) {

        }

        if (list.size() > 0) {
            android.location.Address address = list.get(0);

            return (new LatLng(address.getLatitude(),address.getLongitude()));

        }
        return null;
    }

    private void generateFile(){
        String fileName = "orderedAdd.txt";
        File cacheDirec = getBaseContext().getCacheDir();
        File tempFile = new File(cacheDirec.getPath() + "/" + fileName);
        //writing to the file
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(tempFile,false);
            for (int i = 0; i < orderedAdd.size(); i++){
                fileWriter.write(orderedAdd.get(i) + "/");
            }

            fileWriter.close();
        } catch (IOException e) {
        e.printStackTrace();
        }
    }



}
