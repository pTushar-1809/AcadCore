package com.acadcore.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
public ResponseEntity<?> handleException(Exception ex) {

    ex.printStackTrace();

    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                    "message", ex.getMessage() != null
                            ? ex.getMessage()
                            : "Unexpected server error"
            ));
        }
}