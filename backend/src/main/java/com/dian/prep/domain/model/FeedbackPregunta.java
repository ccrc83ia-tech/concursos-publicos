package com.dian.prep.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reporte de un aspirante sobre una pregunta que considera problemática. Varios
 * reportes de usuarios distintos sobre la misma pregunta llevan a degradarla
 * (marcarla RECHAZADA) para que deje de servirse en simulacros.
 */
public class FeedbackPregunta {

    private UUID id;
    private UUID preguntaId;
    private UUID usuarioId;
    private TipoFeedback tipo;
    private String comentario;
    private LocalDateTime createdAt;

    /** Motivo del reporte, desde la perspectiva del aspirante. */
    public enum TipoFeedback {
        CLAVE_INCORRECTA,
        ENUNCIADO_CONFUSO,
        VARIAS_CORRECTAS,
        FUERA_DE_TEMA,
        ERROR_REDACCION,
        OTRO
    }

    public static FeedbackPregunta crear(UUID preguntaId, UUID usuarioId,
                                         TipoFeedback tipo, String comentario) {
        FeedbackPregunta f = new FeedbackPregunta();
        f.preguntaId = preguntaId;
        f.usuarioId  = usuarioId;
        f.tipo       = tipo;
        f.comentario = comentario;
        f.createdAt  = LocalDateTime.now();
        return f;
    }

    public UUID getId()                       { return id; }
    public void setId(UUID id)                { this.id = id; }
    public UUID getPreguntaId()               { return preguntaId; }
    public void setPreguntaId(UUID p)         { this.preguntaId = p; }
    public UUID getUsuarioId()                { return usuarioId; }
    public void setUsuarioId(UUID u)          { this.usuarioId = u; }
    public TipoFeedback getTipo()             { return tipo; }
    public void setTipo(TipoFeedback t)       { this.tipo = t; }
    public String getComentario()             { return comentario; }
    public void setComentario(String c)       { this.comentario = c; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }
}
