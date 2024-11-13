package com.shop.generic.orderservice.cucumber.stepdefinitions;

import com.shop.generic.orderservice.cucumber.configurations.CucumberSpringConfiguration;
import com.shop.generic.orderservice.repositories.OrderRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class HooksStepDefs extends CucumberSpringConfiguration {

    @Autowired
    private OrderRepository orderRepository;

    @Before
    public void before() {
        log.info("In HooksStepDefs.before()");
    }

    @After
    public void after() {
        log.info("In HooksStepDef.after()");
        this.orderRepository.deleteAll(); //TODO: Fix this by using @DirtiesContext instead
    }
}
