package com.residencial.amenities.infrastructure.web;

import com.residencial.amenities.domain.exception.AmenitiesDomainException;
import com.residencial.amenities.domain.exception.AmenityNotFoundException;
import com.residencial.amenities.domain.exception.BookingConflictException;
import com.residencial.amenities.domain.exception.DelinquentHouseException;
import com.residencial.amenities.domain.exception.DuplicateAmenityCodeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AmenityNotFoundException.class)
    public ProblemDetail handleAmenityNotFound(AmenityNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Amenity Not Found");
        problemDetail.setType(URI.create("urn:problem-type:amenity-not-found"));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        return problemDetail;
    }

    @ExceptionHandler(DuplicateAmenityCodeException.class)
    public ProblemDetail handleDuplicateAmenityCode(DuplicateAmenityCodeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Duplicate Amenity Code");
        problemDetail.setType(URI.create("urn:problem-type:duplicate-amenity-code"));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        return problemDetail;
    }

    @ExceptionHandler(BookingConflictException.class)
    public ProblemDetail handleBookingConflict(BookingConflictException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Booking Slot Conflict");
        problemDetail.setType(URI.create("urn:problem-type:booking-conflict"));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        return problemDetail;
    }

    @ExceptionHandler(DelinquentHouseException.class)
    public ProblemDetail handleDelinquentHouse(DelinquentHouseException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problemDetail.setTitle("House Delinquent - Booking Forbidden");
        problemDetail.setType(URI.create("urn:problem-type:delinquent-house-blocked"));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        return problemDetail;
    }

    @ExceptionHandler(AmenitiesDomainException.class)
    public ProblemDetail handleDomainException(AmenitiesDomainException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Amenities Domain Violation");
        problemDetail.setType(URI.create("urn:problem-type:amenities-domain-error"));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Invalid Argument");
        problemDetail.setType(URI.create("urn:problem-type:invalid-argument"));
        return problemDetail;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Invalid State");
        problemDetail.setType(URI.create("urn:problem-type:invalid-state"));
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed for one or more fields");
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("urn:problem-type:validation-error"));

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        problemDetail.setProperty("errors", fieldErrors);
        return problemDetail;
    }
}
