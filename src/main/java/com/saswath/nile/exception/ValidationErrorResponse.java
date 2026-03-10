package com.saswath.nile.exception;

import java.time.LocalDateTime;
import java.util.List;

public class ValidationErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private List<String> errors;

    public ValidationErrorResponse(int status, String error, List<String> errors) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.errors = errors;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public List<String> getErrors() { return errors; }
}