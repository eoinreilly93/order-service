package com.shop.generic.orderservice.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.generic.common.auth.MicroserviceAuthorisationService;
import com.shop.generic.common.dtos.OrderCreationDTO;
import com.shop.generic.common.dtos.OrderStatusDTO;
import com.shop.generic.common.dtos.PurchaseProductDTO;
import com.shop.generic.common.enums.OrderStatus;
import com.shop.generic.common.rest.response.RestApiResponse;
import com.shop.generic.common.rest.response.RestApiResponseFactory;
import com.shop.generic.orderservice.dtos.OrderDetailsDTO;
import com.shop.generic.orderservice.services.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = OrderController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@AutoConfigureJsonTesters
@DisplayName("Requests to the orders controller")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<RestApiResponse<?>> jacksonTester;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestApiResponseFactory restApiResponseFactory;

    @MockBean
    private OrderService orderService;

    @MockBean
    private MicroserviceAuthorisationService microserviceAuthorisationService;

    @Test
    @DisplayName("Should create an order")
    void createOrder() throws Exception {

        //Given
        final PurchaseProductDTO purchaseDTO = new PurchaseProductDTO(1, 10, BigDecimal.TEN);
        final PurchaseProductDTO purchaseDTO2 = new PurchaseProductDTO(2, 50,
                BigDecimal.valueOf(49.99));

        final OrderCreationDTO orderCreationDTO = new OrderCreationDTO(
                List.of(purchaseDTO, purchaseDTO2), "London");

        final UUID orderId = UUID.randomUUID();
        final OrderStatusDTO orderStatusDTO = new OrderStatusDTO(orderId,
                OrderStatus.CREATED);

        final RestApiResponse<OrderStatusDTO> mockApiResponse = new RestApiResponse<>(null, null,
                orderStatusDTO,
                LocalDateTime.now());

        given(orderService.createShippingOrder(orderCreationDTO)).willReturn(orderStatusDTO);

        given(restApiResponseFactory.createSuccessResponse(
                any(OrderStatusDTO.class)))
                .willReturn(mockApiResponse);

        //When
        final MockHttpServletResponse response = this.mockMvc.perform(
                        post("/orders").contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderCreationDTO)))
                .andReturn().getResponse();

        //Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getHeader("location")).isEqualTo(
                String.format("http://localhost/orders/%s", orderId));
        assertThat(response.getContentAsString()).isEqualTo(
                jacksonTester.write(mockApiResponse).getJson());
    }

    @Test
    @DisplayName("Should update order status when it has necessary permissions")
    void updateOrderStatus() throws Exception {
        // Given
        final UUID orderId = UUID.randomUUID();
        final OrderStatus newStatus = OrderStatus.SHIPPED;
        final OrderStatusDTO orderStatusDTO = new OrderStatusDTO(orderId, newStatus);

        final RestApiResponse<OrderStatusDTO> mockApiResponse = new RestApiResponse<>(null, null,
                orderStatusDTO, LocalDateTime.now());

        given(orderService.updateOrder(orderId, newStatus)).willReturn(orderStatusDTO);
        given(restApiResponseFactory.createSuccessResponse(any(OrderStatusDTO.class)))
                .willReturn(mockApiResponse);
        given(microserviceAuthorisationService.canServiceUpdateOrderStatus()).willReturn(true);

        // When
        final MockHttpServletResponse response = this.mockMvc.perform(
                        put("/orders/order/{orderId}/{newStatus}", orderId, newStatus)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(
                jacksonTester.write(mockApiResponse).getJson());
    }

    @Test
    @DisplayName("Should fetch order status")
    void getOrderStatus() throws Exception {
        // Given
        final UUID orderId = UUID.randomUUID();
        final OrderDetailsDTO orderDetailsDTO = new OrderDetailsDTO(orderId, BigDecimal.TEN, "1,2",
                OrderStatus.SHIPPED, "London", LocalDateTime.now());

        final RestApiResponse<OrderDetailsDTO> mockApiResponse = new RestApiResponse<>(null, null,
                orderDetailsDTO, LocalDateTime.now());

        given(orderService.fetchOrderDetails(orderId)).willReturn(orderDetailsDTO);
        given(restApiResponseFactory.createSuccessResponse(any(OrderDetailsDTO.class)))
                .willReturn(mockApiResponse);

        // When
        final MockHttpServletResponse response = this.mockMvc.perform(
                        get("/orders/order/{orderId}/status", orderId)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(
                jacksonTester.write(mockApiResponse).getJson());
    }

}