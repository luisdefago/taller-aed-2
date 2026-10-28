package redsens.registro;

import redsens.modelo.Estacion;

/** Operaciones de la tabla hash de estaciones. La clave es el código. */
public interface TablaHash {
    /** Inserta la estación; si ya existe una con el mismo código, la sobrescribe. */
    void insertar(Estacion e);

    /** @return la estación con ese código, o null si no existe (el menú decide qué mostrar). */
    Estacion buscar(String codigo);

    /** Eliminación perezosa (marca como borrada). @return true si existía. */
    boolean eliminar(String codigo);

    /** Saca todos los elementos y deja la misma capacidad. */
    void vaciar();

    /** Cantidad de estaciones (no cuenta las borradas). */
    int size();

    int capacidad();

    /** Factor de carga = celdas usadas / capacidad. */
    double factorCarga();

    Estadisticas estadisticas();

    String nombre();
}
