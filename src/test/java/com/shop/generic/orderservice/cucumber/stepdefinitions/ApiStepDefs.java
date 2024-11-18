package com.shop.generic.orderservice.cucumber.stepdefinitions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.generic.common.auth.MicroserviceAuthorisationService;
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
import org.skyscreamer.jsonassert.ArrayValueMatcher;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private MicroserviceAuthorisationService microserviceAuthorisationService;

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

    @Then("a successful response is generated with a {int} status and a body similar to")
    public void aResponseIsGeneratedWithAStatusAndABodySimilarTo(
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

    @Then("an error response is generated with a {int} status and a body similar to")
    public void anErrorResponseIsGeneratedWithAStatusAndABodySimilarTo(
            final int expectedStatus,
            final String expectedBody) throws Exception {
        response = mvcResult.getResponse();
        log.info(response.getContentAsString());
        assertEquals(expectedStatus, response.getStatus());
        JSONAssert.assertEquals(expectedBody, response.getContentAsString(), new CustomComparator(
                JSONCompareMode.LENIENT,
                new Customization("timestamp", (o1, o2) -> true)
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

    @And("an order is not saved to the database")
    public void anOrderIsNotSavedToTheDatabase() {
        assertEquals(0, this.orderRepository.count());
    }

    @And("postgres is up and running")
    public void postgresIsUpAndRunning() {
        assertThat(postgreSQLContainer.isRunning()).isTrue();
    }

    @Given("an order exists in the database with the following data")
    public void anOrderExistsInTheDatabaseWithTheFollowingData(final String json)
            throws JsonProcessingException {
        final Order order = createOrderFromJsonString(json);
        this.orderRepository.save(order);
    }

    @When("a PUT request is sent to {string}")
    public void aPUTRequestIsSentTo(final String url) throws Exception {
        //TODO: Configure authentication properly for these tests
        when(this.microserviceAuthorisationService.canServiceUpdateOrderStatus()).thenReturn(true);
        mvcResult = this.mockMvc.perform(
                        MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();
    }

    @And("the order in the database should be")
    public void theOrderInTheDatabaseShouldBe(final String expectedJson)
            throws JsonProcessingException {
        assertOrderJsonEqualToOrderInDatabase(new JSONObject(expectedJson));
    }

    private Order createOrderFromJsonString(final String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Order.class);
    }

    private void assertOrderJsonEqualToOrderInDatabase(final JSONObject expectedOrderJson)
            throws JsonProcessingException {
        final int id = (int) retrieveJsonItem(expectedOrderJson, "id");
        final Order orderActual = this.orderRepository.findById(id)
                .orElseThrow(() -> new AssertionError(
                        "Expected order with id \"" + id + "\" but none was found"));

        // annoyingly, if expectedOrderJson has auditItems = [] but orderActual has non-zero length auditItems, JSONAssert.assertEquals will throw a divide-by-zero error instead of showing the diff
        assertEquals(expectedOrderJson.getJSONArray("auditItems").length(),
                orderActual.getAuditItems().size());

        JSONAssert.assertEquals(expectedOrderJson.toString(),
                objectMapper.writeValueAsString(orderActual), new CustomComparator(
                        JSONCompareMode.LENIENT,
                        new Customization("timestamp", (o1, o2) -> true),
                        new Customization("lastUpdated", (o1, o2) -> true),
                        new Customization("auditItems", new ArrayValueMatcher<>(
                                new DefaultComparator(JSONCompareMode.LENIENT)))
                ));
    }

    private Object retrieveJsonItem(final JSONObject jsonObject, final String key) {
        return jsonObject.isNull(key) ? null : jsonObject.get(key);
    }
}
