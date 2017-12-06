package com.example.qula.todolist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import db.TaskContract;
import db.TaskDbHelper;

public class CreateActivity extends AppCompatActivity {

    private Button btnCreateTask, btnBack;
    private TextView txtName, txtDescription;
    private String name, description;

    private TaskDbHelper mHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        btnCreateTask = (Button) findViewById(R.id.btnCreate);
        btnBack = (Button) findViewById(R.id.btnBack);
        txtName = (TextView) findViewById(R.id.txtName);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        mHelper = new TaskDbHelper(this);


        btnCreateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = txtName.getText().toString().trim();
                description = txtDescription.getText().toString().trim();


                if(name.isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            "Proszę podać nazwę zadania",
                            Toast.LENGTH_SHORT).show();
                }else{
                    String task = String.valueOf(txtName.getText()).trim();
                    String desc =  String.valueOf(txtDescription.getText()).trim();
                    SQLiteDatabase db = mHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
                    values.put(TaskContract.TaskEntry.COL_TASK_DESCRIPTION, desc);
                    db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                            null,
                            values,
                            SQLiteDatabase.CONFLICT_REPLACE);
                    db.close();


                    Intent result = new Intent();
                    //result.putExtra(EXTRA_TASK_DESCRIPTION, "yolo");
                    setResult(RESULT_OK, result);

                    Toast.makeText(getApplicationContext(),
                            "Dodano zadanie.",
                            Toast.LENGTH_SHORT).show();

                    finish();
                }


            }
        });




        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                Intent listActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(listActivity);

            }
        });

    }
}
