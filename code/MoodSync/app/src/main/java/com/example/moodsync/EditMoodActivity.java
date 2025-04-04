package com.example.moodsync;
import static android.app.Activity.RESULT_OK;
import static com.example.moodsync.BitmapUtils.compressImageFromUri;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.example.moodsync.databinding.EditMoodFragmentBinding;
import com.example.moodsync.databinding.EditMoodFragment2Binding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EditMoodActivity extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String moodDescription;
    private String selectedMood;
    private Uri photoUri;
    private EditMoodFragmentBinding binding1;
    private EditMoodFragment2Binding binding2;
    private boolean isSecondLayout = false;
    private RelativeLayout mainLayout;
    static String imageUrl;
    private String selectedSongUrl;
    private String selectedSongTitle;

    private final Map<String, Integer> moodGradients = new HashMap<>();
    LocalStorage globalStorage = LocalStorage.getInstance();

    private ImageView happyImage, sadImage, angryImage, confusedImage, surprisedImage, ashamedImage, scaredImage, disgustedImage;
    private ImageView lastSelectedImageView = null;
    private Button selectedSocialSituationButton = null;

    private static final int ANIMATION_DURATION = 300; // Animation duration in milliseconds

    private FirebaseFirestore db;
    private CollectionReference moodEventsRef;
    private MoodEvent moodEventToEdit;
    public long oldTime;
    private String currentLocation = null; //Default to no location
    private FusedLocationProviderClient fusedLocationClient;

    private boolean isPublic = false; // Default to private
    /**
     * Creates the view for the fragment.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The root view of the fragment's layout.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FirebaseApp.initializeApp(requireContext());
        db = FirebaseFirestore.getInstance();
        moodEventsRef = db.collection("mood_events");

        if (getArguments() != null && getArguments().getBoolean("isSecondLayout", false)) {
            isSecondLayout = true;
            binding2 = EditMoodFragment2Binding.inflate(inflater, container, false);
            return binding2.getRoot();
        } else {
            binding1 = EditMoodFragmentBinding.inflate(inflater, container, false);
            return binding1.getRoot();
        }
    }
    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned, but before any saved state has been restored in to the view.
     *
     * @param view               The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        moodGradients.put("Happy", R.drawable.happy_gradient);
        moodGradients.put("Sad", R.drawable.sad_gradient);
        moodGradients.put("Angry", R.drawable.angry_gradient);
        moodGradients.put("Confused", R.drawable.confused_gradient);
        moodGradients.put("Surprised", R.drawable.surprised_gradient);
        moodGradients.put("Ashamed", R.drawable.ashamed_gradient);
        moodGradients.put("Scared", R.drawable.scared_gradient);
        moodGradients.put("Disgusted", R.drawable.disgusted_gradient);

        moodEventToEdit = globalStorage.getMoodEvent(globalStorage.getCurrentMoodForEdit());

        if (moodEventToEdit == null) {
            // Handle error:  Maybe navigate back or show an error message.
            Toast.makeText(requireContext(), "Error: No mood event to edit", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSecondLayout) {
            setupSecondLayout();
        } else {
            setupFirstLayout(view);
        }
    }
    /**
     * Fetches songs for a specific mood by querying a Firestore collection.
     * Displays a loading dialog while fetching the songs and shows a song selection dialog
     * if songs are successfully retrieved.
     *
     * @param collectionName The name of the Firestore collection to query for songs.
     */
    private void fetchSongsForMood(String collectionName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Show loading dialog
        AlertDialog.Builder loadingBuilder = new AlertDialog.Builder(requireContext());
        loadingBuilder.setTitle("Loading Songs");
        loadingBuilder.setMessage("Please wait while we fetch songs...");
        AlertDialog loadingDialog = loadingBuilder.create();
        loadingDialog.show();

        db.collection(collectionName).get().addOnCompleteListener(task -> {
            loadingDialog.dismiss();

            if (task.isSuccessful()) {
                List<String> songTitles = new ArrayList<>();
                List<String> songUrls = new ArrayList<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String title = document.getString("title");
                    String url = document.getString("url");
                    String singer = document.getString("singer");

                    if (title != null && url != null) {
                        String displayTitle = (singer != null && !singer.isEmpty())
                                ? title + " - " + singer
                                : title;

                        songTitles.add(displayTitle);
                        songUrls.add(url);
                    }
                }

                if (songTitles.isEmpty()) {
                    Toast.makeText(requireContext(), "No songs found for this mood", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show song selection dialog
                showSongSelectionDialog(songTitles, songUrls);
            } else {
                Toast.makeText(requireContext(), "Failed to load songs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays a song selection dialog with a list of song titles and URLs.
     * Allows the user to select a song from the list and updates the music spinner accordingly.
     *
     * @param songTitles A list of song titles to display in the dialog.
     * @param songUrls   A list of corresponding song URLs for each title.
     */
    private void showSongSelectionDialog(List<String> songTitles, List<String> songUrls) {AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_song_selection_dialog, null);builder.setView(dialogView);ListView listView = dialogView.findViewById(R.id.songListView);ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, songTitles);listView.setAdapter(adapter);AlertDialog dialog = builder.create();listView.setOnItemClickListener((parent, view, position, id) -> {this.selectedSongUrl = songUrls.get(position);this.selectedSongTitle = songTitles.get(position);ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item);spinnerAdapter.add(this.selectedSongTitle);spinnerAdapter.add("Choose a song");spinnerAdapter.add("No music");spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);binding1.musicSpinner.setAdapter(spinnerAdapter);binding1.musicSpinner.setSelection(0);Toast.makeText(requireContext(), "Selected: " + this.selectedSongTitle, Toast.LENGTH_SHORT).show();dialog.dismiss();});dialog.show();}

    /**
     * Sets up the first layout of the fragment.
     *
     * @param view The root view of the layout.
     */
    private void setupFirstLayout(View view) {
        mainLayout = view.findViewById(R.id.main_layout);

        happyImage = view.findViewById(R.id.happy_image);
        sadImage = view.findViewById(R.id.sad_image);
        angryImage = view.findViewById(R.id.angry_image);
        confusedImage = view.findViewById(R.id.confused_image);
        surprisedImage = view.findViewById(R.id.surprised_image);
        ashamedImage = view.findViewById(R.id.ashamed_image);
        scaredImage = view.findViewById(R.id.scared_image);
        disgustedImage = view.findViewById(R.id.disgusted_image);
        View rectangle2 = view.findViewById(R.id.rectangle_2);

        happyImage.setOnClickListener(v -> selectMood("Happy", happyImage));
        sadImage.setOnClickListener(v -> selectMood("Sad", sadImage));
        angryImage.setOnClickListener(v -> selectMood("Angry", angryImage));
        confusedImage.setOnClickListener(v -> selectMood("Confused", confusedImage));
        surprisedImage.setOnClickListener(v -> selectMood("Surprised", surprisedImage));
        ashamedImage.setOnClickListener(v -> selectMood("Ashamed", ashamedImage));
        scaredImage.setOnClickListener(v -> selectMood("Scared", scaredImage));
        disgustedImage.setOnClickListener(v -> selectMood("Disgusted", disgustedImage));
        rectangle2.setOnClickListener(v -> showPhotoOptionsDialog());

        Spinner moodSpinner = binding1.mainCard;
        EditText descriptionInput = binding1.editDescription;

        String moodToSelect = moodEventToEdit.getMood();
        if (moodToSelect != null && !moodToSelect.isEmpty()) {
            int spinnerPosition = getPositionOfValue(moodSpinner, moodToSelect);
            if (spinnerPosition != -1) {
                moodSpinner.setSelection(spinnerPosition);
            }
        }

        descriptionInput.setText(moodEventToEdit.getDescription());
        oldTime = moodEventToEdit.getDate();

        if (moodEventToEdit != null && moodEventToEdit.getSongTitle() != null) {
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    new String[]{moodEventToEdit.getSongTitle(), "None", "Happy", "Sad", "Angry", "Confused", "Surprised", "Ashamed", "Disgusted", "Scared"}
            );
            binding1.musicSpinner.setAdapter(spinnerAdapter);
            binding1.musicSpinner.setSelection(0);
        }

        moodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMood = parent.getItemAtPosition(position).toString();
                updateBackgroundColor(selectedMood);
                Toast.makeText(getContext(), "Selected: " + selectedMood, Toast.LENGTH_SHORT).show();

                switch (selectedMood) {
                    case "Happy":
                        selectMood("Happy", happyImage);
                        break;
                    case "Sad":
                        selectMood("Sad", sadImage);
                        break;
                    case "Angry":
                        selectMood("Angry", angryImage);
                        break;
                    case "Confused":
                        selectMood("Confused", confusedImage);
                        break;
                    case "Surprised":
                        selectMood("Surprised", surprisedImage);
                        break;
                    case "Ashamed":
                        selectMood("Ashamed", ashamedImage);
                        break;
                    case "Scared":
                        selectMood("Scared", scaredImage);
                        break;
                    case "Disgusted":
                        selectMood("Disgusted", disgustedImage);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //do nothing
            }
        });


        binding1.musicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();

                // If a mood is selected, fetch songs for that mood
                if (!selectedOption.equals("None")) {
                    // Convert the mood name to lowercase for collection name
                    String collectionName = "songs_" + selectedOption.toLowerCase();
                    fetchSongsForMood(collectionName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        binding1.cancel.setOnClickListener(v -> NavHostFragment.findNavController(EditMoodActivity.this)
                .navigate(R.id.action_editMoodActivityFragment_to_moodHistoryFragment));

        // In setupFirstLayout()
        binding1.next.setOnClickListener(v -> {
            String selectedMood = binding1.mainCard.getSelectedItem().toString();

            if (selectedMood.equals("None")) {
                Toast.makeText(requireContext(), "Please select a mood other than 'None'", Toast.LENGTH_SHORT).show();
            } else {
                this.selectedMood = selectedMood;
                this.moodDescription = binding1.editDescription.getText().toString();

                Bundle args = new Bundle();
                args.putBoolean("isSecondLayout", true);
                args.putString("selectedMood", this.selectedMood);
                args.putString("description", this.moodDescription);
                args.putString("songTitle" , this.selectedSongTitle);
                args.putString("songUrl" , this.selectedSongUrl);
                args.putParcelable("moodEvent", (Parcelable) moodEventToEdit); //Pass the object to the fragment.

                NavHostFragment.findNavController(EditMoodActivity.this)
                        .navigate(R.id.action_editMoodActivityFragment_to_editMoodActivityFragment2, args);
            }
        });

    }

    private void showSongSelectionDialog() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Show loading dialog
        AlertDialog.Builder loadingBuilder = new AlertDialog.Builder(requireContext());
        loadingBuilder.setTitle("Loading Songs");
        loadingBuilder.setMessage("Please wait while we fetch songs...");
        AlertDialog loadingDialog = loadingBuilder.create();
        loadingDialog.show();

        // Define collections
        List<String> collectionNames = Arrays.asList(
                "songs_happy", "songs_sad", "songs_angry",
                "songs_disgusted", "songs_surprised", "songs_excited");

        List<String> songTitles = new ArrayList<>();
        List<String> songUrls = new ArrayList<>();
        List<String> songCategories = new ArrayList<>();

        final int[] completedQueries = {0};
        final int totalQueries = collectionNames.size();

        for (String collectionName : collectionNames) {
            db.collection(collectionName).get().addOnCompleteListener(task -> {
                completedQueries[0]++;

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String title = document.getString("title");
                        String url = document.getString("url");
                        String singer = document.getString("singer");

                        if (title != null && url != null) {
                            String displayTitle = (singer != null && !singer.isEmpty())
                                    ? title + " - " + singer
                                    : title;

                            songTitles.add(displayTitle);
                            songUrls.add(url);
                            songCategories.add(collectionName.replace("songs_", ""));
                        }
                    }
                }

                // Check if all queries are done
                if (completedQueries[0] >= totalQueries) {
                    loadingDialog.dismiss();

                    if (songTitles.isEmpty()) {
                        Toast.makeText(requireContext(), "No songs found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create song selection dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Select a Song");

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_list_item_1, // Fixed layout issue
                            songTitles);

                    builder.setAdapter(adapter, (dialog, which) -> {
                        selectedSongUrl = songUrls.get(which);
                        selectedSongTitle = songTitles.get(which);

                        // Update spinner
                        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_spinner_item);
                        spinnerAdapter.add(selectedSongTitle);
                        spinnerAdapter.add("Happy");
                        spinnerAdapter.add("Sad");
                        spinnerAdapter.add("Ashamed");
                        spinnerAdapter.add("Disgusted");
                        spinnerAdapter.add("Scared");
                        spinnerAdapter.add("Angry");
                        spinnerAdapter.add("Surprised");
                        spinnerAdapter.add("Confused");
                        binding1.musicSpinner.setAdapter(spinnerAdapter);
                        binding1.musicSpinner.setSelection(0);

                        Toast.makeText(requireContext(), "Selected: " + selectedSongTitle, Toast.LENGTH_SHORT).show();
                    });

                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                    builder.show();
                }
            });

        }


    }

    /**
     * Resets the background of all mood-related emoji image views to transparent.
     * This ensures that no emoji is highlighted or selected visually.
     */
    private void resetAllEmojis() {
        happyImage.setBackgroundResource(android.R.color.transparent);
        sadImage.setBackgroundResource(android.R.color.transparent);
        angryImage.setBackgroundResource(android.R.color.transparent);
        confusedImage.setBackgroundResource(android.R.color.transparent);
        surprisedImage.setBackgroundResource(android.R.color.transparent);
        ashamedImage.setBackgroundResource(android.R.color.transparent);
        scaredImage.setBackgroundResource(android.R.color.transparent);
        disgustedImage.setBackgroundResource(android.R.color.transparent);
    }

    /**
     * Returns an emoji representing a specific mood.
     * Maps mood names to their corresponding emojis.
     *
     * @param mood The mood name (e.g., "Happy", "Sad", etc.)
     * @return Emoji for the given mood, or an empty string if not found.
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


    /**
     * Sets the given value on the spinner based on a string value.
     *
     * @param spinner The spinner to update.
     * @param value The value to select in the spinner.
     */
    private void setSpinnerToValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    /**
     * Callback interface for fetching original mood data.
     */
    public interface OnMoodFetchedListener {
        /**
         * Called when the original mood is successfully fetched.
         * @param originalMood The original mood string, or null if fetching fails.
         */
        void onMoodFetched(String originalMood);
    }

    /**
     * Fetches the original mood from a Firestore document.
     * Uses a callback to notify the listener of the result.
     *
     * @param documentId The ID of the Firestore document containing the mood.
     * @param listener   The listener to notify with the fetched mood.
     */
    private void fetchOriginalMood(String documentId, OnMoodFetchedListener listener) {
        moodEventsRef.document(documentId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String originalMood = task.getResult().getString("mood");
                listener.onMoodFetched(originalMood);
            } else {
                Log.e("FIRESTORE", "Failed to fetch original mood", task.getException());
                listener.onMoodFetched(null); // Handle failure
            }
        });
    }

    /**
     * Sets up the second layout, initializing the fields, mood descriptions,
     * and handling the creation or update of a mood event.
     */
    private void setupSecondLayout() {
        Button publicButton = binding2.publicButton;
        Button privateButton = binding2.privateButton;

        publicButton.setOnClickListener(v -> {
            isPublic = true;
            animateButtonClick(publicButton);
            privateButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_normal));
        });

        privateButton.setOnClickListener(v -> {
            isPublic = false;
            animateButtonClick(privateButton);
            publicButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_normal));
        });
        Button locationYesButton = binding2.locationYesButton;
        Button locationNoButton = binding2.locationNoButton;
        if (moodEventToEdit.getLocation() != null && !moodEventToEdit.getLocation().isEmpty()) {
            currentLocation = moodEventToEdit.getLocation();
            animateButtonClick(locationYesButton);
            locationNoButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_normal));
        } else {
            animateButtonClick(locationNoButton);
            locationYesButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_normal));
        }

        if (moodEventToEdit.isPublic()) {
            isPublic = true;
            animateButtonClick(publicButton);
            privateButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_normal));
        } else {
            isPublic = false;
            animateButtonClick(privateButton);
            publicButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_normal));
        }


        locationYesButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }

            //Checking fusedLocationClient works
            if (fusedLocationClient == null) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    String latLngFormatted = location.getLatitude() + "," + location.getLongitude();
                    currentLocation = latLngFormatted;

                    // Only animates button if current location given
                    animateButtonClick(locationYesButton);
                    locationNoButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_normal));
                }
            });
        });

        locationNoButton.setOnClickListener(v -> {
            currentLocation = null;
            animateButtonClick(locationNoButton);
            locationYesButton.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_normal));
        });

        Log.d("LIFECYCLE", "setupSecondLayout called");
        if (getArguments() != null) {
            this.selectedMood = getArguments().getString("selectedMood", "");
            this.moodDescription = getArguments().getString("description", "");
            this.selectedSongTitle = getArguments().getString("songTitle", "");
            this.selectedSongUrl = getArguments().getString("songUrl","");
            moodEventToEdit = getArguments().getParcelable("moodEvent"); // Get moodEvent from parameters
        }



        EditText triggerInput = binding2.ReasonInput;
        triggerInput.setText(moodEventToEdit.getTrigger());

        String socialSituation = moodEventToEdit.getSocialSituation();
        if (socialSituation != null && !socialSituation.isEmpty()) {
            if (socialSituation.equals(binding2.ss1.getText().toString())) {
                selectSocialSituation(binding2.ss1);
            } else if (socialSituation.equals(binding2.ss2.getText().toString())) {
                selectSocialSituation(binding2.ss2);
            } else if (socialSituation.equals(binding2.ss3.getText().toString())) {
                selectSocialSituation(binding2.ss3);
            } else if (socialSituation.equals(binding2.ss4.getText().toString())) {
                selectSocialSituation(binding2.ss4);
            }
        }
        oldTime = moodEventToEdit.getDate();

        binding2.createmood.setOnClickListener(v -> {

            String trigger = triggerInput.getText().toString();

            String socialSituation1 = (selectedSocialSituationButton != null) ?
                    selectedSocialSituationButton.getText().toString() : "None";

            String username = ((MyApplication) requireActivity().getApplication()).getLoggedInUsername();

            MoodEvent moodEvent = new MoodEvent(
                    this.selectedMood,
                    trigger,
                    this.moodDescription,
                    socialSituation,
                    oldTime, // Pass the timestamp to the MoodEvent
                    imageUrl,
                    isPublic,
                    username,
                    this.selectedSongUrl,
                    this.selectedSongTitle,
                    globalStorage.getMoodEvent(globalStorage.getCurrentMoodForEdit()).getLocation()
            );


            Log.d("FIREBASE", "Saving: " + moodEvent);

            updateMoodEvent(moodEvent);
            showSuccessDialogUI();
        });


        binding2.backButton.setOnClickListener(v -> NavHostFragment.findNavController(EditMoodActivity.this)
                .navigateUp());

        binding2.ss1.setOnClickListener(v -> selectSocialSituation(binding2.ss1));
        binding2.ss2.setOnClickListener(v -> selectSocialSituation(binding2.ss2));
        binding2.ss3.setOnClickListener(v -> selectSocialSituation(binding2.ss3));
        binding2.ss4.setOnClickListener(v -> selectSocialSituation(binding2.ss4));

    }

    /**
     * Selects the social situation button and animates the selection.
     *
     * @param button The button to select.
     */
    private void selectSocialSituation(Button button) {
        if (selectedSocialSituationButton != null) {
            animateButtonDeselection(selectedSocialSituationButton);
        }

        animateButtonSelection(button);
        selectedSocialSituationButton = button;
    }
    /**
     * Displays a dialog offering options to add a photo from the camera or gallery.
     */
    private void showPhotoOptionsDialog() {
        String[] options = {"Add from Camera", "Add from Photos"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openGallery();
                    }
                })
                .show();
    }
    /**
     * Applies a pulsing animation effect to a button when clicked.
     * Also updates the button's background tint to indicate selection.
     *
     * @param button The button view to animate.
     */
    private void animateButtonClick(Button button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.1f, 1f);

        scaleX.setDuration(ANIMATION_DURATION);
        scaleY.setDuration(ANIMATION_DURATION);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();

        button.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_selected));
    }

    /**
     * Checks the size of an image.
     *
     * @param imageUri The URI of the image to check.
     * @return The size of the image in kilobytes.
     */
    private long checkImageSize(Uri imageUri) {
        Cursor cursor = requireContext().getContentResolver().query(imageUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            if (sizeIndex != -1) {
                long imageSizeInBytes = cursor.getLong(sizeIndex);
                long imageSizeInKB = imageSizeInBytes / 1024;
                Log.d("Image Size", "Size in bytes: " + imageSizeInKB);
                return imageSizeInKB;
            }
            cursor.close();
        }
        return 0;
    }
    /**
     * Opens the camera to capture an image.
     */
    private void openCamera() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            createImageFile();
            if (photoUri != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, 1);

            }
        }
    }

    /**
     * Creates a new image file.
     */
    private void createImageFile() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()));
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/");

        photoUri = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }

    /**
     * Opens the gallery to select an image.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    /**
     * Uploads an image to Firebase Storage.
     *
     * @param imageUri The URI of the image to upload.
     * @param listener The listener to notify when the upload is complete.
     */
    private void uploadImageToFirebase(Uri imageUri, OnImageUploadedListener listener) {
        try {
            File imageFile = new File(imageUri.getPath()); // Get the image as a File
            long imageSizeInKB = checkImageSize(imageUri); // Get image size in KB
            File compressedFile = null;
            Uri uploadUri = imageUri;

            if (imageSizeInKB > 64) {
                compressedFile = compressImageFromUri(this.getContext(), imageUri); // Compress the image
                if (compressedFile != null) {
                    uploadUri = Uri.fromFile(compressedFile);
                    Log.d("COMPRESSION", "Image compressed and be rotated");
                    // Handle 90-degree rotation for compressed images
                }
            } else {
                Log.d("COMPRESSION", "no compression needed");
            }
            if (uploadUri != null) {
                if(compressedFile != null){
                    uploadUri = rotateImage(this.getContext(), uploadUri, 90); // rotate image by 90 degrees

                }

                String path = "mood_images/" + UUID.randomUUID().toString();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(path);

                UploadTask uploadTask = storageRef.putFile(uploadUri);

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                        imageUrl = downloadUrl.toString();
                        Log.d("FirebaseStorage", "Image URL: " + imageUrl);
                        listener.onImageUploaded(imageUrl);
                    }).addOnFailureListener(exception -> {
                        Log.e("FirebaseStorage", "Failed to get download URL: " + exception.getMessage());
                        listener.onUploadFailed(exception);
                    });
                }).addOnFailureListener(exception -> {
                    Log.e("FirebaseStorage", "Upload failed: " + exception.getMessage());
                    listener.onUploadFailed(exception);
                    // Add offline caching here
                });
            } else {
                Log.e("FirebaseStorage", "No image available for upload");
                listener.onUploadFailed(new Exception("No image available for upload"));
            }
        } catch (Exception e) {
            Log.e("FirebaseStorage", "Error during image processing", e);
            listener.onUploadFailed(e);
        }
    }

    /**
     * Rotates an image by a specified angle and saves the rotated version.
     *
     * @param context The application context.
     * @param imageUri URI of the original image.
     * @param angle Rotation angle in degrees.
     * @return URI of the rotated image.
     * @throws IOException If file operations fail.
     */
    private Uri rotateImage(Context context, Uri imageUri, float angle) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        File file = new File(context.getCacheDir(), "rotated_image.jpg");
        FileOutputStream fos = new FileOutputStream(file);
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 10, fos);
        fos.close();

        return Uri.fromFile(file);
    }


    /**
     * Animates the selection of a button by scaling it up and changing its background tint.
     *
     * @param button The button to animate.
     */
    private void animateButtonSelection(Button button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.1f);

        scaleX.setDuration(ANIMATION_DURATION);
        scaleY.setDuration(ANIMATION_DURATION);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();

        button.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_selected));
    }

    /**
     * Animates the deselection of a button by scaling it back to the normal size and changing its background tint.
     *
     * @param button The button to animate.
     */
    private void animateButtonDeselection(Button button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1.1f, 1f);

        scaleX.setDuration(ANIMATION_DURATION);
        scaleY.setDuration(ANIMATION_DURATION);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();

        button.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.button_normal));
    }

    /**
     * Updates the image preview with a new bitmap from a given URI.
     *
     * @param imageUri URI of the image to display.
     */
    private void updateImagePreview(Uri imageUri) {
        View rectangle2 = binding1.getRoot().findViewById(R.id.rectangle_2);
        rectangle2.setBackground(new BitmapDrawable(getResources(), getBitmapFromUri(photoUri)));
    }

    /**
     * Retrieves a Bitmap object from the provided URI.
     *
     * @param uri The URI of the image file to convert into a Bitmap.
     * @return The Bitmap representation of the image, or null if an error occurs during retrieval.
     */
    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Handles the upload of an image to Firebase Storage.
     * Uses a callback to notify when the upload is complete.
     *
     * @param imageUri URI of the image to upload.
     */
    private void handleImageUpload(Uri imageUri) {
        uploadImageToFirebase(imageUri, new OnImageUploadedListener() {
            @Override
            public void onImageUploaded(String imageUrl) {
                // Update photoUrl and UI
                photoUri = Uri.parse(imageUrl);
                updateImagePreview(photoUri);
            }

            @Override
            public void onUploadFailed(Exception e) {  // Changed from onError
                Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == PICK_IMAGE_REQUEST) {
                Uri imageUri = (requestCode == REQUEST_IMAGE_CAPTURE) ? photoUri : data.getData();
                handleImageUpload(imageUri);
            }
        }
    }


    /**
     * Refreshes the list of mood events from Firestore.
     */
    private void refreshMoodEventsList() {
    }
    /**
     * Updates the mood event in Firestore with new data.
     *
     * @param moodEvent The mood event to update.
     */
    private void updateMoodEvent(MoodEvent moodEvent) {
        globalStorage.updateMood(globalStorage.getCurrentMoodForEdit(),moodEvent);
        for (int i=0; i<globalStorage.getMHItem().size();i++){
            if (globalStorage.getMHItem().get(i).getDate().getTime()== moodEvent.getDate()){
                MoodHistoryItem temp  = new MoodHistoryItem(moodEvent.getMood(),getEmojiForMood(moodEvent.getMood()),moodEvent.getDescription(), new Date(moodEvent.getDate()));
                globalStorage.getMHItem().set(i,temp);
            }
        }
        refreshMoodEventsList();
        moodEventsRef = db.collection("mood_events");
        moodEventsRef.whereEqualTo("description", moodEventToEdit.getDescription()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            moodEventsRef.document(document.getId()).set(moodEvent)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("FIRESTORE", "Mood event updated successfully!");
                                        refreshMoodEventsList();
                                    })
                                    .addOnFailureListener(e -> Log.e("FIRESTORE", "Update failed", e));
                            refreshMoodEventsList();
                        }
                    } else {
                        Log.e("FIRESTORE", "Mood event not found in Firestore");
                    }
                });
    }




    /**
     * Deletes a mood event from the Firestore database.
     *
     * @param moodEvent The mood event to be deleted.
     */
    private void deleteMoodEvent(MoodEvent moodEvent) {
        moodEventsRef.whereEqualTo("date", moodEvent.getDate()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            moodEventsRef.document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("FIRESTORE", "Mood event deleted successfully");
                                        refreshMoodEventsList();
                                    })
                                    .addOnFailureListener(e -> Log.e("FIRESTORE", "Failed to delete mood event", e));
                        }
                    } else {
                        Log.e("FIRESTORE", "Mood event not found in Firestore");
                    }
                });
    }
    /**
     * Displays a success dialog UI for a mood event action and navigates to the mood history after a delay.
     */
    private void showSuccessDialogUI() {
        if (isAdded() && !isDetached() && !isRemoving()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog);

            View customView = getLayoutInflater().inflate(R.layout.custom_success_dialog, null);
            builder.setView(customView);

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                dialog.dismiss();
                navigateToMoodHistory();
            }, 2000);
        }
    }

    /**
     * Navigates to the Mood History fragment.
     */
    private void navigateToMoodHistory() {
        NavController navController = NavHostFragment.findNavController(this);
        if (navController.getCurrentDestination().getId() == R.id.editMoodActivityFragment ||
                navController.getCurrentDestination().getId() == R.id.editMoodActivityFragment2) {
            try {
                navController.navigate(R.id.action_editMoodActivityFragment2_to_SecondFragment);
            } catch (IllegalArgumentException e) {
                Log.e("Navigation", "Failed to navigate: " + e.getMessage());
                // Fallback navigation if needed
                navController.navigate(R.id.SecondFragment);
            }
        }
    }

    /**
     * Shows a toast message indicating an error during a save operation.
     *
     * @param e The exception that occurred during the save operation.
     */
    private void showErrorToast(Exception e) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Selects a mood, updates the corresponding UI elements, and provides feedback to the user.
     *
     * @param mood The selected mood.
     * @param selectedImageView The ImageView associated with the selected mood.
     */
    private void selectMood(String mood, ImageView selectedImageView) {
        if (lastSelectedImageView != null) {
            lastSelectedImageView.setColorFilter(null);
            animateImageDeselection(lastSelectedImageView);
        }

        selectedImageView.setColorFilter(createColorFilter());
        animateImageSelection(selectedImageView);
        lastSelectedImageView = selectedImageView;

        Spinner moodSpinner = binding1.mainCard;
        int position = getPositionForMood(mood);
        moodSpinner.setSelection(position);

        updateBackgroundColor(mood);

        Toast.makeText(getContext(), "Selected: " + mood, Toast.LENGTH_SHORT).show();
    }

    /**
     * Gets the position of the selected mood in the mood array.
     *
     * @param mood The mood whose position is to be found.
     * @return The position of the mood in the mood array.
     */
    private int getPositionForMood(String mood) {
        String[] moodArray = getResources().getStringArray(R.array.spinner_items);
        for (int i = 0; i < moodArray.length; i++) {
            if (moodArray[i].equals(mood)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Creates a color filter to adjust the image saturation and scale.
     *
     * @return The created color filter.
     */
    private ColorMatrixColorFilter createColorFilter() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        matrix.setScale(0.8f, 0.8f, 0.8f, 1.0f);
        return new ColorMatrixColorFilter(matrix);
    }

    /**
     * Animates the selection of an image (scaling effect).
     *
     * @param imageView The ImageView to be animated.
     */
    private void animateImageSelection(ImageView imageView) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.2f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.2f);

        scaleX.setDuration(ANIMATION_DURATION);
        scaleY.setDuration(ANIMATION_DURATION);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();
    }

    /**
     * Animates the deselection of an image (scaling effect).
     *
     * @param imageView The ImageView to be animated.
     */
    private void animateImageDeselection(ImageView imageView) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1.2f, 1f);

        scaleX.setDuration(ANIMATION_DURATION);
        scaleY.setDuration(ANIMATION_DURATION);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();
    }

    /**
     * Updates the background color of the main layout based on the selected mood.
     *
     * @param mood The selected mood.
     */
    private void updateBackgroundColor(String mood) {
        Integer gradientResId = moodGradients.get(mood);
        if (gradientResId != null) {
            mainLayout.setBackgroundResource(gradientResId);
        }
        else {
            mainLayout.setBackgroundColor(Color.parseColor("#203434")); // Default gradient
        }
        EditText editDescription = binding1.editDescription;
        Spinner spinnerStuff = binding1.mainCard;
        View rectangleViewOrSum = binding1.rectangle2;
        TextView textView = binding1.stepIndicator;
        Button button = binding1.next;
        Spinner spinner = binding1.musicSpinner;

        switch (mood) {
            case "None":
                editDescription.setBackgroundResource(R.drawable.edit_text_background);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_background);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_background);
                button.setBackgroundResource(R.drawable.edit_text_background);
                textView.setBackgroundResource(R.drawable.edit_text_background);
                textView.setTextColor(R.drawable.blackmamba);
                editDescription.setTextColor(Color.parseColor("#5A4A33"));
                break;
            case "Happy":
                editDescription.setBackgroundResource(R.drawable.edit_text_happy);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_happy);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_happy);
                button.setBackgroundResource(R.drawable.edit_text_happy);
                textView.setBackgroundResource(R.drawable.edit_text_happy);
                spinner.setBackgroundResource(R.drawable.edit_text_happy);
                editDescription.setTextColor(Color.parseColor("#5A4A33"));
                break;
            case "Sad":
                editDescription.setBackgroundResource(R.drawable.edit_text_sad);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_sad);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_sad);
                button.setBackgroundResource(R.drawable.edit_text_sad);
                textView.setBackgroundResource(R.drawable.edit_text_sad);
                spinner.setBackgroundResource(R.drawable.edit_text_happy);
                editDescription.setTextColor(Color.parseColor("#2C3E50"));
                break;
            case "Angry":
                editDescription.setBackgroundResource(R.drawable.edit_text_angry);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_angry);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_angry);
                button.setBackgroundResource(R.drawable.edit_text_angry);
                spinner.setBackgroundResource(R.drawable.edit_text_angry);
                textView.setBackgroundResource(R.drawable.edit_text_angry);
                editDescription.setTextColor(Color.parseColor("#4D1A1A"));
                break;
            case "Confused":
                editDescription.setBackgroundResource(R.drawable.edit_text_confused);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_confused);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_confused);
                button.setBackgroundResource(R.drawable.edit_text_confused);
                spinner.setBackgroundResource(R.drawable.edit_text_confused);
                textView.setBackgroundResource(R.drawable.edit_text_confused);
                editDescription.setTextColor(Color.parseColor("#3A2D58"));
                break;
            case "Surprised":
                editDescription.setBackgroundResource(R.drawable.edit_text_surprised);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_surprised);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_surprised);
                spinner.setBackgroundResource(R.drawable.edit_text_surprised);
                button.setBackgroundResource(R.drawable.edit_text_surprised);
                textView.setBackgroundResource(R.drawable.edit_text_surprised);
                editDescription.setTextColor(Color.parseColor("#5D2B3E"));
                break;
            case "Ashamed":
                editDescription.setBackgroundResource(R.drawable.edit_text_ashamed);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_ashamed);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_ashamed);
                spinner.setBackgroundResource(R.drawable.edit_text_ashamed);
                textView.setBackgroundResource(R.drawable.edit_text_ashamed);
                button.setBackgroundResource(R.drawable.edit_text_ashamed);

                editDescription.setTextColor(Color.parseColor("#5C3A21"));
                break;
            case "Scared":
                editDescription.setBackgroundResource(R.drawable.edit_text_scared);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_scared);
                spinner.setBackgroundResource(R.drawable.edit_text_scared);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_scared);
                textView.setBackgroundResource(R.drawable.edit_text_scared);
                button.setBackgroundResource(R.drawable.edit_text_scared);

                editDescription.setTextColor(Color.parseColor("#2B3F5D"));
                break;
            case "Disgusted":
                spinner.setBackgroundResource(R.drawable.edit_text_disgusted);
                editDescription.setBackgroundResource(R.drawable.edit_text_disgusted);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_disgusted);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_disgusted);
                textView.setBackgroundResource(R.drawable.edit_text_disgusted);
                button.setBackgroundResource(R.drawable.edit_text_disgusted);

                editDescription.setTextColor(Color.parseColor("#264D33"));
                break;

            default:
                // Default textbox background if no mood matches
                editDescription.setBackgroundResource(R.drawable.edit_text_default);
                rectangleViewOrSum.setBackgroundResource(R.drawable.edit_text_default);
                button.setBackgroundResource(R.drawable.edit_text_default);
                spinnerStuff.setBackgroundResource(R.drawable.edit_text_default);
                spinner.setBackgroundResource(R.drawable.edit_text_default);
                editDescription.setTextColor(Color.parseColor("#204343"));
        }
    }

    /**
     * Sets the color of an image view to the selected mood's color.
     *
     * @param imageView The ImageView to change the color of.
     */
    private void setImageColor(ImageView imageView) {
        imageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.selected_mood_color));
    }

    /**
     * Resets the color of the image view to its original color.
     *
     * @param imageView The ImageView to reset the color of.
     */
    private void resetImageColor(ImageView imageView) {
        imageView.clearColorFilter();
    }

    /**
     * Finds the position of a value in a spinner.
     *
     * @param spinner The spinner to search through.
     * @param value The value to find in the spinner.
     * @return The position of the value in the spinner, or -1 if not found.
     */
    private int getPositionOfValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Cleans up the view bindings when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding1 = null;
        binding2 = null;
    }
}