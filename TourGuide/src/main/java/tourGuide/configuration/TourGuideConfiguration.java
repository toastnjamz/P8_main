package tourGuide.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import tourGuide.repository.TestUserRepository;

@Configuration
public class TourGuideConfiguration {

	@Bean
	public TestUserRepository getTestUserRepository() {
		return new TestUserRepository();
	}

	@Bean
//	@LoadBalanced
	RestTemplate getRestTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder.build();
	}
}
