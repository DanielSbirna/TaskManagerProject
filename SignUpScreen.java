package com.example.taskmanager.ui.theme;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.widget.EditText;
import android.widget.Toast; // showing messages

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // button CardView

import com.example.taskmanager.R; // App res file

import com.example.taskmanager.data.TaskContract;
import com.example.taskmanager.data.TaskDbHelper;
import com.example.taskmanager.data.PasswordHasher;

import android.database.Cursor;



public class SignUpScreen extends AppCompatActivity {

    private EditText nameInput;
    private EditText usernameInput;
    private EditText passwordInput;

    private TaskDbHelper dbHelper;

    private final int OPERATION_FAIL = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);

        //initialize db helper
        dbHelper = new TaskDbHelper( this);

        //initialize UI elements
        nameInput = findViewById(R.id.name_input);
        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        CardView signUpButton = findViewById(R.id.sign_up_button);

        signUpButton.setOnClickListener(v -> {
            String fullName = nameInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // --- Validations ---
            if (fullName.isEmpty()) { // Add validation for nameInput
                nameInput.setError("Name cannot be empty");
                Toast.makeText(SignUpScreen.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return; // Stop execution
            }
            if (username.isEmpty()) { // Check for empty username specifically
                usernameInput.setError("Username cannot be empty");
                Toast.makeText(SignUpScreen.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return; // Stop execution
            }
            if (password.isEmpty()) { // Check for empty password specifically
                passwordInput.setError("Password cannot be empty");
                Toast.makeText(SignUpScreen.this, "Please enter a password", Toast.LENGTH_SHORT).show();
                return; // Stop execution
            }

            if (username.length() < 8) {
                usernameInput.setError("Username must be at least 8 characters long");
                Toast.makeText(SignUpScreen.this, "Username must be at least 8 characters long", Toast.LENGTH_SHORT).show();
                return; // Stop execution
            }
            if (password.length() < 10 || !password.matches(".*[0-9].*") || !password.matches(".*[a-zA-Z].*")) {
                passwordInput.setError("Password must be at least 10 characters and contain numbers and letters");
                Toast.makeText(SignUpScreen.this, "Password must be at least 10 characters and contain numbers and letters", Toast.LENGTH_SHORT).show();
                return; // Stop execution
            }

            // If all validations pass, call registerUser
            registerUser(fullName, username, password);
        });
    }

    private void registerUser(String fullName, String username, String password){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //Check if username already exists in the db
        String[] projection = {TaskContract.UserEntry._ID}; //SELECT ID
        String selection = TaskContract.UserEntry.COLUMN_NAME_USERNAME + " = ?"; //WHERE username
        String[] selectionArgs = {username}; //username value

        Cursor cursor = db.query(
                TaskContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, null
        );

        //if cursor has rows than username exists
        if(cursor.getCount()>0){
            Toast.makeText(this, "Username already exists. Please choose another", Toast.LENGTH_SHORT).show();
            cursor.close();
            db.close();
            return;
        }

        //salt and hashing
         byte[] salt= PasswordHasher.generateSalt();
        String saltString = PasswordHasher.saltToString(salt); //byte - string for storage
        String hashedPassword = PasswordHasher.hashPassword(password, salt);

        if(hashedPassword == null || saltString == null){
            Toast.makeText(this,"Error processing password. Please Try again.",Toast.LENGTH_LONG).show();
            return;
        }

        //new user insert
        ContentValues values = new ContentValues();
        values.put(TaskContract.UserEntry.COLUMN_NAME_FULL_NAME, fullName);
        values.put(TaskContract.UserEntry.COLUMN_NAME_USERNAME, username);
        values.put(TaskContract.UserEntry.COLUMN_NAME_PASSWORD, hashedPassword);
        values.put(TaskContract.UserEntry.COLUMN_NAME_SALT, saltString);

        long newRowId = db.insert(TaskContract.UserEntry.TABLE_NAME, null, values);

        if(newRowId != OPERATION_FAIL){
            Toast.makeText(SignUpScreen.this, "Sign up successful!", Toast.LENGTH_LONG).show();
            //Go to LoginScreen
            Intent intent = new Intent(SignUpScreen.this, LoginScreen.class);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(SignUpScreen.this, "Sign Up Failed. Please try again.", Toast.LENGTH_LONG).show();
        }
        db.close();
    }
    @Override
    protected void onDestroy(){
        if(dbHelper != null){
            dbHelper.close();
        }
        super.onDestroy();
    }
}
