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

import Android_dev.assignment_2.Model.Data.Entities.DonationRegistration;
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
                    if (documentSnapshot.exists()) {
                        // Match the exact field names from Firestore
                        String name = documentSnapshot.getString("name");
                        String email = currentUser.getEmail();
                        String phone = documentSnapshot.getString("phone");
                        String bloodType = documentSnapshot.getString("bloodType");
                        String role = documentSnapshot.getString("role");

                        // Update UI with null checks
                        if (name != null) nameTextView.setText(name);
                        if (email != null) emailTextView.setText(email);
                        if (phone != null) phoneTextView.setText(phone);
                        if (bloodType != null) bloodTypeTextView.setText(bloodType);

                        loadDonationHistory();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Error loading profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void loadDonationHistory() {
        firestore.collection("donationRegistrations")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalDonations = queryDocumentSnapshots.size();
                    totalDonationsTextView.setText(String.valueOf(totalDonations));
                    statsCardView.setVisibility(totalDonations > 0 ? View.VISIBLE : View.GONE);

                    DonationHistoryAdapter adapter = new DonationHistoryAdapter(
                            queryDocumentSnapshots.toObjects(DonationRegistration.class)
                    );
                    donationHistoryRecyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Error loading donation history: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }
}