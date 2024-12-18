package Android_dev.assignment_2.View.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import Android_dev.assignment_2.R;
import Android_dev.assignment_2.Model.Data.Entities.DonationSite;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore firestore;
    private Location lastKnownLocation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Get the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("MapDebug", "Map is ready");

        // Add default settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Set default location (e.g., RMIT University Vietnam)
        LatLng rmit = new LatLng(10.729567, 106.694207);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rmit, 15f));

        // Test marker to verify map is working
        mMap.addMarker(new MarkerOptions()
                .position(rmit)
                .title("RMIT University Vietnam"));

        // Check location permissions
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            requestLocationPermission();
        }
    }

    private void loadDonationSites() {
        Log.d("MapDebug", "Loading donation sites");
        firestore.collection("donationSites")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("MapDebug", "Successfully got " + queryDocumentSnapshots.size() + " sites");
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        DonationSite site = document.toObject(DonationSite.class);
                        addSiteMarker(site);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MapDebug", "Error loading donation sites: " + e.getMessage());
                    Toast.makeText(requireContext(),
                            "Error loading donation sites: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);

        // Get last known location and move camera
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        lastKnownLocation = location;
                        LatLng currentLatLng = new LatLng(location.getLatitude(),
                                location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    }
                });
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Show an explanation to the user
            Toast.makeText(requireContext(),
                    "Location permission is needed to show your current location",
                    Toast.LENGTH_LONG).show();
        }

        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(requireContext(),
                        "Location permission denied. Some features may be limited.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

//    private void loadDonationSites() {
//        firestore.collection("donationSites")
//                .whereEqualTo("isActive", true)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                        DonationSite site = document.toObject(DonationSite.class);
//                        addSiteMarker(site);
//                    }
//                })
//                .addOnFailureListener(e ->
//                        Toast.makeText(requireContext(),
//                                "Error loading donation sites: " + e.getMessage(),
//                                Toast.LENGTH_SHORT).show());
//    }

    private void addSiteMarker(DonationSite site) {
        LatLng location = site.getLocation();

        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title(site.getName())
                .snippet(site.getAddress())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        mMap.addMarker(markerOptions);
    }
}