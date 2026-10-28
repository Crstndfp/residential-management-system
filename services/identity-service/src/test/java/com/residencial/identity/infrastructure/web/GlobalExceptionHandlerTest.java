package com.residencial.identity.infrastructure.web;

import com.residencial.identity.domain.exception.HouseAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle IdentityDomainException with RFC 7807 ProblemDetail and errorCode")
    void testHandleIdentityDomainException() {
        HouseAlreadyExistsException exception = new HouseAlreadyExistsException("M-10 L-01");

        ProblemDetail problemDetail = exceptionHandler.handleDomainException(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Domain Rule Violation", problemDetail.getTitle());
        assertEquals(URI.create("urn:problem-type:domain-error"), problemDetail.getType());
        assertEquals(exception.getMessage(), problemDetail.getDetail());
        assertEquals("HOUSE_ALREADY_EXISTS", problemDetail.getProperties().get("errorCode"));
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with RFC 7807 ProblemDetail")
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid DPI checksum");

        ProblemDetail problemDetail = exceptionHandler.handleIllegalArgumentException(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Invalid Argument", problemDetail.getTitle());
        assertEquals(URI.create("urn:problem-type:invalid-argument"), problemDetail.getType());
        assertEquals("Invalid DPI checksum", problemDetail.getDetail());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with field errors mapping")
    void testHandleMethodArgumentNotValidException() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "email", "Invalid email format"));
        bindingResult.addError(new FieldError("request", "houseNumber", "House number is required"));

        MethodParameter parameter = new MethodParameter(
                this.getClass().getDeclaredMethod("setUp"), -1
        );
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        ProblemDetail problemDetail = exceptionHandler.handleValidationException(exception);

        assertNotNull(problemDetail);
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Validation Error", problemDetail.getTitle());
        assertEquals(URI.create("urn:problem-type:validation-error"), problemDetail.getType());
        assertEquals("Validation failed for one or more fields", problemDetail.getDetail());

        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) problemDetail.getProperties().get("errors");
        assertNotNull(errors);
        assertEquals("Invalid email format", errors.get("email"));
        assertEquals("House number is required", errors.get("houseNumber"));
    }
}
