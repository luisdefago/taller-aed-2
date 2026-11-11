package redsens.red;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * M3: red vial como grafo no dirigido y ponderado (km), guardado de las dos formas que pide el enunciado.
 *
 * - Matriz de adyacencia (int[V][V]): ver si hay ruta entre A y B es O(1), y es la forma natural de Floyd.
 *   Ocupa V² celdas aunque casi todas estén vacías.
 * - Lista de adyacencia (una lista enlazada de aristas por vértice): ocupa V + 2E y recorrer los vecinos de
 *   un vértice cuesta lo que su grado, así BFS, DFS y Dijkstra son O(V + E) en vez de O(V²).
 *
 * Para este caso conviene la lista: hay 12 localidades y 18 rutas de las 66 posibles (densidad 27 %), así
 * que la matriz tiene casi tres cuartos de celdas vacías. La matriz se usa solo como punto de partida de Floyd.
 *
 * Los nombres se buscan con una búsqueda lineal en un arreglo (no se puede usar HashMap en M3); con 12
 * vértices eso es despreciable.
 */
public class Grafo {
    /** Valor de "no hay ruta" en la matriz. */
    public static final int SIN_RUTA = -1;

    /** Una arista de la lista de adyacencia: destino, km y la siguiente arista del mismo vértice. */
    static final class Arista {
        final int destino;
        final int km;
        Arista sig;

        Arista(int destino, int km, Arista sig) {
            this.destino = destino;
            this.km = km;
            this.sig = sig;
        }
    }

    private final String[] nombres;
    private final int[][] matriz;     // matriz[a][b] = km, o SIN_RUTA
    private final Arista[] lista;     // lista[a] = primera arista que sale de a
    private int aristas;

    public Grafo(String[] nombres) {
        this.nombres = nombres.clone();
        int v = nombres.length;
        matriz = new int[v][v];
        for (int i = 0; i < v; i++)
            for (int j = 0; j < v; j++)
                matriz[i][j] = SIN_RUTA;
        lista = new Arista[v];
    }

