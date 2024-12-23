package Android_dev.assignment_2.View.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import Android_dev.assignment_2.R;

public class SettingsFragment extends Fragment {
    private MaterialSwitch notificationSwitch;
    private MaterialSwitch reminderSwitch;
    private MaterialSwitch soundSwitch;
    private MaterialSwitch vibrationSwitch;
    private Slider reminderTimeSlider;
    private MaterialSwitch locationSwitch;
    private Slider searchRadiusSlider;

    private SharedPreferences preferences;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeFirebase();
        initializeViews(view);
        loadPreferences();
        setupListeners();

        return view;
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
    }

    private void initializeViews(View view) {
        // Notification Settings
        notificationSwitch = view.findViewById(R.id.notificationSwitch);
        reminderSwitch = view.findViewById(R.id.reminderSwitch);
        soundSwitch = view.findViewById(R.id.soundSwitch);
        vibrationSwitch = view.findViewById(R.id.vibrationSwitch);
        reminderTimeSlider = view.findViewById(R.id.reminderTimeSlider);

        // Location Settings
        locationSwitch = view.findViewById(R.id.locationSwitch);
        searchRadiusSlider = view.findViewById(R.id.searchRadiusSlider);

        // Initialize SharedPreferences
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    private void loadPreferences() {
        // Load notification preferences
        notificationSwitch.setChecked(preferences.getBoolean("notifications_enabled", true));
        reminderSwitch.setChecked(preferences.getBoolean("reminders_enabled", true));
        soundSwitch.setChecked(preferences.getBoolean("sound_enabled", true));
        vibrationSwitch.setChecked(preferences.getBoolean("vibration_enabled", true));
        reminderTimeSlider.setValue(preferences.getFloat("reminder_time", 24)); // Default 24 hours

        // Load location preferences
        locationSwitch.setChecked(preferences.getBoolean("location_enabled", true));
        searchRadiusSlider.setValue(preferences.getFloat("search_radius", 10)); // Default 10 km
    }

    private void setupListeners() {
        // Notification switch listener
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("notifications_enabled", isChecked).apply();

            if (isChecked) {
                subscribeToNotifications();
            } else {
                unsubscribeFromNotifications();
            }

            // Enable/disable related settings
            reminderSwitch.setEnabled(isChecked);
            soundSwitch.setEnabled(isChecked);
            vibrationSwitch.setEnabled(isChecked);
            reminderTimeSlider.setEnabled(isChecked && reminderSwitch.isChecked());
        });

        // Reminder switch listener
        reminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("reminders_enabled", isChecked).apply();
            reminderTimeSlider.setEnabled(isChecked);
            updateUserPreferences();
        });

        // Sound switch listener
        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("sound_enabled", isChecked).apply();
            updateUserPreferences();
        });

        // Vibration switch listener
        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("vibration_enabled", isChecked).apply();
            updateUserPreferences();
        });

        // Reminder time slider listener
        reminderTimeSlider.addOnChangeListener((slider, value, fromUser) -> {
            preferences.edit().putFloat("reminder_time", value).apply();
            updateUserPreferences();
        });

        // Location switch listener
        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("location_enabled", isChecked).apply();
            searchRadiusSlider.setEnabled(isChecked);
            updateUserPreferences();
        });

        // Search radius slider listener
        searchRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            preferences.edit().putFloat("search_radius", value).apply();
            updateUserPreferences();
        });
    }

    private void subscribeToNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("user_" + currentUserId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Notifications enabled",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        notificationSwitch.setChecked(false);
                        Toast.makeText(getContext(), "Failed to enable notifications",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unsubscribeFromNotifications() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("user_" + currentUserId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Notifications disabled",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        notificationSwitch.setChecked(true);
                        Toast.makeText(getContext(), "Failed to disable notifications",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserPreferences() {
        // Create preferences object to store in Firestore
        UserPreferences userPreferences = new UserPreferences(
                preferences.getBoolean("notifications_enabled", true),
                preferences.getBoolean("reminders_enabled", true),
                preferences.getBoolean("sound_enabled", true),
                preferences.getBoolean("vibration_enabled", true),
                preferences.getFloat("reminder_time", 24),
                preferences.getBoolean("location_enabled", true),
                preferences.getFloat("search_radius", 10)
        );

        // Update preferences in Firestore
        firestore.collection("user_preferences")
                .document(currentUserId)
                .set(userPreferences)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to save preferences",
                                Toast.LENGTH_SHORT).show());
    }

    // Helper class to store preferences in Firestore
    private static class UserPreferences {
        boolean notificationsEnabled;
        boolean remindersEnabled;
        boolean soundEnabled;
        boolean vibrationEnabled;
        float reminderTime;
        boolean locationEnabled;
        float searchRadius;

        UserPreferences(boolean notificationsEnabled, boolean remindersEnabled,
                        boolean soundEnabled, boolean vibrationEnabled, float reminderTime,
                        boolean locationEnabled, float searchRadius) {
            this.notificationsEnabled = notificationsEnabled;
            this.remindersEnabled = remindersEnabled;
            this.soundEnabled = soundEnabled;
            this.vibrationEnabled = vibrationEnabled;
            this.reminderTime = reminderTime;
            this.locationEnabled = locationEnabled;
            this.searchRadius = searchRadius;
        }
    }
}