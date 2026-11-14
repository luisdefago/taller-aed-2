package redsens.guardias;

/**
 * ABB donde cada nodo guarda cuántos nodos hay en su subárbol (contando a sí mismo).
 * Con ese dato se encuentra el k-ésimo menor en O(log N), sin recorrer todo el árbol.
 *
 * El árbol se arma equilibrado desde el principio y después solo se borra,
 * así que su altura nunca crece: no hace falta AVL (lo explica la lectura citando a Weiss).
 */
public class ArbolRango {

    private static class Nodo {
        int valor;
        int cantidad;    // nodos en este subárbol
        Nodo izq, der;

        Nodo(int valor) { this.valor = valor; }
    }

    private Nodo raiz;

    /** Carga los números 1..n equilibrados (como cargarArbolBinario de la lectura). */
    public ArbolRango(int n) {
        raiz = cargar(1, n);
    }

    /** El del medio va a la raíz; la mitad menor a la izquierda y la mayor a la derecha. */
    private Nodo cargar(int desde, int hasta) {
        if (desde > hasta) return null;
        int medio = (desde + hasta) / 2;
        Nodo nodo = new Nodo(medio);
        nodo.izq = cargar(desde, medio - 1);
        nodo.der = cargar(medio + 1, hasta);
        actualizar(nodo);
        return nodo;
    }

    /** Devuelve el k-ésimo menor (k empieza en 1). */
    public int kesimo(int k) {
        if (k < 1 || k > size()) throw new IllegalArgumentException("k fuera de rango: " + k);
        Nodo nodo = raiz;
        while (true) {
            int izquierda = cantidad(nodo.izq);
            if (k <= izquierda) {
                nodo = nodo.izq;                 // está entre los menores
            } else if (k == izquierda + 1) {
                return nodo.valor;               // es este nodo
            } else {
                k -= izquierda + 1;              // descarto los menores y este nodo
                nodo = nodo.der;
            }
        }
    }

    /** Borra un valor que está en el árbol. */
    public void eliminar(int valor) {
        raiz = eliminar(raiz, valor);
    }

    private Nodo eliminar(Nodo nodo, int valor) {
        if (nodo == null) throw new IllegalArgumentException("No está en el árbol: " + valor);

        if (valor < nodo.valor) {
            nodo.izq = eliminar(nodo.izq, valor);
        } else if (valor > nodo.valor) {
            nodo.der = eliminar(nodo.der, valor);
        } else {
            // Encontrado. Con 0 o 1 hijo, lo reemplaza su hijo.
            if (nodo.izq == null) return nodo.der;
            if (nodo.der == null) return nodo.izq;
            // Con 2 hijos: toma el valor del menor de la derecha y se borra ese.
            Nodo menor = nodo.der;
            while (menor.izq != null) menor = menor.izq;
            nodo.valor = menor.valor;
            nodo.der = eliminar(nodo.der, menor.valor);
        }
        actualizar(nodo);   // al volver de la recursión, cada nodo del camino corrige su cantidad
        return nodo;
    }

    private static int cantidad(Nodo nodo) { return nodo == null ? 0 : nodo.cantidad; }

    private static void actualizar(Nodo nodo) { nodo.cantidad = 1 + cantidad(nodo.izq) + cantidad(nodo.der); }

    public int size() { return cantidad(raiz); }

    /** Altura (un solo nodo = 1). Se usa en las pruebas para ver que el árbol está equilibrado. */
    public int altura() { return altura(raiz); }

    private int altura(Nodo nodo) {
        return nodo == null ? 0 : 1 + Math.max(altura(nodo.izq), altura(nodo.der));
    }
}
