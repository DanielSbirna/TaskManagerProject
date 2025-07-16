package com.example.taskmanager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.adapters.TaskAdapter;
import com.example.taskmanager.data.TaskDbHelper;
import com.example.taskmanager.models.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // case-insensitive search

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private Button folderPageButton;
    private ImageButton fabAddTask;
    private EditText searchHint;
    private TaskAdapter taskAdapter;
    private List<Task> tasksList;
    private TaskDbHelper dbHelper;
    private int userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Log.d(TAG, "onCreate: MainActivity started.");

        userId = getIntent().getIntExtra("CURRENT_USER_ID", 0);
        Log.d(TAG, "Received CURRENT_USER_ID: " + userId);

        if (userId == -1) {
            Log.e(TAG, "Invalid user ID received. Redirecting to LoginActivity.");
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        dbHelper = new TaskDbHelper(this);
        Log.d(TAG, "TaskDbHelper initialized.");
        
        recyclerView= findViewById(R.id.recycler_view_tasks);
        recyclerView = findViewById(R.id.recycler_view_tasks);
        folderPageButton = findViewById(R.id.folder_page);
        fabAddTask = findViewById(R.id.add_task_button);

        searchHint = findViewById(R.id.search_hint);
        ImageButton proceedSearchButton = findViewById(R.id.proceed_search);

        tasksList = new ArrayList<>();
        
        //LinearLayoutManager for the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //initialize TaskAdapter with the list of tasks
        taskAdapter = new TaskAdapter(new ArrayList<>());
        taskAdapter.setOnItemClickListener(this);

        //set adapter to recycler
        recyclerView.setAdapter(taskAdapter);
        
        // Floating action button set to listen for events
        fabAddTask.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Add new task clicked!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            intent.putExtra("CURRENT_USER_ID", userId);
            startActivity(intent);
        });

        // Nav to folder page
        folderPageButton.setOnClickListener(v ->{
            Toast.makeText(MainActivity.this, "Folder button clicked!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, FolderActivity.class);
            intent.putExtra("CURRENT_USER_ID", userId);
            startActivity(intent);
        });

        proceedSearchButton.setOnClickListener(v -> {
            String query = searchHint.getText().toString().trim();
            Log.d(TAG, "Search button clicked. Query: '" + query + "'");
            filterTasks(query);
        });

         searchHint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim(); // Get the current text from the EditText
                Log.d(TAG, "onTextChanged: Current query: '" + query + "'");
                filterTasks(query); // Filter tasks based on the current input
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Reloading all tasks from database."); // Added logging
        loadTasksFromDatabase();
        String currentQuery = searchHint.getText().toString().trim();
        if (!currentQuery.isEmpty()) {
            filterTasks(currentQuery);
        }
    }

    private void loadTasksFromDatabase() {
        List<Task> loadedTasks = dbHelper.getTasksForUserAndFolder(userId, null, "due_date");
        tasksList.clear(); // Clear the cache first
        tasksList.addAll(loadedTasks); // Populate the cache with all tasks
        Log.d(TAG, "All tasks loaded from database. Count: " + tasksList.size());

        if (taskAdapter != null) {
            taskAdapter.setTasks(tasksList);
            taskAdapter.notifyDataSetChanged();
        }
    }

    private void filterTasks(String query) {
        List<Task> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            Log.d(TAG, "Filtering with empty query. Displaying all tasks.");
            filteredList.addAll(tasksList);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            for (Task task : tasksList) {
                if (task.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        (task.getDescription() != null && task.getDescription().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery))) {
                    filteredList.add(task);
                }
            }
            Log.d(TAG, "Filtered tasks for query '" + query + "'. Found: " + filteredList.size() + " tasks.");
        }
        taskAdapter.setTasks(filteredList);
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskClick(int position) {
        Task clickedTask = tasksList.get(position);
        Toast.makeText(this, "Clicked: " + clickedTask.getTitle(), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onTaskCheckChanged(int position, boolean isChecked) {
        Task changedTask = tasksList.get(position);
        if(isChecked){
            Toast.makeText(this, "Task " + changedTask.getTitle() + " completed!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Task " + changedTask.getTitle() + " uncompleted", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
