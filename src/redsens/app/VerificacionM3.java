package redsens.app;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import redsens.modelo.Estacion;
import redsens.red.CaminosMinimos;
import redsens.red.Grafo;
import redsens.red.PlanVisitas;
import redsens.red.Recorridos;

/**
 * Verificación del M3 con la red del enunciado (datos/rutas.txt). Los valores esperados se calcularon a mano.
 * Córdoba es el caso de control del enunciado; Villa Dolores es la base del grupo 4.
 */
public class VerificacionM3 {

    public static void main(String[] args) throws Exception {
        Grafo g = Grafo.desdeArchivo(Path.of("datos/rutas.txt"));
        verificar("vértices", "12", String.valueOf(g.cantVertices()));
        verificar("aristas", "18", String.valueOf(g.cantAristas()));
        verificar("matriz Córdoba-Villa María", "146", String.valueOf(km(g, "Córdoba", "Villa María")));
        verificar("matriz es simétrica", "146", String.valueOf(km(g, "Villa María", "Córdoba")));
        verificar("sin ruta directa Córdoba-Río Cuarto", "-1", String.valueOf(km(g, "Córdoba", "Río Cuarto")));
        verificar("grado de Córdoba (lista)", "6", String.valueOf(g.grado(g.indice("Córdoba"))));
        verificar("grado de Villa Dolores (lista)", "2", String.valueOf(g.grado(g.indice("Villa Dolores"))));

        probarBFS(g);
        probarDFS(g);
        probarDijkstra(g);
        probarFloyd(g);
        probarPlan(g);
    }

    private static void probarBFS(Grafo g) {
        System.out.println("--- BFS desde Villa Dolores ---");
        int[] nivel = Recorridos.bfsNiveles(g, g.indice("Villa Dolores"));
        verificar("nivel 1", "[Río Cuarto, Villa Carlos Paz]", ordenado(Recorridos.enNivel(g, nivel, 1)));
        verificar("nivel 2", "[Alta Gracia, Cosquín, Córdoba, Río Tercero, Villa María]",
                ordenado(Recorridos.enNivel(g, nivel, 2)));
        verificar("nivel 3", "[Jesús María, La Falda, San Francisco]", ordenado(Recorridos.enNivel(g, nivel, 3)));
        verificar("nivel 4", "[Deán Funes]", ordenado(Recorridos.enNivel(g, nivel, 4)));
    }

    private static void probarDFS(Grafo g) {
        System.out.println("--- DFS: conexidad y cortes ---");
        verificar("la red es conexa", "true", String.valueOf(Recorridos.esConexa(g)));
        verificar("ninguna ruta es puente", "[]", Recorridos.puentes(g).toString());

        Grafo c1 = g.copia();
        c1.quitarArista(g.indice("Villa Carlos Paz"), g.indice("Villa Dolores"));
        verificar("corte VCP-Villa Dolores: sigue conexa", "true", String.valueOf(Recorridos.esConexa(c1)));
        verificar("la copia no toca el original", "18", String.valueOf(g.cantAristas()));

        c1.quitarArista(g.indice("Villa Dolores"), g.indice("Río Cuarto"));
        verificar("y corte Villa Dolores-Río Cuarto: conexa", "false", String.valueOf(Recorridos.esConexa(c1)));
        verificar("  aislada desde Córdoba", "[Villa Dolores]",
                Recorridos.aisladasDesde(c1, g.indice("Córdoba")).toString());

        Grafo c2 = g.copia();
        c2.quitarArista(g.indice("Córdoba"), g.indice("Jesús María"));
        c2.quitarArista(g.indice("La Falda"), g.indice("Deán Funes"));
        verificar("cortar JM-Córdoba y La Falda-DF: aisladas", "[Jesús María, Deán Funes]",
                Recorridos.aisladasDesde(c2, g.indice("Villa Dolores")).toString());
        int[] comp = Recorridos.componentes(c2);
        verificar("  JM y DF en la misma componente", "true",
                String.valueOf(comp[g.indice("Jesús María")] == comp[g.indice("Deán Funes")]));
        verificar("cortar una ruta inexistente", "false",
                String.valueOf(g.copia().quitarArista(g.indice("Córdoba"), g.indice("Río Cuarto"))));
    }

