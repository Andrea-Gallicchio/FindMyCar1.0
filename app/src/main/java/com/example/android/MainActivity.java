package com.example.android.direction;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Find the UI in the activity_main.xml*/
        findViewById(R.id.btn_simple).setOnClickListener(this);
        findViewById(R.id.btn_simple2).setOnClickListener(this);
        findViewById(R.id.imageView).setOnClickListener(this);

    }


   /* This method is the listener for the two buttons and the imageView.*/
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_simple) {
            goToSimpleDirection();
        }
        else if ( id == R.id.btn_simple2){
            goToSavePosition();
        }
        else if (id == R.id.imageView){
            goToInstructions();
        }
    }

    public void goToSimpleDirection() {
        openActivity(SimpleDirectionActivity.class);
    }

    public void goToSavePosition() { openActivity(Save.class);}

    public void goToInstructions() {openActivity(Instructions.class);}

    public void openActivity(Class<?> cs) {
        startActivity(new Intent(this, cs));
    }
}
