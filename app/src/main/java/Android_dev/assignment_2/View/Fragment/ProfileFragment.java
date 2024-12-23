package Android_dev.assignment_2.View.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import Android_dev.assignment_2.Model.Data.Entities.DonationRegistration;
import Android_dev.assignment_2.Model.Data.Entities.User;
import Android_dev.assignment_2.R;
import Android_dev.assignment_2.View.Adapter.DonationHistoryAdapter;

public class ProfileFragment extends Fragment {
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView bloodTypeTextView;
    private TextView totalDonationsTextView;
    private RecyclerView donationHistoryRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialCardView statsCardView;
    private Button editProfileButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initializeFirebase();
        initializeViews(view);
        setupListeners();
        loadUserProfile();

        return view;
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
    }

    private void initializeViews(View view) {
        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        bloodTypeTextView = view.findViewById(R.id.bloodTypeTextView);
        totalDonationsTextView = view.findViewById(R.id.totalDonationsTextView);
        donationHistoryRecyclerView = view.findViewById(R.id.donationHistoryRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        statsCardView = view.findViewById(R.id.statsCardView);
        editProfileButton = view.findViewById(R.id.editProfileButton);

        // Setup RecyclerView
        donationHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadUserProfile);

        editProfileButton.setOnClickListener(v -> {
            // Launch edit profile dialog
            EditProfileDialogFragment dialog = new EditProfileDialogFragment();
            dialog.show(getChildFragmentManager(), "EditProfileDialog");
        });
    }

    private void loadUserProfile() {
        if (currentUser == null) return;

        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        updateUI(user);
                        loadDonationHistory();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void loadDonationHistory() {
        firestore.collection("donationRegistrations")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DonationRegistration> donationHistory = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DonationRegistration registration = document.toObject(DonationRegistration.class);
                        donationHistory.add(registration);
                    }
                    updateDonationHistory(donationHistory);
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading donation history: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void updateUI(User user) {
        nameTextView.setText(user.getFullName());
        emailTextView.setText(user.getEmail());
        phoneTextView.setText(user.getPhoneNumber());
        bloodTypeTextView.setText(user.getBloodType().getDisplayName());
    }

    private void updateDonationHistory(List<DonationRegistration> donationHistory) {
        totalDonationsTextView.setText(String.valueOf(donationHistory.size()));

        // You could add more statistics here
        statsCardView.setVisibility(donationHistory.isEmpty() ? View.GONE : View.VISIBLE);

        // Update RecyclerView
        // Note: You'll need to create DonationHistoryAdapter
        DonationHistoryAdapter adapter = new DonationHistoryAdapter(donationHistory);
        donationHistoryRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }
}