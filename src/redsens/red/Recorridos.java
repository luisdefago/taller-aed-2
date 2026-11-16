package redsens.red;

import java.util.ArrayList;
import java.util.List;

/**
 * M3: recorridos del grafo. Los dos usan la lista de adyacencia, así cuestan O(V + E).
 * - BFS (en anchura) visita por niveles: primero los vecinos directos, después los vecinos de esos, etc.
 *   El nivel de cada localidad es la menor cantidad de tramos para llegar desde el origen.
 * - DFS (en profundidad) avanza lo más lejos posible y retrocede; sirve para ver qué se alcanza (conexidad).
 */
public final class Recorridos {
    /** Nivel de los vértices que no se alcanzan desde el origen. */
    public static final int INALCANZABLE = -1;

    private Recorridos() { }

    /**
     * BFS desde el origen.
     * @return nivel[v] = cantidad mínima de tramos desde el origen hasta v (INALCANZABLE si no hay camino)
     */
    public static int[] bfsNiveles(Grafo g, int origen) {
        int v = g.cantVertices();
        int[] nivel = new int[v];
        for (int i = 0; i < v; i++)
            nivel[i] = INALCANZABLE;
        // Cola FIFO en un arreglo: cada vértice entra una sola vez, así que alcanza con V lugares.
        int[] cola = new int[v];
        int frente = 0, fin = 0;
        nivel[origen] = 0;
        cola[fin++] = origen;
        while (frente < fin) {
            int actual = cola[frente++];
            for (Grafo.Arista a = g.vecinos(actual); a != null; a = a.sig) {
                if (nivel[a.destino] == INALCANZABLE) { // se marca al encolar, no al sacar
                    nivel[a.destino] = nivel[actual] + 1;
                    cola[fin++] = a.destino;
                }
            }
        }
        return nivel;
    }

    /** Localidades que están exactamente a k tramos (según el resultado de bfsNiveles). */
    public static List<String> enNivel(Grafo g, int[] nivel, int k) {
        List<String> r = new ArrayList<>();
        for (int i = 0; i < nivel.length; i++)
            if (nivel[i] == k)
                r.add(g.nombre(i));
        return r;
    }

    /**
     * DFS recursivo desde el origen.
     * @return visitado[v] = true si v se alcanza desde el origen
     */
    public static boolean[] dfs(Grafo g, int origen) {
        boolean[] visitado = new boolean[g.cantVertices()];
        dfs(g, origen, visitado);
        return visitado;
    }

    private static void dfs(Grafo g, int v, boolean[] visitado) {
        visitado[v] = true;
        for (Grafo.Arista a = g.vecinos(v); a != null; a = a.sig)
            if (!visitado[a.destino])
                dfs(g, a.destino, visitado);
        // caso base implícito: si todos los vecinos ya están visitados, no hay más llamadas
    }

    /** La red es conexa si un DFS desde cualquier vértice (el 0) llega a todos. */
    public static boolean esConexa(Grafo g) {
        if (g.cantVertices() == 0)
            return true;
        for (boolean b : dfs(g, 0))
            if (!b)
                return false;
        return true;
    }

    /**
     * Componentes conexas: comp[v] = número de componente de v (0, 1, 2, ...).
     * Se lanza un DFS desde cada vértice que todavía no tiene componente.
     */
    public static int[] componentes(Grafo g) {
        int v = g.cantVertices();
        int[] comp = new int[v];
        for (int i = 0; i < v; i++)
            comp[i] = -1;
        int nro = 0;
        for (int i = 0; i < v; i++) {
            if (comp[i] != -1)
                continue;
            boolean[] alcanzados = dfs(g, i);
            for (int j = 0; j < v; j++)
                if (alcanzados[j])
                    comp[j] = nro;
            nro++;
        }
        return comp;
    }

    /** Localidades que no se alcanzan desde la base (quedan aisladas para la cuadrilla). */
    public static List<String> aisladasDesde(Grafo g, int base) {
        boolean[] visitado = dfs(g, base);
        List<String> r = new ArrayList<>();
        for (int i = 0; i < visitado.length; i++)
            if (!visitado[i])
                r.add(g.nombre(i));
        return r;
    }

    /**
     * Rutas puente: las que, si se cortan solas, desconectan la red.
     * Se prueba cortar cada arista en una copia y se corre DFS: O(E · (V + E)), con 18 aristas es instantáneo.
     */
    public static List<String> puentes(Grafo g) {
        List<String> r = new ArrayList<>();
        for (int a = 0; a < g.cantVertices(); a++) {
            for (int b = a + 1; b < g.cantVertices(); b++) {
                if (!g.hayRuta(a, b))
                    continue;
                Grafo corte = g.copia();
                corte.quitarArista(a, b);
                if (!esConexa(corte))
                    r.add(g.nombre(a) + " - " + g.nombre(b));
            }
        }
        return r;
    }
}
