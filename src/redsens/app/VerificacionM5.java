package redsens.app;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import redsens.guardias.*;
import redsens.sim.ConfigGrupo;

/**
 * Pruebas del M5 (sin JUnit): caso de la lectura, calendario del grupo, casos límite
 * y comparación lista vs. árbol con muchos N y M al azar.
 * Ejecutar:  java -cp out redsens.app.VerificacionM5 [grupo]   (por defecto, grupo 4)
 */
public class VerificacionM5 {
    static int fallos = 0;
    static void check(String nombre, boolean ok) { System.out.println((ok ? "OK    " : "FALLO ") + nombre); if (!ok) fallos++; }

    /** Sobreviviente por fórmula (recurrencia de Josephus), para controlar a las dos versiones. */
    static int sobreviviente(int n, int m) {
        int s = 0;                                  // con 1 técnico queda la posición 0
        for (int i = 2; i <= n; i++) s = (s + m + 1) % i;
        return s + 1;                               // de posición (desde 0) a número de técnico
    }

    /** True si el orden tiene a cada técnico 1..n exactamente una vez. */
    static boolean esPermutacion(List<Integer> orden, int n) {
        if (orden.size() != n) return false;
        boolean[] visto = new boolean[n + 1];
        for (int t : orden) {
            if (t < 1 || t > n || visto[t]) return false;
            visto[t] = true;
        }
        return true;
    }

    public static void main(String[] args) {
        ConfigGrupo cfg = ConfigGrupo.deGrupo(args.length > 0 ? Integer.parseInt(args[0]) : 4);
        System.out.println(cfg);

        // ---- Caso de la lectura: N = 5, M = 1 -> salen 2, 4, 1, 5 y queda el 3
        List<Integer> esperado = Arrays.asList(2, 4, 1, 5, 3);
        check("lista: N=5, M=1 -> 2, 4, 1, 5 y queda 3", JosephusLista.ordenDeSalida(5, 1).equals(esperado));
        check("árbol: N=5, M=1 -> 2, 4, 1, 5 y queda 3", JosephusArbol.ordenDeSalida(5, 1).equals(esperado));

        // ---- Casos límite
        check("1 técnico: no hay guardias y él es el suplente", JosephusLista.ordenDeSalida(1, 4).equals(List.of(1))
                && JosephusArbol.ordenDeSalida(1, 4).equals(List.of(1)));
        check("M=0: salen en orden 1, 2, 3, 4 y queda 5", JosephusLista.ordenDeSalida(5, 0).equals(List.of(1, 2, 3, 4, 5))
                && JosephusArbol.ordenDeSalida(5, 0).equals(List.of(1, 2, 3, 4, 5)));
        check("M mayor que N (N=3, M=10): lista y árbol coinciden",
                JosephusLista.ordenDeSalida(3, 10).equals(JosephusArbol.ordenDeSalida(3, 10)));
        boolean rechaza = false;
        try { JosephusLista.ordenDeSalida(0, 1); } catch (IllegalArgumentException e) { rechaza = true; }
        check("N=0 se rechaza", rechaza);

        // ---- Árbol con rango
        ArbolRango arbol = new ArbolRango(1000);
        check("árbol de 1000: altura 10 (equilibrado)", arbol.altura() == 10);
        check("árbol: el 1.º es 1, el 500.º es 500, el 1000.º es 1000",
                arbol.kesimo(1) == 1 && arbol.kesimo(500) == 500 && arbol.kesimo(1000) == 1000);
        arbol.eliminar(500);
        check("árbol: tras borrar 500, size 999 y el 500.º pasa a ser 501", arbol.size() == 999 && arbol.kesimo(500) == 501);

        // ---- Calendario del grupo: N = 12 y el M asignado
        CalendarioGuardias cal = new CalendarioGuardias(12, cfg.mJosephus);
        System.out.println();
        System.out.println(cal.texto());
        check("calendario: 11 guardias + 1 suplente, cada técnico una sola vez",
                cal.guardias().size() == 11 && esPermutacion(JosephusLista.ordenDeSalida(12, cfg.mJosephus), 12));
        check("calendario: el suplente coincide con la fórmula", cal.suplente() == sobreviviente(12, cfg.mJosephus));

        // ---- Muchos casos al azar (con la semilla del grupo): lista = árbol = fórmula
        Random rnd = new Random(cfg.semilla);
        boolean todoBien = true;
        for (int i = 0; i < 500; i++) {
            int n = 1 + rnd.nextInt(200), m = rnd.nextInt(300);
            List<Integer> lista = JosephusLista.ordenDeSalida(n, m);
            List<Integer> arb = JosephusArbol.ordenDeSalida(n, m);
            boolean ok = lista.equals(arb) && esPermutacion(lista, n) && lista.get(n - 1) == sobreviviente(n, m);
            if (!ok) { System.out.println("  falla con N=" + n + ", M=" + m); todoBien = false; }
        }
        check("500 casos al azar: lista = árbol = fórmula y nadie se repite", todoBien);

        System.out.println(fallos == 0 ? "\nTODO OK" : "\n" + fallos + " FALLO(S)");
        if (fallos > 0) System.exit(1);
    }
}
