package com.shop.generic.orderservice.cucumber.configurations;

import com.shop.generic.common.CommonKafkaConsumerAutoConfiguration;
import com.shop.generic.orderservice.AbstractBaseIntegrationTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({CommonKafkaConsumerAutoConfiguration.class, TestContext.class})
@ActiveProfiles("cucumber")
@AutoConfigureWireMock(port = 0) //Random port
//@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class CucumberSpringConfiguration extends AbstractBaseIntegrationTest {

}
