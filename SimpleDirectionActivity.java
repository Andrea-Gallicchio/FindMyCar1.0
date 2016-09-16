package com.example.android.direction;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.googledirectionlibrary.DirectionCallback;
import com.example.android.googledirectionlibrary.GoogleDirection;
import com.example.android.googledirectionlibrary.constant.Language;
import com.example.android.googledirectionlibrary.constant.TransportMode;
import com.example.android.googledirectionlibrary.constant.Unit;
import com.example.android.googledirectionlibrary.model.Direction;
import com.example.android.googledirectionlibrary.model.Info;
import com.example.android.googledirectionlibrary.model.Leg;
import com.example.android.googledirectionlibrary.model.Route;
import com.example.android.googledirectionlibrary.model.Step;
import com.example.android.googledirectionlibrary.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/*This is the most important class, that calculates the road, the time, the distance, shows the current indication,
use the vibration mode, and ring the alarm if you are arrive at destination.
 */

public class SimpleDirectionActivity extends AppCompatActivity implements OnMapReadyCallback,/* View.OnClickListener,*/ DirectionCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mGoogleMap;

    //Server key
    private String serverKey = "AIzaSyDzpUBcGKhAXcPdE-HRjaZGr8xiKg55mcY";

    /*The LatLng variables contain the latitute and longitude of origin and destination*/
    private LatLng origin;
    private LatLng destination;

    private Info distanceInfo;
    private Info durationInfo;
    private String distance;
    private String duration;

    private Leg leg;

    /*TextView for the duration of the road*/
    TextView duration_text;

    /*TextView for the distance of the road*/
    TextView distance_text;

    /*TextView for the duration of the current step*/
    TextView duration_step_text;

    /*TextView for the distance of the current step*/
    TextView distance_step_text;

    /*TextView for the indication of the current step*/
    TextView indication_step_text;

    private Route route;
    //private Step step;

    private Info distanceInfo2;
    private Info durationInfo2;
    private String distance2;
    private String duration2;

    private int i = 1;

    private String instruction;
    private String instruction_save;

    private int conta_coordinate = 0;

    private boolean flag=true;

    private float[] distance_cerchio = new float[2];

    protected ArrayList<LatLng> sectionList;

    private ArrayList<LatLng> pointList;

    private boolean entra=true;

    // Get instance of Vibrator from current Context
    private Vibrator vib;

    private String mRoundabout="roundabout";
    private String mFirst="1st";
    private String mSecond="2nd";
    private String mThird="3rd";
    private String mFourth="4th";
    private String mFifth="5th";

    private String left="left";
    private String right="right";

    //Variable for the ImageButton (VibrationMode)
    private boolean flag_imageButton=true;

    //Because 1 leg can be contain with many step. So you have to retrieve the Step in array.
    private List<Step> step_list;

    /*LocationRequest objects are used to request a quality of service for location updates from the FusedLocationProviderApi.*/
    LocationRequest mLocationRequest;

    /**
     * Provides the entry point to Google Play services.
     */
    GoogleApiClient mGoogleApiClient;

    LatLng latLng;

    SupportMapFragment mFragment;

    // Instantiating CircleOptions to draw a circle around the marker
    CircleOptions circleOptions = new CircleOptions();
    private Circle myCircle;

    PolylineOptions myPolyline =new PolylineOptions();

    String mArrive="You Are Arrive";
    String mArrive_space="";

    //This is the name of the file,where are stored the coordinates of the car position
    private final static String filename_coordinate="myfile.txt";

    private int j=0;
    private double x,y;

    //ImageButton thatenabled and disable the vibration mode.
    ImageButton vib_button;

    //Variable to check if the ImageButton is was pressed or not. For default the vibration-mode is enabled.
    boolean isPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_direction);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Read the file,that is where the coordinates of the car parking are saved
        readFileInEditor();

        duration_text = (TextView) findViewById(R.id.duration);
        distance_text = (TextView) findViewById(R.id.distance);
        duration_step_text=(TextView) findViewById(R.id.duration_step);
        distance_step_text=(TextView) findViewById(R.id.distance_step);
        indication_step_text=(TextView) findViewById(R.id.indication);

        vib_button= (ImageButton)findViewById(R.id.imageButton);
        vib_button.setOnClickListener(buttonListener);

        //Initialize the vibrator service
         vib = (Vibrator) getSystemService(this.VIBRATOR_SERVICE);


        mFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mFragment.getMapAsync(this);


    }


    //Method that change the image of the imageButton, when it is clicked.
    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isPressed)
                vib_button.setImageResource(R.drawable.ic_vibration);
            else
                vib_button.setImageResource(R.drawable.ic_slashed_vibration);

            messageVibration();

        }
    };

    /*Method that show to the user a message that say if the VibrationMode is selected or not
    * and maintain tracks of it with the flag variable*/
    public void messageVibration(){

        if(isPressed && !flag_imageButton)
        {
            Toast.makeText(this,"Vibration Mode ENABLED",Toast.LENGTH_SHORT).show();
            flag_imageButton=true;
        }

        if(!isPressed && flag_imageButton)
        {
            Toast.makeText(this,"Vibration Mode DISABLED",Toast.LENGTH_SHORT).show();
            flag_imageButton=false;
        }


        isPressed = !isPressed;
    }


    //Method that reads this newly-created file and populates the text editor with the contents of the file
    public void readFileInEditor()
    {
        try {

            //Open the file with openFileInput,and then create an InputStream from it
            InputStream in = openFileInput(filename_coordinate);

            if (in != null) {

                //Create an InputStreamReader
                InputStreamReader tmp=new InputStreamReader(in);

                //Create a BufferedReader; Using the BufferedReader, we read line after line the text
                // of the storage file, and we store the text in the buffer
                BufferedReader reader=new BufferedReader(tmp);
                String str;
                StringBuilder buf=new StringBuilder();


                while ((str = reader.readLine()) != null) {

                    buf.append(str);

                    /*The method parseDouble returns a new double initialized to the value represented by the specified String*/

                    /*It was used the j variable because if j==0 i'm reading the latitude, and if j==1 i'm reading the longitude*/
                    if(j==0){
                       x =Double.parseDouble(str);
                    }
                    else
                        y=Double.parseDouble(str);
                    j++;

                }

                /*Close the file*/
                in.close();

            }
        }
        catch (java.io.FileNotFoundException e) {

            // that's OK, we probably haven't created it yet
        }
        catch (Throwable t) {
            Toast.makeText(this, "Exception: "+t.toString(), Toast.LENGTH_LONG).show();

        }

        destination=new LatLng(x,y);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mGoogleMap.setMyLocationEnabled(true);

        /*Method to configure a GoogleApiClient.*/
        buildGoogleApiClient();

        mGoogleApiClient.connect();

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


    /*This method request the direction from origin to destination, in walking mode*/
    public void requestDirection() {
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.WALKING)
                .language(Language.ENGLISH)
                .unit(Unit.METRIC)
                .execute(this);
    }



    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {

        //Add markers at the start and the end of the route
        if (direction.isOK()) {
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(origin)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title("Departure")
                    );

            mGoogleMap.addMarker(new MarkerOptions()
                    .position(destination)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title("Arrival"));

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            mGoogleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.BLUE));

            //  To retrieve the Route instance from array at index 0.
            route = direction.getRouteList().get(0);

            //  To retrieve the Leg instance from array at index 0
            leg = route.getLegList().get(0);


            //Show the total distance and duration
            distanceInfo = leg.getDistance();
            durationInfo = leg.getDuration();
            distance = distanceInfo.getText();
            duration = durationInfo.getText();
            duration_text.setText(duration);
            distance_text.setText(distance);


            //Because 1 leg can be contain with many step. So you have to retrieve the Step in array.
            step_list = leg.getStepList();

            for (Step s : step_list) {

                //Retrieve the distance and duration of the each step.
                distanceInfo2 = s.getDistance();
                durationInfo2 = s.getDuration();
                distance2 = distanceInfo2.getText();
                duration2 = durationInfo2.getText();

                Log.i("DISTANZA STEP : " + i, distance2);
                Log.i("DURATA STEP : " + i, duration2);

                instruction = s.getHtmlInstruction();
                instruction = instruction.replace("<b>", "");
                instruction = instruction.replace("</b>", "");
                instruction=instruction.replace("</div>","");
                instruction=instruction.replace("<div style=\"font-size:0.9em\">","");

                Log.i("INSTRUCTION : " + i, instruction);

                i++;
            }


            //Build an object that containt all the Step, so you can move the Camera to contain all the route
           pointList = leg.getDirectionPoint();
            LatLngBounds.Builder bc = new LatLngBounds.Builder();

           //Contain all the coordinates of the Polylines
          for (LatLng lt : pointList) {
              bc.include(lt);
          }

            LatLngBounds bounds = bc.build();
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 140));


        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Toast.makeText(this,"onDirectionFailure",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {

            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            origin=latLng;

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 16));
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        requestDirection();

    }


    @Override
    public void onLocationChanged(Location location) {

        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        origin = latLng;

        // Drawing circle on the map
        drawCircle(latLng);

        // Toast.makeText(this,"Location Changed",Toast.LENGTH_SHORT).show();

        conta_coordinate = 0;

        //If it is at least the second Step of the route
        if (!flag && entra) {

            //the sectionList contains the Coordinates of each Step
            if (sectionList != null && !sectionList.isEmpty()) {
                for (LatLng lt : sectionList) {

                    //Calculate the distance between the current position and the position of the Step
                    Location.distanceBetween(lt.latitude, lt.longitude,
                            circleOptions.getCenter().latitude, circleOptions.getCenter().longitude, distance_cerchio);

                    if (distance_cerchio[0] > circleOptions.getRadius()) {
                        // Toast.makeText(getBaseContext(), "Outside", Toast.LENGTH_LONG).show();
                    } else {
                        //Toast.makeText(getBaseContext(), "Inside", Toast.LENGTH_LONG).show();

                        //Calculate the distance between the destination position and my current position,looking if i arrive
                        Location.distanceBetween(destination.latitude, destination.longitude,
                                circleOptions.getCenter().latitude, circleOptions.getCenter().longitude, distance_cerchio);

                        if (distance_cerchio[0] < circleOptions.getRadius()){
                            entra=false;

                            Log.i("SONO ARRIVATO : ", "DEVE SUONARE");

                           //Notification with default sound
                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

                            if (notification==null){
                                notification=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                                if(notification==null){
                                    notification=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                                }
                            }
                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                            r.play();

                            //Set text to the user
                            indication_step_text.setText(mArrive);
                            duration_step_text.setText(mArrive_space);
                            distance_step_text.setText(mArrive_space);

                            break;
                        }

                        //If the i-position of the Step is inside my circle,i will show the i-direction,with distance and duration
                        if (step_list != null && !step_list.isEmpty()) {

                            if (conta_coordinate > 0) {

                                //Get the distance,the duration and the instruction of the step i
                                distanceInfo2 = step_list.get(conta_coordinate).getDistance();
                                durationInfo2 = step_list.get(conta_coordinate).getDuration();
                                distance2 = distanceInfo2.getText();
                                duration2 = durationInfo2.getText();
                                instruction = step_list.get(conta_coordinate).getHtmlInstruction();

                                //Replace some characters of html with nothing,because the user mustn't see them
                                instruction = instruction.replace("<b>", "");
                                instruction = instruction.replace("</b>", "");
                                instruction = instruction.replace("</div>", "");
                                instruction = instruction.replace("<div style=\"font-size:0.9em\">", "");
                                instruction = instruction.replace("Destination", " Destination");

                                //Save the last instruction
                                if(!instruction_save.equals(instruction) && flag_imageButton){
                                    instruction_save=instruction;


                                    //If you turn left,vibrate one time
                                    if(instruction.toLowerCase().contains(left.toLowerCase())){
                                        vib.vibrate(1000);
                                    }

                                    //If you turn right,vibrate two times
                                    if(instruction.toLowerCase().contains(right.toLowerCase())){
                                        long[] pattern = {0,1000,500,1000};
                                        vib.vibrate(pattern,-1);
                                    }

                                    //Verify to be in a roundabout, and then vibrate one time if you must take the first exit,
                                    // two times if you must take the second exit, and so on.
                                    if(instruction.toLowerCase().contains(mRoundabout.toLowerCase())){

                                            if(instruction.toLowerCase().contains(mFirst.toLowerCase())){
                                                vib.vibrate(1000);
                                            }

                                            if(instruction.toLowerCase().contains(mSecond.toLowerCase())){
                                                long[] pattern = {0,1000,500,1000};
                                                vib.vibrate(pattern,-1);
                                            }

                                            if(instruction.toLowerCase().contains(mThird.toLowerCase())){
                                                long[] pattern={0,250,200,250,200,250};
                                                vib.vibrate(pattern,-1);
                                            }

                                            if(instruction.toLowerCase().contains(mFourth.toLowerCase())){
                                                long[] pattern={0,250,200,250,200,250,200,250};
                                                vib.vibrate(pattern,-1);
                                            }

                                             if(instruction.toLowerCase().contains(mFifth.toLowerCase())){
                                                 long[] pattern={0,250,200,250,200,250,200,250,200,250};
                                                  vib.vibrate(pattern,-1);
                                        }
                                    }

                                }

                                //Show direction,duration and distance of the Step i
                                duration_step_text.setText(duration2);
                                distance_step_text.setText(distance2);
                                indication_step_text.setText(instruction);

                                Log.i("SHOW TO  : ", "USER");
                                Log.i("DISTANZA STEP : ", distance2);
                                Log.i("DURATA STEP : ", duration2);
                                Log.i("INDICAZIONE : ", instruction);
                            }
                        }
                    }

                    conta_coordinate++;
                        }

                }
            }

        //If it is the first Step of the route
        if(flag){
            if (step_list != null && !step_list.isEmpty()){

                //Get the distance,the duration and the instruction of the step 0 (the first)
                distanceInfo2= step_list.get(0).getDistance();
                durationInfo2=step_list.get(0).getDuration();
                distance2=distanceInfo2.getText();
                duration2=durationInfo2.getText();
                instruction=step_list.get(0).getHtmlInstruction();

                //Replace some characters of html with nothing,because the user mustn't see them
                instruction = instruction.replace("<b>", "");
                instruction = instruction.replace("</b>", "");
                instruction=instruction.replace("</div>","");
                instruction=instruction.replace("<div style=\"font-size:0.9em\">","");
                instruction = instruction.replace("Destination", " Destination");

                //Display the text
                duration_step_text.setText(duration2);
                distance_step_text.setText(distance2);
                indication_step_text.setText(instruction);

                flag=false;
                instruction_save=instruction;

                if(flag_imageButton){

                    //If you turn right, vibrate one time
                    if(instruction.toLowerCase().contains(left.toLowerCase())){
                        vib.vibrate(1000);
                    }

                    //If you turn right, vibrate two times
                    if(instruction.toLowerCase().contains(right.toLowerCase())){
                        long[] pattern = {0,1000,500,1000};
                        vib.vibrate(pattern,-1);
                    }

                    //Verify to be in a roundabout, and then vibrate one time if you must take the first exit,
                    // two times if you must take the second exit, and so on.
                    if(instruction.toLowerCase().contains(mRoundabout.toLowerCase())){

                        if(instruction.toLowerCase().contains(mFirst.toLowerCase())){
                            vib.vibrate(1000);
                        }

                        if(instruction.toLowerCase().contains(mSecond.toLowerCase())){
                            long[] pattern = {0,1000,500,1000};
                            vib.vibrate(pattern,-1);
                        }

                        if(instruction.toLowerCase().contains(mThird.toLowerCase())){
                            long[] pattern={0,250,200,250,200,250};
                            vib.vibrate(pattern,-1);
                        }

                        if(instruction.toLowerCase().contains(mFourth.toLowerCase())){
                            long[] pattern={0,250,200,250,200,250,200,250};
                            vib.vibrate(pattern,-1);
                        }

                        if(instruction.toLowerCase().contains(mFifth.toLowerCase())){
                            long[] pattern={0,250,200,250,200,250,200,250,200,250};
                            vib.vibrate(pattern,-1);
                        }
                    }
                }
            }
        }

    }


    private void drawCircle(LatLng point){

        if (myCircle != null) {
            myCircle.remove();
        }

        // Specifying the center of the circle
        circleOptions.center(point);

        // Radius of the circle
        circleOptions.radius(20);

        // Border color of the circle
        circleOptions.strokeColor(Color.BLUE);

        // Fill color of the circle
        circleOptions.fillColor(0x0000ff);

        // Border width of the circle
        circleOptions.strokeWidth(2);

        // Adding the circle to the GoogleMap
        myCircle=mGoogleMap.addCircle(circleOptions);

    }

}
