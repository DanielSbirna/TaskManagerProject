package com.example.taskmanager.ui.theme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.taskmanager.MainActivity;
import com.example.taskmanager.R;
import com.example.taskmanager.data.TaskContract;
import com.example.taskmanager.data.TaskDbHelper;
import com.example.taskmanager.data.PasswordHasher;

import android.util.Log;

public class LoginScreen extends AppCompatActivity {
    private static final String TAG = "LoginScreen";
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
        CardView logInButton = findViewById(R.id.log_in_button);
        CardView signUpButton = findViewById(R.id.sign_up_button);
        rememberMeCheckbox = findViewById(R.id.remember_me_checkbox);

        // 'Remember Me' state and username pre-fill
        SharedPreferences loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE); // loginPrefs consistent key
        loginPrefsEditor = loginPreferences.edit();
        boolean saveLogin = loginPreferences.getBoolean("saveLogin", false); // Checks for 'Remember me' past values

        // pre-fill username if 'Remember Me' checked
        if (saveLogin) {
            usernameInput.setText(loginPreferences.getString("username", ""));
            rememberMeCheckbox.setChecked(true);
        }

        logInButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // validations
            if (username.isEmpty() || password.isEmpty()) {
                //both empty
                Toast.makeText(LoginScreen.this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // validation from db
            if (authenticateUser(username, password)) {
                Toast.makeText(LoginScreen.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                // handle checkbox state after successful login
                if (rememberMeCheckbox.isChecked()) {
                    loginPrefsEditor.putBoolean("saveLogin", true); // save the checkbox state
                    loginPrefsEditor.putString("username", username); // save the username for pre-fill
                    loginPrefsEditor.apply(); // commit
                } else {
                    loginPrefsEditor.clear(); //clear
                    loginPrefsEditor.apply(); //commit
                }

                Intent intent = new Intent(LoginScreen.this, MainActivity.class);
                startActivity(intent);
                finish(); // closing the Login screen so 'back' doesn't go back to it
            } else {
                Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
            }

        });

        // OnClickListener for the SIGN UP
        signUpButton.setOnClickListener(v -> {
            // Intent to go from LoginScreen to SignUpScreen
            Intent intent = new Intent(LoginScreen.this, SignUpScreen.class);
            startActivity(intent); // Start the SignUpScreen activity
        });
    }
    private boolean authenticateUser(String username, String plainPassword){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null; // cursor is outside try-catch and blocks access
        boolean isAuthenticated = false;

        // retrieving the salt and hashed password
        String [] projection = {
                TaskContract.UserEntry.COLUMN_NAME_PASSWORD,
                TaskContract.UserEntry.COLUMN_NAME_SALT
        };

        // selecting the row where username matches
        String selection = TaskContract.UserEntry.COLUMN_NAME_USERNAME + " = ?";
        String[] selectionArgs = {username};

        try {
            cursor = db.query(
                    TaskContract.UserEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null // no group no filter no sort order
            );

            // if username matches, attempt to verify the password
            if(cursor.moveToFirst()){
                String storedHashedPassword = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.UserEntry.COLUMN_NAME_PASSWORD));
                String storedSaltString = cursor.getString(cursor.getColumnIndexOrThrow(TaskContract.UserEntry.COLUMN_NAME_SALT));

                // Convert the stored salt string back to a byte array
                byte[] storedSalt = PasswordHasher.stringToSalt(storedSaltString);

                // Use the PasswordHasher to verify the entered plain password against the stored hash and salt
                if (storedSalt != null) { // Ensure salt was successfully decoded
                    isAuthenticated = PasswordHasher.verifyPassword(plainPassword, storedHashedPassword, storedSalt);
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error during database operation for login: " + e.getMessage(), e); // Log any exceptions during database operation for debugging
        } finally {
            if (cursor != null) {
                cursor.close(); // prevent resource leaks
            }
        }
        return isAuthenticated;
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close(); // database helper is closed when the activity is destroyed
        }
        super.onDestroy();
    }
}

