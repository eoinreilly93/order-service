package com.shop.generic.orderservice.rest.exceptionhandlers;

import com.shop.generic.common.rest.response.RestApiResponse;
import com.shop.generic.common.rest.response.RestApiResponseFactory;
import com.shop.generic.orderservice.exceptions.OrderNotValidException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class OrderExceptionHandlerControllerAdvice {

    private final RestApiResponseFactory restApiResponseFactory;

    public OrderExceptionHandlerControllerAdvice(
            final RestApiResponseFactory restApiResponseFactory) {
        this.restApiResponseFactory = restApiResponseFactory;
    }

    @ExceptionHandler(OrderNotValidException.class)
    public ResponseEntity<RestApiResponse> handleOrderNotValidException(
            final OrderNotValidException e) {
        log.error("Responding with bad request as the order is not valid", e);
        return ResponseEntity.badRequest()
                .body(restApiResponseFactory.createErrorResponse(e.getMessage()));
    }

}
