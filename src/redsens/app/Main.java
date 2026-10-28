package redsens.app;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import redsens.modelo.Estacion;
import redsens.registro.*;
import redsens.sim.*;

/**
 * Menú de consola de RedSens. Por ahora tiene M6 (simulación) y M1 (tabla hash); los otros módulos se agregan acá.
 * Uso:  java -cp out redsens.app.Main [numeroDeGrupo]     (por defecto, grupo 4)
 */
public class Main {
    /** Nuestro grupo: semilla 4026, tabla 19, base Villa Dolores, M=4. */
    private static final int GRUPO_POR_DEFECTO = 4;
    private static ConfigGrupo cfg;
    private static TablaHash tabla;                // M1: registro por código
    private static InformeRendimiento inf;         // M6: informe de rendimiento
    private static List<Estacion> red;             // estaciones de la red cargada (se usan para las mediciones)

    /** Una acción del menú: imprime su resultado y puede pedir datos. */
    private interface Accion { void ejecutar(Scanner in) throws Exception; }

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        int grupo = args.length > 0 ? Integer.parseInt(args[0]) : GRUPO_POR_DEFECTO;
        cfg = ConfigGrupo.deGrupo(grupo);
        inf = new InformeRendimiento(cfg);
        Scanner in = new Scanner(System.in);

