package redsens.registro;

/**
 * Contadores de la tabla (requisito M1.6).
 * Sondeo = una celda (o nodo) examinada.
 * Colisión = al insertar, se encuentra una celda ocupada por otra clave. Se cuenta cada celda ocupada
 * que se encuentra. Como eso puede dar varias colisiones en una misma inserción, también se cuenta
 * cuántas inserciones tuvieron al menos una colisión.
 * Lo que pasa durante un rehash se cuenta aparte.
 */
public class Estadisticas {
    public long insertar, sondeosInsertar;
    public long buscarOk, sondeosBuscarOk;
    public long buscarFail, sondeosBuscarFail;
    public long eliminar, sondeosEliminar;
    /** Celdas ocupadas por otra clave encontradas al insertar. */
    public long colisiones;
    /** Inserciones que tuvieron al menos una colisión. */
    public long insercionesConColision;
    /** Colisiones durante las reinserciones del rehash. */
    public long colisionesRehash;
    public int rehashings;

    public double promInsertar()   { return prom(sondeosInsertar, insertar); }
    public double promBuscarOk()   { return prom(sondeosBuscarOk, buscarOk); }
    public double promBuscarFail() { return prom(sondeosBuscarFail, buscarFail); }
    public double promEliminar()   { return prom(sondeosEliminar, eliminar); }

    private static double prom(long total, long n) { return n == 0 ? 0 : (double) total / n; }

    /** Pone en cero los contadores de búsqueda para medir una fase aparte. */
    public void reiniciarBusquedas() {
        buscarOk = sondeosBuscarOk = buscarFail = sondeosBuscarFail = 0;
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.ROOT,
            "inserciones=%d (%.2f sondeos c/u) | colisiones=%d en %d inserciones (+%d en rehash) | rehashings=%d | "
            + "búsq. exitosa=%.2f | búsq. fallida=%.2f",
            insertar, promInsertar(), colisiones, insercionesConColision, colisionesRehash, rehashings,
            promBuscarOk(), promBuscarFail());
    }
}
