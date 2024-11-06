package com.shop.generic.orderservice.controllers;

import com.shop.generic.common.auth.MicroserviceAuthorisationService;
import com.shop.generic.common.dtos.OrderCreationDTO;
import com.shop.generic.common.dtos.OrderStatusDTO;
import com.shop.generic.common.enums.OrderStatus;
import com.shop.generic.common.rest.response.RestApiResponse;
import com.shop.generic.common.rest.response.RestApiResponseFactory;
import com.shop.generic.orderservice.dtos.OrderDetailsDTO;
import com.shop.generic.orderservice.services.OrderService;
import java.net.URI;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {

    private final RestApiResponseFactory restApiResponseFactory;
    private final OrderService orderService;
    private final MicroserviceAuthorisationService microserviceAuthorisationService;

    public OrderController(final RestApiResponseFactory restApiResponseFactory,
            final OrderService orderService,
            final MicroserviceAuthorisationService microserviceAuthorisationService) {
        this.restApiResponseFactory = restApiResponseFactory;
        this.orderService = orderService;
        this.microserviceAuthorisationService = microserviceAuthorisationService;
    }

    @PostMapping
    public ResponseEntity<RestApiResponse<OrderStatusDTO>> createOrder(
            @RequestBody final OrderCreationDTO orderCreationDTO) {
        log.info("Creating order");
        final OrderStatusDTO responseDTO = this.orderService.createShippingOrder(
                orderCreationDTO);

        final URI newOrderLocationUri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{orderId}")
                .buildAndExpand(responseDTO.orderId())
                .toUri();
        log.info("Created order with order id: {}", responseDTO.orderId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(newOrderLocationUri)
                .body(this.restApiResponseFactory.createSuccessResponse(responseDTO));
    }

    /**
     * TODO: Security concern, how can I protect this endpoint (and all of them for that fact) from someone just hitting
     * this endpoint and updating an order status, when in fact it shouldn't be updated
     * <p>
     * Can be controller at multiple levels e.g. this service should only allow requests from shipping-service, but what
     * if you want support team or someone to be able to hit in the event of an emergency
     */
    @PutMapping("/order/{orderId}/{newStatus}")
    public ResponseEntity<RestApiResponse<OrderStatusDTO>> updateOrderStatus(
            @PathVariable final UUID orderId,
            @PathVariable final OrderStatus newStatus) {
        log.info("Updating status for order {}", orderId);
        if (!this.microserviceAuthorisationService.canServiceUpdateOrderStatus()) {
            log.warn("Microservice is not authorized to update order status");
            throw new IllegalArgumentException(
                    "Microservice is not authorized to update order status");
        }

        final OrderStatusDTO responseDTO = this.orderService.updateOrder(orderId, newStatus);
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.restApiResponseFactory.createSuccessResponse(responseDTO));
    }

    @GetMapping("/order/{orderId}/status")
    public ResponseEntity<RestApiResponse<OrderDetailsDTO>> getOrderStatus(
            @PathVariable final UUID orderId) {
        log.info("Fetching order  status for order {}", orderId);
        final OrderDetailsDTO responseDTO = this.orderService.fetchOrderDetails(orderId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.restApiResponseFactory.createSuccessResponse(responseDTO));
    }
    //TODO: Implement an order details API that contains order status and all the order details such as products, cost etc.

}
