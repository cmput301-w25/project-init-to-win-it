package com.example.moodsync;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
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
import androidx.navigation.NavOptions;
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
/**
 * A fragment that displays the user's mood history and provides filtering capabilities.
 * <p>
 * This fragment shows a list of mood events in chronological order and allows users to:
 * <ul>
 *     <li>Filter by time period (most recent week)</li>
 *     <li>Filter by emotional state (happy, sad, angry, etc.)</li>
 *     <li>Filter by keyword search in descriptions</li>
 *     <li>Navigate to edit specific mood events</li>
 *     <li>Access other app features through bottom navigation</li>
 * </ul>
 * The fragment interacts with Firestore to fetch and display mood event data.
 */
public class MoodHistoryFragment extends Fragment {

    private MoodHistoryFragmentBinding binding;
    private RecyclerView moodRecyclerView;
    private MoodHistoryAdapter moodHistoryAdapter;
    private List<MoodHistoryItem> moodHistoryItems = new ArrayList<>();
    private List<MoodHistoryItem> originalMoodHistoryItems = new ArrayList<>();
    private FirebaseFirestore db;
    public LocalStorage globalStorage = LocalStorage.getInstance();
    private static final String TAG = "MoodHistoryFragment";

    //Variables used for Filter
    Button filterButton;
    Button filterClear;
    Spinner filterSpinner;
    ArrayAdapter<String> filterSpinnerAdapter;
    List<String> filterSpinnerData;
    String selectedEmotionalState;
    String keywordEditTextData;
    private String currentUserId;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = MoodHistoryFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //this is the code that navigates to all parts of the app.
        binding.homeButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(MoodHistoryFragment.this)
                    .navigate(R.id.action_moodHistoryFragment_to_SecondFragment,
                            null,
                            new NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_in_right)
                                    .setExitAnim(R.anim.slide_out_left)
                                    .setPopEnterAnim(R.anim.slide_in_left)
                                    .setPopExitAnim(R.anim.slide_out_right)
                                    .build());
        });
        binding.addCircleButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(MoodHistoryFragment.this)
                    .navigate(R.id.action_moodHistoryFragment_to_addMoodActivityFragment,
                            null,
                            new NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_in_right)
                                    .setExitAnim(R.anim.slide_out_left)
                                    .setPopEnterAnim(R.anim.slide_in_left)
                                    .setPopExitAnim(R.anim.slide_out_right)
                                    .build());
        });

        binding.mapButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(MoodHistoryFragment.this)
                    .navigate(R.id.action_moodHistoryFragment_to_mapsActivity,
                            null,
                            new NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_in_right)
                                    .setExitAnim(R.anim.slide_out_left)
                                    .setPopEnterAnim(R.anim.slide_in_left)
                                    .setPopExitAnim(R.anim.slide_out_right)
                                    .build());
        });
        binding.diaryButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(MoodHistoryFragment.this)
                    .navigate(R.id.action_moodHistoryFragment_to_JournalFragment,
                            null,
                            new NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_in_right)
                                    .setExitAnim(R.anim.slide_out_left)
                                    .setPopEnterAnim(R.anim.slide_in_left)
                                    .setPopExitAnim(R.anim.slide_out_right)
                                    .build());


        });
        binding.historyButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(MoodHistoryFragment.this)
                    .navigate(R.id.action_moodHistoryFragment_to_moodHistoryFragment,
                            null,
                            new NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_in_right)
                                    .setExitAnim(R.anim.slide_out_left)
                                    .setPopEnterAnim(R.anim.slide_in_left)
                                    .setPopExitAnim(R.anim.slide_out_right)
                                    .build());


        });
        binding.historyButton.setTextColor(getResources().getColor(R.color.green));
        binding.historyButton.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.green)));
        //Set up filterFunction
        filterButton = view.findViewById(R.id.filterButton);
        filterSpinner = view.findViewById(R.id.filterSpinner);
        filterClear = view.findViewById(R.id.filterClearButton);
        filterSpinner.setEnabled(false);
        filterClear.setEnabled(false);

        //Adding data to Spinner
        filterSpinnerData = Arrays.asList("Choose Option", "Most Recent Week", "Emotional State", "Keyword");
        filterSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, filterSpinnerData);
        filterSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        filterSpinner.setAdapter(filterSpinnerAdapter);

        MyApplication myApp = (MyApplication) requireActivity().getApplicationContext();
        currentUserId = myApp.getLoggedInUsername();

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
        moodHistoryAdapter = new MoodHistoryAdapter(globalStorage.getMHItem(), getContext());
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
                                    if (moodht.getDescription().toLowerCase().contains(keywordEditTextData.toLowerCase())) {
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
                filterSpinner.setSelection(0);
                selectedEmotionalState = "";
                keywordEditTextData = "";
                //Restoring Original Data
                if (originalMoodHistoryItems.size() == 0){
                    return;
                }
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
    /**
     * Saves a copy of the current mood history items to preserve the original list.
     * This can be used for operations like filtering or restoring the original state later.
     */
    private void saveOriginalMoodHistory() {
        originalMoodHistoryItems = new ArrayList<>(moodHistoryItems);
    }

    /**
     * Fetches a specific mood event from Firestore based on the description of the selected item
     * and navigates to the Edit Mood Fragment with the retrieved mood event data.
     *
     * @param selectedItem The mood history item selected by the user.
     */
    private void fetchMoodEventAndNavigate(MoodHistoryItem selectedItem) {
        MoodEvent matchingMoodEvent = null;
        ArrayList <MoodEvent> temp = new ArrayList<MoodEvent>();
        temp.addAll(globalStorage.getMoodList());
        temp.addAll(globalStorage.getPrivList());
        Log.d("FORLOOP", "fetchMood "+selectedItem.getDate().getTime());
        for (MoodEvent moodEvent : temp) {
            Log.d("FORLOOP", "fetchMoodEventAndNavigate: "+moodEvent.getDate());
            if (moodEvent.getDate() == (selectedItem.getDate().getTime())) {
                matchingMoodEvent = moodEvent;
                break; // Stop once we find a matching MoodEvent
            }
        }
        if (matchingMoodEvent != null) {
            // Create a Bundle and add the MoodEvent as a Parcelable
            Bundle args = new Bundle();
            args.putParcelable("moodEvent", (Parcelable) matchingMoodEvent);

            // Navigate to the editMoodFragment with the selected MoodEvent
            NavHostFragment.findNavController(MoodHistoryFragment.this)
                    .navigate(R.id.action_moodHistoryFragment_to_editMoodFragment, args);
        } else {
            Log.d(TAG, "No matching MoodEvent found in local storage.");
        }
    }
    /**
     * Fetches all mood events for the current user from Firestore, processes them into
     * {@link MoodHistoryItem} objects, sorts them by date, and updates the RecyclerView adapter.
     */
    private void fetchMoodEvents() {
        db.collection("mood_events")
                .whereEqualTo("id" , currentUserId)
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
                        globalStorage.getMHItem().clear();
                        for (int i=0; i<moodHistoryItems.size();i++){
                            if (!globalStorage.getMHItem().contains(moodHistoryItems.get(i))) {
                                globalStorage.getMHItem().add(moodHistoryItems.get(i));
                            }
                        }
                        Log.d(TAG, "Number of items fetched: " + moodHistoryItems.size());
                        moodHistoryAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }


    /**
     * Returns an emoji representation for a given mood string. If no match is found,
     * an empty string is returned.
     *
     * @param mood The mood string for which an emoji is needed.
     * @return A string containing the emoji corresponding to the given mood.
     */
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