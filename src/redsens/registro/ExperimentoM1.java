package redsens.registro;

import java.util.ArrayList;
import java.util.List;
import redsens.modelo.Estacion;
import redsens.sim.ConfigGrupo;
import redsens.sim.GeneradorDatos;
import redsens.sim.InformeRendimiento;

/**
 * Experimento obligatorio M1: insertar N estaciones simuladas en cada variante de tabla y comparar
 * colisiones, sondeos promedio de búsqueda exitosa y no exitosa, y rehashings.
 *
 * Búsqueda exitosa: se buscan los N códigos insertados (CBA-0000 a CBA-(N-1)).
 * Búsqueda no exitosa: se buscan N códigos que no existen, con prefijo CBX (CBX-0000 a CBX-(N-1)).
 * Tienen el mismo largo (8 caracteres) que las claves cargadas, aun con N = 10.000.
 * Las dos búsquedas se hacen con la tabla ya completa.
 */
public class ExperimentoM1 {

    /** Resultado de una variante. */
    public static class Fila {
        public String variante;
        public int capInicial, capFinal;
        public double cargaFinal;
        public long colisiones, colisionesRehash;
        public int rehashings;
        public double sondInsertar, sondOk, sondFail;
        public long insercionesConColision;
        public int maxCluster;       // mayor grupo de celdas seguidas (o cadena más larga)
        public double msInsertar, msBuscar;
    }

    private final ConfigGrupo cfg;
    private final int n;

    public ExperimentoM1(ConfigGrupo cfg, int n) { this.cfg = cfg; this.n = n; }

    public List<Fila> ejecutar() {
        // Mismos datos para todas las variantes (misma semilla).
        List<Estacion> estaciones = new GeneradorDatos(cfg.semilla).generarEstaciones(n);
        List<Fila> filas = new ArrayList<>();
        int c = cfg.tamTablaInicial;

        // Pasada de calentamiento de la JVM (se descarta), para que la primera variante no salga más lenta.
        correr(new TablaExploracionLineal(c), estaciones);
        correr(new TablaExploracionCuadratica(c), estaciones);
        correr(new TablaEncadenada(c), estaciones);

        // Variantes obligatorias, con la función de hash por defecto y el umbral de cada una (ver README).
        filas.add(correr(new TablaExploracionLineal(c), estaciones));
        filas.add(correr(new TablaExploracionCuadratica(c), estaciones));
        // Control: lineal con el mismo umbral que la cuadrática (0,5).
        filas.add(correr(new TablaExploracionLineal(c, 0.5, FuncionHash.POR_DEFECTO), estaciones));
        // Desafío opcional: encadenamiento.
        filas.add(correr(new TablaEncadenada(c), estaciones));
        // Contraste 1: Horner puro (sin mezcla).
        Fila puro = correr(new TablaExploracionLineal(c, 0.75, FuncionHash.HORNER), estaciones);
        puro.variante = "Lineal + Horner puro";
        filas.add(puro);
        // Contraste 2: función mala (suma de caracteres).
        Fila mala = correr(new TablaExploracionLineal(c, 0.75, FuncionHash.SUMA), estaciones);
        mala.variante = "Lineal + hash SUMA (mala)";
        filas.add(mala);
        return filas;
    }


    private Fila correr(TablaHash t, List<Estacion> estaciones) {
        Fila f = new Fila();
        f.variante = t.nombre();
        f.capInicial = t.capacidad();

        long t0 = System.nanoTime();
        for (Estacion e : estaciones) t.insertar(e);
        long t1 = System.nanoTime();

        t.estadisticas().reiniciarBusquedas();
        long t2 = System.nanoTime();
        for (Estacion e : estaciones) t.buscar(e.getCodigo());                       // exitosas
        for (int i = 0; i < n; i++) t.buscar(claveAusente(i));                       // no exitosas
        long t3 = System.nanoTime();

        Estadisticas s = t.estadisticas();
        f.capFinal = t.capacidad();
        f.cargaFinal = t.factorCarga();
        f.colisiones = s.colisiones;
        f.insercionesConColision = s.insercionesConColision;
        f.colisionesRehash = s.colisionesRehash;
        f.rehashings = s.rehashings;
        f.sondInsertar = s.promInsertar();
        f.sondOk = s.promBuscarOk();
        f.sondFail = s.promBuscarFail();
        f.maxCluster = (t instanceof ExploracionTablaHash) ? ((ExploracionTablaHash) t).maxCluster()
                     : (t instanceof TablaEncadenada) ? ((TablaEncadenada) t).maxCadena() : 0;
        f.msInsertar = (t1 - t0) / 1e6;
        f.msBuscar = (t3 - t2) / 1e6;
        return f;
    }

    /** Clave que nunca se carga: mismo largo que CBA-NNNN pero con prefijo CBX. */
    static String claveAusente(int numero) { return String.format("CBX-%04d", numero); }

