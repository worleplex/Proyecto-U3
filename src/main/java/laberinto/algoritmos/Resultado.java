package laberinto.algoritmos;

import java.util.List;

public class Resultado {
    private final boolean alcanzable;
    private final boolean cicloNegativo;
    private final int entrada;
    private final int salida;
    private final List<Integer> ruta;
    private final long costo;
    private final List<String> pasos;

    public Resultado(boolean alcanzable, boolean cicloNegativo, int entrada, int salida,
                     List<Integer> ruta, long costo, List<String> pasos) {
        this.alcanzable = alcanzable;
        this.cicloNegativo = cicloNegativo;
        this.entrada = entrada;
        this.salida = salida;
        this.ruta = ruta;
        this.costo = costo;
        this.pasos = pasos;
    }

    /**true si la entrada llega al menos a una salida*/
    public boolean alcanzable() { return alcanzable; }

    /**true si se encontro un ciclo de peso negativo alcanzable desde la entrada*/
    public boolean cicloNegativo() { return cicloNegativo; }

    public int entrada() { return entrada; }

    /**id del nodo de salida elegido, o -1 si no hay */
    public int salida() { return salida; }

    /** ids de los nodos de la ruta, de la entrada a la salida*/
    public List<Integer> ruta() { return ruta; }

    /** costo total de la ruta o la suma de pesos  */
    public long costo() { return costo; }

    /**bitacora paso a paso para la documentacion del proyecto*/
    public List<String> pasos() { return pasos; }
}
