package redsens.app;

import java.util.List;
import redsens.indice.ArbolABB;
import redsens.indice.IndiceEstaciones;
import redsens.modelo.Estacion;

/** Verificación del M2 con un caso chico, comprobable a mano. */
public class VerificacionM2 {

    public static void main(String[] args) {
        IndiceEstaciones abb = new ArbolABB();

        int[] numeros = { 50, 30, 70, 20, 40, 60, 80 };
        int[] baterias = { 90, 15, 80, 10, 50, 19, 20 };
        for (int i = 0; i < numeros.length; i++) {
            abb.insertar(new Estacion(numeros[i], "Estación " + numeros[i],
                    "Córdoba", "PLUVIOMETRICA", baterias[i], 10));
        }

        verificar("inorden", "20 30 40 50 60 70 80", nums(abb.inorden()));
        verificar("preorden", "50 30 20 40 70 60 80", nums(abb.preorden()));
        verificar("postorden", "20 40 30 60 80 70 50", nums(abb.postorden()));
        verificar("rango 35..65", "40 50 60", nums(abb.listarPorRango(35, 65)));
        verificar("críticas", "20 30 60", nums(abb.detectarCriticas()));
        verificar("altura", "3", String.valueOf(abb.altura()));
        verificar("nodos", "7", String.valueOf(abb.contarNodos()));
        verificar("hojas", "4", String.valueOf(abb.contarHojas()));
        verificar("buscar 60", "encontrada", abb.buscar(60) != null ? "encontrada" : "no existe");
        verificar("buscar 55", "no existe", abb.buscar(55) != null ? "encontrada" : "no existe");

        abb.insertar(new Estacion(50, "Repetida", "Córdoba", "PLUVIOMETRICA", 90, 5));
        verificar("size tras repetir el 50", "7", String.valueOf(abb.size()));

        probarEliminar();
    }

    private static void probarEliminar() {
        IndiceEstaciones abb = new ArbolABB();
        int[] numeros = { 50, 30, 70, 20, 40, 60, 80 };
        for (int n : numeros) {
            abb.insertar(new Estacion(n, "E" + n, "Córdoba", "PLUVIOMETRICA", 50, 10));
        }

        verificar("eliminar hoja (20)", "true", String.valueOf(abb.eliminar(20)));
        verificar("  inorden tras hoja", "30 40 50 60 70 80", nums(abb.inorden()));

        verificar("eliminar con un hijo (30)", "true", String.valueOf(abb.eliminar(30)));
        verificar("  preorden tras un hijo", "50 40 70 60 80", nums(abb.preorden()));

        verificar("eliminar raíz con dos hijos (50)", "true", String.valueOf(abb.eliminar(50)));
        verificar("  preorden tras dos hijos", "60 40 70 80", nums(abb.preorden()));
        verificar("  inorden sigue ordenado", "40 60 70 80", nums(abb.inorden()));

        verificar("eliminar inexistente (99)", "false", String.valueOf(abb.eliminar(99)));
        verificar("  size", "4", String.valueOf(abb.size()));

        abb.eliminar(40);
        abb.eliminar(60);
        abb.eliminar(70);
        abb.eliminar(80);
        verificar("eliminar todo: size", "0", String.valueOf(abb.size()));
        verificar("  altura", "0", String.valueOf(abb.altura()));
    }

    private static String nums(List<Estacion> lista) {
        StringBuilder sb = new StringBuilder();
        for (Estacion e : lista) {
            if (sb.length() > 0)
                sb.append(' ');
            sb.append(e.getNumero());
        }
        return sb.toString();
    }

    private static void verificar(String prueba, String esperado, String obtenido) {
        boolean ok = esperado.equals(obtenido);
        System.out.println((ok ? "OK    " : "FALLA ") + prueba
                + (ok ? "" : "  -> esperado: " + esperado + " | obtenido: " + obtenido));
    }
}