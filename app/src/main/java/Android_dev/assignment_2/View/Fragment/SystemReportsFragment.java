package Android_dev.assignment_2.View.Fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Android_dev.assignment_2.Model.Data.Entities.DonationEvent;
import Android_dev.assignment_2.Model.Data.Entities.DonationRegistration;
import Android_dev.assignment_2.Model.Data.Entities.DonationSite;
import Android_dev.assignment_2.Model.Data.Enums.RegistrationStatus;
import Android_dev.assignment_2.R;

public class SystemReportsFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private MaterialCardView filterCard;
    private Spinner siteSpinner;
    private TextView startDateText;
    private TextView endDateText;
    private MaterialButton generateButton;
    private RecyclerView reportRecyclerView;
    private TextView emptyStateText;

    private FirebaseFirestore firestore;
    private List<DonationSite> sites;
    private Date startDate;
    private Date endDate;
    private SimpleDateFormat dateFormat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_system_reports, container, false);
        initializeViews(view);
        initializeFirebase();
        setupViews();
        loadSites();
        return view;
    }

    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        filterCard = view.findViewById(R.id.filterCard);
        siteSpinner = view.findViewById(R.id.siteSpinner);
        startDateText = view.findViewById(R.id.startDateText);
        endDateText = view.findViewById(R.id.endDateText);
        generateButton = view.findViewById(R.id.generateButton);
        reportRecyclerView = view.findViewById(R.id.reportRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        // Set default date range to last 30 days
        endDate = cal.getTime();
        cal.add(Calendar.MONTH, -1);
        startDate = cal.getTime();
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        sites = new ArrayList<>();
    }

    private void setupViews() {
        swipeRefreshLayout.setOnRefreshListener(this::loadSites);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        startDateText.setText(dateFormat.format(startDate));
        endDateText.setText(dateFormat.format(endDate));

        startDateText.setOnClickListener(v -> showDatePicker(true));
        endDateText.setOnClickListener(v -> showDatePicker(false));
        generateButton.setOnClickListener(v -> generateReport());

        reportRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(isStartDate ? startDate : endDate);

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    if (isStartDate) {
                        startDate = calendar.getTime();
                        startDateText.setText(dateFormat.format(startDate));
                    } else {
                        endDate = calendar.getTime();
                        endDateText.setText(dateFormat.format(endDate));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void loadSites() {
        showLoading(true);
        firestore.collection("donationSites")
                .orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    sites.clear();
                    sites.add(null); // Add "All Sites" option
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DonationSite site = document.toObject(DonationSite.class);
                        sites.add(site);
                    }
                    setupSiteSpinner();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    showError("Error loading sites: " + e.getMessage());
                    showLoading(false);
                });
    }

    private void setupSiteSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                sites.stream()
                        .map(site -> site == null ? "All Sites" : site.getName())
                        .toArray(String[]::new));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        siteSpinner.setAdapter(adapter);
    }

    private void generateReport() {
        if (startDate.after(endDate)) {
            Toast.makeText(getContext(), "Start date must be before end date",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        DonationSite selectedSite = sites.get(siteSpinner.getSelectedItemPosition());

        // Query events within date range
        Query eventsQuery = firestore.collection("donationEvents")
                .whereGreaterThanOrEqualTo("eventDate", startDate)
                .whereLessThanOrEqualTo("eventDate", endDate);

        if (selectedSite != null) {
            eventsQuery = eventsQuery.whereEqualTo("siteId", selectedSite.getId());
        }

        eventsQuery.get()
                .addOnSuccessListener(eventSnapshots -> {
                    List<String> eventIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : eventSnapshots) {
                        eventIds.add(document.getId());
                    }

                    if (eventIds.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    // Get registrations for these events
                    firestore.collection("donationRegistrations")
                            .whereIn("eventId", eventIds)
                            .get()
                            .addOnSuccessListener(registrationSnapshots -> {
                                List<DonationRegistration> registrations = new ArrayList<>();
                                for (QueryDocumentSnapshot document : registrationSnapshots) {
                                    registrations.add(document.toObject(DonationRegistration.class));
                                }
                                processRegistrationsData(registrations);
                            })
                            .addOnFailureListener(e ->
                                    showError("Error loading registrations: " + e.getMessage()));
                })
                .addOnFailureListener(e -> showError("Error loading events: " + e.getMessage()));
    }

    private void processRegistrationsData(List<DonationRegistration> registrations) {
        // Calculate statistics
        int totalRegistrations = registrations.size();
        int completedDonations = 0;
        int cancelledDonations = 0;
        int noShows = 0;
        Map<String, Double> bloodTypeVolumes = new HashMap<>();

        for (DonationRegistration registration : registrations) {
            switch (registration.getStatus()) {
                case COMPLETED:
                    completedDonations++;
                    String bloodType = registration.getBloodType();
                    if (bloodType != null) {
                        bloodTypeVolumes.merge(bloodType, registration.getBloodVolume(), Double::sum);
                    }
                    break;
                case CANCELLED:
                    cancelledDonations++;
                    break;
                case NO_SHOW:
                    noShows++;
                    break;
            }
        }

        // Create report card data
        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("Report Period: ")
                .append(dateFormat.format(startDate))
                .append(" - ")
                .append(dateFormat.format(endDate))
                .append("\n\n");

        reportBuilder.append("Total Registrations: ").append(totalRegistrations).append("\n");
        reportBuilder.append("Completed Donations: ").append(completedDonations).append("\n");
        reportBuilder.append("Cancelled Donations: ").append(cancelledDonations).append("\n");
        reportBuilder.append("No Shows: ").append(noShows).append("\n\n");

        float completionRate = totalRegistrations > 0 ?
                (float) completedDonations / totalRegistrations * 100 : 0;
        reportBuilder.append(String.format("Completion Rate: %.1f%%\n\n", completionRate));

        reportBuilder.append("Blood Collection by Type:\n");
        bloodTypeVolumes.forEach((type, volume) ->
                reportBuilder.append(String.format("%s: %.0f mL\n", type, volume)));

        // Update UI
        TextView reportText = new TextView(requireContext());
        reportText.setPadding(32, 32, 32, 32);
        reportText.setText(reportBuilder.toString());

        reportRecyclerView.removeAllViews();
        reportRecyclerView.addView(reportText);

        showReport();
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        filterCard.setEnabled(!isLoading);
        generateButton.setEnabled(!isLoading);
    }

    private void showError(String message) {
        showLoading(false);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showEmptyState() {
        showLoading(false);
        reportRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText("No donation data found for the selected criteria");
    }

    private void showReport() {
        showLoading(false);
        reportRecyclerView.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSites();
    }
}