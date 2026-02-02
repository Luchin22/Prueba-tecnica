package com.banco.cuenta.exception;

public class MovimientoNotFoundException extends RuntimeException {
    public MovimientoNotFoundException(String message) {
        super(message);
    }
}
