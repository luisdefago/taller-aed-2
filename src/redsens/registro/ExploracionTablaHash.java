package redsens.registro;

import redsens.modelo.Estacion;

/**
 * Tabla hash cerrada (direccionamiento abierto). Todo el algoritmo está acá; lo único que cambia entre
 * lineal y cuadrática es desplazamiento(i), que define cada subclase.
 *
 * El i-ésimo sondeo de una clave con hash H cae en (H + F(i)) mod capacidad:
 *   lineal: F(i) = i      cuadrática: F(i) = i²
 *
 * Una celda puede estar vacía (null), activa o borrada (lápida: activo == false).
 */
public abstract class ExploracionTablaHash implements TablaHash {
    protected EntradaHash[] tabla;
    private int activos;    // estaciones vigentes
    private int borrados;   // lápidas
    private final double umbral;
    private final FuncionHash funcion;
    private final Estadisticas stats = new Estadisticas();
    private boolean enRehash = false;   // true mientras se reinserta en un rehash (contadores aparte)

    /**
     * @param capacidadInicial tamaño inicial (el del grupo; conviene primo)
     * @param umbral           factor de carga máximo: al superarlo se hace rehashing
     * @param funcion          función de hash a usar
     */
    protected ExploracionTablaHash(int capacidadInicial, double umbral, FuncionHash funcion) {
        if (capacidadInicial < 2) throw new IllegalArgumentException("capacidad >= 2");
        if (umbral <= 0 || umbral >= 1) throw new IllegalArgumentException("umbral debe estar en (0,1)");
        this.tabla = new EntradaHash[capacidadInicial];
        this.umbral = umbral;
        this.funcion = funcion;
    }

    /** F(i): desplazamiento del i-ésimo sondeo. */
    protected abstract long desplazamiento(int i);

    /** Posición del i-ésimo sondeo (long porque i² puede desbordar un int). */
    private int posicion(int base, int i) {
        return (int) ((base + desplazamiento(i)) % tabla.length);
    }

    private int base(String clave) { return funcion.hash(clave) % tabla.length; }

    // ---------------------------------------------------------------- INSERTAR
    @Override
    public void insertar(Estacion e) {
        insertarInterno(e);
        // Después de insertar se mira el factor de carga; si pasa el umbral, se hace rehash.
        if (factorCarga() > umbral) rehash();
    }

    /** Inserta sin controlar el umbral (lo usan insertar y el rehash). */
    private void insertarInterno(Estacion e) {
        String clave = e.getCodigo();
        int base = base(clave);
        int primerBorrado = -1;   // primera lápida vista: se reutiliza si la clave no existe
        int sondeos = 0;
        boolean huboColision = false;
        for (int i = 0; i < tabla.length; i++) {
            int pos = posicion(base, i);
            sondeos++;
            EntradaHash en = tabla[pos];
            if (en == null) {
                // Celda vacía: la clave no está. Va en la primera lápida (si hubo) o en esta celda.
                int destino = primerBorrado >= 0 ? primerBorrado : pos;
                colocar(destino, e);
                contarInsercion(sondeos, huboColision);
                return;
            }
            if (en.activo) {
                if (en.estacion.getCodigo().equals(clave)) {   // clave repetida: se sobrescribe
                    en.estacion = e;
                    contarInsercion(sondeos, huboColision);
                    return;
                }
                contarColision();                              // celda ocupada por otra clave
                huboColision = true;
            } else if (primerBorrado < 0) {
                primerBorrado = pos;
            }
        }
        // No apareció ninguna celda vacía (en cuadrática pasa si la carga es muy alta).
        // Si hay una lápida se usa; si no, se agranda la tabla y se reintenta.
        if (primerBorrado >= 0) {
            colocar(primerBorrado, e);
            contarInsercion(sondeos, huboColision);
        } else {
            rehash();
            insertarInterno(e);
        }
    }

    private void colocar(int pos, Estacion e) {
        if (tabla[pos] != null && !tabla[pos].activo) borrados--;   // se pisa una lápida
        tabla[pos] = new EntradaHash(e);
        activos++;
    }

