package com.example.moodsync;



import android.content.res.ColorStateList;

import android.graphics.Bitmap;

import android.graphics.Matrix;

import android.os.Bundle;

import android.text.Editable;

import android.text.TextWatcher;

import android.util.Log;


import android.view.Gravity;

import android.view.LayoutInflater;

import android.view.View;

import android.view.ViewGroup;


import android.widget.AdapterView;

import android.widget.ArrayAdapter;


import android.widget.Button;


import android.widget.EditText;

import android.widget.ImageView;

import android.widget.ListView;


import android.widget.PopupWindow;


import android.widget.Spinner;

import android.widget.Toast;



import androidx.activity.OnBackPressedCallback;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import androidx.navigation.NavOptions;

import androidx.navigation.fragment.NavHostFragment;

import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;



import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import com.example.moodsync.databinding.HomePageFragmentBinding;

import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.firestore.DocumentSnapshot;

import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.firestore.Query;

import com.google.firebase.firestore.QueryDocumentSnapshot;



import java.security.MessageDigest;

import java.util.ArrayList;


import java.util.Arrays;

import java.util.Collections;

import java.util.HashMap;

import java.util.List;

import java.util.Map;



public class SecondFragment extends Fragment {



    private HomePageFragmentBinding binding;

    private RecyclerView moodRecyclerView;

    private MoodCardAdapter moodCardAdapter;

    private FirebaseFirestore db;

    private TextInputEditText searchBar;

    private ImageView pfp;

    private String searchText = "";
    public LocalStorage globalStorage = LocalStorage.getInstance();
    private ListView searchResultsListView;
    private ArrayAdapter<String> searchResultsAdapter;
    //filter functionality
    private ImageView filterIcon;
    private PopupWindow popupWindow;
    private Spinner filterSpinner, emotionalStateSpinner;
    private EditText keywordEditText;

    private Button filterApplyButton, filterCancelButton;
    private String selectedFilter = "", selectedEmotionalState = "", keywordEditTextData = "";
    private List<MoodEvent> moodEvents = new ArrayList<>();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = HomePageFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        binding.mapButton.setOnClickListener(v ->
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_mapsActivity)
        );
        binding.diaryButton.setOnClickListener(v ->
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_JournalFragment));
        searchBar = view.findViewById(R.id.search_bar);

        pfp = view.findViewById(R.id.profile_pic);
        fetchProfileImageUrl(globalStorage.getCurrentUserId());
        binding.homeButton.setTextColor(getResources().getColor(R.color.green));
        binding.homeButton.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.green)));

        // inflate the search results xml layout
        View searchResultsView = inflater.inflate(R.layout.search_results, container, false);
        searchResultsListView = searchResultsView.findViewById(R.id.search_results_listview);
        //filter functionality
        filterIcon = view.findViewById(R.id.filter_icon);
        // set adapter for the search results listview
        searchResultsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        searchResultsListView.setAdapter(searchResultsAdapter);
        // add the inflated search results view to the parent layout
        ((ViewGroup) view).addView(searchResultsView);

        // initially hide the search results, cuz empty list is lame
        searchResultsListView.setVisibility(View.INVISIBLE);
        // set item click listener for search results

        searchResultsListView.setOnItemClickListener((parent, v, position, id) -> {
            String selectedUsername = (String) parent.getItemAtPosition(position);
            Toast.makeText(getContext(), "Selected: " + selectedUsername, Toast.LENGTH_SHORT).show();
            searchResultsListView.setVisibility(View.INVISIBLE);
            searchBar.setText("");
            navigateToUserProfile(selectedUsername);
        });
        // Disable back gestures from this fragment
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        });
        // text change listener for the search bar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // yo, before text changes, nothing to see here
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchText = charSequence.toString();
                if (!searchText.isEmpty()) {
                    searchFirestore(searchText);
                    // set visibility and add extra translation downwards
                    searchResultsListView.setVisibility(View.VISIBLE);
                    searchResultsListView.setTranslationY(dpToPx(80)); // push down extra 70dp
                    searchResultsListView.bringToFront(); // make sure it's on top, boss
                } else {
                    // if nothing in search bar, set visibility to invisible
                    searchResultsListView.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // after text changed, just chill
            }
        });
