package Android_dev.assignment_2.View.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import Android_dev.assignment_2.Model.Data.Entities.DonationEvent;
import Android_dev.assignment_2.Model.Data.Entities.DonationRegistration;
import Android_dev.assignment_2.Model.Data.Entities.DonationSite;
import Android_dev.assignment_2.Model.Data.Entities.User;
import Android_dev.assignment_2.Model.Data.Enums.RegistrationStatus;
import Android_dev.assignment_2.R;

public class RegisteredDonorsFragment extends Fragment {
    private RecyclerView recyclerView;
    private Spinner eventSpinner;
    private TextView donorCountTextView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private DonationSite currentSite;
    private List<DonationEvent> events;
    private List<DonationRegistration> registrations;
    private DonationEvent selectedEvent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registered_donors, container, false);

        initializeViews(view);
        initializeFirebase();
        setupRecyclerView();
        setupSpinner();
        setupSwipeRefresh();
        loadSiteAndEvents();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        eventSpinner = view.findViewById(R.id.eventSpinner);
        donorCountTextView = view.findViewById(R.id.donorCountTextView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        events = new ArrayList<>();
        registrations = new ArrayList<>();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // You'll need to create and set the adapter here
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventSpinner.setAdapter(adapter);

        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedEvent = events.get(position);
                loadRegistrations();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedEvent = null;
                updateUI(new ArrayList<>());
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadSiteAndEvents);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void loadSiteAndEvents() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

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
                .orderBy("eventDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    events.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DonationEvent event = document.toObject(DonationEvent.class);
                        events.add(event);
                    }
                    updateEventSpinner();
                })
                .addOnFailureListener(e -> showError("Error loading events: " + e.getMessage()));
    }

    private void updateEventSpinner() {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) eventSpinner.getAdapter();
        adapter.clear();
        adapter.addAll(events.stream()
                .map(event -> String.format("%s - %d registrations",
                        event.getEventDate().toString(),
                        event.getCurrentRegistrations()))
                .collect(Collectors.toList()));
        adapter.notifyDataSetChanged();

        if (!events.isEmpty()) {
            selectedEvent = events.get(0);
            loadRegistrations();
        } else {
            updateUI(new ArrayList<>());
        }
    }

    private void loadRegistrations() {
        if (selectedEvent == null) return;

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("donationRegistrations")
                .whereEqualTo("eventId", selectedEvent.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DonationRegistration> newRegistrations = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DonationRegistration registration = document.toObject(DonationRegistration.class);
                        newRegistrations.add(registration);
                    }
                    updateUI(newRegistrations);
                })
                .addOnFailureListener(e -> showError("Error loading registrations: " + e.getMessage()));
    }

    private void updateUI(List<DonationRegistration> newRegistrations) {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        registrations.clear();
        registrations.addAll(newRegistrations);

        // Update donor count text
        String countText = String.format("%d/%d donors registered",
                registrations.size(),
                selectedEvent != null ? selectedEvent.getMaxDonors() : 0);
        donorCountTextView.setText(countText);

        if (registrations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText(R.string.no_registered_donors);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
            // Notify adapter of changes
        }
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_registered_donors, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            showFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        // TODO: Implement filter dialog to filter by registration status
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedEvent != null) {
            loadRegistrations();
        } else {
            loadSiteAndEvents();
        }
    }
}