    private static void probarDijkstra(Grafo g) {
        System.out.println("--- Dijkstra desde Córdoba (control) ---");
        CaminosMinimos.Dijkstra d = CaminosMinimos.dijkstra(g, g.indice("Córdoba"));
        String[] loc = { "Córdoba", "Villa Carlos Paz", "Alta Gracia", "Jesús María", "Río Tercero", "Villa María",
                "San Francisco", "Cosquín", "La Falda", "Deán Funes", "Río Cuarto", "Villa Dolores" };
        int[] esperado = { 0, 36, 39, 51, 102, 146, 205, 60, 80, 121, 212, 176 };
        for (int i = 0; i < loc.length; i++)
            verificar("Córdoba -> " + loc[i], String.valueOf(esperado[i]), String.valueOf(d.dist[g.indice(loc[i])]));
        verificar("camino a Río Cuarto", "Córdoba -> Río Tercero -> Río Cuarto",
                CaminosMinimos.nombres(g, d.camino(g.indice("Río Cuarto"))));
        verificar("camino a La Falda", "Córdoba -> Villa Carlos Paz -> Cosquín -> La Falda",
                CaminosMinimos.nombres(g, d.camino(g.indice("La Falda"))));

        System.out.println("--- Dijkstra desde Villa Dolores (base del grupo 4) ---");
        d = CaminosMinimos.dijkstra(g, g.indice("Villa Dolores"));
        int[] esperadoVD = { 176, 140, 175, 227, 260, 322, 381, 164, 184, 279, 210, 0 };
        for (int i = 0; i < loc.length; i++)
            verificar("Villa Dolores -> " + loc[i], String.valueOf(esperadoVD[i]),
                    String.valueOf(d.dist[g.indice(loc[i])]));
        verificar("camino a San Francisco", "Villa Dolores -> Villa Carlos Paz -> Córdoba -> San Francisco",
                CaminosMinimos.nombres(g, d.camino(g.indice("San Francisco"))));
        verificar("camino a Deán Funes",
                "Villa Dolores -> Villa Carlos Paz -> Cosquín -> La Falda -> Deán Funes",
                CaminosMinimos.nombres(g, d.camino(g.indice("Deán Funes"))));

        Grafo aislado = g.copia();
        aislado.quitarArista(g.indice("Villa Carlos Paz"), g.indice("Villa Dolores"));
        aislado.quitarArista(g.indice("Villa Dolores"), g.indice("Río Cuarto"));
        CaminosMinimos.Dijkstra sin = CaminosMinimos.dijkstra(aislado, g.indice("Córdoba"));
        verificar("sin camino: distancia infinita", "true",
                String.valueOf(sin.dist[g.indice("Villa Dolores")] == CaminosMinimos.INF));
        verificar("sin camino: camino vacío", "[]", sin.camino(g.indice("Villa Dolores")).toString());
    }

    private static void probarFloyd(Grafo g) {
        System.out.println("--- Floyd ---");
        CaminosMinimos.Floyd f = CaminosMinimos.floyd(g);
        boolean igualADijkstra = true, simetrica = true;
        for (int i = 0; i < g.cantVertices(); i++) {
            CaminosMinimos.Dijkstra d = CaminosMinimos.dijkstra(g, i);
            for (int j = 0; j < g.cantVertices(); j++) {
                igualADijkstra &= f.dist[i][j] == d.dist[j];
                simetrica &= f.dist[i][j] == f.dist[j][i];
            }
        }
        verificar("Floyd = Dijkstra desde cada vértice", "true", String.valueOf(igualADijkstra));
        verificar("matriz simétrica", "true", String.valueOf(simetrica));
        verificar("Deán Funes -> Villa Dolores", "279",
                String.valueOf(f.dist[g.indice("Deán Funes")][g.indice("Villa Dolores")]));
        verificar("camino San Francisco -> Río Cuarto", "San Francisco -> Villa María -> Río Cuarto",
                CaminosMinimos.nombres(g, f.camino(g.indice("San Francisco"), g.indice("Río Cuarto"))));
    }

