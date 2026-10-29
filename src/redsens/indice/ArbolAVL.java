package redsens.indice;

import redsens.modelo.Estacion;

/**
 * Árbol AVL: ABB que se rebalancea con rotaciones. Clave: número de estación.
 */
public class ArbolAVL extends ArbolBase {

    private boolean insertoNuevo; // para saber si la cantidad de nodos crece

    @Override
    public void insertar(Estacion e) {
        insertoNuevo = false;
        raiz = insertar(raiz, e);
        if (insertoNuevo)
            cantidad++;
    }

    /**
     * Inserta recursivamente y devuelve la nueva raíz del subárbol ya balanceado.
     */
    private NodoArbol insertar(NodoArbol n, Estacion e) {
        if (n == null) { // caso base: lugar libre
            insertoNuevo = true;
            return new NodoArbol(e);
        }
        if (e.getNumero() < n.clave()) {
            n.izq = insertar(n.izq, e);
        } else if (e.getNumero() > n.clave()) {
            n.der = insertar(n.der, e);
        } else {
            n.dato = e; // mismo número: sobrescribe
            return n;
        }
        return rebalancear(n); // al volver de la recursión, rebalancea
    }

    // ---------- Altura y factor de equilibrio ----------
    private int alturaNodo(NodoArbol n) {
        return (n == null) ? 0 : n.altura;
    }

    private void actualizarAltura(NodoArbol n) {
        n.altura = 1 + Math.max(alturaNodo(n.izq), alturaNodo(n.der));
    }

    /** Factor de equilibrio = altura izquierda - altura derecha. */
    private int factorEquilibrio(NodoArbol n) {
        return (n == null) ? 0 : alturaNodo(n.izq) - alturaNodo(n.der);
    }

    // ---------- Rotaciones ----------
    private NodoArbol rotarDerecha(NodoArbol y) {
        NodoArbol x = y.izq;
        y.izq = x.der;
        x.der = y;
        actualizarAltura(y); // primero el que quedó abajo
        actualizarAltura(x);
        return x; // x es la nueva raíz del subárbol
    }

    private NodoArbol rotarIzquierda(NodoArbol x) {
        NodoArbol y = x.der;
        x.der = y.izq;
        y.izq = x;
        actualizarAltura(x);
        actualizarAltura(y);
        return y; // y es la nueva raíz del subárbol
    }

    private NodoArbol rebalancear(NodoArbol n) {
        actualizarAltura(n);
        int fe = factorEquilibrio(n);

        if (fe > 1) { // pesado a la izquierda
            if (factorEquilibrio(n.izq) < 0) // caso izquierda-derecha
                n.izq = rotarIzquierda(n.izq);
            return rotarDerecha(n);
        }
        if (fe < -1) { // pesado a la derecha
            if (factorEquilibrio(n.der) > 0) // caso derecha-izquierda
                n.der = rotarDerecha(n.der);
            return rotarIzquierda(n);
        }
        return n; // ya estaba balanceado
    }

    @Override
    public boolean eliminar(int numero) {
        throw new UnsupportedOperationException("Falta implementar (Paso 6)");
    }

    @Override
    public String nombre() {
        return "AVL";
    }
}