package com.example.moodsync;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
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

        // inflate the search results xml layout
        View searchResultsView = inflater.inflate(R.layout.search_results, container, false);
        searchResultsListView = searchResultsView.findViewById(R.id.search_results_listview);

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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("profileImageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            loadProfileImage(imageUrl);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching profile image URL", e);
                });
    }

    private void loadProfileImage(String imageUrl) {
        Glide.with(this)
                .load(imageUrl)
                .circleCrop()
                .transform(new EditProfileActivity.RotateTransformation(90))
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


    //    changed this to only display moods of followees
    private void fetchMoodEvents() {
        // Get the logged in username from MyApplication
        MyApplication myApp = (MyApplication) requireActivity().getApplicationContext();
        String currentUsername = myApp.getLoggedInUsername();

        if (currentUsername == null || currentUsername.isEmpty()) {
            Log.e("SecondFragment", "No logged in user found");
            moodCardAdapter.updateMoodEvents(new ArrayList<>());
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

                        // Add current user to see their own posts too
                        if (!followingUsers.contains(currentUsername)) {
                            followingUsers.add(currentUsername);
                        }

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
                                                moodEvents.add(moodEvent);
                                            }
                                            filterToRecentMoods(moodEvents);
                                        } else {
                                            Log.e("Firestore", "Error fetching mood events", task.getException());
                                            Toast.makeText(getContext(), "Failed to load mood events", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            return;
                        }

                        // Query mood_events where user is in the list of followed users
                        db.collection("mood_events")
                                .whereIn("id", followingUsers)
                                .whereEqualTo("public", true)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        List<MoodEvent> moodEvents = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                            moodEvents.add(moodEvent);
                                        }
                                        filterToRecentMoods(moodEvents);
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
