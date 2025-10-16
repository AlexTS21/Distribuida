/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set; // Importar java.util.Set

public interface ClockSyncService extends Remote {
    

    void notifyClientConnected(String clientName) throws RemoteException;
    

    void notifyClientDisconnected(String clientName) throws RemoteException;
    
    // El cliente sigue registrando su hora, el servidor responde con su hora base
    double registerClientTime(double clientTimeSec, int rttSec, String clientName) throws RemoteException;
    
    /**
     * Nuevo método: El Servidor inicia la sincronización para un conjunto de clientes seleccionados.
     * Retorna el ajuste del servidor.
     */
    Map<String, Double> synchronizeSelectedClients(Set<String> clientsToSync) throws RemoteException;
    
    // El cliente usa este método para "preguntar" si hay una actualización disponible para él.
    Map<String, Double> checkForSyncUpdate(String clientName) throws RemoteException;
    
    // Método para obtener la lista actual de clientes conectados y registrados
    Set<String> getRegisteredClients() throws RemoteException;
}