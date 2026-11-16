package laberinto.modelo;

import java.util.*;

public class Laberinto {
    private final int filas;
    private final int columnas;
    private final boolean[][] celdaLibre;
    private final Random random;
    private Grafo grafo;
    private Modo modo;

    public Laberinto(int filas, int columnas, int porcentajeParedes, Random random) {
        if (filas < 1 || columnas < 2) {
            throw new IllegalArgumentException("se requieren al menos 1 fila y 2 columnas");
        }
        this.filas = filas;
        this.columnas = columnas;
        this.random = random;
        celdaLibre = new boolean[filas][columnas];
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                celdaLibre[fila][columna] = random.nextInt(100) >= porcentajeParedes;
            }
        }
        asegurarCeldaLibreEnColumna(0);
        asegurarCeldaLibreEnColumna(columnas - 1);
    }

    private void asegurarCeldaLibreEnColumna(int columna) {
        for (int fila = 0; fila < filas; fila++) {
            if (celdaLibre[fila][columna]) {
                return;
            }
        }
        celdaLibre[random.nextInt(filas)][columna] = true;
    }

    public void construirGrafo(Modo modoGrafo) {
        this.modo = modoGrafo;
        boolean esDirigido = modoGrafo != Modo.NO_DIRIGIDO;
        grafo = new Grafo(filas * columnas, esDirigido);

        int[] direccionVertical = new int[columnas];
        for (int columna = 0; columna < columnas; columna++) {
            direccionVertical[columna] = random.nextBoolean() ? 1 : -1;
        }

        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                if (!celdaLibre[fila][columna]) {
                    continue;
                }
                int nodoActual = id(fila, columna);

                boolean hayCeldaLibreADerecha = columna + 1 < columnas && celdaLibre[fila][columna + 1];
                if (hayCeldaLibreADerecha) {
                    int nodoDerecha = id(fila, columna + 1);
                    if (esDirigido) {
                        grafo.agregarAristas(nodoActual, nodoDerecha, generarPesoAleatorio());
                    } else {
                        grafo.agregarAristaNoDirigida(nodoActual, nodoDerecha, generarPesoAleatorio());
                    }
                }

                boolean hayCeldaLibreAbajo = fila + 1 < filas && celdaLibre[fila + 1][columna];
                if (hayCeldaLibreAbajo) {
                    int nodoAbajo = id(fila + 1, columna);
                    int peso = generarPesoAleatorio();
                    if (!esDirigido) {
                        grafo.agregarAristaNoDirigida(nodoActual, nodoAbajo, peso);
                    } else if (direccionVertical[columna] == 1) {
                        grafo.agregarAristas(nodoActual, nodoAbajo, peso);
                    } else {
                        grafo.agregarAristas(nodoAbajo, nodoActual, peso);
                    }
                }
            }
        }
    }

    private int generarPesoAleatorio() {
        if (modo == Modo.DAG_NEGATIVO) {
            int peso;
            do {
                peso = random.nextInt(201) - 100;
            } while (peso == 0);
            return peso;
        }
        return random.nextInt(100) + 1;
    }

    public int id(int fila, int columna) {
        return fila * columnas + columna;
    }

    public int fila(int idNodo) {
        return idNodo / columnas;
    }

    public int col(int idNodo) {
        return idNodo % columnas;
    }

    public int filas() {
        return filas;
    }

    public int columnas() {
        return columnas;
    }

    public boolean esLibre(int fila, int columna) {
        return celdaLibre[fila][columna];
    }

    public boolean esLibre(int idNodo) {
        return celdaLibre[fila(idNodo)][col(idNodo)];
    }

    public boolean esEntrada(int fila, int columna) {
        return columna == 0 && celdaLibre[fila][columna];
    }

    public boolean esSalida(int fila, int columna) {
        return columna == columnas - 1 && celdaLibre[fila][columna];
    }

    public Grafo grafo() {
        return grafo;
    }

    public Modo modo() {
        return modo;
    }

    public String etiqueta(int idNodo) {
        return "(" + (fila(idNodo) + 1) + "," + (col(idNodo) + 1) + ")";
    }

    public String lista(Collection<Integer> idsNodos) {
        StringJoiner union = new StringJoiner(", ", "[", "]");
        for (int idNodo : idsNodos) {
            union.add(etiqueta(idNodo));
        }
        return union.toString();
    }

    public List<Integer> entradas() {
        List<Integer> nodosEntrada = new ArrayList<>();
        for (int fila = 0; fila < filas; fila++) {
            if (esEntrada(fila, 0)) {
                nodosEntrada.add(id(fila, 0));
            }
        }
        return nodosEntrada;
    }

    public List<Integer> salidas() {
        List<Integer> nodosSalida = new ArrayList<>();
        for (int fila = 0; fila < filas; fila++) {
            if (esSalida(fila, columnas - 1)) {
                nodosSalida.add(id(fila, columnas - 1));
            }
        }
        return nodosSalida;
    }

    public int numActivos() {
        int cantidadCeldasLibres = 0;
        for (boolean[] filaDeCeldas : celdaLibre) {
            for (boolean estaLibre : filaDeCeldas) {
                if (estaLibre) {
                    cantidadCeldasLibres++;
                }
            }
        }
        return cantidadCeldasLibres;
    }

    /**
     * convierte el grafo en texto mostrando para cada celda libre a qué celdas se puede ir desde ella y
     * cuanto cuesta cada pasillo es la lista de adyacencia escrita de forma entendible,
     * y es lo que esta en el PDF para mostrar el grafo que corresponde al laberinto
     */
    public String describirGrafo() {
        StringBuilder texto = new StringBuilder("Lista de adyacencia (" + modo + "):\n");
        for (int nodo = 0; nodo < grafo.numNodos(); nodo++) {
            if (!esLibre(nodo)) {
                continue;
            }
            StringJoiner destinos = new StringJoiner(", ");
            for (Arista arista : grafo.vecinos(nodo)) {
                destinos.add(etiqueta(arista.getDestino()) + "[" + arista.getPeso() + "]");
            }
            texto.append(String.format("  %-8s -> %s%n", etiqueta(nodo),
                    destinos.length() == 0 ? "(sin aristas)" : destinos));
        }
        return texto.toString();
    }
}
