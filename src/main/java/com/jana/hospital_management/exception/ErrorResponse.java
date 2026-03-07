package com.jana.hospital_management.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {

    private final LocalDateTime timeStamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String traceId;
    private final Map<String, String> validationErrors;

    public ErrorResponse(
            LocalDateTime timeStamp,
            int status,
            String error,
            String message,
            String path,
            String traceId,
            Map<String, String> validationErrors
    ) {
        this.timeStamp = timeStamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.traceId = traceId;
        this.validationErrors = validationErrors;
    }

    public LocalDateTime getTimeStamp() { return timeStamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public String getTraceId() { return traceId; }
    public Map<String, String> getValidationErrors() { return validationErrors; }

}
