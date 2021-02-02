package tourGuide.tracker;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tourGuide.domain.user.User;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;

import java.util.List;
import java.util.concurrent.*;

public class Tracker extends Thread {

	private Logger logger = LoggerFactory.getLogger(Tracker.class);
	private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final TourGuideService tourGuideService;
	private final RewardsService rewardsService;
	private boolean stop = false;

//	int processors = Runtime.getRuntime().availableProcessors();

	public Tracker(TourGuideService tourGuideService, RewardsService rewardsService) {
		this.tourGuideService = tourGuideService;
		this.rewardsService = rewardsService;

		executorService.submit(this);
	}

	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	}

	@Override
	public void run() {
		StopWatch stopWatch = new StopWatch();
		while(true) {
			if(Thread.currentThread().isInterrupted() || stop) {
				logger.debug("Tracker stopping");
				break;
			}

			List<User> users = tourGuideService.getAllUsers();
			logger.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();

			users.forEach(u -> {
				try {
					tourGuideService.trackUserLocation(u);
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});

//			ExecutorService executorService = Executors.newFixedThreadPool(100);
//
//			users.forEach(u -> {
//				CompletableFuture.supplyAsync(() -> tourGuideService.trackUserLocation(u), executorService)
//						.thenAccept(visitedLocation -> {rewardsService.calculateRewards(u);});
//			});


//			ForkJoinPool forkJoinPool = new ForkJoinPool(100);
//
//			users.forEach(u -> {
//				CompletableFuture.runAsync(() -> tourGuideService.trackUserLocation(u), forkJoinPool)
//						.thenAccept(r -> rewardsService.calculateRewards(u));
//			});

			stopWatch.stop();
			logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
			stopWatch.reset();
			try {
				logger.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(trackingPollingInterval);
			} catch (InterruptedException e) {
				break;
			}
		}
	}
}
