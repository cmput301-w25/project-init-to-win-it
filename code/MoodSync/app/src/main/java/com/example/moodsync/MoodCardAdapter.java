package com.example.moodsync;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.bumptech.glide.Glide;

import java.util.List;

public class MoodCardAdapter extends RecyclerView.Adapter<MoodCardAdapter.MoodCardViewHolder> {

    private final List<MoodEvent> moodEvents;

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
        holder.moodTextView.setText("Mood: " + moodEvent.mood);
        holder.triggerTextView.setText("Trigger: " + moodEvent.trigger);

        // Handle "View Details" button click
        holder.detailsButton.setOnClickListener(v -> showDetailsDialog(holder.itemView.getContext(), moodEvent));
    }

    @Override
    public int getItemCount() {
        return moodEvents != null ? moodEvents.size() : 0;
    }

    public void updateMoodEvents(List<MoodEvent> newMoodEvents) {
        this.moodEvents.clear();
        this.moodEvents.addAll(newMoodEvents);
        notifyDataSetChanged();
    }

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
        detailsMood.setText("Mood: " + moodEvent.mood);
        detailsTrigger.setText("Trigger: " + (moodEvent.trigger != null ? moodEvent.trigger : "N/A"));
        detailsDescription.setText("Description: " + (moodEvent.description != null ? moodEvent.description : "N/A"));
        detailsSocialSituation.setText("Social Situation: " + (moodEvent.socialSituation != null ? moodEvent.socialSituation : "N/A"));
        detailsLocation.setText("Location: " + (moodEvent.location != null ? moodEvent.location : "N/A"));

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

    static class MoodCardViewHolder extends RecyclerView.ViewHolder {
        TextView moodTextView;
        TextView triggerTextView;
        Button detailsButton;

        public MoodCardViewHolder(@NonNull View itemView) {
            super(itemView);
            moodTextView = itemView.findViewById(R.id.mood_text_view); // Matches XML ID for mood
            triggerTextView = itemView.findViewById(R.id.status); // Matches XML ID for trigger
            detailsButton = itemView.findViewById(R.id.details_button); // Matches XML ID for button
        }
    }
}