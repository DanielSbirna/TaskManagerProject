package com.example.taskmanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.taskmanager.models.Folder;
import com.example.taskmanager.models.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "TaskManager.db";

    private static final String SQL_CREATE_TASKS_ENTRIES =
            "CREATE TABLE " + TaskContract.TaskEntry.TABLE_NAME + " (" +
                    TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY," +
                    TaskContract.TaskEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL," +
                    TaskContract.TaskEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    TaskContract.TaskEntry.COLUMN_NAME_DUE_DATE + " TEXT," +
                    TaskContract.TaskEntry.COLUMN_NAME_DUE_TIME + " TEXT," +
                    TaskContract.TaskEntry.COLUMN_NAME_COMPLETED + " INTEGER DEFAULT 0,)"
                    TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID + " INTEGER," +
                    "FOREIGN KEY (" + TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID + ") REFERENCES " +
                    TaskContract.FolderEntry.TABLE_NAME + "(" + TaskContract.FolderEntry._ID + ") ON DELETE CASCADE" + ")";

    private  static final String SQL_CREATE_USERS_ENTRIES =
            "CREATE TABLE " + TaskContract.UserEntry.TABLE_NAME + " (" +
                    TaskContract.UserEntry._ID + " INTEGER PRIMARY KEY," +
                    TaskContract.UserEntry.COLUMN_NAME_USERNAME + " TEXT UNIQUE NOT NULL," +
                    TaskContract.UserEntry.COLUMN_NAME_PASSWORD + " TEXT NOT NULL," +
                    TaskContract.UserEntry.COLUMN_NAME_SALT + " TEXT NOT NULL," +
                    TaskContract.UserEntry.COLUMN_NAME_FULL_NAME + " TEXT)";

    private static final String SQL_CREATE_FOLDERS_ENTRIES =
            "CREATE TABLE " + TaskContract.FolderEntry.TABLE_NAME + " (" +
                    TaskContract.FolderEntry._ID + " INTEGER PRIMARY KEY ," +
                    TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME + " TEXT UNIQUE NOT NULL ," +
                    TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT + " INTEGER)";

    // Delete table statements
    private static final String SQL_DELETE_TASKS_ENTRIES =
            "DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE_NAME;

    private static final String SQL_DELETE_USERS_ENTRIES =
            "DROP TABLE IF EXISTS " + TaskContract.UserEntry.TABLE_NAME;

    private static final String SQL_DELETE_FOLDERS_ENTRIES =
            "DROP TABLE IF EXISTS " + TaskContract.FolderEntry.TABLE_NAME;


    public TaskDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    // Create tables when database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TASKS_ENTRIES);
        db.execSQL(SQL_CREATE_USERS_ENTRIES);
        db.execSQL(SQL_CREATE_FOLDERS_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(SQL_DELETE_TASKS_ENTRIES);
        db.execSQL(SQL_DELETE_USERS_ENTRIES);
        db.execSQL(SQL_DELETE_FOLDERS_ENTRIES);
        onCreate(db);
        Log.d("TaskDbHelper", "Database upgraded. Old Tables dropped, new tables created.");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }

    // Inserts a new user int the database
    public long insertUser(String fullName, String username, String hashedPassword, String salt){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.UserEntry.COLUMN_NAME_FULL_NAME, fullName);
        values.put(TaskContract.UserEntry.COLUMN_NAME_USERNAME, username);
        values.put(TaskContract.UserEntry.COLUMN_NAME_PASSWORD, hashedPassword);
        values.put(TaskContract.UserEntry.COLUMN_NAME_SALT, salt);

        long newRowId = db.insert(TaskContract.UserEntry.TABLE_NAME, null, values);
        db.close();
        Log.d("TaskDbHelper", "User inserted with ID: " + newRowId);
        return newRowId;
    }

    // Retrieves user credentials (hashedPassword and salt) for authentication.
    public Cursor getUserCredentials(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                TaskContract.UserEntry.COLUMN_NAME_USERNAME,
                TaskContract.UserEntry.COLUMN_NAME_PASSWORD,
                TaskContract.UserEntry.COLUMN_NAME_SALT
        };
        String selection = TaskContract.UserEntry.COLUMN_NAME_USERNAME + " = ?";
        String[] selectionArgs = {username};

        Cursor cursor = db.query(
                TaskContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null
        );
        return cursor;
    }

    // Check if a username already exists in the database
    public boolean doesUsernameExist(String username){
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {TaskContract.UserEntry._ID};
        String selection = TaskContract.UserEntry.COLUMN_NAME_USERNAME + " = ?";
        String[] selectionArgs ={username};
        Cursor cursor = db.query(
                TaskContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null
        );
        boolean exists = (cursor != null && cursor.getCount()>0);
        if(cursor != null){
            cursor.close();
        }
        db.close();
        return exists;
    }

    // Inserts a new folder into the database
    public long insertFolder(String folderName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME, folderName);
        values.put(TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT, 0); // New folder starts with 0 tasks

        long newRowId = -1;
        try {
            newRowId = db.insertOrThrow(TaskContract.FolderEntry.TABLE_NAME, null, values);
            Log.d("TaskDbHelper", "Folder '" + folderName + "' inserted with ID: " + newRowId);
        } catch (android.database.SQLException e) {
            Log.e("TaskDbHelper", "Error inserting folder: " + e.getMessage());
        } finally {
            db.close();
        }
        return newRowId;
    }

    public List<Folder> getAllFolders() {
        List<Folder> folderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] projection = {
                    TaskContract.FolderEntry._ID,
                    TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME,
                    TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT
            };

            cursor = db.query(
                    TaskContract.FolderEntry.TABLE_NAME,
                    projection,
                    null, null, null, null,
                    TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME + " ASC" // Order by name
            );

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry._ID);
                int nameIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME);
                int componentsIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT);

                do {
                    long id = cursor.getLong(idIndex);
                    String folderName = cursor.getString(nameIndex);
                    int components = cursor.getInt(componentsIndex);
                    folderList.add(new Folder(id, folderName, components));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("TaskDbHelper", "Error getting all folders: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        Log.d("TaskDbHelper", "Retrieved " + folderList.size() + " folders.");
        return folderList;
    }

    // Update task for a specific folder
    public int updateFolderTaskCount(long folderId, int newCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT, newCount);

        String selection = TaskContract.FolderEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(folderId)};

        int count = db.update(
                TaskContract.FolderEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        db.close();
        Log.d("TaskDbHelper", "Updated folder ID " + folderId + " with new task count: " + newCount + ". Rows affected: " + count);
        return count;
    }

    // Deletes Folder from the db
    public int deleteFolder(long folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = TaskContract.FolderEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(folderId)};
        int deletedRows = db.delete(TaskContract.FolderEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
        Log.d("TaskDbHelper", "Deleted folder with ID: " + folderId + ". Rows affected: " + deletedRows);
        return deletedRows;
    }

    // Insert new task into the database
    public long insertTask(Task task, long folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_TITLE, task.getTitle());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DESCRIPTION, task.getDescription());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DUE_DATE, task.getDueDate());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DUE_TIME, task.getDueTime());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPLETED, task.getIsDone() ? 1 : 0);
        if (folderId != -1) {
            values.put(TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID, folderId);
        } else {
            values.putNull(TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID);
        }

        long newRowId = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
        db.close();
        Log.d("TaskDbHelper", "Task '" + task.getTitle() + "' inserted with ID: " + newRowId + " into folder ID: " + folderId);
        return newRowId;
    }

    // Retrieves all tasks from the database
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] projection = {
                    TaskContract.TaskEntry._ID,
                    TaskContract.TaskEntry.COLUMN_NAME_TITLE,
                    TaskContract.TaskEntry.COLUMN_NAME_DESCRIPTION,
                    TaskContract.TaskEntry.COLUMN_NAME_DUE_DATE,
                    TaskContract.TaskEntry.COLUMN_NAME_DUE_TIME,
                    TaskContract.TaskEntry.COLUMN_NAME_COMPLETED,
                    TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID
            };

            cursor = db.query(
                    TaskContract.TaskEntry.TABLE_NAME,
                    projection,
                    null, null, null, null,
                    null // No specific order for now, can be added later
            );

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry._ID);
                int titleIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_TITLE);
                int descIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_DESCRIPTION);
                int dueDateIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_DUE_DATE);
                int dueTimeIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_DUE_TIME);
                int completedIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_COMPLETED);
                int folderIdIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID);

                do {
                    long id = cursor.getLong(idIndex);
                    String title = cursor.getString(titleIndex);
                    String description = cursor.getString(descIndex);
                    String dueDate = cursor.getString(dueDateIndex);
                    String dueTime = cursor.getString(dueTimeIndex);
                    boolean isCompleted = cursor.getInt(completedIndex) == 1;
                    long folderId = cursor.getLong(folderIdIndex); // Will be 0 if NULL in DB

                    Task task = new Task(id, title, description, dueDate, dueTime, isCompleted);
                    taskList.add(task);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("TaskDbHelper", "Error getting all tasks: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        Log.d("TaskDbHelper", "Retrieved " + taskList.size() + " tasks.");
        return taskList;
    }

    // Updates the completion status of a task
    public int updateTaskCompletionStatus(long taskId, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPLETED, isCompleted ? 1 : 0);

        String selection = TaskContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(taskId)};

        int count = db.update(
                TaskContract.TaskEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        db.close();
        Log.d("TaskDbHelper", "Updated task ID " + taskId + " completion status to " + isCompleted + ". Rows affected: " + count);
        return count;
    }

    // Deletes a task from the database
    public int deleteTask(long taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = TaskContract.TaskEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(taskId)};
        int deletedRows = db.delete(TaskContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
        Log.d("TaskDbHelper", "Deleted task with ID: " + taskId + ". Rows affected: " + deletedRows);
        return deletedRows;
    }

    // Retrieve a single folder object by its ID
    public Folder getFolderById(long folderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Folder folder = null;

        try {
            String[] projection = {
                    TaskContract.FolderEntry._ID,
                    TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME,
                    TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT
            };
            String selection = TaskContract.FolderEntry._ID + " = ?";
            String[] selectionArgs = {String.valueOf(folderId)};

            cursor = db.query(
                    TaskContract.FolderEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry._ID);
                int nameIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME);
                int componentsIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT);

                long id = cursor.getLong(idIndex);
                String folderName = cursor.getString(nameIndex);
                int components = cursor.getInt(componentsIndex);
                folder = new Folder(id, folderName, components);
                Log.d("TaskDbHelper", "Retrieved folder by ID: " + folderId + ", Name: " + folderName + ", Components: " + components);
            } else {
                Log.w("TaskDbHelper", "No folder found with ID: " + folderId);
            }
        } catch (Exception e) {
            Log.e("TaskDbHelper", "Error getting folder by ID " + folderId + ": " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close(); // Close the database connection here
        }
        return folder;
    }

}
}
