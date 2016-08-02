package com.example.android.direction;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class SimpleDirectionActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, DirectionCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Button btnRequestDirection;
    private GoogleMap mGoogleMap;
    private String serverKey = "AIzaSyDzpUBcGKhAXcPdE-HRjaZGr8xiKg55mcY";
    private LatLng origin;
    private LatLng destination = new LatLng(44.553134, 10.791660);
    private Info distanceInfo;
    private Info durationInfo;
    private String distance;
    private String duration;
    private Leg leg;
    TextView duration_text;
    TextView distance_text;
    TextView duration_step_text;
    TextView distance_step_text;
    TextView indication_step_text;
    private Route route;
    private Step step;
    private Info distanceInfo2;
    private Info durationInfo2;
    private String distance2;
    private String duration2;
    private int i = 1;
    private String instruction;

    private boolean flag=true;

    private float[] distance_cerchio = new float[2];

    protected ArrayList<LatLng> sectionList;

    private ArrayList<LatLng> pointList;

    //Because 1 leg can be contain with many step. So you have to retrieve the Step in array.
    //Contiene tutti i vari step,quindi tutte le indicazioni però ancora codificate
    private List<Step> step_list;

    //Variabili per aggiornamento costante posizione

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    LatLng latLng;

    SupportMapFragment mFragment;
    Marker currLocationMarker;

    // Instantiating CircleOptions to draw a circle around the marker
    CircleOptions circleOptions = new CircleOptions();
    private Circle myCircle;

    PolylineOptions myPolyline =new PolylineOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_direction);

        btnRequestDirection = (Button) findViewById(R.id.btn_request_direction);

        btnRequestDirection.setOnClickListener(this);

        duration_text = (TextView) findViewById(R.id.duration);
        distance_text = (TextView) findViewById(R.id.distance);
        duration_step_text=(TextView) findViewById(R.id.duration_step);
        distance_step_text=(TextView) findViewById(R.id.distance_step);
        indication_step_text=(TextView) findViewById(R.id.indication);

        mFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mGoogleMap.setMyLocationEnabled(true);

        buildGoogleApiClient();

        mGoogleApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient() {
        //Toast.makeText(this, "buildGoogleApiClient", Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_request_direction) {
            requestDirection();
        }
    }

    public void requestDirection() {
        //Snackbar.make(btnRequestDirection, "Direction Requesting...", Snackbar.LENGTH_SHORT).show();
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
        //Snackbar.make(btnRequestDirection, "Success with status : " + direction.getStatus(), Snackbar.LENGTH_SHORT).show();
        if (direction.isOK()) {
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(origin)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title("Partenza")
                    );
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(destination)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title("Arrivo"));

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            mGoogleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.BLUE));

            //  To retrieve the Route instance from array at index 0.
            route = direction.getRouteList().get(0);
            //  To retrieve the Leg instance from array at index 0
            leg = route.getLegList().get(0);


            //MOSTRA DISTANZA E DURATA TOTALE
            distanceInfo = leg.getDistance();
            durationInfo = leg.getDuration();
            distance = distanceInfo.getText();
            duration = durationInfo.getText();
            duration_text.setText(duration);
            distance_text.setText(distance);


            //Because 1 leg can be contain with many step. So you have to retrieve the Step in array.
            //Contiene tutti i vari step,quindi tutte le indicazioni però ancora codificate
            step_list = leg.getStepList();

            for (Step s : step_list) {
                //Log.i("CONTENUTO STEP LIST : ", String.valueOf(s));
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

                Log.i("INSTRUCTION : ", instruction);

                i++;
            }


            //So you can retrieve them in LatLgn array directly. Just using getDirectionPoint method
           pointList = leg.getDirectionPoint();
            LatLngBounds.Builder bc = new LatLngBounds.Builder();
           //Contiene tutte le coordinate del polylines
          for (LatLng lt : pointList) {
               Log.i("CONTENUTO ARRAY LIST : ", String.valueOf(lt));
              bc.include(lt);
          }

            LatLngBounds bounds = bc.build();
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 110));



            //   If you want to retrieve the location of the each step.
            //Contiene le coordinate di ogni step
             sectionList = leg.getSectionPoint();
            for (LatLng lt : sectionList) {
                Log.i("CONTENUTO ARRAY STEP : ", String.valueOf(lt));
            }


            btnRequestDirection.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Snackbar.make(btnRequestDirection, t.getMessage(), Snackbar.LENGTH_SHORT).show();
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
        //Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            //place marker at current position
            //mGoogleMap.clear();
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            origin=latLng;

        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onLocationChanged(Location location) {

        //place marker at current position
        //mGoogleMap.clear();
        if (currLocationMarker != null) {
            currLocationMarker.remove();
        }
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        origin = latLng;

        // Drawing circle on the map
        drawCircle(latLng);

        // Toast.makeText(this,"Location Changed",Toast.LENGTH_SHORT).show();

        int conta_coordinate = 0;

        if (flag == false) {
            //Contiene le coordinate di ogni step
            if (sectionList != null && !sectionList.isEmpty()) {
                for (LatLng lt : sectionList) {

                    Location.distanceBetween(lt.latitude, lt.longitude,
                            circleOptions.getCenter().latitude, circleOptions.getCenter().longitude, distance_cerchio);

                    if (distance_cerchio[0] > circleOptions.getRadius()) {
                        // Toast.makeText(getBaseContext(), "Outside", Toast.LENGTH_LONG).show();
                    } else {
                        //Toast.makeText(getBaseContext(), "Inside", Toast.LENGTH_LONG).show();
                        //QUA VOGLIO MOSTRARE L'INDICAZIONE RIFERITA ALLA COORDINATA CHE è DENTRO
                        //Contiene tutti i vari step,quindi tutte le indicazioni
                        if (step_list != null && !step_list.isEmpty()) {

                            if(conta_coordinate==0)
                                    break;

                                    distanceInfo2 = step_list.get(conta_coordinate).getDistance();
                                    durationInfo2 = step_list.get(conta_coordinate).getDuration();
                                    distance2 = distanceInfo2.getText();
                                    duration2 = durationInfo2.getText();
                                    instruction = step_list.get(conta_coordinate).getHtmlInstruction();
                                    instruction = instruction.replace("<b>", "");
                                    instruction = instruction.replace("</b>", "");
                                    instruction=instruction.replace("</div>","");
                                    instruction=instruction.replace("<div style=\"font-size:0.9em\">","");

                                    //Mostrami le indicazioni
                                    duration_step_text.setText(duration2);
                                    distance_step_text.setText(distance2);
                                    indication_step_text.setText(instruction);
                                }
                    }

                    conta_coordinate++;
                        }

                }
            }


        if(flag==true){
            if (step_list != null && !step_list.isEmpty()){

                distanceInfo2= step_list.get(0).getDistance();
                durationInfo2=step_list.get(0).getDuration();
                distance2=distanceInfo2.getText();
                duration2=durationInfo2.getText();
                instruction=step_list.get(0).getHtmlInstruction();
                instruction = instruction.replace("<b>", "");
                instruction = instruction.replace("</b>", "");
                instruction=instruction.replace("</div>","");
                instruction=instruction.replace("<div style=\"font-size:0.9em\">","");
                duration_step_text.setText(duration2);
                distance_step_text.setText(distance2);
                indication_step_text.setText(instruction);
                flag=false;
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
