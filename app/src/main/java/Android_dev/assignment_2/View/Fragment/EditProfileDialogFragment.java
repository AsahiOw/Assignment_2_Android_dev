package Android_dev.assignment_2.View.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import Android_dev.assignment_2.Model.Data.Enums.BloodType;
import Android_dev.assignment_2.R;
import Android_dev.assignment_2.Model.Data.Entities.User;

public class EditProfileDialogFragment extends BottomSheetDialogFragment {
    private EditText nameEditText;
    private EditText phoneEditText;
    private Spinner bloodTypeSpinner;
    private Button saveButton;
    private Button cancelButton;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile_dialog, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        initializeViews(view);
        setupSpinner();
        loadCurrentUserData();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        nameEditText = view.findViewById(R.id.nameEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        bloodTypeSpinner = view.findViewById(R.id.bloodTypeSpinner);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupSpinner() {
        ArrayAdapter<BloodType> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                BloodType.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodTypeSpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> validateAndSave());
        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void loadCurrentUserData() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        progressBar.setVisibility(View.VISIBLE);

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        populateFields();
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading user data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    dismiss();
                });
    }

    private void populateFields() {
        nameEditText.setText(currentUser.getFullName());
        phoneEditText.setText(currentUser.getPhoneNumber());

        // Set blood type spinner selection
        BloodType[] bloodTypes = BloodType.values();
        for (int i = 0; i < bloodTypes.length; i++) {
            if (bloodTypes[i] == currentUser.getBloodType()) {
                bloodTypeSpinner.setSelection(i);
                break;
            }
        }
    }

    private void validateAndSave() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        BloodType selectedBloodType = (BloodType) bloodTypeSpinner.getSelectedItem();

        // Validation
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Phone number is required");
            return;
        }

        if (!phone.matches("^[0-9]{10,}$")) {
            phoneEditText.setError("Please enter a valid phone number (minimum 10 digits)");
            return;
        }

        // Update user object
        currentUser.setFullName(name);
        currentUser.setPhoneNumber(phone);
        currentUser.setBloodType(selectedBloodType);

        saveUserData();
    }

    private void saveUserData() {
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        firestore.collection("users")
                .document(firebaseAuth.getCurrentUser().getUid())
                .set(currentUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile updated successfully",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error updating profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                });
    }
}