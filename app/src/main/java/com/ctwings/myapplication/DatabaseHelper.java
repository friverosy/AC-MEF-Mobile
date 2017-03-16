package com.ctwings.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    //Instance of Helper to keep one conection all time
    private static DatabaseHelper sInstance;

    // Database Version
    private static final int DATABASE_VERSION = 2;
    // Database Name
    private static final String DATABASE_NAME = "MultiexportFoods";
    //get context for use
    private Context context;

    public static synchronized DatabaseHelper getInstance(Context context) {
        //one single instance of DB
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    // SQL statement to create User table
    String CREATE_PERSON_TABLE = "CREATE TABLE " + TABLE_PERSON + " ( " +
            "person_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "person_fullname TEXT, " + "person_run TEXT, " +
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
            "record_sync INTEGER," + "person_profile TEXT, " + "person_card INTEGER, " +
            "UNIQUE (record_input_datetime,record_output_datetime)) ";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_PERSON_TABLE);

        db.execSQL(CREATE_RECORD_TABLE);

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SETTING + " (" +
                "id INTEGER PRIMARY KEY, url TEXT, port INTEGET, id_pda INTEGER);");

        db.execSQL("CREATE INDEX people_idx_by_run " +
                "  ON " + TABLE_PERSON + " (" + PERSON_RUN + ");");

        db.execSQL("CREATE INDEX people_idx_by_card " +
                "  ON " + TABLE_PERSON + " (" + PERSON_CARD + ");");

        db.execSQL("CREATE INDEX record_idx_by_sync " +
                " ON " + TABLE_RECORD + " (" + RECORD_SYNC + ");");

        //seed
        db.execSQL("INSERT INTO " + TABLE_SETTING + " (id_pda) VALUES (0);");
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
    public void add_people(String json) {
        log_app log = new log_app();
        Log.i("---", "start");

        JSONArray json_db_array;
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            json_db_array = new JSONArray(json);

            db.delete(TABLE_PERSON, null, null);

            String sql = "";

            for (int i = 0; i < json_db_array.length(); i++) {
                ContentValues values = new ContentValues();
                try {
                    values.put(PERSON_RUN, json_db_array.getJSONObject(i).getString("run"));
                    values.put(PERSON_PROFILE, json_db_array.getJSONObject(i).getString("profile"));

                    switch (json_db_array.getJSONObject(i).getString("profile")) {
                        case "E": // Employee
                            values.put(PERSON_FULLNAME, json_db_array.getJSONObject(i).getString("fullname"));
                            values.put(PERSON_COMPANY, json_db_array.getJSONObject(i).getString("company"));
                            values.put(PERSON_COMPANY_CODE, json_db_array.getJSONObject(i).getString("company_code"));
                            values.put(PERSON_PLACE, json_db_array.getJSONObject(i).getString("place"));
                            values.put(PERSON_CARD, json_db_array.getJSONObject(i).getString("card"));
                            values.put(PERSON_IS_PERMITTED, json_db_array.getJSONObject(i).getString("is_permitted"));
                            break;
                        case "C": // Contactor
                            values.put(PERSON_FULLNAME, json_db_array.getJSONObject(i).getString("fullname"));
                            values.put(PERSON_COMPANY, json_db_array.getJSONObject(i).getString("company"));
                            values.put(PERSON_COMPANY_CODE, json_db_array.getJSONObject(i).getString("company_code"));
                            values.put(PERSON_IS_PERMITTED, json_db_array.getJSONObject(i).getString("is_permitted"));
                            break;
                        case "V": // Visit
                            if (!json_db_array.getJSONObject(i).getString("fullname").isEmpty())
                                values.put(PERSON_FULLNAME, json_db_array.getJSONObject(i).getString("fullname"));
                            if (!json_db_array.getJSONObject(i).getString("company").isEmpty())
                                values.put(PERSON_COMPANY, json_db_array.getJSONObject(i).getString("company"));
                            break;
                        default:
                            break;
                    }
                    db.insert(TABLE_PERSON, // table
                            null, //nullColumnHack
                            values); // key/value -> keys = column names/ values = column values

                } catch (Exception e) {
                    Log.e("json", json_db_array.getJSONObject(i).toString());
                    Log.e("ERROR", e.getMessage().toString());
                }
            }
            db.setTransactionSuccessful();
        } catch (JSONException e) {
            log.writeLog(context, "DBhelper:line 182", "ERROR", e.getMessage());
        } catch (IllegalStateException e) {
            log.writeLog(context, "DBhelper:line 184", "ERROR", e.getMessage());
        } catch (SQLiteDatabaseLockedException e) {
            log.writeLog(context, "DBhelper:line 186", "ERROR", e.getMessage());
        } catch (Exception e) {
            log.writeLog(context, "DBhelper:line 186", "ERROR", e.getMessage());
        } finally {
            db.endTransaction();
        }
        Log.i("---", "end");
    }

    public String get_one_person(String id) {
        //Log.i("get_one_person(id)", id);
        // 1. get reference to readable DB
        log_app log = new log_app();
        SQLiteDatabase db = getWritableDatabase();
        String out = "";
        Cursor cursor = null;
        try {
            //db.beginTransaction();
            id.replace("%", ""); // Remove 0 at beginner
            //id = String.valueOf(Integer.parseInt(id));

            // 2. build query
            cursor =
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
            //cursor.close();
        } catch (IllegalStateException e) {
            log.writeLog(context, "DBhelper:line 238", "ERROR", e.getMessage());
        } catch (SQLiteDatabaseLockedException e) {
            log.writeLog(context, "DBhelper:line 238", "ERROR", e.getMessage());
        } catch (NumberFormatException e) {
            log.writeLog(context, "DBhelper:line 238", "ERROR", e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        // db.close();
        // 5. return
        return out;
    }

    //Records
    public void add_record(Record record) {
        log_app log = new log_app();
        //Log.i("add_record(record)", record.toString());
        // 1. get reference to writable DB
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        // 3. insert
        try {
            // 2. create ContentValues to add key "column"/value
            values.put(PERSON_FULLNAME, record.getPerson_fullname());
            values.put(PERSON_RUN, record.getPerson_run());
            values.put(RECORD_IS_INPUT, record.getRecord_is_input());
            values.put(RECORD_BUS, record.getRecord_bus());
            values.put(PERSON_IS_PERMITTED, record.getPerson_is_permitted());
            values.put(PERSON_COMPANY, record.getPerson_company());
            values.put(PERSON_PLACE, record.getPerson_place());
            values.put(PERSON_COMPANY_CODE, record.getPerson_company_code());
            if (record.getRecord_input_datetime() != null)
                values.put(RECORD_INPUT_DATETIME, record.getRecord_input_datetime());
            if (record.getRecord_output_datetime() != null)
                values.put(RECORD_OUTPUT_DATETIME, record.getRecord_output_datetime());
            values.put(RECORD_SYNC, record.getRecord_sync());
            values.put(PERSON_PROFILE, record.getPerson_profile());
            values.put(PERSON_CARD, record.getPerson_card());
            db.insertWithOnConflict(TABLE_RECORD, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("DataBase Error", "Error to insert record: " + values);
            log.writeLog(context, "DBhelper:line 283", "ERROR", e.getMessage());
        } finally {
            db.endTransaction();
        }

        // 4. close
        //db.close();
    }

    public List<Record> get_desynchronized_records() {

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getWritableDatabase();
        log_app log = new log_app();
        List<Record> records = new ArrayList<>();
        Cursor cursor = null;
        try {
            // 2. build query
            cursor = //db.rawQuery("SELECT * FROM " + TABLE_RECORD, null);
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

            while (!cursor.isAfterLast()) {
                Record record = new Record();
                record.setRecord_id(cursor.getInt(0));                  // ID
                record.setPerson_fullname(cursor.getString(1));         // FULLNAME
                record.setPerson_run(cursor.getString(2));              // RUN
                record.setRecord_is_input(cursor.getInt(3));            // IS_INPUT
                record.setRecord_bus(cursor.getInt(4));                 // BUS
                record.setPerson_is_permitted(cursor.getInt(5));        // IS_PERMITTED
                record.setPerson_company(cursor.getString(6));          // COMPANY
                record.setPerson_place(cursor.getString(7));            // PLACE
                record.setPerson_company_code(cursor.getString(8));     // COMPANY_CODE
                record.setRecord_input_datetime(cursor.getString(9));   // INPUT_DATETIME
                record.setRecord_output_datetime(cursor.getString(10)); // INPUT_DATETIME
                record.setRecord_sync(cursor.getInt(11));               // SYNC
                record.setPerson_profile(cursor.getString(12));         // PROFILE
                record.setPerson_card(cursor.getInt(13));               // CARD

                records.add(record);
                cursor.moveToNext();
            }
        } catch (SQLException e) {
            Log.e("DataBase Error", e.getMessage().toString());
            log.writeLog(context, "DBhelper:line 238", "ERROR", e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        //db.close();

        // 5. return
        return records;
    }

    public int record_desync_count() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + RECORD_ID + " FROM " + TABLE_RECORD +
                " WHERE " + RECORD_SYNC + "=0;", null);
        return cursor.getCount();
    }

    public int people_count() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + PERSON_ID + " FROM " + TABLE_PERSON, null);
        return cursor.getCount();
    }

    public int employees_count() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + PERSON_ID + " FROM " + TABLE_PERSON +
                " WHERE " + PERSON_PROFILE + "= 'E';", null);
        return cursor.getCount();
    }

    public int contractors_count() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + PERSON_ID + " FROM " + TABLE_PERSON + " " +
                "WHERE " + PERSON_PROFILE + "= 'C';", null);
        return cursor.getCount();
    }

    public int visits_count() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + PERSON_ID + " FROM " + TABLE_PERSON + " " +
                "WHERE " + PERSON_PROFILE + "= 'V';", null);
        return cursor.getCount();
    }

    public void clean_people() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PERSON, null, null);
    }

    public void clean_records() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECORD, null, null);
    }

    public void update_record(int id) {
        //Log.i("update_record(int id)", String.valueOf(id));
        log_app log = new log_app();
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(RECORD_SYNC, 1);

            // 3. updating row
            int i = db.update(TABLE_RECORD, //table
                    values, // column/value
                    RECORD_ID + "=" + id, // where
                    null);

            // 4. close
            //db.close();
            if (i == 0) Log.e("Error updating record", String.valueOf(id));
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            log.writeLog(context, "DBhelper:line 405", "ERROR", e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    public Cursor get_config() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SETTING, null);
        return cursor;
    }

    public int get_config_id_pda() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT id_pda FROM " + TABLE_SETTING, null);
        if (cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex("id_pda"));
        } else {
            return 0;
        }
    }

    public String get_config_url() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT url FROM " + TABLE_SETTING, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex("id_pda"));
        } else {
            return "";
        }
    }

    public int get_config_port() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT port FROM " + TABLE_SETTING, null);
        if (cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex("port"));
        } else {
            return 0;
        }
    }

    public void set_config_url(String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("url", url);
        db.update(TABLE_SETTING, cv, null, null);
    }

    public void set_config_port(int port) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("port", port);
        db.update(TABLE_SETTING, cv, null, null);
    }

    public void set_config_id_pda(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        Log.i("puting", String.valueOf(id));
        cv.put("id_pda", id);
        db.update(TABLE_SETTING, cv, null, null);
    }
}
