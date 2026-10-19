package com.dian.prep.infrastructure.adapter.in.web;

import com.dian.prep.infrastructure.adapter.in.web.dto.ApiErrorResponse;
import com.dian.prep.infrastructure.config.RequestLoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        log.warn("Validación fallida: {}", detalles);
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(400, "Validación fallida", "Revise los campos enviados", detalles));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        // Error de negocio esperado (datos inválidos): basta WARN sin stacktrace.
        log.warn("Solicitud inválida: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(400, "Solicitud inválida", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiErrorResponse> handleSecurity(SecurityException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(403, "Acceso denegado", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneral(Exception ex) {
        // Error inesperado: se registra el stacktrace completo para poder diagnosticarlo.
        // El traceId permite al usuario reportar el incidente y cruzarlo con los logs.
        String traceId = MDC.get(RequestLoggingFilter.TRACE_ID);
        log.error("Error interno no controlado (traceId={})", traceId, ex);
        String mensaje = traceId != null
                ? "Ocurrió un error inesperado. Referencia: " + traceId
                : "Ocurrió un error inesperado. Intente nuevamente.";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(500, "Error interno", mensaje));
    }
}
