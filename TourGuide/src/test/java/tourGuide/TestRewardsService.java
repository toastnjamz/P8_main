package tourGuide;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;
import tourGuide.domain.location.Attraction;
import tourGuide.domain.location.Location;
import tourGuide.domain.location.VisitedLocation;
import tourGuide.domain.user.User;
import tourGuide.domain.user.UserReward;
import tourGuide.helper.InternalTestHelper;
import tourGuide.repository.TestUserRepository;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class TestRewardsService {

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
	public void calculateRewards_userHasRewards_rewardsReturned() {
		// arrange
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = new Attraction("Mojave National Preserve", "Kelso", "CA", 35.141689D, -115.510399D);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));

		// act
		rewardsService.calculateRewards(user);

		// assert
		List<UserReward> rewardList = user.getUserRewards();
		assertTrue(rewardList.size() == 1);
	}

	@Test
	public void getRewardPoints_userHasRewards_rewardPointsReturned() {
		// arrange
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		Attraction attraction = new Attraction("Mojave National Preserve", "Kelso", "CA", 35.141689D, -115.510399D);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));

		// act
		int rewardPoints = rewardsService.getRewardPoints(attraction, user);

		// assert
		assertNotNull(rewardPoints);
	}

	@Test
	public void isWithinAttractionProximity_attractionIsWithinProximity_trueReturned() {
		// arrange
		Attraction attraction = new Attraction("Mojave National Preserve", "Kelso", "CA", 35.141689D, -115.510399D);

		// act

		// assert
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}

	@Test
	public void nearAttraction_() {
		// arrange
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		Attraction attraction = new Attraction("Mojave National Preserve", "Kelso", "CA", 35.141689D, -115.510399D);
		VisitedLocation visitedLocation = new VisitedLocation(UUID.randomUUID(), new Location(testUserRepository.generateRandomLatitude(), testUserRepository.generateRandomLongitude()), testUserRepository.getRandomTime());

		// act

		// assert
		assertTrue(rewardsService.nearAttraction(visitedLocation, attraction));
	}

	@Test
	public void nearAllAttractions() {
		// arrange
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);
		InternalTestHelper.setInternalUserNumber(1);

		// act
		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));

		// assert
		assertEquals(26, userRewards.size());
	}

}
