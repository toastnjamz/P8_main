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

//	int processors = Runtime.getRuntime().availableProcessors();

	@Ignore
	@Test
	public void highVolumeTrackLocation() throws ExecutionException, InterruptedException {
		RestTemplate restTemplate = new RestTemplate();
		RewardsService rewardsService = new RewardsService(restTemplate);
		TestUserRepository testUserRepository = new TestUserRepository();

		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(5000);
		TourGuideService tourGuideService = new TourGuideService(rewardsService, testUserRepository, restTemplate);

		List<User> allUsers = tourGuideService.getAllUsers();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

//		for(User user : allUsers) {
//			tourGuideService.trackUserLocation(user);
//		}

		ExecutorService executorService = Executors.newFixedThreadPool(100);

		allUsers.forEach(u -> {
			CompletableFuture.supplyAsync(() -> tourGuideService.trackUserLocation(u), executorService)
					.thenAccept(visitedLocation -> {tourGuideService.completeTrack(u, visitedLocation);});
				});


//		ForkJoinPool forkJoinPool = new ForkJoinPool(100);
//		allUsers.forEach(u -> {
//			CompletableFuture.runAsync(() -> tourGuideService.trackUserLocation(u), forkJoinPool)
//					.thenAccept(v -> rewardsService.calculateRewards(u));
//		});
//
//		forkJoinPool.awaitQuiescence(15,TimeUnit.MINUTES);

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

//	@Ignore
	@Test
	public void highVolumeGetRewards() {
		RestTemplate restTemplate = new RestTemplate();
		RewardsService rewardsService = new RewardsService(restTemplate);
		TestUserRepository testUserRepository = new TestUserRepository();

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(100000);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(rewardsService, testUserRepository, restTemplate);

		//Subbing in the first Attraction in the list of attractions
		Attraction attraction = new Attraction("Disneyland", "Anaheim", "CA", 33.817595D, -117.922008D);
		List<User> allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		allUsers.forEach(u -> rewardsService.calculateRewards(u));

//		ForkJoinPool forkJoinPool = new ForkJoinPool(100);
//		allUsers.forEach(u -> {
//			CompletableFuture.runAsync(() -> tourGuideService.trackUserLocation(u), forkJoinPool)
//					.thenAccept(v -> rewardsService.calculateRewards(u));
//		});

//		forkJoinPool.awaitQuiescence(20,TimeUnit.MINUTES);

		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

}