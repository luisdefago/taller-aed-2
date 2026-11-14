package redsens.guardias;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * M5 - Solución simple de Josephus (como en la lectura de Weiss): lista enlazada + iterador.
 *
 * Los técnicos 1..N están en ronda. Se cuentan M pasadas y el técnico alcanzado sale.
 * Costo O(N·M): para sacar a cada técnico el iterador avanza M+1 lugares.
 */
public class JosephusLista {

    /** Devuelve el orden de salida de los técnicos 1..n. El último de la lista es el que queda. */
    public static List<Integer> ordenDeSalida(int n, int m) {
        if (n < 1 || m < 0) throw new IllegalArgumentException("Se necesita n >= 1 y m >= 0");

        // Armar la ronda: 1, 2, 3, ..., n
        LinkedList<Integer> ronda = new LinkedList<>();
        for (int i = 1; i <= n; i++) ronda.add(i);

        List<Integer> orden = new ArrayList<>();
        ListIterator<Integer> it = ronda.listIterator();

        while (ronda.size() > 1) {
            int alcanzado = 0;
            // M pasadas + 1 paso más para llegar al técnico alcanzado
            for (int paso = 0; paso <= m; paso++) {
                if (!it.hasNext()) it = ronda.listIterator();   // llegó al final: vuelve al principio (es una ronda)
                alcanzado = it.next();
            }
            orden.add(alcanzado);
            it.remove();   // sale de la ronda; el iterador queda listo para seguir desde el siguiente
        }

        orden.add(ronda.getFirst());   // el que queda: suplente de emergencias
        return orden;
    }
}
