package com.example.moodsync;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SpotifySearchFragment extends Fragment implements TrackAdapter.OnTrackSelectedListener {
    private static final String TAG = "SpotifySearchFragment";
    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "moodsync://auth";
    private static final String CLIENT_ID = "c301faf5744f44cd8a7deca54b1a2b5c";

    private EditText searchInput;
    private Button searchButton;
    private Button loginButton;
    private RecyclerView tracksRecyclerView;
    private TrackAdapter trackAdapter;
    private String authToken;
    private OkHttpClient httpClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spotify_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        searchInput = view.findViewById(R.id.search_input);
        searchButton = view.findViewById(R.id.search_button);
        loginButton = view.findViewById(R.id.spotify_login_button);
        tracksRecyclerView = view.findViewById(R.id.tracks_recycler_view);

        // Initialize HTTP client
        httpClient = new OkHttpClient();

        // Set up RecyclerView
        trackAdapter = new TrackAdapter(new ArrayList<>(), this);
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tracksRecyclerView.setAdapter(trackAdapter);

        // Initially disable search until logged in
        searchButton.setEnabled(false);

        // Set up click listeners
        loginButton.setOnClickListener(v -> authenticateSpotify());
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                searchTracks(query);
            } else {
                Toast.makeText(getContext(), "Please enter a search term", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void authenticateSpotify() {
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"user-read-private", "user-library-modify"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(requireActivity(), REQUEST_CODE, request);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                case TOKEN:
                    authToken = response.getAccessToken();
                    loginButton.setText("Logged In");
                    searchButton.setEnabled(true);
                    Toast.makeText(getContext(), "Successfully connected to Spotify", Toast.LENGTH_SHORT).show();
                    break;

                case ERROR:
                    Toast.makeText(getContext(), "Error: " + response.getError(), Toast.LENGTH_SHORT).show();
                    break;

                default:
                    Toast.makeText(getContext(), "Auth flow returned: " + response.getType(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchTracks(String query) {
        if (authToken == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://api.spotify.com/v1/search?q=" + query + "&type=track&limit=20";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Gson gson = new Gson();
                    SpotifySearchResponse searchResponse = gson.fromJson(responseData, SpotifySearchResponse.class);

                    requireActivity().runOnUiThread(() -> {
                        trackAdapter.updateTracks(searchResponse.tracks.items);
                    });
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Search failed: " + response.message(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    @Override
    public void onTrackSelected(Track track) {
        // This is where you would add the track to a post
        // For example, save the track to a global variable or pass it back to the post creation activity
        Toast.makeText(getContext(), "Added \"" + track.name + "\" to your post", Toast.LENGTH_SHORT).show();

        // Example: Pass track back to parent activity/fragment
        Bundle result = new Bundle();
        result.putString("track_id", track.id);
        result.putString("track_name", track.name);
        result.putString("track_artist", track.artists.get(0).name);
        result.putString("track_image_url", track.album.images.get(0).url);
        getParentFragmentManager().setFragmentResult("selected_track", result);

        // Close this fragment or navigate back
        getParentFragmentManager().popBackStack();
    }

    // Model classes for Spotify API responses
    public static class SpotifySearchResponse {
        public Tracks tracks;
    }

    public static class Tracks {
        public List<Track> items;
    }

    public static class Track {
        public String id;
        public String name;
        public List<Artist> artists;
        public Album album;
    }

    public static class Artist {
        public String id;
        public String name;
    }

    public static class Album {
        public String id;
        public String name;
        public List<Image> images;
    }

    public static class Image {
        public String url;
        public int width;
        public int height;
    }
}
