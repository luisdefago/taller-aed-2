package redsens.registro;

/** Números primos: el tamaño de la tabla hash conviene que sea primo. */
public final class Primos {
    private Primos() { }

    /** Prueba dividiendo hasta la raíz de n. Alcanza para tamaños de tabla. */
    public static boolean esPrimo(int n) {
        if (n < 2) return false;
        if (n % 2 == 0) return n == 2;
        for (int d = 3; (long) d * d <= n; d += 2) {
            if (n % d == 0) return false;
        }
        return true;
    }

    /** Menor primo mayor o igual a n. */
    public static int siguientePrimo(int n) {
        int p = Math.max(n, 2);
        while (!esPrimo(p)) p++;
        return p;
    }
}
