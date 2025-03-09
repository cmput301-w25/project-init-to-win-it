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

public class FirstFragment extends Fragment {

    private GetStartedFragmentBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = GetStartedFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyAnimations();
        binding.button.setOnClickListener(v -> {v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction(() -> {
                        if (binding == null) return;
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .withEndAction(() -> {
                                    if (!isAdded()) return;
                                    NavHostFragment.findNavController(FirstFragment.this)
                                            .navigate(R.id.action_FirstFragment_to_SecondFragment);});});});}

    private void applyAnimations() {
        // Add a check to make sure the fragment is still attached
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

                    //some floating animation i found of stack overflow
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

    @Override
    public void onPause() {
        super.onPause();
        //this cancels any anims when the frag is paused
        if (binding != null) {
            if (binding.centeredImage != null) binding.centeredImage.clearAnimation();
            if (binding.welcomeTo != null) binding.welcomeTo.clearAnimation();
            if (binding.shareYour != null) binding.shareYour.clearAnimation();
            if (binding.button != null) binding.button.clearAnimation();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // this thing is necessary cuz without this the app literally crashed
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