package com.example.taskmanager.ui.theme;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
    private ImageButton backButton;
    private EditText addTaskTitle;
    private EditText descriptionHint;
    private RecyclerView recyclerViewFolder;
    private AddFolderAdapter addFolderAdapter;
    private List<Folder> folderList;
    private Switch allDaySwitch;
    private ConstraintLayout startDatePickerLayout;
    private ConstraintLayout endDatePickerLayout;
    private TextView dayStartPick;
    private TextView timeStartPick;
    private TextView dayEndPick;
    private TextView timeEndPick;
    private TextView saveButton;
    private TaskDbHelper dbHelper;

    private Calendar startCalendar;
    private Calendar endCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task_screen);

        dbHelper = new TaskDbHelper(this); // Initialize DB Helper

        // Initialize UI elements
        backButton = findViewById(R.id.back_button);
        addTaskTitle = findViewById(R.id.add_task_title);
        descriptionHint = findViewById(R.id.description_hint);
        recyclerViewFolder = findViewById(R.id.recycler_view_folder);
        allDaySwitch = findViewById(R.id.all_day_switch);
        startDatePickerLayout = findViewById(R.id.start_date_picker);
        endDatePickerLayout = findViewById(R.id.end_date_picker);
        dayStartPick = findViewById(R.id.day_start_pick);
        timeStartPick = findViewById(R.id.time_start_pick);
        dayEndPick = findViewById(R.id.day_end_pick);
        timeEndPick = findViewById(R.id.time_end_pick);
        saveButton = findViewById(R.id.save_button);

        // Initialize Calendars with current time
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        endCalendar.add(Calendar.HOUR_OF_DAY, 1); // Default end time 1 hour after start

        // Set initial date/time display
        updateDateTimeDisplay(dayStartPick, timeStartPick, startCalendar);
        updateDateTimeDisplay(dayEndPick, timeEndPick, endCalendar);


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
            startActivity(intent);
            finish();
        });

        // All Day Switch listener
        allDaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // If "All Day" is checked, hide time pickers and set default all-day times
                timeStartPick.setVisibility(View.GONE);
                timeEndPick.setVisibility(View.GONE);
                // Set start of day and end of day
                startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                startCalendar.set(Calendar.MINUTE, 0);
                endCalendar.set(Calendar.HOUR_OF_DAY, 23);
                endCalendar.set(Calendar.MINUTE, 59);
            } else {
                // If "All Day" is unchecked, show time pickers
                timeStartPick.setVisibility(View.VISIBLE);
                timeEndPick.setVisibility(View.VISIBLE);
                // Reset times to current or a default interval if needed
                startCalendar = Calendar.getInstance();
                endCalendar = Calendar.getInstance();
                endCalendar.add(Calendar.HOUR_OF_DAY, 1);
            }
            updateDateTimeDisplay(dayStartPick, timeStartPick, startCalendar);
            updateDateTimeDisplay(dayEndPick, timeEndPick, endCalendar);
        });
        // Date and Time Picker Listeners
        startDatePickerLayout.setOnClickListener(v -> showDateTimePicker(true));
        endDatePickerLayout.setOnClickListener(v -> showDateTimePicker(false));

        // Save Button Listener
        saveButton.setOnClickListener(v -> saveTask());
    }

    private void loadFoldersForSelection() {
        Log.d(TAG, "Loading folders for selection...");
        List<Folder> folders = dbHelper.getAllFolders();
        // Add a "No Folder" option at the beginning if it doesn't already exist
        boolean noFolderExists = false;
        for (Folder f : folders) {
            if ("No Folder".equals(f.getFolderName())) {
                noFolderExists = true;
                break;
            }
        }
        if (!noFolderExists) {
            folders.add(0, new Folder(-1, "No Folder", 0)); // Use a specific ID like -1 for "No Folder"
            Log.d(TAG, "Added 'No Folder' option.");
        }
        folderList.clear();
        folderList.addAll(folders);
        if (addFolderAdapter != null) {
            addFolderAdapter.notifyDataSetChanged();
            Log.d(TAG, "Folder selection adapter notified. Current folderList size: " + folderList.size());
        }
    }

    private void showDateTimePicker(final boolean isStart) {
        final Calendar currentCalendar = isStart ? startCalendar : endCalendar;

        // Date Picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    currentCalendar.set(Calendar.YEAR, year);
                    currentCalendar.set(Calendar.MONTH, month);
                    currentCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Time Picker (only if not all-day)
                    if (!allDaySwitch.isChecked()) {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                this,
                                (timeView, hourOfDay, minute) -> {
                                    currentCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    currentCalendar.set(Calendar.MINUTE, minute);
                                    updateDateTimeDisplay(isStart ? dayStartPick : dayEndPick, isStart ? timeStartPick : timeEndPick, currentCalendar);
                                },
                                currentCalendar.get(Calendar.HOUR_OF_DAY),
                                currentCalendar.get(Calendar.MINUTE),
                                true // 24-hour format
                        );
                        timePickerDialog.show();
                    } else {
                        // For all-day, just update date
                        updateDateTimeDisplay(isStart ? dayStartPick : dayEndPick, isStart ? timeStartPick : timeEndPick, currentCalendar);
                    }
                },
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateTimeDisplay(TextView dateTextView, TextView timeTextView, Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        dateTextView.setText(dateFormat.format(calendar.getTime()));
        if (timeTextView.getVisibility() == View.VISIBLE) {
            timeTextView.setText(timeFormat.format(calendar.getTime()));
        }
    }

    private void saveTask() {
        String title = addTaskTitle.getText().toString().trim();
        String description = descriptionHint.getText().toString().trim();
        String dueDate = dayStartPick.getText().toString(); // Using start date as due date
        String dueTime = timeStartPick.getText().toString(); // Using start time as due time

        if (title.isEmpty()) {
            addTaskTitle.setError("Title cannot be empty");
            Toast.makeText(this, "Task title is required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected folder ID from the adapter
        long selectedFolderId = addFolderAdapter.getSelectedFolderId();
        Log.d(TAG, "Selected Folder ID from adapter: " + selectedFolderId);

        Task newTask = new Task(title, description, dueDate, dueTime, false); // New tasks are not completed by default
        long taskId = dbHelper.insertTask(newTask, selectedFolderId);

        if (taskId != -1) {
            Toast.makeText(this, "Task saved successfully!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Task saved with ID: " + taskId);

            if (selectedFolderId != -1) {
                // Retrieve the latest folder data from the database
                Folder folderToUpdate = dbHelper.getFolderById(selectedFolderId);
                if (folderToUpdate != null) {
                    int currentComponents = folderToUpdate.getComponents();
                    int newComponents = currentComponents + 1;
                    dbHelper.updateFolderTaskCount(selectedFolderId, newComponents);
                    Log.d(TAG, "Updated folder ID " + selectedFolderId + " count from " + currentComponents + " to " + newComponents);
                } else {
                    Log.e(TAG, "Failed to retrieve folder with ID " + selectedFolderId + " to update count.");
                }
            } else {
                Log.d(TAG, "Task saved without a specific folder. Folder count not updated.");
            }

            Intent intent = new Intent(AddTaskActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to save task.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to save task to database.");
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