    /**
     * Experimento complementario: sondeos promedio según el factor de carga, con capacidad fija prima
     * y sin rehash, para comparar lineal y cuadrática con la misma carga. Incluye las fórmulas teóricas
     * de la exploración lineal: exitosa ½(1+1/(1-λ)), fallida ½(1+1/(1-λ)²).
     * Si una tabla tuvo que hacer rehash (la cuadrática puede fallar con λ > 0,5) se muestra "n/d".
     */
    public static String curvaCarga(ConfigGrupo cfg) {
        final int cap = 10007;                       // primo; con carga 0,90 son 9.006 claves, todas de 4 dígitos
        final double[] cargas = {0.25, 0.50, 0.75, 0.90};
        int max = (int) (cap * 0.90);
        List<Estacion> est = new GeneradorDatos(cfg.semilla).generarEstaciones(max);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  Sondeos vs factor de carga (capacidad fija %d, sin rehash) - exitosa / fallida%n%n", cap));
        sb.append(String.format("  %-6s %-13s %-13s %-15s %-13s%n", "Carga", "Lineal", "Cuadrática", "Teoría lineal", "Horner puro"));
        sb.append("  ").append("-".repeat(64)).append('\n');
        for (double lam : cargas) {
            int k = (int) (cap * lam);
            sb.append(String.format(java.util.Locale.ROOT, "  %-6.2f %-13s %-13s %-15s %-13s%n", lam,
                medirCarga(new TablaExploracionLineal(cap, 0.99, FuncionHash.POR_DEFECTO), est, k),
                medirCarga(new TablaExploracionCuadratica(cap, 0.99, FuncionHash.POR_DEFECTO), est, k),
                String.format(java.util.Locale.ROOT, "%.2f / %.2f", 0.5 * (1 + 1 / (1 - lam)), 0.5 * (1 + 1 / ((1 - lam) * (1 - lam)))),
                medirCarga(new TablaExploracionLineal(cap, 0.99, FuncionHash.HORNER), est, k)));
        }
        return sb.toString();
    }

    private static String medirCarga(TablaHash t, List<Estacion> est, int k) {
        for (int i = 0; i < k; i++) t.insertar(est.get(i));
        if (t.estadisticas().rehashings > 0) return "n/d (rehash)";
        for (int i = 0; i < k; i++) t.buscar(est.get(i).getCodigo());
        for (int i = 0; i < k; i++) t.buscar(claveAusente(i));
        return String.format(java.util.Locale.ROOT, "%.2f / %.2f", t.estadisticas().promBuscarOk(), t.estadisticas().promBuscarFail());
    }

    /** Tabla de texto para pegar en el informe. */
    public static String formatear(List<Fila> filas, int n, ConfigGrupo cfg) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  Grupo %d | semilla %d | tabla inicial %d | hash Horner+mezcla (salvo indicación)%n%n",
            cfg.grupo, cfg.semilla, cfg.tamTablaInicial));
        sb.append(String.format("  %-26s %7s %5s %9s %8s %8s %3s %6s %7s %5s %6s%n",
            "Variante", "Cap.fin", "Carga", "Colision.", "Ins.col.", "Col.rhsh", "Rhs", "S.exit", "S.fall", "Clust", "ms ins"));
        sb.append("  ").append("-".repeat(101)).append('\n');
        for (Fila f : filas) {
            sb.append(String.format(java.util.Locale.ROOT, "  %-26s %7d %5.2f %9d %8d %8d %3d %6.2f %7.2f %5d %6.1f%n",
                f.variante, f.capFinal, f.cargaFinal, f.colisiones, f.insercionesConColision, f.colisionesRehash, f.rehashings,
                f.sondOk, f.sondFail, f.maxCluster, f.msInsertar));
        }
        sb.append("\n  Colision. = celdas ocupadas encontradas al insertar · Ins.col. = inserciones con al menos una colisión\n");
        sb.append("  Rhs = rehashings · S.exit / S.fall = sondeos promedio de búsqueda exitosa / fallida · Clust = mayor grupo seguido\n");
        return sb.toString();
    }

    /** Pasa las filas al informe de rendimiento (M6). */
    public static void alInforme(List<Fila> filas, int n, InformeRendimiento inf) {
        for (Fila f : filas) {
            inf.agregar("M1", "Insertar " + n + " estaciones", f.variante, f.msInsertar,
                String.format(java.util.Locale.ROOT, "colisiones=%d rehash=%d cap=%d", f.colisiones, f.rehashings, f.capFinal));
            inf.agregar("M1", "Buscar " + n + " ok + " + n + " fallidas", f.variante, f.msBuscar,
                String.format(java.util.Locale.ROOT, "sond.ok=%.2f sond.fail=%.2f", f.sondOk, f.sondFail));
        }
    }
}
