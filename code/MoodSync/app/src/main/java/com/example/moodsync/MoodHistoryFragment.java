package com.example.moodsync;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodsync.databinding.MoodHistoryFragmentBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Date;

public class MoodHistoryFragment extends Fragment {

    private MoodHistoryFragmentBinding binding;
    private RecyclerView moodRecyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodHistoryItem> moodHistoryItems = new ArrayList<>();
    private List<MoodHistoryItem> originalMoodHistoryItems = new ArrayList<>();
    private FirebaseFirestore db;
    private static final String TAG = "MoodHistoryFragment";

    //Variables used for Filter
    Button filterButton;
    Button filterClear;
    Spinner filterSpinner;
    ArrayAdapter<String> filterSpinnerAdapter;
    List<String> filterSpinnerData;
    String selectedEmotionalState;
    String keywordEditTextData;




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MoodHistoryFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Set up filterFunction
        filterButton = view.findViewById(R.id.filterButton);
        filterSpinner = view.findViewById(R.id.filterSpinner);
        filterClear = view.findViewById(R.id.filterClearButton);
        filterSpinner.setEnabled(false);
        filterClear.setEnabled(false);

        //Adding data to Spinner
        filterSpinnerData = Arrays.asList("Choose Option", "Most Recent Week", "Emotional State", "Keyword");
        filterSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, filterSpinnerData);
        filterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        filterSpinner.setAdapter(filterSpinnerAdapter);


        //Set up Filter Data
        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Saving original data
                if (originalMoodHistoryItems.size() == 0){
                    //Only saves original data once
                    saveOriginalMoodHistory();
                }
                //Managing visibility of spinner
                if (filterSpinner.getVisibility() == View.VISIBLE) {
                    filterSpinner.setVisibility(View.INVISIBLE);
                    filterSpinner.setEnabled(false);
                    filterClear.setVisibility(View.INVISIBLE);
                    filterClear.setEnabled(false);
                } else {
                    filterSpinner.setVisibility(View.VISIBLE);
                    filterSpinner.setEnabled(true);
                    filterClear.setVisibility(View.VISIBLE);
                    filterClear.setEnabled(true);
                }
            }
        });

        // Set up RecyclerView
        binding.moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodHistoryAdapter = new MoodHistoryAdapter(moodHistoryItems, getContext());
        binding.moodRecyclerView.setAdapter(moodHistoryAdapter);

        db = FirebaseFirestore.getInstance();

        moodHistoryAdapter.setOnItemClickListener(item -> {
            fetchMoodEventAndNavigate(item);
        });

        fetchMoodEvents();

        //Detecting which option in Spinner used:
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                if (selectedFilter.equals("Most Recent Week")){
                    //Automatically sorting
                    long currentTime = System.currentTimeMillis();
                    long oneWeekinMilli = 7 * 24 * 60 * 60 * 1000; //One week in ms

                    List<MoodHistoryItem> filteredMoodHistory = new ArrayList<>();
                    for (MoodHistoryItem moodht: moodHistoryItems) {
                        if ((currentTime - moodht.getDate().getTime()) <= oneWeekinMilli) {
                            filteredMoodHistory.add(moodht);
                        }
                    }
                    // Update the current list with the filtered data
                    moodHistoryItems.clear();
                    moodHistoryItems.addAll(filteredMoodHistory);
                    moodHistoryAdapter.notifyDataSetChanged();

                } else if (selectedFilter.equals("Emotional State")) {
                    //Open Dialog
                    AlertDialog.Builder emotionalFilterDialog = new AlertDialog.Builder(getContext());
                    LayoutInflater emotionalFilterInflater = getLayoutInflater(); //Setting up Dialog
                    View emotionalDialogView = emotionalFilterInflater.inflate(R.layout.dialog_filter_layout, null);
                    emotionalFilterDialog.setView(emotionalDialogView);

                    List<String> emotionalFilterSpinnerData = Arrays.asList("Choose Option", "Happy", "Sad", "Angry", "Confused", "Surprised", "Ashamed", "Scared", "Disgusted");
                    ArrayAdapter<String> emotionalFilterSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, emotionalFilterSpinnerData);
                    emotionalFilterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                    Spinner emotionalFilterSpinner = emotionalDialogView.findViewById(R.id.dialogFilterSpinner);
                    emotionalFilterSpinner.setAdapter(emotionalFilterSpinnerAdapter);

                    emotionalFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedEmotionalState = parent.getItemAtPosition(position).toString(); //Contains current emotional state
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            //Do nothing
                            selectedEmotionalState = "";
                        }
                    });

                    emotionalFilterDialog.setTitle("Filter by Emotional State")
                            .setPositiveButton("OK", (dialog, which) -> {
                                dialog.dismiss();

                                //Filtering based on emotional state
                                List<MoodHistoryItem> filteredMoodHistory = new ArrayList<>();
                                for (MoodHistoryItem moodht: moodHistoryItems) {
                                    if (moodht.getMood().equals(selectedEmotionalState)) {
                                        filteredMoodHistory.add(moodht);
                                    } else if (selectedEmotionalState.equals("Choose Option")){
                                        filteredMoodHistory.add(moodht);
                                    }
                                }
                                // Update the current list with the filtered data
                                moodHistoryItems.clear();
                                moodHistoryItems.addAll(filteredMoodHistory);
                                moodHistoryAdapter.notifyDataSetChanged();

                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                            });
                    AlertDialog tempEmotionalFilterDialog = emotionalFilterDialog.create();
                    tempEmotionalFilterDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_thick);
                    tempEmotionalFilterDialog.show();

                } else if (selectedFilter.equals("Keyword")) {
                    //Open Dialog
                    AlertDialog.Builder keywordFilterDialog = new AlertDialog.Builder(getContext());
                    LayoutInflater keywordFilterInflater = getLayoutInflater(); //Setting up Dialog
                    View keywordDialogView  = keywordFilterInflater.inflate(R.layout.dialog_filter_layout_keyword, null);
                    keywordFilterDialog.setView(keywordDialogView);

                    EditText keywordEditTextDialog = keywordDialogView.findViewById(R.id.keywordEditTextDialog);
                    keywordEditTextData = "";

                    keywordFilterDialog.setTitle("Filter by Keyword")
                            .setPositiveButton("OK", (dialog, which) -> {
                                dialog.dismiss();

                                //Filtering based on keyword
                                keywordEditTextData = keywordEditTextDialog.getText().toString();
                                List<MoodHistoryItem> filteredMoodHistory = new ArrayList<>();
                                for (MoodHistoryItem moodht: moodHistoryItems) {
                                    if (moodht.getDescription().contains(keywordEditTextData)) {
                                        filteredMoodHistory.add(moodht);
                                    } else if (keywordEditTextData.equals("Choose Option")){
                                        filteredMoodHistory.add(moodht);
                                    }


                                }
                                // Update the current list with the filtered data
                                moodHistoryItems.clear();
                                moodHistoryItems.addAll(filteredMoodHistory);
                                moodHistoryAdapter.notifyDataSetChanged();
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                dialog.dismiss();
                            });

                    AlertDialog tempKeywordFilterDialog = keywordFilterDialog.create();
                    tempKeywordFilterDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_thick);
                    tempKeywordFilterDialog.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing if nothing is selected
            }
        });

        binding.addButton.setOnClickListener(v ->
                NavHostFragment.findNavController(MoodHistoryFragment.this)
                        .navigate(R.id.action_moodHistoryFragment_to_SecondFragment)
        );

        //Clear Button Functionality (To restore filters)
        saveOriginalMoodHistory();
        filterClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalMoodHistoryItems.size() == 0){
                    return;
                }
                filterSpinner.setSelection(0);
                //Restoring Original Data
                moodHistoryItems.clear();
                moodHistoryItems.addAll(originalMoodHistoryItems);
                moodHistoryAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchMoodEvents();
    }

    private void saveOriginalMoodHistory() {
        originalMoodHistoryItems = new ArrayList<>(moodHistoryItems);
    }

    private void fetchMoodEventAndNavigate(MoodHistoryItem selectedItem) {
        db.collection("mood_events")
                .whereEqualTo("description", selectedItem.getDescription())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                            moodEvent.setId(document.getId()); // Store the document ID in MoodEvent

                            Bundle args = new Bundle();
                            args.putParcelable("moodEvent", (Parcelable) moodEvent);

                            NavHostFragment.findNavController(MoodHistoryFragment.this)
                                    .navigate(R.id.action_moodHistoryFragment_to_editMoodFragment, args);
                            break; // Assuming only one MoodEvent will match the description
                        }
                    } else {
                        Log.d(TAG, "Error getting MoodEvent: ", task.getException());
                    }
                });
    }

    private void fetchMoodEvents() {
        db.collection("mood_events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Fetch task successful");
                        moodHistoryItems.clear(); // Clear the previous list

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "Processing document: " + document.getId());
                            String id = document.getId();
                            String mood = document.getString("mood");
                            String description = document.getString("description");
                            Long datems = document.getLong("date");

                            if (mood == null || description == null || datems == null) {
                                Log.w(TAG, "Missing data in document: " + id);
                                continue;
                            }

                            Date date = new Date(datems);
                            String emoji = getEmojiForMood(mood);

                            MoodHistoryItem item = new MoodHistoryItem(mood, emoji, description, date);
                            item.setId(id);
                            moodHistoryItems.add(item);
                            Log.d(TAG, "Added item: " + item.toString());
                        }

                        Collections.sort(moodHistoryItems, (item1, item2) -> item2.getDate().compareTo(item1.getDate()));
                        Log.d(TAG, "Sorted items. First item date: " +
                                (moodHistoryItems.isEmpty() ? "N/A" : moodHistoryItems.get(0).getDate()));

                        Log.d(TAG, "Number of items fetched: " + moodHistoryItems.size());
                        moodHistoryAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private String getEmojiForMood(String mood) {
        switch (mood.toLowerCase()) {
            case "happy":
                return "😊";
            case "sad":
                return "😢";
            case "excited":
                return "😃";
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
                return "";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
