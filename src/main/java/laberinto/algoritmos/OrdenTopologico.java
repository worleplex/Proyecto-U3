package laberinto.algoritmos;

import laberinto.modelo.Arista;
import laberinto.modelo.Grafo;
import laberinto.modelo.Laberinto;

import java.util.*;

/**
 * Orden topologico (Kahn).
 * Solo existe si el grafo es un DAG (sin ciclos)
 */
public class OrdenTopologico {

    /**
     * Esta clase se encarga de guardar el orden de los nodos y si es DAG o no
     * Se usa para el resultado que regresara la implementacion de kahn
     */
    public static class Resultado {
        private final boolean esDAG;
        private final List<Integer> orden;
        private final int nodosEnCiclo;

        Resultado(boolean esDAG, List<Integer> orden, int nodosEnCiclo) {
            this.esDAG = esDAG;
            this.orden = orden;
            this.nodosEnCiclo = nodosEnCiclo;
        }

        //true si se pudieron ordenar todos los nodos, o sea no hay ciclos
        public boolean esDAG() {
            return esDAG;
        }
        
        //trae el orden de los nodos, si hay un ciclo trae el orden hasta donde encontro un ciclo
        public List<Integer> getOrden() {
            return orden;
        }

        //trae los nodos que no se pudieron ordenar por estar en ciclo
        public int getNodosEnCiclo() {
            return nodosEnCiclo;
        }
        
        //segun si es DAG o no escribe uno u otro comentario
        //si es DAG escribe que se hizo el orden de tantos nodos
        //si no es DAG escribe los nodos que no se pudieron ordenar por estar en ciclo
        @Override
        public String toString() {
            return esDAG
                    ? "Orden topologico de " + orden.size() + " nodos"
                    : "El grafo tiene ciclos (" + nodosEnCiclo + " nodos sin ordenar)";
        }
    }

    /**
     * Implementacion del Kahn, se calcula las entradas de cada nodo, y se empieza con el que tiene 0
     * y cada que se saca un nodo tambien salen las aristas de salida del nodo, haciendo que otros vecinos
     * lleguen a 0 entregas entra a la cola para hacer lo mismo, si sobran nodos siginifica que no hay
     * ningun nodo para continuar (o sea no hay ninguno con 0 entradas) por lo que el grafo tiene un ciclo
     */
    public Resultado kahn(Laberinto laberinto) {
        Grafo grafo = laberinto.grafo();
        if (grafo == null) {
            throw new IllegalStateException("primero hay que llamar a construirGrafo");
        }
        
        //Se crea la lista de grados de entrada, esta almacena los grados de entrada de cada nodo
        int[] gradoEntrada = new int[grafo.numNodos()];
        for (Arista arista : grafo.todasLasAristas()) {
            gradoEntrada[arista.getDestino()]++;
        }

        //se crea la cola que se usara par ir metiendo los nodos sin entrada y ordenarlos
        Deque<Integer> cola = new ArrayDeque<>();
        //contador de nodos, para saber cuantos nodos pudimos meter al orden al final
        int totalNodos = 0;
        //recorre todos los nodos, desde 0 hasta el numero de nodos en el grafo
        for (int nodo = 0; nodo < grafo.numNodos(); nodo++) {
            //pregunta si el nodo es libre que significaria que es un nodo
            //si no es libre es una pared, entonces no la tratamos
            if (!laberinto.esLibre(nodo)) {
                continue;
            }
            totalNodos++;
            //si el nodo no tiene entradas ya lo podemos meter
            if (gradoEntrada[nodo] == 0) {
                cola.add(nodo);
            }
        }
        
        //se crea la lista donde se guardara el orden
        List<Integer> orden = new ArrayList<>();
        //trabaja hasta que no haya cola
        while (!cola.isEmpty()) {
            //sacamos el 1er elemento de la cola y lo almacenamos en una variable
            int actual = cola.poll();
            //guardamos el elemento sacado de la cola
            orden.add(actual);
            //se iteran las aristas del nodo sacado de la cola
            for (Arista arista : grafo.vecinos(actual)) {
                //se agarrarn los nodos apuntados por las aristas
                int vecino = arista.getDestino();
                //se le resta el grado de entrada al nodo
                gradoEntrada[vecino]--;
                //si es 0 lo mete a la cola para continuar con el siguiente ciclo
                if (gradoEntrada[vecino] == 0) {
                    cola.add(vecino);
                }
            }
        }
        
        //resta los nodos ordenados con el total de nodos, para saber cuantos no pudo ordenar
        int sinOrdenar = totalNodos - orden.size();
        //crea el resultado, el primero si es 0 es true si no es false, o sea si falto
        //algun nodo por ordenar no es DAG si no es DAG, el segundo es la lista ordenada
        //el tercero son la cantidad de nodos sin ordenar
        return new Resultado(sinOrdenar == 0, orden, sinOrdenar);
    }
}
