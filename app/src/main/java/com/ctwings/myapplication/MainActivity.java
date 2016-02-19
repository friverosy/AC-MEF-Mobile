package com.ctwings.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.KeyEvent;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class MainActivity extends AppCompatActivity {

    private ImageView imageview;
    private EditText editTextRun;
    private EditText editTextFullName;
    private String runStr, fullNameStr;

    private static final Logger log = Logger.getLogger(MainActivity.class.getName());
    private static String server = "http://192.168.2.149:3000" ;
    private boolean state;

    private final static String SCAN_ACTION = "urovo.rcv.message";//扫描结束action
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private int soundid;
    private String barcodeStr;
    private boolean isScaning = false;

    MediaPlayer mp3Dennied;
    MediaPlayer mp3Permitted;

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            isScaning = false;
            //soundpool.play(soundid, 1, 1, 0, 0, 1);
            editTextRun.setText("");
            editTextFullName.setText("");
            imageview.setImageDrawable(null);

//            imageview.clearAnimation();
//            editTextRun.clearAnimation();
//            editTextFullName.clearAnimation();
//            imageview.setAlpha((float) 1.0);
//            editTextRun.setAlpha((float) 1.0);
//            editTextFullName.setAlpha((float) 1.0);

            mVibrator.vibrate(100);

            byte[] barcode = intent.getByteArrayExtra("barocode");
            int barocodelen = intent.getIntExtra("length", 0);
            //byte temp = intent.getByteExtra("barcodeType", (byte) 0);
            //android.util.Log.i("debug", "----codetype--" + temp);
            barcodeStr = new String(barcode, 0, barocodelen);

            if(barcodeStr.startsWith("https")){
                barcodeStr = barcodeStr.substring(52, 62);
                barcodeStr = barcodeStr.replace("-", "");
                barcodeStr = barcodeStr.replace("&","");
            }else{
                barcodeStr = barcodeStr.substring(0, 9);
            }

            editTextRun.setText("Run: " + barcodeStr);
//            if (isConnected()) {
//                new GetPeopleTask().execute("http://192.168.2.149:3000/api/people/" + barcodeStr);
//            } else {
//                Toast.makeText(MainActivity.this, "Sin coneccion a internet!",
//                        Toast.LENGTH_LONG).show();
//            }
            new GetPeopleTask().execute(server + "/api/people/" + barcodeStr);
        }

    };

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

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        editTextRun = (EditText)findViewById(R.id.editText_run);
        editTextFullName = (EditText)findViewById(R.id.editText_fullname);
        imageview = (ImageView)findViewById(R.id.imageView);
        mp3Dennied = MediaPlayer.create(MainActivity.this, R.raw.dennied);
        mp3Permitted = MediaPlayer.create(MainActivity.this, R.raw.permitted);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (isConnected()) {
//                    new GetPeopleTask().execute("http://192.168.2.149:3000/api/people/" +
//                            barcodeStr);
//                } else {
//                    Toast.makeText(MainActivity.this, "Sin conecciÃ³n a internet!",
//                            Toast.LENGTH_LONG).show();
//                }
                new GetPeopleTask().execute(server + "/api/people/" + barcodeStr);
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

    private void initScan() {
        // TODO Auto-generated method stub
        mScanManager = new ScanManager();
        mScanManager.openScanner();

        mScanManager.switchOutputMode( 0);
        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        //soundid = soundpool.load("/etc/Scan_new.ogg", 1);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if(mScanManager != null) {
            mScanManager.stopDecode();
            isScaning = false;
        }
        unregisterReceiver(mScanReceiver);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        initScan();
        editTextRun.setText("");
        editTextFullName.setText("");
        IntentFilter filter = new IntentFilter();
        filter.addAction(SCAN_ACTION);
        registerReceiver(mScanReceiver, filter);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return super.onKeyDown(keyCode, event);
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
                    Toast.makeText(MainActivity.this, "Error al obtener datos, intente nuevamente",
                            Toast.LENGTH_SHORT).show();
                    return null;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                log.info("Persona no encontrada");
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
            try {
                super.onPostExecute(s);
                String[] arr = s.split(",");
                editTextRun.setText("Run: " + arr[0]);
                editTextFullName.setText(arr[1]);

                runStr = arr[0];
                fullNameStr = arr[1];

                if(arr[2].equals("true")) {
                    mp3Permitted.start();
                    state = true;
                    imageview.setImageResource(R.drawable.img_true);
                }else {
                    mp3Dennied.start();
                    state = false;
                    imageview.setImageResource(R.drawable.img_false);
                }

                // effect to reduce alpha
//                Animation AlphaAnimation;
//                AlphaAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.transparentar);
//                AlphaAnimation.reset();
//                imageview.startAnimation(AlphaAnimation);
//                editTextRun.startAnimation(AlphaAnimation);
//                editTextFullName.startAnimation(AlphaAnimation);

                // after start effect, let alpha down.

//                try {
//                    Thread.sleep(3000);
//                    imageview.setAlpha((float) 0.3);
//                    editTextRun.setAlpha((float) 0.3);
//                    editTextFullName.setAlpha((float) 0.3);
//                }catch(InterruptedException ex) {
//                    Thread.currentThread().interrupt();
//                }

                //  new POST
                new RegisterTask().execute(server + "/api/records/");

            } catch (NullPointerException e){
                // people don't exist in DB
                state = false;
                imageview.setImageResource(R.drawable.img_false);
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public String POST(String url){
        InputStream inputStream;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json;

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("people_run", runStr);
            jsonObject.accumulate("fullname", fullNameStr);

            if(state) {
                jsonObject.accumulate("is_permitted", true);
                //log.info("true");
            }else {
                jsonObject.accumulate("is_permitted", false);
                //log.info("false");
            }

            log.info(jsonObject.toString());

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 11. return result
        return result;
    }

    public class RegisterTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            return POST(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            //Toast.makeText(MainActivity.this, "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }
}