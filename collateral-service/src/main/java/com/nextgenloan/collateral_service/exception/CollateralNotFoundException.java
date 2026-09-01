package com.nextgenloan.collateral_service.exception;

public class CollateralNotFoundException extends RuntimeException {
    public CollateralNotFoundException(String message) {
        super(message);
    }
}
