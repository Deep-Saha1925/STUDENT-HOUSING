package com.deep.studenthousing.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFound(UserNotFoundException ex, Model model){
        model.addAttribute("errorMessage", ex.getMessage());
        return "error-page";
    }

    // Validation problems we raise ourselves (e.g. "Title is required.") are safe to show.
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadInput(IllegalArgumentException ex, Model model){
        model.addAttribute("errorMessage", ex.getMessage());
        return "error-page";
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleUnauthorizedAction(UnauthorizedActionException ex, Model model){
        model.addAttribute("errorMessage", ex.getMessage());
        return "unauthorized";
    }

    // Without this, an oversized upload surfaces as a bare whitelabel 400 with
    // no indication of what went wrong — the owner just sees "Bad Request".
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(MaxUploadSizeExceededException ex, Model model){
        log.warn("Upload rejected — file too large", ex);
        model.addAttribute("errorMessage",
                "Those images are too large to upload. Please use photos under 10MB each " +
                        "(and under 50MB total), then try again.");
        return "error-page";
    }

    // Catch-all so unexpected failures during property save (image upload issues,
    // geocoding errors, etc.) show a readable message and get logged with a full
    // stack trace, instead of a whitelabel page that tells us nothing.
    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, Model model){
        // Details go to the server log only. Exception class names and messages can
        // reveal table names, library versions or other internals to an attacker.
        log.error("Unhandled exception while processing request", ex);
        model.addAttribute("errorMessage", "Something went wrong. Please try again, or contact the admin if it keeps happening.");
        return "error-page";
    }
}