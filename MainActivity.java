package com.example.taskmanager;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.adapters.TaskAdapter;
import com.example.taskmanager.data.TaskDbHelper;
import com.example.taskmanager.models.Folder;
import com.example.taskmanager.models.Task;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private Spinner filterSpinner;
    List<Task> filteredList;
    private String currentFilter = "due";
    private String currentSearchQuery = "";


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
        taskAdapter = new TaskAdapter(new ArrayList<>(tasksList));
        taskAdapter.setOnItemClickListener(this);

        //set adapter to recycler
        recyclerView.setAdapter(taskAdapter);

        // setup swipe
        setUpItemTouchHelper();

        // SPINNER
        // Create ArrayAdapter using string-array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.filter_by,
                R.layout.spinner_item_custom
        );

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);

        // Apply the adapter to the spinner
        filterSpinner.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = parent.getItemAtPosition(position).toString().toLowerCase(Locale.ROOT);
                Log.d(TAG, "Spinner selected: " + currentFilter);
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no action
            }
        });
        
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
        tasksList.clear();
        tasksList.addAll(loadedTasks); // Populate the cache with all tasks
        Log.d(TAG, "All tasks loaded from database. Count: " + tasksList.size());

        if (taskAdapter != null) {
            taskAdapter.setTasks(tasksList);
            taskAdapter.notifyDataSetChanged();
        }
    }

    // Swipe to delete
    private void setUpItemTouchHelper() {
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            Drawable background;
            boolean initiated;

            private void init() {
                background = new ColorDrawable(Color.RED);
                initiated = true; // Added this line to mark initiation complete
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // for drag and drop
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final int position = viewHolder.getBindingAdapterPosition();
                final Task deletedTask = taskAdapter.getTaskAt(position);
                final int deletedTaskId = (int) deletedTask.getId();
                final int currentUserId = getIntent().getIntExtra("CURRENT_USER_ID", -1);

                // Remove from adapter
                taskAdapter.removeItem(position);

                Snackbar snackbar = Snackbar
                        .make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> {
                            // Restore the item if Undo is clicked
                            taskAdapter.restoreItem(deletedTask, position);
                            recyclerView.scrollToPosition(position);
                        });
                // Set a callback for when the Snackbar is dismissed
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT ||
                                event == Snackbar.Callback.DISMISS_EVENT_SWIPE ||
                                event == Snackbar.Callback.DISMISS_EVENT_MANUAL) {
                            // if Snackbar dismissed without UNDO, permanently delete from DB
                            if (currentUserId != -1) {
                                dbHelper.deleteTask(deletedTaskId, currentUserId);
                                if (deletedTask.getFolderId() != null && deletedTask.getFolderId() != 0L) {
                                    // Corrected: Declare and initialize 'folder'
                                    Folder folder = dbHelper.getFolderById(deletedTask.getFolderId(), currentUserId);
                                    if (folder != null) {
                                        dbHelper.updateFolderTaskCount(folder.getId(), folder.getComponentsCount() - 1, currentUserId);
                                    }
                                }
                            }
                        }
                    }
                });
                snackbar.show();
            }
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (!initiated) {
                    init();
                }
                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getBottom() - itemView.getTop();

                // Draw the red delete background
                background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                // Draw the delete icon
                Drawable deleteIcon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                int intrinsicWidth = deleteIcon.getIntrinsicWidth();
                int intrinsicHeight = deleteIcon.getIntrinsicHeight();
                int xMarkLeft = itemView.getRight() - 2 * intrinsicWidth;
                int xMarkRight = itemView.getRight() - intrinsicWidth;
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int xMarkBottom = xMarkTop + intrinsicHeight;
                deleteIcon.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
                deleteIcon.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        // This line was already correct, as it now uses the correctly named itemTouchHelperCallback
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView); // Corrected: Use taskRecyclerView
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
        Task changedTask = taskAdapter.getTaskAt(position);

        dbHelper.updateTaskCompletionStatus((int) changedTask.getId(), isChecked, userId);

        // update the main tasksList directly
        for(Task task : tasksList) {
            if(task.getId() == changedTask.getId()) {
                task.setIsDone(isChecked);
                break;
            }
        }

        // simulated a refresh
        loadTasksFromDatabase();
        // re-applied filters
        applyFilters();
        Toast.makeText(this, "Task " + changedTask.getTitle() + (isChecked ? " completed!" : " uncompleted"), Toast.LENGTH_SHORT).show();

    }

    private void applyFilters() {
        List<Task> filteredByStatus = new ArrayList<>();

        Calendar now = Calendar.getInstance();

        for (Task task : tasksList) {
            boolean add = false;

            String dueDateStr = task.getDueDate();
            String dueTimeStr = task.getDueTime();

            Calendar taskDueDateTime = Calendar.getInstance();
            boolean hasDueDate = false;

            try {
                if (dueDateStr != null && !dueDateStr.isEmpty()) {
                    String dateTimeStr = dueDateStr;
                    SimpleDateFormat sdf;

                    if (dueTimeStr != null && !dueTimeStr.isEmpty()) {
                        dateTimeStr += " " + dueTimeStr;
                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    } else {
                        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    }

                    Date dueDate = sdf.parse(dateTimeStr);
                    taskDueDateTime.setTime(dueDate);
                    hasDueDate = true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            switch (currentFilter) {
                case "due":
                    if (!task.getIsDone() && (!hasDueDate || taskDueDateTime.after(now))) {
                        add = true;
                    }
                    break;
                case "overdue":
                    if (!task.getIsDone() && hasDueDate && taskDueDateTime.before(now)) {
                        add = true;
                    }
                    break;
                case "done":
                    if (task.getIsDone()) {
                        add = true;
                    }
                    break;
                default:
                    add = true;
            }

            if (add) filteredByStatus.add(task);
        }

        // Search filtering
        List<Task> finalFiltered = new ArrayList<>();
        if (currentSearchQuery.isEmpty()) {
            finalFiltered.addAll(filteredByStatus);
        } else {
            String lowerQuery = currentSearchQuery.toLowerCase(Locale.getDefault());
            for (Task task : filteredByStatus) {
                if (task.getTitle().toLowerCase(Locale.getDefault()).contains(lowerQuery) ||
                        (task.getDescription() != null && task.getDescription().toLowerCase(Locale.getDefault()).contains(lowerQuery))) {
                    finalFiltered.add(task);
                }
            }
        }

        taskAdapter.setTasks(finalFiltered);
        taskAdapter.notifyDataSetChanged();
    }
    
    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
