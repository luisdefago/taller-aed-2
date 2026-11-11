package redsens.red;

import java.util.ArrayList;
import java.util.List;
import redsens.modelo.Estacion;

/**
 * M3: plan de visitas de la cuadrilla. Sale de la base, pasa por todas las localidades con estaciones críticas
 * (reporte del M2) y vuelve a la base, usando las distancias de Floyd.
 *
 * Criterio del vecino más cercano: desde donde está, la cuadrilla va a la localidad pendiente más cercana.
 * Es O(k²) para k localidades, pero es un criterio "goloso": elige lo mejor en cada paso sin mirar el
 * recorrido completo, y puede dejar una localidad lejana para el final y pagar caro la vuelta a la base.
 * Por eso no garantiza el óptimo. Para mostrarlo, se calcula también el recorrido óptimo con programación
 * dinámica sobre subconjuntos (Held-Karp, O(2^k · k²)); con k <= 11 son unos 250.000 pasos.
 */
public final class PlanVisitas {
    /** Hasta cuántas localidades se calcula el óptimo (2^k crece rápido). */
    public static final int MAX_OPTIMO = 15;

    /** Un recorrido cerrado: base, localidades en orden de visita, base. */
    public static final class Recorrido {
        public final List<Integer> orden; // incluye la base al principio y al final
        public final int km;

        Recorrido(List<Integer> orden, int km) {
            this.orden = orden;
            this.km = km;
        }
    }

    private PlanVisitas() { }

    /**
     * Localidades distintas (índices del grafo) que tienen al menos una estación crítica, sin la base
     * (la cuadrilla ya está ahí). Se marcan en un arreglo de booleanos, sin HashSet.
     */
    public static List<Integer> localidadesCriticas(Grafo g, List<Estacion> criticas, int base) {
        boolean[] tiene = new boolean[g.cantVertices()];
        for (Estacion e : criticas) {
            int v = g.indice(e.getLocalidad());
            if (v < 0)
                throw new IllegalArgumentException("La localidad \"" + e.getLocalidad() + "\" de " + e.getCodigo()
                        + " no es un vértice del grafo");
            tiene[v] = true;
        }
        List<Integer> r = new ArrayList<>();
        for (int v = 0; v < tiene.length; v++)
            if (tiene[v] && v != base)
                r.add(v);
        return r;
    }

    /** Cantidad de estaciones críticas por localidad (índice del grafo). */
    public static int[] criticasPorLocalidad(Grafo g, List<Estacion> criticas) {
        int[] c = new int[g.cantVertices()];
        for (Estacion e : criticas) {
            int v = g.indice(e.getLocalidad());
            if (v >= 0)
                c[v]++;
        }
        return c;
    }

    /** Recorrido con el criterio del vecino más cercano. */
    public static Recorrido vecinoMasCercano(CaminosMinimos.Floyd f, int base, List<Integer> destinos) {
        boolean[] pendiente = new boolean[f.dist.length];
        for (int d : destinos)
            pendiente[d] = true;
        List<Integer> orden = new ArrayList<>();
        orden.add(base);
        int actual = base, km = 0;
        for (int paso = 0; paso < destinos.size(); paso++) {
            int mejor = -1;
            for (int d : destinos) // en empate gana la que aparece primero en el grafo
                if (pendiente[d] && (mejor == -1 || f.dist[actual][d] < f.dist[actual][mejor]))
                    mejor = d;
            if (f.dist[actual][mejor] == CaminosMinimos.INF)
                throw new IllegalStateException("No hay camino hasta una de las localidades a visitar");
            km += f.dist[actual][mejor];
            pendiente[mejor] = false;
            orden.add(mejor);
            actual = mejor;
        }
        if (f.dist[actual][base] == CaminosMinimos.INF)
            throw new IllegalStateException("No hay camino de vuelta a la base");
        km += f.dist[actual][base];
        orden.add(base);
        return new Recorrido(orden, km);
    }

    /**
     * Recorrido óptimo (Held-Karp). costo[S][i] = km mínimos saliendo de la base, visitando exactamente el
     * conjunto S (máscara de bits) y terminando en el destino i de S.
     */
    public static Recorrido optimo(CaminosMinimos.Floyd f, int base, List<Integer> destinos) {
        int k = destinos.size();
        if (k > MAX_OPTIMO)
            throw new IllegalArgumentException("Demasiadas localidades para calcular el óptimo (" + k + ")");
        if (k == 0) {
            List<Integer> solo = new ArrayList<>();
            solo.add(base);
            solo.add(base);
            return new Recorrido(solo, 0);
        }
        int[] d = new int[k];
        for (int i = 0; i < k; i++)
            d[i] = destinos.get(i);
        long inf = Long.MAX_VALUE / 4;
        int subconjuntos = 1 << k;
        long[][] costo = new long[subconjuntos][k];
        int[][] anterior = new int[subconjuntos][k];
        for (int s = 0; s < subconjuntos; s++)
            for (int i = 0; i < k; i++)
                costo[s][i] = inf;
        for (int i = 0; i < k; i++) { // caso base: de la base directo a d[i]
            costo[1 << i][i] = distancia(f, base, d[i], inf);
            anterior[1 << i][i] = -1;
        }
        for (int s = 1; s < subconjuntos; s++) {
            for (int i = 0; i < k; i++) {
                if ((s & (1 << i)) == 0 || costo[s][i] >= inf)
                    continue;
                for (int j = 0; j < k; j++) { // extender el recorrido de s (que termina en i) hasta j
                    if ((s & (1 << j)) != 0)
                        continue;
                    int t = s | (1 << j);
                    long c = costo[s][i] + distancia(f, d[i], d[j], inf);
                    if (c < costo[t][j]) {
                        costo[t][j] = c;
                        anterior[t][j] = i;
                    }
                }
            }
        }
        int todos = subconjuntos - 1, ultimo = -1;
        long mejor = inf;
        for (int i = 0; i < k; i++) {
            long c = costo[todos][i] + distancia(f, d[i], base, inf);
            if (c < mejor) {
                mejor = c;
                ultimo = i;
            }
        }
        if (ultimo == -1)
            throw new IllegalStateException("No hay recorrido que pase por todas las localidades");
        List<Integer> orden = new ArrayList<>();
        orden.add(base);
        for (int s = todos, i = ultimo; i != -1;) { // se reconstruye de atrás para adelante
            orden.add(1, d[i]);
            int prev = anterior[s][i];
            s &= ~(1 << i);
            i = prev;
        }
        orden.add(base);
        return new Recorrido(orden, (int) mejor);
    }

    private static long distancia(CaminosMinimos.Floyd f, int a, int b, long inf) {
        return f.dist[a][b] == CaminosMinimos.INF ? inf : f.dist[a][b];
    }

    /** Recorrido con todas las localidades por las que pasa la cuadrilla (incluye las intermedias). */
    public static List<Integer> recorridoCompleto(CaminosMinimos.Floyd f, Recorrido r) {
        List<Integer> todo = new ArrayList<>();
        todo.add(r.orden.get(0));
        for (int i = 1; i < r.orden.size(); i++) {
            List<Integer> tramo = f.camino(r.orden.get(i - 1), r.orden.get(i));
            for (int j = 1; j < tramo.size(); j++) // el primero ya está (es el final del tramo anterior)
                todo.add(tramo.get(j));
        }
        return todo;
    }
}