    /**
     * Carga el grafo desde un archivo de texto con una arista por línea: "A;B;km".
     * Los vértices salen del propio archivo, en el orden en que aparecen.
     */
    public static Grafo desdeArchivo(Path archivo) throws IOException {
        List<String[]> filas = new ArrayList<>();
        List<String> vertices = new ArrayList<>();
        int nroLinea = 0;
        for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
            nroLinea++;
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("#"))
                continue;
            String[] p = linea.split(";");
            if (p.length != 3)
                throw new IOException("Línea " + nroLinea + " de " + archivo + ": se esperaba \"A;B;km\"");
            p[0] = p[0].trim();
            p[1] = p[1].trim();
            if (!vertices.contains(p[0]))
                vertices.add(p[0]);
            if (!vertices.contains(p[1]))
                vertices.add(p[1]);
            filas.add(p);
        }
        Grafo g = new Grafo(vertices.toArray(new String[0]));
        for (String[] p : filas) {
            int km;
            try {
                km = Integer.parseInt(p[2].trim());
            } catch (NumberFormatException e) {
                throw new IOException("Distancia inválida en " + archivo + ": \"" + p[2] + "\"");
            }
            g.agregarArista(p[0], p[1], km);
        }
        return g;
    }

    /** Copia independiente (para simular cortes sin tocar la red original). */
    public Grafo copia() {
        Grafo g = new Grafo(nombres);
        for (int a = 0; a < nombres.length; a++)
            for (int b = a + 1; b < nombres.length; b++)
                if (matriz[a][b] != SIN_RUTA)
                    g.agregarArista(a, b, matriz[a][b]);
        return g;
    }

    // ---------- Vértices ----------
    public int cantVertices() { return nombres.length; }

    public int cantAristas() { return aristas; }

    public String nombre(int v) { return nombres[v]; }

    /** @return el índice de la localidad, o -1 si no es un vértice del grafo. Búsqueda lineal, O(V). */
    public int indice(String nombre) {
        for (int i = 0; i < nombres.length; i++)
            if (nombres[i].equalsIgnoreCase(nombre.trim()))
                return i;
        return -1;
    }

    private int indiceObligatorio(String nombre) {
        int i = indice(nombre);
        if (i < 0)
            throw new IllegalArgumentException("\"" + nombre + "\" no es una localidad de la red");
        return i;
    }

    // ---------- Aristas ----------
    public void agregarArista(String a, String b, int km) {
        agregarArista(indiceObligatorio(a), indiceObligatorio(b), km);
    }

    /** Agrega la ruta en los dos sentidos (grafo no dirigido). Si ya existía, actualiza los km. */
    public void agregarArista(int a, int b, int km) {
        if (a == b)
            throw new IllegalArgumentException("Una ruta no puede unir una localidad consigo misma");
        if (km <= 0)
            throw new IllegalArgumentException("Los km tienen que ser positivos");
        if (matriz[a][b] != SIN_RUTA)
            quitarArista(a, b);
        matriz[a][b] = km;
        matriz[b][a] = km;
        lista[a] = new Arista(b, km, lista[a]); // se agrega adelante: O(1)
        lista[b] = new Arista(a, km, lista[b]);
        aristas++;
    }

    /** Corta la ruta entre a y b. @return true si existía. */
    public boolean quitarArista(int a, int b) {
        if (matriz[a][b] == SIN_RUTA)
            return false;
        matriz[a][b] = SIN_RUTA;
        matriz[b][a] = SIN_RUTA;
        lista[a] = quitarDeLista(lista[a], b);
        lista[b] = quitarDeLista(lista[b], a);
        aristas--;
        return true;
    }

    private static Arista quitarDeLista(Arista primera, int destino) {
        if (primera == null)
            return null; // caso base: lista vacía
        if (primera.destino == destino)
            return primera.sig; // caso base: era esta
        primera.sig = quitarDeLista(primera.sig, destino);
        return primera;
    }

    public boolean hayRuta(int a, int b) { return matriz[a][b] != SIN_RUTA; }

    /** @return km de la ruta directa entre a y b, o SIN_RUTA. Consulta a la matriz: O(1). */
    public int km(int a, int b) { return matriz[a][b]; }

    /** Primera arista de la lista de adyacencia de v (para recorrer sus vecinos). */
    Arista vecinos(int v) { return lista[v]; }

    public int grado(int v) {
        int g = 0;
        for (Arista a = lista[v]; a != null; a = a.sig)
            g++;
        return g;
    }

    /** Copia de la matriz de adyacencia (Floyd trabaja sobre una copia). */
    public int[][] matrizAdyacencia() {
        int[][] m = new int[matriz.length][];
        for (int i = 0; i < matriz.length; i++)
            m[i] = matriz[i].clone();
        return m;
    }

    // ---------- Impresión ----------
    /** Matriz de adyacencia como tabla (km; "-" = sin ruta directa). */
    public String renderMatriz() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-17s", ""));
        for (int j = 0; j < nombres.length; j++)
            sb.append(String.format("%5s", abreviar(nombres[j])));
        sb.append('\n');
        for (int i = 0; i < nombres.length; i++) {
            sb.append(String.format("%-17s", nombres[i]));
            for (int j = 0; j < nombres.length; j++)
                sb.append(String.format("%5s", matriz[i][j] == SIN_RUTA ? "-" : String.valueOf(matriz[i][j])));
            sb.append('\n');
        }
        return sb.toString();
    }

    /** Lista de adyacencia: cada localidad con sus vecinos y los km. */
    public String renderLista() {
        StringBuilder sb = new StringBuilder();
        for (int v = 0; v < nombres.length; v++) {
            sb.append(String.format("%-17s ->", nombres[v]));
            for (Arista a = lista[v]; a != null; a = a.sig)
                sb.append(' ').append(nombres[a.destino]).append(" (").append(a.km).append(')')
                        .append(a.sig != null ? "," : "");
            sb.append('\n');
        }
        return sb.toString();
    }

    /** Nombre abreviado a 4 letras para los encabezados de las tablas (ej. "Villa Carlos Paz" -> "VPaz"). */
    public static String abreviar(String nombre) {
        String[] p = nombre.split(" ");
        if (p.length == 1)
            return nombre.substring(0, Math.min(4, nombre.length()));
        String ult = p[p.length - 1];
        return p[0].charAt(0) + ult.substring(0, Math.min(3, ult.length()));
    }
}
