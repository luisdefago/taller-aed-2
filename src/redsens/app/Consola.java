package redsens.app;

import java.util.Scanner;

/**
 * Pantalla de consola: limpiar, títulos, mensajes y lectura de datos con validación.
 * La pantalla se limpia con códigos ANSI solo si se pasa -Dredsens.ansi=true (lo hace launch.json).
 * No se fuerza la codificación de salida: forzar UTF-8 rompía las tildes en la terminal de Windows.
 */
public final class Consola {
    private static final boolean ANSI = Boolean.getBoolean("redsens.ansi");
    private static final int ANCHO = 78;

    private Consola() { }

    public static void limpiar() {
        if (ANSI) { System.out.print("\033[H\033[2J"); System.out.flush(); }
        else System.out.println("\n".repeat(2));
    }

    public static void titulo(String texto) {
        System.out.println("=".repeat(ANCHO));
        System.out.println("  " + texto);
        System.out.println("=".repeat(ANCHO));
    }

    public static void subtitulo(String texto) { System.out.println("\n-- " + texto + " " + "-".repeat(Math.max(0, ANCHO - texto.length() - 4))); }

    public static void info(String texto) { System.out.println("  " + texto); }

    public static void error(String texto) { System.out.println("  [!] " + texto); }

    public static void ok(String texto) { System.out.println("  [OK] " + texto); }

    /** Pide un entero (mínimo dado). Si se deja vacío devuelve el valor por defecto; si es inválido, repite. */
    public static int pedirEntero(Scanner in, String pregunta, int defecto, int minimo) {
        return pedirEntero(in, pregunta, defecto, minimo, Integer.MAX_VALUE);
    }

    /** Igual que la anterior, con un máximo incluido. */
    public static int pedirEntero(Scanner in, String pregunta, int defecto, int minimo, int maximo) {
        while (true) {
            System.out.print("  " + pregunta + " [" + defecto + "]: ");
            String s = in.hasNextLine() ? in.nextLine().trim() : "";
            if (s.isEmpty()) return defecto;
            try {
                int v = Integer.parseInt(s);
                if (v >= minimo && v <= maximo) return v;
                if (maximo == Integer.MAX_VALUE) error("Ingresá un número mayor o igual a " + minimo + ".");
                else error("Ingresá un número entre " + minimo + " y " + maximo + ".");
            } catch (NumberFormatException e) {
                error("\"" + s + "\" no es un número entero.");
            }
        }
    }

    public static String pedirTexto(Scanner in, String pregunta) {
        System.out.print("  " + pregunta + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : "";
    }
}
