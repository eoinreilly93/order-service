package com.shop.generic.orderservice.cucumber.stepdefinitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.generic.common.entities.Order;
import com.shop.generic.orderservice.cucumber.configurations.CucumberSpringConfiguration;
import com.shop.generic.orderservice.repositories.OrderRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Slf4j
public class ApiStepDefs extends CucumberSpringConfiguration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult mvcResult;
    private MockHttpServletResponse response;

    @Given("initial setup is complete")
    public void initialSetupIsComplete() {
//        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
//                .apply(SecurityMockMvcConfigurers.springSecurity()).build();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Given("the service is up and running")
    public void setup() throws Exception {
//        this.mockMvc.perform(get("/actuator/health").contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("UP"));
    }

    @And("now is {string}")
    public void nowIs(final String time) {
        //TODO: Implement once clock is setup
    }

    @When("a POST request is sent to {string} with data")
    public void aPOSTRequestIsSentToWithData(final String resource, final String body)
            throws Exception {
        mvcResult = this.mockMvc.perform(MockMvcRequestBuilders.post(resource)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andReturn();
    }

    @Then("a response is generated with a {int} status and a body similar to")
    public void aResponseIsGeneratedWithAStatusAndABodySimilarToAndContainsHeaders(
            final int expectedStatus,
            final String expectedBody) throws Exception {
        response = mvcResult.getResponse();
        log.info(response.getContentAsString());
        assertEquals(expectedStatus, response.getStatus());
        JSONAssert.assertEquals(expectedBody, response.getContentAsString(), new CustomComparator(
                JSONCompareMode.LENIENT,
                new Customization("timestamp", (o1, o2) -> true),
                new Customization("result.orderId", (o1, o2) -> true)
        ));
    }

    @And("contains headers")
    public void theResponseContainsHeaders(final DataTable dataTable) {
        final Map<String, String> expectedHeaders = dataTable.asMap(String.class, String.class);
        expectedHeaders.forEach(
                (headerName, headerValue) -> assertNotNull(response.getHeader(headerName)));
    }

    @And("an order is saved to the database")
    public void aOrderIsSavedToTheDatabase() throws UnsupportedEncodingException, JSONException {
        final JSONObject jsonObject = new JSONObject(response.getContentAsString());
        final UUID orderId = UUID.fromString(
                jsonObject.getJSONObject("result").getString("orderId"));
        final Optional<Order> order = this.orderRepository.findByOrderId(orderId);
        assertTrue(order.isPresent());
    }
}
