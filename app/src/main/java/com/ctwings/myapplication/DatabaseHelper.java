package com.ctwings.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by nicolasmartin on 03-08-16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "mbd";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        // SQL statement to create User table
        String CREATE_PERSON_TABLE = "CREATE TABLE PERSON ( " +
                "person_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "person_fullname TEXT, "+"person_run TEXT, " +
                "person_is_permitted TEXT, " + "person_company TEXT, " +
                "person_location TEXT, " + "person_company_code TEXT" + ")";



        db.execSQL(CREATE_PERSON_TABLE);
        //db.execSQL("PRAGMA foreign_keys=ON;");


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
    private static final String TABLE_PERSON = "person";

    //Person table columns names
    private static final String PERSON_ID = "person_id";
    private static final String PERSON_FULLNAME = "person_fullname";
    private static final String PERSON_RUN = "person_run";
    private static final String PERSON_IS_PERMITTED = "person_is_permitted";
    private static final String PERSON_COMPANY = "person_company";
    private static final String PERSON_LOCATION = "person_location";
    private static final String PERSON_COMPANY_CODE = "person_company_code";

    private static final String[] PERSON_COLUMNS = {PERSON_ID,PERSON_FULLNAME, PERSON_RUN, PERSON_IS_PERMITTED, PERSON_COMPANY, PERSON_LOCATION, PERSON_COMPANY_CODE};


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

                String CREATE_PERSON_TABLE = "CREATE TABLE PERSON ( " +
                        "person_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "person_fullname TEXT, "+"person_run TEXT, " +
                        "person_is_permitted TEXT, " + "person_company TEXT, " +
                        "person_location TEXT, " + "person_company_code TEXT" + ")";

                db.execSQL(CREATE_PERSON_TABLE);

                for(int i = 0; i<json_db_array.length();i++){


                    Person person = new Person(json_db_array.getJSONObject(i).getString("fullname"),
                            json_db_array.getJSONObject(i).getString("run"),
                            json_db_array.getJSONObject(i).getString("is_permitted"),
                            json_db_array.getJSONObject(i).getString("company"),
                            json_db_array.getJSONObject(i).getString("location"),
                            json_db_array.getJSONObject(i).getString("company_code")
                    );

                    ContentValues values = new ContentValues();
                    values.put(PERSON_FULLNAME, person.get_person_fullname());
                    values.put(PERSON_RUN, person.get_person_run());
                    values.put(PERSON_IS_PERMITTED, person.get_person_is_permitted());
                    values.put(PERSON_COMPANY, person.get_person_company());
                    values.put(PERSON_LOCATION, person.get_person_location());
                    values.put(PERSON_COMPANY_CODE, person.get_person_company_code());

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

        db.close();

        // 5. return
        return person;
    }

    public String get_person_by_run(String run){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        String out="null;null;null;null;null";

        // 2. build query
        Cursor cursor =
                db.query(TABLE_PERSON, // a. table
                        PERSON_COLUMNS, // b. column names
                        " person_run = ?", // c. selections
                        new String[] { String.valueOf(run) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        if (cursor != null) {
            if (!(cursor.moveToFirst()) || cursor.getCount() == 0) {
                //cursor is empty
                out = "No encontrado;No encontrado;No encontrado;No encontrado;No encontrado;No encontrado"; //Change to whatever error message.....
            } else {
                // 3. if we got results get the first one
                cursor.moveToFirst();

                // 4. build String
                out = cursor.getString(2) + ";" + cursor.getString(1) + ";" + cursor.getString(3) + ";" + cursor.getString(4) + ";" + cursor.getString(5) + ";" + cursor.getString(6);
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
            //
            Exists = false;

        } else
        {
            //EMPTY
            Exists = true;
        }

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


}
