package redsens.modelo;

/** Estación de medición (modelo de datos de la sección 1.1 del enunciado). */
public class Estacion {
    public static final int BATERIA_CRITICA = 20;

    private final String codigo;     // clave única, formato CBA-NNNN (clave de la tabla hash)
    private final int numero;        // parte numérica (clave del índice ordenado)
    private String nombre;
    private String localidad;        // tiene que ser un vértice del grafo
    private String tipo;             // PLUVIOMETRICA | METEOROLOGICA | HIDROMETRICA
    private int bateria;             // 0..100
    private int diasSinVisita;

    public Estacion(int numero, String nombre, String localidad, String tipo, int bateria, int diasSinVisita) {
        this.numero = numero;
        this.codigo = String.format("CBA-%04d", numero);
        this.nombre = nombre;
        this.localidad = localidad;
        this.tipo = tipo;
        this.bateria = bateria;
        this.diasSinVisita = diasSinVisita;
    }

    public String getCodigo() { return codigo; }
    public int getNumero() { return numero; }
    public String getNombre() { return nombre; }
    public String getLocalidad() { return localidad; }
    public String getTipo() { return tipo; }
    public int getBateria() { return bateria; }
    public int getDiasSinVisita() { return diasSinVisita; }
    public void setBateria(int b) { this.bateria = b; }
    public void setDiasSinVisita(int d) { this.diasSinVisita = d; }
    public boolean esCritica() { return bateria < BATERIA_CRITICA; }

    @Override
    public String toString() {
        return String.format("%s | %-28s | %-18s | %-14s | bat=%3d%% | %3d días%s",
                codigo, nombre, localidad, tipo, bateria, diasSinVisita, esCritica() ? " [CRÍTICA]" : "");
    }
}
