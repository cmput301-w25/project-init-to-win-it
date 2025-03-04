    package com.example.moodsync;

    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import java.util.List;

    public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder> {

        private List<MoodHistoryItem> moodList;
        private OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(MoodHistoryItem item);
        }

        public MoodHistoryAdapter(List<MoodHistoryItem> moodList) {
            this.moodList = moodList;
        }

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mood_card_item, parent, false);
            return new MoodViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
            MoodHistoryItem currentItem = moodList.get(position);

            holder.moodHeadingTextView.setText(currentItem.getMoodHeading());
            holder.moodEmojiTextView.setText(currentItem.getMoodEmoji());
            holder.moodDescriptionTextView.setText(currentItem.getMoodDescription());

            // Set background color based on mood (you can customize this)
            holder.itemView.setBackgroundColor(getMoodColor(currentItem.getMoodHeading()));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(currentItem);
                }
            });
        }

        @Override
        public int getItemCount() {
            return moodList.size();
        }

        public void updateData(List<MoodHistoryItem> newMoodList) {
            moodList.clear();
            moodList.addAll(newMoodList);
            notifyDataSetChanged();
        }

        private int getMoodColor(String mood) {
            // You can define colors for different moods
            switch (mood.toLowerCase()) {
                case "happy":
                    return 0xFFFFEB3B; // Yellow
                case "sad":
                    return 0xFF3F51B5; // Indigo
                case "excited":
                    return 0xFFFF9800; // Orange
                case "angry":
                    return 0xFFF44336; // Red
                default:
                    return 0xFFFFFFFF; // White
            }
        }

        static class MoodViewHolder extends RecyclerView.ViewHolder {

            TextView moodHeadingTextView;
            TextView moodEmojiTextView;
            TextView moodDescriptionTextView;

            public MoodViewHolder(@NonNull View itemView) {
                super(itemView);
                moodHeadingTextView = itemView.findViewById(R.id.moodHeadingTextView);
                moodEmojiTextView = itemView.findViewById(R.id.moodEmojiTextView);
                moodDescriptionTextView = itemView.findViewById(R.id.moodDescriptionTextView);
            }
        }
    }
