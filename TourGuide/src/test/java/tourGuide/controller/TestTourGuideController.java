package tourGuide.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tourGuide.helper.InternalTestHelper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class TestTourGuideController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    WebApplicationContext webContext;

    @Before
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
    }

    @Before
    public void setupTestHelper() {
        InternalTestHelper internalTestHelper = new InternalTestHelper();
        internalTestHelper.setInternalUserNumber(1);
    }

    @Test
    public void index_statusIsSuccessful() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void getLocation_statusIsSuccessful() throws Exception {
        String userName = "internalUser0";
        mockMvc.perform(get("/location")
                .param("userName", userName))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void getAllCurrentLocations_statusIsSuccessful() throws Exception {
        mockMvc.perform(get("/all-current-locations"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void getRewards_statusIsSuccessful() throws Exception {
        String userName = "internalUser0";
        mockMvc.perform(get("/rewards")
                .param("userName", userName))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void getTripDeals_statusIsSuccessful() throws Exception {
        String userName = "internalUser0";
        mockMvc.perform(get("/trip-deals")
                .param("userName", userName))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void getNearbyAttractions_statusIsSuccessful() throws Exception {
        String userName = "internalUser0";
        mockMvc.perform(get("/nearby-attractions")
                .param("userName", userName))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void getUserPreferences_statusIsSuccessful() throws Exception {
        String userName = "internalUser0";
        mockMvc.perform(get("/user-preferences")
                .param("userName", userName))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void postUserPreferences_statusIsSuccessful() throws Exception {
        String userName = "internalUser0";
        String content = "{\n" +
                "\"attractionProximity\": 55555,\n" +
                "\"currency\": \"USD\",\n" +
                "\"lowerPricePoint\": 0.0,\n" +
                "\"highPricePoint\": 100.0,\n" +
                "\"tripDuration\": 1,\n" +
                "\"ticketQuantity\": 1,\n" +
                "\"numberOfAdults\": 1,\n" +
                "\"numberOfChildren\": 0\n" +
                "}";

        mockMvc.perform(get("/user-preferences")
                .param("userName", userName)
                .content(content)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }
}
