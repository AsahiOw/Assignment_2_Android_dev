package Android_dev.assignment_2.Model.Data.Entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DonationSite implements Parcelable {
    private String id;
    private String name;
    private String address;
    private LatLng location;
    private String managerId;
    private List<String> requiredBloodTypes;
    private List<DonationEvent> events;
    private String contactPhone;
    private String description;
    private boolean isActive;
    private Date createdAt;
    private Date updatedAt;

    public DonationSite() {
        // Required empty constructor for Firestore
        requiredBloodTypes = new ArrayList<>();
        events = new ArrayList<>();
    }

    public DonationSite(String id, String name, String address, LatLng location, String managerId,
                        List<String> requiredBloodTypes, List<DonationEvent> events,
                        String contactPhone, String description, boolean isActive,
                        Date createdAt, Date updatedAt) {
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

    protected DonationSite(Parcel in) {
        id = in.readString();
        name = in.readString();
        address = in.readString();
        location = new LatLng(in.readDouble(), in.readDouble());
        managerId = in.readString();
        requiredBloodTypes = new ArrayList<>();
        in.readStringList(requiredBloodTypes);
        events = new ArrayList<>(); // Events are not parceled
        contactPhone = in.readString();
        description = in.readString();
        isActive = in.readByte() != 0;
        createdAt = new Date(in.readLong());
        updatedAt = new Date(in.readLong());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeDouble(location.latitude);
        dest.writeDouble(location.longitude);
        dest.writeString(managerId);
        dest.writeStringList(requiredBloodTypes);
        dest.writeString(contactPhone);
        dest.writeString(description);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeLong(createdAt.getTime());
        dest.writeLong(updatedAt.getTime());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DonationSite> CREATOR = new Creator<DonationSite>() {
        @Override
        public DonationSite createFromParcel(Parcel in) {
            return new DonationSite(in);
        }

        @Override
        public DonationSite[] newArray(int size) {
            return new DonationSite[size];
        }
    };

    // Getters and Setters remain the same
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