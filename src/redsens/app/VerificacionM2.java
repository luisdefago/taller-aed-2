package redsens.app;

import java.util.List;
import redsens.indice.ArbolABB;
import redsens.indice.ArbolAVL;
import redsens.indice.IndiceEstaciones;
import redsens.modelo.Estacion;

/** Verificación del M2 con casos chicos, comprobables a mano. */
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
        probarAVL();
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

    private static void probarAVL() {
        System.out.println("--- AVL ---");

        verificar("rot. simple izquierda (10,20,30)", "20 10 30", preordenAVL(10, 20, 30));
        verificar("rot. simple derecha (30,20,10)", "20 10 30", preordenAVL(30, 20, 10));
        verificar("rot. doble izq-der (30,10,20)", "20 10 30", preordenAVL(30, 10, 20));
        verificar("rot. doble der-izq (10,30,20)", "20 10 30", preordenAVL(10, 30, 20));

        IndiceEstaciones siete = armarAVL(1, 2, 3, 4, 5, 6, 7);
        verificar("1..7 en orden: preorden", "4 2 1 3 6 5 7", nums(siete.preorden()));
        verificar("1..7 en orden: altura", "3", String.valueOf(siete.altura()));

        IndiceEstaciones avl = new ArbolAVL();
        IndiceEstaciones abb = new ArbolABB();
        for (int i = 1; i <= 1000; i++) {
            Estacion e = new Estacion(i, "E" + i, "Córdoba", "PLUVIOMETRICA", 50, 10);
            avl.insertar(e);
            abb.insertar(e);
        }
        verificar("AVL con 1000 en orden: nodos", "1000", String.valueOf(avl.contarNodos()));
        verificar("AVL con 1000 en orden: altura <= 14", "true", String.valueOf(avl.altura() <= 14));
        verificar("ABB con 1000 en orden: altura", "1000", String.valueOf(abb.altura()));
        System.out.println("      (altura AVL = " + avl.altura() + " | altura ABB = " + abb.altura() + ")");
    }

    private static IndiceEstaciones armarAVL(int... claves) {
        IndiceEstaciones avl = new ArbolAVL();
        for (int c : claves) {
            avl.insertar(new Estacion(c, "E" + c, "Córdoba", "PLUVIOMETRICA", 50, 10));
        }
        return avl;
    }

    private static String preordenAVL(int... claves) {
        return nums(armarAVL(claves).preorden());
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