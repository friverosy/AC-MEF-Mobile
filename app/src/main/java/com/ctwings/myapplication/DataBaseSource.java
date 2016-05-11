package com.ctwings.myapplication;

/**
 * Created by jmora on 02/05/2016.
 */
public class DataBaseSource {
    // Contacts table name

    // Setting Table Columns names
    private static final String RECORD_TABLE_NAME = "record";
    public static final String TABLE_SETTING = "setting";
    private static final String STRING_TYPE = "text";
    private static final String INT_TYPE = "integer";


    //creation of field record
    public static class recordTable {
        public static final String RUT = "rut";
        public static final String FULL_NAME = "full_name";
        public static final String DATETIME = "datetime_imput";
    }

    //creation of field settings table
    public static class settingsTable {
        public static final String ID = "id";
        private static final String URL = "url";
        private static final String PORT = "port";
    }

    //script creation of table record
    public static final String CREATE_RECORD_TABLE = "CREATE TABLE" + RECORD_TABLE_NAME + "(" + recordTable.RUT + " " + INT_TYPE + " INTEGER PRIMARY KEY NOT NULL" +
            recordTable.FULL_NAME + " " + STRING_TYPE + "NOT NULL" + recordTable.DATETIME + " " + STRING_TYPE + ")";
    //script creation of table settings
    public static final String CREATE_SETTING_TABLE = "CREATE TABLE " + TABLE_SETTING +
            "(" + settingsTable.ID + " INTEGER PRIMARY KEY, " + settingsTable.URL + " TEXT, " +
            settingsTable.PORT + " INTEGET" + ")";


}



