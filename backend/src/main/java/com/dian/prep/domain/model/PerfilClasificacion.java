package com.dian.prep.domain.model;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Perfil de clasificación automática de indicadores de un concurso.
 *
 * <p>El SIMO/CNSC publica convocatorias de muchas entidades, y cada una puede
 * tener una estructura de pruebas distinta (distintos nombres, distintos
 * umbrales, con o sin prueba de integridad). Este perfil parametriza las reglas
 * que antes estaban cableadas a la DIAN, de forma que la misma lógica sirva para
 * cualquier concurso.</p>
 *
 * <p>Las reglas son datos, no código: quien registra el concurso puede ajustar
 * palabras clave de integridad, patrón de nivel y umbrales sin recompilar.</p>
 */
public class PerfilClasificacion {

    /** Palabras clave que, si el nombre del indicador coincide exactamente, lo marcan como INTEGRIDAD. */
    private Set<String> palabrasIntegridad;

    /** Si el nombre contiene esta palabra clave, el indicador se marca como COMPORTAMENTAL. */
    private String palabraComportamental;

    /** Regex para extraer el nivel de competencia del nombre (grupo 1 = número de nivel). */
    private Pattern patronNivel;

    /** Umbral de brecha (%) para indicadores funcionales. */
    private double umbralFuncional;

    /** Umbral de brecha (%) para comportamentales e integridad. */
    private double umbralComportamentalIntegridad;

    private PerfilClasificacion() {
    }

    /**
     * Perfil por defecto, basado en la estructura típica de la CNSC
     * (usado históricamente para la DIAN 2676). Sirve como punto de partida
     * y puede sobrescribirse por concurso.
     */
    public static PerfilClasificacion porDefecto() {
        PerfilClasificacion p = new PerfilClasificacion();
        p.palabrasIntegridad = new LinkedHashSet<>(Set.of(
                "COMPROMISO", "DILIGENCIA", "HONESTIDAD", "JUSTICIA", "RESPETO"));
        p.palabraComportamental = "NIVEL";
        p.patronNivel = Pattern.compile("NIVEL\\s+(\\d)", Pattern.CASE_INSENSITIVE);
        p.umbralFuncional = 70.0;
        p.umbralComportamentalIntegridad = 60.0;
        return p;
    }

    /**
     * Construye un perfil a partir de valores opcionales. Cualquier parámetro
     * nulo o vacío cae al valor del perfil por defecto, de modo que el llamador
     * solo necesita especificar lo que quiere cambiar.
     */
    public static PerfilClasificacion de(Set<String> palabrasIntegridad,
                                         String palabraComportamental,
                                         Double umbralFuncional,
                                         Double umbralComportamentalIntegridad) {
        PerfilClasificacion base = porDefecto();
        PerfilClasificacion p = new PerfilClasificacion();

        if (palabrasIntegridad != null && !palabrasIntegridad.isEmpty()) {
            Set<String> normalizadas = new LinkedHashSet<>();
            for (String palabra : palabrasIntegridad) {
                if (palabra != null && !palabra.isBlank()) {
                    normalizadas.add(palabra.trim().toUpperCase());
                }
            }
            p.palabrasIntegridad = normalizadas.isEmpty() ? base.palabrasIntegridad : normalizadas;
        } else {
            p.palabrasIntegridad = base.palabrasIntegridad;
        }

        p.palabraComportamental = (palabraComportamental != null && !palabraComportamental.isBlank())
                ? palabraComportamental.trim().toUpperCase()
                : base.palabraComportamental;

        p.patronNivel = Pattern.compile(
                Pattern.quote(p.palabraComportamental) + "\\s+(\\d)", Pattern.CASE_INSENSITIVE);

        p.umbralFuncional = (umbralFuncional != null && umbralFuncional > 0)
                ? umbralFuncional : base.umbralFuncional;
        p.umbralComportamentalIntegridad = (umbralComportamentalIntegridad != null && umbralComportamentalIntegridad > 0)
                ? umbralComportamentalIntegridad : base.umbralComportamentalIntegridad;

        return p;
    }

    /** Clasifica un indicador según su nombre. */
    public Item.TipoPrueba clasificar(String nombre) {
        String nombreUpper = nombre == null ? "" : nombre.trim().toUpperCase();
        if (palabrasIntegridad.contains(nombreUpper)) {
            return Item.TipoPrueba.INTEGRIDAD;
        }
        if (nombreUpper.contains(palabraComportamental)) {
            return Item.TipoPrueba.COMPORTAMENTAL;
        }
        return Item.TipoPrueba.FUNCIONAL;
    }

    /** Extrae el nivel de competencia del nombre, o null si no aplica. */
    public Integer extraerNivel(String nombre) {
        if (nombre == null) return null;
        Matcher m = patronNivel.matcher(nombre.toUpperCase());
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    /** Umbral de brecha (%) correspondiente al tipo de prueba. */
    public double umbralPara(Item.TipoPrueba tipo) {
        return tipo == Item.TipoPrueba.FUNCIONAL
                ? umbralFuncional
                : umbralComportamentalIntegridad;
    }

    public Set<String> getPalabrasIntegridad()          { return palabrasIntegridad; }
    public String getPalabraComportamental()            { return palabraComportamental; }
    public double getUmbralFuncional()                  { return umbralFuncional; }
    public double getUmbralComportamentalIntegridad()   { return umbralComportamentalIntegridad; }
}
