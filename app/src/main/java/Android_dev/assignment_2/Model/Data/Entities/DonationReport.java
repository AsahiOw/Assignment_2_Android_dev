package Android_dev.assignment_2.Model.Data.Entities;

import java.util.Date;
import java.util.Map;

public class DonationReport {
    private String id;
    private Date startDate;
    private Date endDate;
    private String siteId;  // Optional, null means all sites
    private int totalDonors;
    private Map<String, Double> bloodTypeVolumes;  // Blood type -> Volume in mL
    private int completedDonations;
    private int cancelledDonations;
    private int noShows;
    private Date generatedAt;

    // Constructors, getters, setters

    public DonationReport(String id, Date startDate, Date endDate, String siteId, int totalDonors, Map<String, Double> bloodTypeVolumes, int completedDonations, int cancelledDonations, int noShows, Date generatedAt) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.siteId = siteId;
        this.totalDonors = totalDonors;
        this.bloodTypeVolumes = bloodTypeVolumes;
        this.completedDonations = completedDonations;
        this.cancelledDonations = cancelledDonations;
        this.noShows = noShows;
        this.generatedAt = generatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public int getTotalDonors() {
        return totalDonors;
    }

    public void setTotalDonors(int totalDonors) {
        this.totalDonors = totalDonors;
    }

    public Map<String, Double> getBloodTypeVolumes() {
        return bloodTypeVolumes;
    }

    public void setBloodTypeVolumes(Map<String, Double> bloodTypeVolumes) {
        this.bloodTypeVolumes = bloodTypeVolumes;
    }

    public int getCompletedDonations() {
        return completedDonations;
    }

    public void setCompletedDonations(int completedDonations) {
        this.completedDonations = completedDonations;
    }

    public int getCancelledDonations() {
        return cancelledDonations;
    }

    public void setCancelledDonations(int cancelledDonations) {
        this.cancelledDonations = cancelledDonations;
    }

    public int getNoShows() {
        return noShows;
    }

    public void setNoShows(int noShows) {
        this.noShows = noShows;
    }

    public Date getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Date generatedAt) {
        this.generatedAt = generatedAt;
    }
}