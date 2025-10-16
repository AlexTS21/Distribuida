/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servidor;

import common.ClockSyncService;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JTextArea; // Importar JTextArea

public class ClockSyncServiceImpl extends UnicastRemoteObject implements ClockSyncService {

    private final Set<String> connectedClients = new HashSet<>();
    private final Map<String, Double> clientDrifts = new HashMap<>(); // Desfases de cada cliente
    private final Map<String, Map<String, Double>> pendingSyncUpdates = new HashMap<>(); // Actualizaciones pendientes por cliente
    private final double serverCoordinatorTimeSec;
    private final JTextArea logArea; // Referencia al log de la GUI del Servidor

    // Modificar el constructor para recibir el JTextArea
    public ClockSyncServiceImpl(JTextArea logArea) throws RemoteException {
        super();
        this.logArea = logArea;
        // Hora del servidor
        serverCoordinatorTimeSec = miliAsegundosDeDia(System.currentTimeMillis());
        log(" Hora base del coordinador: " + segundosDelDiaAHHMMSS(serverCoordinatorTimeSec) 
            + " (" + String.format("%.2f", serverCoordinatorTimeSec) + " seg)");
    }
    
    // NUEVO: Método para redirigir la salida al JTextArea y la consola
    private void log(String message) {
        System.out.println(message);
        if (logArea != null) {
            logArea.append(message + "\n");
        }
    }
    
    // Métodos utilitarios (sin cambios)
    private double miliAsegundosDeDia(long millis) {
        LocalTime time = LocalTime.ofInstant(
            java.time.Instant.ofEpochMilli(millis), 
            java.time.ZoneId.systemDefault()
        );
        return time.toSecondOfDay() + (millis % 1000) / 1000.0;
    }
    
    private String segundosDelDiaAHHMMSS(double totalSeconds) {
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        double seconds = totalSeconds % 60;
        
        return String.format("%02d:%02d:%05.2f", hours, minutes, seconds);
    }
    
    
    @Override
    public synchronized void notifyClientConnected(String clientName) throws RemoteException {
        connectedClients.add(clientName);
        log(" " + clientName + " conectado. Total clientes conectados: " + connectedClients.size());
    }
    
    @Override
    public synchronized void notifyClientDisconnected(String clientName) throws RemoteException {
        connectedClients.remove(clientName);
        clientDrifts.remove(clientName);
        pendingSyncUpdates.remove(clientName);
        log(" " + clientName + " desconectado. Total clientes conectados: " + connectedClients.size());
    }

    
    @Override
    public synchronized double registerClientTime(double clientTimeSec, int rttSec, String clientName) throws RemoteException {
        // Calcular tiempo del cliente con compensación RTT/2
        double rttHalf = rttSec / 2.0;
        double clientTimeWithRTT = clientTimeSec + rttHalf;
        
        // Calcular desfase
        double drift = clientTimeWithRTT - serverCoordinatorTimeSec;
        
        // Guardar el desfase
        clientDrifts.put(clientName, drift);
        
        log(" " + clientName + " registro su hora:");
        log("   Cliente: " + segundosDelDiaAHHMMSS(clientTimeSec) + " (" + String.format("%.2f", clientTimeSec) + " seg)");
        log("   RTT/2: " + String.format("%.2f", rttHalf) + " seg");
        log("   Cliente+RTT/2: " + String.format("%.2f", clientTimeWithRTT) + " seg");
        log("   Desfase calculado: " + String.format("%.2f", drift) + " seg");
        log("   Clientes registrados: " + clientDrifts.size() + "/" + connectedClients.size() + "\n");
        
        return serverCoordinatorTimeSec;
    }

