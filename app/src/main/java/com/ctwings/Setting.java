package com.ctwings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ctwings.myapplication.DBHelper;
import com.ctwings.myapplication.R;

public class Setting extends AppCompatActivity {
    EditText editText_url, editText_port;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        editText_url = (EditText) findViewById(R.id.editText_url);
        editText_port = (EditText) findViewById(R.id.editText_port);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Save(view);
            }
        });

        Load();
    }

    public void Load(){
        DBHelper databaseHelper = new DBHelper(this, "Sealand", null,1);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        if(db!=null){
            Cursor c = db.rawQuery("select url,port from Setting",null);
            String url="";
            Integer port=0;
            if(c.moveToFirst()) {
                do {
                    url = c.getString(0);
                    port = c.getInt(1);
                } while (c.moveToNext());
            }
            if(url != null) {
                editText_url.setText(url);
                editText_port.setText(Integer.toString(port));
            }
        }
        try {
            //Method invocation 'db.close()' may produce 'java.lang.NullPointerException'
            db.close();
        }catch (NullPointerException n){
            n.printStackTrace();
        }
    }

    public void Save(View view){
        String url = editText_url.getText().toString();
        int port = Integer.parseInt(editText_port.getText().toString());

        DBHelper databaseHelper = new DBHelper(this, "Sealand", null,1);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        if(db!=null){
            ContentValues registro = new ContentValues();
            registro.put("url",url);
            registro.put("port", port);
            int i = db.update("Setting", registro, null, null);
            if(i == 1){
                mVibrator.vibrate(100);
                Toast.makeText(this, "Registro insertado", Toast.LENGTH_SHORT).show();

            }
        }
        try {
            //Method invocation 'db.close()' may produce 'java.lang.NullPointerException'
            db.close();
        }catch (NullPointerException n){
            n.printStackTrace();
        }
    }
}
