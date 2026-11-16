package redsens.red;

import java.util.ArrayList;
import java.util.List;

/**
 * M3: caminos mínimos.
 * - Dijkstra: de UN origen a todas las localidades. Versión con arreglos: en cada paso se elige el vértice no
 *   fijado con menor distancia (O(V)) y se relajan sus aristas con la lista de adyacencia. Total O(V² + E).
 *   Con 12 vértices no hace falta cola de prioridad; con miles convendría una (O((V + E) log V)).
 * - Floyd: entre TODOS los pares. Tres ciclos anidados sobre la matriz: O(V³) = 1.728 pasos para V = 12.
 *   Hacer Dijkstra desde cada vértice daría lo mismo; Floyd es más simple de escribir y deja la tabla lista.
 * Los dos funcionan porque los km son positivos (Dijkstra no admite pesos negativos).
 */
public final class CaminosMinimos {
    /** Distancia de una localidad inalcanzable. */
    public static final int INF = Integer.MAX_VALUE;

    private CaminosMinimos() { }

    /** Resultado de Dijkstra: distancias y el anterior de cada vértice en el camino mínimo. */
    public static final class Dijkstra {
        public final int origen;
        public final int[] dist;
        public final int[] previo; // -1 = es el origen o no se alcanza

        private Dijkstra(int origen, int[] dist, int[] previo) {
            this.origen = origen;
            this.dist = dist;
            this.previo = previo;
        }

        /** Camino completo desde el origen hasta destino (vacío si no hay camino). */
        public List<Integer> camino(int destino) {
            List<Integer> r = new ArrayList<>();
            if (dist[destino] == INF)
                return r;
            for (int v = destino; v != -1; v = previo[v])
                r.add(0, v); // se reconstruye de atrás para adelante siguiendo los previos
            return r;
        }
    }

    public static Dijkstra dijkstra(Grafo g, int origen) {
        int n = g.cantVertices();
        int[] dist = new int[n];
        int[] previo = new int[n];
        boolean[] fijado = new boolean[n];
        for (int i = 0; i < n; i++) {
            dist[i] = INF;
            previo[i] = -1;
        }
        dist[origen] = 0;
        for (int paso = 0; paso < n; paso++) {
            // 1) El vértice no fijado más cercano: su distancia ya no puede mejorar.
            int u = -1;
            for (int i = 0; i < n; i++)
                if (!fijado[i] && dist[i] != INF && (u == -1 || dist[i] < dist[u]))
                    u = i;
            if (u == -1)
                break; // los que quedan no se alcanzan
            fijado[u] = true;
            // 2) Relajación: ¿llegar a cada vecino pasando por u es más corto?
            for (Grafo.Arista a = g.vecinos(u); a != null; a = a.sig) {
                if (!fijado[a.destino] && dist[u] + a.km < dist[a.destino]) {
                    dist[a.destino] = dist[u] + a.km;
                    previo[a.destino] = u;
                }
            }
        }
        return new Dijkstra(origen, dist, previo);
    }

    /** Resultado de Floyd: matriz de distancias y matriz "siguiente" para reconstruir caminos. */
    public static final class Floyd {
        public final int[][] dist;
        public final int[][] siguiente; // siguiente[i][j] = primer paso de i hacia j (-1 = sin camino)

        private Floyd(int[][] dist, int[][] siguiente) {
            this.dist = dist;
            this.siguiente = siguiente;
        }

        /** Camino completo de i a j (vacío si no hay camino). */
        public List<Integer> camino(int i, int j) {
            List<Integer> r = new ArrayList<>();
            if (dist[i][j] == INF)
                return r;
            r.add(i);
            while (i != j) {
                i = siguiente[i][j];
                r.add(i);
            }
            return r;
        }
    }

    public static Floyd floyd(Grafo g) {
        int n = g.cantVertices();
        int[][] dist = g.matrizAdyacencia(); // se parte de la matriz de adyacencia
        int[][] sig = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                    sig[i][j] = j;
                } else if (dist[i][j] == Grafo.SIN_RUTA) {
                    dist[i][j] = INF;
                    sig[i][j] = -1;
                } else {
                    sig[i][j] = j;
                }
            }
        }
        // k = localidad intermedia que se habilita en esta vuelta.
        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (dist[i][k] != INF && dist[k][j] != INF && dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        sig[i][j] = sig[i][k];
                    }
        return new Floyd(dist, sig);
    }

    /** "A -> B -> C" con los nombres de las localidades. */
    public static String nombres(Grafo g, List<Integer> camino) {
        StringBuilder sb = new StringBuilder();
        for (int v : camino) {
            if (sb.length() > 0)
                sb.append(" -> ");
            sb.append(g.nombre(v));
        }
        return sb.toString();
    }

    /** Matriz de distancias mínimas como tabla ("inf" = sin camino). */
    public static String renderFloyd(Grafo g, Floyd f) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-17s", ""));
        for (int j = 0; j < g.cantVertices(); j++)
            sb.append(String.format("%5s", Grafo.abreviar(g.nombre(j))));
        sb.append('\n');
        for (int i = 0; i < g.cantVertices(); i++) {
            sb.append(String.format("%-17s", g.nombre(i)));
            for (int j = 0; j < g.cantVertices(); j++)
                sb.append(String.format("%5s", f.dist[i][j] == INF ? "inf" : String.valueOf(f.dist[i][j])));
            sb.append('\n');
        }
        return sb.toString();
    }
}
