package db;

/**
 * Created by Qula on 24.05.2016.
 */
import android.provider.BaseColumns;

public class TaskContract {
    public static final String DB_NAME = "com.qula.todolist.db";
    public static final int DB_VERSION = 1;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "tasks";

        public static final String COL_TASK_TITLE = "title";
        public static final String COL_TASK_STAN = "stan";
        public static final String COL_TASK_PRIORYTET = "priorytet";
        public static final String COL_TASK_DATE_REMINDER = "date_reminder";
        public static final String COL_TASK_DESCRIPTION = "description";
        public static final String COL_TASK_IMG = "img";
    }
}