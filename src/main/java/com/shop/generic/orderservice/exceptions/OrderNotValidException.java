package com.shop.generic.orderservice.exceptions;

public class OrderNotValidException extends RuntimeException {

    public OrderNotValidException(final String message) {
        super(message);
    }

}
