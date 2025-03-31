package com.example.moodsync;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@code MoodHistoryAdapter} is a RecyclerView adapter that displays a list of mood history items.
 * It allows users to view their past mood entries, each represented as a card with details
 * such as the mood, an associated emoji, a short description, and the time elapsed since
 * the entry was created. Users can also delete mood entries through a confirmation dialog.
 *
 * <p>The adapter uses a custom ViewHolder to efficiently manage and update the views for
 * each mood history item.</p>
 */
public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MoodHistoryViewHolder> {

    private final List<MoodHistoryItem> moodHistoryItems;
    private final Context context;
    private final FirebaseFirestore db;
    private OnItemClickListener listener;
    public LocalStorage globalStorage = LocalStorage.getInstance();

    public interface OnItemClickListener {
        void onItemClick(MoodHistoryItem item);
    }

    /**
     * Constructs a new {@code MoodHistoryAdapter} with the provided list of mood history items and context.
     * Initializes the Firebase Firestore instance for database operations.
     *
     * @param moodHistoryItems The list of {@link MoodHistoryItem} objects to display.
     * @param context The context in which the adapter is being created, typically an Activity or Fragment.
     */
    public MoodHistoryAdapter(List<MoodHistoryItem> moodHistoryItems, Context context) {
        this.moodHistoryItems = moodHistoryItems;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }


    /**
     * Sets the item click listener for the adapter. This listener will be notified when a user clicks
     * on a mood history item in the RecyclerView.
     *
     * @param listener The {@link OnItemClickListener} to set. If null, no item click events will be handled.
     */
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

        long timeElapsed = System.currentTimeMillis() - currentItem.getDate().getTime();
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeElapsed) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeElapsed) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(timeElapsed) % 24;
        long days = TimeUnit.MILLISECONDS.toDays(timeElapsed);

        String elapsedTime = String.format(" %d seconds ago", seconds);
        if (days!=0){
             elapsedTime = String.format("%d d %d h ago", days, hours);
        }
        else if(hours!=0){
             elapsedTime = String.format("%d h %d minutes ago", hours, minutes);
        }
        else if(minutes!=0){
             elapsedTime = String.format("%d minutes ago", seconds);
        }

        holder.dateTextView.setText(elapsedTime);



        // i adjusted text colors based on background color for better readability
        adjustTextColors(holder, currentItem.getMood());

        holder.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(currentItem, position));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                globalStorage.setCurrentMoodForEdit(currentItem.getDate().getTime()); // Set the millis for the current mood
                listener.onItemClick(currentItem);
            }
        });
    }

    /**
     * Returns the appropriate emoji based on the mood. If the mood is null or unrecognized,
     * it returns a neutral face emoji.
     *
     * @param mood The mood string for which to retrieve the corresponding emoji.
     * @return A string containing the emoji that represents the given mood.
     */
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

    /**
     * Adjusts the text colors of the mood and description TextViews in the ViewHolder based on the mood.
     * This is done to ensure that the text is readable against different background colors.
     *
     * @param holder The {@link MoodHistoryViewHolder} containing the text views to adjust.
     * @param mood The mood associated with the item, used to determine the appropriate text colors.
     */
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

    /**
     * Displays a confirmation dialog to the user before deleting a mood entry.
     * The dialog informs the user about the action and its consequences, and allows them
     * to either proceed with the deletion or cancel.
     *
     * @param item The {@link MoodHistoryItem} to be deleted, used to identify the mood entry in Firestore.
     * @param position The position of the item in the list, used to update the RecyclerView after deletion.
     */
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


    /**
     * Deletes a mood entry from Firestore and updates the RecyclerView to reflect the deletion.
     * This method first removes the item from Firestore, and upon successful deletion, it updates
     * the local list of mood history items and notifies the RecyclerView adapter to refresh the view.
     *
     * @param item The {@link MoodHistoryItem} to delete from Firestore. The item's ID is used to locate the document in Firestore.
     * @param position The position of the item in the RecyclerView, used to update the view after deletion.
     */
    private void deleteMoodFromFirestore(MoodHistoryItem item, int position) {
        moodHistoryItems.remove(position);
        globalStorage.removeMood(item.getDate().getTime());
        notifyItemRemoved(position);
        notifyItemChanged(position,moodHistoryItems.size());
        db.collection("mood_events").document(item.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                    // Handle the error (optional logging)
                    e.printStackTrace();
                });
    }

    static class MoodHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView moodTextView;
        TextView emojiTextView;
        TextView dateTextView;
        TextView descriptionTextView;
        AppCompatImageButton deleteButton;

        RelativeLayout moodBackground;

        /**
         * {@code MoodHistoryViewHolder} is a RecyclerView.ViewHolder that provides references to the views for each
         * mood history item. It is responsible for caching the views for each item to improve performance
         * by avoiding unnecessary findViewById calls.
         */
        public MoodHistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            // Match these IDs with your XML layout file
            moodTextView = itemView.findViewById(R.id.mood_text_view);
            emojiTextView = itemView.findViewById(R.id.moodEmojiTextView);
            descriptionTextView = itemView.findViewById(R.id.moodDescriptionTextView);
            deleteButton = itemView.findViewById(R.id.delete_button);
            dateTextView = itemView.findViewById(R.id.date_text_view);


        }
    }
}
