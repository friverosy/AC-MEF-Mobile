package com.ctwings.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class log_show extends AppCompatActivity {
    private static final String LOG_NAME = "MultiExport.log";
    private String logType;
    private TextView showlog;
    private StringBuilder text = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_app);
        showlog = (TextView) findViewById(R.id.fill_log);
        if (readLog() != null && !readLog().toString().equals("")) {
            showlog.setText(readLog());
            //showlog.setMovementMethod(new ScrollingMovementMethod());
        }

    }

    private StringBuilder readLog() {
        StringBuilder stringBuilder= null;
        try {
            InputStream inputStream = openFileInput(LOG_NAME);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                stringBuilder = new StringBuilder();
                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                    stringBuilder.append("\n\r");
                    stringBuilder.append("-----------------------------------------------------------------------");
                    //stringBuilder.append(System.getProperty("line.separator"));

                }
                inputStream.close();
                //content = stringBuilder.toString();
            }

        } catch (FileNotFoundException e) {
            Log.e("file state", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("file state", "Can not read file: " + e.toString());
        }
        return stringBuilder;
    }
}
