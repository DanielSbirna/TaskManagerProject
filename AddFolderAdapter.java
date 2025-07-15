package com.example.taskmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.models.Folder;

import java.util.List;

public class AddFolderAdapter extends RecyclerView.Adapter<AddFolderAdapter.AddFolderViewHolder> {

    private List<Folder> folderList;
    private OnItemClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // Tracks the currently selected folder

    public interface OnItemClickListener {
        void onFolderClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public AddFolderAdapter(List<Folder> folderList) {
        this.folderList = folderList;
    }

    // Method to update the data in the adapter
    public void setFolders(List<Folder> newFolders) {
        this.folderList.clear();
        this.folderList.addAll(newFolders);
        notifyDataSetChanged();
    }

    public long getSelectedFolderId() {
        if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < folderList.size()) {
            return folderList.get(selectedPosition).getId();
        }
        return -1; // No folder selected or invalid position
    }

    @NonNull
    @Override
    public AddFolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for folder items in the Add Task screen
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_choose_folders, parent, false);
        return new AddFolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddFolderViewHolder holder, int position) {
        Folder currentFolder = folderList.get(position);
        holder.folderNameTextView.setText(currentFolder.getFolderName());

        // Highlight selected item
        if (selectedPosition == position) {
            holder.folderButtonCardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.pressedButton));
        } else {
            holder.folderButtonCardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));
        }
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public class AddFolderViewHolder extends RecyclerView.ViewHolder {
        public TextView folderNameTextView;
        public CardView folderButtonCardView;

        public AddFolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderNameTextView = itemView.findViewById(R.id.folder_name_text);
            folderButtonCardView = itemView.findViewById(R.id.choose_folder_buton);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        // Update selected position and notify adapter for re-binding
                        int previousSelectedPosition = selectedPosition;
                        selectedPosition = position;
                        notifyItemChanged(previousSelectedPosition); // Un-highlight previous
                        notifyItemChanged(selectedPosition); // Highlight new

                        listener.onFolderClick(position);
                    }
                }
            });
        }
    }
}
