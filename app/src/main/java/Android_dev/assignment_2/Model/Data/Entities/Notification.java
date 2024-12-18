package Android_dev.assignment_2.Model.Data.Entities;

import java.util.Date;

import Android_dev.assignment_2.Model.Data.Enums.NotificationType;

public class Notification {
    private String id;
    private String userId;
    private String title;
    private String message;
    private Date createdAt;
    private boolean isRead;
    private NotificationType type;
    private String relatedEntityId;  // Could be eventId or siteId

    // Constructors, getters, setters

    public Notification(String id, String userId, String title, String message, Date createdAt, boolean isRead, NotificationType type, String relatedEntityId) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
        this.type = type;
        this.relatedEntityId = relatedEntityId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(String relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }
}