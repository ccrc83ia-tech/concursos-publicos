package com.dian.prep.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
    int            status,
    String         error,
    String         mensaje,
    List<String>   detalles,
    LocalDateTime  timestamp
) {
    public static ApiErrorResponse of(int status, String error, String mensaje) {
        return new ApiErrorResponse(status, error, mensaje, List.of(), LocalDateTime.now());
    }

    public static ApiErrorResponse of(int status, String error, String mensaje, List<String> detalles) {
        return new ApiErrorResponse(status, error, mensaje, detalles, LocalDateTime.now());
    }
}
