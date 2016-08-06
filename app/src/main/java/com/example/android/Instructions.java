package com.example.android.direction;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * This class is used for the information activity,and will show the info at the user..
 */
public class Instructions extends AppCompatActivity {

    TextView instruction;
    String testo = "For save the position of your car click on \"Save Position\".\n" +
            "After that, when you want to find your car, click on \"Find My Car\".\n" +
            "It will open a window, where you can choose if you want to reach your car using the \"Vibration Mode\" or not.\n" +
            "In this modality, the phone will vibrate one time if the indication is to go on the left, two time if the indication is to go on the right, or if you are near a roundabout it will vibrate as many times as the output number to be taken.\n" +
            "Alternately, you can choose to not use this modality, and reach your car with the route and the direction shows on the screen. If your current position is not available, the system will get your last known location. ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        instruction=(TextView)findViewById(R.id.text_instruction);
        instruction.setText(testo);

    }

}
