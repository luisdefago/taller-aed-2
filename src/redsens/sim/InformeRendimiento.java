package redsens.sim;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * M6: mide tiempos (System.nanoTime) y junta los resultados de los experimentos de M1, M2 y M5
 * en una sola tabla. Cada módulo agrega sus filas con medir o agregar.
 */
public class InformeRendimiento {
    public static class Fila {
        final String modulo, experimento, variante, detalle; final double ms;
        Fila(String m, String e, String v, double ms, String d) { modulo = m; experimento = e; variante = v; this.ms = ms; detalle = d; }
    }

    private final List<Fila> filas = new ArrayList<>();
    private final ConfigGrupo cfg;

    public InformeRendimiento(ConfigGrupo cfg) { this.cfg = cfg; }

    /** Ejecuta la tarea, mide el tiempo y agrega la fila. Devuelve lo que devuelve la tarea. */
    public <T> T medir(String modulo, String experimento, String variante, Supplier<T> tarea, java.util.function.Function<T, String> detalle) {
        long t0 = System.nanoTime();
        T r = tarea.get();
        long t1 = System.nanoTime();
        agregar(modulo, experimento, variante, (t1 - t0) / 1e6, detalle == null ? "" : detalle.apply(r));
        return r;
    }

    public void agregar(String modulo, String experimento, String variante, double ms, String detalle) {
        filas.add(new Fila(modulo, experimento, variante, ms, detalle));
    }

    public int size() { return filas.size(); }

    /** Borra las filas de un módulo (para no duplicarlas si se repite el experimento). */
    public void quitarModulo(String modulo) { filas.removeIf(f -> f.modulo.equals(modulo)); }

    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("INFORME DE RENDIMIENTO - RedSens - ").append(cfg).append('\n');
        sb.append(String.format("%-4s %-34s %-22s %12s  %s%n", "Mód", "Experimento", "Variante", "Tiempo (ms)", "Detalle"));
        sb.append("-".repeat(100)).append('\n');
        for (Fila f : filas)
            sb.append(String.format(java.util.Locale.ROOT, "%-4s %-34s %-22s %12.3f  %s%n", f.modulo, f.experimento, f.variante, f.ms, f.detalle));
        return sb.toString();
    }

    public void guardar(Path destino) throws IOException {
        if (destino.getParent() != null) Files.createDirectories(destino.getParent());
        Files.writeString(destino, render(), StandardCharsets.UTF_8);
    }
}
