package Android_dev.assignment_2.View.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Android_dev.assignment_2.Model.Data.Entities.DonationEvent;
import Android_dev.assignment_2.Model.Data.Entities.DonationRegistration;
import Android_dev.assignment_2.Model.Data.Enums.RegistrationStatus;
import Android_dev.assignment_2.R;
import Android_dev.assignment_2.View.Adapter.DonationPagerAdapter;

public class DonationsFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donations, container, false);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        // Initialize views
        initializeViews(view);
        setupViewPager();
        setupRefreshLayout();

        return view;
    }

    private void initializeViews(View view) {
        viewPager = view.findViewById(R.id.donationsPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Setup FAB for creating new donation registration
        FloatingActionButton fabNewDonation = view.findViewById(R.id.fabNewDonation);
        fabNewDonation.setOnClickListener(v -> showNewDonationDialog());
    }

    private void setupViewPager() {
        DonationPagerAdapter pagerAdapter = new DonationPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.upcoming_donations);
                    break;
                case 1:
                    tab.setText(R.string.past_donations);
                    break;
            }
        }).attach();
    }

    private void setupRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void refreshData() {
        loadDonations();
    }

    private void loadDonations() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        firestore.collection("donationRegistrations")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DonationRegistration> upcomingDonations = new ArrayList<>();
                    List<DonationRegistration> pastDonations = new ArrayList<>();
                    Date currentDate = new Date();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DonationRegistration registration = document.toObject(DonationRegistration.class);

                        // Load the associated event details
                        loadEventDetails(registration, currentDate, upcomingDonations, pastDonations);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Error loading donations: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadEventDetails(DonationRegistration registration, Date currentDate,
                                  List<DonationRegistration> upcomingDonations,
                                  List<DonationRegistration> pastDonations) {
        firestore.collection("donationEvents")
                .document(registration.getEventId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    DonationEvent event = documentSnapshot.toObject(DonationEvent.class);
                    if (event != null) {
                        if (event.getEventDate().after(currentDate) &&
                                registration.getStatus() != RegistrationStatus.CANCELLED) {
                            upcomingDonations.add(registration);
                        } else {
                            pastDonations.add(registration);
                        }

                        // Update ViewPager fragments
                        updateDonationLists(upcomingDonations, pastDonations);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading event details: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void updateDonationLists(List<DonationRegistration> upcomingDonations,
                                     List<DonationRegistration> pastDonations) {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        // Update the ViewPager fragments
        DonationPagerAdapter adapter = (DonationPagerAdapter) viewPager.getAdapter();
        if (adapter != null) {
            adapter.updateUpcomingDonations(upcomingDonations);
            adapter.updatePastDonations(pastDonations);
        }

        // Show empty state if no donations
        boolean isEmpty = upcomingDonations.isEmpty() && pastDonations.isEmpty();
        emptyStateText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void showNewDonationDialog() {
        // Create and show dialog for new donation registration
        // This will be implemented in a separate dialog fragment
        NewDonationDialogFragment dialog = new NewDonationDialogFragment();
        dialog.show(getChildFragmentManager(), "NewDonationDialog");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDonations();
    }
}