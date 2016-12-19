package com.ctwings.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

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
            "person_is_permitted TEXT, " + "person_company TEXT DEFAULT '', " +
            "person_place TEXT, " + "person_company_code TEXT, " +
            "person_card INTEGER, " + "person_profile TEXT)";

    String CREATE_RECORD_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_RECORD + " ( " +
            "record_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "person_fullname TEXT, " + "person_run TEXT, " +
            "record_is_input INTEGER, " + "record_bus INTEGER, " +
            "person_is_permitted INTEGER, " + "person_company TEXT, " +
            "person_place TEXT, " + "person_company_code TEXT," +
            "record_input_datetime TEXT, " + "record_output_datetime TEXT, " +
            "record_sync INTEGER,"+ "person_profile TEXT, "+"person_card INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_PERSON_TABLE);

        db.execSQL(CREATE_RECORD_TABLE);

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SETTING + " (" +
                "id INTEGER PRIMARY KEY, url TEXT, port INTEGET)");

        db.execSQL("CREATE INDEX people_idx_by_run " +
                "  ON " + TABLE_PERSON + " (" + PERSON_RUN + ");");

        db.execSQL("CREATE INDEX people_idx_by_card " +
                "  ON " + TABLE_PERSON + " (" + PERSON_CARD + ");");

        db.execSQL("CREATE INDEX record_idx_by_sync " +
                " ON " + TABLE_RECORD + " (" + RECORD_SYNC + ");");

        //db.execSQL("PRAGMA foreign_keys=ON;");
        db.execSQL("PRAGMA encoding = 'UTF-8';");
        //db.rawQuery("PRAGMA journal_mode = MEMORY",null);
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
    private static final String PERSON_PLACE = "person_place";
    private static final String PERSON_COMPANY_CODE = "person_company_code";
    private static final String RECORD_INPUT_DATETIME = "record_input_datetime";
    private static final String RECORD_OUTPUT_DATETIME = "record_output_datetime";
    private static final String RECORD_SYNC = "record_sync";
    private static final String PERSON_CARD = "person_card";

    // Setting Table Columns names
    private static final String SETTING_ID = "id";
    private static final String SETTING_URL = "url";
    private static final String SETTING_PORT = "port";

    private static final String[] PERSON_COLUMNS = {PERSON_ID, PERSON_FULLNAME, PERSON_RUN, PERSON_IS_PERMITTED, PERSON_COMPANY, PERSON_PLACE, PERSON_COMPANY_CODE, PERSON_CARD, PERSON_PROFILE};
    private static final String[] RECORD_COLUMNS = {RECORD_ID, PERSON_FULLNAME, PERSON_RUN, RECORD_IS_INPUT, RECORD_BUS, PERSON_IS_PERMITTED, PERSON_COMPANY, PERSON_PLACE, PERSON_COMPANY_CODE, RECORD_INPUT_DATETIME, RECORD_OUTPUT_DATETIME, RECORD_SYNC, PERSON_PROFILE, PERSON_CARD};


    //Person
    public void add_people(String json){

        JSONArray json_db_array;
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            json_db_array = new JSONArray(json);

            db.delete(TABLE_PERSON, null, null);

            for (int i = 0; i<json_db_array.length();i++) {
                ContentValues values = new ContentValues();

                try { // for employees
                    values.put(PERSON_RUN, json_db_array.getJSONObject(i).getString("run"));
                    values.put(PERSON_PROFILE, json_db_array.getJSONObject(i).getString("profile"));
                    values.put(PERSON_IS_PERMITTED, json_db_array.getJSONObject(i).getString("is_permitted"));

                    switch (json_db_array.getJSONObject(i).getString("profile")) {
                        case "E":
                            // Employee
                            values.put(PERSON_FULLNAME, json_db_array.getJSONObject(i).getString("fullname"));
                            values.put(PERSON_COMPANY, json_db_array.getJSONObject(i).getString("company"));
                            values.put(PERSON_COMPANY_CODE, json_db_array.getJSONObject(i).getString("company_code"));
                            values.put(PERSON_PLACE, json_db_array.getJSONObject(i).getString("place"));
                            values.put(PERSON_CARD, json_db_array.getJSONObject(i).getString("card"));
                            break;
                        case "C":
                            // Contactor
                            values.put(PERSON_FULLNAME, json_db_array.getJSONObject(i).getString("fullname"));
                            values.put(PERSON_COMPANY, json_db_array.getJSONObject(i).getString("company"));
                            values.put(PERSON_COMPANY_CODE, json_db_array.getJSONObject(i).getString("company_code"));
                            break;
                        case "V":
                            // Visit
                            if (!json_db_array.getJSONObject(i).getString("fullname").isEmpty())
                                values.put(PERSON_FULLNAME, json_db_array.getJSONObject(i).getString("fullname"));
                            if (!json_db_array.getJSONObject(i).getString("company").isEmpty())
                                values.put(PERSON_COMPANY, json_db_array.getJSONObject(i).getString("company"));
                            break;
                        default:
                            break;
                    }


                } catch (Exception e){ // for contractors (without card)
                    Person person = new Person(json_db_array.getJSONObject(i).getString("fullname"),
                            json_db_array.getJSONObject(i).getString("id"),
                            json_db_array.getJSONObject(i).getString("is_permitted"),
                            json_db_array.getJSONObject(i).getString("company"),
                            json_db_array.getJSONObject(i).getString("place"),
                            json_db_array.getJSONObject(i).getString("company_code"),0,
                            json_db_array.getJSONObject(i).getString("profile"));
                    values.put(PERSON_FULLNAME, person.get_person_fullname());
                    values.put(PERSON_RUN, person.get_person_run());
                    values.put(PERSON_IS_PERMITTED, person.get_person_is_permitted());
                    values.put(PERSON_COMPANY, person.get_person_company());
                    values.put(PERSON_PLACE, person.get_person_place());
                    values.put(PERSON_COMPANY_CODE, person.get_person_company_code());
                    values.put(PERSON_PROFILE, person.get_person_profile());
                }

                db.insert(TABLE_PERSON, // table
                        null, //nullColumnHack
                        values); // key/value -> keys = column names/ values = column values
            }
            db.setTransactionSuccessful();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch(IllegalStateException ise){
            ise.printStackTrace();
        } catch (SQLiteDatabaseLockedException qdle) {
            qdle.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        db.close();
    }

    public String get_one_person(String id){
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        String out="";

        try {
            //db.beginTransaction();
            id.replace("%", ""); // Remove 0 at beginner
            id = String.valueOf(Integer.parseInt(id));

            // 2. build query
            Cursor cursor =
                    db.query(TABLE_PERSON, // a. table
                            PERSON_COLUMNS, // b. column names
                            " person_run = ? OR person_card = ?", // c. selections
                            new String[]{String.valueOf(id), String.valueOf(id)}, // d. selections args
                            null, // e. group by
                            null, // f. having
                            null, // g. order by
                            null); // h. limit

            if (cursor != null) {
                if (!(cursor.moveToFirst()) || cursor.getCount() == 0) {
                    //cursor is empty
                    out = id + ";;;;;0;0;V;";
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
            cursor.close();
            //db.setTransactionSuccessful();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SQLiteDatabaseLockedException sdle) {
            sdle.printStackTrace();
        } catch (NumberFormatException nfe){
            nfe.printStackTrace();
        }
        finally {
            //db.endTransaction();
        }

        db.close();

        // 5. return
        return out;
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
        values.put(PERSON_PLACE, record.getPerson_place());
        values.put(PERSON_COMPANY_CODE, record.getPerson_company_code());
        if(record.getRecord_input_datetime() != null)
            values.put(RECORD_INPUT_DATETIME, record.getRecord_input_datetime());
        if(record.getRecord_output_datetime() != null)
            values.put(RECORD_OUTPUT_DATETIME, record.getRecord_output_datetime());
        values.put(RECORD_SYNC, record.getRecord_sync());
        values.put(PERSON_PROFILE,record.getPerson_profile());
        values.put(PERSON_CARD, record.getPerson_card());

        // 3. insert
        try {
            db.insert(TABLE_RECORD, null, values);
        } catch (SQLException e) {
            Log.e("DataBase Error", "Error to insert record: "+values);
            e.printStackTrace();
        }

        // 4. close
        db.close();
    }

    public List get_desynchronized_records(){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> records = new ArrayList<>();
        try {
            // 2. build query
            Cursor cursor = //db.rawQuery("SELECT * FROM " + TABLE_RECORD, null);
                    db.query(TABLE_RECORD, // a. table
                            RECORD_COLUMNS, // b. column names
                            RECORD_SYNC + "=0", // c. selections
                            null, // d. selections args
                            null, // e. group by
                            null, // f. having
                            null, // g. order by
                            null); // h. limit

            // 3. get all
            cursor.moveToFirst();

            while (cursor.isAfterLast() == false) {
                records.add(
                        cursor.getInt(0) + ";" + //ID
                                cursor.getString(1) + ";" + //FULLNAME
                                cursor.getString(2) + ";" + //RUN
                                cursor.getInt(3) + ";" + //IS_INPUT
                                cursor.getInt(4) + ";" + //BUS
                                cursor.getInt(5) + ";" + //IS_PERMITTED
                                cursor.getString(6) + ";" + //COMPANY
                                cursor.getString(7) + ";" + //PLACE
                                cursor.getString(8) + ";" + //COMPANY_CODE
                                cursor.getString(9) + ";" + //INPUT
                                cursor.getString(10) + ";" + //OUTPUT
                                cursor.getInt(11) + ";" + //SYNC
                                cursor.getString(12) + ";" + //PROFILE
                                cursor.getInt(13) //CARD
                        //getInt to boolean type 0 (false), 1 (true)
                );
                cursor.moveToNext();
            }

            cursor.close();
        } catch (SQLException e) {
                Log.e("DataBase Error", e.getMessage().toString());
                e.printStackTrace();
        }
        db.close();

        // 5. return
        return records;
    }

    public int record_desync_count(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + RECORD_ID + " FROM " + TABLE_RECORD +
                " WHERE " + RECORD_SYNC + "=0;", null);
        return cursor.getCount();
    }

    public int people_count(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + PERSON_ID + " FROM " + TABLE_PERSON, null);
        return cursor.getCount();
    }

    public int employees_count(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + PERSON_ID + " FROM " + TABLE_PERSON +
                " WHERE " + PERSON_PROFILE + "= 'E';", null);
        return cursor.getCount();
    }

    public int contractors_count(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + PERSON_ID + " FROM " + TABLE_PERSON + " " +
                "WHERE " + PERSON_PROFILE + "= 'C';", null);
        return cursor.getCount();
    }

    public int visits_count(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + PERSON_ID + " FROM " + TABLE_PERSON + " " +
                "WHERE " + PERSON_PROFILE + "= 'V';", null);
        return cursor.getCount();
    }

    public void clean_people(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PERSON, null, null);
    }

    public void clean_records(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECORD, null, null);
    }

    public void update_record(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(RECORD_SYNC, 1);

            // 3. updating row
            int i = db.update(TABLE_RECORD, //table
                    values, // column/value
                    RECORD_ID + "=" + id, // where
                    null);

            // 4. close
            db.close();
            if (i > 0) Log.i("Local Record updated", String.valueOf(id));
            else Log.e("Error updating record", String.valueOf(id));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
