package Android_dev.assignment_2.View.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import Android_dev.assignment_2.Model.Data.Entities.DonationSite;
import Android_dev.assignment_2.Model.Data.Enums.BloodType;
import Android_dev.assignment_2.R;

public class ManageSiteFragment extends Fragment {
    private EditText nameEditText;
    private EditText addressEditText;
    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private EditText contactPhoneEditText;
    private EditText descriptionEditText;
    private MultiAutoCompleteTextView bloodTypesTextView;
    private Switch activeSwitch;
    private Button saveButton;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private DonationSite currentSite;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_sites, container, false);

        initializeViews(view);
        initializeFirebase();
        setupBloodTypeSelector();
        setupSwipeRefresh();
        loadSiteData();

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
        activeSwitch = view.findViewById(R.id.activeSwitch);
        saveButton = view.findViewById(R.id.saveButton);
        progressBar = view.findViewById(R.id.progressBar);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        saveButton.setOnClickListener(v -> validateAndSaveSite());
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
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

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadSiteData);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void loadSiteData() {
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        firestore.collection("donationSites")
                .whereEqualTo("managerId", currentUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        currentSite = queryDocumentSnapshots.getDocuments().get(0)
                                .toObject(DonationSite.class);
                        populateFields();
                    }
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Error loading site data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void populateFields() {
        if (currentSite == null) return;

        nameEditText.setText(currentSite.getName());
        addressEditText.setText(currentSite.getAddress());
        latitudeEditText.setText(String.valueOf(currentSite.getLocation().latitude));
        longitudeEditText.setText(String.valueOf(currentSite.getLocation().longitude));
        contactPhoneEditText.setText(currentSite.getContactPhone());
        descriptionEditText.setText(currentSite.getDescription());
        activeSwitch.setChecked(currentSite.isActive());

        String bloodTypes = TextUtils.join(", ", currentSite.getRequiredBloodTypes());
        bloodTypesTextView.setText(bloodTypes);
    }

    private void validateAndSaveSite() {
        if (!validateInputs()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        String name = nameEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        double latitude = Double.parseDouble(latitudeEditText.getText().toString());
        double longitude = Double.parseDouble(longitudeEditText.getText().toString());
        String contactPhone = contactPhoneEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        boolean isActive = activeSwitch.isChecked();

        List<String> bloodTypes = new ArrayList<>(Arrays.asList(
                bloodTypesTextView.getText().toString().split("\\s*,\\s*")));

        DonationSite site = new DonationSite(
                currentSite != null ? currentSite.getId() : firestore.collection("donationSites").document().getId(),
                name,
                address,
                new LatLng(latitude, longitude),
                currentUserId,
                bloodTypes,
                new ArrayList<>(),
                contactPhone,
                description,
                isActive,
                currentSite != null ? currentSite.getCreatedAt() : new Date(),
                new Date()
        );

        firestore.collection("donationSites")
                .document(site.getId())
                .set(site)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Site saved successfully",
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Error saving site: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                });
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

        if (TextUtils.isEmpty(contactPhoneEditText.getText())) {
            contactPhoneEditText.setError("Contact phone is required");
            isValid = false;
        } else if (!contactPhoneEditText.getText().toString().matches("^[0-9]{10,}$")) {
            contactPhoneEditText.setError("Invalid phone number");
            isValid = false;
        }

        if (TextUtils.isEmpty(bloodTypesTextView.getText())) {
            bloodTypesTextView.setError("At least one blood type is required");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSiteData();
    }
}