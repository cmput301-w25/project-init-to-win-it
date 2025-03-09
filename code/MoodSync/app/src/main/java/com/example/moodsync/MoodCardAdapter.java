package com.example.moodsync;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.concurrent.CompletableFuture;

//import com.bumptech.glide.Glide;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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

        CompletableFuture<Bitmap> bitmapFuture = BitmapUtils.getBitmapFromUrl(holder.itemView.getContext(), moodEvent.getImageUrl()); // Use context here!

        bitmapFuture.thenAccept(bitmap -> {
            if (bitmap != null) {
                holder.moodBanner.post(() -> {
                    try {
                        holder.moodBanner.setBackground(new BitmapDrawable(holder.itemView.getContext().getResources(), bitmap));
                    } catch (Exception e) {
                        Log.e("BitmapDraw", "Error setting background", e);
                    }
                });
            }
        }).exceptionally(throwable -> {
            Log.e("BitmapUtils", "Bitmap load failed: " + throwable.getMessage());
            throwable.printStackTrace();
            return null;
        });
        holder.moodTextView.setText("Mood: " + moodEvent.getMood());
        holder.triggerTextView.setText("Trigger: " + moodEvent.getTrigger());
        holder.moodDescrip.setText(moodEvent.getDescription());

        // Set the background color of the mood_banner based on the mood
        int moodColor = getMoodColor(moodEvent.getMood());
        holder.moodBanner.setBackgroundColor(moodColor);

        // Set appropriate emoji based on mood
        if (holder.moodEmoji != null) {
            setMoodEmoji(holder.moodEmoji, moodEvent.getMood());
        }

        // Handle "View Details" button click
        holder.detailsButton.setOnClickListener(v -> showDetailsDialog(holder.itemView.getContext(), moodEvent));
    }

    private void setMoodEmoji(TextView textView, String mood) {
        if (mood == null) {
            textView.setText("😐");
            return;
        }

        switch (mood.toLowerCase()) {
            case "happy":
                textView.setText("😄");
                break;
            case "sad":
                textView.setText("😞");
                break;
            case "angry":
                textView.setText("😠");
                break;
            case "confused":
                textView.setText("😕");
                break;
            case "surprised":
                textView.setText("😲");
                break;
            case "ashamed":
                textView.setText("😳");
                break;
            case "scared":
                textView.setText("😨");
                break;
            case "disgusted":
                textView.setText("🤢");
                break;
            default:
                textView.setText("😐");
                break;
        }
    }

    private int getMoodColor(String mood) {
        if (mood == null) {
            return 0xFFFFFFFF; // Default white if mood is null
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
                return 0xFFFFFFFF; // Pure White
        }
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
        View dialogView = LayoutInflater.from(context).inflate(R.layout.popup_details2, null);
        TextView detailsMood = dialogView.findViewById(R.id.details_mood);
        TextView detailsTrigger = dialogView.findViewById(R.id.details_trigger);
        TextView detailsDescription = dialogView.findViewById(R.id.details_description);
        TextView detailsSocialSituation = dialogView.findViewById(R.id.details_social_situation);
        TextView detailsLocation = dialogView.findViewById(R.id.details_location);
        LinearLayout headerLayout = dialogView.findViewById(R.id.dialog_header);
        TextView emojiView = dialogView.findViewById(R.id.details_emoji);
        headerLayout.setBackgroundColor(getMoodColor(moodEvent.getMood()));
        setMoodEmoji(emojiView, moodEvent.getMood());
        detailsMood.setText("Mood: " + moodEvent.getMood());
        detailsTrigger.setText("Trigger: " + (moodEvent.getTrigger() != null ? moodEvent.getTrigger() : "N/A"));
        detailsDescription.setText("Description: " + (moodEvent.getDescription() != null ? moodEvent.getDescription() : "N/A"));
        detailsSocialSituation.setText("Social Situation: " + (moodEvent.getSocialSituation() != null ? moodEvent.getSocialSituation() : "N/A"));
        detailsLocation.setText("Location: " + (moodEvent.getLocation() != null ? moodEvent.getLocation() : "N/A"));
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.BottomSheetDialogTheme);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        Button closeButton = dialogView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dialog.dismiss());
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
            layoutParams.gravity = Gravity.BOTTOM;  // This part is fine
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // **ADD THIS LINE**
            dialog.getWindow().setAttributes(layoutParams);
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        }

        dialog.show();
    }

    static class MoodCardViewHolder extends RecyclerView.ViewHolder {
        TextView moodTextView;
        TextView triggerTextView;
        Button detailsButton;
        ImageView moodBanner;
        TextView moodEmoji;
        TextView moodDescrip;

        public MoodCardViewHolder(@NonNull View itemView) {
            super(itemView);
            moodTextView = itemView.findViewById(R.id.mood_text_view); // Matches XML ID for mood
            triggerTextView = itemView.findViewById(R.id.trigger_text_view); // Matches XML ID for trigger
            detailsButton = itemView.findViewById(R.id.details_button); // Matches XML ID for button
            moodBanner = itemView.findViewById(R.id.post_image); // Reference to the mood banner
            moodDescrip = itemView.findViewById(R.id.status);
            // Commented out as per your code
             moodEmoji = itemView.findViewById(R.id.details_emoji); // Reference to the mood emoji
        }
    }
}
