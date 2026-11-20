package laberinto.modelo;

import java.util.*;

/**
 * Representa el tablero y guarda las conexiones entre sus celdas.
 * Cada pasillo tiene un costo que se usa para comparar rutas.
 */
public class Laberinto {
    private final int filas;
    private final int columnas;
    private final boolean[][] celdaLibre;
    private final Random random;
    private Grafo grafo;
    private Modo modo;

    /**
     * Crea un tablero con celdas libres y paredes elegidas al azar.
     * Deja al menos una entrada en la primera columna y una salida en la última.
     * Después se llama a construirGrafo para preparar los pasillos.
     */
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

    /**
     * Conecta las celdas libres con pasillos para que se puedan buscar rutas.
     * El modo elegido determina el sentido de los pasillos y los costos permitidos.
     */
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
     * Muestra qué celdas están conectadas y cuánto cuesta recorrer cada pasillo.
     * Incluye las celdas libres que no tienen pasillos de salida.
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

    /**
     * Crea un laberinto fijo de 3 filas y 4 columnas, con dos entradas y dos salidas.
     * Desde la entrada (0, 0), llegar a (0, 3) cuesta 5 y llegar a (2, 3) cuesta -2.
     * La segunda opción es la de menor costo. Desde (2, 0) no se llega a ninguna salida.
     * Las filas y columnas del ejemplo se cuentan desde cero.
     */
    public static Laberinto ejemploDePrueba() {
        Laberinto ejemplo = new Laberinto(3, 4, 0, new Random(0));
        ejemplo.celdaLibre[1][0] = false;
        ejemplo.celdaLibre[1][3] = false;
        ejemplo.celdaLibre[2][1] = false;
        ejemplo.modo = Modo.DAG_NEGATIVO;

        Grafo grafoEjemplo = new Grafo(ejemplo.filas * ejemplo.columnas, true);
        grafoEjemplo.agregarAristas(ejemplo.id(0, 0), ejemplo.id(0, 1), 2);
        grafoEjemplo.agregarAristas(ejemplo.id(0, 1), ejemplo.id(0, 2), 1);
        grafoEjemplo.agregarAristas(ejemplo.id(0, 2), ejemplo.id(0, 3), 2);
        grafoEjemplo.agregarAristas(ejemplo.id(0, 1), ejemplo.id(1, 1), 3);
        grafoEjemplo.agregarAristas(ejemplo.id(1, 1), ejemplo.id(1, 2), -10);
        grafoEjemplo.agregarAristas(ejemplo.id(1, 2), ejemplo.id(2, 2), 1);
        grafoEjemplo.agregarAristas(ejemplo.id(2, 2), ejemplo.id(2, 3), 2);
        ejemplo.grafo = grafoEjemplo;
        return ejemplo;
    }
}
