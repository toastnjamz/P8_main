package tourGuide.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tourGuide.domain.location.*;
import tourGuide.domain.rewards.Provider;
import tourGuide.domain.rewards.ProviderListWrapper;
import tourGuide.domain.user.User;
import tourGuide.domain.user.UserPreferences;
import tourGuide.domain.user.UserReward;
import tourGuide.repository.TestUserRepository;
import tourGuide.tracker.Tracker;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final RewardsService rewardsService;
	public final Tracker tracker;
	boolean testMode = true;

	private TestUserRepository testUserRepository;
	private final RestTemplate restTemplate;

	private int numberOfClosestAttractions = 5;

	@Autowired
	public TourGuideService(RewardsService rewardsService, TestUserRepository testUserRepository, RestTemplate restTemplate) {
		this.rewardsService = rewardsService;
		this.testUserRepository = testUserRepository;
		this.restTemplate = restTemplate;

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			testUserRepository.initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this, rewardsService);
		addShutDownHook();
	}

	public User getUser(String userName) {
		return testUserRepository.getInternalUserMap().get(userName);
	}

	public List<User> getAllUsers() {
		return testUserRepository.getInternalUserMap().values().stream().collect(Collectors.toList());
	}

	public void addUser(User user) {
		if(!testUserRepository.getInternalUserMap().containsKey(user.getUserName())) {
			testUserRepository.getInternalUserMap().put(user.getUserName(), user);
		}
	}

	public VisitedLocation getUserLocation(User user) throws ExecutionException, InterruptedException {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
				user.getLastVisitedLocation() :
				trackUserLocation(user);
		return visitedLocation;
	}

	public Map<String, Location> getAllUsersLocations() {
		Map<String, Location> allUsersLocations = new HashMap<String, Location>();
		for (User user : getAllUsers()) {
			allUsersLocations.put(user.getUserId().toString(), (user.getVisitedLocations().size() > 0) ?
					user.getLastVisitedLocation().location : null);
		}
		return allUsersLocations;
	}

	public VisitedLocation trackUserLocation(User user) throws InterruptedException {
		VisitedLocation visitedLocation = new VisitedLocation();
		String requestURI = "http://localhost:8082/user-location?userId=" + user.getUserId();

		visitedLocation = restTemplate.getForObject(requestURI, VisitedLocation.class);
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public List<Provider> getTripDeals(User user) {
		int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();

		List<Provider> providers;
		String uri = "http://localhost:8084/trip-deals?tripPricerApiKey=" + testUserRepository.getTripPricerApiKey() +
				"&userId=" + user.getUserId() + "&numberOfAdults=" + user.getUserPreferences().getNumberOfAdults() + "&numberOfChildren=" +
				user.getUserPreferences().getNumberOfChildren() + "&tripDuration=" + user.getUserPreferences().getTripDuration() +
				"&cumulativeRewardPoints=" + cumulativeRewardPoints;

		ProviderListWrapper providerListWrapper = restTemplate.getForObject(uri, ProviderListWrapper.class);

		providers = providerListWrapper.getProviderList();
		user.setTripDeals(providers);
		return providers;
	}

	public List<NearbyAttraction> getClosestAttractions(VisitedLocation visitedLocation, User user) {
		List<Attraction> attractions;
		List<NearbyAttraction> nearbyAttractions = new ArrayList<>();
		Map<Double, Attraction> attractionsMap = new TreeMap<>();

		AttractionListWrapper attractionListWrapper = restTemplate.getForObject("http://localhost:8082/attractions", AttractionListWrapper.class);
		attractions = attractionListWrapper.getAttractionList();

		for (Attraction attraction : attractions) {
			attractionsMap.put(rewardsService.getDistance(attraction, visitedLocation.location), attraction);
		}

		attractionsMap.forEach((distance, attraction) -> {
			if(nearbyAttractions.size() < numberOfClosestAttractions) {
				NearbyAttraction nearbyAttraction = new NearbyAttraction();
				nearbyAttraction.setAttractionName(attraction.attractionName);
				nearbyAttraction.setAttractionLocation(new Location(attraction.longitude, attraction.latitude));
				nearbyAttraction.setUserLocation(visitedLocation.location);
				nearbyAttraction.setAttractionDistance(distance);
				nearbyAttraction.setAttractionRewardPoints(rewardsService.getRewardPoints(attraction, user));
				nearbyAttractions.add(nearbyAttraction);
			}
		});
		return nearbyAttractions;
	}

    public UserPreferences getUserPreferences(User user) {
        if(testUserRepository.getInternalUserMap().containsKey(user.getUserName())) {
            return testUserRepository.getInternalUserMap().get(user.getUserName()).getUserPreferences();
        }
        return null;
    }

	public void setUserPreferences(String userName, UserPreferences userPreferences) {
		User user = getUser(userName);
		user.setUserPreferences(userPreferences);
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}
}
