package com.example.taskmanager.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.models.Folder;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private static final String TAG = "FolderAdapter";
    private List<Folder> folderList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onFolderClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public FolderAdapter(List<Folder> folderList) {
        this.folderList = folderList;
        Log.d(TAG, "FolderAdapter initialized with " + folderList.size() + " folders.");
    }

    // Method to update the data in the adapter
    public void setFolders(List<Folder> newFolders) {
        this.folderList.clear();
        this.folderList.addAll(newFolders);
        notifyDataSetChanged();
        Log.d(TAG, "Folders updated in adapter via setFolders. New count: " + this.folderList.size());
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Inflating recycler_view_folders.xml for viewType: " + viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_folders, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        if (position < folderList.size()) {
            Folder currentFolder = folderList.get(position);
            holder.folderNameTextView.setText(currentFolder.getFolderName());
            holder.folderComponentsTextView.setText(String.format("%d tasks", currentFolder.getComponents()));
            Log.d(TAG, "onBindViewHolder: Binding folder '" + currentFolder.getFolderName() + "' (ID: " + currentFolder.getId() + ") at position " + position + ". Tasks: " + currentFolder.getComponents());
        } else {
            Log.e(TAG, "onBindViewHolder: Invalid position " + position + ". folderList size: " + folderList.size());
        }
    }

    @Override
    public int getItemCount() {
        int count = folderList.size();
        Log.d(TAG, "getItemCount: Returning " + count);
        return count;
    }

    public class FolderViewHolder extends RecyclerView.ViewHolder {
        public TextView folderNameTextView;
        public TextView folderComponentsTextView;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                folderNameTextView = itemView.findViewById(R.id.folder_name_text);
                folderComponentsTextView = itemView.findViewById(R.id.folder_number);
                if (folderNameTextView == null || folderComponentsTextView == null) {
                    Log.e(TAG, "FolderViewHolder: One or more TextViews not found. Check recycler_view_folders.xml IDs.");
                } else {
                    Log.d(TAG, "FolderViewHolder: Views found successfully.");
                }
            } catch (Exception e) {
                Log.e(TAG, "FolderViewHolder: Error finding views. Check recycler_view_folders.xml IDs.", e);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onFolderClick(position);
                        Log.d(TAG, "Item clicked at position: " + position);
                    } else {
                        Log.w(TAG, "Item click: Position is NO_POSITION.");
                    }
                } else {
                    Log.w(TAG, "Item click: Listener is null.");
                }
            });
        }
    }
}
