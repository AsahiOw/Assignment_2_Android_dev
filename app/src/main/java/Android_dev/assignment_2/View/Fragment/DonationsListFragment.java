package Android_dev.assignment_2.View.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import Android_dev.assignment_2.Model.Data.Entities.DonationRegistration;
import Android_dev.assignment_2.R;
import Android_dev.assignment_2.View.Adapter.DonationRegistrationAdapter;

public class DonationsListFragment extends Fragment {
    private static final String ARG_IS_UPCOMING = "is_upcoming";

    private boolean isUpcoming;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private DonationRegistrationAdapter adapter;
    private List<DonationRegistration> donations = new ArrayList<>();

    public static DonationsListFragment newInstance(boolean isUpcoming) {
        DonationsListFragment fragment = new DonationsListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_UPCOMING, isUpcoming);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isUpcoming = getArguments().getBoolean(ARG_IS_UPCOMING);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donations_list, container, false);
        initializeViews(view);
        setupRecyclerView();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
    }

    private void setupRecyclerView() {
        adapter = new DonationRegistrationAdapter(donations, isUpcoming);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    public void updateDonations(List<DonationRegistration> newDonations) {
        donations.clear();
        donations.addAll(newDonations);
        adapter.notifyDataSetChanged();

        // Update empty view visibility
        if (donations.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(isUpcoming ?
                    R.string.no_upcoming_donations :
                    R.string.no_past_donations);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}