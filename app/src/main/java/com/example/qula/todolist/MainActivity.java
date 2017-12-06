package com.example.qula.todolist;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import db.TaskContract;
import db.TaskDbHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private String orderBy;
    private final int ADD_TASK_REQUEST = 1;
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;
    private List<String> idList;
    private List<String> saveList;

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHelper = new TaskDbHelper(this);
        idList = new ArrayList<>();
        saveList = new ArrayList<>();
        mTaskListView = (ListView) findViewById(R.id.list_todo);
        settings = getSharedPreferences("sort_settings", MODE_PRIVATE);


        if(settings.contains("sort")){
            orderBy = settings.getString("sort", orderBy);
        }else{
            orderBy = "NULL";
        }

        updateUI();

        AdapterView.OnItemClickListener l = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getBaseContext(), UpdateActivity.class);
                intent.putExtra("objectId", idList.get(position));

                startActivity(intent);
            }
        };

        mTaskListView.setOnItemClickListener(l);
        verifyStoragePermissions(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                Intent intent = new Intent(MainActivity.this, CreateActivity.class);
                startActivityForResult(intent, ADD_TASK_REQUEST);
                return true;

            case R.id.action_sort:
                List<String> sortList = new ArrayList<String>();
                sortList.add("Data dodania");
                sortList.add("Data zakończenia");
                sortList.add("Priorytet");
                sortList.add("Nazwa");
                sortList.add("Status");
                final CharSequence[] sortSeq = sortList.toArray(new String[sortList.size()]);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle("Sortuj według:");
                dialogBuilder.setItems(sortSeq, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if(item == 0){
                            if(orderBy.contains("ASC")){
                                orderBy = TaskContract.TaskEntry._ID +" DESC";
                            }else{
                                orderBy = TaskContract.TaskEntry._ID +" ASC";
                            }
                        }
                        else if(item == 1) {
                            if(orderBy.contains("ASC")){
                                orderBy = TaskContract.TaskEntry.COL_TASK_DATE_REMINDER +" DESC";
                            }else{
                                orderBy = TaskContract.TaskEntry.COL_TASK_DATE_REMINDER +" ASC";
                            }
                        }
                        else if(item == 2) {
                            if(orderBy.contains("ASC")){
                                orderBy = TaskContract.TaskEntry.COL_TASK_PRIORYTET +" DESC";
                            }else{
                                orderBy = TaskContract.TaskEntry.COL_TASK_PRIORYTET+" ASC";
                            }
                        }
                        else if(item == 3) {
                            if(orderBy.contains("ASC")){
                                orderBy = TaskContract.TaskEntry.COL_TASK_TITLE +" COLLATE NOCASE DESC";
                            }else{
                                orderBy = TaskContract.TaskEntry.COL_TASK_TITLE +" COLLATE NOCASE ASC";
                            }
                        }
                        else if(item == 4) {
                            if(orderBy.contains("ASC")){
                                orderBy = TaskContract.TaskEntry.COL_TASK_STAN +" DESC";
                            }else{
                                orderBy = TaskContract.TaskEntry.COL_TASK_STAN +" ASC";
                            }
                        }
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("sort", orderBy);
                        editor.apply();

                        updateUI();

                        if(orderBy.contains("ASC")){
                            Toast.makeText(getApplicationContext(), "Rosnąco", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "Malejąco", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                AlertDialog alertDialogObject = dialogBuilder.create();
                alertDialogObject.show();
                return  true;

            case R.id.action_save:
                try {
                    File sdDir = Environment.getExternalStorageDirectory();
                    File myFile = new File(sdDir+"/todolist.txt");
                    myFile.createNewFile();
                    FileOutputStream fOut = new FileOutputStream(myFile);
                    OutputStreamWriter myOutWriter =
                            new OutputStreamWriter(fOut);

                    saveDb();
                    for(int i=0; i<saveList.size(); i++){
                        myOutWriter.append(saveList.get(i));
                    }

                    myOutWriter.close();
                    fOut.close();
                    Toast.makeText(getBaseContext(),
                            "Zapisano do 'todolist.txt'",
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_TASK_REQUEST) {
            if (resultCode == RESULT_OK) {
                updateUI();
            }
        }
    }

    private void updateUI(){
        Log.d(TAG, "UPDATE LISTY");

        idList.clear();
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                null, null, null, null, null, orderBy);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);

            String tmp = cursor.getString(idx).replace("\n"," ").replace("\r", "");
            if(tmp.length() > 20){
                taskList.add(tmp.substring(0, 20));
            }else {
                taskList.add(tmp);
            }
            idList.add(cursor.getString(0));
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    taskList);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    private void saveDb(){
        saveList.clear();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            saveList.add(cursor.getString(0)+ ", "
                    + cursor.getString(1) + ", "
                    + cursor.getString(5) + ", "
                    + cursor.getString(2) + ", "
                    + cursor.getString(3) + ", "
                    + cursor.getString(4) + "\r\n" );
        }
        cursor.close();
        db.close();
    }


}
