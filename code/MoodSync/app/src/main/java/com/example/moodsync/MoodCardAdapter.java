package com.example.moodsync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MoodCardAdapter extends RecyclerView.Adapter<MoodCardAdapter.MoodCardViewHolder> {

    private List<MoodEvent> moodEvents;

    // Constructor
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
        // Leave this method empty to display the XML layout without setting any text
    }

    @Override
    public int getItemCount() {
        return moodEvents != null ? moodEvents.size() : 0; // Ensure null safety
    }

    static class MoodCardViewHolder extends RecyclerView.ViewHolder {
        public MoodCardViewHolder(@NonNull View itemView) {
            super(itemView);
            // No need to bind views if you’re not setting any data for now
        }
    }
}
