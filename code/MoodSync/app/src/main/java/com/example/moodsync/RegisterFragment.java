package com.example.moodsync;

import android.os.Bundle;
import android.util.Log;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A fragment that handles user registration. This class provides a form for users to
 * input their full name, username, and password to create an account. It validates the
 * inputs, checks username availability in Firestore, and registers the user if all
 * conditions are met.
 *
 * <p>
 * The fragment also fetches existing user data from Firestore and stores it in a local
 * storage instance for further use. Upon successful registration, the user is navigated
 * to the next fragment.
 * </p>
 */
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

        LocalStorage globalStorage = LocalStorage.getInstance();
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
                                userData.put("bio", "");
                                userData.put("location", "");
                                userData.put("pfpUrl", "");
                                userData.put("followerList", new ArrayList<String>());
                                userData.put("followingList", new ArrayList<String>());
                                userData.put("commentList", new ArrayList<Integer>());

                                db.collection("users").document(username)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            ((MyApplication) getActivity().getApplication()).setLoggedInUsername(username);

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

        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User tempUser = new User();
                                tempUser.setId(document.getId());
                                tempUser.setName((String) document.get("fullName"));
                                tempUser.setPass((String) document.get("password"));
                                tempUser.setUsername((String) document.get("userName"));
                                tempUser.setPfpUrl((String) document.get("profileImageUrl"));
                                tempUser.setLocation((String) document.get("location"));
                                tempUser.setBio((String) document.get("bio"));
                                tempUser.setFollowerList((ArrayList<String>) document.get("followerList"));
                                tempUser.setFollowingList((ArrayList<String>) document.get("followingList"));
                                tempUser.setCommentList((ArrayList<Integer>) document.get("commentList"));

                                // If doesn't exist, then add
                                if (!globalStorage.checkIfUserExists(tempUser)){
                                    globalStorage.getUserList().add(tempUser);
                                }
                            }
                        } else {
                            Log.d("User Data", "Error fetching users: " + task.getException().getMessage());
                        }
                    }
                });

        return view;
    }}
