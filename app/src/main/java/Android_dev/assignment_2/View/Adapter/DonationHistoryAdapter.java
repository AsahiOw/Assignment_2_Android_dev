package Android_dev.assignment_2.View.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class DonationHistoryAdapter extends RecyclerView.Adapter<DonationHistoryAdapter.HistoryViewHolder> {
    private final List<DonationRegistration> donationHistory;
    private final SimpleDateFormat dateFormat;
    private final FirebaseFirestore firestore;

    public DonationHistoryAdapter(List<DonationRegistration> donationHistory) {
        this.donationHistory = donationHistory;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        DonationRegistration donation = donationHistory.get(position);

        // Load event details
        firestore.collection("donationEvents")
                .document(donation.getEventId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String siteName = documentSnapshot.getString("siteName");
                    holder.locationTextView.setText(siteName != null ? siteName : "Unknown Location");
                });

        // Set date
        String formattedDate = dateFormat.format(donation.getRegistrationDate());
        holder.dateTextView.setText(formattedDate);

        // Set status with appropriate color
        holder.statusTextView.setText(getStatusText(donation.getStatus()));
        holder.statusTextView.setTextColor(getStatusColor(donation.getStatus(), holder.itemView));

        // Set blood volume if available
        if (donation.getStatus() == RegistrationStatus.COMPLETED && donation.getBloodVolume() > 0) {
            holder.bloodVolumeTextView.setVisibility(View.VISIBLE);
            holder.bloodVolumeTextView.setText(String.format(Locale.getDefault(),
                    "%.0f mL", donation.getBloodVolume()));
        } else {
            holder.bloodVolumeTextView.setVisibility(View.GONE);
        }

        // Set blood type if available
        if (donation.getBloodType() != null && !donation.getBloodType().isEmpty()) {
            holder.bloodTypeTextView.setVisibility(View.VISIBLE);
            holder.bloodTypeTextView.setText(donation.getBloodType());
        } else {
            holder.bloodTypeTextView.setVisibility(View.GONE);
        }

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
                return "Scheduled";
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

    @Override
    public int getItemCount() {
        return donationHistory.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView locationTextView;
        TextView statusTextView;
        TextView bloodVolumeTextView;
        TextView bloodTypeTextView;
        TextView notesTextView;

        HistoryViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            bloodVolumeTextView = itemView.findViewById(R.id.bloodVolumeTextView);
            bloodTypeTextView = itemView.findViewById(R.id.bloodTypeTextView);
            notesTextView = itemView.findViewById(R.id.notesTextView);
        }
    }
}