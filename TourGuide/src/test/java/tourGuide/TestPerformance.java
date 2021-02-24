package tourGuide;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;
import org.junit.Ignore;
import org.springframework.web.client.RestTemplate;
import tourGuide.domain.location.Attraction;
import tourGuide.domain.location.VisitedLocation;
import tourGuide.domain.user.User;
import tourGuide.helper.InternalTestHelper;
import tourGuide.repository.TestUserRepository;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;

import java.util.Date;
import java.util.List;
import java.util.concurrent.*;


import static org.junit.Assert.assertTrue;

public class TestPerformance {

	/*
	 * A note on performance improvements:
	 *
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *
	 *
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent.
	 *
	 *     These are performance metrics that we are trying to hit:
	 *
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 *
	 *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

//	@Ignore
	@Test
	public void highVolumeTrackLocation() throws ExecutionException, InterruptedException {
		RestTemplate restTemplate = new RestTemplate();
		RewardsService rewardsService = new RewardsService(restTemplate);
		TestUserRepository testUserRepository = new TestUserRepository();

		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(rewardsService, testUserRepository, restTemplate);

		List<User> allUsers = tourGuideService.getAllUsers();
		ExecutorService executorService = Executors.newFixedThreadPool(32);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		// Run just trackUserLocation functionality for each user in allUsers on a concurrent thread
		for (User user : allUsers) {
			Runnable runnable = () -> {
				VisitedLocation visitedLocation;
				String requestURI = "http://localhost:8082/user-location?userId=" + user.getUserId();
				visitedLocation = restTemplate.getForObject(requestURI, VisitedLocation.class);
				user.addToVisitedLocations(visitedLocation);
			};
			executorService.execute(runnable);
		}
		executorService.shutdown();
		executorService.awaitTermination(15, TimeUnit.MINUTES);

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	@Ignore
	@Test
	public void highVolumeGetRewards() {
		RestTemplate restTemplate = new RestTemplate();
		RewardsService rewardsService = new RewardsService(restTemplate);
		TestUserRepository testUserRepository = new TestUserRepository();

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(rewardsService, testUserRepository, restTemplate);
		tourGuideService.tracker.stopTracking();

		// Subbing in the first Attraction in the list of attractions
		Attraction attraction = new Attraction("Disneyland", "Anaheim", "CA", 33.817595D, -117.922008D);
		List<User> allUsers = tourGuideService.getAllUsers();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		ExecutorService executorService = Executors.newFixedThreadPool(32);

		try {
			// Run just calculateRewards functionality for each user in allUsers on a concurrent thread
			allUsers.forEach((user) -> {
				Runnable runnable = () -> {
					user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
					rewardsService.calculateRewards(user);
					assertTrue(user.getUserRewards().size() > 0);
				};
				executorService.execute(runnable);
			});
			executorService.shutdown();
			executorService.awaitTermination(20, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		stopWatch.stop();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

}