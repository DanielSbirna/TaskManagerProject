package com.example.taskmanager;

import android.os.Bundle;
import android.widget.Toast; //Optional Toast messages

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.taskmanager.adapters.TaskAdapter;
import com.example.taskmanager.models.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> tasksList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);// Links XML
        recyclerView= findViewById(R.id.recycler_view_tasks);

        //list to hold Task objects
        tasksList = new ArrayList<>();

        //examples
        tasksList.add(new Task("Groceryes", "Milk, Banana, Oats", "07.07.2025", "18:00", false));
        tasksList.add(new Task("Add to Github", "add the new files in you project to the github", "07.07.2025", "15:00", false));
        tasksList.add(new Task("Meal Prep", "prepare the next meal", "07.07.2025", "07:30", true));
        tasksList.add(new Task("Gym", "train chest & triceps", "07.07.2025", "17:30", false));

        //LinearLayoutManager for the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //initialize TaskAdapter with the list of tasks
        taskAdapter = new TaskAdapter(tasksList);

        //set adapter to recycler
        recyclerView.setAdapter(taskAdapter);

        taskAdapter.setOnItemClickListener(this);

        //Floating action button
        FloatingActionButton fabAddTask = findViewById(R.id.add_task_button);

        fabAddTask.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Add new task clicked!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onTaskClick(int position) {
        Task clickedTask = tasksList.get(position);
        Toast.makeText(this, "Clicked: " + clickedTask.getTitle(), Toast.LENGTH_SHORT).show();
        //Code to save the updated task in storage
    }


    @Override
    public void onTaskCheckChanged(int position, boolean isChecked) {
        Task changedTask = tasksList.get(position);
        if(isChecked){
            Toast.makeText(this, "Task " + changedTask.getTitle() + " completed!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Task " + changedTask.getTitle() + " uncompleted", Toast.LENGTH_SHORT).show();
        }
        //Code to save the updated task in storage
    }
}
