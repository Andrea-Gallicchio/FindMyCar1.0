package com.example.android.direction;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private final static String filename="myfile.txt";

    /**
     * Displays the location address.
     */
    protected TextView mLocationAddressTextView;

    String preLongStr;
    String postLongStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_simple).setOnClickListener(this);
        findViewById(R.id.btn_simple2).setOnClickListener(this);
        mLocationAddressTextView = (TextView) findViewById(R.id.location_address_view);


            readFileInEditor();

    }

    //method that reads this newly-created file and populates the text editor with the contents of the file
    public void readFileInEditor()
    {
        try {
            //Open the file with openFileInput,and then create an InputStream from it
            InputStream in = openFileInput(filename);

            if (in != null) {
                //Create an InputStreamReader
                InputStreamReader tmp=new InputStreamReader(in);
                //Create a BufferedReader,Using the BufferedReader, we read line after line the text
                // of the storage file, and we store the text in the buffer
                BufferedReader reader=new BufferedReader(tmp);
                String str;
                StringBuilder buf=new StringBuilder();

                while ((str = reader.readLine()) != null) {

                    buf.append(str);

                }

                in.close();

                int i,pos=0;
                for(i=0;i<buf.toString().length();i++){
                    if(buf.toString().charAt(i)>='0' && buf.toString().charAt(i)<='9'){
                        pos=i;
                        break;
                    }

                }
                preLongStr = buf.toString().substring(0, pos);
                postLongStr =  buf.toString().substring(pos);

                 //Once the whole file is read, we send the text into the editor
                mLocationAddressTextView.setText("Parked in :" + '\n' + preLongStr + "\n" + postLongStr);
            }
        }
        catch (java.io.FileNotFoundException e) {

            // that's OK, we probably haven't created it yet
        }
        catch (Throwable t) {
            Toast.makeText(this, "Exception: "+t.toString(), Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_simple) {
            goToSimpleDirection();
        }
        else if ( id == R.id.btn_simple2){
            goToSavePosition();
        }
    }

    public void goToSimpleDirection() {
        openActivity(SimpleDirectionActivity.class);
    }

    public void goToSavePosition() { openActivity(SavePosition.class);}

    public void openActivity(Class<?> cs) {
        startActivity(new Intent(this, cs));
    }
}
