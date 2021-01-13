package tourGuide.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tourGuide.domain.location.Location;
import tourGuide.domain.location.VisitedLocation;
import tourGuide.domain.user.User;
import tourGuide.helper.InternalTestHelper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.IntStream;

/**********************************************************************************
 *
 * Methods Below: For Internal Testing
 *
 **********************************************************************************/

public class TestUserRepository {

    private Logger logger = LoggerFactory.getLogger(TestUserRepository.class);

    private static final String tripPricerApiKey = "test-server-api-key";

    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    public void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i-> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });
    }

    public double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    public double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    public Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

    public Map<String, User> getInternalUserMap() {
        return internalUserMap;
    }

    public String getTripPricerApiKey() {
        return tripPricerApiKey;
    }
}
