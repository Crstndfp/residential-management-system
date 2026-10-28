package com.residencial.parking.infrastructure.web;

import com.residencial.parking.domain.exception.*;
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

    @ExceptionHandler(NoAvailableParkingSpotException.class)
    public ProblemDetail handleNoAvailableSpot(NoAvailableParkingSpotException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("No Available Parking Spot");
        problemDetail.setType(URI.create("urn:problem-type:no-available-parking-spot"));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        return problemDetail;
    }

    @ExceptionHandler(PlateAlreadyActiveException.class)
    public ProblemDetail handlePlateAlreadyActive(PlateAlreadyActiveException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Plate Already Active In Parking");
        problemDetail.setType(URI.create("urn:problem-type:plate-already-active"));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        return problemDetail;
    }

    @ExceptionHandler(ActiveEntryNotFoundException.class)
    public ProblemDetail handleActiveEntryNotFound(ActiveEntryNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Active Vehicle Entry Not Found");
        problemDetail.setType(URI.create("urn:problem-type:active-entry-not-found"));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        return problemDetail;
    }

    @ExceptionHandler(CovertResidentSuspicionNotMetException.class)
    public ProblemDetail handleCovertResidentThreshold(CovertResidentSuspicionNotMetException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Suspicion Threshold Not Met");
        problemDetail.setType(URI.create("urn:problem-type:suspicion-threshold-not-met"));
        problemDetail.setProperty("errorCode", ex.getErrorCode());
        return problemDetail;
    }

    @ExceptionHandler(ParkingDomainException.class)
    public ProblemDetail handleDomainException(ParkingDomainException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Parking Domain Violation");
        problemDetail.setType(URI.create("urn:problem-type:parking-domain-error"));
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
