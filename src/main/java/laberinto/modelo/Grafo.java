package laberinto.modelo;

import java.util.ArrayList;
import java.util.List;

public class Grafo {
    private final int numeroNodos;
    private final boolean dirigido;
    // aqui lo que hacemos es una lista dentro de otra
    // la de afuera tiene una posición por nodo (adyacencia.get(3) es el nodo 3)
    // la de adentro guarda las aristas que salen de ese nodo: a que nodo llegan y su peso
    // un pequeño ejemplo adyacencia.get(0) = [ Arista(0 -> 1, peso 5), Arista(0 -> 2, peso 8) ]
    // esto significa que desde el nodo 0 se va al nodo 1 (costo 5) o al nodo 2 (costo 8)
    // un nodo sin salidas tiene su lista de adentro vacia
    private final List<List<Arista>> adyacencia;

    public Grafo(int numeroNodos, boolean dirigido) {
        this.numeroNodos = numeroNodos;
        this.dirigido = dirigido;
        adyacencia = new ArrayList<>(numeroNodos);
        for (int i = 0; i < numeroNodos; i++) {
            // creamos una lista interna vacia por cada nodo
            adyacencia.add(new ArrayList<>());
        }
    }
    // no creo que falte pero u es el nodo origen y v es el nodo destino
    public void agregarAristas(int u, int v, int peso) {
        adyacencia.get(u).add(new Arista(u, v, peso));
    }

    public void agregarAristaNoDirigida(int u, int v, int peso) {
        agregarAristas(u, v, peso);
        agregarAristas(v, u, peso);
    }

    public List<Arista> vecinos(int u) {
        return adyacencia.get(u);
    }

    public int numNodos() {
        return numeroNodos;
    }

    public boolean esDirigido() {
        return dirigido;
    }
    // juntamos las aristas de todos los nodos en una sola lista
    public List<Arista> todasLasAristas() {
        List<Arista> todasLasAristas = new ArrayList<>();
        for (List<Arista> aristasDelNodo : adyacencia) {
            todasLasAristas.addAll(aristasDelNodo);
        }
        return todasLasAristas;
    }
    // este para dijkstra
    public boolean tienePesosNegativos() {
        for (List<Arista> aristasDelNodo : adyacencia) {
            for (Arista arista : aristasDelNodo) {
                if (arista.getPeso() < 0 ){
                    return true;
                }
            }
        }
        return false;
    }
}

