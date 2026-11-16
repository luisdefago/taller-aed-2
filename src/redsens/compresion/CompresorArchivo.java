package redsens.compresion;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Comprime/descomprime un archivo de texto a disco con Huffman (M4).
 * Formato del .huf: [cantidad de simbolos][(char, frecuencia) * n][cantidad de bits][bits empaquetados].
 * Se guardan las frecuencias y se reconstruye el mismo arbol al descomprimir.
 */

public final class CompresorArchivo {
    private CompresorArchivo() {
    }

    public static void comprimir(Path origen, Path destino) throws IOException {
        String texto = Files.readString(origen, StandardCharsets.UTF_8);
        Map<Character, Integer> frec = Huffman.contarFrecuencias(texto);
        Nodo raiz = Huffman.construirArbolHuffman(frec);
        StringBuilder bits = Huffman.codificarTexto(texto, Huffman.generarCodigos(raiz));

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(destino)))) {
            out.writeInt(frec.size());
            for (var e : frec.entrySet()) {
                out.writeChar(e.getKey());
                out.writeInt(e.getValue());
            }
            out.writeInt(bits.length());
            for (int i = 0; i < bits.length(); i += 8) { // empaqueta 8 bits por byte
                int b = 0;
                for (int j = 0; j < 8; j++) {
                    b <<= 1;
                    if (i + j < bits.length() && bits.charAt(i + j) == '1')
                        b |= 1;
                }
                out.writeByte(b);
            }
        }
    }

    public static void descomprimir(Path origen, Path destino) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(origen)))) {
            int n = in.readInt();
            Map<Character, Integer> frec = new HashMap<>();
            for (int i = 0; i < n; i++)
                frec.put(in.readChar(), in.readInt());
            int totalBits = in.readInt();
            StringBuilder bits = new StringBuilder(totalBits);
            for (int leidos = 0; leidos < totalBits; leidos += 8) {
                int b = in.readUnsignedByte();
                for (int j = 7; j >= 0 && leidos + (7 - j) < totalBits; j--)
                    bits.append(((b >> j) & 1) == 1 ? '1' : '0');
            }
            Nodo raiz = Huffman.construirArbolHuffman(frec);
            Files.writeString(destino, Huffman.decodificarTexto(raiz, bits), StandardCharsets.UTF_8);
        }
    }
}
