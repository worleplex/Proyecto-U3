package laberinto.algoritmos;

import laberinto.modelo.Arista;
import laberinto.modelo.Grafo;
import laberinto.modelo.Laberinto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BellmanFord {
    // se usa como infinito cuando un nodo no ha sido alcanzado
    private static final long INFINITO = Long.MAX_VALUE;

    /**
     * Calcula la ruta de menor costo desde la entrada hasta la mejor salida
     * Si la entrada no tiene salida o hay ciclo negativo devuelve un Resultado no alcanzable
     */
    public static Resultado resolverPorBellmanFord(Laberinto laberinto, int entrada) {
        Grafo grafo = laberinto.grafo();
        if (grafo == null) {
            throw new IllegalStateException("primero hay que construir el grafo");
        }
        if (!laberinto.entradas().contains(entrada)) {
            throw new IllegalArgumentException("el nodo " + entrada + " no es una entrada");
        }

        int n = grafo.numNodos();
        long[] distancia = new long[n];
        int[] predecesor = new int[n];
        java.util.Arrays.fill(distancia, INFINITO);
        java.util.Arrays.fill(predecesor, -1);
        distancia[entrada] = 0;

        List<String> pasos = new ArrayList<>();
        pasos.add("Inicio: dist" + laberinto.etiqueta(entrada) + " = 0, los demas = inf");

        List<Arista> aristas = grafo.todasLasAristas();

        // se relajan todas las aristas hasta n-1 veces, si una vuelta no cambia nada, ya termino
        for (int vuelta = 1; vuelta < n; vuelta++) {
            boolean huboCambio = false;
            pasos.add("Iteración " + vuelta + ":");
            for (Arista a : aristas) {
                if (distancia[a.getOrigen()] == INFINITO) {
                    continue; // todavia no se llega a este nodo, no se puede relajar
                }
                long nuevo = distancia[a.getOrigen()] + a.getPeso();
                if (nuevo < distancia[a.getDestino()]) {
                    String antes = distancia[a.getDestino()] == INFINITO
                            ? "inf" : String.valueOf(distancia[a.getDestino()]);
                    distancia[a.getDestino()] = nuevo;
                    predecesor[a.getDestino()] = a.getOrigen();
                    huboCambio = true;
                    pasos.add("  relaja " + laberinto.etiqueta(a.getOrigen()) + " -> "
                            + laberinto.etiqueta(a.getDestino()) + " [" + a.getPeso() + "]: dist "
                            + antes + " -> " + nuevo);
                }
            }
            if (!huboCambio) {
                pasos.add("  sin cambios, se detiene antes de tiempo");
                break;
            }
        }

        // una vuelta mas si todavia se puede mejorar, hay un ciclo negativo alcanzable
        for (Arista a : aristas) {
            if (distancia[a.getOrigen()] != INFINITO
                    && distancia[a.getOrigen()] + a.getPeso() < distancia[a.getDestino()]) {
                pasos.add("Ciclo negativo detectado en " + a + ", no existe ruta minima valida");
                return new Resultado(false, true, entrada, -1, List.of(), 0, pasos);
            }
        }

        // se comparan las salidas alcanzables y se elige la de menor distancia
        int mejorSalida = -1;
        for (int salida : laberinto.salidas()) {
            if (distancia[salida] == INFINITO) {
                pasos.add("Salida " + laberinto.etiqueta(salida) + ": no alcanzable");
                continue;
            }
            pasos.add("Salida " + laberinto.etiqueta(salida) + ": costo " + distancia[salida]);
            if (mejorSalida == -1 || distancia[salida] < distancia[mejorSalida]) {
                mejorSalida = salida;
            }
        }

        if (mejorSalida == -1) {
            pasos.add("La entrada " + laberinto.etiqueta(entrada) + " no llega a ninguna salida");
            return new Resultado(false, false, entrada, -1, List.of(), 0, pasos);
        }

        // se reconstruye la ruta siguiendo los predecesores de la salida hacia la entrada
        List<Integer> ruta = new ArrayList<>();
        for (int nodo = mejorSalida; nodo != -1; nodo = predecesor[nodo]) {
            ruta.add(nodo);
        }
        Collections.reverse(ruta);

        pasos.add("Mejor salida: " + laberinto.etiqueta(mejorSalida) + " con costo "
                + distancia[mejorSalida] + ", ruta " + laberinto.lista(ruta));
        return new Resultado(true, false, entrada, mejorSalida, ruta, distancia[mejorSalida], pasos);
    }
}
