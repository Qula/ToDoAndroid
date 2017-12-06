package db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDbHelper extends SQLiteOpenHelper {

    public TaskDbHelper(Context context) {
        super(context, TaskContract.DB_NAME, null, TaskContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TaskContract.TaskEntry.TABLE + " ( " +
                TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskContract.TaskEntry.COL_TASK_TITLE + " TEXT NOT NULL," +
                TaskContract.TaskEntry.COL_TASK_STAN + " TEXT NOT NULL DEFAULT '0'," +
                TaskContract.TaskEntry.COL_TASK_PRIORYTET + " TEXT NOT NULL DEFAULT '0.0f'," +
                TaskContract.TaskEntry.COL_TASK_DATE_REMINDER + " TEXT DEFAULT ''," +
                TaskContract.TaskEntry.COL_TASK_DESCRIPTION + " TEXT DEFAULT ''," +
                TaskContract.TaskEntry.COL_TASK_IMG + " TEXT NOT NULL DEFAULT '' )" +
                ";";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE);
        onCreate(db);
    }
}