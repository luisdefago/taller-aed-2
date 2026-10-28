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
        throw new UnsupportedOperationException("Falta implementar (Paso 4)");
    }

    @Override
    public String nombre() {
        return "ABB";
    }
}