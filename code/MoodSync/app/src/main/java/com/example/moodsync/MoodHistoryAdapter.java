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
            holder.emojiTextView.setText(currentItem.getEmoji() != null ? currentItem.getEmoji() : "");
            holder.descriptionTextView.setText(currentItem.getDescription() != null ? currentItem.getDescription() : "No Description");

            holder.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(currentItem, position));

            holder.itemView.setBackgroundColor(getMoodColor(currentItem.getMood()));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(currentItem);
                }
            });
        }

        private int getMoodColor(String mood) {
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
            static TextView moodTextView;
            static TextView emojiTextView;
            static TextView descriptionTextView;
            Button deleteButton;

            public MoodHistoryViewHolder(@NonNull View itemView) {
                super(itemView);

                // Match these IDs with your XML layout file (mood_card_item.xml)
                moodTextView = itemView.findViewById(R.id.mood_text_view);
                emojiTextView = itemView.findViewById(R.id.moodEmojiTextView);
                descriptionTextView = itemView.findViewById(R.id.moodDescriptionTextView);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }


            static class moodHistoryViewHolder extends RecyclerView.ViewHolder {
                TextView moodTextView;
                TextView emojiTextView;
                TextView descriptionTextView;
                Button deleteButton;

                public moodHistoryViewHolder(@NonNull View itemView) {
                    super(itemView);

                    // Match these IDs with your XML layout file (mood_card_item.xml)
                    moodTextView = itemView.findViewById(R.id.mood_text_view);
                    emojiTextView = itemView.findViewById(R.id.moodEmojiTextView);
                    descriptionTextView = itemView.findViewById(R.id.moodDescriptionTextView);
                    deleteButton = itemView.findViewById(R.id.delete_button);
                }
            }
        }
    }
