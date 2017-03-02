package com.ctwings.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity {

    private final int delay = 600000; // 4 Min. 240000; 600000 10 min
    //private final String server = "http://controlid-test.multiexportfoods.com:3000";
    //private final String server = "http://controlid.multiexportfoods.com:3000";
    private final String server = "http://192.168.1.126:3000";
    private final int delayPeople = 600000; // 4 Min. 240000; 600000 10 min
    private final int delayRecords = 500000; // 4 Min. 240000; 480000 8 min
    private static String version = "49a738e";

    private ImageView imageview;
    private EditText editTextRun;
    private EditText editTextFullName;
    private TextView textViewVersion;
    private String name = "";
    private EditText editTextCompany;
    private TextView textViewProfile;
    private ProgressWheel loading;
    private boolean is_input;
    private TextView lastUpdated;
    private LoadDbTask updatePeopleTask;

    private final static String SCAN_ACTION = "urovo.rcv.message";//扫描结束action
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private String barcodeStr;
    private String barcodeCache;
    private boolean isScaning = false;
    private Switch mySwitch;
    MediaPlayer mp3Dennied;
    MediaPlayer mp3Permitted;
    MediaPlayer mp3Error;
    DatabaseHelper db = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //remove it
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "something", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                mVibrator.vibrate(100);
            }
        });
        //create the log file
        File log = new File(this.getFilesDir() + File.separator + "AccessControl.log");
        if (!log.isFile()) {
            try {
                log.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //call the loading library in xml file
        loading = (ProgressWheel) findViewById(R.id.loading);
        loading.setVisibility(View.GONE);

        UpdateDbPeople();//asyntask  updating people
        sendRecords(); //send records to api axxezo

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        editTextRun = (EditText) findViewById(R.id.editText_run);
        editTextFullName = (EditText) findViewById(R.id.editText_fullname);
        editTextCompany = (EditText) findViewById(R.id.editText_company);
        textViewProfile = (TextView) findViewById(R.id.textView_profile);
        imageview = (ImageView) findViewById(R.id.imageView);
        mp3Dennied = MediaPlayer.create(MainActivity.this, R.raw.bad);
        mp3Permitted = MediaPlayer.create(MainActivity.this, R.raw.good);
        mp3Error = MediaPlayer.create(MainActivity.this, R.raw.error);
        editTextCompany.setVisibility(View.GONE);
        mySwitch = (Switch) findViewById(R.id.mySwitch);
        mySwitch.setChecked(true);
        textViewVersion = (TextView) findViewById(R.id.textView_version);
        textViewVersion.setText("Versión: " + version);

        // set by default
        is_input = true;

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    is_input = true;
                } else {
                    is_input = false;
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPeople(editTextRun.getText().toString());
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
            reset();
            return true;
        } else if (id == R.id.action_setting) {
            Intent i = new Intent(this, Setting.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            log_app log = new log_app();
            // TODO Auto-generated method stub
            try {
                if (mp3Error.isPlaying()) mp3Error.stop();
                if (mp3Dennied.isPlaying()) mp3Dennied.stop();
                if (mp3Permitted.isPlaying()) mp3Permitted.stop();

                isScaning = false;
                //soundpool.play(soundid, 1, 1, 0, 0, 1);

                mVibrator.vibrate(100);
                cleanEditText();

                byte[] barcode = intent.getByteArrayExtra("barocode");
                int barocodelen = intent.getIntExtra("length", 0);
                byte barcodeType = intent.getByteExtra("barcodeType", (byte) 0);
                barcodeStr = new String(barcode, 0, barocodelen);
                String rawCode = barcodeStr;

                int flag = 0; // 0 for end without k, 1 with k
                int flagSetUp = 0; // 0 for no config QR code.

                if (barcodeType == 28) { // QR code
                    if (barcodeStr.startsWith("CONFIG-AXX-")) {
                        flagSetUp = 1;
                        SetUp(barcodeStr);
                    } else {
                        // get only rut
                        barcodeStr = barcodeStr.substring(
                                barcodeStr.indexOf("RUN=") + 4,
                                barcodeStr.indexOf("&type"));
                        // remove dv.
                        barcodeStr = barcodeStr.substring(0, barcodeStr.indexOf("-"));
                    }
                } else if (barcodeType == 1 || barcodeStr.startsWith("00")) {
                    //Log.i("Debugger", "CARD");
                } else if (barcodeType == 17) { // PDF417
                    // 1.- validate if the rut is > 10 millions
                    String rutValidator = barcodeStr.substring(0, 8);
                    rutValidator = rutValidator.replace(" ", "");
                    rutValidator=rutValidator.endsWith("K")?rutValidator.replace("K","0"):rutValidator;
                    char dv = barcodeStr.substring(8, 9).charAt(0);
                    boolean isvalid = ValidarRut(Integer.parseInt(rutValidator), dv);
                    if (isvalid)
                        barcodeStr = rutValidator;
                    else { //try validate rut size below 10.000.000
                        rutValidator = barcodeStr.substring(0, 7);
                        rutValidator = rutValidator.replace(" ", "");
                        rutValidator=rutValidator.endsWith("K")?rutValidator.replace("K","0"):rutValidator;
                        dv = barcodeStr.substring(7, 8).charAt(0);
                        isvalid = ValidarRut(Integer.parseInt(rutValidator), dv);
                        if (isvalid)
                            barcodeStr = rutValidator;
                        else
                            log.writeLog(getApplicationContext(), "Main:line 262", "ERROR", "rut invalido " + barcodeStr);

                    }

                    //get name from DNI
                    /*String[] array = rawCode.split("\\s+");
                    Log.d("-----", rawCode);
                    try {
                        name = (array[1].substring(0, array[1].indexOf("CHL")));
                    } catch (Exception e) {
                        name = (array[2].substring(0, array[2].indexOf("CHL")));
                    }*/
                    name = "";
                }

                if (flagSetUp == 0)
                    getPeople(barcodeStr);
                barcodeCache = barcodeStr; // Used to avoid 2 records in a row.
            } catch (NullPointerException e) {
                log.writeLog(getApplicationContext(), "Main:line 278", "ERROR", e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                log.writeLog(getApplicationContext(), "Main:line 281", "ERROR", e.getMessage());
            }
        }
    };

    private void SetUp(String barcodeStr) {
        switch (barcodeStr) {
            case "CONFIG-AXX-637B55B8AA55C7C7D3810E0CE05B1E80":
                // Offline record Syncronize
                if (db.record_desync_count() > 0) {
                    OfflineRecordsSynchronizer();
                    makeToast("Sincronizados!");
                } else
                    makeToast("No hay registros offline para sincronizar");
                break;
            case "CONFIG-AXX-F5CCAFFD2C2225A7CE0FBEC87993F6EF":
                // Offline record counter
                makeToast(String.valueOf(db.record_desync_count()) + " Registros aun no sincronizados");
                break;
            case "CONFIG-AXX-75687092BFAE94A0CBF81572E2C8C015":
                // People counter
                makeToast(String.valueOf(db.people_count()) + " Personas");
                break;
            case "CONFIG-AXX-C78768F72CBE1C08A4AFD98285FE0C7D":
                // Employee counter
                makeToast(String.valueOf(db.employees_count()) + " Empleados");
                break;
            case "CONFIG-AXX-B71580A4F60179BC005D359A8344FA63":
                // Contractors counter
                makeToast(String.valueOf(db.contractors_count()) + " Contratistas");
                break;
            case "CONFIG-AXX-4B6DA20544C994DAE45088C4A80C25F4":
                // Visits counter
                makeToast(String.valueOf(db.visits_count()) + " Visitas");
                break;
            case "CONFIG-AXX-CD0A4191D9CC5214650E32E13EFBD086":
                // Drop people table
                db.clean_people();
                makeToast("Tabla personas vaciada.");
                break;
            case "CONFIG-AXX-6rVLydzn651RsZZ3dqWk":
                // call LOG
                Intent intent = new Intent(this, log_show.class);
                startActivity(intent);
                break;
            case "CONFIG-AXX-A11C9984001C27A12CC09A3C53B39ADF":
                // Drop record table
                db.clean_records();
                makeToast("Tabla records vaciada.");
                break;
            default:
                makeToast("Código de configuración incorrecto!");
                break;
        }
    }

    public boolean ValidarRut(int rut, char dv) {
        dv=dv=='k'?dv='K':dv;
        int m = 0, s = 1;
        for (; rut != 0; rut /= 10) {
            s = (s + rut % 10 * (9 - m++ % 6)) % 11;
        }
        return dv == (char) (s != 0 ? s + 47 : 75);
    }

    private void initScan() {
        // TODO Auto-generated method stub
        mScanManager = new ScanManager();
        mScanManager.openScanner();
        mScanManager.switchOutputMode(0);
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
        if (mScanManager != null) {
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(SCAN_ACTION);
        registerReceiver(mScanReceiver, filter);
    }

    public void UpdateDbPeople() {
        Timer timer = new Timer();
        final Handler handler = new Handler();
        final log_app log = new log_app();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            updatePeopleTask = new LoadDbTask();
                            updatePeopleTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } catch (Exception e) {
                            log.writeLog(getApplicationContext(), "Main:line 397", "ERROR", e.getMessage());
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, delayPeople);

    }

    public void sendRecords() {
        Timer timer = new Timer();
        final Handler handler = new Handler();
        final log_app log = new log_app();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            if (db.record_desync_count() > 0 && updatePeopleTask.getStatus() != AsyncTask.Status.RUNNING) {
                                //if task running dont sync  && updatePeopleTask.getStatus() != AsyncTask.Status.RUNNING
                                OfflineRecordsSynchronizer();
                            }
                        } catch (Exception e) {
                            log.writeLog(getApplicationContext(), "Main:line 419", "ERROR", e.getMessage());
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, delayRecords);
    }

    public void reset() {
        initScan();
        //cleanEditText();
        barcodeStr = "";
        IntentFilter filter = new IntentFilter();
        filter.addAction(SCAN_ACTION);
        registerReceiver(mScanReceiver, filter);
    }

    public void cleanEditText() {
        editTextRun.setText("");
        editTextFullName.setText("");
        editTextCompany.setText("");
        textViewProfile.setText("");
        imageview.setImageDrawable(null);
        name = null;
    }

    public String getCurrentDateTime() {
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.S");
        String localTime = date.format(currentLocalTime);
        return localTime;
    }

    public void getPeople(String rut) {
        log_app log = new log_app();
        String finalJson = db.get_one_person(rut);
        editTextCompany.setVisibility(View.GONE);
        String[] arr = finalJson.split(";");
        try {
            // set editText here before any exceptions.
            editTextRun.setText(arr[0]);
            //build object with that values, then send to registerTarsk()
            Record record = new Record();
            record.setPerson_run(arr[0]);

            if (arr[2].equals("true")) {
                mp3Permitted.start();
                //is_permitted = true;
                record.setPerson_is_permitted(1);
                if (is_input)
                    imageview.setImageResource(R.drawable.permitted);
            } else {
                mp3Dennied.start();
                // if has card number define as denied and as employee
                //is_permitted = false;
                record.setPerson_is_permitted(0);
                if (is_input)
                    imageview.setImageResource(R.drawable.dennied);
            }

            switch (arr[7]) {
                case "E":
                    editTextFullName.setText(arr[1]);
                    record.setPerson_fullname(arr[1]);
                    textViewProfile.setText("Empleado");
                    break;
                case "C":
                    editTextFullName.setText(arr[1]);
                    record.setPerson_fullname(arr[1]);
                    textViewProfile.setText("Subcontratista");
                    editTextCompany.setText(arr[3]);
                    editTextCompany.setVisibility(View.VISIBLE);
                    break;
                case "V":
                    textViewProfile.setText("Visita");
                    // Show denied image, but internally setup record as permitted.
                    record.setPerson_is_permitted(1);
                    // If could get the name of pdf417 show it.

                    try {
                        if (!arr[1].isEmpty()) {
                            editTextFullName.setText(arr[1]);
                            record.setPerson_fullname(arr[1]);
                        } else {
                            editTextFullName.setText(name);
                            record.setPerson_fullname(name);
                        }
                    } catch (NullPointerException e) {
                        editTextFullName.setText("");
                        record.setPerson_fullname("");
                        log.writeLog(getApplicationContext(), "Main:line 504", "ERROR", e.getMessage());
                    }

                    // If have company show it.
                    if (!arr[3].isEmpty()) {
                        editTextCompany.setText(arr[3]);
                        editTextCompany.setVisibility(View.VISIBLE);
                    } else {
                        editTextCompany.setVisibility(View.GONE);
                    }
                    break;
            }

            record.setPerson_profile(arr[7]);
            record.setPerson_company(arr[3]);
            record.setPerson_place(arr[4]);
            if (arr[5].equals("null")) arr[5] = "0"; // Card -> For Contractors it 0.
            record.setPerson_company_code(arr[5]);
            record.setPerson_card(Integer.parseInt(arr[6]));
            record.setRecord_sync(0);
            record.setRecord_bus(0);

            if (is_input) {
                record.setRecord_is_input(1);
                record.setRecord_input_datetime(getCurrentDateTime());
            } else {
                record.setRecord_is_input(0);
                record.setRecord_output_datetime(getCurrentDateTime());
            }

            // Save record on local database
            db.add_record(record);
        } catch (ArrayIndexOutOfBoundsException e) {
            mp3Error.start();
            log.writeLog(getApplicationContext(), "Main:line 538", "ERROR", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mp3Error.start();
            log.writeLog(getApplicationContext(), "Main:line 542", "ERROR", e.getMessage());
        }
    }

    public class LoadDbTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            loading.setSpinSpeed(3);
            loading.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(String... params) {
            return DbCall(server + "/api/people?filter[where][is_permitted]=true");
        }

        protected void onProgressUpdate(String... progress) {
            return;
        }

        protected void onPostExecute(String json) {
            // When response its 200, json save data no code.
            log_app log = new log_app();
            if (json != "408" && json != "204") {
                try {
                    db.add_people(json);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    log.writeLog(getApplicationContext(), "Main:line 572", "ERROR", e.getMessage());
                }
            }
            loading.setVisibility(View.GONE);
        }
    }

    public class getLastUpdateTask extends AsyncTask<String, Void, String> {
        private String Updated, postReturn = "";

        public getLastUpdateTask(String s) {
            Updated = s;
        }

        @Override
        protected String doInBackground(String... params) {
            postReturn = GET(Updated);
            return postReturn;
        }

        @Override
        public String toString() {
            return postReturn + "";
        }

        protected void onPostExecute(String result) {
            log_app log = new log_app();
            try {
                // Parse json
                String[] splitter = result.split("\"");
                result = splitter[4];
                result = result.substring(0, result.length() - 1);
                lastUpdated.setText(result);
            } catch (Exception e) {
                log.writeLog(getApplicationContext(), "Main:line 606", "ERROR", e.getMessage());
            }
        }
    }

    public String GET(String url) {
        log_app log = new log_app();
        String result = "";
        InputStream inputStream;
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse;
        try {
            httpResponse = httpclient.execute(httpGet);
            inputStream = httpResponse.getEntity().getContent();

            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = String.valueOf(httpResponse.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.writeLog(getApplicationContext(), "Main:line 630", "ERROR", e.getMessage());
        }
        return result;
    }


    public String DbCall(String dataUrl) {
        log_app log = new log_app();
        String contentAsString;
        URL url;
        HttpURLConnection connection = null;

        try {
            // Create connection
            url = new URL(dataUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.connect();

            int responsecode = connection.getResponseCode();

            // Get Response
            InputStream is = connection.getInputStream();
            if (responsecode != 200) // OK
                contentAsString = String.valueOf(responsecode);
            else {
                contentAsString = convertInputStreamToString(is);
                new getLastUpdateTask(server + "/api/people/getLastUpdate?profile=E").execute().toString();
            }
        } catch (Exception e) {
            log.writeLog(getApplicationContext(), "Main:line 663", "ERROR", e.getMessage());
            contentAsString = "408"; // Request Timeout
        }
        if (connection != null) {
            connection.disconnect();
        }
        if (contentAsString.length() <= 2) { //[]
            contentAsString = "204"; // No content
        }

        return contentAsString;
    }

    public void OfflineRecordsSynchronizer() {
        List<Record> records = db.get_desynchronized_records();
        if (records.size() > 0)
            new RegisterTask(records).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public String POST(Record record, String url) {
        InputStream inputStream;
        String result = "";
        String json = "";
        JSONObject jsonObject = new JSONObject();
        log_app log = new log_app();
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            // 3. build jsonObject from jsonList
            jsonObject.accumulate("run", record.getPerson_run());
            jsonObject.accumulate("fullname", record.getPerson_fullname());
            jsonObject.accumulate("profile", record.getPerson_profile());

            if (record.getPerson_profile().equals("V")) {
                jsonObject.accumulate("is_permitted", true);
            } else {
                if (record.getPerson_is_permitted() == 1)
                    jsonObject.accumulate("is_permitted", true);
                else jsonObject.accumulate("is_permitted", false);
            }

            if (record.getRecord_is_input() == 1) {
                jsonObject.accumulate("is_input", true);
                jsonObject.accumulate("input_datetime", record.getRecord_input_datetime());

            } else {
                jsonObject.accumulate("is_input", false);
                jsonObject.accumulate("output_datetime", record.getRecord_output_datetime());
            }

            jsonObject.accumulate("company", record.getPerson_company());
            if (!record.getPerson_profile().equals("V")) {
                jsonObject.accumulate("place", record.getPerson_place());
                jsonObject.accumulate("company_code", record.getPerson_company_code());
                jsonObject.accumulate("card", record.getPerson_card());
            }

            jsonObject.accumulate("type", "PDA");
            jsonObject.accumulate("PDA", db.get_config_id_pda());

            // 4. convert JSONObject to JSON to String
            if (jsonObject.length() <= 13) { // 13 element on json
                json = jsonObject.toString();
                //Log.i("json to POST", json);

                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);

                // 6. set httpPost Entity
                httpPost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                // 8. Execute POST request to the given URL
                if (!server.equals("http://:0")) { // || record.getRecord_id() != 0 // not update at first record, only after with sync method.
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    // 9. receive response as inputStream
                    inputStream = httpResponse.getEntity().getContent();

                    // 10. convert inputstream to string
                    if (inputStream != null) {
                        result = convertInputStreamToString(inputStream);
                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            // if has sync = 0 its becouse its an offline record to be will synchronized.
                            if (record.getRecord_sync() == 0) {
                                db.update_record(record.getRecord_id());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        loading.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    } else {
                        result = String.valueOf(httpResponse.getStatusLine().getStatusCode());
                    }
                    //result its the json to sent
                    if (result.startsWith("http://"))
                        result = "204"; //no content
                } else {
                    mp3Error.start();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            makeToast("Configure datos del servidor primero");
                        }
                    });
                }
            } else {
                log.writeLog(getApplicationContext(), "Main:line 815", "ERROR", "Missing elements in the json to be posted");
            }
        } catch (HttpHostConnectException hhc) {
            log.writeLog(getApplicationContext(), "Main: POST method", "ERROR", hhc.getMessage().toString());
        } catch (Exception e) {
            log.writeLog(getApplicationContext(), "Main: POST method", "ERROR", e.getMessage().toString());
        }

        return result;
    }

    public class RegisterTask extends AsyncTask<Void, Void, String> {

        private List<Record> newRecord;

        RegisterTask(List<Record> newRecord) {
            this.newRecord = newRecord;
        }

        @Override
        protected String doInBackground(Void... params) {
            String postReturn = "";
            for (int i = 0; i < newRecord.size(); i++) {
                Record record = newRecord.get(i);
                postReturn = POST(record, server + "/api/records/");
            }
            return postReturn;
        }
    }

    public void makeToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}