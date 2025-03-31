package com.example.moodsync;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;
import android.os.Looper;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.moodsync.databinding.GetStartedFragmentBinding;
/**
 * FirstFragment serves as the introductory screen of the application.
 * It features animated UI elements such as images and text, creating a welcoming experience for users.
 * After completing the animations, it navigates to the LoginFragment.
 *
 * <p>Key Features:</p>
 * <ul>
 *     <li>Animated image and text elements.</li>
 *     <li>Custom navigation with transition animations.</li>
 *     <li>Safety checks to ensure proper fragment lifecycle handling.</li>
 * </ul>
 */
public class FirstFragment extends Fragment {

    private GetStartedFragmentBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int INTRO_ANIM_DURATION = 3000; // 3 seconds

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = GetStartedFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupInitialState();
        startIntroAnimation();
    }

    /**
     * Sets up the initial state of UI elements by hiding them and preparing them for animations.
     */
    private void setupInitialState() {
        if (!isSafe()) return;

        // Hide all elements initially
        binding.centeredImage.setAlpha(0f);
        binding.welcomeTo.setAlpha(0f);
        binding.shareYour.setAlpha(0f);
    }

    /**
     * Starts an introductory animation sequence for UI elements, including scaling, rotation, and fading effects.
     * After completing animations, navigates to the LoginFragment with custom transitions.
     */
    private void startIntroAnimation() {
        if (!isSafe()) return;

        // Initial state - fully transparent and slightly smaller
        binding.centeredImage.setAlpha(0f);
        binding.centeredImage.setScaleX(0.8f);
        binding.centeredImage.setScaleY(0.8f);
        binding.centeredImage.setRotation(-5f); // Slight initial tilt for dynamism

        // 3-stage welcoming animation sequence
        binding.centeredImage.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0f) // Corrects the initial tilt
                .setDuration(1200) // Initial pop-in duration
                .setInterpolator(AnimationUtils.loadInterpolator(
                        requireContext(), android.R.interpolator.bounce))
                .withEndAction(() -> {
                    if (!isSafe()) return;

                    // Secondary "breathing" effect
                    binding.centeredImage.animate()
                            .scaleX(1.05f)
                            .scaleY(1.05f)
                            .setDuration(600)
                            .setInterpolator(AnimationUtils.loadInterpolator(
                                    requireContext(), android.R.interpolator.linear_out_slow_in))
                            .withEndAction(() -> {
                                if (!isSafe()) return;

                                // Final settle animation
                                binding.centeredImage.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(600)
                                        .setInterpolator(AnimationUtils.loadInterpolator(
                                                requireContext(), android.R.interpolator.fast_out_slow_in))
                                        .withEndAction(() -> {
                                            if (!isSafe()) return;

                                            animateWelcomeGlow();
                                            animateWelcomeText();

                                            // Navigate after full sequence
                                            handler.postDelayed(() -> {
                                                if (isSafe()) {
                                                    NavHostFragment.findNavController(FirstFragment.this)
                                                            .navigate(R.id.action_FirstFragment_to_LoginFragment,
                                                                    null,
                                                                    new NavOptions.Builder()
                                                                            .setEnterAnim(R.anim.slide_in_right)
                                                                            .setExitAnim(R.anim.slide_out_left)
                                                                            .setPopEnterAnim(R.anim.slide_in_left)
                                                                            .setPopExitAnim(R.anim.slide_out_right)
                                                                            .build());
                                                }
                                            }, 1500);
                                        });
                            });
                });
    }

    /**
     * Adds a subtle pulsing glow effect to the centered image using an ObjectAnimator.
     */
    private void animateWelcomeGlow() {
        // Add subtle pulsing glow effect
        ObjectAnimator glowAnim = ObjectAnimator.ofFloat(binding.centeredImage,
                "alpha", 1f, 0.9f, 1f);
        glowAnim.setDuration(2000);
        glowAnim.setRepeatCount(ValueAnimator.INFINITE);
        glowAnim.setRepeatMode(ValueAnimator.REVERSE);
        glowAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        glowAnim.start();
    }

    /**
     * Animates welcome text elements with fade-in, scale-up, and translation effects for a dynamic entrance.
     */
    private void animateWelcomeText() {
        // Fade in welcome text with staggered entrance
        binding.welcomeTo.setAlpha(0f);
        binding.shareYour.setAlpha(0f);

        binding.welcomeTo.setScaleX(0.9f);
        binding.shareYour.setScaleX(0.9f);

        binding.welcomeTo.setScaleY(0.9f);
        binding.shareYour.setScaleY(0.9f);

        binding.welcomeTo.setTranslationY(20f);
        binding.shareYour.setTranslationY(20f);


        binding.welcomeTo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .setStartDelay(300);

        binding.shareYour.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .setStartDelay(300);
    }


    /**
     * Checks if the fragment is in a safe state (added to activity, context is non-null, and binding is initialized).
     *
     * @return True if the fragment is in a safe state; false otherwise.
     */
    private boolean isSafe() {
        return isAdded() && getContext() != null && binding != null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding != null) {
            binding.centeredImage.clearAnimation();
            binding.welcomeTo.clearAnimation();
            binding.shareYour.clearAnimation();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.centeredImage.animate().cancel();
            binding.welcomeTo.animate().cancel();
            binding.shareYour.animate().cancel();
        }
        binding = null;
    }
}