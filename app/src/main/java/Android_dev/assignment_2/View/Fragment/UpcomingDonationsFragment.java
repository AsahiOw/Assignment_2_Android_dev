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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Android_dev.assignment_2.Model.Data.Entities.DonationRegistration;
import Android_dev.assignment_2.Model.Data.Entities.DonationEvent;
import Android_dev.assignment_2.Model.Data.Enums.RegistrationStatus;
import Android_dev.assignment_2.R;
import Android_dev.assignment_2.View.Adapter.DonationRegistrationAdapter;

public class UpcomingDonationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private DonationRegistrationAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabNewDonation;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private List<DonationRegistration> upcomingDonations;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming_donations, container, false);
        initializeViews(view);
        initializeFirebase();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();
        loadUpcomingDonations();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        fabNewDonation = view.findViewById(R.id.fabNewDonation);
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        upcomingDonations = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new DonationRegistrationAdapter(upcomingDonations, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadUpcomingDonations);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void setupFab() {
        fabNewDonation.setOnClickListener(v -> showNewDonationDialog());
    }

    private void showNewDonationDialog() {
        NewDonationDialogFragment dialog = new NewDonationDialogFragment();
        dialog.show(getChildFragmentManager(), "NewDonationDialog");
    }

    private void loadUpcomingDonations() {
        if (firebaseAuth.getCurrentUser() == null) {
            updateUI(new ArrayList<>());
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();
        Date currentDate = new Date();
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        firestore.collection("donationRegistrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", RegistrationStatus.REGISTERED)
                .get()
                .addOnSuccessListener(registrationSnapshots -> {
                    List<DonationRegistration> newDonations = new ArrayList<>();
                    final int[] remainingQueries = {registrationSnapshots.size()};

                    if (remainingQueries[0] == 0) {
                        updateUI(newDonations);
                        return;
                    }

                    for (QueryDocumentSnapshot registrationDoc : registrationSnapshots) {
                        DonationRegistration registration = registrationDoc.toObject(DonationRegistration.class);

                        // Get the associated event to check the date
                        firestore.collection("donationEvents")
                                .document(registration.getEventId())
                                .get()
                                .addOnSuccessListener(eventDoc -> {
                                    DonationEvent event = eventDoc.toObject(DonationEvent.class);
                                    if (event != null && event.getEventDate().after(currentDate)) {
                                        newDonations.add(registration);
                                    }

                                    remainingQueries[0]--;
                                    if (remainingQueries[0] == 0) {
                                        updateUI(newDonations);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    handleError(e);
                                    remainingQueries[0]--;
                                    if (remainingQueries[0] == 0) {
                                        updateUI(newDonations);
                                    }
                                });
                    }
                })
                .addOnFailureListener(this::handleError);
    }

    private void handleError(Exception e) {
        Toast.makeText(getContext(),
                "Error loading upcoming donations: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void updateUI(List<DonationRegistration> newDonations) {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        upcomingDonations.clear();
        upcomingDonations.addAll(newDonations);
        adapter.notifyDataSetChanged();

        if (upcomingDonations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText(R.string.no_upcoming_donations);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUpcomingDonations();
    }
}