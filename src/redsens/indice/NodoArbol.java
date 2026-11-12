package redsens.indice;

import redsens.modelo.Estacion;

/** Nodo de un árbol binario de búsqueda. La clave es dato.getNumero(). */
class NodoArbol {
    Estacion dato;
    NodoArbol izq;
    NodoArbol der;
    int altura; // solo lo usa el AVL; el ABB lo ignora

    NodoArbol(Estacion dato) {
        this.dato = dato;
        this.altura = 1; // un nodo sin hijos tiene altura 1
    }

    int clave() {
        return dato.getNumero();
    }
}
