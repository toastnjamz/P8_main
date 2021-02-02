package tourGuide.controller;

import com.jsoniter.output.JsonStream;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tourGuide.domain.location.VisitedLocation;
import tourGuide.domain.rewards.Provider;
import tourGuide.domain.user.User;
import tourGuide.domain.user.UserPreferences;
import tourGuide.service.TourGuideService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@Api(description="Main application methods for TourGuide")
public class TourGuideController {

    @Autowired
    private TourGuideService tourGuideService;

    /**
     * Loads the home screen
     * @return Welcome message
     */
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * Gets a user's last-visited location
     * @param userName
     * @return location
     */
    @RequestMapping("/location")
    public String getLocation(@RequestParam String userName) throws ExecutionException, InterruptedException {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(visitedLocation.location);
    }

    /**
     * Gets all users' last-visited locations
     * @return a list of locations
     */
    @RequestMapping("/all-current-locations")
    public String getAllCurrentLocations() {
        return JsonStream.serialize(tourGuideService.getAllUsersLocations());
    }

    /**
     * Gets a user's list of rewards
     * @param userName
     * @return a list of user rewards
     */
    @RequestMapping("/rewards")
    public String getRewards(@RequestParam String userName) {
        return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }

    /**
     * Gets available trip deals for a user
     * @param userName
     * @return list of trip deals
     */
    @RequestMapping("/trip-deals")
    public String getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
        return JsonStream.serialize(providers);
    }

    /**
     * Gets a list of five attractions for a user
     * @param userName
     * @return list of nearest attractions
     */
    @RequestMapping("/nearby-attractions")
    public String getNearbyAttractions(@RequestParam String userName) throws ExecutionException, InterruptedException {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(tourGuideService.getClosestAttractions(visitedLocation, getUser(userName)));
    }

    /**
     * Gets a user's preferences
     * @param userName
     * @return user preferences
     */
    @RequestMapping("/user-preferences")
    public UserPreferences getUserPreferences(@RequestParam String userName) {
        return tourGuideService.getUserPreferences(getUser(userName));
    }

    /**
     * Posts changes to a user's preferences
     * @param userName
     * @param userPreferences
     * @param response
     */
    @PostMapping("/user-preferences")
    public void postUserPreferences(@RequestParam String userName, @RequestBody UserPreferences userPreferences,
                                    HttpServletResponse response) {
        tourGuideService.setUserPreferences(userName, userPreferences);
        response.setStatus(201);
    }

    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }

}
