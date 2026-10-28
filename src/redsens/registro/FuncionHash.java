package redsens.registro;

/**
 * Función de hash para claves String. Devuelve un entero no negativo; la posición en la tabla
 * es hash % capacidad (por eso la capacidad conviene que sea prima).
 */
@FunctionalInterface
public interface FuncionHash {
    int hash(String clave);

    /**
     * Horner con base 31: h = s0·31^(n-1) + s1·31^(n-2) + ... + s(n-1).
     *  - Usa todos los caracteres y su posición. Con una suma simple, CBA-0123 y CBA-3210 darían igual.
     *  - 31 es primo e impar.
     *  - Una sola pasada sobre la clave (8 caracteres): O(1).
     *  - El desborde de int es a propósito; con & 0x7fffffff se saca el bit de signo para que no dé negativo.
     * Problema que vimos al medir: los códigos son consecutivos y Horner los deja en hashes consecutivos,
     * se forman bloques de celdas ocupadas y la exploración lineal rinde mucho peor que la teoría.
     * Por eso se agrega la mezcla de abajo.
     */
    FuncionHash HORNER = clave -> {
        int h = 0;
        for (int i = 0; i < clave.length(); i++) {
            h = 31 * h + clave.charAt(i);
        }
        return h & 0x7fffffff;
    };

    /**
     * Función elegida: Horner y después una mezcla final (dos desplazamientos con xor y una multiplicación).
     * Así claves consecutivas caen en posiciones lejanas y la lineal da los sondeos que predice la teoría.
     *  h ^= h >>> 16   : lleva los bits altos a los bajos.
     *  h *= 0x45d9f3b  : multiplica por una constante impar grande para esparcir los bits.
     *  h ^= h >>> 16   : mezcla otra vez.
     */
    FuncionHash HORNER_MEZCLA = clave -> {
        int h = HORNER.hash(clave);
        h ^= h >>> 16;
        h *= 0x45d9f3b;
        h ^= h >>> 16;
        return h & 0x7fffffff;
    };

    /** Función que usan todas las tablas por defecto. Si se cambia acá, cambia en todo el sistema. */
    FuncionHash POR_DEFECTO = HORNER_MEZCLA;

    /**
     * Suma de los códigos de los caracteres. Solo para comparar en el experimento.
     * Es mala: para CBA-NNNN da unos 60 valores distintos, así que casi todas las claves chocan.
     */
    FuncionHash SUMA = clave -> {
        int h = 0;
        for (int i = 0; i < clave.length(); i++) h += clave.charAt(i);
        return h;
    };

    /** Solo para pruebas: el hash es el número del código (CBA-0089 -> 89), como en los ejemplos de las lecturas. */
    FuncionHash NUMERO = clave -> Integer.parseInt(clave.substring(clave.indexOf('-') + 1));
}
