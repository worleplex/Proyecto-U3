package pruebasLoBorran;

import laberinto.algoritmos.BFS;
import laberinto.algoritmos.OrdenTopologico;
import laberinto.modelo.Laberinto;
import laberinto.modelo.Modo;

import java.util.Random;

/**
 * Una prueba para ver si funciona lo que hice, por si te sirve Yulian, lo borras cuando no lo necesites
 */
public class PruebaBfsKahn {
    public static void main(String[] args) {
        BFS bfs = new BFS();
        OrdenTopologico topologico = new OrdenTopologico();

        System.out.println("Laberinto de ejemplo");
        probar(Laberinto.ejemploDePrueba(), bfs, topologico);

        for (Modo modo : Modo.values()) {
            System.out.println("\n Laberinto aleatorio 5x6, modo " + modo);
            Laberinto laberinto = new Laberinto(5, 6, 35, new Random(7));
            laberinto.construirGrafo(modo);
            probar(laberinto, bfs, topologico);
        }
    }

    private static void probar(Laberinto laberinto, BFS bfs, OrdenTopologico topologico) {
        System.out.println("Entradas: " + laberinto.lista(laberinto.entradas()));
        System.out.println("Salidas:  " + laberinto.lista(laberinto.salidas()));

        BFS.Resultado general = bfs.existeCamino(laberinto);
        System.out.println(general);
        if (general.hayCamino()) {
            System.out.println("  ruta: " + laberinto.lista(general.getRuta()));
        }
        for (int entrada : laberinto.entradas()) {
            BFS.Resultado porEntrada = bfs.existeCamino(laberinto, entrada);
            System.out.println("  desde " + laberinto.etiqueta(entrada) + ": "
                    + (porEntrada.hayCamino() ? "llega a " + laberinto.etiqueta(porEntrada.getSalida()) : "sin salida"));
        }

        OrdenTopologico.Resultado orden = topologico.kahn(laberinto);
        System.out.println(orden);
        System.out.println("  orden: " + laberinto.lista(orden.getOrden()));
    }
}
