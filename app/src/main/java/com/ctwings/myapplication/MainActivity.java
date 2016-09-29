package com.ctwings.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.device.ScanManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.pnikosis.materialishprogress.ProgressWheel;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
//import android.nfc.NfcAdapter;

public class MainActivity extends AppCompatActivity {

    private ImageView imageview;
    private EditText editTextRun;
    private EditText editTextFullName;
    private EditText editTextCompany;
    private RadioGroup rdgProfile;
    private RadioButton rdbEmployee;
    private RadioButton rdbContractor;
    private String profile;
    private ProgressWheel loading;
    private static String server;
    private boolean is_input;
    private boolean bus;
    private TextView lastUpdated;

    private final static String SCAN_ACTION = "urovo.rcv.message";//扫描结束action
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private int soundid;
    private String barcodeStr;
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
                writeLog("ERROR", e.toString());
                e.printStackTrace();
            }
        }
        //call the loading library in xml file
        loading = (ProgressWheel) findViewById(R.id.loading);
        loading.setVisibility(View.GONE);

        writeLog("DEBUG", "Application has started Correctly");
        server = "http://192.168.1.101:3000"; // use getSetting();

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        editTextRun = (EditText) findViewById(R.id.editText_run);
        editTextFullName = (EditText) findViewById(R.id.editText_fullname);
        editTextCompany = (EditText) findViewById(R.id.editText_company);
        imageview = (ImageView) findViewById(R.id.imageView);
        mp3Dennied = MediaPlayer.create(MainActivity.this, R.raw.bad);
        mp3Permitted = MediaPlayer.create(MainActivity.this, R.raw.good);
        mp3Error = MediaPlayer.create(MainActivity.this, R.raw.error);
        rdgProfile = (RadioGroup) findViewById(R.id.rdgProfile);
        rdbEmployee = (RadioButton) findViewById(R.id.rdbEmployee);
        rdbContractor = (RadioButton) findViewById(R.id.rdbContractor);
        rdbEmployee.setChecked(true);
        editTextCompany.setVisibility(View.GONE);
        mySwitch = (Switch) findViewById(R.id.mySwitch);
        mySwitch.setChecked(true);
        lastUpdated = (TextView) findViewById(R.id.textView_lastUpdate);
        // set by default
        is_input = true;
        profile = "E";
        bus = false;
        // end set by default

        rdgProfile.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                reset();
                if (checkedId == R.id.rdbEmployee) {
                    profile = "E";
                    bus = false;
                    editTextCompany.setVisibility(View.GONE);
                    mySwitch.setVisibility(View.VISIBLE);
                    lastUpdated.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.rdbVisit) {
                    profile = "V";
                    bus = false;
                    lastUpdated.setVisibility(View.GONE);
                    imageview.setImageDrawable(null);
                    editTextCompany.setVisibility(View.VISIBLE);
                    mySwitch.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.rdbContractor) {
                    profile = "C";
                    bus = false;
                    imageview.setImageDrawable(null);
                    editTextCompany.setVisibility(View.VISIBLE);
                    mySwitch.setVisibility(View.VISIBLE);
                    lastUpdated.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.rdbBus) {
                    profile = "E";
                    bus = true;
                    lastUpdated.setVisibility(View.GONE);
                    imageview.setImageDrawable(null);
                    editTextCompany.setVisibility(View.GONE);
                    mySwitch.setVisibility(View.GONE);
                }
            }
        });

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

