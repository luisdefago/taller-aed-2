package redsens.registro;

/**
 * Exploración cuadrática: F(i) = i². Si la celda está ocupada se prueba H+1², H+2², H+3², ... (módulo capacidad).
 * Los saltos van creciendo, así que los que colisionan se separan y no se forman bloques (agrupamiento primario).
 * Queda un agrupamiento secundario más leve: dos claves con el mismo H siguen la misma secuencia.
 *
 * Umbral 0,5: es el valor que da la teoría estándar (no aparece en las lecturas). Con capacidad prima y carga
 * menor a 0,5 siempre se encuentra una celda libre. Con más carga puede no encontrarse aunque queden celdas libres.
 */
public class TablaExploracionCuadratica extends ExploracionTablaHash {
    public static final double UMBRAL_DEFECTO = 0.5;

    public TablaExploracionCuadratica(int capacidadInicial) { this(capacidadInicial, UMBRAL_DEFECTO, FuncionHash.POR_DEFECTO); }

    public TablaExploracionCuadratica(int capacidadInicial, double umbral, FuncionHash f) { super(capacidadInicial, umbral, f); }

    @Override protected long desplazamiento(int i) { return (long) i * i; }

    @Override public String nombre() { return "Cuadrática (umbral " + umbral() + ")"; }
}
