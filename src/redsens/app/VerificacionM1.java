package redsens.app;

import java.util.List;
import redsens.modelo.Estacion;
import redsens.registro.*;
import redsens.sim.ConfigGrupo;
import redsens.sim.GeneradorDatos;

/**
 * Pruebas del M1 (sin JUnit). Primero repite los ejemplos de las lecturas (tabla de tamaño 10, claves
 * 89, 18, 49, 58, 9) y después prueba rehash, borrado perezoso y 10.000 estaciones.
 * Ejecutar:  java -cp out redsens.app.VerificacionM1 [grupo]   (por defecto, grupo 4)
 */
public class VerificacionM1 {
    static int fallos = 0;
    static void check(String nombre, boolean ok) { System.out.println((ok ? "OK    " : "FALLO ") + nombre); if (!ok) fallos++; }

    static Estacion est(int numero) { return new Estacion(numero, "t", "Córdoba", "METEOROLOGICA", 50, 0); }
    static String cod(int numero) { return String.format("CBA-%04d", numero); }

    public static void main(String[] args) {
        ConfigGrupo cfg = ConfigGrupo.deGrupo(args.length > 0 ? Integer.parseInt(args[0]) : 4);
        System.out.println(cfg);

        // ---- Primos
        check("siguientePrimo(23)=23, (24)=29, (45)=47", Primos.siguientePrimo(23) == 23 && Primos.siguientePrimo(24) == 29 && Primos.siguientePrimo(45) == 47);
        check("los tamaños iniciales de los 15 grupos son primos", todosPrimos());

        // ---- Ejemplo de la lectura con exploración LINEAL (hash(n)=n, tamaño 10)
        TablaExploracionLineal lin = new TablaExploracionLineal(10, 0.75, FuncionHash.NUMERO);
        for (int n : new int[]{89, 18, 49, 58, 9}) lin.insertar(est(n));
        check("lineal: 89->9, 18->8, 49->0, 58->1, 9->2 (lectura L3)",
            lin.posicionDe(cod(89)) == 9 && lin.posicionDe(cod(18)) == 8 && lin.posicionDe(cod(49)) == 0
            && lin.posicionDe(cod(58)) == 1 && lin.posicionDe(cod(9)) == 2);
        check("lineal: colisiones = 1 (49) + 3 (58) + 3 (9) = 7", lin.estadisticas().colisiones == 7);
        check("lineal: 3 inserciones tuvieron colisión (49, 58 y 9)", lin.estadisticas().insercionesConColision == 3);
        check("lineal: buscar 58 encuentra y examina 8,9,0,1 (4 sondeos)", lin.buscar(cod(58)) != null && lin.estadisticas().sondeosBuscarOk == 4);
        check("lineal: buscar 19 (hash 9) examina 9,0,1,2,3 y falla (5 sondeos)", lin.buscar(cod(19)) == null && lin.estadisticas().sondeosBuscarFail == 5);

        // ---- Ejemplo con exploración CUADRÁTICA
        TablaExploracionCuadratica cua = new TablaExploracionCuadratica(10, 0.5, FuncionHash.NUMERO);
        for (int n : new int[]{89, 18, 49, 58, 9}) cua.insertar(est(n));
        check("cuadrática: 89->9, 18->8, 49->0, 58->2, 9->3 (lectura L3)",
            cua.posicionDe(cod(89)) == 9 && cua.posicionDe(cod(18)) == 8 && cua.posicionDe(cod(49)) == 0
            && cua.posicionDe(cod(58)) == 2 && cua.posicionDe(cod(9)) == 3);
        check("cuadrática: colisiones = 1 + 2 + 2 = 5 (menos que lineal)", cua.estadisticas().colisiones == 5);
        check("cuadrática: 3 inserciones tuvieron colisión", cua.estadisticas().insercionesConColision == 3);

        // ---- Borrado perezoso (49, 58 y 9 pasaron por la celda 9, donde estaba 89)
        check("eliminar 89 devuelve true", lin.eliminar(cod(89)));
        check("tras borrar 89 siguen encontrándose 49, 58 y 9 (la lápida no corta la cadena)",
            lin.buscar(cod(49)) != null && lin.buscar(cod(58)) != null && lin.buscar(cod(9)) != null);
        check("89 ya no se encuentra, size=4, 1 lápida", lin.buscar(cod(89)) == null && lin.size() == 4 && lin.borrados() == 1);
        check("eliminar algo inexistente devuelve false", !lin.eliminar(cod(77)));
        lin.insertar(est(19));   // hash 9: la celda 9 es lápida y 19 no existe => reutiliza la lápida
        check("insertar reutiliza la lápida (19 queda en la posición 9, sin lápidas)", lin.posicionDe(cod(19)) == 9 && lin.borrados() == 0);

        // ---- Clave repetida sobrescribe
        TablaExploracionLineal rep = new TablaExploracionLineal(11);
        rep.insertar(new Estacion(5, "viejo", "Córdoba", "PLUVIOMETRICA", 10, 0));
        rep.insertar(new Estacion(5, "nuevo", "Córdoba", "PLUVIOMETRICA", 10, 0));
        check("clave repetida sobrescribe (size=1, nombre=nuevo)", rep.size() == 1 && rep.buscar(cod(5)).getNombre().equals("nuevo"));

        // ---- Rehash: tamaño 11, umbral 0,75 => al insertar el 9.º hay 9/11 > 0,75 => nueva capacidad 23
        TablaExploracionLineal rh = new TablaExploracionLineal(11);
        for (int i = 1; i <= 8; i++) rh.insertar(est(i));
        check("8 elementos en 11: todavía sin rehash (8/11=0,73)", rh.capacidad() == 11 && rh.estadisticas().rehashings == 0);
        rh.insertar(est(9));
        check("9.º elemento: rehash a 23 (primo siguiente a 2·11)", rh.capacidad() == 23 && rh.estadisticas().rehashings == 1);
        boolean todos = true;
        for (int i = 1; i <= 9; i++) if (rh.buscar(cod(i)) == null) todos = false;
        check("tras el rehash se encuentran los 9 elementos", todos);
        TablaExploracionCuadratica rq = new TablaExploracionCuadratica(11);
        for (int i = 1; i <= 6; i++) rq.insertar(est(i));   // 6/11 = 0,54 > 0,5
        check("cuadrática (umbral 0,5): rehash al 6.º elemento, capacidad 23", rq.capacidad() == 23 && rq.estadisticas().rehashings == 1);

        // ---- Las lápidas cuentan para el factor de carga y el rehash las saca
        TablaExploracionLineal lap = new TablaExploracionLineal(11, 0.75, FuncionHash.NUMERO);
        for (int i = 1; i <= 7; i++) lap.insertar(est(i));
        for (int i = 1; i <= 3; i++) lap.eliminar(cod(i));
        check("lápidas cuentan en el factor de carga (7 usadas/11)", Math.abs(lap.factorCarga() - 7.0 / 11) < 1e-9 && lap.size() == 4);
        lap.insertar(est(20)); lap.insertar(est(21));       // 9 usadas > 0,75 => el rehash descarta las 3 lápidas
        check("el rehash descarta las lápidas (borrados=0, size=6)", lap.borrados() == 0 && lap.size() == 6 && lap.capacidad() == 23);

        // ---- 10.000 estaciones: todas las variantes, con borrado de la mitad
        List<Estacion> datos = new GeneradorDatos(cfg.semilla).generarEstaciones(10_000);
        TablaHash[] tablas = {
            new TablaExploracionLineal(cfg.tamTablaInicial), new TablaExploracionCuadratica(cfg.tamTablaInicial),
            new TablaEncadenada(cfg.tamTablaInicial),
            new TablaExploracionLineal(cfg.tamTablaInicial, 0.75, FuncionHash.HORNER) };
        for (TablaHash t : tablas) {
            for (Estacion e : datos) t.insertar(e);
            boolean ok = t.size() == 10_000;
            for (Estacion e : datos) if (t.buscar(e.getCodigo()) != e) ok = false;
            check(t.nombre() + ": 10.000 insertadas y todas encontradas (cap. final " + t.capacidad() + ")", ok);
            boolean borr = true;
            for (int i = 0; i < datos.size(); i += 2) if (!t.eliminar(datos.get(i).getCodigo())) borr = false;
            for (int i = 0; i < datos.size(); i++) {
                boolean debeEstar = i % 2 == 1;
                if ((t.buscar(datos.get(i).getCodigo()) != null) != debeEstar) borr = false;
            }
            check(t.nombre() + ": borrar la mitad y verificar presentes/ausentes (size=" + t.size() + ")", borr && t.size() == 5_000);
        }

        System.out.println(fallos == 0 ? "\nTODO OK" : "\n" + fallos + " FALLO(S)");
        System.exit(fallos == 0 ? 0 : 1);
    }

    private static boolean todosPrimos() {
        for (int g = 1; g <= 15; g++) if (!Primos.esPrimo(ConfigGrupo.deGrupo(g).tamTablaInicial)) return false;
        return true;
    }
}
