package com.shop.generic.orderservice.cucumber.configurations;

import com.shop.generic.common.CommonKafkaConsumerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({CommonKafkaConsumerAutoConfiguration.class, TestContext.class})
@ActiveProfiles("cucumber")
@AutoConfigureWireMock(port = 0) //Random port
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
//@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class CucumberSpringConfiguration {

    @Container
    protected static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            "postgres:14.8");

    @Container
    protected static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.4.4"));

    static {
        postgreSQLContainer.start();
        kafkaContainer.start();
    }

    @DynamicPropertySource
    static void overrideProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @DynamicPropertySource
    static void configureDatabaseProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

}
