package redsens.indice;

import java.util.List;
import redsens.modelo.Estacion;

/**
 * Índice ordenado de estaciones. La clave es el número de estación
 * (Estacion.getNumero()).
 */
public interface IndiceEstaciones {
    /**
     * Inserta la estación; si ya existe una con el mismo número, la sobrescribe.
     */
    void insertar(Estacion e);

    /** @return la estación con ese número, o null si no existe. */
    Estacion buscar(int numero);

    /** @return true si existía y fue eliminada. */
    boolean eliminar(int numero);

    /** Saca todos los elementos. */
    void vaciar();

    int size();

    List<Estacion> inorden();

    List<Estacion> preorden();

    List<Estacion> postorden();

    /** Estaciones con número entre desde y hasta (ambos incluidos), en orden. */
    List<Estacion> listarPorRango(int desde, int hasta);

    /** Estaciones con batería menor a 20, ordenadas por número. Alimenta a M3. */
    List<Estacion> detectarCriticas();

    int altura();

    int contarNodos();

    int contarHojas();

    String nombre();
}