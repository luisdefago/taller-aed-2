package redsens.app;

import java.util.List;
import redsens.indice.ExperimentoM2;
import redsens.indice.ExperimentoM2.Fila;

/** Corre el experimento obligatorio del M2 y muestra la tabla. */
public class ExperimentoM2Main {

    public static void main(String[] args) {
        long semilla = 4026; // semilla del grupo 4
        ExperimentoM2 experimento = new ExperimentoM2(semilla);
        List<Fila> filas = experimento.ejecutar(1000, 5000, 10000);
        System.out.println(ExperimentoM2.formatear(filas, semilla));
    }
}