package com.example.moodsync;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

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

import com.example.moodsync.databinding.FragmentMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap mMap;
    Button mapfilterButton;
    Button mapfilterClear;
    Spinner mapfilterSpinner;
    ArrayAdapter<String> mapfilterSpinnerAdapter;
    List<String> mapfilterSpinnerData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);






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
                    .navigate(R.id.action_mapFragment_to_mapsActivity,
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
        mapfilterSpinner.setEnabled(false);
        mapfilterClear.setEnabled(false);

        //Adding data to Spinner
        mapfilterSpinnerData = Arrays.asList("Choose Option", "Filtered Following List", "Filtered Mood History", "Most Recent Mood");
        mapfilterSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, mapfilterSpinnerData);
        mapfilterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mapfilterSpinner.setAdapter(mapfilterSpinnerAdapter);

        //Set up Filter Data
        mapfilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Saving original data
                /*
                if (maporiginalMoodHistoryItems.size() == 0){
                    //Only saves original data once
                    saveOriginalMoodHistory();
                }*/
                //Managing visibility of spinner
                if (mapfilterSpinner.getVisibility() == View.VISIBLE) {
                    mapfilterSpinner.setVisibility(View.INVISIBLE);
                    mapfilterSpinner.setEnabled(false);
                    mapfilterClear.setVisibility(View.INVISIBLE);
                    mapfilterClear.setEnabled(false);
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
                    //Add toast

                } else if (selectedFilter.equals("Filtered Mood History")) {
                    //Add toast

                } else if (selectedFilter.equals("Most Recent Mood")) {
                    //Add toast
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing if nothing is selected
            }
        });

        //Clear Button Functionality (To restore filters)
        //saveOriginalMoodHistory();
        mapfilterClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                if (originalMoodHistoryItems.size() == 0){
                    return;
                }*/
                mapfilterSpinner.setSelection(0);
                //Restoring Original Data
                /*
                moodHistoryItems.clear();
                moodHistoryItems.addAll(originalMoodHistoryItems);
                moodHistoryAdapter.notifyDataSetChanged();
                 */
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
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

    private Bitmap editCustomMarker(Bitmap imageBitmap, String emoji, String user_id) {
        View markerView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_marker, null);

        ImageView img = markerView.findViewById(R.id.marker_image);
        TextView emj= markerView.findViewById(R.id.marker_emoji);
        TextView usrid = markerView.findViewById(R.id.marker_id);

        img.setImageBitmap(imageBitmap);
        emj.setText(emoji);
        usrid.setText(user_id);

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);

        return bitmap;
    }
}
