package com.example.moodsync;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;



import java.util.List;
/**
 * Adapter for displaying MoodEvent objects in a RecyclerView as mood cards.
 * This adapter binds the data from MoodEvent objects to the views within each card,
 * allowing users to see a summary of their recorded moods.
 */
public class MoodCardAdapter extends RecyclerView.Adapter<MoodCardAdapter.MoodCardViewHolder> {

    private final List<MoodEvent> moodEvents;

    /**
     * Constructs a MoodCardAdapter with the provided list of MoodEvent objects.
     * @param moodEvents The list of MoodEvent objects to display.
     */
    public MoodCardAdapter(List<MoodEvent> moodEvents) {
        this.moodEvents = moodEvents;
    }


    @NonNull
    @Override
    public MoodCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_mood_card_item, parent, false);
        return new MoodCardViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MoodCardViewHolder holder, int position) {
        MoodEvent moodEvent = moodEvents.get(position);

        // Bind mood and trigger to TextViews
        holder.moodTextView.setText("Mood: " + moodEvent.getMood());
        holder.triggerTextView.setText("Trigger: " + moodEvent.getTrigger());

        holder.itemView.setBackgroundColor(getMoodColor(moodEvent.getMood()));

        // Handle "View Details" button click
        holder.detailsButton.setOnClickListener(v -> showDetailsDialog(holder.itemView.getContext(), moodEvent));
    }

    /**
     * Determines the background color of the mood card based on the mood.
     * @param mood The mood string.
     * @return The corresponding color resource ID.
     */
    private int getMoodColor(String mood) {
        switch (mood.toLowerCase()) {
            case "happy":
                return 0xFFFFEB3B;
            case "sad":
                return 0xFF3F51B5;
            case "angry":
                return 0xFFF44336;
            case "confused":
                return 0xFF9C27B0;
            case "surprised":
                return 0xFF00BCD4;
            case "ashamed":
                return 0xFF795548;
            case "scared":
                return 0xFF607D8B;
            case "disgusted":
                return 0xFF4CAF50;
            default:
                return 0xFFFFFFFF;
        }
    }



    @Override
    public int getItemCount() {
        return moodEvents != null ? moodEvents.size() : 0;
    }

    /**
     * Updates the list of MoodEvent objects in the adapter and refreshes the RecyclerView.
     * @param newMoodEvents The new list of MoodEvent objects.
     */
    public void updateMoodEvents(List<MoodEvent> newMoodEvents) {
        this.moodEvents.clear();
        this.moodEvents.addAll(newMoodEvents);
        notifyDataSetChanged();
    }

    /**
     * Shows a detailed view of a MoodEvent in a popup dialog.
     * @param context The context in which to show the dialog.
     * @param moodEvent The MoodEvent object to display.
     */
    private void showDetailsDialog(Context context, MoodEvent moodEvent) {
        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.popup_details, null);

        // Bind views in the dialog layout
        TextView detailsMood = dialogView.findViewById(R.id.details_mood);
        TextView detailsTrigger = dialogView.findViewById(R.id.details_trigger);
        TextView detailsDescription = dialogView.findViewById(R.id.details_description);
        TextView detailsSocialSituation = dialogView.findViewById(R.id.details_social_situation);
        TextView detailsLocation = dialogView.findViewById(R.id.details_location);
        //ImageView detailsImage = dialogView.findViewById(R.id.details_image);

        // Set data to views
        detailsMood.setText("Mood: " + moodEvent.getMood());
        detailsTrigger.setText("Trigger: " + (moodEvent.getTrigger() != null ? moodEvent.getTrigger() : "N/A"));
        detailsDescription.setText("Description: " + (moodEvent.getDescription() != null ? moodEvent.getDescription() : "N/A"));
        detailsSocialSituation.setText("Social Situation: " + (moodEvent.getSocialSituation() != null ? moodEvent.getSocialSituation() : "N/A"));
        detailsLocation.setText("Location: " + (moodEvent.getLocation() != null ? moodEvent.getLocation() : "N/A"));

//        // Handle image loading or hiding
//        if (moodEvent.getImageUrl() != null && !moodEvent.getImageUrl().isEmpty()) {
//            Glide.with(context)
//                    .load(moodEvent.getImageUrl())
//                    .into(detailsImage);
//            detailsImage.setVisibility(View.VISIBLE);
//        } else {
//            detailsImage.setVisibility(View.GONE); // Hide Image if no URL is provided
//        }

        // Build and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Mood Details")
                .setCancelable(true)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .setView(dialogView);

        builder.create().show();
    }

    /**
     * ViewHolder class that holds the views for each mood card item in the RecyclerView.
     */
    static class MoodCardViewHolder extends RecyclerView.ViewHolder {
        TextView moodTextView;
        TextView triggerTextView;
        Button detailsButton;

        /**
         * Constructs a MoodCardViewHolder.
         * @param itemView The item view.
         */
        public MoodCardViewHolder(@NonNull View itemView) {
            super(itemView);
            moodTextView = itemView.findViewById(R.id.mood_text_view); // Matches XML ID for mood
            triggerTextView = itemView.findViewById(R.id.status); // Matches XML ID for trigger
            detailsButton = itemView.findViewById(R.id.details_button); // Matches XML ID for button
        }
    }
}