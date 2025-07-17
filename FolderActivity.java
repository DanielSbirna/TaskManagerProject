package com.example.taskmanager.ui.theme;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.adapters.FolderAdapter;
import com.example.taskmanager.models.Folder;
import com.example.taskmanager.data.TaskDbHelper;

import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends AppCompatActivity implements FolderAdapter.OnItemClickListener {

    private static final String TAG = "FolderScreen";
    private RecyclerView folderRecyclerView;
    private FolderAdapter folderAdapter;
    private List<Folder> folderList;
    private ImageButton backButton;
    private ImageButton addFolderButton;
    private TaskDbHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_screen);
        Log.d(TAG, "onCreate: FolderScreen started.");

        try {
            dbHelper = new TaskDbHelper(this);
            Log.d(TAG, "TaskDbHelper initialized.");

            userId = getIntent().getIntExtra("CURRENT_USER_ID", 0);
            if (userId == -1) {
                // Handle case where user ID is missing (e.g., not logged in, or intent error)
                Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "userId not found in Intent. Finishing activity.");
                finish(); // Close this activity
                return; // Stop further onCreate execution
            }
            Log.d(TAG, "Retrieved userId: " + userId);

            folderRecyclerView = findViewById(R.id.folder_list_recyclerView);
            backButton = findViewById(R.id.back_button);
            addFolderButton = findViewById(R.id.add_folder_button);
            Log.d(TAG, "UI elements initialized.");

            folderList = new ArrayList<>();
            
            // Initialize adapter BEFORE loading data, so it's ready to be notified
            folderAdapter = new FolderAdapter(folderList);
            folderRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            folderRecyclerView.setAdapter(folderAdapter);
            folderAdapter.setOnItemClickListener(this);
            Log.d(TAG, "RecyclerView and Adapter setup complete.");

            loadFoldersFromDatabase(); // Load folders when the activity starts
            Log.d(TAG, "Initial loadFoldersFromDatabase called in onCreate.");


            backButton.setOnClickListener(v -> {
                Log.d(TAG, "Back button clicked. Navigating to MainActivity.");
                Intent intent = new Intent(FolderActivity.this, MainActivity.class);
                intent.putExtra("CURRENT_USER_ID", userId);
                startActivity(intent);
                finish();
                Log.d(TAG, "Navigating back to MainActivity.");
            });

            addFolderButton.setOnClickListener(v -> {
                Log.d(TAG, "Add Folder button clicked. Showing dialog.");
                showCreateFolderDialog();
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error during FolderScreen onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading folders: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure data is refreshed if returning to this screen
        Log.d(TAG, "onResume: Refreshing folders.");
        loadFoldersFromDatabase();
    }

    // Loads folders from the database and updates the RecyclerView
    private void loadFoldersFromDatabase() {
        Log.d(TAG, "Attempting to load folders from database...");
        List<Folder> folders = dbHelper.getFoldersForUser (userId);
        Log.d(TAG, "Folders retrieved from DB. Count: " + folders.size());
        folderList.clear();
        folderList.addAll(folders);
        if (folderAdapter != null) {
            folderAdapter.notifyDataSetChanged();
            Log.d(TAG, "FolderAdapter notified of data change. Current folderList size: " + folderList.size());
        } else {
            Log.w(TAG, "FolderAdapter is null when trying to notifyDataSetChanged. This should not happen if setup correctly.");
        }
    }

    @Override
    public void onFolderClick(int position) {
        if (position >= 0 && position < folderList.size()) {
            Folder clickedFolder = folderList.get(position);
            Toast.makeText(this, "Clicked folder: " + clickedFolder.getFolderName(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Folder clicked: " + clickedFolder.getFolderName() + " at position " + position + " (ID: " + clickedFolder.getId() + ")");
            // TODO: Implement code to open a new activity to show tasks within this folder
        } else {
            Log.e(TAG, "onFolderClick: Invalid position clicked: " + position);
        }
    }

    private void showCreateFolderDialog() {
        Log.d(TAG, "Showing create folder dialog.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_folder_pop, null);
        builder.setView(dialogView);

        final EditText folderNameEditText = dialogView.findViewById(R.id.folder_name);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button saveButton = dialogView.findViewById(R.id.save_button);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Log.d(TAG, "Create folder dialog displayed.");

        cancelButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            Log.d(TAG, "Create folder dialog cancelled.");
        });

        saveButton.setOnClickListener(v -> {
            String folderName = folderNameEditText.getText().toString().trim();
            if (folderName.isEmpty()) {
                Toast.makeText(FolderActivity.this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Attempted to save empty folder name.");
            } else {
                long newFolderId = dbHelper.insertFolder(folderName, userId);
                if (newFolderId != -1) {
                    Toast.makeText(FolderActivity.this, "Folder '" + folderName + "' created!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Folder '" + folderName + "' created with ID: " + newFolderId);
                    loadFoldersFromDatabase(); // Reload folders to update the RecyclerView
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(FolderActivity.this, "Failed to create folder. It might already exist.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to create folder: '" + folderName + "'. It might already exist or another DB error occurred.");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
            Log.d(TAG, "TaskDbHelper closed in onDestroy.");
        }
    }
}
