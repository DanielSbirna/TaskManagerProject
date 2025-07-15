package com.example.taskmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.CompoundButton; // CheckBox listener

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.models.Task;

import java.util.List;

import android.content.Context;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList;
    private OnItemClickListener listener;

    // Constructor initializes the adapter with the list of tasks
    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    // Method to update in the data adapter
    public void setTasks(List<Task> newTasks) {
        this.taskList.clear();
        this.taskList.addAll(newTasks);
        notifyDataSetChanged();
    }

    // Allows MainActivity to listen for events.
    public interface OnItemClickListener {
        void onTaskClick(int position); // For general click on the entire task item
        void onTaskCheckChanged(int position, boolean isChecked); // For when the checkbox state changes
    }

    // Method to set the click listener from MainActivity
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This method is called when a new ViewHolder needs to be created.
        // It inflates the layout for a single task item.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_tasks, parent, false); // Inflates recycler_view_tasks.xml
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = taskList.get(position);

        holder.taskTitleTextView.setText(currentTask.getTitle());
        holder.taskDescriptionTextView.setText(currentTask.getDescription());

        Context context = holder.itemView.getContext();
        holder.taskDueDateTextView.setText(
                context.getString(R.string.task_due_date_time, currentTask.getDueDate(), currentTask.getDueTime())
        );

        holder.taskCompletedCheckBox.setOnCheckedChangeListener(null);
        holder.taskCompletedCheckBox.setChecked(currentTask.getIsDone());
        holder.taskCompletedCheckBox.setOnCheckedChangeListener((@NonNull CompoundButton buttonView, boolean isChecked) -> {
            if (listener != null) {
                currentTask.setIsDone(isChecked);
                listener.onTaskCheckChanged(holder.getBindingAdapterPosition(), isChecked); // Replaced getAdapterPosition()
            }
        });

        holder.itemView.setOnClickListener((@NonNull View v) -> {
            if (listener != null) {
                listener.onTaskClick(holder.getBindingAdapterPosition()); // Replaced getAdapterPosition()
            }
        });
    }

    @Override
    public int getItemCount() {
        // Returns the total number of items in the data set held by the adapter.
        return taskList.size();
    }


    // TaskViewHolder Class

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public TextView taskTitleTextView;
        public TextView taskDescriptionTextView;
        public TextView taskDueDateTextView;
        public CheckBox taskCompletedCheckBox;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find and assign the views from list_item_task.xml using their IDs
            taskTitleTextView = itemView.findViewById(R.id.task_title);
            taskDescriptionTextView = itemView.findViewById(R.id.task_description);
            taskDueDateTextView = itemView.findViewById(R.id.task_due_date);
            taskCompletedCheckBox = itemView.findViewById(R.id.checkbox_task_completed); // Use your checkbox's ID
        }
    }
}
