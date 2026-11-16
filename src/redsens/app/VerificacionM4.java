package redsens.app;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import redsens.compresion.CompresorArchivo;
import redsens.compresion.Huffman;
import redsens.compresion.Nodo;
import redsens.modelo.Estacion;
import redsens.sim.ConfigGrupo;
import redsens.sim.GeneradorDatos;

/**
 * Pruebas del M4 (sin JUnit): caso de la lectura, códigos libres de prefijos, casos límite
 * y el archivo de mediciones comprimido y descomprimido.
 * Ejecutar:  java -cp out redsens.app.VerificacionM4 [grupo]   (por defecto, grupo 4)
 */
public class VerificacionM4 {
    static int fallos = 0;
    static void check(String nombre, boolean ok) { System.out.println((ok ? "OK    " : "FALLO ") + nombre); if (!ok) fallos++; }

    /** Comprime y descomprime un texto en memoria; devuelve el texto recuperado. */
    static String idaYVuelta(String texto) {
        Nodo raiz = Huffman.construirArbolHuffman(Huffman.contarFrecuencias(texto));
        StringBuilder bits = Huffman.codificarTexto(texto, Huffman.generarCodigos(raiz));
        return Huffman.decodificarTexto(raiz, bits);
    }

    public static void main(String[] args) throws Exception {
        ConfigGrupo cfg = ConfigGrupo.deGrupo(args.length > 0 ? Integer.parseInt(args[0]) : 4);
        System.out.println(cfg);

        // ---- Caso de la lectura: WWXWYZWX ocupa 64 bits fijos y 14 con Huffman
        String s = "WWXWYZWX";
        Map<Character, Integer> frec = Huffman.contarFrecuencias(s);
        Nodo raiz = Huffman.construirArbolHuffman(frec);
        Map<Character, String> codigos = Huffman.generarCodigos(raiz);
        StringBuilder bits = Huffman.codificarTexto(s, codigos);
        check("frecuencias: W=4, X=2, Y=1, Z=1", frec.get('W') == 4 && frec.get('X') == 2 && frec.get('Y') == 1 && frec.get('Z') == 1);
        check("tamaño fijo: 8 caracteres x 8 bits = 64 bits", 8 * s.length() == 64);
        check("tamaño con Huffman: 14 bits (lectura)", bits.length() == 14);
        check("W (la más frecuente) tiene el código más corto (1 bit)", codigos.get('W').length() == 1);
        check("ningún código es prefijo de otro", Huffman.libreDePrefijos(codigos));
        check("decodificar WWXWYZWX devuelve el mismo texto", Huffman.decodificarTexto(raiz, bits).equals(s));

        // ---- El chequeo de prefijos detecta un código mal armado
        check("libreDePrefijos detecta A=0, B=01 como ambiguo", !Huffman.libreDePrefijos(Map.of('A', "0", 'B', "01")));

        // ---- Casos límite
        check("un solo símbolo (aaaa): se recupera igual", idaYVuelta("aaaa").equals("aaaa"));
        check("un solo carácter (a): se recupera igual", idaYVuelta("a").equals("a"));
        check("dos símbolos (abab): se recupera igual", idaYVuelta("abab").equals("abab"));
        String especiales = "Río Cuarto;Deán Funes\nT=18.4;H=62\n\tñ";
        check("tildes, ñ, saltos de línea y tabs: se recupera igual", idaYVuelta(especiales).equals(especiales));

        // ---- Archivo de mediciones (M6 -> M4): al menos 5.000 líneas, comprimir a disco y descomprimir
        Path dir = Files.createTempDirectory("redsens-m4");
        Path txt = dir.resolve("mediciones.txt"), huf = dir.resolve("mediciones.huf"), out = dir.resolve("descomprimido.txt");
        List<Estacion> est = new GeneradorDatos(cfg.semilla).generarEstaciones(1000);
        int lineas = new GeneradorDatos(cfg.semilla).generarArchivoMediciones(txt, est, 5000, "2026-10-13");
        String texto = Files.readString(txt, StandardCharsets.UTF_8);
        check("el archivo de mediciones tiene 5.000 líneas", lineas == 5000 && texto.split("\n").length == 5000);

        Map<Character, String> cod = Huffman.generarCodigos(Huffman.construirArbolHuffman(Huffman.contarFrecuencias(texto)));
        long original = 8L * texto.length();
        long comprimido = Huffman.codificarTexto(texto, cod).length();
        System.out.printf("      (original %d bits, comprimido %d bits, ahorro %.1f %%)%n",
                original, comprimido, 100.0 * (1 - (double) comprimido / original));
        check("mediciones: ningún código es prefijo de otro", Huffman.libreDePrefijos(cod));
        check("mediciones: el comprimido ocupa menos bits que el original", comprimido < original);

        CompresorArchivo.comprimir(txt, huf);
        CompresorArchivo.descomprimir(huf, out);
        check("el archivo .huf en disco es más chico que el original", Files.size(huf) < Files.size(txt));
        check("el archivo descomprimido es idéntico al original", Files.readString(out, StandardCharsets.UTF_8).equals(texto));

        // Limpieza de los archivos temporales
        Files.deleteIfExists(txt); Files.deleteIfExists(huf); Files.deleteIfExists(out); Files.deleteIfExists(dir);

        System.out.println(fallos == 0 ? "\nTODO OK" : "\n" + fallos + " FALLO(S)");
        if (fallos > 0) System.exit(1);
    }
}
