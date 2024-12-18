package Android_dev.assignment_2.View.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import Android_dev.assignment_2.Model.Data.Enums.BloodType;
import Android_dev.assignment_2.R;
import Android_dev.assignment_2.Model.Data.Enums.UserRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText, phoneEditText;
    private Spinner bloodTypeSpinner, roleSpinner;
    private Button registerButton;
    private TextView loginLink;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Set up click listeners
        registerButton.setOnClickListener(v -> registerUser());
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        bloodTypeSpinner = findViewById(R.id.bloodTypeSpinner);
        roleSpinner = findViewById(R.id.roleSpinner);
        registerButton = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
        progressBar = findViewById(R.id.progressBar);

        // Setup blood type spinner
        ArrayAdapter<BloodType> bloodTypeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                BloodType.values()
        );
        bloodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodTypeSpinner.setAdapter(bloodTypeAdapter);

        // Setup role spinner with only DONOR and SITE_MANAGER options
        UserRole[] availableRoles = {UserRole.DONOR, UserRole.SITE_MANAGER};
        ArrayAdapter<UserRole> roleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                availableRoles
        );
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);
    }

    private void registerUser() {
        // Get values
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        BloodType bloodType = (BloodType) bloodTypeSpinner.getSelectedItem();
        UserRole selectedRole = (UserRole) roleSpinner.getSelectedItem();

        // Validation
        if (!validateInputs(name, email, password, confirmPassword, phone)) {
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        disableInputs(true);

        // Create user with Firebase Auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Send verification email
                        firebaseAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(emailTask -> {
                                    if (emailTask.isSuccessful()) {
                                        // Save additional user data to Firestore
                                        saveUserDataToFirestore(
                                                task.getResult().getUser().getUid(),
                                                name,
                                                email,
                                                phone,
                                                bloodType
                                        );
                                    } else {
                                        handleError("Failed to send verification email", emailTask.getException());
                                    }
                                });
                    } else {
                        handleError("Registration failed", task.getException());
                    }
                });
    }

    private void handleError(String message, Exception e) {
        progressBar.setVisibility(View.GONE);
        disableInputs(false);

        String errorMessage = message;
        if (e != null) {
            if (e.getMessage().contains("email already in use")) {
                errorMessage = "This email is already registered. Please use a different email or try logging in.";
            } else if (e.getMessage().contains("network")) {
                errorMessage = "Network error. Please check your internet connection and try again.";
            } else {
                errorMessage = message + ": " + e.getMessage();
            }
        }

        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void disableInputs(boolean disable) {
        nameEditText.setEnabled(!disable);
        emailEditText.setEnabled(!disable);
        passwordEditText.setEnabled(!disable);
        confirmPasswordEditText.setEnabled(!disable);
        phoneEditText.setEnabled(!disable);
        bloodTypeSpinner.setEnabled(!disable);
        roleSpinner.setEnabled(!disable);
        registerButton.setEnabled(!disable);
        loginLink.setEnabled(!disable);
    }

    private boolean validateInputs(String name, String email, String password,
                                   String confirmPassword, String phone) {
        boolean isValid = true;

        // Name validation
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            isValid = false;
        } else if (name.length() < 2) {
            nameEditText.setError("Name must be at least 2 characters");
            isValid = false;
        }

        // Email validation
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email address");
            isValid = false;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            isValid = false;
        } else if (!password.matches(".*[A-Z].*")) {
            passwordEditText.setError("Password must contain at least one uppercase letter");
            isValid = false;
        } else if (!password.matches(".*[a-z].*")) {
            passwordEditText.setError("Password must contain at least one lowercase letter");
            isValid = false;
        } else if (!password.matches(".*\\d.*")) {
            passwordEditText.setError("Password must contain at least one number");
            isValid = false;
        }

        // Confirm password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            isValid = false;
        }

        // Phone validation
        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Phone number is required");
            isValid = false;
        } else if (!phone.matches("^[0-9]{10,}$")) {
            phoneEditText.setError("Please enter a valid phone number (minimum 10 digits)");
            isValid = false;
        }

        return isValid;
    }

    private void saveUserDataToFirestore(String uid, String name, String email,
                                         String phone, BloodType bloodType) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("bloodType", bloodType.getDisplayName());  // Using the display name from enum

        // Get selected role from spinner
        UserRole selectedRole = (UserRole) roleSpinner.getSelectedItem();
        user.put("role", selectedRole.name());

        user.put("createdAt", System.currentTimeMillis());

        // Initialize empty lists for new user
        user.put("managedSiteIds", new ArrayList<String>());
        user.put("donationHistory", new ArrayList<>());

        firestore.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this,
                            "Registration successful. Please verify your email.",
                            Toast.LENGTH_LONG).show();
                    firebaseAuth.signOut();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this,
                            "Failed to save user data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}