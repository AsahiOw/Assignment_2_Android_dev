package Android_dev.assignment_2.Util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Android_dev.assignment_2.Model.Data.Enums.NotificationType;

public class FirestoreHelper {
    private static FirestoreHelper instance;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    private FirestoreHelper() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirestoreHelper getInstance() {
        if (instance == null) {
            instance = new FirestoreHelper();
        }
        return instance;
    }

    public void createNotification(String title, String message, NotificationType type) {
        if (auth.getCurrentUser() == null) return;

        Map<String, Object> notification = new HashMap<>();
        notification.put("id", UUID.randomUUID().toString());
        notification.put("userId", auth.getCurrentUser().getUid());
        notification.put("title", title);
        notification.put("message", message);
        notification.put("createdAt", new Date());
        notification.put("isRead", false);
        notification.put("type", type.name());
        notification.put("relatedEntityId", null);

        firestore.collection("notifications")
                .document(notification.get("id").toString())
                .set(notification);
    }

    public void markNotificationAsRead(String notificationId) {
        if (auth.getCurrentUser() == null) return;

        firestore.collection("notifications")
                .document(notificationId)
                .update("isRead", true);
    }

    public void deleteNotification(String notificationId) {
        if (auth.getCurrentUser() == null) return;

        firestore.collection("notifications")
                .document(notificationId)
                .delete();
    }
}