    private void contarInsercion(int sondeos, boolean huboColision) {
        if (enRehash) return;                  // las reinserciones del rehash no cuentan como inserciones
        stats.insertar++;
        stats.sondeosInsertar += sondeos;
        if (huboColision) stats.insercionesConColision++;
    }

    private void contarColision() {
        if (enRehash) stats.colisionesRehash++; else stats.colisiones++;
    }

    // ----------------------------------------------------------------- REHASH
    /**
     * Rehash: se crea una tabla del doble de tamaño (llevado al siguiente primo) y se reinserta cada
     * entrada activa, porque la posición depende de la capacidad. Las lápidas no se reinsertan.
     */
    private void rehash() {
        EntradaHash[] vieja = tabla;
        boolean estabaEnRehash = enRehash;
        tabla = new EntradaHash[Primos.siguientePrimo(vieja.length * 2 + 1)];
        activos = 0;
        borrados = 0;
        stats.rehashings++;
        enRehash = true;
        for (EntradaHash en : vieja) {
            if (en != null && en.activo) insertarInterno(en.estacion);
        }
        enRehash = estabaEnRehash;
    }

    // ------------------------------------------------------------------ BUSCAR
    @Override
    public Estacion buscar(String codigo) {
        int base = base(codigo);
        int sondeos = 0;
        for (int i = 0; i < tabla.length; i++) {
            int pos = posicion(base, i);
            sondeos++;
            EntradaHash en = tabla[pos];
            if (en == null) break;   // celda vacía: la clave no está
            if (en.activo && en.estacion.getCodigo().equals(codigo)) {
                stats.buscarOk++;
                stats.sondeosBuscarOk += sondeos;
                return en.estacion;
            }
            // Las lápidas y las celdas con otra clave no cortan la búsqueda.
        }
        stats.buscarFail++;
        stats.sondeosBuscarFail += sondeos;
        return null;
    }

    // ---------------------------------------------------------------- ELIMINAR
    /** Eliminación perezosa: la celda no se vacía (cortaría las cadenas), se marca como borrada. */
    @Override
    public boolean eliminar(String codigo) {
        int base = base(codigo);
        int sondeos = 0;
        boolean ok = false;
        for (int i = 0; i < tabla.length; i++) {
            int pos = posicion(base, i);
            sondeos++;
            EntradaHash en = tabla[pos];
            if (en == null) break;
            if (en.activo && en.estacion.getCodigo().equals(codigo)) {
                en.activo = false;
                en.estacion = null;
                activos--;
                borrados++;
                ok = true;
                break;
            }
        }
        stats.eliminar++;
        stats.sondeosEliminar += sondeos;
        return ok;
    }

    @Override
    public void vaciar() {
        tabla = new EntradaHash[tabla.length];
        activos = 0;
        borrados = 0;
    }

    // -------------------------------------------------------------- CONSULTAS
    @Override public int size() { return activos; }
    @Override public int capacidad() { return tabla.length; }
    /** Las lápidas cuentan porque alargan las búsquedas igual que un elemento vivo. Solo el rehash las saca. */
    @Override public double factorCarga() { return (double) (activos + borrados) / tabla.length; }
    @Override public Estadisticas estadisticas() { return stats; }
    public double umbral() { return umbral; }
    public int borrados() { return borrados; }

    /** Índice donde está la clave, o -1. Sirve para las pruebas y para mostrar trazas. */
    public int posicionDe(String codigo) {
        int base = base(codigo);
        for (int i = 0; i < tabla.length; i++) {
            int pos = posicion(base, i);
            EntradaHash en = tabla[pos];
            if (en == null) return -1;
            if (en.activo && en.estacion.getCodigo().equals(codigo)) return pos;
        }
        return -1;
    }

    /** Largo del mayor grupo de celdas ocupadas seguidas. Muestra el agrupamiento primario. */
    public int maxCluster() {
        int max = 0, actual = 0;
        for (EntradaHash en : tabla) {
            actual = (en != null) ? actual + 1 : 0;
            max = Math.max(max, actual);
        }
        return max;
    }
}
