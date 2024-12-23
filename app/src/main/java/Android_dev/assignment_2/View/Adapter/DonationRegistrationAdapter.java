package Android_dev.assignment_2.View.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import Android_dev.assignment_2.Model.Data.Entities.DonationRegistration;
import Android_dev.assignment_2.Model.Data.Enums.RegistrationStatus;
import Android_dev.assignment_2.R;

public class DonationRegistrationAdapter extends RecyclerView.Adapter<DonationRegistrationAdapter.DonationViewHolder> {
    private final List<DonationRegistration> donations;
    private final boolean isUpcoming;
    private final SimpleDateFormat dateFormat;
    private final FirebaseFirestore firestore;

    public DonationRegistrationAdapter(List<DonationRegistration> donations, boolean isUpcoming) {
        this.donations = donations;
        this.isUpcoming = isUpcoming;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public DonationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation_registration, parent, false);
        return new DonationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonationViewHolder holder, int position) {
        DonationRegistration donation = donations.get(position);

        // Load and display event details
        firestore.collection("donationEvents")
                .document(donation.getEventId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String siteName = documentSnapshot.getString("siteName");
                    holder.siteNameTextView.setText(siteName);

                    // Format and set date
                    String formattedDate = dateFormat.format(documentSnapshot.getDate("eventDate"));
                    holder.dateTextView.setText(formattedDate);
                });

        // Set status
        holder.statusTextView.setText(getStatusText(donation.getStatus()));
        holder.statusTextView.setTextColor(getStatusColor(donation.getStatus(), holder.itemView));

        // Show blood volume for completed donations
        if (donation.getStatus() == RegistrationStatus.COMPLETED) {
            holder.bloodVolumeTextView.setVisibility(View.VISIBLE);
            holder.bloodVolumeTextView.setText(String.format(Locale.getDefault(),
                    "Blood Volume: %.0f mL", donation.getBloodVolume()));
        } else {
            holder.bloodVolumeTextView.setVisibility(View.GONE);
        }

        // Handle cancel button visibility and click
        if (isUpcoming && donation.getStatus() == RegistrationStatus.REGISTERED) {
            holder.cancelButton.setVisibility(View.VISIBLE);
            holder.cancelButton.setOnClickListener(v -> cancelDonation(donation, position));
        } else {
            holder.cancelButton.setVisibility(View.GONE);
        }

        // Set blood type
        holder.bloodTypeTextView.setText(donation.getBloodType());

        // Set notes if available
        if (donation.getNotes() != null && !donation.getNotes().isEmpty()) {
            holder.notesTextView.setVisibility(View.VISIBLE);
            holder.notesTextView.setText(donation.getNotes());
        } else {
            holder.notesTextView.setVisibility(View.GONE);
        }
    }

    private String getStatusText(RegistrationStatus status) {
        switch (status) {
            case REGISTERED:
                return "Registered";
            case COMPLETED:
                return "Completed";
            case CANCELLED:
                return "Cancelled";
            case NO_SHOW:
                return "No Show";
            default:
                return "Unknown";
        }
    }

    private int getStatusColor(RegistrationStatus status, View itemView) {
        switch (status) {
            case REGISTERED:
                return itemView.getContext().getColor(R.color.colorPrimary);
            case COMPLETED:
                return itemView.getContext().getColor(R.color.colorSuccess);
            case CANCELLED:
                return itemView.getContext().getColor(R.color.colorError);
            case NO_SHOW:
                return itemView.getContext().getColor(R.color.colorWarning);
            default:
                return itemView.getContext().getColor(R.color.colorTextPrimary);
        }
    }

    private void cancelDonation(DonationRegistration donation, int position) {
        donation.setStatus(RegistrationStatus.CANCELLED);

        // Update in Firestore
        firestore.collection("donationRegistrations")
                .document(donation.getId())
                .update("status", RegistrationStatus.CANCELLED)
                .addOnSuccessListener(aVoid -> {
                    notifyItemChanged(position);
                    // You might want to add a callback here to inform the fragment
                });
    }

    @Override
    public int getItemCount() {
        return donations.size();
    }

    static class DonationViewHolder extends RecyclerView.ViewHolder {
        TextView siteNameTextView;
        TextView dateTextView;
        TextView statusTextView;
        TextView bloodVolumeTextView;
        TextView bloodTypeTextView;
        TextView notesTextView;
        Button cancelButton;

        DonationViewHolder(View itemView) {
            super(itemView);
            siteNameTextView = itemView.findViewById(R.id.siteNameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            bloodVolumeTextView = itemView.findViewById(R.id.bloodVolumeTextView);
            bloodTypeTextView = itemView.findViewById(R.id.bloodTypeTextView);
            notesTextView = itemView.findViewById(R.id.notesTextView);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
}