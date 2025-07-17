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

    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "TaskManager.db";

    private  static final String SQL_CREATE_USERS_ENTRIES =
            "CREATE TABLE " + TaskContract.UserEntry.TABLE_NAME + " (" +
                    TaskContract.UserEntry._ID + " INTEGER PRIMARY KEY," +
                    TaskContract.UserEntry.COLUMN_NAME_USERNAME + " TEXT UNIQUE NOT NULL," +
                    TaskContract.UserEntry.COLUMN_NAME_PASSWORD + " TEXT NOT NULL," +
                    TaskContract.UserEntry.COLUMN_NAME_SALT + " TEXT NOT NULL," +
                    TaskContract.UserEntry.COLUMN_NAME_FULL_NAME + " TEXT)";

    private static final String SQL_CREATE_FOLDERS_ENTRIES =
            "CREATE TABLE " + TaskContract.FolderEntry.TABLE_NAME + " (" +
                    TaskContract.FolderEntry._ID + " INTEGER PRIMARY KEY," +
                    TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME + " TEXT NOT NULL," +
                    TaskContract.FolderEntry.COLUMN_NAME_USER_ID + " INTEGER NOT NULL," +
                    TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT + " INTEGER," +
                    "FOREIGN KEY (" + TaskContract.FolderEntry.COLUMN_NAME_USER_ID + ") REFERENCES " +
                    TaskContract.UserEntry.TABLE_NAME + "(" + TaskContract.UserEntry._ID + ") ON DELETE CASCADE" + ")";

    private static final String SQL_CREATE_TASKS_ENTRIES =
            "CREATE TABLE " + TaskContract.TaskEntry.TABLE_NAME + " (" +
                    TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY," +
                    TaskContract.TaskEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL," +
                    TaskContract.TaskEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    TaskContract.TaskEntry.COLUMN_NAME_DUE_DATE + " TEXT," +
                    TaskContract.TaskEntry.COLUMN_NAME_DUE_TIME + " TEXT," +
                    TaskContract.TaskEntry.COLUMN_NAME_COMPLETED + " INTEGER DEFAULT 0,)" +
                    TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID + " INTEGER," + 
                    TaskContract.TaskEntry.COLUMN_NAME_USER_ID + " INTEGER NOT NULL," +
                    "FOREIGN KEY (" + TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID + ") REFERENCES " +
                    TaskContract.FolderEntry.TABLE_NAME + "(" + TaskContract.FolderEntry._ID + ") ON DELETE SET NULL," +
                    "FOREIGN KEY (" + TaskContract.TaskEntry.COLUMN_NAME_USER_ID + ") REFERENCES " +
                    TaskContract.UserEntry.TABLE_NAME + "(" + TaskContract.UserEntry._ID + ") ON DELETE CASCADE" + ")";

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

    // Create tables when database is created for the first time in least to most dependencies order
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS_ENTRIES); // Create users first
        db.execSQL(SQL_CREATE_FOLDERS_ENTRIES); // Create folders 2nd
        db.execSQL(SQL_CREATE_TASKS_ENTRIES); // Create folder 3rd
    }

    // Drops tables in order from most dependencies to least
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
         db.execSQL(SQL_DELETE_TASKS_ENTRIES);
        db.execSQL(SQL_DELETE_FOLDERS_ENTRIES);
        db.execSQL(SQL_DELETE_USERS_ENTRIES);
        onCreate(db);
        Log.d("TaskDbHelper", "Database upgraded. Old Tables dropped, new tables created.");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }

    // Inserts a new user int the database
    public int insertUser(String fullName, String username, String hashedPassword, String salt){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.UserEntry.COLUMN_NAME_FULL_NAME, fullName);
        values.put(TaskContract.UserEntry.COLUMN_NAME_USERNAME, username);
        values.put(TaskContract.UserEntry.COLUMN_NAME_PASSWORD, hashedPassword);
        values.put(TaskContract.UserEntry.COLUMN_NAME_SALT, salt);

        int newRowId = (int)(db.insert(TaskContract.UserEntry.TABLE_NAME, null, values));
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

        return db.query(
                TaskContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null
        );
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
    public long insertFolder(String folderName, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME, folderName);
        values.put(TaskContract.FolderEntry.COLUMN_NAME_USER_ID, userId); // Link to user
        values.put(TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT, 0); // New folder starts with 0 tasks

        int newRowId = -1;
        try {
            newRowId = (int) db.insertOrThrow(TaskContract.FolderEntry.TABLE_NAME, null, values);
            Log.d("TaskDbHelper", "Folder '" + folderName + "' inserted with ID: " + newRowId + " for user ID: " + userId);
        } catch (android.database.SQLException e) {
            Log.e("TaskDbHelper", "Error inserting folder: " + e.getMessage());
        }
        return newRowId;
    }

    // Retrieves folders for a specific user (no parent folder filter needed)
    public List<Folder> getFoldersForUser(int userId) {
        List<Folder> folderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String[] projection = {
                    TaskContract.FolderEntry._ID,
                    TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME,
                    TaskContract.FolderEntry.COLUMN_NAME_USER_ID, // Include user_id
                    TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT // Include components count
            };

            String selection = TaskContract.FolderEntry.COLUMN_NAME_USER_ID + " = ?";
            String[] selectionArgs = new String[]{String.valueOf(userId)};

            cursor = db.query(
                    TaskContract.FolderEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null, null,
                    TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME + " ASC" // Order by name
            );

            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry._ID);
                int nameIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME);
                int userIdColIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry.COLUMN_NAME_USER_ID);
                int componentsCountIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT);

                do {
                    // Changed to getInt() as per your preference for 'id'
                    int id = cursor.getInt(idIndex);
                    String folderName = cursor.getString(nameIndex);
                    // Use getInt() for userId
                    int folderUserId = cursor.getInt(userIdColIndex);
                    int componentsCount = cursor.getInt(componentsCountIndex);

                    // Now calling Folder constructor with int id and int userId
                    folderList.add(new Folder(id, folderName, folderUserId, componentsCount));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("TaskDbHelper", "Error getting folders for user " + userId + ": " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            // db.close(); // Don't close here, manage connection outside
        }
        Log.d("TaskDbHelper", "Retrieved " + folderList.size() + " folders for user " + userId + ".");
        return folderList;
    }
    
    // Update task for a specific folder
    public int updateFolderTaskCount(long folderId, int newCount, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT, newCount);

        String selection = TaskContract.FolderEntry._ID + " = ? AND " + TaskContract.FolderEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(folderId), String.valueOf(userId)};

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
    public int deleteFolder(long folderId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = TaskContract.FolderEntry._ID + " = ? AND " + TaskContract.FolderEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(folderId), String.valueOf(userId)};
        int deletedRows = db.delete(TaskContract.FolderEntry.TABLE_NAME, selection, selectionArgs);
        Log.d("TaskDbHelper", "Deleted folder with ID: " + folderId + ". Rows affected: " + deletedRows);
        return deletedRows;
    }

    // Insert new task into the database
    public long insertTask(Task task, int userId, Long folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_TITLE, task.getTitle());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DESCRIPTION, task.getDescription());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DUE_DATE, task.getDueDate());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DUE_TIME, task.getDueTime());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPLETED, task.getIsDone() ? 1 : 0);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_USER_ID, userId); // Link task to the current user
        if (folderId != null) { // Check for null
            values.put(TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID, folderId);
        } else {
            values.putNull(TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID); // Task is not in a folder
        }

        int newRowId = (int) db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
        Log.d("TaskDbHelper", "Task '" + task.getTitle() + "' inserted with ID: " + newRowId + " for user ID: " + userId + " into folder ID: " + folderId);
        return newRowId;
    }

    // Retrieves all tasks from the database
    public List<Task> getTasksForUserAndFolder(int userId, Long folderId, String orderBy) {
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
                    TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID,
                    TaskContract.TaskEntry.COLUMN_NAME_USER_ID
            };

            String selection;
            String[] selectionArgs;

            if (folderId == null || folderId == 0L) { // Added 0L check for consistency if a default "no folder" ID is used
                // Get tasks not assigned to any folder for this user (root tasks)
                selection = TaskContract.TaskEntry.COLUMN_NAME_USER_ID + " = ? AND " +
                        TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID + " IS NULL";
                selectionArgs = new String[]{String.valueOf(userId)};
            } else {
                // Get tasks within a specific folder for this user
                selection = TaskContract.TaskEntry.COLUMN_NAME_USER_ID + " = ? AND " +
                        TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID + " = ?";
                selectionArgs = new String[]{String.valueOf(userId), String.valueOf(folderId)};
            }

            cursor = db.query(
                    TaskContract.TaskEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null, null,
                    orderBy // Order by clause passed in
            );

            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry._ID);
                int titleIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_TITLE);
                int descIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_DESCRIPTION);
                int dueDateIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_DUE_DATE);
                int dueTimeIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_DUE_TIME);
                int completedIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_COMPLETED);
                int folderIdColIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID);
                int userIdColIndex = cursor.getColumnIndexOrThrow(TaskContract.TaskEntry.COLUMN_NAME_USER_ID);

                do {
                    int id = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    String description = cursor.getString(descIndex);
                    String dueDate = cursor.getString(dueDateIndex);
                    String dueTime = cursor.getString(dueTimeIndex);
                    boolean isDone = cursor.getInt(completedIndex) == 1;
                    Long taskFolderId = cursor.isNull(folderIdColIndex) ? null : cursor.getLong(folderIdColIndex);
                    int taskUserId = cursor.getInt(userIdColIndex);

                    taskList.add(new Task(id, title, description, dueDate, dueTime, isDone, taskUserId, taskFolderId));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("TaskDbHelper", "Error getting tasks for user " + userId + " in folder " + folderId + ": " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d("TaskDbHelper", "Retrieved " + taskList.size() + " tasks for user " + userId + " in folder " + folderId + ".");
        return taskList;
    }
    
    // Updates the completion status of a task
    public int updateTaskCompletionStatus(long taskId,, boolean isDone, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPLETED, isCompleted ? 1 : 0);

        String selection = TaskContract.TaskEntry._ID + " = ? AND " + TaskContract.TaskEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(taskId), String.valueOf(userId)};

        int count = db.update(
                TaskContract.TaskEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        Log.d("TaskDbHelper", "Updated task ID " + taskId + " completion status to " + isDone + " for user " + userId + ". Rows affected: " + count);
        return count;
    }

    // Deletes a task from the database for a specific user
    public int deleteTask(long taskId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = TaskContract.TaskEntry._ID + " = ? AND " + TaskContract.TaskEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(taskId), String.valueOf(userId)}};
        int deletedRows = db.delete(TaskContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
       Log.d("TaskDbHelper", "Deleted task with ID: " + taskId + " for user " + userId + ". Rows affected: " + deletedRows);
        return deletedRows;
    }

    // Update a task's details for a specific user
    public int updateTask(Task task, int userId, Long folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_NAME_TITLE, task.getTitle());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DESCRIPTION, task.getDescription());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DUE_DATE, task.getDueDate());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_DUE_TIME, task.getDueTime());
        values.put(TaskContract.TaskEntry.COLUMN_NAME_COMPLETED, task.getIsDone() ? 1 : 0);
        values.put(TaskContract.TaskEntry.COLUMN_NAME_USER_ID, userId); // Ensure userId is passed

        if (folderId != null) {
            values.put(TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID, folderId);
        } else {
            values.putNull(TaskContract.TaskEntry.COLUMN_NAME_FOLDER_ID);
        }

        String selection = TaskContract.TaskEntry._ID + " = ? AND " + TaskContract.TaskEntry.COLUMN_NAME_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(task.getId()), String.valueOf(userId)};

        int count = db.update(TaskContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
        Log.d("TaskDbHelper", "Updated task ID " + task.getId() + " for user " + userId + ". Rows affected: " + count);
        return count;
    }

    // Retrieve a single folder object by its ID for a specific user
    public Folder getFolderById(long folderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Folder folder = null;

        try {
            String[] projection = {
                    TaskContract.FolderEntry._ID,
                    TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME,
                    TaskContract.FolderEntry.COLUMN_NAME_USER_ID,
                    TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT
            };
            String selection = TaskContract.FolderEntry._ID + " = ? AND " + TaskContract.FolderEntry.COLUMN_NAME_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(folderId), String.valueOf(userId)};

            cursor = db.query(
                    TaskContract.FolderEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry._ID);
                int nameIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry.COLUMN_NAME_FOLDER_NAME);
                int userIdColIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry.COLUMN_NAME_USER_ID);
                int componentsIndex = cursor.getColumnIndexOrThrow(TaskContract.FolderEntry.COLUMN_NAME_COMPONENTS_COUNT);


                // Changed to getInt() as per your preference for 'id'
                int id = cursor.getInt(idIndex);
                String folderName = cursor.getString(nameIndex);
                // Use getInt() for userId
                int folderUserId = cursor.getInt(userIdColIndex);
                int components = cursor.getInt(componentsIndex);

                // Now calling Folder constructor with int id and int userId
                folder = new Folder(id, folderName, folderUserId, components);
                Log.d("TaskDbHelper", "Retrieved folder by ID: " + folderId + ", Name: " + folderName + ", User: " + folderUserId);
            } else {
                Log.w("TaskDbHelper", "No folder found with ID: " + folderId + " for user: " + userId);
            }
        } catch (Exception e) {
            Log.e("TaskDbHelper", "Error getting folder by ID " + folderId + " for user " + userId + ": " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return folder;
    }
}
