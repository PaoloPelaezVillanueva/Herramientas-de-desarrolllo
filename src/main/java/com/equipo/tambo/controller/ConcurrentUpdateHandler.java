package com.equipo.tambo.controller;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ConcurrentUpdateHandler {
    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail onConcurrentUpdate() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Otro usuario modificó los datos. Vuelve a consultar e inténtalo de nuevo.");
    }
}
