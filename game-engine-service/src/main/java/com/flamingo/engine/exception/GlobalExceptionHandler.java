package com.flamingo.engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidMoveException.class)
    public ResponseEntity<ProblemDetail> handleInvalidMove(InvalidMoveException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setType(URI.create("/errors/invalid-move"));
        pd.setTitle("Invalid Move");
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleGameNotFound(GameNotFoundException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("/errors/game-not-found"));
        pd.setTitle("Game Not Found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(GameAlreadyFinishedException.class)
    public ResponseEntity<ProblemDetail> handleGameAlreadyFinished(GameAlreadyFinishedException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("/errors/game-already-finished"));
        pd.setTitle("Game Already Finished");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    /**
     * Maps bean validation failures to HTTP 400 with a map of field-level error messages.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setType(URI.create("/errors/validation"));
        pd.setTitle("Validation Failed");
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid value",
                        (first, second) -> first)
                );
        pd.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(pd);
    }
}
