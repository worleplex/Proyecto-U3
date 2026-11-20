package laberinto.vista;

import laberinto.modelo.Arista;
import laberinto.modelo.Grafo;
import laberinto.modelo.Laberinto;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PanelLaberinto extends JPanel {
    // colores para que se vea asi bien chido en el salon, colores claros bueno el fondo claro y se entienden
    // el caminito
    private static final Color COLOR_PARED = Color.BLACK;
    private static final Color COLOR_CELDA_LIBRE = Color.WHITE;
    // azul clarito para las entradas y rosita para las salidas
    private static final Color COLOR_ENTRADA = new Color(186, 218, 250);
    private static final Color COLOR_SALIDA = new Color(255, 205, 210);
    // la ruta final va en azul marino
    private static final Color COLOR_CELDA_RUTA = new Color(150, 192, 242);
    private static final Color COLOR_LINEA_RUTA = new Color(20, 90, 200);
    private static final Color COLOR_VISITADO = new Color(255, 246, 214);
    private static final Color COLOR_NODO_ACTUAL = new Color(255, 200, 100);
    private static final Color COLOR_BORDE_NODO_ACTUAL = new Color(225, 125, 0);
    private static final Color COLOR_VECINO_CONSULTADO = new Color(224, 212, 250);
    private static final Color COLOR_REJILLA = new Color(215, 220, 228);
    private static final Color COLOR_TEXTO = new Color(70, 70, 70);
    private static final Color COLOR_BORDE_PESO = new Color(190, 195, 205);
    // los pesos negativos salen en rojo para que se noten
    private static final Color COLOR_PESO_NEGATIVO = new Color(192, 57, 43);
    // la letra que se usa para las letras e y s, los numeros de orden y los presos
    private static final Font FUENTE = new Font(Font.SANS_SERIF, Font.BOLD, 12);

    // lo que se esta dibujando ahora la ventana se los va cambiando segun el algoritmo que se ejecute
    private Laberinto laberinto;
    // es la ruta final son los nodos en el orden en que se recorren
    private List<Integer> ruta = List.of();
    // los nodos que ya visito bfs
    private List<Integer> nodosVisitados = List.of();
    // los nodos en el orden que dio kahn, la posicion en la lista es el numero que se escribe en la celda
    private List<Integer> ordenTopologico = List.of();
    // el nodo que se esta revisando en este paso -1 quiere decir que no hay ninguno
    private int nodoActual = -1;
    // los vecinos del nodo actual son los que se pintan de morado claro
    private List<Integer> vecinosConsultados = List.of();
    // el tamaño de cada celda se recalcula cada vez que se dibuja porque depende de que tan grande
    // esta la ventana y se guarda aqui para usarlo en los otros metodos
    private double anchoCelda;
    private double altoCelda;

    // fondo blanco y un borde fino alrededor
    public PanelLaberinto() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(200, 210, 225), 1));
    }

    // cuando llega un laberinto nuevo se borra todo lo que se tenia dibujado del anterior
    public void setLaberinto(Laberinto laberinto) {
        this.laberinto = laberinto;
        limpiarResultados();
    }

    // borra la ruta, los visitados y el orden
    public void limpiarResultados() {
        ruta = List.of();
        nodosVisitados = List.of();
        ordenTopologico = List.of();
        limpiarMarcas();
    }

    // solo quita la celda actual y sus vecinos, la ruta y lo visitado se quedan
    // el repaint le dice al panel que se vuelva a dibujar
    public void limpiarMarcas() {
        nodoActual = -1;
        vecinosConsultados = List.of();
        repaint();
    }

    // estos 4 metodos solo guardan lo que manda la ventana y piden que se repinte para que se vea el cambio
    public void setRuta(List<Integer> ruta) {
        this.ruta = ruta;
        repaint();
    }

    public void setVisitados(List<Integer> nodosVisitados) {
        this.nodosVisitados = nodosVisitados;
        repaint();
    }

    public void setOrdenTopologico(List<Integer> ordenTopologico) {
        this.ordenTopologico = ordenTopologico;
        repaint();
    }

    public void setActual(int nodoActual, List<Integer> vecinosConsultados) {
        this.nodoActual = nodoActual;
        this.vecinosConsultados = vecinosConsultados;
        repaint();
    }

    @Override
    // aqui se calcula el tamano de las celdas y se dibuja todo en orden primero las celdas, luego la ruta encima y al final los pesos
    protected void paintComponent(Graphics graficos) {
        super.paintComponent(graficos);
        if (laberinto == null) {
            return;
        }

        // se descuenta el borde del panel y el espacio que queda se divide entre las columnas y las filas
        // asi el laberinto se adapta si cambia el tamano de la ventana
        Insets margen = getInsets();
        anchoCelda = (double) (getWidth() - margen.left - margen.right) / laberinto.columnas();
        altoCelda = (double) (getHeight() - margen.top - margen.bottom) / laberinto.filas();

        // se hace una copia de los graficos con antialiasing para que las lineas y las letras se vean suaves
        // y se mueve el origen para dibujar dentro del borde
        Graphics2D g2d = (Graphics2D) graficos.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.translate(margen.left, margen.top);
        g2d.setFont(FUENTE);
        // el orden importa lo que se dibuja al ultimo queda encima de lo anterior
        dibujarCeldas(g2d);
        dibujarRuta(g2d);
        dibujarPesos(g2d);
        // se libera la copia de graficos que creamos
        g2d.dispose();
    }

    // pinta cada celda segun lo que sea: si es pared va negra y si no, se escoge un color por prioridad
    private void dibujarCeldas(Graphics2D g2d) {
        // se pasan a set para preguntar rapido si una celda esta en la lista
        Set<Integer> nodosEnRuta = new HashSet<>(ruta);
        Set<Integer> nodosEnVisitados = new HashSet<>(nodosVisitados);
        Set<Integer> nodosEnVecinos = new HashSet<>(vecinosConsultados);

        for (int fila = 0; fila < laberinto.filas(); fila++) {
            for (int columna = 0; columna < laberinto.columnas(); columna++) {
                // la esquina de la celda y su tamaño, el ancho se saca restando con la siguiente celda
                // para que no queden huequitos por los redondeos
                int x = (int) (columna * anchoCelda);
                int y = (int) (fila * altoCelda);
                int ancho = (int) ((columna + 1) * anchoCelda) - x;
                int alto = (int) ((fila + 1) * altoCelda) - y;

                // las paredes solo se pintan de negro y se pasa a la siguiente celda
                if (!laberinto.esLibre(fila, columna)) {
                    g2d.setColor(COLOR_PARED);
                    g2d.fillRect(x, y, ancho, alto);
                    continue;
                }

                int idNodo = laberinto.id(fila, columna);
                // el orden de estos if es la prioridad del color actual, vecino, entrada, salida, ruta, visitado
                // y por ultimo la celda normal en blanco
                if (idNodo == nodoActual) {
                    g2d.setColor(COLOR_NODO_ACTUAL);
                } else if (nodosEnVecinos.contains(idNodo)) {
                    g2d.setColor(COLOR_VECINO_CONSULTADO);
                } else if (laberinto.esEntrada(fila, columna)) {
                    g2d.setColor(COLOR_ENTRADA);
                } else if (laberinto.esSalida(fila, columna)) {
                    g2d.setColor(COLOR_SALIDA);
                } else if (nodosEnRuta.contains(idNodo)) {
                    g2d.setColor(COLOR_CELDA_RUTA);
                } else if (nodosEnVisitados.contains(idNodo)) {
                    g2d.setColor(COLOR_VISITADO);
                } else {
                    g2d.setColor(COLOR_CELDA_LIBRE);
                }
                g2d.fillRect(x, y, ancho, alto);
                g2d.setColor(COLOR_REJILLA);
                g2d.drawRect(x, y, ancho, alto);

                if (idNodo == nodoActual) {
                    g2d.setColor(COLOR_BORDE_NODO_ACTUAL);
                    g2d.setStroke(new BasicStroke(2.5f));
                    g2d.drawRect(x + 1, y + 1, ancho - 2, alto - 2);
                    g2d.setStroke(new BasicStroke(1f));
                }

                // si la celda esta muy chiquita no caben las letras, asi que no se escribe nada
                if (ancho < 22 || alto < 18) {
                    continue;
                }

                g2d.setColor(COLOR_TEXTO);
                if (laberinto.esEntrada(fila, columna)) {
                    g2d.drawString("E", x + 4, y + 14);
                } else if (laberinto.esSalida(fila, columna)) {
                    g2d.drawString("S", x + 4, y + 14);
                }
                // el numero de abajo es la posicion de la celda en el orden de kahn si no esta en la lista
                // el indexof da -1 y con el +1 queda en 0, o sea que no se escribe nada
                int posicionEnOrden = ordenTopologico.indexOf(idNodo) + 1;
                if (posicionEnOrden > 0) {
                    g2d.drawString(String.valueOf(posicionEnOrden), x + 4, y + alto - 4);
                }
            }
        }
    }

    // el centro de la celda de un nodo en pixeles sirve para que la linea de la ruta
    // vaya de centro a centro
    private int centroX(int nodo) {
        return (int) ((laberinto.col(nodo) + 0.5) * anchoCelda);
    }

    private int centroY(int nodo) {
        return (int) ((laberinto.fila(nodo) + 0.5) * altoCelda);
    }

    // dibuja una linea azul gruesa uniendo los centros de las celdas de la ruta de dos en dos
    private void dibujarRuta(Graphics2D g2d) {
        // el grosor se hace mas chico si las celdas son chicas con un maximo de 8
        float grosor = (float) Math.min(8, Math.min(anchoCelda, altoCelda) / 6);
        g2d.setColor(COLOR_LINEA_RUTA);
        g2d.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int indice = 0; indice < ruta.size() - 1; indice++) {
            int origen = ruta.get(indice);
            int destino = ruta.get(indice + 1);
            g2d.drawLine(centroX(origen), centroY(origen), centroX(destino), centroY(destino));
        }
        g2d.setStroke(new BasicStroke(1f));
    }

    // escribe el costo de cada pasillo entre dos celdas libres justo en la orilla que las separa
    private void dibujarPesos(Graphics2D g2d) {
        FontMetrics metricas = g2d.getFontMetrics();
        // si la celda es muy chica y el texto no cabe mejor no se dibuja ningun peso para que no se encimen
        boolean caben = metricas.stringWidth("-> -100") + 8 <= anchoCelda * 0.9
                && metricas.getHeight() + 4 <= altoCelda * 0.6;
        if (!caben) {
            return;
        }

        for (int fila = 0; fila < laberinto.filas(); fila++) {
            for (int columna = 0; columna < laberinto.columnas(); columna++) {
                if (!laberinto.esLibre(fila, columna)) {
                    continue;
                }
                int idCelda = laberinto.id(fila, columna);

                // cada celda se encarga del pasillo de su derecha y del de abajo,
                // asi ningun pasillo se dibuja dos veces
                if (columna + 1 < laberinto.columnas() && laberinto.esLibre(fila, columna + 1)) {
                    dibujarPeso(g2d, metricas, idCelda, laberinto.id(fila, columna + 1),
                            // no recuerdo como poner flechas habia trucos si recuerdo asi que voy a ser creativo
                            // para indicar las direcciones
                            (int) ((columna + 1) * anchoCelda), (int) ((fila + 0.5) * altoCelda), "->", "<-");
                }
                if (fila + 1 < laberinto.filas() && laberinto.esLibre(fila + 1, columna)) {
                    dibujarPeso(g2d, metricas, idCelda, laberinto.id(fila + 1, columna),
                            (int) ((columna + 0.5) * anchoCelda), (int) ((fila + 1) * altoCelda), "^", "v");
                }
            }
        }
    }

    // dibuja una cajita blanca con el peso en el punto x,y
    // en grafos dirigidos le pone flecha para saber hacia donde va el pasillo
    private void dibujarPeso(Graphics2D g2d, FontMetrics metricas, int nodoA, int nodoB, int x, int y,
                             String flechaDeAHaciaB, String flechaDeBHaciaA) {
        Grafo grafo = laberinto.grafo();
        String flecha = flechaDeAHaciaB;
        // primero se busca el pasillo de a hacia b y si no existe se busca el de b hacia a
        // con la flecha que le toca. si no hay ninguno no se dibuja nada
        Arista arista = buscarArista(grafo, nodoA, nodoB);
        if (arista == null) {
            arista = buscarArista(grafo, nodoB, nodoA);
            flecha = flechaDeBHaciaA;
        }
        if (arista == null) {
            return;
        }

        String texto = String.valueOf(arista.getPeso());
        if (grafo.esDirigido()) {
            texto = flecha + " " + texto;
        }
        // la cajita se ajusta al tamano del texto
        int ancho = metricas.stringWidth(texto) + 8;
        int alto = metricas.getHeight();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(x - ancho / 2, y - alto / 2, ancho, alto);
        g2d.setColor(COLOR_BORDE_PESO);
        g2d.drawRect(x - ancho / 2, y - alto / 2, ancho, alto);
        // los negativos van en rojo
        g2d.setColor(arista.getPeso() < 0 ? COLOR_PESO_NEGATIVO : COLOR_TEXTO);
        g2d.drawString(texto, x - ancho / 2 + 4, y - alto / 2 + metricas.getAscent());
    }

    // recorre las aristas que salen de un nodo y regresa la que llega al destino, o null si no hay
    private Arista buscarArista(Grafo grafo, int nodoOrigen, int nodoDestino) {
        for (Arista arista : grafo.vecinos(nodoOrigen)) {
            if (arista.getDestino() == nodoDestino) {
                return arista;
            }
        }
        return null;
    }
}