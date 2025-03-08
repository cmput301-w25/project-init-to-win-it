/**
 * FirstFragment class for the MoodSync application.
 *
 * This fragment represents the initial screen of the application and provides a button
 * to navigate to the second fragment.
 */
package com.example.moodsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.moodsync.databinding.GetStartedFragmentBinding;

/**
 * FirstFragment is the starting fragment of the application.
 * It displays a button that navigates to the SecondFragment when clicked.
 */
public class FirstFragment extends Fragment {

    /**
     * Binding object for accessing views in the get_started_fragment.xml layout file.
     */
    private GetStartedFragmentBinding binding;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout using ViewBinding
        binding = GetStartedFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set click listener for the button to navigate to SecondFragment
        binding.button.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
