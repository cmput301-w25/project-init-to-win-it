package com.example.moodsync;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moodsync.databinding.FragmentMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
/**
 * Represents a fragment that displays a map and provides functionality for interacting with
 * Google Maps, filtering map data, and managing user-specific mood events.
 *
 * <p>This class extends {@link Fragment} and implements {@link OnMapReadyCallback} to
 * initialize and interact with a {@link GoogleMap} instance. It also includes UI components
 * such as buttons and spinners for filtering map data.</p>
 *
 * <p>The fragment uses Firebase Firestore to fetch and manage mood-related data, including
 * mood history and moods of followed users. It also tracks the user's current location
 * and profile image URL.</p>
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap mMap;
    Button mapfilterButton;
    Button mapfilterClear;
    Spinner mapfilterSpinner;
    LocalStorage globalStorage = LocalStorage.getInstance();
    Spinner mapfilterSpinner2;
    ArrayAdapter<String> mapfilterSpinnerAdapter;
    List<String> mapfilterSpinnerData;
    ArrayAdapter<String> mapfilterSpinner2Adapter;
    List<String> mapfilterSpinner2Data;
    String selectedEmotionalState;
    String keywordEditTextData;
    private String currentUserId;
    private static final String TAG = "MapFragment";
    FirebaseFirestore db;
    List<MoodEvent> moodHistoryItems = new ArrayList<>();
    private List<MoodEvent> originalMoodHistoryItems = new ArrayList<>();
    private List<MoodEvent> moodFollowingList = new ArrayList<>();
    private List<MoodEvent> mostRecentMoodsList = new ArrayList<>();
    private List<MoodEvent> originalMoodFollowingList = new ArrayList<>();
    double currentLat;
    double currentLng;
    String profileImageUrl = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Getting current user id
        MyApplication myApp = (MyApplication) requireActivity().getApplicationContext();
        currentUserId = myApp.getLoggedInUsername();

        db = FirebaseFirestore.getInstance();
        fetchMoodEvents();
        saveOriginalMoodHistory(); //Saving original data
        saveOriginalMoodFollowingList(); //Saving original data

        //this is the code that navigates to all parts of the app.
        binding.homeButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(MapFragment.this)
                    .navigate(R.id.action_mapFragment_to_SecondFragment,
                            null,
                            new NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_in_right)
                                    .setExitAnim(R.anim.slide_out_left)
                                    .setPopEnterAnim(R.anim.slide_in_left)
                                    .setPopExitAnim(R.anim.slide_out_right)
                                    .build());
        });
        binding.addCircleButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(MapFragment.this)
                    .navigate(R.id.action_mapFragment_to_addMoodActivityFragment,
                            null,
                            new NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_in_right)
                                    .setExitAnim(R.anim.slide_out_left)
                                    .setPopEnterAnim(R.anim.slide_in_left)
                                    .setPopExitAnim(R.anim.slide_out_right)
                                    .build());
        });

        binding.historyButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(MapFragment.this)
                    .navigate(R.id.action_mapFragment_to_SecondFragment,
                            null,
                            new NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_in_right)
                                    .setExitAnim(R.anim.slide_out_left)
                                    .setPopEnterAnim(R.anim.slide_in_left)
                                    .setPopExitAnim(R.anim.slide_out_right)
                                    .build());
        });
        binding.diaryButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(MapFragment.this)
                    .navigate(R.id.action_mapFragment_to_JournalFragment,
                            null,
                            new NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_in_right)
                                    .setExitAnim(R.anim.slide_out_left)
                                    .setPopEnterAnim(R.anim.slide_in_left)
                                    .setPopExitAnim(R.anim.slide_out_right)
                                    .build());


        });
        binding.mapButton.setTextColor(getResources().getColor(R.color.green));
        binding.mapButton.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.green)));

        //Set up filterFunction
        mapfilterButton = view.findViewById(R.id.mapFilterButton);
        mapfilterSpinner = view.findViewById(R.id.mapFilterSpinner);
        mapfilterClear = view.findViewById(R.id.mapFilterClearButton);
        mapfilterSpinner2 = view.findViewById(R.id.mapFilterSpinner2);
        mapfilterSpinner.setEnabled(false);
        mapfilterSpinner2.setEnabled(false);
        mapfilterClear.setEnabled(false);

        //Adding data to Spinners
        mapfilterSpinnerData = Arrays.asList("Choose Option", "Filtered Following List", "Filtered Mood History", "Most Recent Mood");
        mapfilterSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mapfilterSpinnerData);
        mapfilterSpinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mapfilterSpinner.setAdapter(mapfilterSpinnerAdapter);

        mapfilterSpinner2Data = Arrays.asList("Choose Option", "No Filter", "Most Recent Week", "Emotional State", "Keyword");
        mapfilterSpinner2Adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mapfilterSpinner2Data);
        mapfilterSpinner2Adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        mapfilterSpinner2.setAdapter(mapfilterSpinner2Adapter);

        //Set up Filter Data
        mapfilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Saving original data

                if (originalMoodHistoryItems.size() == 0){
                    //Only saves original data once
                    saveOriginalMoodHistory();
                }
                if (originalMoodFollowingList.size() == 0){
                    //Only saves original data once
                    saveOriginalMoodFollowingList();
                }
                //Managing visibility of spinner
                if (mapfilterSpinner.getVisibility() == View.VISIBLE) {
                    mapfilterSpinner.setVisibility(View.INVISIBLE);
                    mapfilterSpinner.setEnabled(false);
                    mapfilterClear.setVisibility(View.INVISIBLE);
                    mapfilterClear.setEnabled(false);
                    mapfilterSpinner2.setVisibility(View.INVISIBLE);
                    mapfilterSpinner2.setEnabled(false);
                } else {
                    mapfilterSpinner.setVisibility(View.VISIBLE);
                    mapfilterSpinner.setEnabled(true);
                    mapfilterClear.setVisibility(View.VISIBLE);
                    mapfilterClear.setEnabled(true);
                }
            }
        });

        //Detecting which option in Spinner used:
        mapfilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = parent.getItemAtPosition(position).toString();
                if (selectedFilter.equals("Filtered Following List")){
                    //Show Options
                    mapfilterSpinner2.setVisibility(View.VISIBLE);
                    mapfilterSpinner2.setEnabled(true);

                    //Detecting which option in Spinner used:
                    mapfilterSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedFilter = parent.getItemAtPosition(position).toString();
                            if (selectedFilter.equals("Most Recent Week")){
                                //Automatically sorting
                                long currentTime = System.currentTimeMillis();
                                long oneWeekinMilli = 7 * 24 * 60 * 60 * 1000; //One week in ms

                                List<MoodEvent> filteredMoodHistory = new ArrayList<>();
                                for (MoodEvent moodht: moodFollowingList) {
                                    if ((currentTime - moodht.getDate()) <= oneWeekinMilli) {
                                        filteredMoodHistory.add(moodht);
                                    }
                                }
                                // Update the current list with the filtered data
                                moodFollowingList.clear();
                                moodFollowingList.addAll(filteredMoodHistory);
                                displayRequestedMoods(moodFollowingList, 0);

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
                                            List<MoodEvent> filteredMoodHistory = new ArrayList<>();
                                            for (MoodEvent moodht: moodFollowingList) {
                                                if (moodht.getMood().equals(selectedEmotionalState)) {
                                                    filteredMoodHistory.add(moodht);
                                                } else if (selectedEmotionalState.equals("Choose Option")){
                                                    filteredMoodHistory.add(moodht);
                                                }
                                            }
                                            // Update the current list with the filtered data
                                            moodFollowingList.clear();
                                            moodFollowingList.addAll(filteredMoodHistory);
                                            displayRequestedMoods(moodFollowingList, 0);
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
                                            List<MoodEvent> filteredMoodHistory = new ArrayList<>();
                                            for (MoodEvent moodht: moodFollowingList) {
                                                if (moodht.getDescription().contains(keywordEditTextData)) {
                                                    filteredMoodHistory.add(moodht);
                                                } else if (keywordEditTextData.equals("Choose Option")){
                                                    filteredMoodHistory.add(moodht);
                                                }


                                            }
                                            // Update the current list with the filtered data
                                            moodFollowingList.clear();
                                            moodFollowingList.addAll(filteredMoodHistory);
                                            displayRequestedMoods(moodFollowingList, 0);
                                        })
                                        .setNegativeButton("Cancel", (dialog, which) -> {
                                            dialog.dismiss();
                                        });

                                AlertDialog tempKeywordFilterDialog = keywordFilterDialog.create();
                                tempKeywordFilterDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_thick);
                                tempKeywordFilterDialog.show();
                            }
                            else if (selectedFilter.equals("No Filter")) {
                                //Just do display function
                                displayRequestedMoods(moodFollowingList, 0);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            //Do nothing if nothing is selected
                        }
                    });

                } else if (selectedFilter.equals("Filtered Mood History")) {
                    //Show Options
                    mapfilterSpinner2.setVisibility(View.VISIBLE);
                    mapfilterSpinner2.setEnabled(true);

                    //Detecting which option in Spinner used:
                    mapfilterSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedFilter = parent.getItemAtPosition(position).toString();
                            if (selectedFilter.equals("Most Recent Week")){
                                //Automatically sorting
                                long currentTime = System.currentTimeMillis();
                                long oneWeekinMilli = 7 * 24 * 60 * 60 * 1000; //One week in ms

                                List<MoodEvent> filteredMoodHistory = new ArrayList<>();
                                for (MoodEvent moodht: moodHistoryItems) {
                                    if ((currentTime - moodht.getDate()) <= oneWeekinMilli) {
                                        filteredMoodHistory.add(moodht);
                                    }
                                }
                                // Update the current list with the filtered data
                                moodHistoryItems.clear();
                                moodHistoryItems.addAll(filteredMoodHistory);
                                displayRequestedMoods(moodHistoryItems, 1);

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
                                            List<MoodEvent> filteredMoodHistory = new ArrayList<>();
                                            for (MoodEvent moodht: moodHistoryItems) {
                                                if (moodht.getMood().equals(selectedEmotionalState)) {
                                                    filteredMoodHistory.add(moodht);
                                                } else if (selectedEmotionalState.equals("Choose Option")){
                                                    filteredMoodHistory.add(moodht);
                                                }
                                            }
                                            // Update the current list with the filtered data
                                            moodHistoryItems.clear();
                                            moodHistoryItems.addAll(filteredMoodHistory);
                                            displayRequestedMoods(moodHistoryItems, 1);
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
                                            List<MoodEvent> filteredMoodHistory = new ArrayList<>();
                                            for (MoodEvent moodht: moodHistoryItems) {
                                                if (moodht.getDescription().contains(keywordEditTextData)) {
                                                    filteredMoodHistory.add(moodht);
                                                } else if (keywordEditTextData.equals("Choose Option")){
                                                    filteredMoodHistory.add(moodht);
                                                }


                                            }
                                            // Update the current list with the filtered data
                                            moodHistoryItems.clear();
                                            moodHistoryItems.addAll(filteredMoodHistory);
                                            displayRequestedMoods(moodHistoryItems, 1);
                                        })
                                        .setNegativeButton("Cancel", (dialog, which) -> {
                                            dialog.dismiss();
                                        });

                                AlertDialog tempKeywordFilterDialog = keywordFilterDialog.create();
                                tempKeywordFilterDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_thick);
                                tempKeywordFilterDialog.show();
                            }
                            else if (selectedFilter.equals("No Filter")) {
                                //Just do display function
                                displayRequestedMoods(moodHistoryItems, 1);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            //Do nothing if nothing is selected
                        }
                    });


                } else if (selectedFilter.equals("Most Recent Mood")) {
                    //Sorting mostRecentMoodsList
                    Collections.sort(mostRecentMoodsList, (mood1, mood2) ->
                            Long.compare(mood2.getDate(), mood1.getDate()));

                    //Filtering mostRecentMoodsList
                    List<String> seenIDs = new ArrayList<String>(); //Getting only MOST RECENT

                    List<MoodEvent> recentFilteredMoods = new ArrayList<>();
                    for (MoodEvent recentMood: mostRecentMoodsList) {
                        if (!(seenIDs.contains(recentMood.getId()))) {
                            seenIDs.add(recentMood.getId());
                            if (recentMood.getLocation() != null){
                                Log.d(TAG, "Checking location of moodEvent wOHOO");
                                //Checking if within 5 km of current location:
                                String[] latLngParts = recentMood.getLocation().split(",");
                                double moodEventLat = Double.parseDouble(latLngParts[0]);
                                double moodEventLng = Double.parseDouble(latLngParts[1]);

                                // Calculate the distance between the current location and the mood event location
                                float distance = calculateManhattanDistance(currentLat, currentLng, moodEventLat, moodEventLng);

                                // Check if the distance is within 5 km
                                Log.d(TAG, "The distance: "+distance);
                                if (distance <= 5000) {
                                    Log.d(TAG, "Adding moodEvent with distance: "+distance);
                                    recentFilteredMoods.add(recentMood);
                                }


                            }
                        }
                    }
                    Log.d(TAG, "Size of MostList: "+recentFilteredMoods.size());
                    mostRecentMoodsList.clear();
                    mostRecentMoodsList.addAll(recentFilteredMoods);
                    Log.d(TAG, "Initial Filtering Completed!");
                    displayRequestedMoods(mostRecentMoodsList, 0);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing if nothing is selected
            }
        });

        //Clear Button Functionality (To restore filters)
        mapfilterClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                putCurrentSpot();
                mapfilterSpinner.setSelection(0);
                mapfilterSpinner2.setSelection(0);
                mapfilterSpinner2.setVisibility(View.INVISIBLE);
                mapfilterSpinner2.setEnabled(false);
                selectedEmotionalState = "";
                keywordEditTextData = "";
                //Restoring Original Data
                if (originalMoodHistoryItems.size() == 0){
                    return;
                }
                moodHistoryItems.clear();
                moodHistoryItems.addAll(originalMoodHistoryItems);
                if (originalMoodFollowingList.size() == 0){
                    return;
                }
                moodFollowingList.clear();
                moodFollowingList.addAll(originalMoodFollowingList);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Retrieves a {@link Bitmap} from a given image URL using the Glide library.
     *
     * <p>This method synchronously loads an image from the specified URL into a Bitmap object.
     * It uses Glide's `.submit().get()` method, which blocks the calling thread until the image
     * is loaded. This should be used cautiously as it may cause UI thread blocking.</p>
     *
     * @param context The context used to initialize Glide.
     * @param imageUrl The URL of the image to be loaded.
     * @return A {@link Bitmap} object representing the loaded image, or {@code null} if an error occurs.
     */
    public static Bitmap getBitmapFromUrl(Context context, String imageUrl) {
        try {
            // Glide synchronously loads the image into the Bitmap
            return Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .submit()
                    .get(); // This blocks and waits for the image to be loaded
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a profile image into an {@link ImageView} using the Glide library.
     *
     * <p>This method fetches an image from a given URL and displays it in the specified ImageView.
     * The image is cropped into a circular shape and a placeholder is displayed while the image is loading.</p>
     *
     * @param imageurl The URL of the profile image to be loaded.
     * @param img The {@link ImageView} where the profile image will be displayed.
     */
    public void loadProfileImage(String imageurl,ImageView img){
        Glide.with(this)
                .load(imageurl)
                .circleCrop()
                .placeholder(R.drawable.ic_add_black_24dp)
                .into(img);
    }

    /**
     * Displays mood events as markers on a Google Map.
     *
     * <p>This method takes a list of {@link MoodEvent} objects and displays them on the map as custom markers.
     * Each marker is customized with an emoji representing the mood and optionally includes a profile image.
     * If no mood events are found, a toast message is shown to inform the user.</p>
     *
     * <p>Before adding markers, this method checks for location permissions and requests them if necessary.</p>
     *
     * @param chosenArray The list of {@link MoodEvent} objects to be displayed on the map.
     * @param mode The mode of display (e.g., 1 for mood history, other values for general display).
     */
    public void displayRequestedMoods(List<MoodEvent> chosenArray, int mode) {
        if (chosenArray.size() == 0){
            Toast.makeText(requireContext(), "No mood locations found 😞", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Requesting Permissions
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(false);
        mMap.clear();
        for (MoodEvent moodht: chosenArray) {
            DocumentReference uref = db.collection("users").document(moodht.getId());
            //Default data
            //Trying to fetch image of user into the icon
            profileImageUrl = "";
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_crowd);

            uref.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    profileImageUrl = documentSnapshot.getString("profileImageUrl");
                }
            });
            try {
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    bitmap = getBitmapFromUrl(getContext(), profileImageUrl);
                }
            }
            catch (Exception except) {
                //Nothing
            }
            Bitmap customMarkerBitmap = editCustomMarker(bitmap, getEmojiForMood(moodht.getMood()), moodht.getId());
            if (mode == 1){
                //Mood History
                customMarkerBitmap = editCustomMarker(bitmap, getEmojiForMood(moodht.getMood()), currentUserId);
            }



            //Getting location
            String[] latLngParts = moodht.getLocation().split(",");
            double lat = Double.parseDouble(latLngParts[0]);
            double longitude = Double.parseDouble(latLngParts[1]);
            LatLng location = new LatLng(lat, longitude);

            //Adding marker to map
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .icon(BitmapDescriptorFactory.fromBitmap(customMarkerBitmap)));
        }
    }

    /**
     * Saves a copy of the current mood history list for later use.
     *
     * <p>This method creates a deep copy of the current list of mood history items and stores it in
     * {@code originalMoodHistoryItems}. This allows filters or modifications to be reset back to
     * their original state.</p>
     */
    private void saveOriginalMoodHistory() {
        originalMoodHistoryItems = new ArrayList<>(moodHistoryItems);
    }

    /**
     * Saves a copy of the current list of moods from followed users for later use.
     *
     * <p>This method creates a deep copy of the current list of moods from followed users and stores it in
     * {@code originalMoodFollowingList}. This allows filters or modifications to be reset back to
     * their original state.</p>
     */
    private void saveOriginalMoodFollowingList() {
        originalMoodFollowingList = new ArrayList<>(moodFollowingList);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //Going to Current Location
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Requesting Permissions
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLat = location.getLatitude();
                        currentLng = location.getLongitude();
                        LatLng currentLatLng = new LatLng(currentLat, currentLng);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                    }

                });
    }

    /**
     * Centers the map on the user's current location and enables location tracking.
     *
     * <p>This method performs three key actions:
     * 1. Checks for required location permissions and requests them if missing
     * 2. Enables the "My Location" dot on the map
     * 3. Retrieves the last known location and animates the camera to that position
     *
     * <p>Uses {@link FusedLocationProviderClient} to efficiently retrieve device location.
     * The camera zooms to level 15 (street-level view) when moving to the location.</p>
     */
    public void putCurrentSpot(){
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                    }

                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Generates a custom map marker bitmap by combining multiple visual elements.
     *
     * <p>The marker composition includes:
     * - A base image (typically user profile picture)
     * - An emoji overlay representing the mood
     * - User ID text displayed below the marker
     *
     * @param imageBitmap The base image to use for the marker (default: ic_crowd drawable)
     * @param emoji       Unicode emoji character to display on the marker
     * @param user_id     User identifier to show below the marker
     * @return Bitmap containing the composed marker graphic
     *
     */
    private Bitmap editCustomMarker(Bitmap imageBitmap, String emoji, String user_id) {
        View markerView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_marker, null);

        ImageView img = markerView.findViewById(R.id.marker_image);
        TextView emj= markerView.findViewById(R.id.marker_emoji);
        TextView usrid = markerView.findViewById(R.id.marker_id);

     loadProfileImage(globalStorage.getUserFromUName(user_id).getPfpUrl(),img);
        emj.setText(emoji);
        usrid.setText(user_id);

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        return bitmap;
    }

    /**
     * Fetches mood events for the current user and their followed users from Firestore.
     *
     * <p>This method retrieves:
     * 1. The current user's mood events from the "mood_events" collection, storing them in {@code moodHistoryItems}.
     * 2. Public mood events of followed users, storing them in {@code moodFollowingList}.
     *
     * <p>If the user has no followers, only their own public moods are fetched. Missing or invalid data (e.g., null location) is skipped.
     * The results are processed asynchronously using Firestore's {@code OnCompleteListener}.
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
                            String trigger = document.getString("trigger");
                            String location = document.getString("location");
                            String imageUrl = document.getString("imageUrl");
                            boolean isPublic = document.getBoolean("public");
                            String socialSituation = document.getString("socialSituation");
                            String mood = document.getString("mood");
                            String description = document.getString("description");
                            Long date = document.getLong("date");


                            if (location == null) {
                                Log.w(TAG, "Missing data in document: " + id);
                                continue;
                            }

                            MoodEvent item = new MoodEvent(mood, trigger, description, socialSituation, date, imageUrl, isPublic, id, null, null , location);
                            item.setId(id);
                            moodHistoryItems.add(item);
                            Log.d(TAG, "Added item: " + item.toString());
                        }

                        //Sorting doesnt matter as of now
                        Collections.sort(moodHistoryItems, (item1, item2) -> Long.compare(item2.getDate(), item1.getDate()));
                        Log.d(TAG, "Sorted items. First item date: " +
                                (moodHistoryItems.isEmpty() ? "N/A" : moodHistoryItems.get(0).getDate()));

                        Log.d(TAG, "Number of items fetched: " + moodHistoryItems.size());
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

        //Fetching Mood Following List and storing followers' most recent mood events
        // First, get the current user's document to access their followingList
        db.collection("users")
                .whereEqualTo("userName", currentUserId)
                .get()
                .addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful() && !userTask.getResult().isEmpty()) {
                        DocumentSnapshot userDoc = userTask.getResult().getDocuments().get(0);
                        List<String> followingUsers = (List<String>) userDoc.get("followingList");

                        if (followingUsers == null) {
                            followingUsers = new ArrayList<>();
                        }
                        if (!followingUsers.isEmpty()) {
                            // Query mood_events where user is in the list of followed users
                            db.collection("mood_events")
                                    .whereIn("id", followingUsers)
                                    .whereEqualTo("public", true)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                                moodEvent.setDocumentId(document.getId());
                                                if (moodEvent.getLocation() != null) {
                                                    mostRecentMoodsList.add(moodEvent);
                                                    Log.d(TAG, "Adding moodEvent of mostRecent");
                                                }
                                            }
                                        } else {
                                            Log.e("Firestore", "Error fetching mood events", task.getException());
                                        }
                                    });
                        }
                        //Add current user to see their own posts too
                        if (!followingUsers.contains(currentUserId)) {
                            followingUsers.add(currentUserId);
                        }

                        // If not following anyone (just self), show only own moods
                        if (followingUsers.size() <= 1) {
                            db.collection("mood_events")
                                    .whereEqualTo("id", currentUserId)
                                    .whereEqualTo("public", true)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                                moodEvent.setDocumentId(document.getId());
                                                if (moodEvent.getLocation() != null){
                                                    moodFollowingList.add(moodEvent);
                                                }
                                            }
                                        } else {
                                            Log.e("Firestore", "Error fetching mood events", task.getException());
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
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                            moodEvent.setDocumentId(document.getId());
                                            if (moodEvent.getLocation() != null){
                                                moodFollowingList.add(moodEvent);
                                                Log.d(TAG, "Adding moodEvent");
                                            }
                                        }
                                    } else {
                                        Log.e("Firestore", "Error fetching mood events", task.getException());
                                    }
                                });


                    } else {

                        Log.e("Firestore", "Error fetching user data", userTask.getException());
                    }
                });



    }

    /**
     * Calculates approximate Manhattan distance between two geographic coordinates.
     *
     * <p>Uses spherical Earth projection to convert latitude/longitude differences
     * into meters. Longitude distance is adjusted for latitude convergence.</p>
     *
     * @param currentLat Current location's latitude in degrees
     * @param currentLng Current location's longitude in degrees
     * @param eventLat   Target location's latitude in degrees
     * @param eventLng   Target location's longitude in degrees
     * @return Manhattan distance in meters (sum of N/S and E/W distances)
     */
    private float calculateManhattanDistance(double currentLat, double currentLng, double eventLat, double eventLng) {
        final double EARTH_RADIUS = 6371000;

        // Convert latitude and longitude differences into radians
        double latDiff = Math.toRadians(eventLat - currentLat);
        double lngDiff = Math.toRadians(eventLng - currentLng);

        // Convert the differences into distances (in meters)
        double latDistance = latDiff * EARTH_RADIUS;
        double lngDistance = lngDiff * EARTH_RADIUS * Math.cos(Math.toRadians(currentLat)); // Adjust lng difference for latitude

        // Return Manhattan distance
        return (float) (Math.abs(latDistance) + Math.abs(lngDistance));
    }

    /**
     * Maps mood strings to representative emoji characters.
     *
     * <p>Supported moods (case-insensitive):
     * - Happy → 😊
     * - Sad → 😢
     * - Excited → 😃
     * - Angry → 😠
     * - Confused → 😕
     * - Surprised → 😲
     * - Ashamed → 😳
     * - Scared → 😨
     * - Disgusted → 🤢
     *
     * @param mood Text description of mood (e.g., "happy")
     * @return Corresponding emoji character or empty string if unknown
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
}