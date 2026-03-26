package ru.yandex.practicum.commerce.warehouse.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.interaction.api.exception.BadRequestException;
import ru.yandex.practicum.commerce.interaction.api.exception.ServiceApiException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ServiceApiException.class)
    public ResponseEntity<ServiceApiException> handleServiceException(ServiceApiException exception) {
        return ResponseEntity.status(exception.getStatus()).body(exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ServiceApiException> handleValidationException(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = fieldError == null ? "Validation failed" : fieldError.getField() + ": " + fieldError.getDefaultMessage();
        BadRequestException badRequestException = new BadRequestException(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(badRequestException);
    }

    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<ServiceApiException> handleBadRequest(Exception exception) {
        BadRequestException badRequestException = new BadRequestException(exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(badRequestException);
    }
}
