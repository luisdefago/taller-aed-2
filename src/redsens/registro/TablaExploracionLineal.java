package redsens.registro;

/**
 * Exploración lineal: F(i) = i. Si la celda está ocupada se prueba la siguiente, y así.
 * Ventaja: recorre todas las celdas, así que siempre encuentra lugar si queda alguna libre.
 * Desventaja: agrupamiento primario. Los que colisionan quedan pegados formando bloques, y cada
 * clave nueva que cae en un bloque lo agranda.
 */
public class TablaExploracionLineal extends ExploracionTablaHash {
    /** Umbral 0,75, el que usan las lecturas de análisis de tablas hash y de colisiones. */
    public static final double UMBRAL_DEFECTO = 0.75;

    public TablaExploracionLineal(int capacidadInicial) { this(capacidadInicial, UMBRAL_DEFECTO, FuncionHash.POR_DEFECTO); }

    public TablaExploracionLineal(int capacidadInicial, double umbral, FuncionHash f) { super(capacidadInicial, umbral, f); }

    @Override protected long desplazamiento(int i) { return i; }

    @Override public String nombre() { return "Lineal (umbral " + umbral() + ")"; }
}
