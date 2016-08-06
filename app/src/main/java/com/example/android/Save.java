package com.example.android.direction;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.OutputStreamWriter;

/**
 * Created by Utilizzatore on 04/08/2016.
 */
public class Save extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;

    /*
    * Define a request code to send to Google Play services
    * This code is returned in Activity.onActivityResult
    */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*Tag variable*/
    public static final String TAG = Save.class.getSimpleName();

    private LocationRequest mLocationRequest;

    /*Variable that contain the name of the file where will be saved the position.*/
    private final static String filename_coordinate="myfile.txt";

   /* Variable that contain the current latitude and longitude*/
    private double currentLatitude;
    private double currentLongitude;

    /**
     * Represents a geographical location.
     */
    private Location location;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        buildGoogleApiClient();

        Toast.makeText(this,"Position Saved",Toast.LENGTH_SHORT).show();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);

    }


   /* Method that return to the MainActivity*/
    public void openActivity(Class<?> cs) {
        startActivity(new Intent(this, cs));
    }

    /**
     * Builds a GoogleApiClient. Uses {@code #addApi} to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


 /*   When our client finally connects to the location services, the onConnected() method will be called.*/
    @Override
    public void onConnected(Bundle bundle) {

/*        Using the Google Play services location APIs, your app can request the last known location of the user’s device.
        In most cases, you are interested in the user’s current location, which is usually equivalent to the last known
        location of the device.*/
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        /*The last location might be null if this is the first time Google Play Services is checking location.*/
        if (location == null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            handleNewLocation(location);
        }

        Log.i(TAG, "Location services connected");
    }


    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        currentLatitude = location.getLatitude();
        currentLongitude= location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude,currentLongitude);
        //Log.i("LATITUDE ",String.valueOf(currentLatitude));
        //Log.i("LONGITUDE ",String.valueOf(currentLongitude));
        saveCoordinate();
        openActivity(MainActivity.class);
    }

    protected void saveCoordinate(){
        try {
            OutputStreamWriter out = new OutputStreamWriter(openFileOutput(filename_coordinate, this.MODE_PRIVATE));

            String separator = System.getProperty("line.separator");
            out.write(String.valueOf(currentLatitude));
            out.write(separator);
            out.write(String.valueOf(currentLongitude));

            out.flush();
            out.close();

            //Log.i("COORDINATE ","SALVATE");
        }
        catch(Throwable t){
            Toast.makeText(this,"Exception " + t.toString(),Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended.");
    }

    /*onResume() is called right after onCreate(), so if we connect there then we will always be able to access location
    when our Activity is visible.*/
    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }


    /*we want to disconnect from location services when our Activity is paused.*/
    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    /*
    * Google Play services can resolve some errors it detects.
    * If the error has a resolution, try sending an Intent to
    * start a Google Play services activity that can resolve
    * error.
    */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){
        if(connectionResult.hasResolution()){
            try{
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,CONNECTION_FAILURE_RESOLUTION_REQUEST);
                 /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch(IntentSender.SendIntentException e){
                // Log the error
                e.printStackTrace();
            }
        } else {
             /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG,"Location services connection failed");
        }
    }

    @Override
    public void onLocationChanged(Location location){
        handleNewLocation(location);
    }
}
