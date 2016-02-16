package com.ctwings.myapplication;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ImageView imageview;
    private EditText editTextRun;
    private EditText editTextFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "something", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        editTextRun = (EditText)findViewById(R.id.editText_run);
        editTextFullName = (EditText)findViewById(R.id.editText_fullname);
        imageview = (ImageView)findViewById(R.id.imageView);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected()){
                    //new JSONTask().execute("http://jsonparsing.parseapp.com/jsonData/moviesDemoItem.txt");
                    new GetPeopleTask().execute("http://192.168.2.149:3000/api/people/findOne?run=171793475");
                    new RegisterTask().execute("http://192.168.2.149:3000/api/record/");
                }else{
                    Toast.makeText(MainActivity.this, "Sin conección a internet!", Toast.LENGTH_LONG).show();
                }

            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public class GetPeopleTask extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params) {

            /*
            * // Parse XML
            XmlPullParserFactory pullParserFactory;

            try {
              pullParserFactory = XmlPullParserFactory.newInstance();
              XmlPullParser parser = pullParserFactory.newPullParser();

              parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
              parser.setInput(in, null);
              result = parseXML(parser);
            } catch (XmlPullParserException e) {
              e.printStackTrace();
            } catch (IOException e) {
              e.printStackTrace();
            }

            // Simple logic to determine if the email is dangerous, invalid, or valid
            if (result != null ) {
              if( result.hygieneResult.equals("Spam Trap")) {
                resultToDisplay = "Dangerous email, please correct";
              }
              else if( Integer.parseInt(result.statusNbr) >= 300) {
                resultToDisplay = "Invalid email, please re-enter";
              }
              else {
                resultToDisplay = "Thank you for your submission";
              }
                }
                else {
                  resultToDisplay = "Exception Occured";
                }

                return resultToDisplay;
              }

            * */

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                String finalJson = buffer.toString();

                if(!finalJson.isEmpty()){
                    JSONObject parentObject = new JSONObject(finalJson);
                    return parentObject.getString("run") + "," + parentObject.getString("fullname") +
                            "," + parentObject.getBoolean("is_permitted");
                }else{
                    Toast.makeText(MainActivity.this, "Error al obtener datos, intente nuevamente", Toast.LENGTH_SHORT).show();
                    return null;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String[] arr = s.split(",");
            editTextRun.setText(arr[0].toString());
            editTextFullName.setText(arr[1].toString());
            if(arr[2].toString().equals("true"))
                imageview.setImageResource(R.drawable.img_true);
            else
                imageview.setImageResource(R.drawable.img_false);
        }
    }

    public class RegisterTask extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... params) {
            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}