package redsens.indice;

import java.util.ArrayList;
import java.util.List;
import redsens.modelo.Estacion;

/**
 * Lo que comparten el ABB y el AVL. Las subclases implementan insertar y
 * eliminar.
 */
public abstract class ArbolBase implements IndiceEstaciones {
    protected NodoArbol raiz;
    protected int cantidad;

    @Override
    public Estacion buscar(int numero) {
        NodoArbol actual = raiz;
        while (actual != null) {
            if (numero == actual.clave())
                return actual.dato;
            actual = (numero < actual.clave()) ? actual.izq : actual.der;
        }
        return null;
    }

    @Override
    public void vaciar() {
        raiz = null;
        cantidad = 0;
    }

    @Override
    public int size() {
        return cantidad;
    }

    // ---------- Recorridos ----------
    @Override
    public List<Estacion> inorden() {
        List<Estacion> r = new ArrayList<>();
        inorden(raiz, r);
        return r;
    }

    private void inorden(NodoArbol n, List<Estacion> out) {
        if (n == null)
            return; // caso base
        inorden(n.izq, out);
        out.add(n.dato);
        inorden(n.der, out);
    }

    @Override
    public List<Estacion> preorden() {
        List<Estacion> r = new ArrayList<>();
        preorden(raiz, r);
        return r;
    }

    private void preorden(NodoArbol n, List<Estacion> out) {
        if (n == null)
            return; // caso base
        out.add(n.dato);
        preorden(n.izq, out);
        preorden(n.der, out);
    }

    @Override
    public List<Estacion> postorden() {
        List<Estacion> r = new ArrayList<>();
        postorden(raiz, r);
        return r;
    }

    private void postorden(NodoArbol n, List<Estacion> out) {
        if (n == null)
            return; // caso base
        postorden(n.izq, out);
        postorden(n.der, out);
        out.add(n.dato);
    }

    // ---------- Consulta por rango ----------
    @Override
    public List<Estacion> listarPorRango(int desde, int hasta) {
        List<Estacion> r = new ArrayList<>();
        rango(raiz, desde, hasta, r);
        return r;
    }

    private void rango(NodoArbol n, int desde, int hasta, List<Estacion> out) {
        if (n == null)
            return; // caso base
        if (desde < n.clave())
            rango(n.izq, desde, hasta, out); // solo baja a la izquierda si puede haber claves >= desde
        if (n.clave() >= desde && n.clave() <= hasta)
            out.add(n.dato);
        if (hasta > n.clave())
            rango(n.der, desde, hasta, out); // solo baja a la derecha si puede haber claves <= hasta
    }

    // ---------- Reporte de críticas ----------
    @Override
    public List<Estacion> detectarCriticas() {
        List<Estacion> r = new ArrayList<>();
        criticas(raiz, r);
        return r;
    }

    private void criticas(NodoArbol n, List<Estacion> out) {
        if (n == null)
            return; // caso base
        criticas(n.izq, out);
        if (n.dato.esCritica())
            out.add(n.dato);
        criticas(n.der, out);
    }

    // ---------- Altura, nodos y hojas (recursivos) ----------
    @Override
    public int altura() {
        return alturaDe(raiz);
    }

    private int alturaDe(NodoArbol n) {
        if (n == null)
            return 0; // caso base: árbol vacío
        return 1 + Math.max(alturaDe(n.izq), alturaDe(n.der));
    }

    @Override
    public int contarNodos() {
        return nodosDe(raiz);
    }

    private int nodosDe(NodoArbol n) {
        if (n == null)
            return 0; // caso base
        return 1 + nodosDe(n.izq) + nodosDe(n.der);
    }

    @Override
    public int contarHojas() {
        return hojasDe(raiz);
    }

    private int hojasDe(NodoArbol n) {
        if (n == null)
            return 0; // caso base 1: árbol vacío
        if (n.izq == null && n.der == null)
            return 1; // caso base 2: es una hoja
        return hojasDe(n.izq) + hojasDe(n.der);
    }
}