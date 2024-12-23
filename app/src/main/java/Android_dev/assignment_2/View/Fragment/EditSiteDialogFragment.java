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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Android_dev.assignment_2.Model.Data.Entities.DonationSite;
import Android_dev.assignment_2.Model.Data.Entities.User;
import Android_dev.assignment_2.Model.Data.Enums.BloodType;
import Android_dev.assignment_2.R;

public class EditSiteDialogFragment extends BottomSheetDialogFragment {
    private static final String ARG_SITE = "site";
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
    private DonationSite site;
    private List<User> siteManagers;

    public static EditSiteDialogFragment newInstance(DonationSite site, List<User> siteManagers) {
        EditSiteDialogFragment fragment = new EditSiteDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_SITE, site);
        args.putParcelableArrayList(ARG_SITE_MANAGERS, new ArrayList<>(siteManagers));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            site = getArguments().getParcelable(ARG_SITE);
            siteManagers = new ArrayList<>(getArguments().<User>getParcelableArrayList(ARG_SITE_MANAGERS));
        }
        firestore = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_site_dialog, container, false);

        initializeViews(view);
        setupBloodTypeSelector();
        setupManagerSpinner();
        populateFields();
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

    private void populateFields() {
        if (site == null) return;

        nameEditText.setText(site.getName());
        addressEditText.setText(site.getAddress());
        latitudeEditText.setText(String.valueOf(site.getLocation().latitude));
        longitudeEditText.setText(String.valueOf(site.getLocation().longitude));
        contactPhoneEditText.setText(site.getContactPhone());
        descriptionEditText.setText(site.getDescription());

        // Set blood types
        bloodTypesTextView.setText(TextUtils.join(", ", site.getRequiredBloodTypes()));

        // Set manager selection
        for (int i = 0; i < siteManagers.size(); i++) {
            if (siteManagers.get(i).getId().equals(site.getManagerId())) {
                managerSpinner.setSelection(i);
                break;
            }
        }
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

        User selectedManager = (User) managerSpinner.getSelectedItem();
        boolean managerChanged = !selectedManager.getId().equals(site.getManagerId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", nameEditText.getText().toString().trim());
        updates.put("address", addressEditText.getText().toString().trim());
        updates.put("location", new LatLng(
                Double.parseDouble(latitudeEditText.getText().toString()),
                Double.parseDouble(longitudeEditText.getText().toString())));
        updates.put("managerId", selectedManager.getId());
        updates.put("requiredBloodTypes", getSelectedBloodTypes());
        updates.put("contactPhone", contactPhoneEditText.getText().toString().trim());
        updates.put("description", descriptionEditText.getText().toString().trim());
        updates.put("updatedAt", new Date());

        firestore.collection("donationSites")
                .document(site.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (managerChanged) {
                        updateManagerAssignments(selectedManager);
                    } else {
                        handleSuccess();
                    }
                })
                .addOnFailureListener(e -> handleError(e));
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (TextUtils.isEmpty(nameEditText.getText())) {
            nameEditText.setError("Name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(addressEditText.getText())) {
            addressEditText.setError("Address is required");
            isValid = false;
        }

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

        String phone = contactPhoneEditText.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            contactPhoneEditText.setError("Contact phone is required");
            isValid = false;
        } else if (!phone.matches("^[0-9]{10,}$")) {
            contactPhoneEditText.setError("Invalid phone number");
            isValid = false;
        }

        if (TextUtils.isEmpty(bloodTypesTextView.getText())) {
            bloodTypesTextView.setError("At least one blood type is required");
            isValid = false;
        }

        return isValid;
    }

    private List<String> getSelectedBloodTypes() {
        return Arrays.asList(bloodTypesTextView.getText().toString().split("\\s*,\\s*"));
    }

    private void updateManagerAssignments(User newManager) {
        // Remove site from old manager's list
        firestore.collection("users")
                .document(site.getManagerId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User oldManager = documentSnapshot.toObject(User.class);
                    if (oldManager != null) {
                        List<String> oldManagerSites = new ArrayList<>(oldManager.getManagedSiteIds());
                        oldManagerSites.remove(site.getId());

                        firestore.collection("users")
                                .document(oldManager.getId())
                                .update("managedSiteIds", oldManagerSites);
                    }
                });

        // Add site to new manager's list
        List<String> newManagerSites = new ArrayList<>(newManager.getManagedSiteIds());
        newManagerSites.add(site.getId());

        firestore.collection("users")
                .document(newManager.getId())
                .update("managedSiteIds", newManagerSites)
                .addOnSuccessListener(aVoid -> handleSuccess())
                .addOnFailureListener(this::handleError);
    }

    private void handleSuccess() {
        Toast.makeText(getContext(), "Site updated successfully", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void handleError(Exception e) {
        Toast.makeText(getContext(),
                "Error updating site: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
        saveButton.setEnabled(true);
    }
}