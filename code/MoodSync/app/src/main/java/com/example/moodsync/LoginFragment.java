package com.example.moodsync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moodsync.RegisterFragment.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {
    private TextView goToRegister;

    private TextInputEditText usernameInput, passwordInput;
    private MaterialButton loginButton;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);

        db = FirebaseFirestore.getInstance();

        usernameInput = view.findViewById(R.id.usernameLogin);
        passwordInput = view.findViewById(R.id.passwordLogin);
        loginButton = view.findViewById(R.id.loginButton);
        goToRegister = view.findViewById(R.id.go_to_signup);

        goToRegister.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_LoginFragment_to_RegisterFragment);
        });

        loginButton.setOnClickListener(v -> login());

        return view;
    }

    private void login() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Username and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        boolean userFound = false;

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Map<String, Object> userData = document.getData();
                            String storedUsername = (String) userData.get("userName");
                            String storedPassword = (String) userData.get("password");

                            if (storedUsername.equals(username) && storedPassword.equals(password)) {
                                userFound = true;
                                break;
                            }
                        }

                        if (userFound) {
                            Toast.makeText(getContext(), "Login successful", Toast.LENGTH_SHORT).show();
                            // Navigate to SecondFragment
                            NavHostFragment.findNavController(LoginFragment.this)
                                    .navigate(R.id.action_LoginFragment_to_SecondFragment);
                        } else {
                            Toast.makeText(getContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error fetching users: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
