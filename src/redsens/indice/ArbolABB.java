package redsens.indice;

import redsens.modelo.Estacion;

/** Árbol binario de búsqueda sin balanceo. Clave: número de estación. */
public class ArbolABB extends ArbolBase {

    @Override
    public void insertar(Estacion e) {
        if (raiz == null) {
            raiz = new NodoArbol(e);
            cantidad++;
            return;
        }
        NodoArbol actual = raiz;
        while (true) {
            if (e.getNumero() == actual.clave()) {
                actual.dato = e; // mismo número: sobrescribe
                return;
            }
            if (e.getNumero() < actual.clave()) {
                if (actual.izq == null) {
                    actual.izq = new NodoArbol(e);
                    cantidad++;
                    return;
                }
                actual = actual.izq; // baja a la izquierda
            } else {
                if (actual.der == null) {
                    actual.der = new NodoArbol(e);
                    cantidad++;
                    return;
                }
                actual = actual.der; // baja a la derecha
            }
        }
    }

    @Override
    public boolean eliminar(int numero) {
        // Busca el nodo y recuerda a su padre
        NodoArbol padre = null;
        NodoArbol actual = raiz;
        while (actual != null && actual.clave() != numero) {
            padre = actual;
            actual = (numero < actual.clave()) ? actual.izq : actual.der;
        }
        if (actual == null)
            return false; // no existe

        // Caso 3: dos hijos -> se reemplaza por el sucesor inorden
        if (actual.izq != null && actual.der != null) {
            NodoArbol padreSucesor = actual;
            NodoArbol sucesor = actual.der;
            while (sucesor.izq != null) { // el menor del subárbol derecho
                padreSucesor = sucesor;
                sucesor = sucesor.izq;
            }
            actual.dato = sucesor.dato; // copia el dato del sucesor
            padre = padreSucesor; // ahora hay que borrar el sucesor
            actual = sucesor; // (tiene como mucho un hijo)
        }

        // Casos 1 y 2: actual tiene 0 o 1 hijo
        NodoArbol hijo = (actual.izq != null) ? actual.izq : actual.der;
        if (padre == null)
            raiz = hijo; // se borró la raíz
        else if (padre.izq == actual)
            padre.izq = hijo;
        else
            padre.der = hijo;

        cantidad--;
        return true;
    }

    @Override
    public String nombre() {
        return "ABB";
    }
}