        while (true) {
            Consola.limpiar();
            Consola.titulo("RedSens - Red provincial de estaciones | Grupo " + cfg.grupo);
            Consola.info(String.format("semilla=%d  tabla inicial=%d  base=%s  M=%d", cfg.semilla, cfg.tamTablaInicial, cfg.baseCuadrilla, cfg.mJosephus));
            Consola.info(tabla == null ? "Red cargada: (ninguna todavía)" : "Red cargada: " + tabla.size() + " estaciones en " + tabla.nombre());
            Consola.subtitulo("Menú principal");
            System.out.println("   1) M1 - Registro de estaciones (tabla hash)");
            System.out.println("   2) M6 - Simulación y pruebas");
            System.out.println("   3) Informe de rendimiento");
            System.out.println("   0) Salir");
            switch (leer(in)) {
                case "1" -> seccion(in, "M1 - Registro de estaciones (tabla hash)", new String[]{
                        "Cargar la red (generar N estaciones)", "Buscar estación por código", "Eliminar estación por código",
                        "Estado de la tabla hash", "Experimento M1 (10.000 estaciones)"},
                    new Accion[]{Main::cargarRed, Main::buscar, Main::eliminar, i -> estado(), i -> experimentoM1()});
                case "2" -> seccion(in, "M6 - Simulación y pruebas", new String[]{
                        "Generar 10 estaciones de ejemplo", "Demostrar semillas (iguales / distintas)",
                        "Comprobar uniformidad (batería 0-100)", "Generar archivo de mediciones"},
                    new Accion[]{i -> opcion1(), i -> opcion2(), i -> opcion3(), i -> opcion4()});
                case "3" -> seccion(in, "Informe de rendimiento", new String[]{"Ver informe acumulado"},
                    new Accion[]{i -> System.out.println(inf.size() == 0
                        ? "\n  Todavía no hay mediciones. Ejecutá el experimento M1 desde la sección M1." : "\n" + inf.render())});
                case "0" -> { System.out.println("  ¡Hasta luego!"); return; }
                default -> System.out.println("  [!] Opción inválida. Elegí 0, 1, 2 o 3.");   // y se vuelve a dibujar el menú
            }
        }
    }

    /** Lee una opción; si se cierra la entrada devuelve "0". */
    private static String leer(Scanner in) {
        System.out.print("\n  Opción: ");
        return in.hasNextLine() ? in.nextLine().trim().toUpperCase() : "0";
    }

    /** Pantalla de una sección: título arriba, resultado de la acción y abajo las opciones (con "Volver"). */
    private static void seccion(Scanner in, String titulo, String[] etiquetas, Accion[] acciones) {
        Consola.limpiar();
        Consola.titulo(titulo);
        while (true) {
            Consola.subtitulo("¿Qué querés hacer?");
            for (int i = 0; i < etiquetas.length; i++) System.out.println("   " + (i + 1) + ") " + etiquetas[i]);
            System.out.println("   0) Volver al menú principal");
            String op = leer(in);
            if (op.equals("0")) return;

            Consola.limpiar();
            Consola.titulo(titulo);
            int idx;
            try { idx = Integer.parseInt(op) - 1; } catch (NumberFormatException e) { idx = -1; }
            if (idx < 0 || idx >= acciones.length) { Consola.error("Opción inválida: \"" + op + "\"."); continue; }
            try {
                System.out.println();
                Consola.subtitulo(etiquetas[idx]);
                acciones[idx].ejecutar(in);
            } catch (Exception e) {                  // no se muestra el stack trace
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
        // Se usa la batería de las 10.000 estaciones generadas por GeneradorDatos (el máximo que permite el formato CBA-NNNN).
        List<Estacion> est = new GeneradorDatos(cfg.semilla).generarEstaciones(GeneradorDatos.MAX_ESTACIONES);
        VerificadorAleatorio.imprimir(VerificadorAleatorio.compruebaUniformidadBateria(est), 10);
    }

    private static void opcion4() throws Exception {
        // Si hay una red cargada se usan esas estaciones; si no, se simulan 1.000.
        List<Estacion> est = red != null ? red : new GeneradorDatos(cfg.semilla).generarEstaciones(1000);
        int n = new GeneradorDatos(cfg.semilla).generarArchivoMediciones(Path.of("datos/mediciones.txt"), est, 5000, "2026-10-13");
        System.out.println();
        Consola.ok("Escrito datos/mediciones.txt con " + n + " líneas (de " + est.size() + " estaciones"
                + (red != null ? " de la red cargada" : " simuladas, no hay red cargada") + ").");
    }

    // ------------------------------------------------------------------ M1
    private static void cargarRed(Scanner in) {
        // Máximo 10.000: el formato CBA-NNNN tiene 4 dígitos (CBA-0000 a CBA-9999).
        int n = Consola.pedirEntero(in, "Cantidad de estaciones (1 a 10000)", 1000, 1, 10_000);
        System.out.println("   1) Exploración lineal\n   2) Exploración cuadrática\n   3) Encadenamiento (opcional)");
        int op = Consola.pedirEntero(in, "Tipo de tabla (1 a 3)", 1, 1, 3);
        int c = cfg.tamTablaInicial;                 // tamaño inicial asignado al grupo
        tabla = op == 2 ? new TablaExploracionCuadratica(c) : op == 3 ? new TablaEncadenada(c) : new TablaExploracionLineal(c);
        List<Estacion> est = new GeneradorDatos(cfg.semilla).generarEstaciones(n);
        red = est;
        long t0 = System.nanoTime();
        for (Estacion e : est) {
            tabla.insertar(e);
            // TODO M2: insertar también en el AVL.
        }
        double ms = (System.nanoTime() - t0) / 1e6;
        System.out.println();
        Consola.ok(String.format(Locale.ROOT, "Cargadas %d estaciones en %.2f ms.", n, ms));
        estado();
    }

    private static void buscar(Scanner in) {
        if (!hayTabla()) return;
        String cod = Consola.pedirTexto(in, "Código (ej. CBA-0347)").toUpperCase();
        Estadisticas s = tabla.estadisticas();
        long antes = s.sondeosBuscarOk + s.sondeosBuscarFail;
        Estacion e = tabla.buscar(cod);
        long sondeos = s.sondeosBuscarOk + s.sondeosBuscarFail - antes;
        System.out.println();
        if (e != null) Consola.ok("Encontrada: " + e); else Consola.error("No existe una estación con código " + cod);
        Consola.info("(sondeos en esta búsqueda: " + sondeos + ")");
    }

    private static void eliminar(Scanner in) {
        if (!hayTabla()) return;
        String cod = Consola.pedirTexto(in, "Código a eliminar").toUpperCase();
        System.out.println();
        if (tabla.eliminar(cod)) Consola.ok("Eliminada (borrado perezoso): " + cod);
        else Consola.error("No existe una estación con código " + cod);
    }

    private static void estado() {
        if (!hayTabla()) return;
        Estadisticas s = tabla.estadisticas();
        System.out.println();
        Consola.info("Tipo:           " + tabla.nombre());
        Consola.info(String.format(Locale.ROOT, "Elementos:      %d  (capacidad %d, factor de carga %.3f)", tabla.size(), tabla.capacidad(), tabla.factorCarga()));
        Consola.info(String.format(Locale.ROOT, "Inserciones:    %d  (%.2f sondeos c/u)", s.insertar, s.promInsertar()));
        Consola.info(String.format(Locale.ROOT, "Colisiones:     %d  en %d inserciones  (+%d durante rehash)", s.colisiones, s.insercionesConColision, s.colisionesRehash));
        Consola.info("Rehashings:     " + s.rehashings);
        Consola.info(String.format(Locale.ROOT, "Búsquedas:      %d exitosas (%.2f sondeos) · %d fallidas (%.2f sondeos)",
            s.buscarOk, s.promBuscarOk(), s.buscarFail, s.promBuscarFail()));
    }

    private static boolean hayTabla() {
        if (tabla == null) Consola.error("Primero cargá la red (M1 > opción 1).");
        return tabla != null;
    }

    private static void experimentoM1() throws Exception {
        int n = 10_000;
        System.out.println("\n  Ejecutando con " + n + " estaciones (unos segundos)...\n");
        List<ExperimentoM1.Fila> filas = new ExperimentoM1(cfg, n).ejecutar();
        System.out.println(ExperimentoM1.formatear(filas, n, cfg));
        System.out.println(ExperimentoM1.curvaCarga(cfg));
        inf.quitarModulo("M1");                      // para no duplicar filas si se repite el experimento
        ExperimentoM1.alInforme(filas, n, inf);
        inf.guardar(Path.of("datos/informe_rendimiento.txt"));
        Consola.ok("Resultados agregados al informe (datos/informe_rendimiento.txt).");
    }
}
