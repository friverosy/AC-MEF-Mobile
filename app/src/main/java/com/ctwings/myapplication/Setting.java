package com.ctwings.myapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Setting extends AppCompatActivity {
    EditText editText_url, editText_port, editText_pda;
    DatabaseHelper db = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        editText_url = (EditText) findViewById(R.id.editText_url);
        editText_port = (EditText) findViewById(R.id.editText_port);
        editText_pda = (EditText) findViewById(R.id.editText_pda);

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
        try {
            if(db != null){
                Cursor c = db.get_config();

                String url = "";
                Integer port = 0;
                Integer id_pda = 0;

                if(c.moveToFirst()) {
                    do {
                        url = c.getString(1);
                        port = c.getInt(2);
                        id_pda = c.getInt(3);
                    } while (c.moveToNext());
                }

                if(!url.isEmpty()) {
                    editText_url.setText(url);
                    editText_port.setText(Integer.toString(port));
                    editText_pda.setText(Integer.toString(id_pda));
                }

                if (!c.isClosed()) c.close();
            }
        } catch (NullPointerException n){
            n.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
    }

    public void Save(View view){

        if (editText_url.getText().toString().isEmpty() ||
                editText_port.getText().toString().isEmpty()){
            Toast.makeText(this, "Ingrese datos del servidor", Toast.LENGTH_SHORT).show();
        } else {
            String url = editText_url.getText().toString();
            int port = Integer.parseInt(editText_port.getText().toString());
            int id_pda = Integer.parseInt(editText_pda.getText().toString());

            db.set_config_url(url);
            db.set_config_port(port);
            db.set_config_id_pda(id_pda);

            Toast.makeText(this, "Configuración guardada!", Toast.LENGTH_LONG).show();
        }
    }
}
