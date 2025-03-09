package com.example.moodsync;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MoodHistoryViewHolder> {

    private final List<MoodHistoryItem> moodHistoryItems;
    private final Context context;
    private final FirebaseFirestore db;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(MoodHistoryItem item);
    }

    public MoodHistoryAdapter(List<MoodHistoryItem> moodHistoryItems, Context context) {
        this.moodHistoryItems = moodHistoryItems;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MoodHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mood_card_item, parent, false);
        return new MoodHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodHistoryViewHolder holder, int position) {
        MoodHistoryItem currentItem = moodHistoryItems.get(position);

        holder.moodTextView.setText(currentItem.getMood() != null ? currentItem.getMood() : "No Mood");
        holder.emojiTextView.setText(getEmojiForMood(currentItem.getMood()));
        holder.descriptionTextView.setText(currentItem.getDescription() != null ? currentItem.getDescription() : "No Description");

        // i set the background color of the inner layout based on mood
        holder.moodBackground.setBackgroundColor(getMoodColor(currentItem.getMood()));

        // i adjusted text colors based on background color for better readability
        adjustTextColors(holder, currentItem.getMood());

        holder.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(currentItem, position));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentItem);
            }
        });
    }

    private String getEmojiForMood(String mood) {
        if (mood == null) {
            return "😐";
        }

        switch (mood.toLowerCase()) {
            case "happy":
                return "😄";
            case "sad":
                return "😞";
            case "angry":
                return "😠";
            case "confused":
                return "😕";
            case "surprised":
                return "😲";
            case "ashamed":
                return "😳";
            case "scared":
                return "😨";
            case "disgusted":
                return "🤢";
            default:
                return "😐";
        }
    }

    private void adjustTextColors(MoodHistoryViewHolder holder, String mood) {
        // using consistent text colors that work well with all our pastel backgrounds
        holder.moodTextView.setTextColor(0xFF333333);
        holder.descriptionTextView.setTextColor(0xFF555555);
    }

    private int getMoodColor(String mood) {
        if (mood == null) {
            return 0xFFFFFFFF; // Default white
        }

        switch (mood.toLowerCase()) {
            case "happy":
                return 0xFFFFF8E1; // Soft Yellow
            case "sad":
                return 0xFFE3F2FD; // Soft Blue
            case "angry":
                return 0xFFFFEBEE; // Soft Red
            case "confused":
                return 0xFFF3E5F5; // Soft Purple
            case "surprised":
                return 0xFFE0F7FA; // Soft Cyan
            case "ashamed":
                return 0xFFEFEBE9; // Soft Brown
            case "scared":
                return 0xFFECEFF1; // Soft Blue Grey
            case "disgusted":
                return 0xFFE8F5E9; // Soft Green
            default:
                return 0xFFF5F5F5; // Light Grey
        }
    }

    @Override
    public int getItemCount() {
        return moodHistoryItems.size();
    }

    private void showDeleteConfirmationDialog(MoodHistoryItem item, int position) {
        // Create a custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.BottomSheetDialogTheme);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.delete_confirmation_dialog, null);
        builder.setView(dialogView);

        // Get references to dialog views
        TextView titleTextView = dialogView.findViewById(R.id.dialog_title);
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancel_button);
        MaterialButton deleteButton = dialogView.findViewById(R.id.delete_button);


        titleTextView.setText("Delete Mood");
        messageTextView.setText("Are you sure you want to delete this mood entry? This action cannot be undone.");

        AlertDialog dialog = builder.create();


        cancelButton.setOnClickListener(v -> dialog.dismiss());
        deleteButton.setOnClickListener(v -> {
            deleteMoodFromFirestore(item, position);
            dialog.dismiss();
        });


        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            layoutParams.gravity = Gravity.BOTTOM;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }


        // Show the dialog
        dialog.show();
    }

    private void deleteMoodFromFirestore(MoodHistoryItem item, int position) {
        db.collection("mood_events").document(item.getId())
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
        MaterialButton deleteButton;
        RelativeLayout moodBackground;

        public MoodHistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            // Match these IDs with your XML layout file
            moodTextView = itemView.findViewById(R.id.mood_text_view);
            emojiTextView = itemView.findViewById(R.id.moodEmojiTextView);
            descriptionTextView = itemView.findViewById(R.id.moodDescriptionTextView);
            deleteButton = itemView.findViewById(R.id.delete_button);
            moodBackground = itemView.findViewById(R.id.mood_background);
        }
    }
}
