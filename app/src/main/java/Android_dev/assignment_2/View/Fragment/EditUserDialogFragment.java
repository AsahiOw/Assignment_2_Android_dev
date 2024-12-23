package Android_dev.assignment_2.View.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import Android_dev.assignment_2.Model.Data.Entities.User;
import Android_dev.assignment_2.Model.Data.Enums.BloodType;
import Android_dev.assignment_2.Model.Data.Enums.UserRole;
import Android_dev.assignment_2.R;

public class EditUserDialogFragment extends BottomSheetDialogFragment {
    private static final String ARG_USER_ID = "userId";
    private static final String ARG_USER_NAME = "userName";
    private static final String ARG_USER_PHONE = "userPhone";
    private static final String ARG_USER_BLOOD_TYPE = "userBloodType";
    private static final String ARG_USER_ROLE = "userRole";

    private TextInputEditText nameEditText;
    private TextInputEditText phoneEditText;
    private Spinner bloodTypeSpinner;
    private Spinner roleSpinner;
    private MaterialButton saveButton;
    private MaterialButton cancelButton;

    private FirebaseFirestore firestore;
    private String userId;
    private String userName;
    private String userPhone;
    private BloodType userBloodType;
    private UserRole userRole;

    public static EditUserDialogFragment newInstance(User user) {
        EditUserDialogFragment fragment = new EditUserDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, user.getId());
        args.putString(ARG_USER_NAME, user.getFullName());
        args.putString(ARG_USER_PHONE, user.getPhoneNumber());
        args.putString(ARG_USER_BLOOD_TYPE, user.getBloodType().name());
        args.putString(ARG_USER_ROLE, user.getRole().name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            userName = getArguments().getString(ARG_USER_NAME);
            userPhone = getArguments().getString(ARG_USER_PHONE);
            userBloodType = BloodType.valueOf(getArguments().getString(ARG_USER_BLOOD_TYPE));
            userRole = UserRole.valueOf(getArguments().getString(ARG_USER_ROLE));
        }
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_user_dialog, container, false);

        initializeViews(view);
        setupSpinners();
        populateFields();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        nameEditText = view.findViewById(R.id.nameEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        bloodTypeSpinner = view.findViewById(R.id.bloodTypeSpinner);
        roleSpinner = view.findViewById(R.id.roleSpinner);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);
    }

    private void setupSpinners() {
        // Blood Type Spinner
        ArrayAdapter<BloodType> bloodTypeAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                BloodType.values());
        bloodTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodTypeSpinner.setAdapter(bloodTypeAdapter);

        // Role Spinner
        UserRole[] availableRoles = {UserRole.DONOR, UserRole.SITE_MANAGER};
        ArrayAdapter<UserRole> roleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                availableRoles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);
    }

    private void populateFields() {
        nameEditText.setText(userName);
        phoneEditText.setText(userPhone);

        // Set blood type selection
        for (int i = 0; i < bloodTypeSpinner.getAdapter().getCount(); i++) {
            BloodType bloodType = (BloodType) bloodTypeSpinner.getAdapter().getItem(i);
            if (bloodType == userBloodType) {
                bloodTypeSpinner.setSelection(i);
                break;
            }
        }

        // Set role selection
        for (int i = 0; i < roleSpinner.getAdapter().getCount(); i++) {
            UserRole role = (UserRole) roleSpinner.getAdapter().getItem(i);
            if (role == userRole) {
                roleSpinner.setSelection(i);
                break;
            }
        }
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> validateAndSave());
        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void validateAndSave() {
        // Validate name
        String name = nameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            return;
        }

        // Validate phone
        String phone = phoneEditText.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            phoneEditText.setError("Phone number is required");
            return;
        }
        if (!phone.matches("^[0-9]{10,}$")) {
            phoneEditText.setError("Please enter a valid phone number (minimum 10 digits)");
            return;
        }

        saveButton.setEnabled(false);

        // Get selected values
        BloodType selectedBloodType = (BloodType) bloodTypeSpinner.getSelectedItem();
        UserRole selectedRole = (UserRole) roleSpinner.getSelectedItem();
        boolean roleChanged = selectedRole != userRole;

        // Prepare update data
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", name);
        updates.put("phoneNumber", phone);
        updates.put("bloodType", selectedBloodType.name());
        updates.put("role", selectedRole.name());

        firestore.collection("users")
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "User updated successfully",
                            Toast.LENGTH_SHORT).show();

                    // If role changed to site manager, clear any existing managed sites
                    if (roleChanged && selectedRole == UserRole.SITE_MANAGER) {
                        clearManagedSites();
                    }

                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Error updating user: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                });
    }

    private void clearManagedSites() {
        firestore.collection("users")
                .document(userId)
                .update("managedSiteIds", Arrays.asList())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Error clearing managed sites: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}