package laberinto.vista;

import javax.swing.SwingUtilities;

public class VistaPrincipal {

    public static void main(String[] args) {
        // el main pa correr el proyecto xd y uso el invoke despues porque el Swing no es seguro para hilos
        // esto lo confirme en arquitectura de software hay algo llamada EDT que es donde la GUI se anda
        // ejecutando en un hilo especial asi que recorde estructura de datos y la estructura cola, entonces
        // el invokeLater lo que hace es le dice a java no generes al erick en esta ventana aquí, 
        // ponla en la cola del EDT (erick no tomes ss) y que él la cree cuando le toque asi el nwe laventa visible
        // se crea en el hilo correctop
        SwingUtilities.invokeLater(()->new LaVentana().setVisible(true));
    }

}
