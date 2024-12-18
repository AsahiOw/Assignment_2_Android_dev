package Android_dev.assignment_2.Model.Data.Entities;

import java.util.Date;

import Android_dev.assignment_2.Model.Data.Enums.RegistrationStatus;

public class DonationRegistration {
    private String id;
    private String userId;
    private String eventId;
    private Date registrationDate;
    private RegistrationStatus status;  // REGISTERED, COMPLETED, CANCELLED, NO_SHOW
    private double bloodVolume;  // Only filled after donation
    private String bloodType;
    private String notes;

    // Constructors, getters, setters

    public DonationRegistration(String id, String userId, String eventId, Date registrationDate, RegistrationStatus status, double bloodVolume, String bloodType, String notes) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.registrationDate = registrationDate;
        this.status = status;
        this.bloodVolume = bloodVolume;
        this.bloodType = bloodType;
        this.notes = notes;
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

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public double getBloodVolume() {
        return bloodVolume;
    }

    public void setBloodVolume(double bloodVolume) {
        this.bloodVolume = bloodVolume;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
