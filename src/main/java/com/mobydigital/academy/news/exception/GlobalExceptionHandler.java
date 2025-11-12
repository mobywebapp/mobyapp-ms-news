package com.mobydigital.academy.news.exception;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(RuntimeException.class)
    @ApiResponse(
            responseCode = "500",
            description = "Unexpected error occurred during webhook processing",
            content = @Content(
                    schema = @Schema(implementation = String.class),
                    examples = @ExampleObject(value = "Error processing webhook: detailed error message")
            )
    )
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        log.error("Error processing webhook: {}", ex.getMessage());
        log.error("StackTrace: {}", Arrays.toString(ex.getStackTrace()));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing webhook: " + ex.getMessage());
    }

}
