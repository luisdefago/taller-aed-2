package redsens.registro;

import redsens.modelo.Estacion;

/**
 * Desafío opcional: hashing abierto (encadenamiento). Cada celda tiene una lista enlazada con nodos propios.
 * Una colisión agrega un nodo a la lista y eliminar borra el nodo de verdad (sin lápidas).
 * Acá el factor de carga puede pasar de 1. Sondeo = nodo examinado (mínimo 1, el acceso a la celda).
 */
public class TablaEncadenada implements TablaHash {
    public static final double UMBRAL_DEFECTO = 1.0;

    private static class Nodo {
        Estacion estacion; Nodo sig;
        Nodo(Estacion e, Nodo s) { estacion = e; sig = s; }
    }

    private Nodo[] tabla;
    private int cantidad;
    private final double umbral;
    private final FuncionHash funcion;
    private final Estadisticas stats = new Estadisticas();
    private boolean enRehash = false;

    public TablaEncadenada(int capacidadInicial) { this(capacidadInicial, UMBRAL_DEFECTO, FuncionHash.POR_DEFECTO); }

    public TablaEncadenada(int capacidadInicial, double umbral, FuncionHash f) {
        this.tabla = new Nodo[capacidadInicial];
        this.umbral = umbral;
        this.funcion = f;
    }

    private int base(String c) { return funcion.hash(c) % tabla.length; }

    @Override
    public void insertar(Estacion e) {
        insertarInterno(e);
        if (factorCarga() > umbral) rehash();
    }

    private void insertarInterno(Estacion e) {
        int pos = base(e.getCodigo());
        int sondeos = 0;
        boolean huboColision = false;
        for (Nodo n = tabla[pos]; n != null; n = n.sig) {
            sondeos++;
            if (n.estacion.getCodigo().equals(e.getCodigo())) { n.estacion = e; cuenta(sondeos, huboColision); return; }
            if (enRehash) stats.colisionesRehash++; else stats.colisiones++;
            huboColision = true;
        }
        tabla[pos] = new Nodo(e, tabla[pos]);   // se agrega al frente: O(1)
        cantidad++;
        cuenta(Math.max(1, sondeos), huboColision);
    }

    private void cuenta(int sondeos, boolean huboColision) {
        if (enRehash) return;
        stats.insertar++;
        stats.sondeosInsertar += sondeos;
        if (huboColision) stats.insercionesConColision++;
    }

    private void rehash() {
        Nodo[] vieja = tabla;
        tabla = new Nodo[Primos.siguientePrimo(vieja.length * 2 + 1)];
        cantidad = 0;
        stats.rehashings++;
        enRehash = true;
        for (Nodo cab : vieja) for (Nodo n = cab; n != null; n = n.sig) insertarInterno(n.estacion);
        enRehash = false;
    }

    @Override
    public Estacion buscar(String codigo) {
        int sondeos = 0;
        for (Nodo n = tabla[base(codigo)]; n != null; n = n.sig) {
            sondeos++;
            if (n.estacion.getCodigo().equals(codigo)) {
                stats.buscarOk++; stats.sondeosBuscarOk += sondeos;
                return n.estacion;
            }
        }
        stats.buscarFail++; stats.sondeosBuscarFail += Math.max(1, sondeos);
        return null;
    }

    @Override
    public boolean eliminar(String codigo) {
        int pos = base(codigo), sondeos = 0;
        Nodo ant = null;
        for (Nodo n = tabla[pos]; n != null; ant = n, n = n.sig) {
            sondeos++;
            if (n.estacion.getCodigo().equals(codigo)) {
                if (ant == null) tabla[pos] = n.sig; else ant.sig = n.sig;
                cantidad--;
                stats.eliminar++; stats.sondeosEliminar += sondeos;
                return true;
            }
        }
        stats.eliminar++; stats.sondeosEliminar += Math.max(1, sondeos);
        return false;
    }

    @Override public void vaciar() { tabla = new Nodo[tabla.length]; cantidad = 0; }
    @Override public int size() { return cantidad; }
    @Override public int capacidad() { return tabla.length; }
    @Override public double factorCarga() { return (double) cantidad / tabla.length; }
    @Override public Estadisticas estadisticas() { return stats; }
    @Override public String nombre() { return "Encadenada (umbral " + umbral + ")"; }

    /** Largo de la lista más larga (el equivalente al cluster de las otras tablas). */
    public int maxCadena() {
        int max = 0;
        for (Nodo cab : tabla) { int l = 0; for (Nodo n = cab; n != null; n = n.sig) l++; max = Math.max(max, l); }
        return max;
    }
}
