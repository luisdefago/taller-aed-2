package redsens.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import redsens.modelo.Estacion;
import redsens.sim.*;

/** Pruebas del M6 (sin JUnit): java -cp out redsens.app.VerificacionM6 [grupo]  (por defecto, grupo 4) */
public class VerificacionM6 {
    static int fallos = 0;
    static void check(String nombre, boolean ok) { System.out.println((ok ? "OK    " : "FALLO ") + nombre); if (!ok) fallos++; }

    public static void main(String[] a) throws Exception {
        ConfigGrupo cfg = ConfigGrupo.deGrupo(a.length > 0 ? Integer.parseInt(a[0]) : 4);
        System.out.println(cfg);
        check("parámetros grupo 15 = semilla 15026, tabla 67, Deán Funes, M=3",
                ConfigGrupo.deGrupo(15).semilla == 15026 && ConfigGrupo.deGrupo(15).tamTablaInicial == 67
                && ConfigGrupo.deGrupo(15).baseCuadrilla.equals("Deán Funes") && ConfigGrupo.deGrupo(15).mJosephus == 3);
        check("parámetros grupo 6 = 6026, 29, Río Cuarto, M=2",
                ConfigGrupo.deGrupo(6).semilla == 6026 && ConfigGrupo.deGrupo(6).tamTablaInicial == 29
                && ConfigGrupo.deGrupo(6).baseCuadrilla.equals("Río Cuarto") && ConfigGrupo.deGrupo(6).mJosephus == 2);

        check("misma semilla -> secuencias iguales", VerificadorAleatorio.secuenciasIguales(cfg.semilla, 1000, false));
        check("semillas distintas -> secuencias distintas", VerificadorAleatorio.secuenciasDistintas(cfg.semilla, cfg.semilla + 1, 1000, false));

        List<Estacion> e1 = new GeneradorDatos(cfg.semilla).generarEstaciones(10_000);
        List<Estacion> e2 = new GeneradorDatos(cfg.semilla).generarEstaciones(10_000);
        boolean rep = true;
        for (int i = 0; i < e1.size(); i++) if (!e1.get(i).toString().equals(e2.get(i).toString())) rep = false;
        check("generación reproducible con la misma semilla (10.000 estaciones)", rep);
        Set<String> cods = new HashSet<>(); Set<String> locs = new HashSet<>(Arrays.asList(GeneradorDatos.LOCALIDADES));
        boolean validas = true;
        for (Estacion e : e1) {
            cods.add(e.getCodigo());
            if (e.getBateria() < 0 || e.getBateria() > 100 || !locs.contains(e.getLocalidad())) validas = false;
        }
        check("10.000 códigos únicos", cods.size() == 10_000);
        boolean rechaza = false;
        try { new GeneradorDatos(cfg.semilla).generarEstaciones(10_001); } catch (IllegalArgumentException ex) { rechaza = true; }
        check("no se generan más de 10.000 estaciones (formato CBA-NNNN)", rechaza);
        check("batería 0..100 y localidades válidas", validas);

        var u = VerificadorAleatorio.compruebaUniformidad(new Random(cfg.semilla), 100, 100_000);
        check("uniformidad batería 0-100 (chi² " + String.format("%.1f", u.chi2) + " <= " + String.format("%.1f", u.chi2Critico) + ")", u.uniforme);
        var ub = VerificadorAleatorio.compruebaUniformidadBateria(new GeneradorDatos(cfg.semilla).generarEstaciones(10_000));
        check("uniformidad de la batería de 10.000 estaciones generadas (chi² " + String.format("%.1f", ub.chi2) + " <= " + String.format("%.1f", ub.chi2Critico) + ")", ub.uniforme);
        Random sesgado = new Random(1) { @Override public int nextInt(int n) { return super.nextInt(n) / 2; } };
        check("un generador sesgado es rechazado", !VerificadorAleatorio.compruebaUniformidad(sesgado, 100, 100_000).uniforme);

        Path f = Path.of("datos/mediciones_test.txt");
        new GeneradorDatos(cfg.semilla).generarArchivoMediciones(f, e1, 5000, "2026-10-13");
        List<String> ls = Files.readAllLines(f);
        boolean fmt = ls.size() == 5000;
        for (String l : ls) if (!l.matches("CBA-\\d{4};\\d{4}-\\d{2}-\\d{2};\\d{2}:\\d{2};T=-?\\d+\\.\\d;H=\\d+;P=\\d+\\.\\d;BAT=\\d+")) { fmt = false; System.out.println(l); break; }
        check("archivo de 5.000 líneas con formato correcto", fmt);
        System.out.println("Ejemplo: " + ls.get(0));
        Files.delete(f);

        InformeRendimiento inf = new InformeRendimiento(cfg);
        inf.medir("M6", "demo", "suma", () -> { long s = 0; for (int i = 0; i < 1_000_000; i++) s += i; return s; }, r -> "s=" + r);
        check("informe registra filas con tiempo", inf.size() == 1 && inf.render().contains("demo"));

        System.out.println(fallos == 0 ? "\nTODO OK" : "\n" + fallos + " FALLO(S)");
        System.exit(fallos == 0 ? 0 : 1);
    }
}
