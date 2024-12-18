package Android_dev.assignment_2.Model.Data.Entities;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;

public class DonationSite {
    private String id;
    private String name;
    private String address;
    private LatLng location;  // Google Maps LatLng object
    private String managerId;
    private List<String> requiredBloodTypes;
    private List<DonationEvent> events;
    private String contactPhone;
    private String description;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;

    // Constructors, getters, setters

    public DonationSite(String id, String name, String address, LatLng location, String managerId, List<String> requiredBloodTypes, List<DonationEvent> events, String contactPhone, String description, boolean isActive, Date createdAt, Date updatedAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.location = location;
        this.managerId = managerId;
        this.requiredBloodTypes = requiredBloodTypes;
        this.events = events;
        this.contactPhone = contactPhone;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public List<String> getRequiredBloodTypes() {
        return requiredBloodTypes;
    }

    public void setRequiredBloodTypes(List<String> requiredBloodTypes) {
        this.requiredBloodTypes = requiredBloodTypes;
    }

    public List<DonationEvent> getEvents() {
        return events;
    }

    public void setEvents(List<DonationEvent> events) {
        this.events = events;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}