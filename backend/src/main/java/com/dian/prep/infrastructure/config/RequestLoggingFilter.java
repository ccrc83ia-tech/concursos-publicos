package com.dian.prep.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Registra una línea por petición HTTP (método, ruta, estado y latencia) y
 * asocia un {@code traceId} a cada request mediante el MDC, de modo que todas
 * las trazas generadas durante esa petición queden correlacionadas.
 *
 * <p>Se ejecuta antes que el resto de filtros (orden más alto) para que el
 * {@code traceId} esté disponible desde el inicio, incluido el filtro JWT.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Reutiliza el traceId entrante (si un gateway/cliente lo envía) o genera uno nuevo.
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().substring(0, 8);
        }
        MDC.put(TRACE_ID, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        long inicio = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duracion = System.currentTimeMillis() - inicio;
            int status = response.getStatus();
            String linea = "{} {} -> {} ({} ms)";
            Object[] args = { request.getMethod(), rutaConQuery(request), status, duracion };
            if (status >= 500) {
                log.error(linea, args);
            } else if (status >= 400) {
                log.warn(linea, args);
            } else {
                log.info(linea, args);
            }
            MDC.remove(TRACE_ID);
        }
    }

    private String rutaConQuery(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Evita ruido de endpoints de infraestructura/documentación.
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/favicon.ico");
    }
}
