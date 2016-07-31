package com.example.android.direction;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class SimpleDirectionActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, DirectionCallback {
    private Button btnRequestDirection;
    private GoogleMap googleMap;
    private String serverKey = "AIzaSyDzpUBcGKhAXcPdE-HRjaZGr8xiKg55mcY";
    private LatLng origin = new LatLng(44.552164, 10.790994);
    private LatLng destination = new LatLng(44.541826, 10.781694);
    private Info distanceInfo;
    private Info durationInfo;
    private String distance;
    private String duration;
    private Leg leg;
    TextView duration_text;
    TextView distance_text;
    private Route route;
    private Step step;
    private Info distanceInfo2;
    private Info durationInfo2;
    private String distance2;
    private String duration2;
    private int i = 1;
    private String instruction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_direction);

        btnRequestDirection = (Button) findViewById(R.id.btn_request_direction);

        btnRequestDirection.setOnClickListener(this);

        duration_text = (TextView) findViewById(R.id.duration);
        distance_text = (TextView) findViewById(R.id.distance);


        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
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
        googleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_request_direction) {
            requestDirection();
        }
    }

    public void requestDirection() {
        Snackbar.make(btnRequestDirection, "Direction Requesting...", Snackbar.LENGTH_SHORT).show();
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
        Snackbar.make(btnRequestDirection, "Success with status : " + direction.getStatus(), Snackbar.LENGTH_SHORT).show();
        if (direction.isOK()) {
            googleMap.addMarker(new MarkerOptions().position(origin));
            googleMap.addMarker(new MarkerOptions().position(destination));

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            googleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.BLUE));


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
            List<Step> step_list = leg.getStepList();

            for (Step s : step_list) {
                //Log.i("CONTENUTO STEP LIST : ", String.valueOf(s));
                //Retrieve the distance and duration of the each step.
                distanceInfo2 = s.getDistance();
                durationInfo2 = s.getDuration();
                distance2 = distanceInfo2.getText();
                duration2 = durationInfo2.getText();

                Log.i("DISTANZA STEP : " + i, distance2);
                Log.i("DURATA STEP : " + i, duration2);

                //  Navigation instruction is available by retrieve from Maneuver instance of the each step.
                // HTML instruction also available too but don't forget to convert them from HTML format to
                // show in your app.

                instruction = s.getHtmlInstruction();
                instruction = instruction.replace("<b>", "");
                instruction = instruction.replace("</b>", "");
                Log.i("INSTRUCTION : ", instruction);

            }


            //So you can retrieve them in LatLgn array directly. Just using getDirectionPoint method
           ArrayList<LatLng> pointList = leg.getDirectionPoint();
           //Contiene tutte le coordinate del polylines
          for (LatLng lt : pointList) {
               Log.i("CONTENUTO ARRAY LIST : ", String.valueOf(lt));
          }



            //   If you want to retrieve the location of the each step.
            //Contiene le coordinate di ogni step
            ArrayList<LatLng> sectionList = leg.getSectionPoint();
            for (LatLng lt : sectionList) {
                Log.i("CONTENUTO ARRAY STEP : ", String.valueOf(lt));
            }



            btnRequestDirection.setVisibility(View.GONE);

        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Snackbar.make(btnRequestDirection, t.getMessage(), Snackbar.LENGTH_SHORT).show();
    }
}
