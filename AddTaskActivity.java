package com.example.taskmanager.ui.theme;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout; // For date/time pickers
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.adapters.AddFolderAdapter;
import com.example.taskmanager.data.TaskDbHelper;
import com.example.taskmanager.models.Folder;
import com.example.taskmanager.models.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity implements AddFolderAdapter.OnItemClickListener {
    private static final String TAG = "AddTaskActivity";
    private ImageButton backButton;
    private EditText addTaskTitle;
    private EditText descriptionHint;
    private RecyclerView recyclerViewFolder;
    private AddFolderAdapter addFolderAdapter;
    private List<Folder> folderList;
    private Switch allDaySwitch;
    private ConstraintLayout dueDatePickerLayout;
    private TextView dayDuePick;
    private TextView timeDuePick;
    private TextView saveButton;
    private TaskDbHelper dbHelper;
    private Calendar dueCalendar;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task_screen);

        dbHelper = new TaskDbHelper(this);

        userId = getIntent().getIntExtra("CURRENT_USER_ID", -1);
        Log.d(TAG, "MainActivity onCreate: Retrieved userId from Intent: " + userId);

        if (userId == -1) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show();
            Log.d(TAG, "MainActivity onCreate: Retrieved userId from Intent: " + userId);
            finish();
            return;
        }
        Log.d(TAG, "Retrieved userId: " + userId);

        // Initialize UI elements
        backButton = findViewById(R.id.back_button);
        addTaskTitle = findViewById(R.id.add_task_title);
        descriptionHint = findViewById(R.id.description_hint);
        recyclerViewFolder = findViewById(R.id.recycler_view_folder);
        allDaySwitch = findViewById(R.id.all_day_switch);
        dueDatePickerLayout = findViewById(R.id.due_date_picker);
        dayDuePick = findViewById(R.id.day_due_pick);
        timeDuePick = findViewById(R.id.time_due_pick);
        saveButton = findViewById(R.id.save_button);
        
        // Initialize Calendars with current time
        dueCalendar = Calendar.getInstance();

        // Set initial date/time display
        updateDateTimeDisplay(dayDuePick, timeDuePick, dueCalendar);
        
        // Setup Folder RecyclerView
        folderList = new ArrayList<>();
        loadFoldersForSelection(); // Load folders from DB
        recyclerViewFolder.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        addFolderAdapter = new AddFolderAdapter(folderList);
        recyclerViewFolder.setAdapter(addFolderAdapter);
        addFolderAdapter.setOnItemClickListener(this);

        // Back button functionality
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddTaskActivity.this, MainActivity.class);
            intent.putExtra("CURRENT_USER_ID", userId); // passing userId back to MainActivity
            startActivity(intent);
            finish();
        });

        // All Day Switch listener
        allDaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // If "All Day" is checked, hide time pickers and set default all-day times
                timeDuePick.setVisibility(View.GONE);
                // Set start of day and end of day
                dueCalendar.set(Calendar.HOUR_OF_DAY, 0);
                dueCalendar.set(Calendar.MINUTE, 0);
            } else {
                // If "All Day" is unchecked, show time pickers
                timeDuePick.setVisibility(View.VISIBLE);
                // Reset times to current or a default interval if needed
                dueCalendar = Calendar.getInstance();
            }
            updateDateTimeDisplay(dayDuePick, timeDuePick, dueCalendar);
        });
        // Date and Time Picker Listeners
        dueDatePickerLayout.setOnClickListener(v -> showDateTimePicker());

        // Save Button Listener
        saveButton.setOnClickListener(v -> saveTask());
    }

    private void loadFoldersForSelection() {
        Log.d(TAG, "Loading folders for selection...");
        List<Folder> folders = dbHelper.getFoldersForUser(userId);
        // Add a "Unnamed" option at the beginning if it doesn't already exist
        boolean noFolderExists = false;
        for (Folder f : folders) {
            if ("Unnamed".equals(f.getFolderName())) {
                noFolderExists = true;
                break;
            }
        }
        if (!noFolderExists) {
            folders.add(0, new Folder(-1, "No Folder", userId, 0));
            Log.d(TAG, "Added 'No Folder' option.");
        }
        folderList.clear();
        folderList.addAll(folders);
        if (addFolderAdapter != null) {
            addFolderAdapter.notifyDataSetChanged();
            Log.d(TAG, "Folder selection adapter notified. Current folderList size: " + folderList.size());
        } else {
            Log.w(TAG, "addFolderAdapter is null in loadFoldersForSelection. Adapter setup issue?");
        }
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
        // Date Picker
            dueCalendar.set(Calendar.YEAR, year);
            dueCalendar.set(Calendar.MONTH, month);
            dueCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            // Time Picker (only if not all-day)
            if (!allDaySwitch.isChecked()) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        this,
                        (timeView, hourOfDay, minute) -> {
                            dueCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            dueCalendar.set(Calendar.MINUTE, minute);
                            updateDateTimeDisplay(dayDuePick, timeDuePick, dueCalendar);
                        },
                        dueCalendar.get(Calendar.HOUR_OF_DAY),
                        dueCalendar.get(Calendar.MINUTE),
                        true // 24-hour format
                );
                timePickerDialog.show();
            } else {
                // For all-day, just update date
                updateDateTimeDisplay(dayDuePick, timeDuePick, dueCalendar);
            }
        },
                dueCalendar.get(Calendar.YEAR),
                dueCalendar.get(Calendar.MONTH),
                dueCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateTimeDisplay(TextView dateTextView, TextView timeTextView, Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        dateTextView.setText(dateFormat.format(calendar.getTime()));
        if (timeTextView.getVisibility() == View.VISIBLE) {
            timeTextView.setText(timeFormat.format(calendar.getTime()));
        } else {
            // If time picker is GONE (all day), ensure time TextView is empty or not shown
            timeTextView.setText("");
        }
    }

    private void saveTask() {
        String title = addTaskTitle.getText().toString().trim();
        String description = descriptionHint.getText().toString().trim();
        String dueDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dueCalendar.getTime());
        String dueTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(dueCalendar.getTime());

        if (title.isEmpty()) {
            addTaskTitle.setError("Title cannot be empty");
            Toast.makeText(this, "Task title is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        long rawSelectedFolderId = addFolderAdapter.getSelectedFolderId();
        Long folderIdToAssign = (rawSelectedFolderId == -1) ? null : rawSelectedFolderId;
        Log.d(TAG, "Selected Folder ID from adapter: " + rawSelectedFolderId + " (converted to: " + folderIdToAssign + " for DB)");

        // Get selected folder ID from the adapter
        long selectedFolderId = addFolderAdapter.getSelectedFolderId();
        Log.d(TAG, "Selected Folder ID from adapter: " + selectedFolderId);

        Task newTask = new Task(
                title,             // String title
                description,       // String description
                dueDate,           // String dueDate
                dueTime,           // String dueTime
                false,             // boolean isDone
                userId,     // int userId
                folderIdToAssign   // Long folderId
        );
        
        int taskId = dbHelper.insertTask(newTask,userId, folderIdToAssign);

        if (taskId != -1) {
            Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Task saved with ID: " + taskId);

            if (selectedFolderId != -1) {
                // Retrieve the latest folder data from the database
                Folder folderToUpdate = dbHelper.getFolderById(selectedFolderId, userId);
                if (folderToUpdate != null) {
                    int currentComponents = folderToUpdate.getComponentsCount();
                    int newComponents = currentComponents + 1;
                    dbHelper.updateFolderTaskCount(selectedFolderId, newComponents, userId);
                    Log.d(TAG, "Updated folder ID " + selectedFolderId + " count from " + currentComponents + " to " + newComponents);
                } else {
                    Log.e(TAG, "Failed to retrieve folder with ID " + selectedFolderId + " to update count.");
                }
            } else {
                Log.d(TAG, "Task saved without a specific folder. Folder count not updated.");
            }

            Intent intent = new Intent(AddTaskActivity.this, MainActivity.class);
            intent.putExtra("CURRENT_USER_ID", userId); // pass the userId back to MainActivity
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onFolderClick(int position) {
        Folder clickedFolder = folderList.get(position);
        Toast.makeText(this, "Selected folder: " + clickedFolder.getFolderName(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Folder clicked: " + clickedFolder.getFolderName() + " at position " + position + " (ID: " + clickedFolder.getId() + ")");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFoldersForSelection();
    }

    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
            Log.d(TAG, "TaskDbHelper closed in onDestroy.");
        }
        super.onDestroy();
    }
}
