package com.example.bugreport.exceptions;

/// Thrown when an invalid log file or image is uploaded
public class InvalidAttachmentException extends RuntimeException {
    public InvalidAttachmentException(String message) {
        super(message);
    }
}