package Android_dev.assignment_2.View.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import Android_dev.assignment_2.Model.Data.Entities.DonationSite;
import Android_dev.assignment_2.Model.Data.Entities.User;
import Android_dev.assignment_2.Model.Data.Enums.BloodType;
import Android_dev.assignment_2.R;

public class NewSiteDialogFragment extends BottomSheetDialogFragment {
    private static final String ARG_SITE_MANAGERS = "siteManagers";

    private TextInputEditText nameEditText;
    private TextInputEditText addressEditText;
    private TextInputEditText latitudeEditText;
    private TextInputEditText longitudeEditText;
    private TextInputEditText contactPhoneEditText;
    private TextInputEditText descriptionEditText;
    private MultiAutoCompleteTextView bloodTypesTextView;
    private Spinner managerSpinner;
    private MaterialButton saveButton;
    private MaterialButton cancelButton;

    private FirebaseFirestore firestore;
    private List<User> siteManagers;

    public static NewSiteDialogFragment newInstance(List<User> siteManagers) {
        NewSiteDialogFragment fragment = new NewSiteDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_SITE_MANAGERS, new ArrayList<>(siteManagers));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            siteManagers = getArguments().getParcelableArrayList(ARG_SITE_MANAGERS);
        }
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_site_dialog, container, false);

        initializeViews(view);
        setupBloodTypeSelector();
        setupManagerSpinner();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        nameEditText = view.findViewById(R.id.nameEditText);
        addressEditText = view.findViewById(R.id.addressEditText);
        latitudeEditText = view.findViewById(R.id.latitudeEditText);
        longitudeEditText = view.findViewById(R.id.longitudeEditText);
        contactPhoneEditText = view.findViewById(R.id.contactPhoneEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        bloodTypesTextView = view.findViewById(R.id.bloodTypesTextView);
        managerSpinner = view.findViewById(R.id.managerSpinner);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);
    }

    private void setupBloodTypeSelector() {
        String[] bloodTypes = Arrays.stream(BloodType.values())
                .map(BloodType::getDisplayName)
                .toArray(String[]::new);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                bloodTypes);

        bloodTypesTextView.setAdapter(adapter);
        bloodTypesTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }

    private void setupManagerSpinner() {
        ArrayAdapter<User> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                siteManagers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        managerSpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> validateAndSave());
        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void validateAndSave() {
        if (!validateInputs()) {
            return;
        }

        saveButton.setEnabled(false);

        String siteId = UUID.randomUUID().toString();
        User selectedManager = (User) managerSpinner.getSelectedItem();

        DonationSite newSite = new DonationSite(
                siteId,
                nameEditText.getText().toString().trim(),
                addressEditText.getText().toString().trim(),
                new LatLng(
                        Double.parseDouble(latitudeEditText.getText().toString()),
                        Double.parseDouble(longitudeEditText.getText().toString())
                ),
                selectedManager.getId(),
                getSelectedBloodTypes(),
                new ArrayList<>(),
                contactPhoneEditText.getText().toString().trim(),
                descriptionEditText.getText().toString().trim(),
                true,
                new Date(),
                new Date()
        );

        firestore.collection("donationSites")
                .document(siteId)
                .set(newSite)
                .addOnSuccessListener(aVoid -> {
                    updateManagerSites(selectedManager, siteId);
                    Toast.makeText(getContext(), "Site created successfully",
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Error creating site: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate name
        if (TextUtils.isEmpty(nameEditText.getText())) {
            nameEditText.setError("Name is required");
            isValid = false;
        }

        // Validate address
        if (TextUtils.isEmpty(addressEditText.getText())) {
            addressEditText.setError("Address is required");
            isValid = false;
        }

        // Validate latitude
        try {
            double lat = Double.parseDouble(latitudeEditText.getText().toString());
            if (lat < -90 || lat > 90) {
                latitudeEditText.setError("Invalid latitude");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            latitudeEditText.setError("Invalid latitude");
            isValid = false;
        }

        // Validate longitude
        try {
            double lng = Double.parseDouble(longitudeEditText.getText().toString());
            if (lng < -180 || lng > 180) {
                longitudeEditText.setError("Invalid longitude");
                isValid = false;
            }
        } catch (NumberFormatException e) {
            longitudeEditText.setError("Invalid longitude");
            isValid = false;
        }

        // Validate contact phone
        String phone = contactPhoneEditText.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            contactPhoneEditText.setError("Contact phone is required");
            isValid = false;
        } else if (!phone.matches("^[0-9]{10,}$")) {
            contactPhoneEditText.setError("Invalid phone number");
            isValid = false;
        }

        // Validate blood types
        if (TextUtils.isEmpty(bloodTypesTextView.getText())) {
            bloodTypesTextView.setError("At least one blood type is required");
            isValid = false;
        }

        return isValid;
    }

    private List<String> getSelectedBloodTypes() {
        return Arrays.asList(bloodTypesTextView.getText().toString().split("\\s*,\\s*"));
    }

    private void updateManagerSites(User manager, String newSiteId) {
        List<String> managedSiteIds = new ArrayList<>(manager.getManagedSiteIds());
        managedSiteIds.add(newSiteId);

        firestore.collection("users")
                .document(manager.getId())
                .update("managedSiteIds", managedSiteIds)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Error updating manager sites: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}