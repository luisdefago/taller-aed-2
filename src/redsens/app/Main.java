package redsens.app;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import redsens.indice.ArbolAVL; // M2
import redsens.indice.ExperimentoM2; // M2
import redsens.indice.IndiceEstaciones; // M2
import redsens.modelo.Estacion;
import redsens.red.CaminosMinimos; // M3
import redsens.red.Grafo; // M3
import redsens.red.PlanVisitas; // M3
import redsens.red.Recorridos; // M3
import redsens.registro.*;
import redsens.sim.*;

/**
 * Menú de consola de RedSens. Por ahora tiene M6 (simulación), M2 (índice
 * ordenado) y M1 (tabla hash); los otros módulos se agregan acá.
 * Uso: java -cp out redsens.app.Main [numeroDeGrupo] (por defecto, grupo 4)
 */
public class Main {
    /** Nuestro grupo: semilla 4026, tabla 19, base Villa Dolores, M=4. */
    private static final int GRUPO_POR_DEFECTO = 4;
    private static ConfigGrupo cfg;
    private static TablaHash tabla; // M1: registro por código
    private static IndiceEstaciones indice; // M2: índice ordenado por número (AVL)
    private static InformeRendimiento inf; // M6: informe de rendimiento
    private static List<Estacion> red; // estaciones de la red cargada (se usan para las mediciones)
    private static Grafo vial; // M3: red vial cargada desde datos/rutas.txt
    private static final Path RUTAS = Path.of("datos/rutas.txt"); // M3

    /** Una acción del menú: imprime su resultado y puede pedir datos. */
    private interface Accion {
        void ejecutar(Scanner in) throws Exception;
    }

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        int grupo = args.length > 0 ? Integer.parseInt(args[0]) : GRUPO_POR_DEFECTO;
        cfg = ConfigGrupo.deGrupo(grupo);
        inf = new InformeRendimiento(cfg);
        Scanner in = new Scanner(System.in);

