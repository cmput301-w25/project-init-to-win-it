package com.example.moodsync;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodsync.databinding.HomePageFragmentBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SecondFragment extends Fragment {

    private HomePageFragmentBinding binding;
    private RecyclerView moodRecyclerView;
    private MoodCardAdapter moodCardAdapter;
    private FirebaseFirestore db;
    private TextInputEditText searchBar;
    private String searchText = "";
    public LocalStorage globalStorage = LocalStorage.getInstance();
    private ListView searchResultsListView;
    private ArrayAdapter<String> searchResultsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = HomePageFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        searchBar = view.findViewById(R.id.search_bar);

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

        return view;
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

        binding.addCircleButton.setOnClickListener(v ->
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_addMoodActivityFragment)
        );

        binding.historyButton.setOnClickListener(v ->
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_moodHistoryFragment)
        );

        binding.profilePicContainer.setOnClickListener(v ->
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_editProfileFragment)
        );

        fetchMoodEvents();
    }

    private void setupRecyclerView() {
        moodRecyclerView = binding.moodRecyclerView;
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodCardAdapter = new MoodCardAdapter(new ArrayList<>()); // start empty, then load data
        moodRecyclerView.setAdapter(moodCardAdapter);
    }

    private void fetchMoodEvents() {
        db.collection("mood_events")
                .whereEqualTo("public", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MoodEvent> moodEvents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                            moodEvents.add(moodEvent);
                        }
                        moodCardAdapter.updateMoodEvents(moodEvents);
                    } else {
                        Log.e("Firestore", "Error fetching data", task.getException());
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
