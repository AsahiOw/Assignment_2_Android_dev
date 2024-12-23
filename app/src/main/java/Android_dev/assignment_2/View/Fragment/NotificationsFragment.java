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

import Android_dev.assignment_2.Model.Data.Entities.Notification;
import Android_dev.assignment_2.R;
import Android_dev.assignment_2.View.Adapter.NotificationAdapter;

public class NotificationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private List<Notification> notifications;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        notifications = new ArrayList<>();

        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadNotifications();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(notifications, this::markNotificationAsRead);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadNotifications);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void loadNotifications() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notifications.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Notification notification = document.toObject(Notification.class);
                        notifications.add(notification);
                    }
                    updateUI();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "Error loading notifications: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        if (notifications.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    private void markNotificationAsRead(Notification notification) {
        if (!notification.isRead()) {
            notification.setRead(true);
            firestore.collection("notifications")
                    .document(notification.getId())
                    .update("read", true)
                    .addOnSuccessListener(aVoid -> adapter.notifyDataSetChanged())
                    .addOnFailureListener(e -> Toast.makeText(getContext(),
                            "Error marking notification as read", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
    }
}