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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import Android_dev.assignment_2.Model.Data.Entities.DonationSite;
import Android_dev.assignment_2.Model.Data.Entities.User;
import Android_dev.assignment_2.Model.Data.Enums.UserRole;
import Android_dev.assignment_2.R;

public class ManageSitesFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabNewSite;
    private SearchView searchView;

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private List<DonationSite> sites;
    private List<User> siteManagers;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_sites, container, false);

        initializeViews(view);
        initializeFirebase();
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();
        loadSiteManagers();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        fabNewSite = view.findViewById(R.id.fabNewSite);
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        sites = new ArrayList<>();
        siteManagers = new ArrayList<>();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // TODO: Create and set adapter
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadSiteManagers);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void setupFab() {
        fabNewSite.setOnClickListener(v -> showNewSiteDialog());
    }

    private void loadSiteManagers() {
        showLoading(true);

        firestore.collection("users")
                .whereEqualTo("role", UserRole.SITE_MANAGER.name())
                .get()
                .addOnSuccessListener(managerSnapshots -> {
                    siteManagers.clear();
                    for (QueryDocumentSnapshot document : managerSnapshots) {
                        User manager = document.toObject(User.class);
                        siteManagers.add(manager);
                    }
                    loadSites();
                })
                .addOnFailureListener(e -> showError("Error loading site managers: " + e.getMessage()));
    }

    private void loadSites() {
        firestore.collection("donationSites")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(siteSnapshots -> {
                    sites.clear();
                    for (QueryDocumentSnapshot document : siteSnapshots) {
                        DonationSite site = document.toObject(DonationSite.class);
                        sites.add(site);
                    }
                    updateUI();
                })
                .addOnFailureListener(e -> showError("Error loading sites: " + e.getMessage()));
    }

    private void showNewSiteDialog() {
        if (siteManagers.isEmpty()) {
            Toast.makeText(getContext(),
                    "No site managers available. Please add site managers first.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Show site creation dialog
        NewSiteDialogFragment dialog = NewSiteDialogFragment.newInstance(siteManagers);
        dialog.show(getChildFragmentManager(), "NewSiteDialog");
    }

    private void showEditSiteDialog(DonationSite site) {
        EditSiteDialogFragment dialog = EditSiteDialogFragment.newInstance(site, siteManagers);
        dialog.show(getChildFragmentManager(), "EditSiteDialog");
    }

    private void confirmDeactivateSite(DonationSite site) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Deactivate Site")
                .setMessage("Are you sure you want to deactivate this donation site? " +
                        "This will prevent new donations from being scheduled.")
                .setPositiveButton("Deactivate", (dialog, which) -> deactivateSite(site))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deactivateSite(DonationSite site) {
        site.setActive(false);

        firestore.collection("donationSites")
                .document(site.getId())
                .update("isActive", false)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Site deactivated successfully",
                            Toast.LENGTH_SHORT).show();
                    loadSites();
                })
                .addOnFailureListener(e ->
                        showError("Error deactivating site: " + e.getMessage()));
    }

    private void updateUI() {
        showLoading(false);

        if (sites.isEmpty()) {
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
        inflater.inflate(R.menu.menu_manage_sites, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        setupSearchView();

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setupSearchView() {
        searchView.setQueryHint("Search sites...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterSites(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSites(newText);
                return true;
            }
        });
    }

    private void filterSites(String query) {
        // TODO: Implement site filtering in adapter
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
        // TODO: Implement filter dialog
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSiteManagers();
    }
}