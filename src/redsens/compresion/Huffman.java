package redsens.compresion; // CAMBIO 1: faltaba esta línea

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Huffman {

    // devuelve true si el nodo no tiene hijos
    public static boolean esHoja(Nodo raiz) {
        return raiz.izq == null && raiz.der == null;
    }

    // recorre el arbol: izquierda = '0', derecha = '1'.
    // Al llegar a una hoja, el camino recorrido es el codigo de ese caracter.
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


    public static int decodificar(Nodo raiz, int index, StringBuilder sb, StringBuilder salida) {
        if (raiz == null) {
            return index;
        }

        // llegamos a una hoja: encontramos un caracter completo
        if (esHoja(raiz)) {
            salida.append(raiz.caracter);
            return index;
        }

        index++; // consumimos el siguiente bit

        raiz = (sb.charAt(index) == '0') ? raiz.izq : raiz.der;
        return decodificar(raiz, index, sb, salida);
    }


    // Cuenta la aparicion de cada caracter y almacena este dato en un Map
    public static Map<Character, Integer> contarFrecuencias(String texto) {
        Map<Character, Integer> frecuencia = new HashMap<>();
        for (char c : texto.toCharArray()) {
            frecuencia.put(c, frecuencia.getOrDefault(c, 0) + 1);
        }
        return frecuencia;
    }

    // Arma el arbol y devuelve la raiz.
    public static Nodo construirArbolHuffman(Map<Character, Integer> frecuencia) {

        // crea una cola de prioridad para almacenar los nodos del arbol de Huffman
        // el elem. de mayor prioridad tiene la menor frecuencia
        PriorityQueue<Nodo> colaPrioridad = new PriorityQueue<>(Comparator.comparingInt(n -> n.frecuencia));

        // Se recorre en orden de caracter para que el arbol salga SIEMPRE igual con
        // las mismas frecuencias. Hace falta para poder descomprimir un archivo
        // en otra ejecucion.

        List<Character> claves = new ArrayList<>(frecuencia.keySet());
        Collections.sort(claves);
        for (char c : claves) {
            colaPrioridad.add(new Nodo(c, frecuencia.get(c)));
        }

        // unir los dos nodos de menor frecuencia hasta que quede uno solo
        while (colaPrioridad.size() > 1) {
            Nodo izq = colaPrioridad.poll();
            Nodo der = colaPrioridad.poll();

            int suma = izq.frecuencia + der.frecuencia;
            colaPrioridad.add(new Nodo(null, suma, izq, der));
        }

        return colaPrioridad.peek();
    }

    // Tabla de codigos (caracter -> bits) usando codificar
    public static Map<Character, String> generarCodigos(Nodo raiz) {
        Map<Character, String> codigoHuffman = new HashMap<>();
        codificar(raiz, "", codigoHuffman);
        return codigoHuffman;
    }

    // Reemplaza cada caracter del texto por su codigo
    public static StringBuilder codificarTexto(String texto, Map<Character, String> codigoHuffman) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            sb.append(codigoHuffman.get(c));
        }
        return sb;
    }

    // Recorre todos los bits y devuelve el texto decodificado
    public static String decodificarTexto(Nodo raiz, StringBuilder sb) {
        StringBuilder salida = new StringBuilder();
        if (esHoja(raiz)) {
            // un solo caracter distinto, cada bit es una repeticion
            for (int i = 0; i < sb.length(); i++) {
                salida.append(raiz.caracter);
            }
        } else {
            int index = -1;
            while (index < sb.length() - 1) {
                index = decodificar(raiz, index, sb, salida);
            }
        }
        return salida.toString();
    }

    // Verifica que ningun codigo sea prefijo de otro
    public static boolean libreDePrefijos(Map<Character, String> codigoHuffman) {
        for (var a : codigoHuffman.entrySet()) {
            for (var b : codigoHuffman.entrySet()) {
                if (!a.getKey().equals(b.getKey()) && b.getValue().startsWith(a.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    // Demo para textos cortos (como WWXWYZWX): hace todo y lo imprime.
    // Ahora usa los metodos de arriba, el resto es igual que antes.
    public static void construirArbol(String texto) {

        // el texto esta vacio?
        if (texto == null || texto.length() == 0) {
            return;
        }

        Map<Character, Integer> frecuencia = contarFrecuencias(texto);
        Nodo raiz = construirArbolHuffman(frecuencia);

        // 4) generar los códigos
        Map<Character, String> codigoHuffman = generarCodigos(raiz);

        System.out.println("Códigos de Huffman:");
        for (var entry : codigoHuffman.entrySet()) {
            System.out.println("'" + entry.getKey() + "' -> " + entry.getValue());
        }

        System.out.println("\nTexto original: " + texto);

        // 5) codificar el texto
        StringBuilder sb = codificarTexto(texto, codigoHuffman);
        System.out.println("Texto codificado: " + sb);

        // 6) decodificar
        System.out.println("Texto decodificado: " + decodificarTexto(raiz, sb));
    }
}