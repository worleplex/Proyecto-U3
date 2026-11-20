package laberinto.vista;

import laberinto.algoritmos.BFS;
import laberinto.algoritmos.BellmanFord;
import laberinto.algoritmos.OrdenTopologico;
import laberinto.algoritmos.Resultado;
import laberinto.modelo.Arista;
import laberinto.modelo.Laberinto;
import laberinto.modelo.Modo;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.IntConsumer;

public class LaVentana extends JFrame {
    // mas o menos 30 de cada 100 celdas salen como pared
    private static final int PORCENTAJEPAREDES = 30;
    // estos numeros son la posicion de cada algoritmo dentro del combo asi en el switch
    // no andamos con numeros sueltos
    private static final int ALGORITMOBFS = 0;
    private static final int ALGORITMOBELLMANFORD = 1;
    private static final int ALGORITMOORDENTOPOLOGICO = 2;

    // los controles de arriba en el cuadro blanco, el spinner es el cuadrito con flechitas para elegir un numero
    // (valor inicial, minimo, maximo y de cuanto en cuanto sube)
    private final JSpinner spinnerFilas = new JSpinner(new SpinnerNumberModel(6, 1, 60, 1));
    private final JSpinner spinnerColumnas = new JSpinner(new SpinnerNumberModel(6, 2, 60, 1));
    private final JComboBox<Modo> comboTipoGrafo = new JComboBox<>(Modo.values());
    // lista desplegable con los 3 algoritmos que hicimos
    private final JComboBox<String> comboAlgoritmo = new JComboBox<>(new String[]{
            "BFS (¿hay camino?)",
            "Bellman-Ford (camino más corto)",
            "Orden topológico (Kahn)"});
    // aqui se elige desde que entrada empieza bellman-ford, se llena al generar el laberinto
    private final JComboBox<String> comboEntrada = new JComboBox<>();
    private final JButton botonGenerar = new JButton("Generar");
    private final JButton botonEjecutar = new JButton("Ejecutar");
    // si esta selecionada el algoritmo se muestra paso por paso si no se muestra el resultado de una vez
    private final JCheckBox casillaAnimar = new JCheckBox("Mostrar todos los pasos", true);
    // lo que tarda cada paso de la animacion en milisegundos
    private final JSpinner spinnerVelocidad = new JSpinner(new SpinnerNumberModel(1000, 100, 5000, 100));
    // el texto que va explicando que esta haciendo el algoritmo en cada paso
    private final JLabel etiquetaEstado = new JLabel(" ");
    // el panel que dibuja el laberinto
    private final PanelLaberinto panelLaberinto = new PanelLaberinto();

    // el laberinto actual, es null hasta que se le da click a generar
    private Laberinto laberinto;
    // los nodos que son entrada, van en el mismo orden que salen en el combo de entrada
    private List<Integer> nodosEntrada = List.of();
    // el reloj que va avanzando la animacion paso por paso
    private Timer temporizador;

