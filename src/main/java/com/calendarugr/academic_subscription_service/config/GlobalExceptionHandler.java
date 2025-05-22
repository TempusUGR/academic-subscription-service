package com.calendarugr.academic_subscription_service.config;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.calendarugr.academic_subscription_service.dtos.ErrorResponseDTO;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(org.springframework.web.bind.MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingRequestHeaderException(org.springframework.web.bind.MissingRequestHeaderException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponseDTO("MissingRequestHeader", "Missing required header: " + e.getHeaderName()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponseDTO("RuntimeException", "An unexpected error occurred: " + e.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponseDTO> handleIOException(IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponseDTO("IOException", "An error occurred while processing the request: " + e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponseDTO("IllegalArgumentException", "Invalid input: " + e.getMessage()));
    }

    @ExceptionHandler(com.fasterxml.jackson.core.JsonProcessingException.class)
    public ResponseEntity<ErrorResponseDTO> handleJsonProcessingException(com.fasterxml.jackson.core.JsonProcessingException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponseDTO("JsonProcessingException", "Error processing JSON: " + e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAllUncaughtException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponseDTO("Exception", "An unexpected error occurred: " + e.getMessage()));
    }
}