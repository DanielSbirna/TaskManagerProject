package com.example.taskmanager.ui.theme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.database.Cursor;

import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import com.example.taskmanager.R;
import com.example.taskmanager.data.TaskContract;
import com.example.taskmanager.data.TaskDbHelper;
import com.example.taskmanager.data.PasswordHasher;

import android.util.Log;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText usernameInput;
    private EditText passwordInput;
    private CheckBox rememberMeCheckbox;
    private TaskDbHelper dbHelper;
    private SharedPreferences.Editor loginPrefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_screen);

        dbHelper = new TaskDbHelper(this);

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        MaterialButton logInButton = findViewById(R.id.log_in_button);
        MaterialButton signUpButton = findViewById(R.id.sign_up_button);
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox);

        // 'Remember Me' state and username pre-fill
        SharedPreferences loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE); // loginPrefs consistent key
        loginPrefsEditor = loginPreferences.edit();
        boolean saveLogin = loginPreferences.getBoolean("saveLogin", false); // Checks for 'Remember me' past values

        // pre-fill username if 'Remember Me' checked
        if (saveLogin) {
            String savedUsername = loginPreferences.getString("username", "");
            usernameInput.setText(savedUsername);
            String savedPassword = loginPreferences.getString("password", "");
            passwordInput.setText(savedPassword);
            rememberMeCheckbox.setChecked(true);
        }

        logInButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // validations
            if (username.isEmpty() || password.isEmpty()) {
                //both empty
                Toast.makeText(LoginActivity.this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = getIntent().getIntExtra("CURRENT_USER_ID", 0);

            // Check if the returned userId is valid (not -1)
            if (userId != -1) {
                Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                // handle checkbox state after successful login
                if (rememberMeCheckbox.isChecked()) {
                    loginPrefsEditor.putBoolean("saveLogin", true); // save the checkbox state
                    loginPrefsEditor.putString("username", username); // save the username for pre-fill
                    loginPrefsEditor.apply(); // commit
                } else {
                    loginPrefsEditor.clear(); //clear
                    loginPrefsEditor.apply(); //commit
                }

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("CURRENT_USER_ID", userId); // Pass the user ID
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
            }
        });

        // OnClickListener for the SIGN UP
        signUpButton.setOnClickListener(v -> {
            // Intent to go from LoginActivity to SignUpScreen
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent); // Start the SignUpActivity
        });
    }
    
    private int authenticateUser(String username, String plainPassword){
        Cursor cursor = null;
        int userId;

        try {
            cursor = dbHelper.getUserCredentials(username);

            if (cursor != null && cursor.moveToFirst()) {
                String storedHashedPassword = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.UserEntry.COLUMN_NAME_PASSWORD));
                String storedSaltString = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.UserEntry.COLUMN_NAME_SALT));

                // Get the user ID from the cursor!
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(TaskContract.UserEntry._ID));

                byte[] storedSalt = PasswordHasher.stringToSalt(storedSaltString);

                if (storedSalt != null) {
                    if (PasswordHasher.verifyPassword(plainPassword, storedHashedPassword, storedSalt)) {
                        // Password verified successfully!
                        Log.d(TAG, "Authentication successful for user: " + username + ", ID: " + userId);
                        // userId is already correctly set here
                    } else {
                        // Password verification failed
                        Log.d(TAG, "Password verification failed for user: " + username);
                        userId = -1; // Reset userId to -1 as authentication failed
                    }
                } else {
                    Log.e(TAG, "Stored salt is null for user: " + username);
                    userId = -1; // Reset userId to -1 as authentication failed
                }
            } else {
                // User not found in database
                Log.d(TAG, "User not found: " + username);
                userId = -1; // Reset userId to -1 as authentication failed
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during database operation for login: " + e.getMessage(), e);
            userId = -1; // Ensure userId is -1 on error
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userId;
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close(); // database helper is closed when the activity is destroyed
        }
        super.onDestroy();
    }
}

