package com.example.quitespotfinder.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ProblemDetail handle(Exception ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof NoSpotFoundException) {
                return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, cause.getMessage());
            }
            cause = cause.getCause();
        }
        logger.error("Request failed", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong while finding spots. Please try again.");
    }
}
