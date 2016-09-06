package com.ctwings.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nicolasmartin on 03-08-16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 2;
    // Database Name
    private static final String DATABASE_NAME = "mbd";

    // SQL statement to create User table
    String CREATE_PERSON_TABLE = "CREATE TABLE " + TABLE_PERSON + " ( " +
            "person_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "person_fullname TEXT, "+"person_run TEXT, " +
            "person_is_permitted TEXT, " + "person_company TEXT, " +
            "person_location TEXT, " + "person_company_code TEXT, " +
            "person_card INTEGER, " + "person_profile TEXT)";

    String CREATE_RECORD_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_RECORD + " ( " +
            "record_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "person_fullname TEXT, " + "person_run TEXT, " +
            "record_is_input INTEGER, " + "record_bus INTEGER, " +
            "person_is_permitted INTEGER, " + "person_company TEXT, " +
            "person_location TEXT, " + "person_company_code TEXT," +
            "record_input_datetime TEXT, " + "record_output_datetime TEXT, " +
            "record_sync INTEGER,"+ "person_profile TEXT, "+"person_card INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_PERSON_TABLE);

        db.execSQL(CREATE_RECORD_TABLE);
        //db.execSQL("PRAGMA foreign_keys=ON;");

        String CREATE_SETTING_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_SETTING + " (" +
                "id INTEGER PRIMARY KEY, url TEXT, port INTEGET)";

        db.execSQL(CREATE_SETTING_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Drop older tables if it existed
        db.execSQL("DROP TABLE IF EXISTS person");
        //create fresh tables
        this.onCreate(db);

    }

    /**
     * CRUD operations (create "add", read "get", update, delete)
     */

    //Table names
    private static final String TABLE_PERSON = "PERSON";
    private static final String TABLE_RECORD = "RECORD";
    private static final String TABLE_SETTING = "SETTING";

    //Person & Record table columns names
    private static final String PERSON_ID = "person_id";
    private static final String RECORD_ID = "record_id";
    private static final String PERSON_FULLNAME = "person_fullname";
    private static final String PERSON_RUN = "person_run";
    private static final String PERSON_PROFILE = "person_profile";
    private static final String RECORD_IS_INPUT = "record_is_input";
    private static final String RECORD_BUS = "record_bus";
    private static final String PERSON_IS_PERMITTED = "person_is_permitted";
    private static final String PERSON_COMPANY = "person_company";
    private static final String PERSON_LOCATION = "person_location";
    private static final String PERSON_COMPANY_CODE = "person_company_code";
    private static final String RECORD_INPUT_DATETIME = "record_input_datetime";
    private static final String RECORD_OUTPUT_DATETIME = "record_output_datetime";
    private static final String RECORD_SYNC = "record_sync";
    private static final String PERSON_CARD = "person_card";


    // Setting Table Columns names
    private static final String SETTING_ID = "id";
    private static final String SETTING_URL = "url";
    private static final String SETTING_PORT = "port";

    private static final String[] PERSON_COLUMNS = {PERSON_ID, PERSON_FULLNAME, PERSON_RUN, PERSON_IS_PERMITTED, PERSON_COMPANY, PERSON_LOCATION, PERSON_COMPANY_CODE, PERSON_CARD, PERSON_PROFILE};
    private static final String[] RECORD_COLUMNS = {RECORD_ID, PERSON_FULLNAME, PERSON_RUN, RECORD_IS_INPUT, RECORD_BUS, PERSON_IS_PERMITTED, PERSON_COMPANY, PERSON_LOCATION, PERSON_COMPANY_CODE, RECORD_INPUT_DATETIME, RECORD_OUTPUT_DATETIME, RECORD_SYNC, PERSON_PROFILE, PERSON_CARD};


    //Person
    public void add_person(Person person){

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(PERSON_FULLNAME, person.get_person_fullname());
        values.put(PERSON_RUN, person.get_person_run());
        values.put(PERSON_IS_PERMITTED, person.get_person_is_permitted());
        values.put(PERSON_COMPANY, person.get_person_company());
        values.put(PERSON_LOCATION, person.get_person_location());
        values.put(PERSON_COMPANY_CODE, person.get_person_company_code());
        values.put(PERSON_CARD, person.get_person_card());
        values.put(PERSON_PROFILE, person.get_person_profile());

        // 3. insert
        db.insert(TABLE_PERSON, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public void add_persons(String json){

        JSONArray json_db_array;
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            json_db_array = new JSONArray(json);

            db.beginTransaction();
            try {

                db.execSQL("DROP TABLE IF EXISTS person");
                db.execSQL(CREATE_PERSON_TABLE);

                for(int i = 0; i<json_db_array.length();i++){
                    ContentValues values = new ContentValues();
                    try{ // for employees
                        Person person = new Person(json_db_array.getJSONObject(i).getString("fullname"),
                                json_db_array.getJSONObject(i).getString("run"),
                                json_db_array.getJSONObject(i).getString("is_permitted"),
                                json_db_array.getJSONObject(i).getString("company"),
                                json_db_array.getJSONObject(i).getString("location"),
                                json_db_array.getJSONObject(i).getString("company_code"),
                                json_db_array.getJSONObject(i).getInt("card"),
                                json_db_array.getJSONObject(i).getString("profile"));
                        values.put(PERSON_FULLNAME, person.get_person_fullname());
                        values.put(PERSON_RUN, person.get_person_run());
                        values.put(PERSON_IS_PERMITTED, person.get_person_is_permitted());
                        values.put(PERSON_COMPANY, person.get_person_company());
                        values.put(PERSON_LOCATION, person.get_person_location());
                        values.put(PERSON_COMPANY_CODE, person.get_person_company_code());
                        values.put(PERSON_CARD, person.get_person_card());
                        values.put(PERSON_PROFILE, person.get_person_profile());
                    }catch (Exception e){ // for contractors (without card)
                        Person person = new Person(json_db_array.getJSONObject(i).getString("fullname"),
                                json_db_array.getJSONObject(i).getString("run"),
                                json_db_array.getJSONObject(i).getString("is_permitted"),
                                json_db_array.getJSONObject(i).getString("company"),
                                json_db_array.getJSONObject(i).getString("location"),
                                json_db_array.getJSONObject(i).getString("company_code"),0,
                                json_db_array.getJSONObject(i).getString("profile"));
                        values.put(PERSON_FULLNAME, person.get_person_fullname());
                        values.put(PERSON_RUN, person.get_person_run());
                        values.put(PERSON_IS_PERMITTED, person.get_person_is_permitted());
                        values.put(PERSON_COMPANY, person.get_person_company());
                        values.put(PERSON_LOCATION, person.get_person_location());
                        values.put(PERSON_COMPANY_CODE, person.get_person_company_code());
                        values.put(PERSON_PROFILE, person.get_person_profile());
                    }

                    db.insert(TABLE_PERSON, // table
                            null, //nullColumnHack
                            values); // key/value -> keys = column names/ values = column values
                }
                db.setTransactionSuccessful();
            }catch(Exception e) {
                e.printStackTrace();
            }finally {
                db.endTransaction();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        db.close();
    }

    public Person get_person(int id){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_PERSON, // a. table
                        PERSON_COLUMNS, // b. column names
                        " person_id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build object
        Person person = new Person();
        person.set_person_id(Integer.parseInt(cursor.getString(0)));
        person.set_person_fullname(cursor.getString(1));
        person.set_person_run(cursor.getString(2));
        person.set_person_is_permitted(cursor.getString(3));
        person.set_person_company(cursor.getString(4));
        person.set_person_location(cursor.getString(5));
        person.set_person_company_code(cursor.getString(6));
        person.set_person_card(cursor.getInt(7));
        person.set_person_profile(cursor.getString(8));

        cursor.close();
        db.close();

        // 5. return
        return person;
    }

    public String get_one_person(String id){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        String out="null;null;null;null;null";

        Log.d("cantidad de personas",String.valueOf(person_count()));

        //Remove 0 at beginner
        id = String.valueOf(Integer.parseInt(id));

        Log.d("Buscando a ", id);

        // 2. build query
        Cursor cursor =
                db.query(TABLE_PERSON, // a. table
                        PERSON_COLUMNS, // b. column names
                        " person_run = ? OR person_card = ?", // c. selections
                        new String[] { String.valueOf(id), String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        if (cursor != null) {
            if (!(cursor.moveToFirst()) || cursor.getCount() == 0) {
                //cursor is empty
                out = id+";No encontrado;No encontrado;No encontrado;No encontrado;No encontrado;No encontrado; No encontrado; No encontrado"; //Change to whatever error message.....
            } else {
                // 3. if we got results get the first one
                cursor.moveToFirst();

                // 4. build String
                out = cursor.getString(2) + ";" + cursor.getString(1) + ";" +
                        cursor.getString(3) + ";" + cursor.getString(4) + ";" +
                        cursor.getString(5) + ";" + cursor.getString(6) + ";" +
                        cursor.getInt(7) + ";" + cursor.getString(8);
            }
        }
        db.close();

        // 5. return
        return out;
    }

    public boolean person_is_empty(){

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PERSON, null);
        Boolean Exists;

        if (cursor.moveToFirst())
        {
            Exists = false;
        } else
        {
            //EMPTY
            Exists = true;
        }
        cursor.close();

        return Exists;
    }

    // Updating a single Property
    public int update_person(Person property) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("person_fulname", property.get_person_fullname());
        values.put("person_run", property.get_person_run());
        values.put("person_is_permitted", property.get_person_is_permitted());

        // 3. updating row
        int i = db.update(TABLE_PERSON, //table
                values, // column/value
                PERSON_ID+" = ?", // selections
                new String[] { String.valueOf(property.get_person_id()) }); //selection args

        // 4. close
        db.close();

        Log.d("Update Person", property.toString());
        return i;
    }

    // Deleting a single Person
    public void delete_person(int id) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_PERSON,
                PERSON_ID+" = ?",
                new String[] { String.valueOf(id) });

        // 3. close
        db.close();

        Log.d("delete Person", id+"");
    }

    public int person_count(){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM person;", null);
        return cursor.getCount();
    }

    //Records
    public void add_record(Record record){

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(PERSON_FULLNAME, record.getPerson_fullname());
        values.put(PERSON_RUN, record.getPerson_run());
        values.put(RECORD_IS_INPUT, record.getRecord_is_input());
        values.put(RECORD_BUS, record.getRecord_bus());
        values.put(PERSON_IS_PERMITTED, record.getPerson_is_permitted());
        values.put(PERSON_COMPANY, record.getPerson_company());
        values.put(PERSON_LOCATION, record.getPerson_location());
        values.put(PERSON_COMPANY_CODE, record.getPerson_company_code());
        values.put(RECORD_INPUT_DATETIME, record.getRecord_input_datetime());
        values.put(RECORD_OUTPUT_DATETIME, record.getRecord_output_datetime());
        values.put(RECORD_SYNC, 0);
        values.put(PERSON_PROFILE,record.getPerson_profile());
        values.put(PERSON_CARD, record.getPerson_card());

        // 3. insert
        try{
            db.insert(TABLE_RECORD, null, values);
            Log.d("values record insert", String.valueOf(values));
        }catch (SQLException e) {
            Log.e("DataBase Error", "Error al insertar: "+values);
            e.printStackTrace();
        }

        // 4. close
        db.close();
    }

    public List get_records(){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor = //db.rawQuery("SELECT * FROM " + TABLE_RECORD, null);
                db.query(TABLE_RECORD, // a. table
                        RECORD_COLUMNS, // b. column names
                        null, // c. selections
                        null, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. get all
        cursor.moveToFirst();
        List<String> records = new ArrayList<>();

        while (cursor.isAfterLast() == false) {
            records.add(
                    cursor.getInt(0)+";"+ //ID
                            cursor.getString(1)+";"+ //FULLNAME
                            cursor.getString(2)+";"+ //RUN
                            cursor.getInt(3)+";"+ //IS_INPUT
                            cursor.getInt(4)+";"+ //BUS
                            cursor.getInt(5)+";"+ //IS_PERMITTED
                            cursor.getString(6)+";"+ //COMPANY
                            cursor.getString(7)+";"+ //LOCATION
                            cursor.getString(8)+";"+ //COMPANY_CODE
                            cursor.getString(9)+";"+ //INPUT
                            cursor.getString(10)+";"+ //OUTPUT
                            cursor.getInt(11)+";"+ //SYNC
                            cursor.getString(12)+";"+ //PROFILE
                            cursor.getInt(13) //CARD
                    //getInt to boolean type 0 (false), 1 (true)
            );
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        // 5. return
        return records;
    }

    public List get_desynchronized_records(){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor = //db.rawQuery("SELECT * FROM " + TABLE_RECORD, null);
                db.query(TABLE_RECORD, // a. table
                        RECORD_COLUMNS, // b. column names
                        RECORD_SYNC+"=?", // c. selections
                        new String[]{"0"}, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. get all
        cursor.moveToFirst();
        List<String> records = new ArrayList<>();

        while (cursor.isAfterLast() == false) {
            records.add(
                    cursor.getInt(0)+";"+ //ID
                            cursor.getString(1)+";"+ //FULLNAME
                            cursor.getString(2)+";"+ //RUN
                            cursor.getInt(3)+";"+ //IS_INPUT
                            cursor.getInt(4)+";"+ //BUS
                            cursor.getInt(5)+";"+ //IS_PERMITTED
                            cursor.getString(6)+";"+ //COMPANY
                            cursor.getString(7)+";"+ //LOCATION
                            cursor.getString(8)+";"+ //COMPANY_CODE
                            cursor.getString(9)+";"+ //INPUT
                            cursor.getString(10)+";"+ //OUTPUT
                            cursor.getInt(11)+";"+ //SYNC
                            cursor.getString(12)+";"+ //PROFILE
                            cursor.getInt(13) //CARD
                    //getInt to boolean type 0 (false), 1 (true)
            );
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        // 5. return
        return records;
    }

    public int record_count(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM RECORD;", null);
        return cursor.getCount();
    }

    public int record_desysync_count(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM RECORD WHERE record_sync=0;", null);
        return cursor.getCount();
    }

    public int update_record(Integer id) {
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(RECORD_SYNC, 1);

        // 3. updating row
        int i = db.update(TABLE_RECORD, //table
                values, // column/value
                RECORD_ID+"=?", // selections
                new String[] { String.valueOf(id) }); //selection args

        // 4. close
        db.close();

        if(i>0) Log.d("Update record", String.valueOf(id));
        else Log.e("Error updating record", String.valueOf(id));
        return i;
    }

    //Setting
    public void add_server(Server server){

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(SETTING_PORT, server.get_port());
        values.put(SETTING_URL, server.get_url());

        // 3. insert
        db.insert(TABLE_SETTING, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    // Updating single contact
    public int update_server(String url, Integer port) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SETTING_URL, url);
        values.put(SETTING_PORT, port);

        // updating row
        return db.update(TABLE_SETTING,values,null,null);
    }

}