    // aqui se arma toda la ventana se crean los paneles, se acomodan los controles
    // y se conectan los botones con lo que deben hacer
    public LaVentana() {
        super("Laberinto con grafos");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // panel de fondo que contiene todo, con un margen de 8 para que nada quede pegado al borde
        JPanel panelRaiz = new JPanel(new BorderLayout(0, 8));
        panelRaiz.setBackground(Color.WHITE);
        panelRaiz.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setContentPane(panelRaiz);

        // la caja de arriba donde van todos los controles, uno debajo del otro por eso el boxlayout vertical
        JPanel panelDatos = new JPanel();
        panelDatos.setLayout(new BoxLayout(panelDatos, BoxLayout.Y_AXIS));
        panelDatos.setBackground(Color.WHITE);
        panelDatos.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 225)), "Hola profe")); // un pequeño trolleo

        // primera fila tamaño del laberinto, tipo de grafo y el boton de generar
        JPanel filaDimensiones = crearFila();
        filaDimensiones.add(new JLabel("Filas:"));
        filaDimensiones.add(spinnerFilas);
        filaDimensiones.add(new JLabel("Columnas:"));
        filaDimensiones.add(spinnerColumnas);
        filaDimensiones.add(new JLabel("Tipo de grafo:"));
        filaDimensiones.add(comboTipoGrafo);
        filaDimensiones.add(botonGenerar);

        // segunda fila el algoritmo, la entrada, el boton de ejecutar y las opciones de la animacion
        JPanel filaAlgoritmo = crearFila();
        filaAlgoritmo.add(new JLabel("Algoritmo:"));
        filaAlgoritmo.add(comboAlgoritmo);
        filaAlgoritmo.add(new JLabel("Entrada:"));
        filaAlgoritmo.add(comboEntrada);
        filaAlgoritmo.add(botonEjecutar);
        // esto es para que el checkbox no se pinte con fondo gris y se vea igual que el resto
        casillaAnimar.setOpaque(false);
        filaAlgoritmo.add(casillaAnimar);
        filaAlgoritmo.add(new JLabel("Velocidad en milisegundos:"));
        filaAlgoritmo.add(spinnerVelocidad);

        // tercera fila solo el mensaje de estado, en negritas y azul oscuro
        JPanel filaEstado = crearFila();
        etiquetaEstado.setForeground(new Color(40, 60, 90));
        etiquetaEstado.setFont(etiquetaEstado.getFont().deriveFont(Font.BOLD));
        filaEstado.add(etiquetaEstado);

        panelDatos.add(filaDimensiones);
        panelDatos.add(filaAlgoritmo);
        panelDatos.add(filaEstado);

        panelRaiz.add(panelDatos, BorderLayout.NORTH);
        panelRaiz.add(panelLaberinto, BorderLayout.CENTER);

        // aqui se conectan los botones con sus metodos
        botonGenerar.addActionListener(evento -> generarLaberinto());
        botonEjecutar.addActionListener(evento -> ejecutarAlgoritmo());
        // el combo de entrada solo sirve con bellman-ford, porque bfs y kahn no necesitan
        // escoger entrada, por eso solo se activa cuando se elige ese algoritmo
        comboAlgoritmo.addActionListener(evento ->
                comboEntrada.setEnabled(comboAlgoritmo.getSelectedIndex() == ALGORITMOBELLMANFORD));
        // al iniciar todavia no hay laberinto, entonces la entrada y el boton de ejecutar empiezan apagados
        comboEntrada.setEnabled(false);
        botonEjecutar.setEnabled(false);

        // tamaño de la ventana, el minimo es para que no se aplasten los controles
        // y al final se centra en la pantalla
        setSize(1100, 760);
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(null);
    }

    // metodo chiquito para no repetir el codigo de cada fila, las crea alineadas a la izquierda y transparentes
    private JPanel crearFila() {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        return fila;
    }

    // esto pasa cuando se le da click a generar se crea un laberinto nuevo con las medidas
    // que se eligieron, se le arma el grafo y se manda a dibujar
    private void generarLaberinto() {
        // primero se frena cualquier animacion que estuviera corriendo
        detenerAnimacion();
        int filas = (Integer) spinnerFilas.getValue();
        int columnas = (Integer) spinnerColumnas.getValue();

        // se crea el laberinto con paredes al azar y luego se le arma el grafo segun el tipo que se eligio
        laberinto = new Laberinto(filas, columnas, PORCENTAJEPAREDES, new Random());
        laberinto.construirGrafo((Modo) comboTipoGrafo.getSelectedItem());
        panelLaberinto.setLaberinto(laberinto);

        // se vuelve a llenar el combo con las entradas del laberinto nuevo, se muestran como fila,columna
        nodosEntrada = laberinto.entradas();
        comboEntrada.removeAllItems();
        for (int nodoEntrada : nodosEntrada) {
            comboEntrada.addItem(laberinto.etiqueta(nodoEntrada));
        }
        // ya hay laberinto entonces ya se puede ejecutar y tambien se borra el mensaje de antes
        botonEjecutar.setEnabled(true);
        etiquetaEstado.setText(" ");
    }

    // se le da click a ejecutar se limpia lo que se dibujo antes y se manda llamar el algoritmo 
    // que este elegido en el combo
    private void ejecutarAlgoritmo() {
        detenerAnimacion();
        panelLaberinto.limpiarResultados();
        switch (comboAlgoritmo.getSelectedIndex()) {
            case ALGORITMOBFS -> ejecutarBfs();
            case ALGORITMOBELLMANFORD -> ejecutarBellmanFord();
            case ALGORITMOORDENTOPOLOGICO -> ejecutarOrdenTopologico();
        }
    }

    // frena el reloj si estaba corriendo y quita la celda actual y sus vecinos del dibujo
    // pero la ruta y lo visitado se quedan
    private void detenerAnimacion() {
        if (temporizador != null) {
            temporizador.stop();
        }
        panelLaberinto.limpiarMarcas();
    }

    // este metodo es el que mueve la animacion de todos los algoritmos recibe cuantos pasos hay,
    // que hacer en cada paso y que hacer al final tons si la casilla no esta marcada
    // o no hay pasos, se salta directo al final
    private void animar(int totalPasos, IntConsumer accionPorPaso, Runnable accionFinal) {
        if (!casillaAnimar.isSelected() || totalPasos == 0) {
            accionFinal.run();
            return;
        }
        // es un arreglo de un solo elemento porque dentro del timer no se puede cambiar una variable normal, asi si deja
        int[] pasoActual = {0};
        // cada vez que pasa el tiempo de la velocidad se hace un paso, cuando ya no quedan pasos
        // se apaga el reloj y se muestra el resultado final
        temporizador = new Timer((Integer) spinnerVelocidad.getValue(), evento -> {
            if (pasoActual[0] < totalPasos) {
                accionPorPaso.accept(pasoActual[0]);
                pasoActual[0]++;
            } else {
                detenerAnimacion();
                accionFinal.run();
            }
        });
        // con delay inicial en 0 el primer paso sale luego luego sin esperar
        temporizador.setInitialDelay(0);
        temporizador.start();
    }

    // saca solo los numeros de nodo de los vecinos de un nodo pa pintarlos y ponerlos en el mensaje
    private List<Integer> obtenerVecinos(int nodo) {
        List<Integer> vecinos = new ArrayList<>();
        for (Arista arista : laberinto.grafo().vecinos(nodo)) {
            vecinos.add(arista.getDestino());
        }
        return vecinos;
    }

    // BFS solo dice si hay camino de alguna entrada a alguna salida, los pesos no cuentan
    // la animacion recorre las celdas en el orden en que las fue visitando
    private void ejecutarBfs() {
        BFS.Resultado resultado = new BFS().existeCamino(laberinto);
        List<Integer> ordenDeVisita = resultado.getOrdenDeVisita();

        animar(ordenDeVisita.size(), paso -> {
            // en cada paso se marca la celda actual, se pintan sus vecinos
            // y de amarillo todo lo que ya se visito hasta ahora
            int nodoActual = ordenDeVisita.get(paso);
            List<Integer> vecinos = obtenerVecinos(nodoActual);
            panelLaberinto.setVisitados(new ArrayList<>(ordenDeVisita.subList(0, paso + 1)));
            panelLaberinto.setActual(nodoActual, vecinos);
            etiquetaEstado.setText("BFS: se saca " + laberinto.etiqueta(nodoActual)
                    + " de la cola y consulta a sus vecinos " + laberinto.lista(vecinos));
        // esto se hace al terminar la animacion: se pintan todos los visitados y si hubo camino se dibuja la ruta
        }, () -> {
            panelLaberinto.setVisitados(ordenDeVisita);
            if (resultado.hayCamino()) {
                panelLaberinto.setRuta(resultado.getRuta());
                etiquetaEstado.setText("BFS: si hay camino de " + laberinto.etiqueta(resultado.getEntrada())
                        + " a " + laberinto.etiqueta(resultado.getSalida())
                        + " (" + (resultado.getRuta().size() - 1) + " pasillos, los pesos no se toman en cuenta)");
            } else {
                etiquetaEstado.setText("BFS: no hay camino hacia ninguna salida");
            }
        });
    }

    // Bellman-Ford busca la ruta de menor costo desde la entrada elegida hasta una salida,
    // y si funciona con pesos negativos
    private void ejecutarBellmanFord() {
        int nodoEntrada = nodosEntrada.get(comboEntrada.getSelectedIndex());
        Resultado resultado = BellmanFord.resolverPorBellmanFord(laberinto, nodoEntrada);
        // hay dos casos donde no hay ruta que mostrar un ciclo negativo (el costo bajaria sin parar)
        // o que esa entrada no llegue a ninguna salida, en ambos solo se avisa y ya
        if (resultado.cicloNegativo()) {
            etiquetaEstado.setText("Bellman-Ford: hay un ciclo negativo, no hay ruta minima valida");
            return;
        }
        if (!resultado.alcanzable()) {
            etiquetaEstado.setText("Bellman-Ford: la entrada " + laberinto.etiqueta(nodoEntrada)
                    + " no llega a ninguna salida");
            return;
        }

        // la animacion va dibujando la ruta nodo por nodo
        List<Integer> ruta = resultado.ruta();
        animar(ruta.size(), paso -> {
            int nodoActual = ruta.get(paso);
            List<Integer> vecinos = obtenerVecinos(nodoActual);
            panelLaberinto.setRuta(new ArrayList<>(ruta.subList(0, paso + 1)));
            panelLaberinto.setActual(nodoActual, vecinos);
            etiquetaEstado.setText("Bellman-Ford: ruta optima, en " + laberinto.etiqueta(nodoActual)
                    + " (vecinos " + laberinto.lista(vecinos) + ")");
        // al final se deja la ruta completa y se muestra el costo total
        }, () -> {
            panelLaberinto.setRuta(ruta);
            etiquetaEstado.setText("Bellman-Ford: ruta mas corta de " + laberinto.etiqueta(nodoEntrada)
                    + " a " + laberinto.etiqueta(resultado.salida()) + ", costo " + resultado.costo());
        });
    }

    // kahn solo sirve con grafos dirigidos, si el grafo es no dirigido se avisa y no se hace nada
    private void ejecutarOrdenTopologico() {
        if (!laberinto.grafo().esDirigido()) {
            etiquetaEstado.setText("El orden topológico solo aplica a grafos dirigidos: genera uno tipo DAG o DAG NEGATIVO");
            return;
        }

        // kahn regresa la lista de nodos ya ordenados, en la animacion se van sacando uno por uno
        OrdenTopologico.Resultado resultado = new OrdenTopologico().kahn(laberinto);
        List<Integer> orden = resultado.getOrden();

        animar(orden.size(), paso -> {
            int nodoActual = orden.get(paso);
            List<Integer> vecinos = obtenerVecinos(nodoActual);
            panelLaberinto.setOrdenTopologico(new ArrayList<>(orden.subList(0, paso + 1)));
            panelLaberinto.setActual(nodoActual, vecinos);
            etiquetaEstado.setText("Kahn: se saca " + laberinto.etiqueta(nodoActual) + " (posicion " + (paso + 1)
                    + ") y resta 1 al grado de entrada de sus vecinos " + laberinto.lista(vecinos));
        // al terminar, si es dag cada celda muestra su posicion en el orden,
        // si no, se avisa cuantos nodos quedaron sin ordenar por culpa de un ciclo
        }, () -> {
            panelLaberinto.setOrdenTopologico(orden);
            if (resultado.esDAG()) {
                etiquetaEstado.setText("Orden topológico (Kahn) de " + orden.size()
                        + " nodos: el numero de cada celda es su posicion en el orden");
            } else {
                etiquetaEstado.setText("El grafo tiene ciclos: " + resultado.getNodosEnCiclo() + " nodos sin ordenar");
            }
        });
    }
}