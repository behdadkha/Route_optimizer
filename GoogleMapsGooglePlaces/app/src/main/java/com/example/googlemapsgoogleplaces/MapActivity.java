package com.example.googlemapsgoogleplaces;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import static android.graphics.Color.WHITE;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,LocationListener {



    LocationManager locationManager;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;


        if (mLocationPermissionGranted) {
            getLocation();
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "not granted", Toast.LENGTH_SHORT).show();
            return;

        }

        mMap.setMyLocationEnabled(true);


    }


    //widgets
    private AutoCompleteTextView destination;
    ListView listView;
    private AutocompleteSupportFragment autocompleteFragment;

    //
    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String Course_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_CODE = 1234;
    private static final float DEFAULT_ZOOM = 12f;

    //vars
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private LatLng currentLatLng;
    private ArrayAdapter arrayAdapter;
    public ArrayAdapter orderedArrayAdapter;
    private ArrayList<String> arrayList;
    private int numberOfSelected;
    private ArrayList<String> deletedItems;
    private ArrayList<Marker> deletedItemsMarker;
    public static ArrayList<String> orderedAdd;
    public static String currentCityCountry;
    private AlertDialog alertDialog;
    private List<Marker> markerList;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        final Button optimize = findViewById(R.id.optimize);
        final Button remove = findViewById(R.id.delete);
        final TextView notifyTextView = findViewById(R.id.textView3);


        getLocationPermission();

        listView = findViewById(R.id.listview);
        arrayList = new ArrayList<>();
        deletedItems = new ArrayList<>();
        markerList = new ArrayList<>();
        deletedItemsMarker = new ArrayList<>();


        final Button getPrevDataBtn = findViewById(R.id.getPrevDataBtn);
        if(readFile().size() > 0){
            getPrevDataBtn.setVisibility(View.VISIBLE);
            getPrevDataBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(readFile().size() > 0){
                        arrayList = (ArrayList<String>) readFile().clone();
                        arrayAdapter = new ArrayAdapter(MapActivity.this, android.R.layout.simple_list_item_multiple_choice, arrayList);
                        arrayAdapter.notifyDataSetChanged();
                        listView.setAdapter(arrayAdapter);
                        for(int i = 0; i < arrayList.size(); i++){
                            oldDataMarker(geoLocateReturnLatLng(arrayList.get(i)), arrayList.get(i));
                        }
                        getPrevDataBtn.setVisibility(View.INVISIBLE);
                        optimize.setVisibility(View.VISIBLE);
                    }
                }
            });
        }


        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, arrayList);

        // Initialize the SDK
        Places.initialize(getApplicationContext(), "");

        // Create a new Places client instance
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.NAME));
        //

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                getPrevDataBtn.setVisibility(View.INVISIBLE);
                arrayList.add(place.getName());
                listView.setAdapter(arrayAdapter);
                geoLocate(place.getName());
                if (arrayList.size() > 1) {
                    optimize.setVisibility(View.VISIBLE);
                    notifyTextView.setVisibility(View.INVISIBLE);
                }
                if (arrayList.size() > 3) {
                    notifyTextView.setText("Scroll down to see more");
                    notifyTextView.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });





        optimize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new LongOperation().execute();

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                geoLocateNoMarker(arrayList.get(position));
                return false;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                numberOfSelected = listView.getCount();
                SparseBooleanArray checked = listView.getCheckedItemPositions();

                if (numberOfSelected > 0) {
                    for (int i = 0; i < arrayList.size(); i++) {
                        if(checked.get(i)){
                            remove.setVisibility(View.VISIBLE);
                            return;
                        }
                        remove.setVisibility(View.INVISIBLE);
                    }


                }


            }

        });

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberOfSelected = listView.getCount();
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                for (int i = 0; i < arrayList.size(); i++) {
                    if (checked.get(i)) {
                        deletedItems.add(arrayList.get(i));
                        deletedItemsMarker.add(markerList.get(i));

                        listView.setItemChecked(i, false);
                        listView.setSelection(i);
                    }
                }

                for (int i = 0; i < deletedItems.size(); i++) {
                    toast("Removed");
                    deletedItemsMarker.get(i).remove();
                    markerList.remove(deletedItemsMarker.get(i));
                    arrayList.remove(deletedItems.get(i));

                }
                deletedItemsMarker.clear();
                deletedItems.clear();
                arrayAdapter.notifyDataSetChanged();
                if(arrayList.size() < 1){
                    optimize.setVisibility(View.INVISIBLE);
                }
                remove.setVisibility(View.INVISIBLE);
            }
        });

        EditText editPlace = autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input);
        editPlace.setBackgroundColor(WHITE);


    }


    private class LongOperation extends AsyncTask<Void,Void,Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapActivity.this);
            mBuilder.setCancelable(false);
            View view = getLayoutInflater().inflate(R.layout.loading_layout,null);
            mBuilder.setView(view);
            alertDialog = mBuilder.create();
            alertDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            alertDialog.cancel();
            Intent intent = new Intent(MapActivity.this, orderedlist.class);
            startActivity(intent);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            orderedAdd = new ArrayList<>();
            float closestDistH = 1000000;
            int closesPosH = 0;
            for (int i = 0; i < arrayList.size(); i++) {
                float dist = findDistanceHome(arrayList.get(i));
                if (dist < closestDistH) {
                    closestDistH = dist;
                    closesPosH = arrayList.indexOf(arrayList.get(i));
                }
            }
            String closest = arrayList.get(closesPosH);
            orderedAdd.add(closest);//add the closest to current location
            //
            ArrayList<String> addresses = (ArrayList<String>) arrayList.clone();
            addresses.remove(closesPosH);

            while (addresses.size() != 0) {
                float closestDist = 1000000;
                int closestPos = 0;
                for (int i = 0; i < addresses.size(); i++) {
                    float distance = findDistance(closest, addresses.get(i));
                    if (distance < closestDist) {
                        closestDist = distance;
                        closestPos = addresses.indexOf(addresses.get(i));
                    }
                }
                closest = addresses.get(closestPos);
                orderedAdd.add(closest);
                addresses.remove(closestPos);
            }
            return null;
        }

    }

    private void toast(String givenText) {
        Toast.makeText(this, givenText, Toast.LENGTH_LONG).show();
    }

    private void geoLocate(String searchString) {

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try {

            list = geocoder.getFromLocationName(searchString+currentCityCountry, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException");
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM
                    , address.getAddressLine(0));

        }

    }

    private void geoLocateNoMarker(String searchString) {

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString+currentCityCountry, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException");
        }

        if (list.size() > 0) {
            Address address = list.get(0);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude())
                    , DEFAULT_ZOOM));

        }

    }
    private LatLng geoLocateReturnLatLng(String searchString) {

        Geocoder geocoder = new Geocoder(MapActivity.this);
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

    private float findDistanceHome(String add) {
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();

        try {
            list = geocoder.getFromLocationName(add+currentCityCountry, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException");
        }

        if (list.size() > 0) {

            Address address = list.get(0);

            float results[] = new float[10];
            Location.distanceBetween(currentLatLng.latitude, currentLatLng.longitude, address.getLatitude(), address.getLongitude(), results);
            return results[0];
        }
        return 0;
    }

    private float findDistance(String closest, String add) {
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list1 = new ArrayList<>();
        List<Address> list2 = new ArrayList<>();

        try {
            list1 = geocoder.getFromLocationName(closest+currentCityCountry, 1);
            list2 = geocoder.getFromLocationName(add+currentCityCountry, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException");
        }

        if (list1.size() > 0) {

            Address address1 = list1.get(0);
            Address address2 = list2.get(0);

            float results[] = new float[10];
            Location.distanceBetween(address1.getLatitude(), address1.getLongitude(), address2.getLatitude(), address2.getLongitude(), results);
            return results[0];
        }
        return 0;
    }


    void getLocation(){
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }


    private void moveCamera(LatLng latLng, float zoom, String title){

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(title != "Your location"){

            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            Marker marker = mMap.addMarker(options);
            markerList.add(marker);

        }

        Toast.makeText(this, title, Toast.LENGTH_SHORT).show();


        hideSoftKeyboard();

    }
    private void oldDataMarker(LatLng latLng, String title){
        MarkerOptions options = new MarkerOptions().position(latLng).title(title);
        Marker marker = mMap.addMarker(options);
        markerList.add(marker);
    }
    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Course_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed.");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }
        }


    }
    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    private String findCountry(){
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();

        try{
            list = geocoder.getFromLocation(currentLatLng.latitude, currentLatLng.longitude, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException");
        }

        if (list.size() > 0){
            Address address = list.get(0);
            return address.getCountryCode();
        }

        return "USA";

    }


    private String findCurrentCityCountry(){//returns city,country
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();

        try{
            list = geocoder.getFromLocation(currentLatLng.latitude, currentLatLng.longitude, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException");
        }

        if (list.size() > 0){
            Address address = list.get(0);
            return (address.getLocality()+ "," +address.getCountryName());
        }

        return "Toronto,Canada";//if nothing found

    }
    public ArrayList<String> readFile(){
        String fileName = "orderedAdd.txt";
        File cacheDirec = getBaseContext().getCacheDir();
        File tempFile = new File(cacheDirec.getPath() + "/" + fileName);

        String strLine = "";
        StringBuilder text = new StringBuilder();
        try {
            FileReader fReader = new FileReader(tempFile);
            BufferedReader bReader = new BufferedReader(fReader);

            /** Reading the contents of the file , line by line */
            while( (strLine=bReader.readLine()) != null  ){
                text.append(strLine+"\n");
            }
            fReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }

        String dataString = text.toString();
        String[] data = dataString.split("/");
        ArrayList<String> oldData = new ArrayList<>();
        for (int i = 0; i < data.length-1; i++){
            oldData.add(data[i]);
        }


        return oldData;
    }

    @Override
    public void onLocationChanged(Location location) {
        moveCamera(new LatLng(location.getLatitude(),location.getLongitude()),DEFAULT_ZOOM,"Your location");
        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                .title("Your location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        currentLatLng = new LatLng(location.getLatitude(),location.getLongitude());
        autocompleteFragment.setCountry(findCountry());
        currentCityCountry = findCurrentCityCountry();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setMessage("Your location services is disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MapActivity.this.finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
