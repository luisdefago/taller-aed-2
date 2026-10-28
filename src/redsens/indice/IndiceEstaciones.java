package redsens.indice;

import java.util.List;
import redsens.modelo.Estacion;

/** Operaciones del índice ordenado de estaciones. La clave es el código. */
public interface IndiceEstaciones {
    /**
     * Inserta la estación; si ya existe una con el mismo código, la sobrescribe.
     */
    void insertar(Estacion e);

    /** @return la estación con ese código, o null si no existe. */
    Estacion buscar(String codigo);

    /** @return true si existía y fue eliminada. */
    boolean eliminar(String codigo);

    /** Saca todos los elementos. */
    void vaciar();

    /** Cantidad de estaciones. */
    int size();

    /** Todas las estaciones ordenadas por código (recorrido inorden). */
    List<Estacion> listarEnOrden();

    /** Estaciones con código entre desde y hasta, ambos incluidos, en orden. */
    List<Estacion> listarPorRango(String desde, String hasta);

    /** Estaciones críticas (la definición sale del enunciado). */
    List<Estacion> detectarCriticas();

    /** Altura del árbol, para comparar ABB y AVL en M6. */
    int altura();

    String nombre();
}