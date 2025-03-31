package com.example.APIRestCoches.excepciones;

public class InvalidRentalException extends RuntimeException {
    public InvalidRentalException(String message) {
        super(message);
    }
}