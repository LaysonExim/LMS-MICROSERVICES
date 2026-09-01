package com.nextgenloan.API.Gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false",
    "spring.config.import=optional:configserver:",
    "eureka.client.enabled=false",
    "spring.cloud.discovery.client.enabled=false"
})
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
