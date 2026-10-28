package redsens.sim;

import java.util.Random;

/** M6: verificación de números aleatorios (secuencias iguales/distintas y uniformidad). */
public class VerificadorAleatorio {

    /** Dos generadores con la misma semilla tienen que dar la misma secuencia. */
    public static boolean secuenciasIguales(long semilla, int largo, boolean imprimir) {
        Random a = new Random(semilla), b = new Random();
        b.setSeed(semilla);
        return comparar(a, b, largo, imprimir, "misma semilla " + semilla) ;
    }

    /** Con semillas distintas las secuencias tienen que ser diferentes. */
    public static boolean secuenciasDistintas(long semillaA, long semillaB, int largo, boolean imprimir) {
        return !comparar(new Random(semillaA), new Random(semillaB), largo, imprimir,
                "semillas " + semillaA + " y " + semillaB);
    }

    /** @return true si todos los valores coinciden. */
    private static boolean comparar(Random a, Random b, int largo, boolean imprimir, String titulo) {
        boolean todosIguales = true;
        StringBuilder sa = new StringBuilder(), sb = new StringBuilder();
        for (int i = 0; i < largo; i++) {
            double x = a.nextDouble(), y = b.nextDouble();
            if (x != y) todosIguales = false;
            if (imprimir && i < 10) {
                sa.append(String.format("%.4f\t", x));
                sb.append(String.format("%.4f\t", y));
            }
        }
        if (imprimir) {
            System.out.println("  [" + titulo + "] primeros 10 de " + largo + ":");
            System.out.println("    Gen A: " + sa);
            System.out.println("    Gen B: " + sb);
        }
        return todosIguales;
    }

    /** Resultado de la prueba de uniformidad. */
    public static class ResultadoUniformidad {
        public int[] conteo;
        public int cantidad, valores;
        public double esperado, chi2, chi2Critico, desvioMaxPct;
        public boolean uniforme;
    }

    /**
     * Cuenta cuántas veces sale cada valor de 0 a maxInclusive, lo compara con el esperado
     * (cantidad / valores) y calcula chi-cuadrado con alfa = 0,05.
     */
    public static ResultadoUniformidad compruebaUniformidad(Random rnd, int maxInclusive, int cantidad) {
        ResultadoUniformidad r = new ResultadoUniformidad();
        r.valores = maxInclusive + 1; r.cantidad = cantidad;
        r.conteo = new int[r.valores];
        for (int i = 0; i < cantidad; i++) r.conteo[rnd.nextInt(r.valores)]++;
        return evaluar(r);
    }

    /** Uniformidad de la batería (0..100) de estaciones ya generadas. */
    public static ResultadoUniformidad compruebaUniformidadBateria(java.util.List<redsens.modelo.Estacion> estaciones) {
        ResultadoUniformidad r = new ResultadoUniformidad();
        r.valores = 101; r.cantidad = estaciones.size();
        r.conteo = new int[r.valores];
        for (redsens.modelo.Estacion e : estaciones) r.conteo[e.getBateria()]++;
        return evaluar(r);
    }

    /** Con el conteo por valor calcula el esperado, chi-cuadrado y si es uniforme. */
    private static ResultadoUniformidad evaluar(ResultadoUniformidad r) {
        r.esperado = (double) r.cantidad / r.valores;
        double chi = 0, maxDes = 0;
        for (int c : r.conteo) {
            chi += (c - r.esperado) * (c - r.esperado) / r.esperado;
            maxDes = Math.max(maxDes, Math.abs(c - r.esperado) / r.esperado * 100);
        }
        r.chi2 = chi; r.desvioMaxPct = maxDes;
        r.chi2Critico = chiCritico95(r.valores - 1);
        r.uniforme = chi <= r.chi2Critico;
        return r;
    }

    /** Valor crítico de chi² al 95 % (aproximación de Wilson-Hilferty, z = 1,645). */
    static double chiCritico95(int gl) {
        double k = 2.0 / (9.0 * gl);
        double t = 1 - k + 1.645 * Math.sqrt(k);
        return gl * t * t * t;
    }

    /** Imprime el resultado agrupado por intervalos, con una barra de texto. */
    public static void imprimir(ResultadoUniformidad r, int intervalos) {
        System.out.printf("  %d números sobre %d valores (0..%d). Esperado por valor = %.2f%n",
                r.cantidad, r.valores, r.valores - 1, r.esperado);
        int tam = (int) Math.ceil((double) r.valores / intervalos);
        for (int ini = 0; ini < r.valores; ini += tam) {
            int fin = Math.min(ini + tam, r.valores), suma = 0;
            for (int v = ini; v < fin; v++) suma += r.conteo[v];
            double esp = r.esperado * (fin - ini);
            int barra = (int) Math.round(30.0 * suma / (esp * 1.5));
            System.out.printf("  [%3d-%3d] %7d (esp. %.0f, %+5.1f%%) %s%n", ini, fin - 1, suma, esp,
                    (suma - esp) / esp * 100, "#".repeat(Math.max(0, Math.min(barra, 45))));
        }
        System.out.printf("  Desvío máximo por valor: %.1f%% | chi² = %.2f (crítico 95%%, gl=%d: %.2f) -> %s%n",
                r.desvioMaxPct, r.chi2, r.valores - 1, r.chi2Critico,
                r.uniforme ? "UNIFORME (no se rechaza)" : "NO uniforme");
    }
}
