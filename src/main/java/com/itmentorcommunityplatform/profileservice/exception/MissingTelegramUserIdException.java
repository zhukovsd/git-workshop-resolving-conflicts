package com.itmentorcommunityplatform.profileservice.exception;

public class MissingTelegramUserIdException extends RuntimeException {
    public MissingTelegramUserIdException(String message) {
        super(message);
    }
}
