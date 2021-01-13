package tourGuide.controller;

import com.jsoniter.output.JsonStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tourGuide.domain.location.VisitedLocation;
import tourGuide.domain.rewards.Provider;
import tourGuide.domain.user.User;
import tourGuide.domain.user.UserPreferences;
import tourGuide.service.TourGuideService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class TourGuideController {

    @Autowired
    private TourGuideService tourGuideService;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/location")
    public String getLocation(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(visitedLocation.location);
    }

    @RequestMapping("/all-current-locations")
    public String getAllCurrentLocations() {
        return JsonStream.serialize(tourGuideService.getAllUsersLocations());
    }

    @RequestMapping("/rewards")
    public String getRewards(@RequestParam String userName) {
        return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }

    @RequestMapping("/trip-deals")
    public String getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
        return JsonStream.serialize(providers);
    }

    @RequestMapping("/nearby-attractions")
    public String getNearbyAttractions(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(tourGuideService.getClosestAttractions(visitedLocation, getUser(userName)));
    }

    // TODO: Switch back to using this method
//    @GetMapping("/user-preferences")
//    public String getUserPreferences(@RequestParam String userName) {
//        return JsonStream.serialize(tourGuideService.getUserPreferences(userName));
//    }

    @RequestMapping("/user-preferences")
    public String getUserPreferences(@RequestParam String userName) {
        return JsonStream.serialize(tourGuideService.getUserPreferences(getUser(userName)));
    }

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
