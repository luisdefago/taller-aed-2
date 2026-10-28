package redsens.registro;

import redsens.modelo.Estacion;

/**
 * Celda ocupada de la tabla. Una celda vacía es null.
 * Con activo == false es una lápida (borrado perezoso): sigue ocupada para no cortar las búsquedas,
 * pero ya no cuenta como elemento.
 */
class EntradaHash {
    Estacion estacion;
    boolean activo = true;

    EntradaHash(Estacion e) { this.estacion = e; }
}
