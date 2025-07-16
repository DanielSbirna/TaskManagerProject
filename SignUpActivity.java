package com.example.taskmanager.ui.theme;

import android.content.Intent;
import android.os.Bundle;

import android.widget.EditText;
import android.widget.Toast; // showing messages

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.taskmanager.R;

import com.example.taskmanager.data.TaskDbHelper;
import com.example.taskmanager.data.PasswordHasher;

public class SignUpActivity extends AppCompatActivity {

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

            // Validations
            if (fullName.isEmpty()) {
                nameInput.setError("Name cannot be empty");
                Toast.makeText(SignUpActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (username.isEmpty()) {
                usernameInput.setError("Username cannot be empty");
                Toast.makeText(SignUpActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                passwordInput.setError("Password cannot be empty");
                Toast.makeText(SignUpActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username.length() < 8) {
                usernameInput.setError("Username must be at least 8 characters long");
                Toast.makeText(SignUpActivity.this, "Username must be at least 8 characters long", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 10 || !password.matches(".*[0-9].*") || !password.matches(".*[a-zA-Z].*")) {
                passwordInput.setError("Password must be at least 10 characters and contain numbers and letters");
                Toast.makeText(SignUpActivity.this, "Password must be at least 10 characters and contain numbers and letters", Toast.LENGTH_SHORT).show();
                return;
            }

            // If all validations pass, call registerUser
            registerUser(fullName, username, password);
        });
    }

    private void registerUser(String fullName, String username, String password){
        // Check if username already exists using TaskDbHelper
        if (dbHelper.doesUsernameExist(username)) {
            Toast.makeText(this, "Username already exists. Please choose another", Toast.LENGTH_SHORT).show();
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

        // Insert new user using TaskDbHelper
        long newRowId = dbHelper.insertUser(fullName, username, hashedPassword, saltString);

        if(newRowId != OPERATION_FAIL){
            Toast.makeText(SignUpActivity.this, "Sign up successful!", Toast.LENGTH_LONG).show();
            //Go to LoginScreen
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            intent.putExtra("CURRENT_USER_ID", newRowId);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(SignUpActivity.this, "Sign Up Failed. Please try again.", Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onDestroy(){
        if(dbHelper != null){
            dbHelper.close();
        }
        super.onDestroy();
    }
}
