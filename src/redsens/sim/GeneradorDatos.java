package redsens.sim;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import redsens.modelo.Estacion;

/**
 * M6: genera estaciones y mediciones simuladas.
 * Todo sale de un solo Random con la semilla del grupo, así los resultados se pueden repetir.
 */
public class GeneradorDatos {
    /** Las 12 localidades del grafo (M3). */
    public static final String[] LOCALIDADES = {
        "Córdoba", "Villa Carlos Paz", "Alta Gracia", "Jesús María", "Río Tercero", "Villa María",
        "San Francisco", "Cosquín", "La Falda", "Deán Funes", "Río Cuarto", "Villa Dolores"
    };
    /** Máximo de estaciones: el código CBA-NNNN llega hasta CBA-9999. */
    public static final int MAX_ESTACIONES = 10_000;
    public static final String[] TIPOS = {"PLUVIOMETRICA", "METEOROLOGICA", "HIDROMETRICA"};
    private static final String[] PREFIJOS = {"Estación", "Puesto", "Sensor"};
    private static final String[] SITIOS = {"Embalse", "Cerro", "Ruta", "Arroyo", "Chacra", "Dique", "Loma", "Valle"};
    private static final String[] PUNTOS = {"Norte", "Sur", "Este", "Oeste", "Centro", "Alto", "Bajo"};

    private final long semilla;
    private final Random rnd;

    public GeneradorDatos(long semilla) {
        this.semilla = semilla;
        this.rnd = new Random(semilla);
    }

    public long getSemilla() { return semilla; }

    /**
     * Genera n estaciones con números distintos de 0 a n-1 (10.000 estaciones = CBA-0000 a CBA-9999),
     * en orden aleatorio (Collections.shuffle con el Random del grupo).
     */
    public List<Estacion> generarEstaciones(int n) {
        if (n < 1 || n > MAX_ESTACIONES)
            throw new IllegalArgumentException("n debe estar entre 1 y " + MAX_ESTACIONES + " (el formato CBA-NNNN tiene 4 dígitos)");
        List<Integer> numeros = new ArrayList<>(n);
        for (int i = 0; i < n; i++) numeros.add(i);
        Collections.shuffle(numeros, rnd);
        List<Estacion> res = new ArrayList<>(n);
        for (int num : numeros) res.add(nuevaEstacion(num));
        return res;
    }

    /** Estaciones 0..n-1 ordenadas por número (el "censo ordenado" del experimento M2). */
    public List<Estacion> generarEstacionesOrdenadas(int n) {
        List<Estacion> l = generarEstaciones(n);
        l.sort((a, b) -> Integer.compare(a.getNumero(), b.getNumero()));
        return l;
    }

    private Estacion nuevaEstacion(int numero) {
        String nombre = PREFIJOS[rnd.nextInt(PREFIJOS.length)] + " " + SITIOS[rnd.nextInt(SITIOS.length)]
                + " " + PUNTOS[rnd.nextInt(PUNTOS.length)];
        String loc = LOCALIDADES[rnd.nextInt(LOCALIDADES.length)];
        String tipo = TIPOS[rnd.nextInt(TIPOS.length)];
        int bateria = rnd.nextInt(101);        // uniforme 0..100
        int dias = rnd.nextInt(366);           // 0..365
        return new Estacion(numero, nombre, loc, tipo, bateria, dias);
    }

    /**
     * Genera el archivo de mediciones del día (lo usa el M4).
     * Formato: CBA-0347;2026-10-13;08:00;T=18.4;H=62;P=0.0;BAT=18
     * @return cantidad de líneas escritas
     */
    public int generarArchivoMediciones(Path destino, List<Estacion> estaciones, int lineas, String fecha) throws IOException {
        if (estaciones.isEmpty()) throw new IllegalArgumentException("Sin estaciones");
        if (destino.getParent() != null) Files.createDirectories(destino.getParent());
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(destino, StandardCharsets.UTF_8))) {
            for (int i = 0; i < lineas; i++) {
                pw.print(lineaMedicion(estaciones.get(rnd.nextInt(estaciones.size())), fecha));
                pw.print('\n');
            }
        }
        return lineas;
    }

    private String lineaMedicion(Estacion e, String fecha) {
        int hora = rnd.nextInt(24), min = 15 * rnd.nextInt(4);
        double temp = 5 + rnd.nextDouble() * 30;           // 5.0 .. 35.0 °C
        int hum = 20 + rnd.nextInt(76);                    // 20..95 %
        double prec = rnd.nextInt(10) < 7 ? 0.0 : rnd.nextInt(300) / 10.0;  // 70 % sin lluvia
        int bat = Math.max(0, Math.min(100, e.getBateria() + rnd.nextInt(5) - 2));
        return String.format(java.util.Locale.ROOT, "%s;%s;%02d:%02d;T=%.1f;H=%d;P=%.1f;BAT=%d",
                e.getCodigo(), fecha, hora, min, temp, hum, prec, bat);
    }
}
