package com.piotr.book_network.exception;

import com.piotr.book_network.exception.ExceptionResponse.ExceptionResponseBuilder;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.piotr.book_network.exception.BusinessErrorCode.*;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleLockedException(LockedException exp) {
        return buildErrorResponse(UNAUTHORIZED, ACCOUNT_LOCKED, exp.getMessage());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleDisabledException(DisabledException exp) {
        return buildErrorResponse(UNAUTHORIZED, ACCOUNT_DISABLED, exp.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleBadCredentialsException() {
        return buildErrorResponse(UNAUTHORIZED, BAD_CREDENTIALS, "Login and / or Password is incorrect");
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleMessagingException(MessagingException exp) {
        return buildErrorResponse(INTERNAL_SERVER_ERROR, null, exp.getMessage());
    }

    @ExceptionHandler(ActivationTokenException.class)
    public ResponseEntity<ExceptionResponse> handleActivationTokenException(ActivationTokenException exp) {
        return buildErrorResponse(BAD_REQUEST, null, exp.getMessage());
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<ExceptionResponse> handleException(OperationNotPermittedException exp) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ExceptionResponse.builder()
                                .error(exp.getMessage())
                                .build()
                );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exp) {
        return buildErrorResponse(METHOD_NOT_ALLOWED, HTTP_METHOD_NOT_SUPPORTED, "HTTP method not supported: " + exp.getMethod());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException exp) {
        return buildErrorResponse(UNSUPPORTED_MEDIA_TYPE, MEDIA_TYPE_NOT_SUPPORTED, "Media type not supported: " + exp.getContentType());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException exp) {
        return buildErrorResponse(BAD_REQUEST, REQUEST_BODY_NOT_READABLE, "Request body is not readable");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException exp) {
        return buildErrorResponse(BAD_REQUEST, MISSING_REQUEST_PARAMETER, "Missing request parameter: " + exp.getParameterName());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNoHandlerFoundException(NoHandlerFoundException exp) {
        return buildErrorResponse(NOT_FOUND, NO_HANDLER_FOUND, "No handler found for request: " + exp.getRequestURL());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNoResourceFoundException(NoResourceFoundException exp) {
        return buildErrorResponse(NOT_FOUND, NO_RESOURCE_FOUND, exp.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionResponse> handleDataIntegrityViolationException(DataIntegrityViolationException exp) {
        return buildErrorResponse(CONFLICT, DATA_INTEGRITY_VIOLATION, exp.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exp) {
        Set<String> errors = new HashSet<>();
        exp.getBindingResult()
                .getAllErrors()
                .forEach(
                        error -> errors.add(error.getDefaultMessage()
                        )
                );
        return ResponseEntity.status(BAD_REQUEST).body(ExceptionResponse.builder().validationErrors(errors).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exp) {
        logger.error("Exception occurred: ", exp);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(
                ExceptionResponse.builder()
                        .businessErrorDescription("Internal error, please contact the admin")
                        .error(exp.getMessage())
                        .errors(Map.of("exceptionType", exp.getClass().getSimpleName()))
                        .build()
        );
    }

    private ResponseEntity<ExceptionResponse> buildErrorResponse(HttpStatus status, BusinessErrorCode errorCode, String errorMessage) {
        ExceptionResponse response = buildExceptionResponse(errorCode, errorMessage);
        return ResponseEntity.status(status).body(response);
    }

    private ExceptionResponse buildExceptionResponse(BusinessErrorCode errorCode, String errorMessage) {
        ExceptionResponseBuilder responseBuilder = ExceptionResponse.builder().error(errorMessage);
        if (errorCode != null) {
            responseBuilder.businessErrorCode(errorCode.getCode()).businessErrorDescription(errorCode.getDescription());
        }
        return responseBuilder.build();
    }
}