//        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
//
//        if(nfcAdapter != null && nfcAdapter.isEnabled()){
//            //get text from chip
//        }else{
//            Toast.makeText(this, "NFC desactivado, porfavor activar!", Toast.LENGTH_LONG).show();
//        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((profile.equals("E") || profile.equals("C")) && !editTextRun.getText().toString().isEmpty()) {
                    getPeople(editTextRun.getText().toString());
                } else if (profile.equals("V") && !editTextRun.getText().toString().isEmpty() &&
                        !editTextFullName.getText().toString().isEmpty()) {
                    //Send to AccessControl API
                    Record record = new Record();

                    record.setPerson_run(editTextRun.getText().toString());
                    record.setPerson_fullname(editTextFullName.getText().toString());
                    record.setPerson_profile(profile);
                    if (is_input) record.setRecord_is_input(1);
                    else record.setRecord_is_input(0);
                    if (bus) record.setRecord_bus(1);
                    else record.setRecord_bus(0);

                    new RegisterTask(record).execute();
                    reset();
                } else {
                    mp3Error.start();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            makeToast("Ingrese datos primero.");
                        }
                    });
                    editTextRun.requestFocus();
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

            // TODO Auto-generated method stub
            if (mp3Error.isPlaying()) mp3Error.stop();
            if (mp3Dennied.isPlaying()) mp3Dennied.stop();
            if (mp3Permitted.isPlaying()) mp3Permitted.stop();

            isScaning = false;
            //soundpool.play(soundid, 1, 1, 0, 0, 1);

            mVibrator.vibrate(100);
            reset();

            byte[] barcode = intent.getByteArrayExtra("barocode");
            int barocodelen = intent.getIntExtra("length", 0);
            //byte temp = intent.getByteExtra("barcodeType", (byte) 0);
            //android.util.Log.i("debug", "----codetype--" + temp);
            barcodeStr = new String(barcode, 0, barocodelen);
            String rawCode = barcodeStr;
            writeLog("Barcode RAW", barcodeStr);
            int flag=0; // 0 for end without k, 1 with k
            int lenght=0;

            if (barcodeStr.startsWith("https")) { // new DNI
                barcodeStr = barcodeStr.substring(52, 62);
                barcodeStr = barcodeStr.substring(0, barcodeStr.indexOf("-"));
                if (profile == "V") {
                    //get name from DNI
                    editTextFullName.setText(" ");
                }
                Log.i("Debugger", "NEW DNI");
            } else if (barcodeStr.startsWith("00")) {
                Log.i("Debugger", "CARD");
            } else if (barcodeStr.contains("ABCDEFGHIJKLMNOPQRSTUVWXYZ")) { // old DNI
                Log.i("Debugger", "OLD DNI");
                barcodeStr = barcodeStr.substring(0, 9);
                Log.i("Old DNI Cutted", barcodeStr);
                barcodeStr = barcodeStr.replace(" ", "");
                if (barcodeStr.endsWith("K")) {
                    barcodeStr = barcodeStr.replace("K", "");
                    flag = 1;
                }
                if (Integer.parseInt(barcodeStr) > 400000000 && flag == 0) {
                    barcodeStr = barcodeStr.substring(0, barcodeStr.length() - 2);
                    Log.i("Debugger", "Oldman");
                    lenght = 9;
                } else if (flag == 0) {
                    lenght = 10;
                    Log.i("Debugger", "Elderly person");
                    barcodeStr = barcodeStr.substring(0, barcodeStr.length() - 1);
                }

                if (profile == "V") {
                    //get name from DNI
                    String[] array = rawCode.split("\\s+");
                    try {
                        editTextFullName.setText(array[1].substring(0, array[1].indexOf("CHL")));
                    } catch (Exception e) {
                        editTextFullName.setText(array[2].substring(0, array[2].indexOf("CHL")));
                    }
                }
            }

            barcodeStr = barcodeStr.replace("k", "");
            barcodeStr = barcodeStr.replace("K", "");

            Log.i("Cooked Barcode", barcodeStr);
            writeLog("Cooked Barcode", barcodeStr);

            try {
                if (profile.equals("E") || profile.equals("C")) {
                    getPeople(barcodeStr);
                } else if (profile.equals("V")) {
                    editTextRun.setText(barcodeStr);

                    //Send to AccessControl API
                    Record record = new Record();

                    if (!editTextFullName.getText().toString().isEmpty())
                        record.setPerson_fullname(editTextFullName.getText().toString());
                    record.setPerson_run(barcodeStr);
                    record.setPerson_profile(profile);
                    if (is_input) record.setRecord_is_input(1);
                    else record.setRecord_is_input(0);
                    if (bus) record.setRecord_bus(1);
                    else record.setRecord_bus(0);

                    new RegisterTask(record).execute();
                    //new GetCompanyTask().execute(server2 + "/api/records/findOne?filter[where][people_run]=" + barcodeStr);
                    mp3Permitted.start();
                }
            } catch (NullPointerException e) {
                Log.e("NullPointer", e.toString());
                writeLog("ERROR", e.toString());
                //new GetPeopleTask().execute(server + "/employee/" + barcodeStr);
            }
        }
    };

    private void initScan() {
        // TODO Auto-generated method stub
        mScanManager = new ScanManager();
        mScanManager.openScanner();

        mScanManager.switchOutputMode(0);
        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
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
        UpdateDb();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SCAN_ACTION);
        registerReceiver(mScanReceiver, filter);
    }

    public void UpdateDb() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //new updateDbFromXml().execute();
                    try {
                        new LoadDbTask().execute();
                        Thread.sleep(60000);
                    } catch (Exception e) {
                        writeLog("ERROR", e.toString());
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    public void reset() {

        initScan();
        editTextRun.setText("");
        editTextFullName.setText("");
        editTextCompany.setText("");
        imageview.setImageDrawable(null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(SCAN_ACTION);
        registerReceiver(mScanReceiver, filter);
    }

    public void clean() {
        barcodeStr = "";
    }

    public void getPeople(String rut) {
        String finalJson = db.get_one_person(rut, profile);
        String[] arr = finalJson.split(";");

        // set edittext here before some exceptions.
        editTextRun.setText(arr[0]);
        editTextFullName.setText(arr[1]);

        //build object with that values, then send to registerTarsk()
        Record record = new Record();
        record.setPerson_run(arr[0]);
        record.setPerson_fullname(arr[1]);
        if (arr[2].equals("true")) {
            mp3Permitted.start();
            //is_permitted = true;
            record.setPerson_is_permitted(1);
            if (is_input)
                imageview.setImageResource(R.drawable.permitted);
        } else {
            mp3Dennied.start();
            //is_permitted = false;
            record.setPerson_is_permitted(0);
            if (is_input)
                imageview.setImageResource(R.drawable.dennied);
        }
        record.setPerson_company(arr[3]);
        record.setPerson_location(arr[4]);
        if (arr[5].equals("null")) arr[5]="0"; // For Contractors
        record.setPerson_company_code(arr[5]);
        record.setPerson_card(Integer.parseInt(arr[6]));
        record.setPerson_profile(arr[7]);

        // fix profile if don't change by user.
        if (arr[7].equals("C") && profile.equals("E")) record.setPerson_profile("C");
        if (arr[7].equals("E") && profile.equals("C")) record.setPerson_profile("E");

        if (bus) record.setRecord_bus(1);
        else record.setRecord_bus(0);

        if (is_input) record.setRecord_is_input(1);
        else record.setRecord_is_input(0);

        editTextFullName.setText(record.getPerson_fullname());

        if (profile.equals("C")) editTextCompany.setText(record.getPerson_company());

        new RegisterTask(record).execute();

        clean();
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

    public class updateDbFromXml extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String xml = DbCall(server + "/api/people/updateDbFromXml");
            if (xml != "408" && xml != "204") {
                //Log.d("xml", xml);
            }else{
                Log.d("Network","Offline");
                writeLog("Network","Offline");
            }
            return xml;
        }
    }

    public class LoadDbTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String data = DbCall(server + "/api/people?filter[where][or][0][profile]=E&filter[where][or][1][profile]=C");
            if (data != "408" && data != "204") {
                db.add_persons(data);
                Log.d("count record desync", String.valueOf(db.record_desync_count()));
                if (db.record_desync_count() >= 1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loading.setEnabled(true);
                            loading.setVisibility(View.VISIBLE);
                        }
                    });
                    OfflineRecordsSynchronizer();
                }
            }
            return "Done";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("Person Count", String.valueOf(db.person_count()));
            writeLog("Person Count", String.valueOf(db.person_count()));
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
            try {
                // Parse json
                String[] splitter=result.split("\"");
                result = splitter[4];
                result = result.substring(0,result.length()-1);
                lastUpdated.setText(result);
            } catch (Exception e) {
                e.printStackTrace();
                writeLog("ERROR", e.toString());
            }
        }

    }

    public String GET(String url) {
        String result = "";
        InputStream inputStream;
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = null;
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
            writeLog("ERROR", e.toString());
        }
        return result;
    }


    public String DbCall(String dataUrl) {

        String contentAsString = "";

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
                //update datetime in textbox from XML file
                new getLastUpdateTask(server + "/api/people/getLastUpdate?profile=" + profile).execute().toString();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            contentAsString = "408"; // Request Timeout
        }
        if (connection != null) {
            connection.disconnect();
        }
        if (contentAsString.length() <= 2) { //[]
            contentAsString = "204"; // No content
        }
        Log.d("Server response", contentAsString);
        writeLog("Server response", contentAsString);

        return contentAsString;
    }

    public void OfflineRecordsSynchronizer() {
        List records = db.get_desynchronized_records();
        Log.d("List record", String.valueOf(records));
        Record record = new Record();
        String[] arr;
        for (int i = 0; i <= records.size() - 1; i++) {
            Log.d("falta sincronizar", records.get(i).toString());
            arr = records.get(i).toString().split(";");
            //get each row to be synchronized
            record.setRecord_id(Integer.parseInt(arr[0]));
            record.setPerson_fullname(arr[1]);
            record.setPerson_run(arr[2]);
            record.setRecord_is_input(Integer.parseInt(arr[3]));
            record.setRecord_bus(Integer.parseInt(arr[4]));
            record.setPerson_is_permitted(Integer.parseInt(arr[5]));
            record.setPerson_company(arr[6]);
            record.setPerson_location(arr[7]);
            record.setPerson_company_code(arr[8]);
            record.setRecord_input_datetime(arr[9]);
            record.setRecord_output_datetime(arr[10]);
            record.setRecord_sync(Integer.parseInt(arr[11]));
            record.setPerson_profile(arr[12]);
            record.setPerson_card(Integer.parseInt(arr[13]));
            new RegisterTask(record).execute();
        }
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
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            // 3. build jsonObject from jsonList
            jsonObject.accumulate("run", record.getPerson_run());
            jsonObject.accumulate("fullname", record.getPerson_fullname());

            if (record.getPerson_is_permitted() == 1)
                jsonObject.accumulate("is_permitted", true);
            else
                jsonObject.accumulate("is_permitted", false);

            jsonObject.accumulate("profile", record.getPerson_profile());

            if (record.getRecord_is_input() == 1)
                jsonObject.accumulate("is_input", true);
            else
                jsonObject.accumulate("is_input", false);

            if (record.getRecord_bus() == 1)
                jsonObject.accumulate("bus", true);
            else
                jsonObject.accumulate("bus", false);

            jsonObject.accumulate("company", record.getPerson_company());
            jsonObject.accumulate("place", record.getPerson_location());
            jsonObject.accumulate("company_code", record.getPerson_company_code());
            jsonObject.accumulate("card", record.getPerson_card());

            // For offline records only
            try {
                if (record.getRecord_input_datetime().length() > 4)
                    jsonObject.accumulate("input_datetime", record.getRecord_input_datetime());
            } catch (NullPointerException npe) {
            }

            try {
                if (record.getRecord_output_datetime().length() > 4)
                    jsonObject.accumulate("output_datetime", record.getRecord_output_datetime());
            } catch (NullPointerException npe) {
            }

            // 4. convert JSONObject to JSON to String
            if (jsonObject.length() <= 13) { // 13 element on json
                json = jsonObject.toString();
                Log.d("json to POST", json);
                writeLog("json to POST", json);

                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);

                // 6. set httpPost Entity
                httpPost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                // 8. Execute POST request to the given URL
                if (!server.equals("http://:0")) {
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    // 9. receive response as inputStream
                    inputStream = httpResponse.getEntity().getContent();

                    // 10. convert inputstream to string
                    if (inputStream != null) {
                        result = convertInputStreamToString(inputStream);

                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                            // if has input or output_datetime its becouse its an offline record to be will synchronized.
                            if (!jsonObject.isNull("input_datetime") || !jsonObject.isNull("output_datetime")) {
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
                    //Toast.makeText(MainActivity.this, "Configure datos del servidor primero", Toast.LENGTH_LONG).show();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            makeToast("Configure datos del servidor primero");
                        }
                    });
                }
            } else {
                Log.d("Json length", "Missing elements in the json to be posted");
                writeLog("Json length", "Missing elements in the json to be posted");
            }

        } catch (Exception e) {
            // Insert records to object, then get from DataBaseHelper to save
            Record offlineRecord = new Record();

            try {
                offlineRecord.setPerson_run(jsonObject.getString("run"));
                offlineRecord.setPerson_fullname(jsonObject.getString("fullname"));
                offlineRecord.setPerson_company(jsonObject.getString("company"));
                offlineRecord.setPerson_company_code(jsonObject.getString("company_code"));
                offlineRecord.setPerson_location(jsonObject.getString("location"));
                offlineRecord.setPerson_card(jsonObject.getInt("card"));
                offlineRecord.setPerson_profile(jsonObject.getString("profile"));

                if (jsonObject.getString("is_input").equals("true"))
                    offlineRecord.setRecord_is_input(1);
                else offlineRecord.setRecord_is_input(0);

                if (jsonObject.getString("bus").equals("true")) offlineRecord.setRecord_bus(1);
                else offlineRecord.setRecord_bus(0);

                if (jsonObject.getString("is_permitted").equals("true"))
                    offlineRecord.setPerson_is_permitted(1);
                else offlineRecord.setPerson_is_permitted(0);

                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
                df.setTimeZone(tz);

                if (is_input) offlineRecord.setRecord_input_datetime(df.format(new Date()));
                else offlineRecord.setRecord_output_datetime(df.format(new Date()));

                offlineRecord.setPerson_profile(jsonObject.getString("profile"));

                db.add_record(offlineRecord);
            } catch (JSONException e1) {
                e1.printStackTrace();
                writeLog("ERROR", e1.toString());
            } catch (SQLException sql) {
                sql.printStackTrace();
                writeLog("ERROR", sql.toString());
            }
        }
        // 11. return result
        return result;
    }

    public class RegisterTask extends AsyncTask<Void, Void, String> {

        private Record newRecord;

        RegisterTask(Record newRecord) {
            this.newRecord = newRecord;
        }

        @Override
        protected String doInBackground(Void... params) {
            return POST(newRecord, server + "/api/records/");
        }

        @Override
        protected void onPostExecute(String s) {
            //makeToast("Persona registrada!");
            clean();

            //update record like a synchronized!
        }
    }

    public void makeToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void writeLog(String LogType, String content) {
        String filename = "AccessControl.log";
        String message = getCurrentDateTime() + " [" + LogType + "]" + ": " + content + "\n";
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_APPEND);
            outputStream.write(message.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            writeLog("ERROR", e.toString());
        }
    }

    public String getCurrentDateTime() {
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("dd-MM-yyy HH:mm:ss");
        String localTime = date.format(currentLocalTime);
        return localTime;
    }
}