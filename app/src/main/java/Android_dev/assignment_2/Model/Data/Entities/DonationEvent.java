package Android_dev.assignment_2.Model.Data.Entities;

import java.util.Date;
import java.util.List;

import Android_dev.assignment_2.Model.Data.Enums.EventStatus;

public class DonationEvent {
    private String id;
    private String siteId;
    private Date eventDate;
    private TimeSlot timeSlot;
    private int maxDonors;
    private int currentRegistrations;
    private List<String> requiredBloodTypes;
    private EventStatus status;  // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    private List<DonationRegistration> registrations;

    // Constructors, getters, setters

    public DonationEvent(String id, String siteId, Date eventDate, TimeSlot timeSlot, int maxDonors, int currentRegistrations, List<String> requiredBloodTypes, EventStatus status, List<DonationRegistration> registrations) {
        this.id = id;
        this.siteId = siteId;
        this.eventDate = eventDate;
        this.timeSlot = timeSlot;
        this.maxDonors = maxDonors;
        this.currentRegistrations = currentRegistrations;
        this.requiredBloodTypes = requiredBloodTypes;
        this.status = status;
        this.registrations = registrations;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public int getMaxDonors() {
        return maxDonors;
    }

    public void setMaxDonors(int maxDonors) {
        this.maxDonors = maxDonors;
    }

    public int getCurrentRegistrations() {
        return currentRegistrations;
    }

    public void setCurrentRegistrations(int currentRegistrations) {
        this.currentRegistrations = currentRegistrations;
    }

    public List<String> getRequiredBloodTypes() {
        return requiredBloodTypes;
    }

    public void setRequiredBloodTypes(List<String> requiredBloodTypes) {
        this.requiredBloodTypes = requiredBloodTypes;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public List<DonationRegistration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<DonationRegistration> registrations) {
        this.registrations = registrations;
    }
}