    @Override
    public synchronized Map<String, Double> synchronizeSelectedClients(Set<String> clientsToSync) throws RemoteException {
        
        Map<String, Double> result = new HashMap<>(); // Retornará el ajuste del servidor
        
        // Verificar que todos los clientes conectados hayan registrado su hora (Requisito de Berkeley)
        if (clientDrifts.size() != connectedClients.size()) {
            log("Sincronización solicitada, pero faltan clientes. (" + clientDrifts.size() 
                + "/" + connectedClients.size() + " registrados)");
            return result;
        }
        
        log("Sincronización activada por el Servidor.");
        log("Clientes seleccionados para ajuste: " + clientsToSync.toString());
        
        // 1. Calcular la suma de todos los desfases
        double sumOfDrifts = clientDrifts.values().stream().mapToDouble(Double::doubleValue).sum();
        log("Suma de desfases: " + String.format("%.2f", sumOfDrifts) + " seg");
        
        // 2. Calcular el promedio (desfases / número total de nodos)
        int totalNodes = connectedClients.size() + 1; // Clientes + Servidor
        double averageDrift = sumOfDrifts / totalNodes;
        log("Promedio: " + String.format("%.2f", sumOfDrifts) + " / " + totalNodes 
            + " = " + String.format("%.2f", averageDrift) + " seg");
        
        // 3. Calcular ajustes para cada cliente y el servidor
        log("\nAjustes calculados:");
        
        // Ajuste del servidor
        double serverAdjustment = averageDrift - 0; // Desfase del servidor es 0
        double serverNewTime = serverCoordinatorTimeSec + serverAdjustment;
        log("   Nodos:        Desfase                      Nueva Hora");
        log("   Servidor:     " + String.format("%.2f", averageDrift) + " - 0 = " 
            + String.format("%+.2f", serverAdjustment) + " seg       " 
            + segundosDelDiaAHHMMSS(serverNewTime));
        
        // Guardar ajuste del servidor
        result.put("adjustment", serverAdjustment);
        result.put("newTime", serverNewTime);
        
        // Ajustes de los clientes 
        for (Map.Entry<String, Double> entry : clientDrifts.entrySet()) {
            String client = entry.getKey();
            double drift = entry.getValue();
            
            // Ajuste = Promedio - Desfase
            double adjustment = averageDrift - drift;
            double clientCurrentTime = serverCoordinatorTimeSec + drift; 
            double clientNewTime = clientCurrentTime + adjustment;
            
            // Logear solo si el cliente fue seleccionado o si es un log informativo.
            if (clientsToSync.contains(client)) {
                 log("   -> " + client + ":    " + String.format("%.2f", averageDrift) + " - " 
                     + String.format("%.2f", drift) + " = " + String.format("%+.2f", adjustment) 
                     + " seg    " + segundosDelDiaAHHMMSS(clientNewTime) + " (ACTUALIZADO)");
                
                // Crear mapa de actualización para este cliente
                Map<String, Double> clientUpdate = new HashMap<>();
                clientUpdate.put("adjustment", adjustment);
                clientUpdate.put("newTime", clientNewTime);
            
                // Guardar la actualización para que el cliente la pueda recibir
                pendingSyncUpdates.put(client, clientUpdate);
            } else {
                 log("   " + client + ":    " + String.format("%.2f", averageDrift) + " - " 
                     + String.format("%.2f", drift) + " = " + String.format("%+.2f", adjustment) 
                     + " seg    " + segundosDelDiaAHHMMSS(clientNewTime) + " (NO AJUSTADO)");
            }
        }
        
        log("\nSincronización completada.\n");
        
        // No limpiamos clientDrifts.clear() para permitir futuras sincronizaciones con la misma data.
        
        return result;
    }
    
    @Override
    public synchronized Map<String, Double> checkForSyncUpdate(String clientName) throws RemoteException {
        if (pendingSyncUpdates.containsKey(clientName)) {
            // Se remueve para que no se entregue dos veces
            Map<String, Double> update = pendingSyncUpdates.remove(clientName); 
            return update;
        }
        return new HashMap<>();
    }
    
    // NUEVO: Obtener la lista de clientes registrados (para la GUI del Servidor)
    @Override
    public synchronized Set<String> getRegisteredClients() throws RemoteException {
        return new HashSet<>(clientDrifts.keySet());
    }
}