package com.example.moodsync;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MoodHistoryViewHolder> {

    private final List<MoodHistoryItem> moodHistoryItems;
    private final Context context;
    private final FirebaseFirestore db;

    public MoodHistoryAdapter(List<MoodHistoryItem> moodHistoryItems, Context context) {
        this.moodHistoryItems = moodHistoryItems;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public MoodHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mood_card_item, parent, false);
        return new MoodHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodHistoryViewHolder holder, int position) {
        MoodHistoryItem item = moodHistoryItems.get(position);

        // Bind data to the views with null checks to avoid NullPointerException
        holder.moodTextView.setText(item.getMood() != null ? item.getMood() : "No Mood");
        holder.emojiTextView.setText(item.getEmoji() != null ? item.getEmoji() : "");
        holder.descriptionTextView.setText(item.getDescription() != null ? item.getDescription() : "No Description");

        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(item, position));
    }

    @Override
    public int getItemCount() {
        return moodHistoryItems.size();
    }

    private void showDeleteConfirmationDialog(MoodHistoryItem item, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this mood?")
                .setCancelable(false)
                .setPositiveButton("Confirm Delete", (dialog, which) -> deleteMoodFromFirestore(item, position))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteMoodFromFirestore(MoodHistoryItem item, int position) {
        db.collection("mood_events").document(item.getId()) // Ensure you have an ID in your MoodHistoryItem
                .delete()
                .addOnSuccessListener(aVoid -> {
                    moodHistoryItems.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, moodHistoryItems.size());
                })
                .addOnFailureListener(e -> {
                    // Handle the error (optional logging)
                    e.printStackTrace();
                });
    }

    static class MoodHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView moodTextView;
        TextView emojiTextView;
        TextView descriptionTextView;
        Button deleteButton;

        public MoodHistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            // Match these IDs with your XML layout file (mood_card_item.xml)
            moodTextView = itemView.findViewById(R.id.mood_text_view);
            emojiTextView = itemView.findViewById(R.id.moodEmojiTextView);
            descriptionTextView = itemView.findViewById(R.id.moodDescriptionTextView);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
