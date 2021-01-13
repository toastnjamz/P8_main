package tourGuide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tourGuide.domain.location.Attraction;
import tourGuide.domain.location.AttractionListWrapper;
import tourGuide.domain.location.Location;
import tourGuide.domain.location.VisitedLocation;
import tourGuide.domain.user.User;
import tourGuide.domain.user.UserReward;

import java.util.List;

@Service
public class RewardsService {

	private final Logger logger = LoggerFactory.getLogger(RewardsService.class);
	private final RestTemplate restTemplate;

	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
	private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;

	@Autowired
	public RewardsService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	public void calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();

		List<Attraction> attractions;

		AttractionListWrapper attractionListWrapper = restTemplate.getForObject("http://localhost:8082/attractions", AttractionListWrapper.class);
		attractions = attractionListWrapper.getAttractionList();

		for(VisitedLocation visitedLocation : userLocations) {
			for(Attraction attraction : attractions) {
				if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
					if(nearAttraction(visitedLocation, attraction)) {
						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}
				}
			}
		}
	}

	public int getRewardPoints(Attraction attraction, User user) {
		String uri = "http://localhost:8083/reward-points?attractionId=" + attraction.attractionId +  "&userId=" + user.getUserId();
		Integer requestResult = restTemplate.getForObject(uri, Integer.class);
		int rewardsPoints = requestResult;
		return rewardsPoints;
	}

	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}

	public boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}

	public double getDistance(Location loc1, Location loc2) {
		double lat1 = Math.toRadians(loc1.latitude);
		double lon1 = Math.toRadians(loc1.longitude);
		double lat2 = Math.toRadians(loc2.latitude);
		double lon2 = Math.toRadians(loc2.longitude);

		double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
				+ Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		double nauticalMiles = 60 * Math.toDegrees(angle);
		double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
		return statuteMiles;
	}

}
