package com.itmentorcommunityplatform.profileservice.exception;

import com.itmentorcommunityplatform.profileservice.dto.response.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(InvalidProfileUpdateException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidProfileUpdateException(InvalidProfileUpdateException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleProfileNotFoundException(ProfileNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(AchievementAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAchievementAccessException(AchievementAccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidPaginationException(InvalidPaginationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(MissingTelegramUserIdException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingTelegramUserIdException(MissingTelegramUserIdException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(MissingProfileDetailsException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingProfileDetailsException(MissingProfileDetailsException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(EmptyProfileDetailsException.class)
    public ResponseEntity<ErrorResponseDto> handleEmptyProfileDetailsException(EmptyProfileDetailsException ex) {
        return  ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(InvalidProfileDetailsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidProfileDetailsException(InvalidProfileDetailsException ex) {
        return  ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(ValidationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingHeader(MissingRequestHeaderException ex) {
        log.warn("The required title is missing: {}", ex.getHeaderName());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto("Unauthorized"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleJsonNotValidField(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause != null) {
            log.warn("Invalid request body. CauseType={}, CauseMessage={}",
                    cause.getClass().getSimpleName(), cause.getMessage(), ex);
        } else {
            log.warn("Invalid request body. Cause is null", ex);
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("Invalid request"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleAll(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("Internal server error"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Invalid parameter. name={}, type={}, value={}",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
                ex.getValue());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("Invalid request"));
    }
}