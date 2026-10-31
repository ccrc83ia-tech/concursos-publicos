package com.dian.prep.domain.port.in;

import com.dian.prep.domain.model.Item;

import java.util.List;
import java.util.UUID;

public interface GestionarItemsUseCase {

    record CrearItemCommand(
            UUID    concursoId,
            String  nombre,
            String  descripcion,
            Item.TipoPrueba tipoPrueba,
            Integer nivelCompetencia,
            Double  pesoPorcentual,
            Integer preguntasSolicitadas
    ) {}

    /**
     * Ítem junto con su "dominio" real derivado de los simulacros: el último
     * desempeño registrado para ese ítem (porcentaje de aciertos y nivel de
     * semáforo). Si el usuario aún no ha practicado el tema, {@code desempeno}
     * es null → el dominio se interpreta como 0% ("sin practicar").
     */
    record ItemConDominio(
            Item item,
            com.dian.prep.domain.model.DesempenoItem desempeno
    ) {}

    record ParsearIndicadoresCommand(
            UUID   concursoId,
            String textoIndicadores,          // texto pegado por el usuario
            // Perfil de clasificación opcional (si es null, se usa el perfil por defecto).
            // Permite adaptar el parseo a la estructura de cualquier concurso del SIMO.
            java.util.Set<String> palabrasIntegridad,
            String  palabraComportamental,
            Double  umbralFuncional,
            Double  umbralComportamentalIntegridad
    ) {
        /** Atajo para el caso común: solo el texto, con el perfil por defecto. */
        public ParsearIndicadoresCommand(UUID concursoId, String textoIndicadores) {
            this(concursoId, textoIndicadores, null, null, null, null);
        }
    }

    Item crear(CrearItemCommand command);

    /** Parsea el texto libre con los indicadores y crea los ítems automáticamente */
    List<Item> parsearYCrear(ParsearIndicadoresCommand command);

    List<Item> listarPorConcurso(UUID concursoId);

    /**
     * Lista los ítems de un concurso con su dominio real (último desempeño por
     * ítem del usuario). Para temas sin practicar, el desempeño es null (0%).
     */
    List<ItemConDominio> listarConDominio(UUID concursoId, UUID usuarioId);

    Item actualizar(UUID itemId, CrearItemCommand command);

    void eliminar(UUID itemId);
}
