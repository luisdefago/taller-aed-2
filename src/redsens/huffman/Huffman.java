import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Huffman {

    // devuelve true si el nodo no tiene hijos
    public static boolean esHoja(Nodo raiz) {
        return raiz.izq == null && raiz.der == null;
    }

    public static void codificar(Nodo raiz, String str, Map<Character, String> codigoHuffman) {
        if (raiz == null) {
            return;
        }

        if (esHoja(raiz)) {
            codigoHuffman.put(raiz.caracter, str.length() > 0 ? str : "1");
            return;
        }

        codificar(raiz.izq, str + '0', codigoHuffman);
        codificar(raiz.der, str + '1', codigoHuffman);
    }

    public static int decodificar(Nodo raiz, int index, StringBuilder sb) {
        if (raiz == null) {
            return index;
        }

        // encuentra el nodo raiz
        if (esHoja(raiz)) {
            System.out.print(raiz.caracter);
            return index;
        }

        index++;

        raiz = (sb.charAt(index) == '0') ? raiz.izq : raiz.der;
        index = decodificar(raiz, index, sb);
        return decodificar(raiz, index, sb);
    }

    public static void construirArbol(String texto) {

        // el texto esta vacio?
        if (texto == null || texto.length() == 0) {
            return;
        }

        // Cuenta la aparicion de cada caracter y almacena este dato en un Map
        Map<Character, Integer> frecuencia = new HashMap<>();
        for (char c : texto.toCharArray()) {
            frecuencia.put(c, frecuencia.getOrDefault(c, 0) + 1);
        }

        // crea una cola de prioridad para almacenar los nodos del arbol de Huffman
        // el elem. de mayor prioridad tiene la menor frecuencia
        PriorityQueue<Nodo> colaPrioridad = new PriorityQueue<>(Comparator.comparingInt(n -> n.frecuencia));

        for (var entry : frecuencia.entrySet()) {
            colaPrioridad.add(new Nodo(entry.getKey(), entry.getValue()));
        }

        // unir los dos nodos de menor frecuencia hasta que quede uno solo
        while (colaPrioridad.size() > 1) {
            Nodo izq = colaPrioridad.poll();
            Nodo der = colaPrioridad.poll();

            int suma = izq.frecuencia + der.frecuencia;
            colaPrioridad.add(new Nodo(null, suma, izq, der));
        }

        Nodo raiz = colaPrioridad.peek();

        // 4) generar los códigos
        Map<Character, String> codigoHuffman = new HashMap<>();
        codificar(raiz, "", codigoHuffman);

        System.out.println("Códigos de Huffman:");
        for (var entry : codigoHuffman.entrySet()) {
            System.out.println("'" + entry.getKey() + "' -> " + entry.getValue());
        }

        System.out.println("\nTexto original: " + texto);

        // 5) codificar el texto
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            sb.append(codigoHuffman.get(c));
        }
        System.out.println("Texto codificado: " + sb);

        // 6) decodificar
        System.out.print("Texto decodificado: ");
        if (esHoja(raiz)) {
            // un solo caracter distinto, cada bit es una repetición
            for (int i = 0; i < sb.length(); i++) {
                System.out.print(raiz.caracter);
            }
        } else {
            int index = -1;
            while (index < sb.length() - 1) {
                index = decodificar(raiz, index, sb);
            }
        }
        System.out.println();
    }
}

