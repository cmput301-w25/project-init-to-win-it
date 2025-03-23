package com.example.moodsync;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {
    private TextView goToLogin;

    private TextInputEditText fullnameInput, usernameInput, passwordInput, passwordInput2;
    private CheckBox promiseCheckbox;
    private MaterialButton signupButton;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register, container, false);

        db = FirebaseFirestore.getInstance();

        fullnameInput = view.findViewById(R.id.fullnameInput);
        usernameInput = view.findViewById(R.id.usernameInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        passwordInput2 = view.findViewById(R.id.passwordInput2);
        signupButton = view.findViewById(R.id.signupButton);
        goToLogin = view.findViewById(R.id.go_to_login);

        goToLogin.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_RegisterFragment_to_LoginFragment);
        });


        signupButton.setOnClickListener(v -> {
            String fullname = fullnameInput.getText().toString().trim();
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String password2 = passwordInput2.getText().toString().trim();

            if (fullname.isEmpty()) {
                Toast.makeText(getContext(), "Fullname is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username.isEmpty()) {
                Toast.makeText(getContext(), "Username is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(getContext(), "Password is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(password2)) {
                Toast.makeText(getContext(), "Passwords do not match. Please try again", Toast.LENGTH_SHORT).show();
                return;
            }


            db.collection("users").document(username)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Toast.makeText(getContext(), "This username is taken. Try another one", Toast.LENGTH_SHORT).show();
                            } else {
                                // If username is available, proceed with registration
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("fullName", fullname);
                                userData.put("userName", username);
                                userData.put("password", password);
                                userData.put("followerList", new ArrayList<String>());
                                userData.put("followingList", new ArrayList<String>());
                                userData.put("commentList", new ArrayList<Integer>());

                                db.collection("users").document(username)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            ((MyApplication) getActivity().getApplication()).setLoggedInUsername(username);
                                            Toast.makeText(getContext(), "User registered successfully!", Toast.LENGTH_SHORT).show();

                                            // Navigate to SecondFragment after successful registration
                                            NavController navController = Navigation.findNavController(view);
                                            navController.navigate(R.id.action_RegisterFragment_to_SecondFragment);
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Error registering user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        } else {
                            Toast.makeText(getContext(), "Error checking username: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        return view;
    }}
