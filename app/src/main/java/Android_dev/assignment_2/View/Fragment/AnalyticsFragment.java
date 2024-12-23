package Android_dev.assignment_2.View.Fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import Android_dev.assignment_2.Model.Data.Entities.DonationEvent;
import Android_dev.assignment_2.Model.Data.Entities.DonationRegistration;
import Android_dev.assignment_2.Model.Data.Entities.DonationSite;
import Android_dev.assignment_2.Model.Data.Enums.RegistrationStatus;
import Android_dev.assignment_2.R;

public class AnalyticsFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private MaterialCardView chartCard;
    private Spinner timeframeSpinner;
    private Spinner metricSpinner;
    private TextView totalDonationsText;
    private TextView avgDonationsPerEventText;
    private TextView completionRateText;
    private TextView mostActiveRegionText;
    private TextView trendAnalysisText;
    private WebView chartWebView;
    private boolean isChartReady = false;

    private FirebaseFirestore firestore;
    private List<DonationEvent> events;
    private List<DonationRegistration> registrations;

    private static final String[] TIMEFRAME_OPTIONS = {
            "Last 7 Days", "Last 30 Days", "Last 3 Months", "Last Year", "All Time"
    };

    private static final String[] METRIC_OPTIONS = {
            "Total Donations", "Completion Rate", "Blood Volume", "Regional Distribution"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);
        initializeViews(view);
        initializeFirebase();
        setupViews();
        loadData();
        return view;
    }

    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        chartCard = view.findViewById(R.id.chartCard);
        timeframeSpinner = view.findViewById(R.id.timeframeSpinner);
        metricSpinner = view.findViewById(R.id.metricSpinner);
        totalDonationsText = view.findViewById(R.id.totalDonationsText);
        avgDonationsPerEventText = view.findViewById(R.id.avgDonationsPerEventText);
        completionRateText = view.findViewById(R.id.completionRateText);
        mostActiveRegionText = view.findViewById(R.id.mostActiveRegionText);
        trendAnalysisText = view.findViewById(R.id.trendAnalysisText);
        chartWebView = view.findViewById(R.id.chartWebView);
        setupWebView();
    }

    private void setupWebView() {
        WebSettings webSettings = chartWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDomStorageEnabled(true);

        // Add JavaScript interface
        chartWebView.addJavascriptInterface(new WebAppInterface(requireContext()), "Android");

        // Load the chart HTML
        chartWebView.loadUrl("file:///android_asset/charts/index.html");
    }

    private void initializeFirebase() {
        firestore = FirebaseFirestore.getInstance();
        events = new ArrayList<>();
        registrations = new ArrayList<>();
    }

    private void setupViews() {
        setupSpinners();
        swipeRefreshLayout.setOnRefreshListener(this::loadData);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void setupSpinners() {
        // Timeframe Spinner
        ArrayAdapter<String> timeframeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, TIMEFRAME_OPTIONS);
        timeframeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeframeSpinner.setAdapter(timeframeAdapter);
        timeframeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Metric Spinner
        ArrayAdapter<String> metricAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, METRIC_OPTIONS);
        metricAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        metricSpinner.setAdapter(metricAdapter);
        metricSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateChartForMetric(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadData() {
        showLoading(true);
        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();

        // Calculate start date based on selected timeframe
        switch (timeframeSpinner.getSelectedItemPosition()) {
            case 0: // Last 7 days
                cal.add(Calendar.DAY_OF_YEAR, -7);
                break;
            case 1: // Last 30 days
                cal.add(Calendar.DAY_OF_YEAR, -30);
                break;
            case 2: // Last 3 months
                cal.add(Calendar.MONTH, -3);
                break;
            case 3: // Last year
                cal.add(Calendar.YEAR, -1);
                break;
            case 4: // All time
                cal.add(Calendar.YEAR, -10); // Arbitrary past date
                break;
        }
        Date startDate = cal.getTime();

        // Query events within timeframe
        firestore.collection("donationEvents")
                .whereGreaterThanOrEqualTo("eventDate", startDate)
                .whereLessThanOrEqualTo("eventDate", endDate)
                .orderBy("eventDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(eventSnapshots -> {
                    events.clear();
                    List<String> eventIds = new ArrayList<>();
                    for (QueryDocumentSnapshot document : eventSnapshots) {
                        DonationEvent event = document.toObject(DonationEvent.class);
                        events.add(event);
                        eventIds.add(event.getId());
                    }

                    if (eventIds.isEmpty()) {
                        showEmptyState();
                        return;
                    }

                    // Load registrations for these events
                    firestore.collection("donationRegistrations")
                            .whereIn("eventId", eventIds)
                            .get()
                            .addOnSuccessListener(registrationSnapshots -> {
                                registrations.clear();
                                for (QueryDocumentSnapshot document : registrationSnapshots) {
                                    registrations.add(document.toObject(DonationRegistration.class));
                                }
                                calculateAnalytics();
                            })
                            .addOnFailureListener(e -> showError("Error loading registrations: " + e.getMessage()));
                })
                .addOnFailureListener(e -> showError("Error loading events: " + e.getMessage()));
    }

    private void calculateAnalytics() {
        int totalDonations = registrations.size();
        int completedDonations = 0;
        double totalBloodVolume = 0;
        Map<String, Integer> regionCounts = new HashMap<>();

        for (DonationRegistration registration : registrations) {
            if (registration.getStatus() == RegistrationStatus.COMPLETED) {
                completedDonations++;
                totalBloodVolume += registration.getBloodVolume();
            }

            // Count donations by region (using site IDs)
            DonationEvent event = findEventById(registration.getEventId());
            if (event != null) {
                regionCounts.merge(event.getSiteId(), 1, Integer::sum);
            }
        }

        // Calculate averages and rates
        double completionRate = totalDonations > 0 ?
                (double) completedDonations / totalDonations * 100 : 0;
        double avgDonationsPerEvent = events.size() > 0 ?
                (double) totalDonations / events.size() : 0;

        // Find most active region
        String mostActiveRegion = "";
        int maxDonations = 0;
        for (Map.Entry<String, Integer> entry : regionCounts.entrySet()) {
            if (entry.getValue() > maxDonations) {
                maxDonations = entry.getValue();
                mostActiveRegion = entry.getKey();
            }
        }

        // Update UI
        totalDonationsText.setText(String.format("Total Donations: %d", totalDonations));
        avgDonationsPerEventText.setText(String.format("Avg. Donations per Event: %.1f", avgDonationsPerEvent));
        completionRateText.setText(String.format("Completion Rate: %.1f%%", completionRate));
        mostActiveRegionText.setText("Most Active Region: " + (mostActiveRegion.isEmpty() ? "N/A" : mostActiveRegion));

        // Analyze trends
        analyzeTrends();

        // Update chart
        updateChartForMetric(metricSpinner.getSelectedItemPosition());

        showContent();
    }

    private void analyzeTrends() {
        // Sort events by date
        Map<String, Integer> monthlyDonations = new TreeMap<>();
        for (DonationRegistration registration : registrations) {
            DonationEvent event = findEventById(registration.getEventId());
            if (event != null) {
                String monthYear = new SimpleDateFormat("MM/yyyy").format(event.getEventDate());
                monthlyDonations.merge(monthYear, 1, Integer::sum);
            }
        }

        // Analyze trend
        StringBuilder trend = new StringBuilder("Trend Analysis:\n");
        if (monthlyDonations.size() >= 2) {
            List<Integer> counts = new ArrayList<>(monthlyDonations.values());
            int lastMonth = counts.get(counts.size() - 1);
            int previousMonth = counts.get(counts.size() - 2);

            double percentChange = ((double) (lastMonth - previousMonth) / previousMonth) * 100;

            if (percentChange > 0) {
                trend.append(String.format("Donations increased by %.1f%% compared to previous month", percentChange));
            } else if (percentChange < 0) {
                trend.append(String.format("Donations decreased by %.1f%% compared to previous month", Math.abs(percentChange)));
            } else {
                trend.append("Donation levels remained stable compared to previous month");
            }
        } else {
            trend.append("Not enough data to analyze trends");
        }

        trendAnalysisText.setText(trend.toString());
    }

    private DonationEvent findEventById(String eventId) {
        for (DonationEvent event : events) {
            if (event.getId().equals(eventId)) {
                return event;
            }
        }
        return null;
    }

    private void updateChartForMetric(int metricPosition) {
        if (events.isEmpty() || registrations.isEmpty()) {
            return;
        }

        String selectedMetric = METRIC_OPTIONS[metricPosition];
        Map<String, Object> chartData = new HashMap<>();
        List<Map<String, Object>> dataPoints = new ArrayList<>();

        switch (selectedMetric) {
            case "Total Donations":
                // Group donations by date
                Map<String, Integer> dailyDonations = new TreeMap<>();
                for (DonationRegistration reg : registrations) {
                    DonationEvent event = findEventById(reg.getEventId());
                    if (event != null) {
                        String date = new SimpleDateFormat("MM/dd").format(event.getEventDate());
                        dailyDonations.merge(date, 1, Integer::sum);
                    }
                }

                // Convert to data points
                for (Map.Entry<String, Integer> entry : dailyDonations.entrySet()) {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", entry.getKey());
                    point.put("value", entry.getValue());
                    dataPoints.add(point);
                }
                break;

            case "Completion Rate":
                // Group by date and calculate completion rate
                Map<String, int[]> completionData = new TreeMap<>(); // [total, completed]
                for (DonationRegistration reg : registrations) {
                    DonationEvent event = findEventById(reg.getEventId());
                    if (event != null) {
                        String date = new SimpleDateFormat("MM/dd").format(event.getEventDate());
                        int[] counts = completionData.computeIfAbsent(date, k -> new int[2]);
                        counts[0]++; // total
                        if (reg.getStatus() == RegistrationStatus.COMPLETED) {
                            counts[1]++; // completed
                        }
                    }
                }

                // Convert to data points
                for (Map.Entry<String, int[]> entry : completionData.entrySet()) {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", entry.getKey());
                    double rate = entry.getValue()[0] > 0 ?
                            (double) entry.getValue()[1] / entry.getValue()[0] * 100 : 0;
                    point.put("value", Math.round(rate * 10) / 10.0); // Round to 1 decimal
                    dataPoints.add(point);
                }
                break;

            case "Blood Volume":
                // Group blood volume by date
                Map<String, Double> volumeByDate = new TreeMap<>();
                for (DonationRegistration reg : registrations) {
                    if (reg.getStatus() == RegistrationStatus.COMPLETED) {
                        DonationEvent event = findEventById(reg.getEventId());
                        if (event != null) {
                            String date = new SimpleDateFormat("MM/dd").format(event.getEventDate());
                            volumeByDate.merge(date, reg.getBloodVolume(), Double::sum);
                        }
                    }
                }

                // Convert to data points
                for (Map.Entry<String, Double> entry : volumeByDate.entrySet()) {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", entry.getKey());
                    point.put("value", Math.round(entry.getValue() * 10) / 10.0); // Round to 1 decimal
                    dataPoints.add(point);
                }
                break;

            case "Regional Distribution":
                // Group donations by site/region
                Map<String, Integer> regionalCounts = new HashMap<>();
                for (DonationRegistration reg : registrations) {
                    DonationEvent event = findEventById(reg.getEventId());
                    if (event != null) {
                        firestore.collection("donationSites")
                                .document(event.getSiteId())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    DonationSite site = documentSnapshot.toObject(DonationSite.class);
                                    if (site != null) {
                                        String regionName = site.getName();
                                        regionalCounts.merge(regionName, 1, Integer::sum);

                                        // Check if this is the last site to process
                                        if (regionalCounts.size() == events.stream()
                                                .map(DonationEvent::getSiteId)
                                                .distinct()
                                                .count()) {
                                            // Convert to data points
                                            for (Map.Entry<String, Integer> entry : regionalCounts.entrySet()) {
                                                Map<String, Object> point = new HashMap<>();
                                                point.put("name", entry.getKey());
                                                point.put("value", entry.getValue());
                                                dataPoints.add(point);
                                            }

                                            // Update chart
                                            chartData.put("metric", selectedMetric);
                                            chartData.put("data", dataPoints);
                                            updateChartView(chartData);
                                        }
                                    }
                                });
                        return; // Exit early as we're handling async
                    }
                }
                break;
        }

        // Update chart (for non-regional metrics)
        if (!selectedMetric.equals("Regional Distribution")) {
            chartData.put("metric", selectedMetric);
            chartData.put("data", dataPoints);
            updateChartView(chartData);
        }
    }

    private void updateChartView(Map<String, Object> chartData) {
        if (!isChartReady) {
            return;
        }

        try {
            Gson gson = new Gson();
            String jsonData = gson.toJson(chartData);

            String javascript = "updateChart(" + jsonData + ")";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                chartWebView.evaluateJavascript(javascript, null);
            } else {
                chartWebView.loadUrl("javascript:" + javascript);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error updating chart: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(false);
        chartCard.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        showLoading(false);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showEmptyState() {
        showLoading(false);
        // Update UI to show no data available
        totalDonationsText.setText("Total Donations: 0");
        avgDonationsPerEventText.setText("Avg. Donations per Event: 0");
        completionRateText.setText("Completion Rate: 0%");
        mostActiveRegionText.setText("Most Active Region: N/A");
        trendAnalysisText.setText("No data available for trend analysis");
    }

    private void showContent() {
        showLoading(false);
        chartCard.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void onChartReady() {
            isChartReady = true;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (metricSpinner != null) {
                    updateChartForMetric(metricSpinner.getSelectedItemPosition());
                }
            });
        }
    }
}