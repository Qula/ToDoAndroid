package com.example.qula.todolist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.Rating;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import db.TaskContract;
import db.TaskDbHelper;

public class UpdateActivity extends AppCompatActivity {

    private TextView txtName;
    private TextView txtDescription;
    private Intent intent;
    private Button btnUpdate, btnDelete, btnBack, btnZalacznik;
    private Button btnDateClean, btnTimeClean;

    private CheckBox chbStan;
    private RatingBar rbPriorytet;
    Calendar myCalendar;
    private EditText etData, etCzas;
    private String ajdi;
    private TaskDbHelper mHelper;
    private static int RESULT_LOAD_IMG = 1;
    private Context context;

    private LinearLayout layout;
    private ArrayList<ImageView> imageView;
    private ArrayList<String> picList;
    private String imgDecodableString;
    private View.OnLongClickListener l;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        intent = getIntent();
        ajdi = intent.getStringExtra("objectId");
        mHelper = new TaskDbHelper(this);
        imageView = new ArrayList<ImageView>();
        picList = new ArrayList<String>();

        context = getApplicationContext();

        txtName = (TextView) findViewById(R.id.txtName);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnBack = (Button) findViewById(R.id.btnBack);
        chbStan = (CheckBox) findViewById(R.id.chbStan);
        rbPriorytet = (RatingBar) findViewById(R.id.rbPriorytet);
        etData = (EditText) findViewById(R.id.etData);
        etCzas = (EditText) findViewById(R.id.etCzas);
        btnTimeClean = (Button) findViewById(R.id.btnTimeClean);
        btnDateClean = (Button) findViewById(R.id.btnDateClean);
        btnZalacznik = (Button) findViewById(R.id.btnZalacznik);
        layout = (LinearLayout) findViewById(R.id.linear);


