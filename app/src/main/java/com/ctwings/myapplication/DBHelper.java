package com.ctwings.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.EditText;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.Cursor;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by cristtopher on 25-02-16.
 */
public class DBHelper extends SQLiteOpenHelper{
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "AccessControl";

    // Contacts table name
    private static final String TABLE_SETTING = "setting";

    // Setting Table Columns names
    private static final String ID = "id";
    private static final String URL = "url";
    private static final String PORT = "port";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SETTING_TABLE = "CREATE TABLE " + TABLE_SETTING +
                "(" + ID + " INTEGER PRIMARY KEY, " + URL + " TEXT, " +
                      PORT + " INTEGET" + ")";
        db.execSQL(CREATE_SETTING_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    public void addServer(String url, Integer port) {
//        DBHelper dbh = new DBHelper(this, "Sealand", null,1);
//        SQLiteDatabase db = dbh.getWritableDatabase();
//        if(db!=null){
//            try{
//                ContentValues cv = new ContentValues(); //dictionary
//                cv.put(URL, url);
//                cv.put(PORT, port);
//                db.insert(TABLE_SETTING, cv, null, null);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//        try {
//            if(db.isOpen()) db.close();
//        }catch (NullPointerException n){
//            n.printStackTrace();
//        }
    }

    // Updating single contact
    public int updateServer(String url, Integer port) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("url", url);
        cv.put("port", port);

        // updating row
        return db.update(TABLE_SETTING, cv, null, null);
    }
}
