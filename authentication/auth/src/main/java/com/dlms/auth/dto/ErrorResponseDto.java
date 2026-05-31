package com.dlms.auth.dto;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public class ErrorResponseDto {
    
    private String apiPath; 
    private HttpStatus errorCode; 
    private String errorMsg; 
    private LocalDateTime errorTime; 

    public ErrorResponseDto() {}

    public ErrorResponseDto(String apiPath, HttpStatus errorCode, String errorMsg, LocalDateTime errorTime) {
        this.apiPath = apiPath;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.errorTime = errorTime;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public HttpStatus getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(HttpStatus errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public LocalDateTime getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(LocalDateTime errorTime) {
        this.errorTime = errorTime;
    }
}
