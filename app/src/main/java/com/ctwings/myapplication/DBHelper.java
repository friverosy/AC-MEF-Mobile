package com.ctwings.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.EditText;

/**
 * Created by cristtopher on 25-02-16.
 */
public class DBHelper extends SQLiteOpenHelper{
    String table="CREATE TABLE Setting(id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT, port INTEGER)";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
