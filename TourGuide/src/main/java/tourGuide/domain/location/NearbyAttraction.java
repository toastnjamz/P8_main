package tourGuide.domain.location;

public class NearbyAttraction {

    private String attractionName;
    private Location attractionLocation;
    private Location userLocation;
    private Double attractionDistance;
    private int attractionRewardPoints;

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public Location getAttractionLocation() {
        return attractionLocation;
    }

    public void setAttractionLocation(Location attractionLocation) {
        this.attractionLocation = attractionLocation;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public Double getAttractionDistance() {
        return attractionDistance;
    }

    public void setAttractionDistance(Double attractionDistance) {
        this.attractionDistance = attractionDistance;
    }

    public int getAttractionRewardPoints() {
        return attractionRewardPoints;
    }

    public void setAttractionRewardPoints(int attractionRewardPoints) {
        this.attractionRewardPoints = attractionRewardPoints;
    }
}
