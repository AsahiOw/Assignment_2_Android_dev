package Android_dev.assignment_2.View.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import Android_dev.assignment_2.R;
import Android_dev.assignment_2.Model.Data.Enums.UserRole;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private TextView userNameTextView;
    private TextView userEmailTextView;
    private ImageView userProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Check if user is logged in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize views
        setupViews();

        // Load user data
        loadUserData(currentUser);

        // Set up navigation
        setupNavigation();
    }

    private void setupViews() {
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Setup Navigation Drawer Header
        View headerView = navigationView.getHeaderView(0);
        userNameTextView = headerView.findViewById(R.id.nav_header_name);
        userEmailTextView = headerView.findViewById(R.id.nav_header_email);
        userProfileImage = headerView.findViewById(R.id.nav_header_image);
    }

    private void setupNavigation() {
        // Setup Navigation Controller
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // Setup AppBarConfiguration
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_map, R.id.nav_donations, R.id.nav_notifications, R.id.nav_profile)
                .setOpenableLayout(drawerLayout)
                .build();

        // Setup NavigationUI
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Setup Navigation Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, findViewById(R.id.toolbar),
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set Navigation Item Selected Listener
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void loadUserData(FirebaseUser currentUser) {
        firestore.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String email = currentUser.getEmail();
                        UserRole role = UserRole.valueOf(documentSnapshot.getString("role"));

                        // Update UI
                        userNameTextView.setText(name);
                        userEmailTextView.setText(email);

                        // Update navigation menu based on user role
                        updateNavigationMenu(role);
                    }
                });
    }

    private void updateNavigationMenu(UserRole role) {
        navigationView.getMenu().clear();
        switch (role) {
            case SUPER_USER:
                navigationView.inflateMenu(R.menu.menu_admin_drawer);
                break;
            case SITE_MANAGER:
                navigationView.inflateMenu(R.menu.menu_manager_drawer);
                break;
            default: // DONOR
                navigationView.inflateMenu(R.menu.menu_donor_drawer);
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_logout) {
            firebaseAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }

        // Handle other navigation item clicks
        boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
        if (handled) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return handled;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}