//        globalStorage.updateUserList();
        return view;
    }
    private void handleBackPress() {
        // If anything is too be added do it here
    }
    private void fetchProfileImageUrl(String userId) {
        loadProfileImage(globalStorage.getUserFromUName(userId).getPfpUrl());
    }

    private void loadProfileImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_person_black_24dp)
                .into(pfp);
    }

    private void searchFirestore(String searchText) {
        db.collection("users")
                .whereGreaterThanOrEqualTo("userName", searchText)
                .whereLessThanOrEqualTo("userName", searchText + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> usernames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            usernames.add(document.getString("userName"));
                        }
                        updateSearchResults(usernames);
                    } else {
                        Log.e("Firestore", "Error searching users", task.getException());
                    }
                });
    }
    private void navigateToUserProfile(String selectedUsername) {
        db.collection("users")
                .whereEqualTo("userName", selectedUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String userId = document.getId();
                        globalStorage.setSearchResult(userId);
                        Bundle args = new Bundle();
                        args.putString("selectedUserId", userId);
                        NavHostFragment.findNavController(SecondFragment.this)
                                .navigate(R.id.action_SecondFragment_to_userProfileFragment, args);
                    } else {
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateSearchResults(ArrayList<String> usernames) {
        searchResultsAdapter.clear();
        searchResultsAdapter.addAll(usernames);
        searchResultsAdapter.notifyDataSetChanged();
        // set height based on number of results (limit to max 5 visible items)
        int itemHeight = 100; // approx height per item in dp, tweak if needed
        int maxItems = Math.min(usernames.size(), 5);
        int totalHeight = dpToPx(maxItems * itemHeight);
        ViewGroup.LayoutParams layoutParams = searchResultsListView.getLayoutParams();
        layoutParams.height = totalHeight;
        searchResultsListView.setLayoutParams(layoutParams);
    }
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // init firestore, bro
        db = FirebaseFirestore.getInstance();
        setupRecyclerView();
        binding.addCircleButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_addMoodActivityFragment,
                            null,
                            new NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_in_right)
                                    .setExitAnim(R.anim.slide_out_left)
                                    .setPopEnterAnim(R.anim.slide_in_left)
                                    .setPopExitAnim(R.anim.slide_out_right)
                                    .build());
        });
        binding.historyButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_moodHistoryFragment,
                            null,
                            new NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_in_right)
                                    .setExitAnim(R.anim.slide_out_left)
                                    .setPopEnterAnim(R.anim.slide_in_left)
                                    .setPopExitAnim(R.anim.slide_out_right)
                                    .build());
        });
        binding.profilePicContainer.setOnClickListener(v -> {
            NavHostFragment.findNavController(SecondFragment.this)
                    .navigate(R.id.action_SecondFragment_to_editProfileFragment,
                            null,
                            new NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_in_right)
                                    .setExitAnim(R.anim.slide_out_left)
                                    .setPopEnterAnim(R.anim.slide_in_left)
                                    .setPopExitAnim(R.anim.slide_out_right)
                                    .build());
        });
        fetchMoodEvents();
        globalStorage.updateMood(globalStorage.getCurrentUserId());
    }
    private void setupRecyclerView() {
        moodRecyclerView = binding.moodRecyclerView;
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodCardAdapter = new MoodCardAdapter(new ArrayList<>()); // start empty, then load data
        moodRecyclerView.setAdapter(moodCardAdapter);
    }
    //added this method to disply 3 most recent moods of followees
    private void filterToRecentMoods(List<MoodEvent> allMoodEvents) {
        // Group mood events by user
        Map<String, List<MoodEvent>> moodsByUser = new HashMap<>();
        globalStorage.clearMoods();
        for (MoodEvent event : allMoodEvents) {
            String userId = event.getId();
            if (!moodsByUser.containsKey(userId)) {
                moodsByUser.put(userId, new ArrayList<>());
            }
            moodsByUser.get(userId).add(event);
            globalStorage.insertMood(event);
        }
        // Sort each user's moods by timestamp (descending) and keep only the 3 most recent
        List<MoodEvent> filteredMoods = new ArrayList<>();
        for (List<MoodEvent> userMoods : moodsByUser.values()) {
            // Sort by timestamp in descending order (most recent first)
            Collections.sort(userMoods, (mood1, mood2) ->
                    Long.compare(mood2.getDate(), mood1.getDate()));
            // Take only the first 3 (or fewer if the user has less than 3 moods)
            int modsToTake = Math.min(3, userMoods.size());
            filteredMoods.addAll(userMoods.subList(0, modsToTake));
        }
        // Sort all filtered moods by timestamp in descending order
        Collections.sort(filteredMoods, (mood1, mood2) ->
                Long.compare(mood2.getDate(), mood1.getDate()));
        // Update the adaptetr with the filtered list
        moodCardAdapter.updateMoodEvents(filteredMoods);
        Log.d("MoodEvents", "Filtered to " + filteredMoods.size() + " recent mood events");
    }
    //    changed this to only display moods of followees
    private void fetchMoodEvents() {
        Log.d("MoodSync", "Starting fetchMoodEvents");
        MyApplication myApp = (MyApplication) requireActivity().getApplicationContext();
        String currentUsername = myApp.getLoggedInUsername();

        // Check for valid user
        if (currentUsername == null || currentUsername.isEmpty()) {
            Log.e("SecondFragment", "No logged in user found");
            moodCardAdapter.updateMoodEvents(new ArrayList<>());
            return;
        }

        // Try to load from cache first
        List<MoodEvent> cachedMoods = globalStorage.getMoodList();
        if (!cachedMoods.isEmpty()) {
            Log.d("MoodSync", "Displaying cached moods");
            filterToRecentMoods(cachedMoods);
            moodCardAdapter.updateMoodEvents(cachedMoods);
        }

        // Get user data (first from cache, then network)
        User cachedUser = globalStorage.getUserFromUName(currentUsername);
        List<String> followingUsers;

        if (cachedUser != null && cachedUser.getFollowingList() != null) {
            followingUsers = cachedUser.getFollowingList();
            Log.d("MoodSync", "Using cached following list");
        } else {
            followingUsers = new ArrayList<>();
        }

        // Add current user if not already in list
        if (!followingUsers.contains(currentUsername)) {
            followingUsers.add(currentUsername);
        }
        if (NetworkUtils.isConnected(getContext())) {
            // Network request for fresh data
            db.collection("users")
                    .whereEqualTo("userName", currentUsername)
                    .get()
                    .addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful() && !userTask.getResult().isEmpty()) {
                            DocumentSnapshot userDoc = userTask.getResult().getDocuments().get(0);
                            List<String> freshFollowingUsers = (List<String>) userDoc.get("followingList");
                            // Update local cache with fresh following list
                            if (freshFollowingUsers != null) {
                                followingUsers.clear();
                                followingUsers.addAll(freshFollowingUsers);
                                if (!followingUsers.contains(currentUsername)) {
                                    followingUsers.add(currentUsername);
                                }

                                // Update user in cache
                                User updatedUser = userDoc.toObject(User.class);
                                globalStorage.addUser(updatedUser);
                            }

                            // Determine query based on following count
                            if (followingUsers.size() <= 1) {
                                fetchSingleUserMoods(currentUsername, globalStorage);
                            } else {
                                fetchFollowedUsersMoods(followingUsers, globalStorage);
                            }
                        } else {
                            Log.e("Firestore", "Error fetching user data", userTask.getException());
                            // If we have cached data, use it instead of showing error
                            if (!cachedMoods.isEmpty()) {
                                Toast.makeText(getContext(), "Using cached data", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to load data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void fetchSingleUserMoods(String username, LocalStorage globalStorage) {
        // Try cache first
        List<MoodEvent> userMoods = new ArrayList<>();
        for (MoodEvent mood : globalStorage.getMoodList()) {
            if (mood.getId().equals(username) && mood.isPublic()) {
                userMoods.add(mood);
            }
        }

        if (!userMoods.isEmpty()) {
            filterToRecentMoods(userMoods);
            moodCardAdapter.updateMoodEvents(userMoods);
        }

        if (NetworkUtils.isConnected(getContext())) {
            // Network request for fresh data
            db.collection("mood_events")
                    .whereEqualTo("id", username)
                    .whereEqualTo("public", true)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<MoodEvent> freshMoods = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                moodEvent.setDocumentId(document.getId());
                                freshMoods.add(moodEvent);
                                globalStorage.addMood(moodEvent); // Update cache
                            }
                            filterToRecentMoods(freshMoods);
                            moodCardAdapter.updateMoodEvents(freshMoods);
                        } else {
                            Log.e("Firestore", "Error fetching single user moods", task.getException());
                        }
                    });
        }
    }

    private void fetchFollowedUsersMoods(List<String> followingUsers, LocalStorage globalStorage) {
        // Try cache first
        List<MoodEvent> followedMoods = new ArrayList<>();
        for (MoodEvent mood : globalStorage.getMoodList()) {
            if (followingUsers.contains(mood.getId()) && mood.isPublic()) {
                followedMoods.add(mood);
            }
        }

        if (!followedMoods.isEmpty()) {
            filterToRecentMoods(followedMoods);
            moodCardAdapter.updateMoodEvents(followedMoods);
        }

        if (NetworkUtils.isConnected(getContext())) {
            // Network request for fresh data
            db.collection("mood_events")
                    .whereIn("id", followingUsers)
                    .whereEqualTo("public", true)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<MoodEvent> freshMoods = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                moodEvent.setDocumentId(document.getId());
                                freshMoods.add(moodEvent);
                                globalStorage.addMood(moodEvent); // Update cache
                            }
                            filterToRecentMoods(freshMoods);
                            moodCardAdapter.updateMoodEvents(freshMoods);
                            filterIcon.setOnClickListener(v -> showFilterPopup(freshMoods));
                        } else {
                            Log.e("Firestore", "Error fetching followed users' moods", task.getException());
                        }
                    });
        }
    }

    //filter functionality
    private void showFilterPopup(List<MoodEvent> moodEvents) {
        Log.d("FUCK2", "fetchMoodEvents: "+ moodEvents);

        LayoutInflater inflater = LayoutInflater.from(getContext());

        View popupView = inflater.inflate(R.layout.filter_popup, null);
        filterSpinner = popupView.findViewById(R.id.filterSpinner);

        emotionalStateSpinner = popupView.findViewById(R.id.emotionalStateSpinner);

        keywordEditText = popupView.findViewById(R.id.keywordEditText);

        filterApplyButton = popupView.findViewById(R.id.filterApplyButton);

        filterCancelButton = popupView.findViewById(R.id.filterCancelButton);
        List<String> filterOptions = Arrays.asList("Most Recent Week", "Emotional State", "Keyword");
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, filterOptions);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        filterSpinner.setAdapter(filterAdapter);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = parent.getItemAtPosition(position).toString();
                if (selectedFilter.equals("Most Recent Week")) {
                    keywordEditText.setVisibility(View.GONE);
                    emotionalStateSpinner.setVisibility(View.GONE);
                } else if (selectedFilter.equals("Emotional State")) {
                    keywordEditText.setVisibility(View.GONE);
                    emotionalStateSpinner.setVisibility(View.VISIBLE);
                    List<String> emotionalStates = Arrays.asList("Choose Option", "Happy", "Sad", "Angry", "Confused", "Surprised", "Ashamed", "Scared", "Disgusted");
                    ArrayAdapter<String> emotionalAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, emotionalStates);
                    emotionalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                    emotionalStateSpinner.setAdapter(emotionalAdapter);
                    emotionalStateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedEmotionalState = parent.getItemAtPosition(position).toString();
                        }@Override


                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedEmotionalState = "";
                        }
                    });
                } else if (selectedFilter.equals("Keyword")) {
                    keywordEditText.setVisibility(View.VISIBLE);
                    emotionalStateSpinner.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedFilter = "";
            }
        });
        filterApplyButton.setOnClickListener(v -> {
            popupWindow.dismiss();
            Log.d("FUCK3", "fetchMoodEvents: "+ moodEvents);
            applyFilter(moodEvents);
        });
        filterCancelButton.setOnClickListener(v -> {
            popupWindow.dismiss();
            fetchMoodEvents(); // Call fetchMoodEvents to reset filters and fetch all mood event
        });
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.PopupDialogAnimation);
        View view = getView();
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }
    private void applyFilter(List<MoodEvent> moodEvents) {
        if (selectedFilter.equals("Most Recent Week")) {
            Log.d("selected recent week", "applyFilter: ");
            long currentTime = System.currentTimeMillis();
            long oneWeekInMilli = 7 * 24 * 60 * 60 * 1000;
            List<MoodEvent> filteredMoodEvents = new ArrayList<>();
            Log.d("makichut", "applyFilter: "+ moodEvents);
            for (MoodEvent moodEvent : moodEvents) {
                if ((currentTime - moodEvent.getDate()) <= oneWeekInMilli) {
                    filteredMoodEvents.add(moodEvent);
                }
            }
            filterToRecentMoods(filteredMoodEvents);
            Log.d("Filter recent week", "Filtered to " + filteredMoodEvents);
        } else if (selectedFilter.equals("Emotional State")) {
            Log.d("selected emotional state", "applyFilter: ");
            List<MoodEvent> filteredMoodEvents = new ArrayList<>();
            for (MoodEvent moodEvent : moodEvents) {
                if (moodEvent.getMood().equals(selectedEmotionalState) || selectedEmotionalState.equals("Choose Option")) {
                    filteredMoodEvents.add(moodEvent);
                }
            }
            filterToRecentMoods(filteredMoodEvents);

//            moodCardAdapter.updateMoodEvents(filteredMoodEvents);
            Log.d("Filter emotional state", "Filtered to " + moodEvents + " mood: " );
        } else if (selectedFilter.equals("Keyword")) {
            Log.d("keyword", "applyFilter: ");
            keywordEditTextData = keywordEditText.getText().toString();
            List<MoodEvent> filteredMoodEvents = new ArrayList<>();
            for (MoodEvent moodEvent : moodEvents) {
                if (moodEvent.getTrigger().contains(keywordEditTextData)) {
                    filteredMoodEvents.add(moodEvent);
                }
            }
            filterToRecentMoods(filteredMoodEvents);
//            moodCardAdapter.updateMoodEvents(filteredMoodEvents);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    public ArrayList<User> Search(String searchText) {
        ArrayList<User> result = new ArrayList<>();
        ArrayList<User> tempList = globalStorage.getUserList();
        for (User user : tempList) {
            if (user.getUsername().toLowerCase().contains(searchText.toLowerCase())) {
                result.add(user);
            }
        }
        return result;

    }
}