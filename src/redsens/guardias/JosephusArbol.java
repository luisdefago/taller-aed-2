package redsens.guardias;

import java.util.ArrayList;
import java.util.List;

/**
 * M5 - Solución eficiente de Josephus: ABB con acceso al k-ésimo (ArbolRango).
 *
 * En vez de avanzar de a un técnico, se calcula directamente qué posición sale
 * y se la busca en el árbol. Cada salida cuesta O(log N), en total O(N log N).
 */
public class JosephusArbol {

    /** Mismo resultado que JosephusLista.ordenDeSalida, pero sin depender de M. */
    public static List<Integer> ordenDeSalida(int n, int m) {
        if (n < 1 || m < 0) throw new IllegalArgumentException("Se necesita n >= 1 y m >= 0");

        ArbolRango ronda = new ArbolRango(n);
        List<Integer> orden = new ArrayList<>();
        int pos = 0;   // posición (desde 0) donde arranca la cuenta

        for (int quedan = n; quedan > 1; quedan--) {
            pos = (pos + m % quedan) % quedan;      // M pasadas dando vueltas a la ronda
            int alcanzado = ronda.kesimo(pos + 1);  // kesimo cuenta desde 1
            orden.add(alcanzado);
            ronda.eliminar(alcanzado);
            // pos no cambia: ahora apunta al técnico que estaba después del que salió
        }

        orden.add(ronda.kesimo(1));   // el que queda: suplente de emergencias
        return orden;
    }
}
