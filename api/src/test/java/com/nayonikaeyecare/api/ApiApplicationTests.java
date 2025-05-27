package com.nayonikaeyecare.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.nayonikaeyecare.api.config.MongoTestConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(MongoTestConfig.class)
class ApiApplicationTests {

	@Test
	void contextLoads() {
	}
}
