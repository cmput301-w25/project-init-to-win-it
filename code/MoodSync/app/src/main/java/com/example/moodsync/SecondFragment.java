    package com.example.moodsync;

import android.os.Bundle;
import android.text.Editable;
    import android.text.TextWatcher;
    import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

    import androidx.annotation.NonNull;
    import androidx.fragment.app.Fragment;
    import androidx.navigation.fragment.NavHostFragment;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

import com.example.moodsync.databinding.HomePageFragmentBinding;
import com.google.android.material.textfield.TextInputEditText;
    import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Array;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = HomePageFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

            searchBar = view.findViewById(R.id.search_bar);


            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                    // This method is called before text is changed
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    searchText = charSequence.toString();
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    ArrayList<User> searchResult = Search(getSearchText());
                    Log.d("RESULT","got result" + searchResult.size());
                }
            });

            return view;
        }
        public String getSearchText() {
            return searchText;
        }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


            // Initialize Firestore
            db = FirebaseFirestore.getInstance();

            setupRecyclerView();

            // Set up navigation actions
            binding.addCircleButton.setOnClickListener(v ->
                    NavHostFragment.findNavController(SecondFragment.this)
                            .navigate(R.id.action_SecondFragment_to_addMoodActivityFragment)
            );

            binding.historyButton.setOnClickListener(v ->
                    NavHostFragment.findNavController(SecondFragment.this)
                            .navigate(R.id.action_SecondFragment_to_moodHistoryFragment)
            );

            // Add click listener for profile picture icon
            binding.profilePicContainer.setOnClickListener(v ->
                    NavHostFragment.findNavController(SecondFragment.this)
                            .navigate(R.id.action_SecondFragment_to_userProfileFragment)
            );


            fetchMoodEvents(); // Fetch mood events from Firestore
        }

    private void setupRecyclerView() {
        moodRecyclerView = binding.moodRecyclerView;
        moodRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moodCardAdapter = new MoodCardAdapter(new ArrayList<>()); // Start with an empty list
        moodRecyclerView.setAdapter(moodCardAdapter);
    }

    //When search is clicked
    private void fetchMoodEvents() {
        db.collection("mood_events")
                .whereEqualTo("public", true) // Filter for public mood events
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MoodEvent> moodEvents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Convert Firestore document to MoodEvent object
                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                            moodEvents.add(moodEvent);
                        }
                        // Update RecyclerView with fetched data
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

        public ArrayList<User> Search(String searchText){
            ArrayList<User> result = new ArrayList<User>();
            ArrayList<User> tempList = globalStorage.getUserList();
            for (int i= 0;i< tempList.size();i++){
                if(tempList.get(i).getUsername().contains(searchText)){
                    result.add(tempList.get(i));
                }
            }
            return result;
        }
    }


