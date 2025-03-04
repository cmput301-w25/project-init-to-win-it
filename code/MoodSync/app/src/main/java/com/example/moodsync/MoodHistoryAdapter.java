package com.example.moodsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MoodHistoryAdapter extends RecyclerView.Adapter<MoodHistoryAdapter.MoodHistoryViewHolder> {

    private List<MoodHistoryItem> moodHistoryItems;

    public MoodHistoryAdapter(List<MoodHistoryItem> moodHistoryItems) {
        this.moodHistoryItems = moodHistoryItems;
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
        holder.moodHeadingTextView.setText(item.getMoodHeading());
        holder.moodEmojiTextView.setText(item.getMoodEmoji());
    }

    @Override
    public int getItemCount() {
        return moodHistoryItems.size();
    }

    static class MoodHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView moodHeadingTextView;
        TextView moodEmojiTextView;

        MoodHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            moodHeadingTextView = itemView.findViewById(R.id.moodHeadingTextView);
            moodEmojiTextView = itemView.findViewById(R.id.moodEmojiTextView);
        }
    }
}
