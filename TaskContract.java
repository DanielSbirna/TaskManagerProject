package com.example.taskmanager.data;

import android.provider.BaseColumns;

public final class TaskContract {
    private TaskContract() {}

    public static class TaskEntry implements BaseColumns{
        public static final String TABLE_NAME = "Tasks";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_DUE_DATE = "due_date";
        public static final String COLUMN_NAME_DUE_TIME = "due_time";
        public static final String COLUMN_NAME_COMPLETED = "completed"; // 0 = false ; 1 = true;

    }

    public static class UserEntry implements BaseColumns{
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME_FULL_NAME = "full_name";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static final String COLUMN_NAME_PASSWORD = "password"; // Store the hashed password
        public static final String COLUMN_NAME_SALT = "salt"; // salt password
    }
}
