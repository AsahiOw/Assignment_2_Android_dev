package Android_dev.assignment_2.Model.Data.Entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

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
    private String bloodTypeStr; // Store as string in Firestore
    @Exclude
    private BloodType bloodType; // Exclude from Firestore
    private UserRole role;
    private List<String> managedSiteIds;
    private List<DonationRegistration> donationHistory;

    // Default constructor required for Firestore
    public User() {
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
        setBloodType(bloodType);
        this.role = role;
        this.managedSiteIds = managedSiteIds != null ? managedSiteIds : new ArrayList<>();
        this.donationHistory = donationHistory != null ? donationHistory : new ArrayList<>();
    }

    // Getters and Setters
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

    @Exclude
    public BloodType getBloodType() {
        if (bloodType == null && bloodTypeStr != null) {
            try {
                bloodType = BloodType.fromString(bloodTypeStr);
            } catch (IllegalArgumentException e) {
                bloodType = null;
            }
        }
        return bloodType;
    }

    @Exclude
    public void setBloodType(BloodType bloodType) {
        this.bloodType = bloodType;
        this.bloodTypeStr = bloodType != null ? bloodType.getDisplayName() : null;
    }

    // Methods for Firestore serialization
    @PropertyName("bloodType")
    public String getBloodTypeStr() {
        return bloodTypeStr;
    }

    @PropertyName("bloodType")
    public void setBloodTypeStr(String bloodTypeStr) {
        this.bloodTypeStr = bloodTypeStr;
        if (bloodTypeStr != null) {
            try {
                this.bloodType = BloodType.fromString(bloodTypeStr);
            } catch (IllegalArgumentException e) {
                this.bloodType = null;
            }
        } else {
            this.bloodType = null;
        }
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
        this.managedSiteIds = managedSiteIds != null ? managedSiteIds : new ArrayList<>();
    }

    public List<DonationRegistration> getDonationHistory() {
        return donationHistory;
    }

    public void setDonationHistory(List<DonationRegistration> donationHistory) {
        this.donationHistory = donationHistory != null ? donationHistory : new ArrayList<>();
    }

    // Parcelable implementation
    protected User(Parcel in) {
        id = in.readString();
        email = in.readString();
        password = in.readString();
        fullName = in.readString();
        phoneNumber = in.readString();
        bloodTypeStr = in.readString();
        if (bloodTypeStr != null) {
            try {
                bloodType = BloodType.fromString(bloodTypeStr);
            } catch (IllegalArgumentException e) {
                bloodType = null;
            }
        }
        try {
            role = UserRole.valueOf(in.readString());
        } catch (IllegalArgumentException e) {
            role = UserRole.DONOR; // Default to DONOR if invalid
        }
        managedSiteIds = new ArrayList<>();
        in.readStringList(managedSiteIds);
        donationHistory = new ArrayList<>();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(fullName);
        dest.writeString(phoneNumber);
        dest.writeString(bloodTypeStr);
        dest.writeString(role != null ? role.name() : UserRole.DONOR.name());
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

    @Override
    public String toString() {
        return fullName;
    }
}