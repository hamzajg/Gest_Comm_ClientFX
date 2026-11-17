package com.xdev.gest_comm_clientfx.api;

/** Error talking to the PyGestComm backend (network or HTTP status). */
public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
