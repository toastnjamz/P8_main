package tourGuide.domain.location;

import java.util.Date;
import java.util.UUID;

public class VisitedLocation {

    public UUID userId;
    public Location location;
    public Date timeVisited;

    public VisitedLocation(UUID userId, Location location, Date timeVisited) {
        this.userId = userId;
        this.location = location;
        this.timeVisited = timeVisited;
    }

    public VisitedLocation() {}
}
