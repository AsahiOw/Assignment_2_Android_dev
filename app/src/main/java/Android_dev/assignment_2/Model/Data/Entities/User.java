package Android_dev.assignment_2.Model.Data.Entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import Android_dev.assignment_2.Model.Data.Enums.BloodType;
import Android_dev.assignment_2.Model.Data.Enums.UserRole;

public class User implements Parcelable {
    private String id;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private BloodType bloodType;
    private UserRole role;
    private List<String> managedSiteIds;
    private List<DonationRegistration> donationHistory;

    public User() {
        // Required empty constructor for Firestore
        managedSiteIds = new ArrayList<>();
        donationHistory = new ArrayList<>();
    }

    public User(String id, String email, String password, String fullName, String phoneNumber,
                BloodType bloodType, UserRole role, List<String> managedSiteIds,
                List<DonationRegistration> donationHistory) {
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

    protected User(Parcel in) {
        id = in.readString();
        email = in.readString();
        password = in.readString();
        fullName = in.readString();
        phoneNumber = in.readString();
        bloodType = BloodType.valueOf(in.readString());
        role = UserRole.valueOf(in.readString());
        managedSiteIds = new ArrayList<>();
        in.readStringList(managedSiteIds);
        donationHistory = new ArrayList<>();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(fullName);
        dest.writeString(phoneNumber);
        dest.writeString(bloodType.name());
        dest.writeString(role.name());
        dest.writeStringList(managedSiteIds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // Getters and Setters remain the same
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

    @Override
    public String toString() {
        return fullName;
    }
}