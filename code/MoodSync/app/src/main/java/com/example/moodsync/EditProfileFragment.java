package com.example.moodsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private ImageView profileImageEdit;
    private TextView nameTextView;
    private TextView usernameTextView;
    private TextView locationTextView;
    private TextView bioTextView;
    private ImageView backButton;
    private MaterialButton editProfileButton;
    private GridView photosListView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_profile_fragment, container, false);

        // Initialize views
        profileImageEdit = view.findViewById(R.id.profile_image_edit);
        nameTextView = view.findViewById(R.id.nameofuser);
        usernameTextView = view.findViewById(R.id.usernameofuser);
        locationTextView = view.findViewById(R.id.locationofuser);
        bioTextView = view.findViewById(R.id.bioofuser);
        backButton = view.findViewById(R.id.back_button);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        photosListView = view.findViewById(R.id.photos_listview);

        // Load dummy data
        loadDummyData();

        // Load photos in the ListView
        loadPhotosListView();

        // Set click listeners
        profileImageEdit.setOnClickListener(v -> changeProfileImage());

        editProfileButton.setOnClickListener(v -> saveProfile());

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void loadDummyData() {
        // Set default profile image from drawable
        profileImageEdit.setImageResource(R.drawable.arijitsingh);

        // Set dummy text data
        nameTextView.setText("Teri maa ka bhosada");
        usernameTextView.setText("@"+"johndoe");
        locationTextView.setText("New York, USA");
        bioTextView.setText("Photographer | Travel Enthusiast | Coffee Lover\nCapturing moments and sharing stories through my lens. Always on the lookout for the next adventure.");
    }

    private void loadPhotosListView() {
        // Create a list of sample photos using arijitsingh drawable
        List<Map<String, Object>> photosList = new ArrayList<>();

        // Add multiple entries to simulate a photo gallery
        for (int i = 0; i < 6; i++) {
            Map<String, Object> photo = new HashMap<>();
            photo.put("image", R.drawable.arijitsingh);
            photosList.add(photo);
        }

        // Create an adapter for the ListView
        SimpleAdapter adapter = new SimpleAdapter(
                requireContext(),
                photosList,
                R.layout.photo_item, // You'll need to create this layout with an ImageView
                new String[]{"image"},
                new int[]{R.id.photo_image} // Assuming your photo_item_layout has an ImageView with this ID
        );

        // Set the adapter to the ListView
        photosListView.setAdapter(adapter);
    }

    private void changeProfileImage() {
        // Just toggle between two drawable images for demonstration
        if (profileImageEdit.getTag() != null && (boolean) profileImageEdit.getTag()) {
            profileImageEdit.setImageResource(R.drawable.arijitsingh);
            profileImageEdit.setTag(false);
        } else {
            // Using the same drawable since we only have arijitsingh
            profileImageEdit.setImageResource(R.drawable.arijitsingh);
            profileImageEdit.setTag(true);
        }

        Toast.makeText(requireContext(), "Profile image updated", Toast.LENGTH_SHORT).show();

        // Refresh the photos list to show the change
        loadPhotosListView();
    }

    private void saveProfile() {
        // Get text from fields
        String name = nameTextView.getText().toString().trim();
        String username = usernameTextView.getText().toString().trim();
        String location = locationTextView.getText().toString().trim();
        String bio = bioTextView.getText().toString().trim();

        // Validate inputs (basic validation)
        if (name.isEmpty() || username.isEmpty()) {
            Toast.makeText(requireContext(), "Name and username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // In a real app, you would save this data to a database or preferences
        // For this dummy implementation, just show a success message
        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();

        // Navigate back
        requireActivity().onBackPressed();
    }
}
