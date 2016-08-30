package com.ctwings.myapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Setting extends AppCompatActivity {
    EditText editText_url, editText_port;
    DatabaseHelper db = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        editText_url = (EditText) findViewById(R.id.editText_url);
        editText_port = (EditText) findViewById(R.id.editText_port);

        Load();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Save(view);
            }
        });
    }

    public void Load(){
//        DBHelper databaseHelper = new DBHelper(this, "Sealand", null,1);
//        SQLiteDatabase db = databaseHelper.getReadableDatabase();
//        if(db != null){
//            Cursor c = db.rawQuery("select * from Setting",null);
//
//            String url="";
//            Integer port=0;
//
//            if(c.moveToFirst()) {
//                Log.d(TAG, c.toString());
//                do {
//                    Log.d("LOAD select ----------", c.getString(0));
//                    url = c.getString(0);
//                    port = c.getInt(1);
//                } while (c.moveToNext());
//            }
//
//            if(!url.isEmpty()) {
//                editText_url.setText(url);
//                editText_port.setText(Integer.toString(port));
//            }
//
//            if (!c.isClosed()) c.close();
//        }
//        try {
//            //Method invocation 'db.close()' may produce 'java.lang.NullPointerException'
//            db.close();
//        }catch (NullPointerException n){
//            n.printStackTrace();
//        }
    }

    public void Save(View view){

        if(editText_url.getText().toString().isEmpty() ||
                editText_port.getText().toString().isEmpty()){
            Toast.makeText(this, "Ingrese datos del servidor", Toast.LENGTH_SHORT).show();
        }else{
            String url = editText_url.getText().toString();
            int port = Integer.parseInt(editText_port.getText().toString());


        }
    }
}
