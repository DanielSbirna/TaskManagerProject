package com.example.taskmanager.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class TaskDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "TaskManager.db";

    private static final String SQL_CREATE_TASKS_ENTRIES =
            "CREATE TABLE " + TaskContract.TaskEntry.TABLE_NAME + " (" +
                    TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY," +
                    TaskContract.TaskEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL," +
                    TaskContract.TaskEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    TaskContract.TaskEntry.COLUMN_NAME_DUE_DATE + " TEXT," +
                    TaskContract.TaskEntry.COLUMN_NAME_DUE_TIME + " TEXT," +
                    TaskContract.TaskEntry.COLUMN_NAME_COMPLETED + " INTEGER DEFAULT 0)";

    private  static final String SQL_CREATE_USERS_ENTRIES =
            "CREATE TABLE " + TaskContract.UserEntry.TABLE_NAME + " (" +
                    TaskContract.UserEntry._ID + " INTEGER PRIMARY KEY," +
                    TaskContract.UserEntry.COLUMN_NAME_USERNAME + " TEXT UNIQUE NOT NULL," +
                    TaskContract.UserEntry.COLUMN_NAME_PASSWORD + " TEXT NOT NULL," +
                    TaskContract.UserEntry.COLUMN_NAME_SALT + " TEXT NOT NULL," +
                    TaskContract.UserEntry.COLUMN_NAME_FULL_NAME + " TEXT)";

    private static final String SQL_DELETE_TASKS_ENTRIES =
            "DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE_NAME;

    private static final String SQL_DELETE_USERS_ENTRIES =
            "DROP TABLE IF EXISTS " + TaskContract.UserEntry.TABLE_NAME;

    public TaskDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TASKS_ENTRIES);
        db.execSQL(SQL_CREATE_USERS_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(SQL_DELETE_TASKS_ENTRIES);
        db.execSQL(SQL_DELETE_USERS_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }
}
