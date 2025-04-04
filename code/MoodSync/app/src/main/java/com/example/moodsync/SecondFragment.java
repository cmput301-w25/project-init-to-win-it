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
//import com.example.moodsync.databinding.HomePageFragmentBinding;
import com.example.moodsync.databinding.HomePageFragmentBinding;
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

/**
 * A fragment that serves as the main home page for the application. It displays a list of mood events,
 * provides search functionality, and includes navigation to other parts of the app such as maps, journal,
 * and user profiles.
 *
 * <p>
 * This fragment interacts with Firebase Firestore to fetch user data, mood events, and profile images.
 * It also includes filtering functionality to display mood events based on specific criteria such as
 * time range, emotional state, or keyword.
 * </p>
 *
 * <p>
 * The fragment uses a RecyclerView to display mood events and supports dynamic updates based on user
 * interactions like searching or applying filters.
 * </p>
 */
public class  SecondFragment extends Fragment {

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

        binding.mapButton.setOnClickListener(v ->{
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_mapsActivity,
                                null,
                                new NavOptions.Builder()
                                        .setEnterAnim(R.anim.slide_in_right)
                                        .setExitAnim(R.anim.slide_out_left)
                                        .setPopEnterAnim(R.anim.slide_in_left)
                                        .setPopExitAnim(R.anim.slide_out_right)
                                        .build());
    });


        binding.diaryButton.setOnClickListener(v ->{
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_JournalFragment,
                                null,
                                new NavOptions.Builder()
                                        .setEnterAnim(R.anim.slide_in_right)
                                        .setExitAnim(R.anim.slide_out_left)
                                        .setPopEnterAnim(R.anim.slide_in_left)
                                        .setPopExitAnim(R.anim.slide_out_right)
                                        .build());
    });

        binding.homeButton.setOnClickListener(v ->{
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_SecondFragment,
                                null,
                                new NavOptions.Builder()
                                        .setEnterAnim(R.anim.slide_in_right)
                                        .setExitAnim(R.anim.slide_out_left)
                                        .setPopEnterAnim(R.anim.slide_in_left)
                                        .setPopExitAnim(R.anim.slide_out_right)
                                        .build());
    });

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

    /**
     * Handles the back press action for this fragment. This method can be extended
     * to perform additional actions when the back button is pressed.
     */
    private void handleBackPress() {
        // If anything is too be added do it here
    }


    /**
     * Fetches the profile image URL of a user from Firestore based on their user ID. If a valid URL is found,
     * it loads the image into the profile picture ImageView using Glide.
     *
     * @param userId The unique ID of the user whose profile image is being fetched.
     */
    private void fetchProfileImageUrl(String userId) {
        String imageUrl = globalStorage.getCurrentUser().getPfpUrl();
        if(imageUrl!=null && !imageUrl.isEmpty()){
            loadProfileImage(imageUrl);
        }
    }

    /**
     * Loads a profile image into an ImageView using Glide. Applies a circular crop transformation
     * and sets a placeholder image while loading.
     *
     * @param imageUrl The URL of the profile image to load.
     */
    private void loadProfileImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_person_black_24dp)
                .into(pfp);
    }

    /**
     * Searches Firestore for users whose usernames match or start with the given search text.
     * Updates the search results ListView with matching usernames dynamically as the user types in the search bar.
     *
     * @param searchText The text entered by the user in the search bar.
     */
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

    /**
     * Navigates to a user's profile page based on their username. Fetches the user's ID from Firestore,
     * stores it in global storage, and passes it as an argument to the UserProfileFragment.
     *
     * @param selectedUsername The username of the selected user whose profile is being viewed.
     */
    private void navigateToUserProfile(String selectedUsername) {
           db.collection("users")
                .whereEqualTo("userName", selectedUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String userId = document.getId();
                        globalStorage.setSearchResult(userId);

                        // Check if the selected user is the logged-in user
                        if (selectedUsername.equals(globalStorage.getCurrentUserId())) {
                            // Navigate to EditProfileFragment
                            NavHostFragment.findNavController(SecondFragment.this)
                                    .navigate(R.id.action_SecondFragment_to_editProfileFragment);
                        } else {
                            // Navigate to UserProfileFragment for other users
                            Bundle args = new Bundle();
                            args.putString("selectedUserId", userId);
                            NavHostFragment.findNavController(SecondFragment.this)
                                    .navigate(R.id.action_SecondFragment_to_userProfileFragment, args);
                        }
                    } else {
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * Updates the ListView with new search results. Adjusts its height dynamically based on
     * the number of results (up to a maximum of 5 visible items).
     *
     * @param usernames A list of usernames matching the current search query.
     */
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

    /**
     * Converts a value in density-independent pixels (dp) to pixels (px) based on screen density.
     *
     * @param dp The value in dp to be converted to px.
     * @return The equivalent pixel value for the given dp value.
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }


    public String getSearchText() {
        return searchText;
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
    }

    /**
     * Sets up and initializes the RecyclerView used to display mood events. Configures
     * its layout manager and adapter with an empty list initially.
     */
    private void setupRecyclerView() {
        moodRecyclerView = binding.moodRecyclerView;
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodCardAdapter = new MoodCardAdapter(globalStorage.getMoodList()); // start empty, then load data
        moodRecyclerView.setAdapter(moodCardAdapter);
    }

    /**
     * Filters mood events to display only the three most recent events for each user
     * being followed. Sorts these events by timestamp in descending order before updating
     * the adapter with filtered data.
     *
     * @param allMoodEvents A list of all mood events fetched from Firestore.
     */
    private void filterToRecentMoods(List<MoodEvent> allMoodEvents) {
        // Group mood events by user
        Map<String, List<MoodEvent>> moodsByUser = new HashMap<>();

        for (MoodEvent event : allMoodEvents) {
            String userId = event.getId();
            if (!moodsByUser.containsKey(userId)) {
                moodsByUser.put(userId, new ArrayList<>());
            }
            moodsByUser.get(userId).add(event);
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

        // Update the adapter with the filtered list
        moodCardAdapter.updateMoodEvents(filteredMoods);
        Log.d("MoodEvents", "Filtered to " + filteredMoods.size() + " recent mood events");
    }


    /**
     * Fetches mood events from Firestore for users that are being followed by the current user,
     * including their own mood events. Filters and displays these events in a RecyclerView.
     */
    private void fetchMoodEvents() {
        Log.d("fetched", "applyFilter: ");
        // Get the logged in username from MyApplication
        MyApplication myApp = (MyApplication) requireActivity().getApplicationContext();
        String currentUsername = myApp.getLoggedInUsername();

        if (currentUsername == null || currentUsername.isEmpty()) {
            Log.e("SecondFragment", "No logged in user found");
            moodCardAdapter.updateMoodEvents(globalStorage.getMoodList());
            return;
        }

        // First, get the current user's document to access their followingList
        db.collection("users")
                .whereEqualTo("userName", currentUsername)
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful() && !userTask.getResult().isEmpty()) {
                        DocumentSnapshot userDoc = userTask.getResult().getDocuments().get(0);
                        List<String> followingUsers = (List<String>) userDoc.get("followingList");

                        if (followingUsers == null) {
                            followingUsers = new ArrayList<>();
                        }

                        //Add current user to see their own posts too
                        if (!followingUsers.contains(currentUsername)) {
                            followingUsers.add(currentUsername);
                            Log.d("JJJJ", "fetchMoodEvents: "+ followingUsers);
                        }
                        globalStorage.getPrivList().clear();

                        // If not following anyone (just self), show only own moods
                        if (followingUsers.size() <= 1) {
                            db.collection("mood_events")
                                    .whereEqualTo("id", currentUsername)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            List<MoodEvent> moodEvents = new ArrayList<>();
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                                moodEvent.setDocumentId(document.getId());
                                                moodEvents.add(moodEvent);
                                                if (moodEvent.isPublic()){
                                                    globalStorage.insertMood(moodEvent);
                                                } else if (!moodEvent.isPublic() && moodEvent.getId().equals(myApp.getLoggedInUsername())){
                                                    globalStorage.updatePrivMood(moodEvent);
                                                }
                                            }
                                            globalStorage.deletePrivDups();
                                            filterToRecentMoods(globalStorage.getMoodList());
                                        } else {
                                            Log.e("Firestore", "Error fetching mood events", task.getException());
                                            Toast.makeText(getContext(), "Failed to load mood events", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            globalStorage.refreshPubList();
                            return;
                        }

                        // Query mood_events where user is in the list of followed users
                        db.collection("mood_events")
                                .whereIn("id", followingUsers)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
//                                        List<MoodEvent> moodEvents = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                            moodEvent.setDocumentId(document.getId());
                                            moodEvents.add(moodEvent);
                                            // Add moods to local storage
                                            if (moodEvent.isPublic()){
                                                globalStorage.insertMood(moodEvent);
                                            } else if (!moodEvent.isPublic() && moodEvent.getId().equals(myApp.getLoggedInUsername())){
                                                Log.d("ADDED", "Added priv mood "+moodEvent.getDescription() );
                                                    globalStorage.updatePrivMood(moodEvent);
                                            }
                                        }
                                        globalStorage.deletePrivDups();
                                        filterToRecentMoods(globalStorage.getMoodList());
                                        Log.d("sex8", "fetchMoodEvents: " + moodEvents);
//                                        filterToRecentMoods(moodEvents);
                                        filterIcon.setOnClickListener(v -> showFilterPopup(moodEvents));
                                    } else {
                                        Log.e("Firestore", "Error fetching mood events", task.getException());
                                        Toast.makeText(getContext(), "Failed to load mood events", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {

                        Log.e("Firestore", "Error fetching user data", userTask.getException());
                        Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
        globalStorage.refreshPubList();
        Log.d("FUCK1", "fetchMoodEvents: "+ moodEvents);
    }


    /**
     * Displays a popup window with filtering options for mood events. Allows users to filter
     * by time range (e.g., most recent week), emotional state, or keyword. Applies selected filters
     * when confirmed or resets filters when canceled.
     *
     * @param moodEvents A list of all mood events fetched from Firestore to be filtered.
     */
    private void showFilterPopup(List<MoodEvent> moodEvents) {

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
                        }

                        @Override
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
            applyFilter(moodEvents);
        });

        filterCancelButton.setOnClickListener(v -> {
            popupWindow.dismiss();
            moodEvents.clear();
            fetchMoodEvents(); // Call fetchMoodEvents to reset filters and fetch all mood events
        });

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(R.style.PopupDialogAnimation);
        View view = getView();
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }

    /**
     * Applies filters to a list of mood events based on user-selected criteria such as time range,
     * emotional state, or keyword. Updates the RecyclerView with filtered results accordingly.
     *
     * @param moodEvents A list of all mood events fetched from Firestore to be filtered.
     */
    private void applyFilter(List<MoodEvent> moodEvents) {
        if (selectedFilter.equals("Most Recent Week")) {
            long currentTime = System.currentTimeMillis();
            long oneWeekInMilli = 7 * 24 * 60 * 60 * 1000;
            List<MoodEvent> filteredMoodEvents = new ArrayList<>();
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
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Searches for users whose usernames contain or match a given search text. Returns a list of matching users.
     * @param searchText The text entered by the user in the search bar for searching users.
     * @return A list of User objects that match or contain the given search text.
     */
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