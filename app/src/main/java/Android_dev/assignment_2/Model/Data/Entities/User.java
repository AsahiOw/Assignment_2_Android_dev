package Android_dev.assignment_2.Model.Data.Entities;

import java.util.List;

import Android_dev.assignment_2.Model.Data.Enums.BloodType;
import Android_dev.assignment_2.Model.Data.Enums.UserRole;

public class User {
    private String id;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private BloodType bloodType;
    private UserRole role;  // DONOR, SITE_MANAGER, SUPER_USER
    private List<String> managedSiteIds;  // Only for SITE_MANAGER role
    private List<DonationRegistration> donationHistory;

    // Constructors, getters, setters

    public User(String id, String email, String password, String fullName, String phoneNumber, BloodType bloodType, UserRole role, List<String> managedSiteIds, List<DonationRegistration> donationHistory) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.bloodType = bloodType;
        this.role = role;
        this.managedSiteIds = managedSiteIds;
        this.donationHistory = donationHistory;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public void setBloodType(BloodType bloodType) {
        this.bloodType = bloodType;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public List<String> getManagedSiteIds() {
        return managedSiteIds;
    }

    public void setManagedSiteIds(List<String> managedSiteIds) {
        this.managedSiteIds = managedSiteIds;
    }

    public List<DonationRegistration> getDonationHistory() {
        return donationHistory;
    }

    public void setDonationHistory(List<DonationRegistration> donationHistory) {
        this.donationHistory = donationHistory;
    }
}
