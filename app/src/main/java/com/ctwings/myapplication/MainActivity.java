package com.ctwings.myapplication;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.device.ScanManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
//import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.KeyEvent;

import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
    private EditText editTextCompany;
    private String runStr;
    private String fullNameStr;
    private String companyStr;
    private String location;
    private String companyCode;
    private RadioGroup rdgProfile;
    private RadioButton rdbEmployee;
    private RadioButton rdbContractor;
    private String profile;

    ProgressDialog pd;

    private static final Logger log = Logger.getLogger(MainActivity.class.getName());
    private static String server;
    private boolean is_permitted;
    private boolean is_input;
    private boolean bus;

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

        pd = new ProgressDialog(MainActivity.this);

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

        LoadSettings();

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        editTextRun = (EditText) findViewById(R.id.editText_run);
        editTextFullName = (EditText) findViewById(R.id.editText_fullname);
        editTextCompany = (EditText) findViewById(R.id.editText_company);
        imageview = (ImageView) findViewById(R.id.imageView);
        mp3Dennied = MediaPlayer.create(MainActivity.this, R.raw.dennied);
        mp3Permitted = MediaPlayer.create(MainActivity.this, R.raw.permitted);
        mp3Error = MediaPlayer.create(MainActivity.this, R.raw.error);
        rdgProfile = (RadioGroup)findViewById(R.id.rdgProfile);
        rdbEmployee = (RadioButton)findViewById(R.id.rdbEmployee);
        rdbContractor = (RadioButton)findViewById(R.id.rdbContractor);
        rdbEmployee.setChecked(true);
        editTextCompany.setVisibility(View.GONE);
        mySwitch = (Switch) findViewById(R.id.mySwitch);
        mySwitch.setChecked(true);

        // set by default
        is_input = true;
        profile = "E";
        bus = false;
        // end set by default

        rdgProfile.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // TODO Auto-generated method stub
                reset();
                if (checkedId == R.id.rdbEmployee){
                    profile = "E";
                    bus = false;
                    editTextCompany.setVisibility(View.GONE);
                    mySwitch.setVisibility(View.VISIBLE);
                }else if (checkedId == R.id.rdbVisit) {
                    profile = "V";
                    bus = false;
                    imageview.setImageDrawable(null);
                    editTextCompany.setVisibility(View.VISIBLE);
                    mySwitch.setVisibility(View.VISIBLE);
                }else if (checkedId == R.id.rdbContractor) {
                    profile = "C";
                    bus = false;
                    imageview.setImageDrawable(null);
                    editTextCompany.setVisibility(View.VISIBLE);
                    mySwitch.setVisibility(View.VISIBLE);
                }else if (checkedId == R.id.rdbBus) {
                    profile = "E";
                    bus = true;
                    imageview.setImageDrawable(null);
                    editTextCompany.setVisibility(View.GONE);
                    mySwitch.setVisibility(View.GONE);
                }
            }
        });

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){ is_input = true; }
                else{ is_input = false; }
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
                try {
                    if(profile.equals("E") || profile.equals("C")) {
                        new GetPeopleTask().execute(editTextFullName.getText().toString());
                    }else if(profile.equals("V") && !editTextRun.getText().toString().isEmpty() &&
                            !editTextFullName.getText().toString().isEmpty()){
                        //Send to AccessControl API
                        Record record = new Record();

                        record.setPerson_run(editTextRun.getText().toString());
                        record.setPerson_fullname(editTextFullName.getText().toString());
                        record.setPerson_profile(profile);
                        if (is_input) record.setRecord_is_input(1);
                        else record.setRecord_is_input(0);
                        if (bus) record.setRecord_bus(1);
                        else record.setRecord_bus(0);
                        // check first if have net...
                        new RegisterTask(record).execute();
                        Toast.makeText(MainActivity.this, "Visita Registrada",
                                Toast.LENGTH_SHORT).show();
                        //onResume();
                        reset();
                    }
                }catch (Exception e) {
                    mp3Error.start();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() { makeToast("Ingrese datos primero.");
                        }
                    });
                    e.printStackTrace();
                }
            }
        });
    }

    public void LoadSettings(){
        server = "http://192.168.123.12:3000";
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
            //onResume();
            reset();
            return true;
        }else if (id == R.id.action_setting) {
            Intent i = new Intent(this, Setting.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    //get visitFullname on 24x7
    private static String getUrlContents(String theUrl, String people_run) {
        StringBuilder content = new StringBuilder();

        // many of these calls can throw exceptions, so i've just
        // wrapped them all in one try/catch statement.
        try
        {
            // create a url object
            URL url = new URL(theUrl);

            // create a urlconnection object
            URLConnection urlConnection = url.openConnection();

            // wrap the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;

            // read from the urlconnection via the bufferedreader
            while ((line = bufferedReader.readLine()) != null)
            {
                content.append(line + "\n");
            }
            bufferedReader.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return content.toString();
    }

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            // TODO Auto-generated method stub
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
            Log.w("Barcode RAW", barcodeStr);
            int flag=0; // 0 for end without k, 1 with k
            int lenght=0;

            if(barcodeStr.startsWith("https")){ // new DNI
                barcodeStr = barcodeStr.substring(52, 62);
                barcodeStr = barcodeStr.substring(0, barcodeStr.indexOf("-"));
                if(profile == "V"){
                    //get name from DNI
                    editTextFullName.setText(" ");
                    //http://datos.24x7.cl/rut/17179347-5/
                    // use webscrapping here
                    //getUrlContents("http://datos.24x7.cl/rut/", barcodeStr)
                    //the from getUrlContents set other params
                }
                Log.i("Debugger","NEW DNI");
            }else if(barcodeStr.startsWith("00")) {
                Log.i("Debugger","CARD");
            }else if(barcodeStr.contains("ABCDEFGHIJKLMNOPQRSTUVWXYZ")){ // old DNI
                Log.i("Debugger","OLD DNI");
                barcodeStr = barcodeStr.substring(0, 9);
                Log.i("Old DNI Cutted",barcodeStr);
                barcodeStr = barcodeStr.replace(" ", "");
                if(barcodeStr.endsWith("K")) {
                    barcodeStr = barcodeStr.replace("K", "");
                    flag = 1;
                }
                if(Integer.parseInt(barcodeStr) > 400000000 && flag == 0){
                    barcodeStr = barcodeStr.substring(0, barcodeStr.length() - 2);
                    Log.i("Debugger","Oldman");
                    lenght=9;
                }else if(flag == 0){
                    lenght=10;
                    Log.i("Debugger","Elderly person");
                    barcodeStr = barcodeStr.substring(0, barcodeStr.length() - 1);
                }

                if(profile == "V"){
                    //get name from DNI
                    String[] array = rawCode.split("\\s+");
                    try{
                        editTextFullName.setText(array[1].substring(0, array[1].indexOf("CHL")));
                    }catch (Exception e){
                        editTextFullName.setText(array[2].substring(0, array[2].indexOf("CHL")));
                    }
                }
            }

            barcodeStr = barcodeStr.replace("k", "");
            barcodeStr = barcodeStr.replace("K", "");

            Log.i("Cooked Barcode", barcodeStr);

            try{
                if(profile.equals("E") || profile.equals("C")) {
                    new GetPeopleTask().execute(barcodeStr);
                }
                else if(profile.equals("V")){
                    editTextRun.setText(barcodeStr);

                    //Send to AccessControl API
                    Record record = new Record();

                    if(!editTextFullName.getText().toString().isEmpty())
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
            }catch(NullPointerException e){
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
        //getApplicationContext().deleteDatabase("mbd");
        // Synch Records also, pending
        UpdateDb();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SCAN_ACTION);
        registerReceiver(mScanReceiver, filter);
    }

    public void UpdateDb(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(true) {
                    new LoadDbTask().execute();
                    Log.d("Update!", "Updating DB");
                    try {
                        // thread to sleep for 60000 milliseconds
                        Thread.sleep(60000);
                    } catch (Exception e) {
                        Log.d("Update!","error sleep");
                    }
                }
            }
        };
        new Thread(runnable).start();
    }

    public void reset(){

        initScan();
        editTextRun.setText("");
        editTextFullName.setText("");
        editTextCompany.setText("");
        imageview.setImageDrawable(null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(SCAN_ACTION);
        registerReceiver(mScanReceiver, filter);
    }

    public void clean(){
        barcodeStr="";
        runStr="";
        fullNameStr="";
        companyStr="";
        location="";
        companyCode="";
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

    /*public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }*/


    public class LoadDbTask extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            /*pd.setTitle("Processing...");
              pd.setMessage("Please wait.");
              pd.setCancelable(false);
              pd.setIndeterminate(true);
              pd.show();*/
        }

        @Override
        protected String doInBackground(String... params) {

            String data = DbCall();
            if(data!="error") {
                db.add_persons(data);
                if (db.record_desysync_count() > 0){
                    OfflineRecordsSynchronizer();
                }
            }else{
                Log.d("Network","Offline");
            }
            return "Done";
        }

        @Override
        protected void onPostExecute(String result) {
            //Log.d("COUNT", db.person_count()+"");
            //Log.d("Person by id", db.get_person(4000)+"");

            //pd.dismiss();
        }
    }

    public String DbCall(){
        String dataUrl = server + "/api/people?filter[where][or][0][profile]=E&filter[where][or][1][profile]=C";
        String contentAsString="";

        URL url;
        HttpURLConnection connection = null;

        try {
            // Create connection
            url = new URL(dataUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.connect();

            //int responsecode = connection.getResponseCode();

            // Get Response
            InputStream is = connection.getInputStream();
            contentAsString = convertInputStreamToString(is);
        } catch (Exception e) {
            e.printStackTrace();
            contentAsString="error";
        }
        if (connection != null) {
            connection.disconnect();
        }
        Log.d("TAG-contentAsString","Server response: "+contentAsString);

        return contentAsString;
    }

    public void OfflineRecordsSynchronizer(){
        int count = db.record_desysync_count();
        if (count > 0) {
            List records = db.get_desynchronized_records();
            //Log.d("Lista", String.valueOf(records));
            Record record = new Record();
            String[] arr;
            for (int i = 0; i <= records.size()-1; i++){
                //Log.d("falta sincronizar", records.get(i).toString());
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
                new RegisterTask(record).execute();
            }
        }else{
            Log.i("Offline", "Nothing to Synchronize");
        }
    }

    //BD query instead of api rest
    public class GetPeopleTask extends AsyncTask<String, String, String>{
        @Override
        protected String doInBackground(String... params) {
            String finalJson = db.get_person_by_run(params[0].toString());

            if(!finalJson.isEmpty()){
                //JSONObject parentObject = new JSONObject(finalJson);
                //return parentObject.getString("people");
                return finalJson;
            }else{
                mp3Error.start();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        makeToast("Error al obtener datos, intente nuevamente");
                    }
                });
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                super.onPostExecute(s);
                String[] arr = s.split(";");

                editTextRun.setText(arr[0]);

                //build object with that values, then send to registerTarsk()
                Record record = new Record();
                record.setPerson_company(arr[3]);
                record.setPerson_run(arr[0]);
                record.setPerson_fullname(arr[1]);
                record.setPerson_location(arr[4]);
                record.setPerson_company_code(arr[5]);

                if (bus) record.setRecord_bus(1);
                else record.setRecord_bus(0);

                if (is_input) record.setRecord_is_input(1);
                else record.setRecord_is_input(0);

                record.setPerson_profile(profile);

                /*runStr = arr[0];
                fullNameStr = arr[1];
                companyStr = arr[3];
                location = arr[4];
                companyCode = arr[5];*/

                editTextFullName.setText(record.getPerson_fullname());
                if(profile.equals("C")) editTextCompany.setText(record.getPerson_company());

                if(arr[2].equals("true")) {
                    //******changed true to 1********
                    mp3Permitted.start();
                    //is_permitted = true;
                    record.setPerson_is_permitted(1);
                    imageview.setImageResource(R.drawable.img_true);
                }else {
                    mp3Dennied.start();
                    //is_permitted = false;
                    record.setPerson_is_permitted(0);
                    imageview.setImageResource(R.drawable.img_false);
                }

                new RegisterTask(record).execute();

                /*runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        makeToast("REGISTERTASK "+runStr+" - "+fullNameStr);
                    }
                });*/

                clean();

            } catch (NullPointerException e){
                mp3Error.start();
                e.printStackTrace();
                //new RegisterTask().execute(server + "/api/records/");
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

    public String POST(Record record, String url){
        InputStream inputStream;
        String result = "";
        String json="";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            // 3. build jsonObject from jsonList
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("people_run", record.getPerson_run());
            jsonObject.accumulate("fullname", record.getPerson_fullname());

            if(record.getPerson_is_permitted() == 1 )
                jsonObject.accumulate("is_permitted", true);
            else
                jsonObject.accumulate("is_permitted", false);

            jsonObject.accumulate("profile", record.getPerson_profile());

            if(record.getRecord_is_input() == 1)
                jsonObject.accumulate("is_input", true);
            else
                jsonObject.accumulate("is_input", false);

            if(record.getRecord_bus() == 1)
                jsonObject.accumulate("bus", true);
            else
                jsonObject.accumulate("bus", false);

            jsonObject.accumulate("company", record.getPerson_company());
            jsonObject.accumulate("location", record.getPerson_location());
            jsonObject.accumulate("company_code", record.getPerson_company_code());

            // For offline records only
            if (record.getRecord_input_datetime() != null)
                jsonObject.accumulate("input_datetime", record.getRecord_input_datetime());
            if (record.getRecord_output_datetime() != null)
                jsonObject.accumulate("output_datetime", record.getRecord_output_datetime());

            // 4. convert JSONObject to JSON to String
            if(jsonObject.length() <= 12){ // 10 element on json
                json = jsonObject.toString();

                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);

                // 6. set httpPost Entity
                httpPost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                // 8. Execute POST request to the given URL
                if(!server.equals("http://:0")) {
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    // 9. receive response as inputStream
                    inputStream = httpResponse.getEntity().getContent();

                    // 10. convert inputstream to string
                    if(inputStream != null)
                        result = convertInputStreamToString(inputStream);
                    else
                        result = "Did not work!";
                    //result its the json to sent
                    if (result.startsWith("http://"))
                        result = "Did not work!";
                    //Log.d("json to POST", result);
                }else{
                    mp3Error.start();
                    //Toast.makeText(MainActivity.this, "Configure datos del servidor primero", Toast.LENGTH_LONG).show();
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            makeToast("Configure datos del servidor primero");
                        }
                    });
                }
            }else{
                Log.d("Json length", "Missing elements in the json");
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Insert records to object, then get from DataBaseHelper to save
            Record offlineRecord = new Record();

            try {
                JSONObject jsonObject = new JSONObject(json);
                offlineRecord.setPerson_run(jsonObject.getString("people_run"));
                offlineRecord.setPerson_fullname(jsonObject.getString("fullname"));
                offlineRecord.setPerson_company(jsonObject.getString("company"));
                offlineRecord.setPerson_company_code(jsonObject.getString("company_code"));
                offlineRecord.setPerson_location(jsonObject.getString("location"));

                if (jsonObject.getString("is_input").equals("true")) offlineRecord.setRecord_is_input(1);
                else offlineRecord.setRecord_is_input(0);

                if (jsonObject.getString("bus").equals("true")) offlineRecord.setRecord_bus(1);
                else offlineRecord.setRecord_bus(0);

                if (jsonObject.getString("is_permitted").equals("true")) offlineRecord.setPerson_is_permitted(1);
                else offlineRecord.setPerson_is_permitted(0);

                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
                df.setTimeZone(tz);

                if(is_input) offlineRecord.setRecord_input_datetime(df.format(new Date()));
                else offlineRecord.setRecord_output_datetime(df.format(new Date()));

                offlineRecord.setPerson_profile(jsonObject.getString("profile"));
                Log.d("offline record", offlineRecord.toString());
                db.add_record(offlineRecord);

                // then must be synchronized.
            } catch (JSONException e1) {
                e1.printStackTrace();
            } catch (SQLException sql) {
                sql.printStackTrace();
            }
        }
        // 11. return result
        return result;
    }

    public class RegisterTask extends AsyncTask<Void, Void, String>{

        private Record newRecord;

        RegisterTask(Record newRecord){
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

            // Update record to synchronized (record_sync = 1)
            Log.d("count", String.valueOf(db.record_desysync_count()));
            if (db.record_desysync_count()>0){
                db.update_record(newRecord.getRecord_id());
            }
        }
    }

    public void makeToast(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}