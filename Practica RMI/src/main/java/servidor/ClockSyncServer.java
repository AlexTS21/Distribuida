/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClockSyncServer {
    
    private static final int RMI_PORT = 1099;

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            ClockSyncServiceImpl service = new ClockSyncServiceImpl();
            registry.rebind("ClockSyncService", service);
            System.out.printf("Servidor RMI de Sincronizacion de Reloj iniciado en puerto %d.%n", RMI_PORT);
            System.out.println("Servidor inicializado y listo para la sincronizacion de clientes.");
        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.toString());
            e.printStackTrace();
        }
    }
}