    private static void probarPlan(Grafo g) {
        System.out.println("--- Plan de visitas (vecino más cercano) ---");
        // Red en línea: X(-2) - B(0) - A(1) - C(5). El vecino más cercano va B, A, X, C y vuelve:
        // 1 + 3 + 7 + 5 = 16 km. El óptimo B, A, C, X, B es 1 + 4 + 7 + 2 = 14 km.
        Grafo linea = new Grafo(new String[] { "B", "A", "X", "C" });
        linea.agregarArista("X", "B", 2);
        linea.agregarArista("B", "A", 1);
        linea.agregarArista("A", "C", 4);
        CaminosMinimos.Floyd fl = CaminosMinimos.floyd(linea);
        List<Integer> destinos = List.of(1, 2, 3);
        PlanVisitas.Recorrido nn = PlanVisitas.vecinoMasCercano(fl, 0, destinos);
        PlanVisitas.Recorrido opt = PlanVisitas.optimo(fl, 0, destinos);
        verificar("vecino más cercano: orden", "B -> A -> X -> C -> B", CaminosMinimos.nombres(linea, nn.orden));
        verificar("vecino más cercano: km", "16", String.valueOf(nn.km));
        verificar("óptimo: km", "14", String.valueOf(opt.km));
        verificar("recorrido completo con intermedias", "B -> A -> B -> X -> B -> A -> C -> A -> B",
                CaminosMinimos.nombres(linea, PlanVisitas.recorridoCompleto(fl, nn)));

        // Con la red real: estaciones críticas en tres localidades (una repetida y otra en la base).
        int base = g.indice("Villa Dolores");
        List<Estacion> criticas = new ArrayList<>();
        criticas.add(new Estacion(1, "E1", "Cosquín", "PLUVIOMETRICA", 5, 10));
        criticas.add(new Estacion(2, "E2", "Río Cuarto", "PLUVIOMETRICA", 10, 10));
        criticas.add(new Estacion(3, "E3", "Cosquín", "PLUVIOMETRICA", 15, 10));
        criticas.add(new Estacion(4, "E4", "Villa Dolores", "PLUVIOMETRICA", 1, 10));
        criticas.add(new Estacion(5, "E5", "Deán Funes", "PLUVIOMETRICA", 19, 10));
        List<Integer> locs = PlanVisitas.localidadesCriticas(g, criticas, base);
        verificar("localidades a visitar (sin repetir ni la base)", "3", String.valueOf(locs.size()));
        CaminosMinimos.Floyd f = CaminosMinimos.floyd(g);
        PlanVisitas.Recorrido r = PlanVisitas.vecinoMasCercano(f, base, locs);
        // VD -> Cosquín 164; desde Cosquín, DF (115) está más cerca que RC (254); DF -> RC 333 (por JM, Córdoba
        // y Río Tercero); RC -> VD 210. Total 822 km.
        verificar("orden", "Villa Dolores -> Cosquín -> Deán Funes -> Río Cuarto -> Villa Dolores",
                CaminosMinimos.nombres(g, r.orden));
        int esperado = f.dist[base][g.indice("Cosquín")] + f.dist[g.indice("Cosquín")][g.indice("Deán Funes")]
                + f.dist[g.indice("Deán Funes")][g.indice("Río Cuarto")] + f.dist[g.indice("Río Cuarto")][base];
        verificar("km totales", "822", String.valueOf(r.km));
        verificar("km totales = suma de tramos de Floyd", String.valueOf(esperado), String.valueOf(r.km));
        verificar("óptimo <= vecino más cercano", "true",
                String.valueOf(PlanVisitas.optimo(f, base, locs).km <= r.km));
        verificar("sin críticas: 0 km", "0",
                String.valueOf(PlanVisitas.vecinoMasCercano(f, base, new ArrayList<>()).km));
    }

    private static int km(Grafo g, String a, String b) {
        return g.km(g.indice(a), g.indice(b));
    }

    private static String ordenado(List<String> l) {
        List<String> c = new ArrayList<>(l);
        c.sort(null);
        return c.toString();
    }

    private static void verificar(String prueba, String esperado, String obtenido) {
        boolean ok = esperado.equals(obtenido);
        System.out.println((ok ? "OK    " : "FALLA ") + prueba
                + (ok ? "" : "  -> esperado: " + esperado + " | obtenido: " + obtenido));
    }
}
