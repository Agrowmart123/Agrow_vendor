package com.agrowmart.exception;

/**
 * Container for all authentication & validation related custom exceptions
 */
public final class AuthExceptions {

    private AuthExceptions() {} // prevent instantiation

    public static class BusinessValidationException extends RuntimeException {
        public BusinessValidationException(String message) {
            super(message);
        }
    }

    public static class DuplicateResourceException extends RuntimeException {
        public DuplicateResourceException(String message) {
            super(message);
        }
    }

    public static class AuthenticationFailedException extends RuntimeException {
        public AuthenticationFailedException(String message) {
            super(message);
        }
    }

    public static class InvalidOtpException extends RuntimeException {
        public InvalidOtpException(String message) {
            super(message);
        }
    }

    public static class FileUploadException extends RuntimeException {
        public FileUploadException(String message) {
            super(message);
        }

        public FileUploadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    
 // In AuthExceptions.java
    public static class CloudinaryOperationException extends RuntimeException {
        public CloudinaryOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}