        l = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                deletePicFromList(v.getId());
                updatePicList();
                return true;
            }
        };


        btnZalacznik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImagefromGallery(v);
            }
        });

        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID,
                        TaskContract.TaskEntry.COL_TASK_TITLE,
                        TaskContract.TaskEntry.COL_TASK_STAN,
                        TaskContract.TaskEntry.COL_TASK_PRIORYTET,
                        TaskContract.TaskEntry.COL_TASK_DATE_REMINDER,
                        TaskContract.TaskEntry.COL_TASK_DESCRIPTION,
                        TaskContract.TaskEntry.COL_TASK_IMG},
                TaskContract.TaskEntry._ID + "=?" ,
                new String[] {ajdi},
                null, null, null);
        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            int idxStan = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_STAN);
            int idxPriorytet = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_PRIORYTET);
            int idxDate = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_DATE_REMINDER);
            int idxDesc = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_DESCRIPTION);
            int idxImg = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_IMG);

            txtName.setText(cursor.getString(idx));
            txtDescription.setText(cursor.getString(idxDesc));

            if( cursor.getString(idxStan).equals("0") ){
                chbStan.setChecked(false);
            }
            else { chbStan.setChecked(true); }

            rbPriorytet.setRating(Float.parseFloat(cursor.getString(idxPriorytet)));
            String data, czas;
            if(cursor.getString(idxDate).length() > 10){
                data = cursor.getString(idxDate).substring(0, 10);
                czas = cursor.getString(idxDate).substring(11);
            }else{
                data="";
                czas="";
            }
            etData.setText(data);
            etCzas.setText(czas);
            picList = new ArrayList<String>(Arrays.asList(cursor.getString(idxImg).split(" ")));

            for(int i=0; i<picList.size();i++){
                addPicToList(picList.get(i));
            }

            updatePicList();

        }


        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtName.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(),"Podaj nazwę", Toast.LENGTH_SHORT).show();
                }else if(etCzas.getText().toString().isEmpty() && !etData.getText().toString().isEmpty() || etData.getText().toString().isEmpty() && !etCzas.getText().toString().isEmpty() ){
                    Toast.makeText(getApplicationContext(),"Podaj datę i czas", Toast.LENGTH_SHORT).show();
                }else{
                    updateData();
                }
            }
        });


        btnDateClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etData.setText("");
            }
        });

        btnTimeClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCzas.setText("");
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                SQLiteDatabase db = mHelper.getWritableDatabase();
                db.delete(TaskContract.TaskEntry.TABLE,
                        TaskContract.TaskEntry._ID + " = ?",
                        new String[]{ajdi});
                db.close();

                cancelReminder();

                Toast.makeText(getApplicationContext(),
                        "Usunięto zadanie.",
                        Toast.LENGTH_SHORT).show();

                Intent back = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(back);

            }


        });


        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                Intent listActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(listActivity);

            }
        });


        myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        etData.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(UpdateActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        etCzas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                czas();
            }
        });

    }

    private void updateData(){
        ContentValues args = new ContentValues();
        args.put("title", txtName.getText().toString().trim());
        args.put("description", txtDescription.getText().toString().trim());

        if(chbStan.isChecked()){ args.put("stan", "1"); }
        else { args.put("stan", "0"); }
        args.put("priorytet", String.valueOf(rbPriorytet.getRating()));

        if(!etData.getText().toString().trim().isEmpty()){
            args.put("date_reminder", etData.getText().toString() +" "+ etCzas.getText().toString());
            if(chbStan.isChecked()){
                cancelReminder();
            }else{
                setReminder();
            }
        } else {
            args.put("date_reminder", "");
            cancelReminder();
        }

        if(!picList.isEmpty()){
            String temp ="";
            for(int i=0; i<picList.size(); i++){
                temp += picList.get(i) + " ";
            }
            args.put("img", temp);
        }else{
            args.put("img", "f");
        }

        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.update(TaskContract.TaskEntry.TABLE,
                args,
                TaskContract.TaskEntry._ID + "=" + ajdi,
                null);
        db.close();

        Toast.makeText(getApplicationContext(),
                "Zapisano zmiany.",
                Toast.LENGTH_SHORT).show();

        Intent back = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(back);
    }


    private void updateLabel() {

        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        etData.setText(sdf.format(myCalendar.getTime()));
    }

    private void czas(){
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;

        mTimePicker = new TimePickerDialog(UpdateActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                etCzas.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Czas: ");
        mTimePicker.show();
    }

    private void setReminder(){
        Calendar sixCalendar = Calendar.getInstance();
        String data = etData.getText().toString();
        String czas = etCzas.getText().toString();
        sixCalendar.setTimeInMillis(System.currentTimeMillis());
        sixCalendar.set(Integer.parseInt(data.substring(0, 4)), Integer.parseInt(data.substring(5, 7)) - 1, Integer.parseInt(data.substring(8, 10)),
                Integer.parseInt(czas.substring(0, 2)), Integer.parseInt(czas.substring(3,5)), 00);

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("topic", "ToDoList App");
        alarmIntent.putExtra("name", txtName.getText().toString());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(ajdi) , alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, sixCalendar.getTimeInMillis(), pendingIntent);

    }

    private void cancelReminder(){
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(ajdi), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }


    public void loadImagefromGallery(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();

                picList.add(imgDecodableString);
                addPicToList(picList.get(picList.size() - 1));
                updatePicList();

            } else {
                Toast.makeText(this, "Nie wybrano załącznika.",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, ":(", Toast.LENGTH_LONG)
                    .show();
        }

    }

    private void addPicToList(String path){
        imageView.add(new ImageView(this));
        imageView.get( imageView.size()-1 ).setId(imageView.size()-1);
        imageView.get( imageView.size()-1 ).setClickable(false);
        imageView.get( imageView.size()-1 ).setPadding(2, 2, 2, 2);
        imageView.get( imageView.size()-1 ).setImageBitmap((BitmapFactory.decodeFile(path)));
        imageView.get( imageView.size()-1 ).setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.get(imageView.size() - 1).setOnLongClickListener(l);
    }

    private void deletePicFromList(int picId){
        picList.remove(picId);
        imageView.remove(picId);

        for(int i=0; i< imageView.size(); i++){
            imageView.get(i).setId(i);
        }

    }

    private void updatePicList(){
        if(layout.getChildCount() > 0){
            layout.removeAllViews();
        }

        for(int i=0; i< picList.size(); i++){
            layout.addView(imageView.get(i));
        }

    }




}
