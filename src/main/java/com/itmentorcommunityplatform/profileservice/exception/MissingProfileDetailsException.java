package com.itmentorcommunityplatform.profileservice.exception;

public class MissingProfileDetailsException extends RuntimeException {
    public MissingProfileDetailsException(String message) {
        super(message);
    }
}
