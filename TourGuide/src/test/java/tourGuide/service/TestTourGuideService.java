package tourGuide.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.springframework.web.client.RestTemplate;
import tourGuide.domain.location.Location;
import tourGuide.domain.location.NearbyAttraction;
import tourGuide.domain.location.VisitedLocation;
import tourGuide.domain.rewards.Provider;
import tourGuide.domain.user.User;
import tourGuide.domain.user.UserPreferences;
import tourGuide.domain.user.UserReward;
import tourGuide.helper.InternalTestHelper;
import tourGuide.repository.TestUserRepository;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class TestTourGuideService {

	private RestTemplate restTemplate;
	private RewardsService rewardsService;
	private TestUserRepository testUserRepository;
	private TourGuideService tourGuideService;

	@Before
	public void setup() {
		restTemplate = new RestTemplate();
		rewardsService = new RewardsService(restTemplate);
		testUserRepository = new TestUserRepository();
		tourGuideService = new TourGuideService(rewardsService, testUserRepository, restTemplate);
		InternalTestHelper.setInternalUserNumber(0);
		tourGuideService.tracker.stopTracking();
	}

	@Test
	public void getUser_userExists_userReturned() {
		// arrange
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.addUser(user);

		// act
		User result = tourGuideService.getUser(user.getUserName());

		// assert
		assertEquals(user, result);
	}

	@Test
	public void getUser_userDoesNotExist_nullReturned() {
		// arrange

		// act
		User result = tourGuideService.getUser("fakeUser");

		// assert
		assertNull(result);
	}

	@Test
	public void getAllUsers_usersExists_usersReturned() {
		// arrange
		User user1 = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jane", "111", "jane@tourGuide.com");
		tourGuideService.addUser(user1);
		tourGuideService.addUser(user2);

		// act
		List<User> allUsers = tourGuideService.getAllUsers();

		// assert
		assertTrue(allUsers.contains(user1));
		assertTrue(allUsers.contains(user2));
	}

	@Test
	public void getAllUsers_usersDoNotExist_emptyListReturned() {
		// arrange

		// act
		List<User> allUsers = tourGuideService.getAllUsers();

		// assert
		assertTrue(allUsers.size() == 0);
	}

	@Test
	public void addUser_usersExists_usersReturned() {
		// arrange
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		// act
		User retrievedUser = tourGuideService.getUser(user.getUserName());
		User retrievedUser2 = tourGuideService.getUser(user2.getUserName());

		// assert
		assertEquals(user, retrievedUser);
		assertEquals(user2, retrievedUser2);
	}

	@Test
	public void getUserLocation_userExists_visitedLocationReturned() throws ExecutionException, InterruptedException {
		// arrange
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.addUser(user);

		// act
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

		// assert
		assertTrue(visitedLocation.userId.equals(user.getUserId()));
	}

	@Test
	public void getAllUserLocations_usersExist_visitedLocationsMapReturned() {
		// arrange
		User user1 = new User(UUID.fromString("8dcabd60-a9f5-40b0-8413-49b892449470"), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.fromString("8dcabd60-a9f5-40b0-8413-49b892449471"), "jon2", "000", "jon2@tourGuide.com");
		tourGuideService.addUser(user1);
		tourGuideService.addUser(user2);

		Location location1 = new Location(33.817595D, -117.922008D);
		Location location2 = new Location(33.817596D, -117.922009D);
		user1.addToVisitedLocations(new VisitedLocation(user1.getUserId(), location1, new Date()));
		user2.addToVisitedLocations(new VisitedLocation(user2.getUserId(), location2, new Date()));

		Map<String, Location> allLocationsMap = new HashMap<>();
		allLocationsMap.put(user1.getUserId().toString(), location1);
		allLocationsMap.put(user2.getUserId().toString(), location2);

		// act
		Map<String, Location> resultsMap = tourGuideService.getAllUsersLocations();

		// assert
		assertEquals(allLocationsMap, resultsMap);
	}

	@Ignore
	@Test
	public void getAllUserLocations_usersDoNotExist_emptyMapReturned() {
		// arrange

		// act
		Map<String, Location> resultsMap = tourGuideService.getAllUsersLocations();

		// assert
		assertTrue(resultsMap.size() == 0);
	}

	@Test
	public void trackUserLocation_userExists_visitedLocationMatches() {
		// arrange
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		// act
		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

		// assert
		assertEquals(user.getUserId(), visitedLocation.userId);
	}

	@Test
	public void trackUserLocationConcurrent_userListValid_vistedLocationMatches() throws ExecutionException, InterruptedException {
		// arrange
		List<User> userList = new ArrayList<>();
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		userList.add(user);

		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

		// act
		tourGuideService.trackUserLocationConcurrent(userList);

		// assert
		assertEquals(user.getUserId(), visitedLocation.userId);
	}

	@Test
	public void getUserRewards_usersExistsNoRewards_emptyRewardsListReturned() {
		// arrange
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.addUser(user);

		// act
		List<UserReward> rewardsList = tourGuideService.getUser(user.getUserName()).getUserRewards();

		// assert
		assertEquals(rewardsList.size(), 0);
	}

	public void getTripDeals_userExists_tripDealsReturned() {
		// arrange
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		// act
		List<Provider> providers = tourGuideService.getTripDeals(user);

		// assert
		assertEquals(10, providers.size());
	}

	@Test
	public void getNearbyAttractions_nearbyAttractionsExist_attractionsReturned() {
		// arrange
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = new VisitedLocation(user.getUserId(), new Location(33.817595D, -117.922008D), new Date());
		List<NearbyAttraction> nearbyAttractionList = new ArrayList<>();

		NearbyAttraction nearbyAttraction1 = new NearbyAttraction();
		NearbyAttraction nearbyAttraction2 = new NearbyAttraction();
		NearbyAttraction nearbyAttraction3 = new NearbyAttraction();
		NearbyAttraction nearbyAttraction4 = new NearbyAttraction();
		NearbyAttraction nearbyAttraction5 = new NearbyAttraction();
		nearbyAttractionList.add(nearbyAttraction1);
		nearbyAttractionList.add(nearbyAttraction2);
		nearbyAttractionList.add(nearbyAttraction3);
		nearbyAttractionList.add(nearbyAttraction4);
		nearbyAttractionList.add(nearbyAttraction5);

		// act
		List<NearbyAttraction> resultNearbyAttractionList = tourGuideService.getClosestAttractions(visitedLocation, user);

		// assert
		assertEquals(5, resultNearbyAttractionList.size());
	}

	@Test
	public void getUserPreferences_usersExist_preferencesReturned() {
		// arrange
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setAttractionProximity(100);
		userPreferences.setTripDuration(7);
		userPreferences.setTicketQuantity(3);
		userPreferences.setNumberOfAdults(2);
		userPreferences.setNumberOfChildren(1);
		user.setUserPreferences(userPreferences);

		// act
		UserPreferences resultPreferences = tourGuideService.getUserPreferences(user);

		// assert
		assertEquals(userPreferences, resultPreferences);
	}

	@Test
	public void postUserPreferences_validPreferences_preferencesReturned() {
		// arrange
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.addUser(user);

		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setAttractionProximity(100);
		userPreferences.setTripDuration(7);
		userPreferences.setTicketQuantity(3);
		userPreferences.setNumberOfAdults(2);
		userPreferences.setNumberOfChildren(1);
		user.setUserPreferences(userPreferences);

		// act
		tourGuideService.setUserPreferences(user.getUserName(), userPreferences);

		// assert
		assertEquals(userPreferences, user.getUserPreferences());
	}
}
