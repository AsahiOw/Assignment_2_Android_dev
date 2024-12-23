package Android_dev.assignment_2.View.Fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import Android_dev.assignment_2.Model.Data.Entities.DonationEvent;
import Android_dev.assignment_2.Model.Data.Entities.DonationSite;
import Android_dev.assignment_2.Model.Data.Entities.TimeSlot;
import Android_dev.assignment_2.Model.Data.Enums.EventStatus;
import Android_dev.assignment_2.R;

public class ManageEventsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabNewEvent;

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;
    private DonationSite currentSite;
    private List<DonationEvent> events;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_events, container, false);

        initializeViews(view);
        initializeFirebase();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();
        loadSiteAndEvents();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        fabNewEvent = view.findViewById(R.id.fabNewEvent);
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        events = new ArrayList<>();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadSiteAndEvents);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void setupFab() {
        fabNewEvent.setOnClickListener(v -> showNewEventDialog());
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
                .orderBy("eventDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    events.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DonationEvent event = document.toObject(DonationEvent.class);
                        events.add(event);
                    }
                    updateUI();
                })
                .addOnFailureListener(e -> showError("Error loading events: " + e.getMessage()));
    }

    private void updateUI() {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        if (events.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
            // Update adapter here
        }
    }

    private void showNewEventDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    showTimePickerDialog(calendar);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePickerDialog(Calendar calendar) {
        new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    createNewEvent(calendar.getTime());
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false).show();
    }

    private void createNewEvent(Date eventDate) {
        if (currentSite == null) return;

        String eventId = UUID.randomUUID().toString();
        TimeSlot timeSlot = new TimeSlot(
                LocalTime.of(eventDate.getHours(), eventDate.getMinutes()),
                LocalTime.of(eventDate.getHours() + 2, eventDate.getMinutes()) // 2-hour slot
        );

        DonationEvent newEvent = new DonationEvent(
                eventId,
                currentSite.getId(),
                eventDate,
                timeSlot,
                20, // Default max donors
                0,  // Current registrations
                currentSite.getRequiredBloodTypes(),
                EventStatus.SCHEDULED,
                new ArrayList<>()
        );

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("donationEvents")
                .document(eventId)
                .set(newEvent)
                .addOnSuccessListener(aVoid -> {
                    events.add(newEvent);
                    updateUI();
                    Toast.makeText(getContext(), "Event created successfully",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> showError("Error creating event: " + e.getMessage()));
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_manage_events, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            // Show filter dialog
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSiteAndEvents();
    }
}