package Android_dev.assignment_2.View.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;

import java.util.ArrayList;
import java.util.List;

import Android_dev.assignment_2.Model.Data.Entities.DonationRegistration;
import Android_dev.assignment_2.View.Fragment.DonationsListFragment;

public class DonationPagerAdapter extends FragmentStateAdapter {
    private DonationsListFragment upcomingFragment;
    private DonationsListFragment pastFragment;
    private List<DonationRegistration> upcomingDonations = new ArrayList<>();
    private List<DonationRegistration> pastDonations = new ArrayList<>();

    public DonationPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        initializeFragments();
    }

    private void initializeFragments() {
        upcomingFragment = DonationsListFragment.newInstance(true);  // true for upcoming
        pastFragment = DonationsListFragment.newInstance(false);     // false for past
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return upcomingFragment;
            case 1:
                return pastFragment;
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 2;  // Always 2 pages: Upcoming and Past
    }

    public void updateUpcomingDonations(List<DonationRegistration> donations) {
        this.upcomingDonations = donations;
        if (upcomingFragment != null) {
            upcomingFragment.updateDonations(donations);
        }
    }

    public void updatePastDonations(List<DonationRegistration> donations) {
        this.pastDonations = donations;
        if (pastFragment != null) {
            pastFragment.updateDonations(donations);
        }
    }

    public List<DonationRegistration> getUpcomingDonations() {
        return upcomingDonations;
    }

    public List<DonationRegistration> getPastDonations() {
        return pastDonations;
    }
}