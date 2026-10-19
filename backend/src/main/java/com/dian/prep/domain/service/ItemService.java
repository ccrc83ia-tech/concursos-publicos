package com.dian.prep.domain.service;

import com.dian.prep.domain.model.Item;
import com.dian.prep.domain.model.Item.TipoPrueba;
import com.dian.prep.domain.model.Item.DominioPrevio;
import com.dian.prep.domain.model.PerfilClasificacion;
import com.dian.prep.domain.port.in.GestionarItemsUseCase;
import com.dian.prep.domain.port.out.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ItemService implements GestionarItemsUseCase {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item crear(CrearItemCommand cmd) {
        Item item = Item.crear(cmd.concursoId(), cmd.nombre(), cmd.tipoPrueba());
        item.setDescripcion(cmd.descripcion());
        item.setNivelCompetencia(cmd.nivelCompetencia());
        item.setPesoPorcentual(cmd.pesoPorcentual() != null ? cmd.pesoPorcentual() : 0.0);
        item.setPreguntasSolicitadas(
                cmd.preguntasSolicitadas() != null ? cmd.preguntasSolicitadas()
                        : cmd.dominioPrevio() != null ? cmd.dominioPrevio().sugerirCantidadPreguntas()
                        : 50);
        item.setDominioPrevio(cmd.dominioPrevio() != null ? cmd.dominioPrevio() : DominioPrevio.NO_SE);
        return itemRepository.guardar(item);
    }

    @Override
    public List<Item> parsarYCrear(ParsarIndicadoresCommand cmd) {
        // El perfil de clasificación se arma a partir del comando. Si el usuario
        // no envía reglas propias, se usa el perfil por defecto. Así el mismo
        // parser sirve para cualquier concurso del SIMO, no solo la DIAN.
        PerfilClasificacion perfil = PerfilClasificacion.de(
                cmd.palabrasIntegridad(),
                cmd.palabraComportamental(),
                cmd.umbralFuncional(),
                cmd.umbralComportamentalIntegridad());

        List<String> lineas = Arrays.stream(cmd.textoIndicadores().split("[\\r\\n]+"))
                .map(String::trim)
                .filter(l -> !l.isBlank())
                .toList();

        List<Item> items = new ArrayList<>();
        int orden = 0;
        for (String linea : lineas) {
            TipoPrueba tipo = perfil.clasificar(linea);
            Integer nivel   = perfil.extraerNivel(linea);
            String nombre   = normalizarNombre(linea);

            Item item = Item.crear(cmd.concursoId(), nombre, tipo);
            item.setNivelCompetencia(nivel);
            item.setOrden(orden++);
            item.setUmbralBrecha(perfil.umbralPara(tipo));
            items.add(item);
        }
        return itemRepository.guardarTodos(items);
    }

    @Override
    public List<Item> listarPorConcurso(UUID concursoId) {
        return itemRepository.buscarPorConcursoId(concursoId);
    }

    @Override
    public Item actualizar(UUID itemId, CrearItemCommand cmd) {
        Item item = itemRepository.buscarPorId(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Ítem no encontrado: " + itemId));
        if (cmd.nombre() != null)              item.setNombre(cmd.nombre());
        if (cmd.descripcion() != null)         item.setDescripcion(cmd.descripcion());
        if (cmd.tipoPrueba() != null)          item.setTipoPrueba(cmd.tipoPrueba());
        if (cmd.nivelCompetencia() != null)    item.setNivelCompetencia(cmd.nivelCompetencia());
        if (cmd.pesoPorcentual() != null)      item.setPesoPorcentual(cmd.pesoPorcentual());
        if (cmd.preguntasSolicitadas() != null) item.setPreguntasSolicitadas(cmd.preguntasSolicitadas());
        if (cmd.dominioPrevio() != null)       item.setDominioPrevio(cmd.dominioPrevio());
        return itemRepository.guardar(item);
    }

    @Override
    public void eliminar(UUID itemId) {
        itemRepository.eliminar(itemId);
    }

    // --- Normalización del nombre ---

    /** Capitaliza correctamente el nombre del indicador para presentación */
    private String normalizarNombre(String original) {
        // Mantiene el nombre original con capitalización Title Case
        return Arrays.stream(original.split("\\s+"))
                .map(w -> w.length() > 1
                        ? Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase()
                        : w.toUpperCase())
                .reduce((a, b) -> a + " " + b)
                .orElse(original);
    }
}
