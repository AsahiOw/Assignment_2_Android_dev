package Android_dev.assignment_2.View.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Android_dev.assignment_2.Model.Data.Entities.DonationEvent;
import Android_dev.assignment_2.Model.Data.Entities.DonationRegistration;
import Android_dev.assignment_2.Model.Data.Entities.DonationSite;
import Android_dev.assignment_2.Model.Data.Enums.BloodType;
import Android_dev.assignment_2.Model.Data.Enums.EventStatus;
import Android_dev.assignment_2.Model.Data.Enums.RegistrationStatus;
import Android_dev.assignment_2.R;

public class SiteStatisticsFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    // Statistics TextViews
    private TextView totalEventsTextView;
    private TextView totalDonorsTextView;
    private TextView completedDonationsTextView;
    private TextView cancelledDonationsTextView;
    private TextView noShowDonationsTextView;
    private TextView averageAttendanceTextView;
    private TextView bloodTypeStatsTextView;

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private DonationSite currentSite;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_site_statistics, container, false);

        initializeViews(view);
        initializeFirebase();
        setupSwipeRefresh();
        loadSiteAndStatistics();

        return view;
    }

    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        totalEventsTextView = view.findViewById(R.id.totalEventsTextView);
        totalDonorsTextView = view.findViewById(R.id.totalDonorsTextView);
        completedDonationsTextView = view.findViewById(R.id.completedDonationsTextView);
        cancelledDonationsTextView = view.findViewById(R.id.cancelledDonationsTextView);
        noShowDonationsTextView = view.findViewById(R.id.noShowDonationsTextView);
        averageAttendanceTextView = view.findViewById(R.id.averageAttendanceTextView);
        bloodTypeStatsTextView = view.findViewById(R.id.bloodTypeStatsTextView);
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadSiteAndStatistics);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void loadSiteAndStatistics() {
        showLoading(true);

        firestore.collection("donationSites")
                .whereEqualTo("managerId", currentUserId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        currentSite = queryDocumentSnapshots.getDocuments().get(0)
                                .toObject(DonationSite.class);
                        loadEvents();
                    } else {
                        showError("No site found for current manager");
                    }
                })
                .addOnFailureListener(e -> showError("Error loading site: " + e.getMessage()));
    }

    private void loadEvents() {
        if (currentSite == null) return;

        firestore.collection("donationEvents")
                .whereEqualTo("siteId", currentSite.getId())
                .get()
                .addOnSuccessListener(eventSnapshots -> {
                    List<DonationEvent> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : eventSnapshots) {
                        events.add(document.toObject(DonationEvent.class));
                    }
                    loadRegistrations(events);
                })
                .addOnFailureListener(e -> showError("Error loading events: " + e.getMessage()));
    }

    private void loadRegistrations(List<DonationEvent> events) {
        if (events.isEmpty()) {
            calculateAndDisplayStatistics(events, new ArrayList<>());
            return;
        }

        List<String> eventIds = new ArrayList<>();
        for (DonationEvent event : events) {
            eventIds.add(event.getId());
        }

        firestore.collection("donationRegistrations")
                .whereIn("eventId", eventIds)
                .get()
                .addOnSuccessListener(registrationSnapshots -> {
                    List<DonationRegistration> registrations = new ArrayList<>();
                    for (QueryDocumentSnapshot document : registrationSnapshots) {
                        registrations.add(document.toObject(DonationRegistration.class));
                    }
                    calculateAndDisplayStatistics(events, registrations);
                })
                .addOnFailureListener(e -> showError("Error loading registrations: " + e.getMessage()));
    }

    private void calculateAndDisplayStatistics(List<DonationEvent> events,
                                               List<DonationRegistration> registrations) {
        // Calculate basic statistics
        int totalEvents = events.size();
        int totalDonors = registrations.size();
        int completedDonations = 0;
        int cancelledDonations = 0;
        int noShows = 0;

        // Blood type statistics
        Map<String, Integer> bloodTypeStats = new HashMap<>();

        // Calculate detailed statistics
        for (DonationRegistration registration : registrations) {
            switch (registration.getStatus()) {
                case COMPLETED:
                    completedDonations++;
                    if (registration.getBloodType() != null) {
                        bloodTypeStats.merge(registration.getBloodType(), 1, Integer::sum);
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

        // Calculate average attendance rate
        float attendanceRate = totalDonors > 0 ?
                ((float) completedDonations / totalDonors) * 100 : 0;

        // Update UI
        totalEventsTextView.setText(String.format("Total Events: %d", totalEvents));
        totalDonorsTextView.setText(String.format("Total Registrations: %d", totalDonors));
        completedDonationsTextView.setText(String.format("Completed Donations: %d", completedDonations));
        cancelledDonationsTextView.setText(String.format("Cancelled Donations: %d", cancelledDonations));
        noShowDonationsTextView.setText(String.format("No Shows: %d", noShows));
        averageAttendanceTextView.setText(String.format("Attendance Rate: %.1f%%", attendanceRate));

        // Display blood type statistics
        StringBuilder bloodTypeStatsText = new StringBuilder("Blood Type Distribution:\n");
        for (Map.Entry<String, Integer> entry : bloodTypeStats.entrySet()) {
            bloodTypeStatsText.append(String.format("%s: %d\n", entry.getKey(), entry.getValue()));
        }
        bloodTypeStatsTextView.setText(bloodTypeStatsText.toString());

        showLoading(false);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showError(String message) {
        showLoading(false);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSiteAndStatistics();
    }
}