        while (true) {
            Consola.limpiar();
            Consola.titulo("RedSens - Red provincial de estaciones | Grupo " + cfg.grupo);
            Consola.info(String.format("semilla=%d  tabla inicial=%d  base=%s  M=%d", cfg.semilla, cfg.tamTablaInicial,
                    cfg.baseCuadrilla, cfg.mJosephus));
            Consola.info(tabla == null ? "Red cargada: (ninguna todavía)"
                    : "Red cargada: " + tabla.size() + " estaciones en " + tabla.nombre());
            Consola.subtitulo("Menú principal");
            System.out.println("   1) M1 - Registro de estaciones (tabla hash)");
            System.out.println("   2) M6 - Simulación y pruebas");
            System.out.println("   3) Informe de rendimiento");
            System.out.println("   4) M2 - Índice ordenado (AVL)"); // M2
            System.out.println("   5) M3 - Red vial y plan de visitas (grafos)"); // M3
            System.out.println("   0) Salir");
            switch (leer(in)) {
                case "1" -> seccion(in, "M1 - Registro de estaciones (tabla hash)", new String[] {
                        "Cargar la red (generar N estaciones)", "Buscar estación por código",
                        "Eliminar estación por código",
                        "Estado de la tabla hash", "Experimento M1 (10.000 estaciones)" },
                        new Accion[] { Main::cargarRed, Main::buscar, Main::eliminar, i -> estado(),
                                i -> experimentoM1() });
                case "2" -> seccion(in, "M6 - Simulación y pruebas", new String[] {
                        "Generar 10 estaciones de ejemplo", "Demostrar semillas (iguales / distintas)",
                        "Comprobar uniformidad (batería 0-100)", "Generar archivo de mediciones" },
                        new Accion[] { i -> opcion1(), i -> opcion2(), i -> opcion3(), i -> opcion4() });
                case "3" -> seccion(in, "Informe de rendimiento", new String[] { "Ver informe acumulado" },
                        new Accion[] { i -> System.out.println(inf.size() == 0
                                ? "\n  Todavía no hay mediciones. Ejecutá el experimento M1 desde la sección M1."
                                : "\n" + inf.render()) });
                case "4" -> seccion(in, "M2 - Índice ordenado (AVL)", new String[] { // M2
                        "Listar estaciones por rango de números", "Reporte de estaciones críticas",
                        "Estado del índice (altura, nodos, hojas)", "Recorridos (inorden, preorden, postorden)",
                        "Experimento M2 (ABB vs AVL)" },
                        new Accion[] { Main::listarRango, i -> reporteCriticas(), i -> estadoIndice(),
                                i -> recorridos(), i -> experimentoM2() });
                case "5" -> seccion(in, "M3 - Red vial y plan de visitas (grafos)", new String[] { // M3
                        "Ver la red (matriz y lista de adyacencia)", "BFS desde la base (niveles 1, 2 y 3)",
                        "DFS: conexidad y corte de rutas", "Dijkstra: distancias y caminos desde una localidad",
                        "Floyd: distancias mínimas entre todos los pares",
                        "Plan de visitas de la cuadrilla (críticas del M2)" },
                        new Accion[] { i -> verRed(), i -> bfsBase(), Main::cortarRutas, Main::dijkstra,
                                i -> floyd(), i -> planVisitas() });
                case "0" -> {
                    System.out.println("  ¡Hasta luego!");
                    return;
                }
                default -> System.out.println("  [!] Opción inválida. Elegí 0, 1, 2, 3, 4 o 5."); // y se vuelve a
                                                                                                  // dibujar el menú
            }
        }
    }

    /** Lee una opción; si se cierra la entrada devuelve "0". */
    private static String leer(Scanner in) {
        System.out.print("\n  Opción: ");
        return in.hasNextLine() ? in.nextLine().trim().toUpperCase() : "0";
    }

    /**
     * Pantalla de una sección: título arriba, resultado de la acción y abajo las
     * opciones (con "Volver").
     */
    private static void seccion(Scanner in, String titulo, String[] etiquetas, Accion[] acciones) {
        Consola.limpiar();
        Consola.titulo(titulo);
        while (true) {
            Consola.subtitulo("¿Qué querés hacer?");
            for (int i = 0; i < etiquetas.length; i++)
                System.out.println("   " + (i + 1) + ") " + etiquetas[i]);
            System.out.println("   0) Volver al menú principal");
            String op = leer(in);
            if (op.equals("0"))
                return;

            Consola.limpiar();
            Consola.titulo(titulo);
            int idx;
            try {
                idx = Integer.parseInt(op) - 1;
            } catch (NumberFormatException e) {
                idx = -1;
            }
            if (idx < 0 || idx >= acciones.length) {
                Consola.error("Opción inválida: \"" + op + "\".");
                continue;
            }
            try {
                System.out.println();
                Consola.subtitulo(etiquetas[idx]);
                acciones[idx].ejecutar(in);
            } catch (Exception e) { // no se muestra el stack trace
                Consola.error("Ocurrió un problema: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------ M6
    private static void opcion1() {
        System.out.println();
        new GeneradorDatos(cfg.semilla).generarEstaciones(10).forEach(e -> System.out.println("  " + e));
    }

    private static void opcion2() {
        System.out.println();
        Consola.info("Misma semilla (" + cfg.semilla + "):");
        boolean iguales = VerificadorAleatorio.secuenciasIguales(cfg.semilla, 1000, true);
        Consola.info("-> ¿secuencias iguales? " + (iguales ? "SÍ (correcto)" : "NO (error)"));
        System.out.println();
        Consola.info("Semillas distintas (" + cfg.semilla + " y " + (cfg.semilla + 1) + "):");
        boolean distintas = VerificadorAleatorio.secuenciasDistintas(cfg.semilla, cfg.semilla + 1, 1000, true);
        Consola.info("-> ¿secuencias distintas? " + (distintas ? "SÍ (correcto)" : "NO (error)"));
    }

    private static void opcion3() {
        System.out.println();
        // Se usa la batería de las 10.000 estaciones generadas por GeneradorDatos (el
        // máximo que permite el formato CBA-NNNN).
        List<Estacion> est = new GeneradorDatos(cfg.semilla).generarEstaciones(GeneradorDatos.MAX_ESTACIONES);
        VerificadorAleatorio.imprimir(VerificadorAleatorio.compruebaUniformidadBateria(est), 10);
    }

    private static void opcion4() throws Exception {
        // Si hay una red cargada se usan esas estaciones; si no, se simulan 1.000.
        List<Estacion> est = red != null ? red : new GeneradorDatos(cfg.semilla).generarEstaciones(1000);
        int n = new GeneradorDatos(cfg.semilla).generarArchivoMediciones(Path.of("datos/mediciones.txt"), est, 5000,
                "2026-10-13");
        System.out.println();
        Consola.ok("Escrito datos/mediciones.txt con " + n + " líneas (de " + est.size() + " estaciones"
                + (red != null ? " de la red cargada" : " simuladas, no hay red cargada") + ").");
    }

    // ------------------------------------------------------------------ M2
    private static void listarRango(Scanner in) {
        if (!hayIndice())
            return;
        Consola.info("Los números de estación van de 0 a " + (indice.size() - 1) + ".");
        int desde = Consola.pedirEntero(in, "Desde el número", 0, 0);
        int hasta = Consola.pedirEntero(in, "Hasta el número", desde + 99, desde);
        List<Estacion> r = indice.listarPorRango(desde, hasta);
        System.out.println();
        Consola.info("Estaciones con número entre " + desde + " y " + hasta + " (en orden): " + r.size());
        mostrar(r, 20);
    }

    private static void reporteCriticas() {
        if (!hayIndice())
            return;
        List<Estacion> c = indice.detectarCriticas();
        System.out.println();
        Consola.info("Estaciones críticas (batería menor a " + Estacion.BATERIA_CRITICA + "%), ordenadas por número: "
                + c.size() + " de " + indice.size());
        mostrar(c, 20);
    }

    private static void estadoIndice() {
        if (!hayIndice())
            return;
        int n = indice.size();
        int ideal = Integer.SIZE - Integer.numberOfLeadingZeros(n); // altura mínima posible con n nodos
        System.out.println();
        Consola.info("Tipo:     " + indice.nombre());
        Consola.info("Nodos:    " + indice.contarNodos() + "  (size = " + n + ")");
        Consola.info("Hojas:    " + indice.contarHojas());
        Consola.info("Altura:   " + indice.altura() + "  (la mínima posible con " + n + " nodos es " + ideal + ")");
    }

    private static void recorridos() {
        if (!hayIndice())
            return;
        System.out.println();
        Consola.info("Primeros 10 números de cada recorrido:");
        Consola.info("inorden:   " + numeros(indice.inorden(), 10));
        Consola.info("preorden:  " + numeros(indice.preorden(), 10));
        Consola.info("postorden: " + numeros(indice.postorden(), 10));
    }

    private static void experimentoM2() throws Exception {
        System.out.println("\n  Ejecutando con 1.000, 5.000 y 10.000 estaciones (unos segundos)...\n");
        List<ExperimentoM2.Fila> filas = new ExperimentoM2(cfg.semilla).ejecutar(1000, 5000, 10000);
        System.out.println(ExperimentoM2.formatear(filas, cfg.semilla));
        inf.quitarModulo("M2"); // para no duplicar filas si se repite el experimento
        ExperimentoM2.alInforme(filas, inf);
        inf.guardar(Path.of("datos/informe_rendimiento.txt"));
        Consola.ok("Resultados agregados al informe (datos/informe_rendimiento.txt).");
    }

    private static boolean hayIndice() {
        if (indice == null)
            Consola.error("Primero cargá la red (M1 > opción 1).");
        return indice != null;
    }

    /** Muestra las primeras max estaciones de la lista. */
    private static void mostrar(List<Estacion> lista, int max) {
        if (lista.isEmpty()) {
            Consola.info("(ninguna)");
            return;
        }
        for (int i = 0; i < Math.min(max, lista.size()); i++)
            System.out.println("  " + lista.get(i));
        if (lista.size() > max)
            Consola.info("... y " + (lista.size() - max) + " más (se muestran las primeras " + max + ").");
    }

    private static String numeros(List<Estacion> lista, int max) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(max, lista.size()); i++)
            sb.append(lista.get(i).getNumero()).append(' ');
        return sb.toString().trim() + (lista.size() > max ? " ..." : "");
    }

    // ------------------------------------------------------------------ M3
    /** Carga la red vial desde datos/rutas.txt la primera vez que se usa. */
    private static Grafo grafo() throws Exception {
        if (vial == null)
            vial = Grafo.desdeArchivo(RUTAS);
        return vial;
    }

    private static int base(Grafo g) {
        int b = g.indice(cfg.baseCuadrilla);
        if (b < 0)
            throw new IllegalStateException("La base " + cfg.baseCuadrilla + " no está en " + RUTAS);
        return b;
    }

    /** Muestra las localidades numeradas y pide una (por defecto, la indicada). */
    private static int pedirLocalidad(Scanner in, Grafo g, String pregunta, int defecto) {
        for (int i = 0; i < g.cantVertices(); i++)
            System.out.printf("   %2d) %s%n", i + 1, g.nombre(i));
        return Consola.pedirEntero(in, pregunta, defecto + 1, 1, g.cantVertices()) - 1;
    }

    private static void verRed() throws Exception {
        Grafo g = grafo();
        System.out.println();
        Consola.info("Cargada desde " + RUTAS + ": " + g.cantVertices() + " localidades y " + g.cantAristas()
                + " rutas (no dirigido, km).");
        System.out.println("\n  Matriz de adyacencia (km; \"-\" = sin ruta directa):\n");
        System.out.println(g.renderMatriz().indent(2));
        System.out.println("  Lista de adyacencia:\n");
        System.out.println(g.renderLista().indent(2));
        int posibles = g.cantVertices() * (g.cantVertices() - 1) / 2;
        Consola.info(String.format(Locale.ROOT, "Densidad: %d de %d rutas posibles (%.0f %%). Conviene la lista:",
                g.cantAristas(), posibles, 100.0 * g.cantAristas() / posibles));
        Consola.info("BFS, DFS y Dijkstra recorren solo los vecinos reales (O(V + E)); la matriz se usa en Floyd.");
    }

    private static void bfsBase() throws Exception {
        Grafo g = grafo();
        int b = base(g);
        int[] nivel = Recorridos.bfsNiveles(g, b);
        System.out.println();
        Consola.info("BFS desde la base (" + g.nombre(b) + "):");
        for (int k = 1; k <= 3; k++)
            Consola.info("  a " + k + " tramo" + (k > 1 ? "s" : "") + ":  " + String.join(", ",
                    Recorridos.enNivel(g, nivel, k)));
        int max = 0;
        for (int n : nivel)
            max = Math.max(max, n);
        for (int k = 4; k <= max; k++)
            Consola.info("  a " + k + " tramos:  " + String.join(", ", Recorridos.enNivel(g, nivel, k))
                    + "  (fuera de los 3 tramos)");
        List<String> no = Recorridos.enNivel(g, nivel, Recorridos.INALCANZABLE);
        if (!no.isEmpty())
            Consola.info("  inalcanzables: " + String.join(", ", no));
    }

    private static void cortarRutas(Scanner in) throws Exception {
        Grafo g = grafo();
        System.out.println();
        Consola.info("Red original: " + (Recorridos.esConexa(g) ? "CONEXA (DFS llega a todas las localidades)"
                : "NO conexa"));
        List<String> puentes = Recorridos.puentes(g);
        Consola.info("Rutas que aíslan localidades si se cortan solas: "
                + (puentes.isEmpty() ? "ninguna (cada localidad tiene al menos dos caminos)"
                        : String.join(", ", puentes)));
        Consola.info("Ahora se simulan cortes (sobre una copia; la red original no cambia).");
        Grafo corte = g.copia();
        while (true) {
            System.out.println();
            int a = pedirLocalidad(in, corte, "Extremo A de la ruta a cortar", base(g));
            System.out.println("   (0 = terminar)");
            int bb = Consola.pedirEntero(in, "Extremo B de la ruta a cortar", 0, 0, corte.cantVertices()) - 1;
            if (bb < 0)
                break;
            if (!corte.quitarArista(a, bb)) {
                Consola.error("No hay ruta directa entre " + corte.nombre(a) + " y " + corte.nombre(bb) + ".");
                continue;
            }
            Consola.ok("Cortada la ruta " + corte.nombre(a) + " - " + corte.nombre(bb) + ".");
            List<String> aisladas = Recorridos.aisladasDesde(corte, base(g));
            if (Recorridos.esConexa(corte))
                Consola.info("La red sigue conexa: ninguna localidad queda aislada.");
            else
                Consola.error("La red quedó dividida. Desde la base (" + cfg.baseCuadrilla
                        + ") no se llega a: " + String.join(", ", aisladas));
            String seguir = Consola.pedirTexto(in, "¿Cortar otra ruta? (s/N)");
            if (!seguir.equalsIgnoreCase("s"))
                break;
        }
    }

    private static void dijkstra(Scanner in) throws Exception {
        Grafo g = grafo();
        Consola.info("Base del grupo: " + cfg.baseCuadrilla + ". Córdoba sirve como caso de control.");
        int o = pedirLocalidad(in, g, "Localidad de origen", base(g));
        CaminosMinimos.Dijkstra d = CaminosMinimos.dijkstra(g, o);
        System.out.println();
        Consola.info("Dijkstra desde " + g.nombre(o) + ":");
        System.out.println();
        for (int v = 0; v < g.cantVertices(); v++) {
            if (v == o)
                continue;
            String km = d.dist[v] == CaminosMinimos.INF ? "sin camino" : d.dist[v] + " km";
            System.out.printf("  %-17s %10s   %s%n", g.nombre(v), km, CaminosMinimos.nombres(g, d.camino(v)));
        }
    }

    private static void floyd() throws Exception {
        Grafo g = grafo();
        CaminosMinimos.Floyd f = CaminosMinimos.floyd(g);
        System.out.println("\n  Matriz de distancias mínimas en km (Floyd):\n");
        System.out.println(CaminosMinimos.renderFloyd(g, f).indent(2));
    }

    private static void planVisitas() throws Exception {
        if (!hayIndice())
            return;
        Grafo g = grafo();
        int b = base(g);
        List<Estacion> criticas = indice.detectarCriticas(); // reporte del M2
        int[] porLoc = PlanVisitas.criticasPorLocalidad(g, criticas);
        List<Integer> destinos = PlanVisitas.localidadesCriticas(g, criticas, b);
        System.out.println();
        Consola.info("Estaciones críticas (M2): " + criticas.size() + " en " + (destinos.size() + (porLoc[b] > 0 ? 1 : 0))
                + " localidades.");
        for (int v = 0; v < g.cantVertices(); v++)
            if (porLoc[v] > 0)
                Consola.info(String.format("  %-17s %4d%s", g.nombre(v), porLoc[v],
                        v == b ? "  (es la base: se atienden al salir)" : ""));
        if (destinos.isEmpty()) {
            Consola.ok("No hay localidades para visitar fuera de la base.");
            return;
        }
        CaminosMinimos.Floyd f = CaminosMinimos.floyd(g);
        PlanVisitas.Recorrido nn = PlanVisitas.vecinoMasCercano(f, b, destinos);
        System.out.println();
        Consola.subtitulo("Plan con el criterio del vecino más cercano (distancias de Floyd)");
        for (int i = 1; i < nn.orden.size(); i++) {
            int desde = nn.orden.get(i - 1), hasta = nn.orden.get(i);
            System.out.printf("  %2d) %-17s -> %-17s %4d km   %s%n", i, g.nombre(desde), g.nombre(hasta),
                    f.dist[desde][hasta], CaminosMinimos.nombres(g, f.camino(desde, hasta)));
        }
        System.out.println();
        Consola.ok("Total: " + nn.km + " km, saliendo y volviendo a " + g.nombre(b) + ".");
        Consola.info("Recorrido completo: " + CaminosMinimos.nombres(g, PlanVisitas.recorridoCompleto(f, nn)));

        if (destinos.size() <= PlanVisitas.MAX_OPTIMO) {
            PlanVisitas.Recorrido opt = PlanVisitas.optimo(f, b, destinos);
            System.out.println();
            Consola.info("Para comparar, el recorrido óptimo (probando todos los órdenes con Held-Karp):");
            Consola.info("  " + CaminosMinimos.nombres(g, opt.orden) + " = " + opt.km + " km");
            if (opt.km < nn.km)
                Consola.info(String.format(Locale.ROOT, "  El vecino más cercano hace %d km de más (%.1f %%).",
                        nn.km - opt.km, 100.0 * (nn.km - opt.km) / opt.km));
            else
                Consola.info("  En este caso el vecino más cercano coincide con el óptimo, pero no siempre pasa.");
        }
        Consola.info("¿Por qué no es óptimo? Elige el más cercano en cada paso sin mirar el resto: puede dejar");
        Consola.info("localidades lejanas para el final y pagar caro la vuelta a la base.");
    }

    // ------------------------------------------------------------------ M1
    private static void cargarRed(Scanner in) {
        // Máximo 10.000: el formato CBA-NNNN tiene 4 dígitos (CBA-0000 a CBA-9999).
        int n = Consola.pedirEntero(in, "Cantidad de estaciones (1 a 10000)", 1000, 1, 10_000);
        System.out.println("   1) Exploración lineal\n   2) Exploración cuadrática\n   3) Encadenamiento (opcional)");
        int op = Consola.pedirEntero(in, "Tipo de tabla (1 a 3)", 1, 1, 3);
        int c = cfg.tamTablaInicial; // tamaño inicial asignado al grupo
        tabla = op == 2 ? new TablaExploracionCuadratica(c)
                : op == 3 ? new TablaEncadenada(c) : new TablaExploracionLineal(c);
        List<Estacion> est = new GeneradorDatos(cfg.semilla).generarEstaciones(n);
        red = est;
        long t0 = System.nanoTime();
        for (Estacion e : est) {
            tabla.insertar(e);
        }
        double ms = (System.nanoTime() - t0) / 1e6;
        indice = new ArbolAVL(); // M2: las mismas estaciones, ahora en el índice ordenado
        for (Estacion e : est)
            indice.insertar(e); // M2 (fuera de la medición, que es solo de la tabla hash)
        System.out.println();
        Consola.ok(String.format(Locale.ROOT, "Cargadas %d estaciones en %.2f ms.", n, ms));
        estado();
    }

    private static void buscar(Scanner in) {
        if (!hayTabla())
            return;
        String cod = Consola.pedirTexto(in, "Código (ej. CBA-0347)").toUpperCase();
        Estadisticas s = tabla.estadisticas();
        long antes = s.sondeosBuscarOk + s.sondeosBuscarFail;
        Estacion e = tabla.buscar(cod);
        long sondeos = s.sondeosBuscarOk + s.sondeosBuscarFail - antes;
        System.out.println();
        if (e != null)
            Consola.ok("Encontrada: " + e);
        else
            Consola.error("No existe una estación con código " + cod);
        Consola.info("(sondeos en esta búsqueda: " + sondeos + ")");
    }

    private static void eliminar(Scanner in) {
        if (!hayTabla())
            return;
        String cod = Consola.pedirTexto(in, "Código a eliminar").toUpperCase();
        System.out.println();
        if (tabla.eliminar(cod))
            Consola.ok("Eliminada (borrado perezoso): " + cod);
        else
            Consola.error("No existe una estación con código " + cod);
    }

    private static void estado() {
        if (!hayTabla())
            return;
        Estadisticas s = tabla.estadisticas();
        System.out.println();
        Consola.info("Tipo:           " + tabla.nombre());
        Consola.info(String.format(Locale.ROOT, "Elementos:      %d  (capacidad %d, factor de carga %.3f)",
                tabla.size(), tabla.capacidad(), tabla.factorCarga()));
        Consola.info(
                String.format(Locale.ROOT, "Inserciones:    %d  (%.2f sondeos c/u)", s.insertar, s.promInsertar()));
        Consola.info(String.format(Locale.ROOT, "Colisiones:     %d  en %d inserciones  (+%d durante rehash)",
                s.colisiones, s.insercionesConColision, s.colisionesRehash));
        Consola.info("Rehashings:     " + s.rehashings);
        Consola.info(
                String.format(Locale.ROOT, "Búsquedas:      %d exitosas (%.2f sondeos) · %d fallidas (%.2f sondeos)",
                        s.buscarOk, s.promBuscarOk(), s.buscarFail, s.promBuscarFail()));
    }

    private static boolean hayTabla() {
        if (tabla == null)
            Consola.error("Primero cargá la red (M1 > opción 1).");
        return tabla != null;
    }

    private static void experimentoM1() throws Exception {
        int n = 10_000;
        System.out.println("\n  Ejecutando con " + n + " estaciones (unos segundos)...\n");
        List<ExperimentoM1.Fila> filas = new ExperimentoM1(cfg, n).ejecutar();
        System.out.println(ExperimentoM1.formatear(filas, n, cfg));
        System.out.println(ExperimentoM1.curvaCarga(cfg));
        inf.quitarModulo("M1"); // para no duplicar filas si se repite el experimento
        ExperimentoM1.alInforme(filas, n, inf);
        inf.guardar(Path.of("datos/informe_rendimiento.txt"));
        Consola.ok("Resultados agregados al informe (datos/informe_rendimiento.txt).");
    }
}