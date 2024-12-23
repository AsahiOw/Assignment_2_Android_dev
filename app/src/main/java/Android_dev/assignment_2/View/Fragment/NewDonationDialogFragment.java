package Android_dev.assignment_2.View.Fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import Android_dev.assignment_2.Model.Data.Entities.DonationEvent;
import Android_dev.assignment_2.Model.Data.Entities.DonationRegistration;
import Android_dev.assignment_2.Model.Data.Entities.DonationSite;
import Android_dev.assignment_2.Model.Data.Enums.RegistrationStatus;
import Android_dev.assignment_2.R;

public class NewDonationDialogFragment extends BottomSheetDialogFragment {
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private Spinner siteSpinner;
    private TextView dateTextView;
    private TextView timeTextView;
    private Calendar selectedDateTime;
    private List<DonationSite> availableSites;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        availableSites = new ArrayList<>();
        selectedDateTime = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_donation_dialog, container, false);
        initializeViews(view);
        loadAvailableSites();
        return view;
    }

    private void initializeViews(View view) {
        siteSpinner = view.findViewById(R.id.siteSpinner);
        dateTextView = view.findViewById(R.id.dateTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        Button selectDateButton = view.findViewById(R.id.selectDateButton);
        Button selectTimeButton = view.findViewById(R.id.selectTimeButton);
        Button registerButton = view.findViewById(R.id.registerButton);
        Button cancelButton = view.findViewById(R.id.cancelButton);

        // Set click listeners
        selectDateButton.setOnClickListener(v -> showDatePicker());
        selectTimeButton.setOnClickListener(v -> showTimePicker());
        registerButton.setOnClickListener(v -> registerDonation());
        cancelButton.setOnClickListener(v -> dismiss());

        // Set initial date and time
        updateDateDisplay();
        updateTimeDisplay();
    }

    private void loadAvailableSites() {
        firestore.collection("donationSites")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    availableSites.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DonationSite site = document.toObject(DonationSite.class);
                        availableSites.add(site);
                    }
                    setupSiteSpinner();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading sites: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void setupSiteSpinner() {
        ArrayAdapter<DonationSite> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                availableSites);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        siteSpinner.setAdapter(adapter);
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH));

        // Set minimum date to today
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showTimePicker() {
        new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                false).show();
    }

    private void updateDateDisplay() {
        dateTextView.setText(dateFormat.format(selectedDateTime.getTime()));
    }

    private void updateTimeDisplay() {
        timeTextView.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void registerDonation() {
        if (siteSpinner.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please select a donation site", Toast.LENGTH_SHORT).show();
            return;
        }

        DonationSite selectedSite = (DonationSite) siteSpinner.getSelectedItem();
        String userId = firebaseAuth.getCurrentUser().getUid();
        Date selectedDate = selectedDateTime.getTime();

        // First check if there's an available event
        firestore.collection("donationEvents")
                .whereEqualTo("siteId", selectedSite.getId())
                .whereEqualTo("eventDate", selectedDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    DonationEvent event = null;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        event = document.toObject(DonationEvent.class);
                        if (event.getCurrentRegistrations() < event.getMaxDonors()) {
                            break;
                        }
                    }

                    if (event != null) {
                        createDonationRegistration(event);
                    } else {
                        Toast.makeText(getContext(), "No available slots for selected date/time",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error checking events: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void createDonationRegistration(DonationEvent event) {
        String registrationId = UUID.randomUUID().toString();

        DonationRegistration registration = new DonationRegistration(
                registrationId,
                firebaseAuth.getCurrentUser().getUid(),
                event.getId(),
                new Date(),
                RegistrationStatus.REGISTERED,
                0.0,  // Blood volume will be updated after donation
                null, // Blood type will be updated after donation
                ""   // No notes initially
        );

        // Save to Firestore
        firestore.collection("donationRegistrations")
                .document(registrationId)
                .set(registration)
                .addOnSuccessListener(aVoid -> {
                    // Update event's current registrations count
                    firestore.collection("donationEvents")
                            .document(event.getId())
                            .update("currentRegistrations", event.getCurrentRegistrations() + 1)
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(getContext(), "Registration successful",
                                        Toast.LENGTH_SHORT).show();
                                dismiss();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Registration failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}