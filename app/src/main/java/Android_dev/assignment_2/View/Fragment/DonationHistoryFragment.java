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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import Android_dev.assignment_2.Model.Data.Entities.DonationRegistration;
import Android_dev.assignment_2.R;
import Android_dev.assignment_2.View.Adapter.DonationHistoryAdapter;

public class DonationHistoryFragment extends Fragment {
    private RecyclerView recyclerView;
    private DonationHistoryAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private List<DonationRegistration> donationHistory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donation_history, container, false);
        initializeViews(view);
        initializeFirebase();
        setupRecyclerView();
        setupSwipeRefresh();
        loadDonationHistory();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        donationHistory = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new DonationHistoryAdapter(donationHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadDonationHistory);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void loadDonationHistory() {
        if (firebaseAuth.getCurrentUser() == null) {
            updateUI(new ArrayList<>());
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        firestore.collection("donationRegistrations")
                .whereEqualTo("userId", userId)
                .orderBy("registrationDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DonationRegistration> newDonations = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DonationRegistration donation = document.toObject(DonationRegistration.class);
                        newDonations.add(donation);
                    }
                    updateUI(newDonations);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Error loading donation history: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void updateUI(List<DonationRegistration> newDonations) {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        donationHistory.clear();
        donationHistory.addAll(newDonations);
        adapter.notifyDataSetChanged();

        if (donationHistory.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText(R.string.no_past_donations);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDonationHistory();
    }
}