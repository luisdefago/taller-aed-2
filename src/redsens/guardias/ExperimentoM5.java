package redsens.guardias;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import redsens.sim.InformeRendimiento;

/**
 * M5 - Experimento obligatorio: tiempo de la lista y del árbol para N = 1.000, 10.000 y 100.000,
 * con dos valores de M (el del grupo y uno grande).
 *
 * Qué se espera ver: con M chico las dos tardan parecido; con M grande la lista
 * hace N·M pasos y el árbol sigue en N log N, así que la diferencia se vuelve enorme.
 */
public class ExperimentoM5 {

    public static final int[] TAMANOS = {1_000, 10_000, 100_000};
    public static final int M_GRANDE = 1_000;

    /** Un resultado: N, M y el tiempo de cada versión en milisegundos. */
    public static class Fila {
        public final int n, m;
        public final double msLista, msArbol;
        public final boolean coinciden;   // las dos versiones dieron el mismo orden

        Fila(int n, int m, double msLista, double msArbol, boolean coinciden) {
            this.n = n; this.m = m; this.msLista = msLista; this.msArbol = msArbol; this.coinciden = coinciden;
        }
    }

    public static List<Fila> ejecutar(int mGrupo) {
        // Calentamiento: la primera ejecución en Java es más lenta (compilación JIT) y ensuciaría la medición.
        JosephusLista.ordenDeSalida(1_000, mGrupo);
        JosephusArbol.ordenDeSalida(1_000, mGrupo);

        List<Fila> filas = new ArrayList<>();
        for (int m : new int[]{mGrupo, M_GRANDE}) {
            for (int n : TAMANOS) {
                long t0 = System.nanoTime();
                List<Integer> lista = JosephusLista.ordenDeSalida(n, m);
                long t1 = System.nanoTime();
                List<Integer> arbol = JosephusArbol.ordenDeSalida(n, m);
                long t2 = System.nanoTime();
                filas.add(new Fila(n, m, (t1 - t0) / 1e6, (t2 - t1) / 1e6, lista.equals(arbol)));
            }
        }
        return filas;
    }

    public static String formatear(List<Fila> filas) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  %9s %7s %14s %14s %10s  %s%n", "N", "M", "Lista (ms)", "Árbol (ms)", "Lista/Árbol", "¿Iguales?"));
        for (Fila f : filas) {
            sb.append(String.format(Locale.ROOT, "  %9d %7d %14.2f %14.2f %10.1fx  %s%n",
                    f.n, f.m, f.msLista, f.msArbol, f.msLista / f.msArbol, f.coinciden ? "sí" : "NO"));
        }
        return sb.toString();
    }

    /** Agrega las filas al informe de rendimiento de M6. */
    public static void alInforme(List<Fila> filas, InformeRendimiento inf) {
        for (Fila f : filas) {
            String exp = "Josephus N=" + f.n + " M=" + f.m;
            inf.agregar("M5", exp, "Lista + iterador", f.msLista, "O(N·M)");
            inf.agregar("M5", exp, "ABB k-ésimo", f.msArbol, "O(N log N)");
        }
    }
}
