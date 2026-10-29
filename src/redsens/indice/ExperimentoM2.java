package redsens.indice;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import redsens.modelo.Estacion;
import redsens.sim.GeneradorDatos;
import redsens.sim.InformeRendimiento;

/**
 * Experimento obligatorio M2: insertar N estaciones en un ABB y en un AVL,
 * primero en orden ascendente
 * (como llegan del censo) y después en orden aleatorio, y comparar la altura y
 * el tiempo de búsqueda.
 *
 * Los dos árboles reciben exactamente las mismas estaciones (misma semilla);
 * solo cambia el orden de inserción.
 * Búsqueda: se buscan las N claves cargadas, siempre en el mismo orden
 * mezclado, así las seis
 * combinaciones se comparan en igualdad de condiciones.
 */
public class ExperimentoM2 {

    /**
     * Cantidad de pasadas de búsqueda que se promedian (una pasada sola es
     * demasiado corta para medirla).
     */
    private static final int REPETICIONES = 10;

    /** Resultado de una combinación (N, orden, árbol). */
    public static class Fila {
        public int n;
        public String orden; // "ascendente" o "aleatorio"
        public String arbol; // "ABB" o "AVL"
        public int altura; // altura real obtenida
        public int alturaIdeal; // altura mínima posible con n nodos
        public double msInsertar; // tiempo de insertar las N estaciones
        public double msBuscar; // tiempo de una pasada de N búsquedas
        public double nsPorBusqueda; // promedio por búsqueda
    }

    private final long semilla;

    public ExperimentoM2(long semilla) {
        this.semilla = semilla;
    }

    /**
     * Corre el experimento para cada tamaño. La altura es recursiva y el ABB con
     * 10.000 claves en orden
     * tiene 10.000 niveles, así que todo se ejecuta en un hilo con pila grande para
     * no depender
     * del tamaño de pila por defecto de la JVM.
     */
    public List<Fila> ejecutar(int... tamanios) {
        List<Fila> resultado = new ArrayList<>();
        Throwable[] error = new Throwable[1];
        Thread hilo = new Thread(null, () -> {
            try {
                resultado.addAll(correrTodo(tamanios));
            } catch (Throwable t) {
                error[0] = t;
            }
        }, "experimento-m2", 64L * 1024 * 1024);
        hilo.start();
        try {
            hilo.join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (error[0] != null)
            throw new RuntimeException("Falló el experimento M2", error[0]);
        return resultado;
    }

    private List<Fila> correrTodo(int[] tamanios) {
        // Pasada de calentamiento de la JVM (se descarta), para que la primera medición
        // no salga más lenta.
        List<Estacion> calentamiento = new GeneradorDatos(semilla).generarEstaciones(1000);
        correr(new ArbolABB(), calentamiento, calentamiento, "aleatorio");
        correr(new ArbolAVL(), calentamiento, calentamiento, "aleatorio");

        List<Fila> filas = new ArrayList<>();
        for (int n : tamanios) {
            // Mismas estaciones en las dos listas: solo cambia el orden.
            List<Estacion> aleatorias = new GeneradorDatos(semilla).generarEstaciones(n);
            List<Estacion> ordenadas = new GeneradorDatos(semilla).generarEstacionesOrdenadas(n);
            for (String orden : new String[] { "ascendente", "aleatorio" }) {
                List<Estacion> aInsertar = orden.equals("ascendente") ? ordenadas : aleatorias;
                filas.add(correr(new ArbolABB(), aInsertar, aleatorias, orden));
                filas.add(correr(new ArbolAVL(), aInsertar, aleatorias, orden));
            }
        }
        return filas;
    }

    private Fila correr(ArbolBase arbol, List<Estacion> aInsertar, List<Estacion> aBuscar, String orden) {
        Fila f = new Fila();
        f.n = aInsertar.size();
        f.orden = orden;
        f.arbol = arbol.nombre();

        long t0 = System.nanoTime();
        for (Estacion e : aInsertar)
            arbol.insertar(e);
        long t1 = System.nanoTime();

        int encontradas = 0;
        long t2 = System.nanoTime();
        for (int rep = 0; rep < REPETICIONES; rep++) {
            for (Estacion e : aBuscar) {
                if (arbol.buscar(e.getNumero()) != null)
                    encontradas++;
            }
        }
        long t3 = System.nanoTime();

        // Verificación: el árbol tiene las N estaciones y todas se encuentran.
        if (arbol.size() != f.n || encontradas != f.n * REPETICIONES)
            throw new IllegalStateException(f.arbol + " " + orden + ": faltan estaciones (size=" + arbol.size()
                    + ", encontradas=" + encontradas + ")");

        f.altura = arbol.altura();
        f.alturaIdeal = Integer.SIZE - Integer.numberOfLeadingZeros(f.n); // = ceil(log2(n + 1))
        f.msInsertar = (t1 - t0) / 1e6;
        f.msBuscar = (t3 - t2) / 1e6 / REPETICIONES;
        f.nsPorBusqueda = (t3 - t2) / (double) (REPETICIONES * f.n);
        return f;
    }

    /** Tabla de texto para pegar en el informe. */
    public static String formatear(List<Fila> filas, long semilla) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT,
                "  Experimento M2 | semilla %d | búsqueda exitosa de las N claves (promedio de %d pasadas)%n%n",
                semilla, REPETICIONES));
        sb.append(String.format("  %6s %-11s %-5s %7s %7s %12s %11s %13s%n",
                "N", "Orden", "Árbol", "Altura", "Ideal", "ms insertar", "ms buscar", "ns/búsqueda"));
        sb.append("  ").append("-".repeat(81)).append('\n');
        for (Fila f : filas) {
            sb.append(String.format(Locale.ROOT, "  %6d %-11s %-5s %7d %7d %12.2f %11.3f %13.0f%n",
                    f.n, f.orden, f.arbol, f.altura, f.alturaIdeal, f.msInsertar, f.msBuscar, f.nsPorBusqueda));
        }
        sb.append("\n  Ideal = altura mínima posible con N nodos (árbol perfectamente balanceado)\n");
        return sb.toString();
    }

    /** Pasa las filas al informe de rendimiento (M6). */
    public static void alInforme(List<Fila> filas, InformeRendimiento inf) {
        for (Fila f : filas) {
            inf.agregar("M2", "Insertar " + f.n + " " + f.orden, f.arbol, f.msInsertar,
                    "altura=" + f.altura + " ideal=" + f.alturaIdeal);
            inf.agregar("M2", "Buscar " + f.n + " " + f.orden, f.arbol, f.msBuscar,
                    String.format(Locale.ROOT, "ns/búsqueda=%.0f", f.nsPorBusqueda));
        }
    }
}