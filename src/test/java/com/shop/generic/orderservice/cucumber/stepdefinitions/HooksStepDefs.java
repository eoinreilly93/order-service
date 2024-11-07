package com.shop.generic.orderservice.cucumber.stepdefinitions;

import com.shop.generic.orderservice.cucumber.configurations.CucumberSpringConfiguration;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HooksStepDefs extends CucumberSpringConfiguration {

    @Before
    public void before() {
        log.info("In HooksStepDefs.before()");
    }

    @After
    public void after() {
        log.info("In HooksStepDef.after()");
    }

}
