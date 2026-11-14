package redsens.guardias;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * M5 - Calendario de guardias a partir del orden de salida de Josephus.
 * El 1.º que sale hace la guardia del 1.er fin de semana, el 2.º la del 2.º, etc.
 * El último que queda en la ronda es el suplente de emergencias.
 */
public class CalendarioGuardias {

    /** Primer sábado después del día de las mediciones simuladas (13/10/2026). */
    public static final LocalDate PRIMER_SABADO = LocalDate.of(2026, 10, 17);

    private final int n, m;
    private final List<Integer> orden;

    public CalendarioGuardias(int n, int m) {
        this.n = n;
        this.m = m;
        this.orden = JosephusLista.ordenDeSalida(n, m);
    }

    /** Técnicos en el orden en que hacen guardia (sin el suplente). */
    public List<Integer> guardias() { return orden.subList(0, orden.size() - 1); }

    public int suplente() { return orden.get(orden.size() - 1); }

    public static String nombre(int tecnico) { return String.format("Técnico %02d", tecnico); }

    /** Tabla lista para imprimir. */
    public String texto() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder();
        sb.append("Calendario de guardias: N = ").append(n).append(" técnicos, M = ").append(m).append(" pasadas\n\n");
        sb.append("  Fin de semana   Sábado       Técnico\n");
        List<Integer> guardias = guardias();
        for (int i = 0; i < guardias.size(); i++) {
            LocalDate sabado = PRIMER_SABADO.plusWeeks(i);
            sb.append(String.format("  %8d        %s   %s%n", i + 1, sabado.format(f), nombre(guardias.get(i))));
        }
        sb.append("\n  Suplente de emergencias: ").append(nombre(suplente())).append('\n');
        return sb.toString();
    }
}
