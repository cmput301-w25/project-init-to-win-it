package com.example.moodsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.moodsync.databinding.GetStartedFragmentBinding;

/**
 * FirstFragment serves as the starting screen with animated UI elements and a navigation button.
 */
public class FirstFragment extends Fragment {

    private GetStartedFragmentBinding binding;

    /**
     * Inflates the fragment layout and initializes view binding.
     *
     * @param inflater The LayoutInflater object that can inflate views in the fragment.
     * @param container The parent view that the fragment's UI is attached to.
     * @param savedInstanceState Previous saved state data.
     * @return The root view of the fragment's layout.
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = GetStartedFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called immediately after the view is created. Applies animations and sets up click listeners.
     *
     * @param view The created view.
     * @param savedInstanceState Previous saved state data.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyAnimations();

        // btn click anims for transition to next frag
        binding.button.setOnClickListener(v -> v.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    if (binding == null) return;
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                if (!isAdded()) return;
                                NavHostFragment.findNavController(FirstFragment.this)
                                        .navigate(R.id.action_FirstFragment_to_RegisterFragment);
                            });
                }));
    }

    /**
     * Applies fade-in and movement animations to the screen's UI elements.
     */
    private void applyAnimations() {
        if (!isAdded() || getContext() == null || binding == null) {
            return;
        }

        // intializing the screen. there was this nice video on animations in java
        binding.centeredImage.setAlpha(0f);
        binding.centeredImage.setScaleX(0.8f);
        binding.centeredImage.setScaleY(0.8f);

        binding.welcomeTo.setAlpha(0f);
        binding.welcomeTo.setTranslationY(50f);

        binding.shareYour.setAlpha(0f);
        binding.shareYour.setTranslationY(50f);

        binding.button.setAlpha(0f);
        binding.button.setTranslationY(50f);

        // its a full sequence with animations
        binding.centeredImage.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .withEndAction(() -> {
                    if (!isAdded() || getContext() == null || binding == null) {
                        return;
                    }

                    //some floating animation i found on stack overflow
                    startFloatingAnimation();
                });

        // starting anim
        binding.welcomeTo.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(800)
                .setStartDelay(400);

        // share anim
        binding.shareYour.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(800)
                .setStartDelay(600);

        // button anim
        binding.button.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(800)
                .setStartDelay(800);
    }

    /**
     * Starts a looping floating animation for the centered image.
     */
    private void startFloatingAnimation() {
        if (!isAdded() || getContext() == null || binding == null) {
            return;
        }

        binding.centeredImage.animate()
                .translationYBy(15f)
                .setDuration(2000)
                .setInterpolator(AnimationUtils.loadInterpolator(
                        getContext(), android.R.interpolator.linear_out_slow_in))
                .withEndAction(() -> {
                    if (!isAdded() || getContext() == null || binding == null) {
                        return;
                    }
                    binding.centeredImage.animate()
                            .translationYBy(-15f)
                            .setDuration(2000)
                            .setInterpolator(AnimationUtils.loadInterpolator(
                                    getContext(), android.R.interpolator.fast_out_slow_in))
                            .withEndAction(this::startFloatingAnimation);
                });
    }

    /**
     * Cancels any ongoing animations when the fragment is paused.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (binding != null) {
            if (binding.centeredImage != null) binding.centeredImage.clearAnimation();
            if (binding.welcomeTo != null) binding.welcomeTo.clearAnimation();
            if (binding.shareYour != null) binding.shareYour.clearAnimation();
            if (binding.button != null) binding.button.clearAnimation();
        }
    }

    /**
     * Cancels and clears all animations and releases view binding references when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            if (binding.centeredImage != null) {
                binding.centeredImage.animate().cancel();
                binding.centeredImage.clearAnimation();
            }
            if (binding.welcomeTo != null) {
                binding.welcomeTo.animate().cancel();
                binding.welcomeTo.clearAnimation();
            }
            if (binding.shareYour != null) {
                binding.shareYour.animate().cancel();
                binding.shareYour.clearAnimation();
            }
            if (binding.button != null) {
                binding.button.animate().cancel();
                binding.button.clearAnimation();
            }
        }
        binding = null;
    }
}