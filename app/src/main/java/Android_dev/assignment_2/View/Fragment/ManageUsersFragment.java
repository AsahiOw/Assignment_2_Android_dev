package Android_dev.assignment_2.View.Fragment;

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
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import Android_dev.assignment_2.Model.Data.Entities.User;
import Android_dev.assignment_2.Model.Data.Enums.UserRole;
import Android_dev.assignment_2.R;

public class ManageUsersFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private List<User> users;
    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_users, container, false);

        initializeViews(view);
        initializeFirebase();
        setupRecyclerView();
        setupSwipeRefresh();
        loadUsers();

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
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        users = new ArrayList<>();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // TODO: Create and set adapter
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadUsers);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void loadUsers() {
        showLoading(true);

        firestore.collection("users")
                .orderBy("role", Query.Direction.ASCENDING)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    users.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        // Don't show current admin in the list
                        if (!user.getId().equals(currentUserId)) {
                            users.add(user);
                        }
                    }
                    updateUI();
                })
                .addOnFailureListener(e -> showError("Error loading users: " + e.getMessage()));
    }

    private void showEditUserDialog(User user) {
        if (user.getId().equals(currentUserId)) {
            Toast.makeText(getContext(), "Cannot edit current admin user",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        EditUserDialogFragment dialog = EditUserDialogFragment.newInstance(user);
        dialog.show(getChildFragmentManager(), "EditUserDialog");
    }

    private void confirmDeactivateUser(User user) {
        if (user.getId().equals(currentUserId)) {
            Toast.makeText(getContext(), "Cannot deactivate current admin user",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Deactivate User")
                .setMessage("Are you sure you want to deactivate this user? " +
                        "They will no longer be able to access the system.")
                .setPositiveButton("Deactivate", (dialog, which) -> deactivateUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deactivateUser(User user) {
        firestore.collection("users")
                .document(user.getId())
                .update("active", false)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "User deactivated successfully",
                            Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e ->
                        showError("Error deactivating user: " + e.getMessage()));
    }

    private void updateUserRole(User user, UserRole newRole) {
        if (user.getId().equals(currentUserId)) {
            Toast.makeText(getContext(), "Cannot change current admin user's role",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users")
                .document(user.getId())
                .update("role", newRole.name())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "User role updated successfully",
                            Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e ->
                        showError("Error updating user role: " + e.getMessage()));
    }

    private void updateUI() {
        showLoading(false);

        if (users.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
            // Update adapter
        }
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_manage_users, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        setupSearchView();

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setupSearchView() {
        searchView.setQueryHint("Search users...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
    }

    private void filterUsers(String query) {
        // TODO: Implement user filtering in adapter
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
        String[] roles = new String[]{"All Roles", "Donors", "Site Managers"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Filter by Role")
                .setItems(roles, (dialog, which) -> {
                    // TODO: Implement role filtering
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUsers();
    }
}