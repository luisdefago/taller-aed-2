package redsens.sim;

/** Parámetros asignados a cada grupo (sección 3 del enunciado). */
public final class ConfigGrupo {
    public final int grupo, semilla, tamTablaInicial, mJosephus;
    public final String baseCuadrilla;

    private ConfigGrupo(int g, int semilla, int tam, String base, int m) {
        this.grupo = g; this.semilla = semilla; this.tamTablaInicial = tam;
        this.baseCuadrilla = base; this.mJosephus = m;
    }

    private static final String[] BASES = {"Río Cuarto", "Villa María", "San Francisco", "Villa Dolores", "Deán Funes"};
    private static final int[] TAM = {11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67};

    /** Parámetros del grupo (1 a 15). */
    public static ConfigGrupo deGrupo(int g) {
        if (g < 1 || g > 15) throw new IllegalArgumentException("Grupo debe estar entre 1 y 15");
        return new ConfigGrupo(g, g * 1000 + 26, TAM[g - 1], BASES[(g - 1) % 5], ((g - 1) % 4) + 1);
    }

    @Override
    public String toString() {
        return "Grupo " + grupo + " | semilla=" + semilla + " | tabla inicial=" + tamTablaInicial
                + " | base=" + baseCuadrilla + " | M Josephus=" + mJosephus;
    }
}
