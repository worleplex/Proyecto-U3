package laberinto.algoritmos;

import laberinto.modelo.Arista;
import laberinto.modelo.Grafo;
import laberinto.modelo.Laberinto;

import java.util.*;

/**
 * Recorrido en anchura (BFS) sobre el grafo del laberinto
 * Se usa para saber si alguna entrada puede llegar a alguna salida
 * No importan los pesos
 */
public class BFS {

    /**
     * Esta clase guarda si hay camino para salir del laberinto, la entrada desde donde se comenzo
     * la salida a donde se llego, la ruta de nodos desde la entrada hasta la salida y el camino
     * que se siguio para ir visitando los nodos
     * Se usa para el resultado 
     */
    public static class Resultado {
        private final boolean hayCamino;
        private final int entrada;
        private final int salida;
        private final List<Integer> ruta;
        private final List<Integer> ordenDeVisita;

        Resultado(boolean hayCamino, int entrada, int salida, List<Integer> ruta, List<Integer> ordenDeVisita) {
            this.hayCamino = hayCamino;
            this.entrada = entrada;
            this.salida = salida;
            this.ruta = ruta;
            this.ordenDeVisita = ordenDeVisita;
        }
        public boolean hayCamino() {
            return hayCamino;
        }
        
        public int getEntrada() {
            return entrada;
        }

        public int getSalida() {
            return salida;
        }

        public List<Integer> getRuta() {
            return ruta;
        }

        public List<Integer> getOrdenDeVisita() {
            return ordenDeVisita;
        }
        
        //si no hay camino simplemente te regresa el texto diciendolo
        @Override
        public String toString() {
            return hayCamino
                    ? "BFS: hay camino de " + entrada + " a " + salida + " (" + (ruta.size() - 1) + " pasillos)"
                    : "BFS: no hay camino";
        }
    }

    /**
     * Revisa todas las entradas y salidas, encuento encuentre una parara
     */
    public Resultado existeCamino(Laberinto laberinto) {
        return buscar(laberinto, laberinto.entradas());
    }

    /**
     * revisa si una entrada llega a alguna salida
     */
    public Resultado existeCamino(Laberinto laberinto, int entrada) {
        //chequeamos que la entrada de verdad sea una entrada valida
        if (!laberinto.entradas().contains(entrada)) {
            throw new IllegalArgumentException("el nodo " + entrada + " no es una entrada del laberinto");
        }
        //se le manda los parametros, pero se crea una lista con la unica entrada 
        //para cumplir con las entradas del metodo de busqueda
        return buscar(laberinto, List.of(entrada));
    }

    private Resultado buscar(Laberinto laberinto, List<Integer> origenes) {
        Grafo grafo = laberinto.grafo();
        if (grafo == null) {
            throw new IllegalStateException("primero hay que llamar a construirGrafo");
        }
        
        //agarramos todas las salidas del laberinto
        Set<Integer> salidas = new HashSet<>(laberinto.salidas());
        //una lista que guardara el nodo que se usa para llegar al nodo (o sea el nodo predecesor)
        int[] predecesor = new int[grafo.numNodos()];
        //una lista que guardara si el nodo esta visitado o no
        boolean[] visitado = new boolean[grafo.numNodos()];
        //llenamos toda la lista de predecesores con -1 para poder iniciar
        Arrays.fill(predecesor, -1);
        
        //una lista que guardara el orden en el que se visitan los nodos
        List<Integer> ordenDeVisita = new ArrayList<>();
        //la cola que se usara para poder ir manejando los nodos
        //los que esten en la cola son los que vamos a ir manejando
        Deque<Integer> cola = new ArrayDeque<>();

        //los origenes recordemos que son los que tienen 0 entradas o sea se puede comenzar por ahi
        //por ende se marcan como visitado y se van metiendo a la cola para trabajar con ellos
        for (int origen : origenes) {
            visitado[origen] = true;
            cola.add(origen);
        }
        
        //el ciclo que no para hasta que ya no hay nodos en la cola
        while (!cola.isEmpty()) {
            //se saca el primer nodo de la cola y se guarda en una variable
            int actual = cola.poll();
            //el nodo sacado de la cola se pone en el orden de visita
            ordenDeVisita.add(actual);
            
            //se pregunta si el actual es salida, porque si lo fuera entonces ya llego al final
            //entonces si ya llego al final termina todo y arma el resultado final
            if (salidas.contains(actual)) {
                return armarResultado(actual, predecesor, ordenDeVisita);
            }
            
            //se iteran las aristas del nodo sacado de la cola
            for (Arista arista : grafo.vecinos(actual)) {
                //se agarran los nodos a los que las aristas apuntaban
                int vecino = arista.getDestino();
                //se pregunta si el vecino es visitado, si no lo es se mete al if
                if (!visitado[vecino]) {
                    //pone el vecino como visitado
                    visitado[vecino] = true;
                    //guarda a la lista de predecesores el nodo actual, con el indice del vecino
                    predecesor[vecino] = actual;
                    //mete el nodo vecino a la cola para seguir trabajando
                    cola.add(vecino);
                }
            }
        }
        //si itera toda el laberinto y no encuentra una salida arma un resultado, donde indica que no hay camino
        //lo unico que si se puede enviar bien es el orden de visita
        return new Resultado(false, -1, -1, List.of(), ordenDeVisita);
    }
    
    private Resultado armarResultado(int salida, int[] predecesor, List<Integer> ordenDeVisita) {
        //se crea una lista para guardar la ruta
        LinkedList<Integer> ruta = new LinkedList<>();
        //se iteran los nodos de salida de forma inversa (usando la lsita dle predecesor)
        for (int nodo = salida; nodo != -1; nodo = predecesor[nodo]) {
            //se van guardando los nodos predecesores al inicio de la ruta para ir creandola
            //finaliza hasta que ya no hay mas predecesores
            ruta.addFirst(nodo);
        }
        //crea y regresa el resultado ya con todo y ruta
        return new Resultado(true, ruta.getFirst(), salida, ruta, ordenDeVisita);
    }
}
