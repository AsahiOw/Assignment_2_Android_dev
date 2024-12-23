package Android_dev.assignment_2.View.Adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Android_dev.assignment_2.Model.Data.Entities.Notification;
import Android_dev.assignment_2.Model.Data.Enums.NotificationType;
import Android_dev.assignment_2.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private final List<Notification> notifications;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        holder.titleTextView.setText(notification.getTitle());
        holder.messageTextView.setText(notification.getMessage());

        // Set time ago
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                notification.getCreatedAt().getTime(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS);
        holder.timeTextView.setText(timeAgo);

        // Set icon based on notification type
        holder.iconImageView.setImageResource(getIconForType(notification.getType()));

        // Set background based on read status
        holder.itemView.setAlpha(notification.isRead() ? 0.7f : 1.0f);

        holder.itemView.setOnClickListener(v -> {
            listener.onNotificationClick(notification);
            notifyItemChanged(position);
        });
    }

    private int getIconForType(NotificationType type) {
        switch (type) {
            case EVENT_REMINDER:
                return R.drawable.ic_calendar;
            case SITE_UPDATE:
                return R.drawable.ic_map;
            case REGISTRATION_CONFIRMATION:
                return R.drawable.ic_donations;
            case EVENT_CANCELLATION:
                return R.drawable.ic_calendar;
            case GENERAL_ANNOUNCEMENT:
            default:
                return R.drawable.ic_notifications;
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView titleTextView;
        TextView messageTextView;
        TextView timeTextView;

        NotificationViewHolder(View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }
}