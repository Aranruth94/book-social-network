package com.piotr.book_network.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum BusinessErrorCode {
    NO_CODE(0, NOT_IMPLEMENTED, "No specific error code provided"),
    INCORRECT_CURRENT_PASSWORD(300, BAD_REQUEST, "The current password provided is incorrect"),
    NEW_PASSWORD_DOES_NOT_MATCH(301, BAD_REQUEST, "The new password does not match the confirmation password"),
    ACCOUNT_LOCKED(302, FORBIDDEN, "The user account is locked"),
    ACCOUNT_DISABLED(303, FORBIDDEN, "The user account is disabled"),
    BAD_CREDENTIALS(304, FORBIDDEN, "The login or password provided is incorrect"),
    HTTP_METHOD_NOT_SUPPORTED(305, METHOD_NOT_ALLOWED, "The HTTP method used is not supported"),
    MEDIA_TYPE_NOT_SUPPORTED(306, UNSUPPORTED_MEDIA_TYPE, "The media type provided is not supported"),
    REQUEST_BODY_NOT_READABLE(307, BAD_REQUEST, "The request body could not be read"),
    MISSING_REQUEST_PARAMETER(308, BAD_REQUEST, "A required request parameter is missing"),
    NO_HANDLER_FOUND(309, NOT_FOUND, "No handler found for the requested URL"),
    NO_RESOURCE_FOUND(310, NOT_FOUND, "The requested resource could not be found"),
    DATA_INTEGRITY_VIOLATION(311, CONFLICT, "The request could not be completed due to a conflict with the current state of the resource");

    private final int code;
    private final String description;
    private final HttpStatus httpStatus;

    BusinessErrorCode(int code, HttpStatus httpStatus, String description) {
        this.code = code;
        this.description = description;
        this.httpStatus = httpStatus;
    }
}
