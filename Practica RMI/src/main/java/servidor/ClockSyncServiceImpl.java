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

public class ClockSyncServiceImpl extends UnicastRemoteObject implements ClockSyncService {

    private final Set<String> connectedClients = new HashSet<>();
    private final Map<String, Double> clientDrifts = new HashMap<>(); // Desfases de cada cliente
    private final Map<String, Map<String, Double>> pendingSyncUpdates = new HashMap<>(); // Actualizaciones pendientes por cliente
    private final double serverCoordinatorTimeSec;

    public ClockSyncServiceImpl() throws RemoteException {
        super();
        // Hora base del servidor (Coordinador)
        serverCoordinatorTimeSec = millisToSecondsOfDay(System.currentTimeMillis());
        System.out.printf(" Hora base del coordinador: %s (%.2f seg)%n", 
                          secondsOfDayToHHMMSS(serverCoordinatorTimeSec), serverCoordinatorTimeSec);
    }
    
    // --- Métodos Auxiliares ---
    
    private double millisToSecondsOfDay(long millis) {
        LocalTime time = LocalTime.ofInstant(
            java.time.Instant.ofEpochMilli(millis), 
            java.time.ZoneId.systemDefault()
        );
        return time.toSecondOfDay() + (millis % 1000) / 1000.0;
    }
    
    private String secondsOfDayToHHMMSS(double totalSeconds) {
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        double seconds = totalSeconds % 60;
        
        return String.format("%02d:%02d:%05.2f", hours, minutes, seconds);
    }
    
    // --- Métodos de Gestión de Conexiones ---
    
    @Override
    public synchronized void notifyClientConnected(String clientName) throws RemoteException {
        connectedClients.add(clientName);
        System.out.printf(" %s conectado. Total clientes conectados: %d%n", 
                          clientName, connectedClients.size());
    }
    
    @Override
    public synchronized void notifyClientDisconnected(String clientName) throws RemoteException {
        connectedClients.remove(clientName);
        clientDrifts.remove(clientName);
        System.out.printf(" %s desconectado. Total clientes conectados: %d%n", 
                          clientName, connectedClients.size());
    }

    // --- Registro de Hora del Cliente ---
    
    @Override
    public synchronized double registerClientTime(double clientTimeSec, int rttSec, String clientName) throws RemoteException {
        // Calcular tiempo del cliente con compensación RTT/2
        double rttHalf = rttSec / 2.0;
        double clientTimeWithRTT = clientTimeSec + rttHalf;
        
        // Calcular desfase: (Cliente + RTT/2) - Servidor
        double drift = clientTimeWithRTT - serverCoordinatorTimeSec;
        
        // Guardar el desfase
        clientDrifts.put(clientName, drift);
        
        System.out.printf(" %s registro su hora:%n", clientName);
        System.out.printf("   Cliente: %s (%.2f seg)%n", 
                          secondsOfDayToHHMMSS(clientTimeSec), clientTimeSec);
        System.out.printf("   RTT/2: %.2f seg%n", rttHalf);
        System.out.printf("   Cliente+RTT/2: %.2f seg%n", clientTimeWithRTT);
        System.out.printf("   Desfase calculado: %.2f seg%n", drift);
        System.out.printf("   Clientes registrados: %d/%d%n%n", 
                          clientDrifts.size(), connectedClients.size());
        
        return serverCoordinatorTimeSec;
    }

    // --- Sincronización Global ---
    
    @Override
    public synchronized Map<String, Double> synchronizeAllClients(String clientName) throws RemoteException {
        
        Map<String, Double> result = new HashMap<>();
        
        // Verificar que todos los clientes conectados hayan registrado su hora
        if (clientDrifts.size() != connectedClients.size()) {
            System.out.printf("[SERVER] Sincronización solicitada por %s, pero faltan clientes. (%d/%d registrados)%n", 
                              clientName, clientDrifts.size(), connectedClients.size());
            return result; // Mapa vacío
        }
        
        System.out.println("\n**************************************************");
        System.out.printf("[SERVER] SINCRONIZACIÓN ACTIVADA por %s%n", clientName);
        System.out.printf("[SERVER] Sincronizando %d clientes conectados.%n", connectedClients.size());
        System.out.println("**************************************************\n");
        
        // 1. Calcular la suma de todos los desfases
        double sumOfDrifts = clientDrifts.values().stream().mapToDouble(Double::doubleValue).sum();
        System.out.printf("[SERVER] Suma de desfases: %.2f seg%n", sumOfDrifts);
        
        // 2. Calcular el promedio (desfases / número total de nodos)
        int totalNodes = connectedClients.size() + 1; // Clientes + Servidor
        double averageDrift = sumOfDrifts / totalNodes;
        System.out.printf("[SERVER] Promedio: %.2f / %d = %.2f seg%n", sumOfDrifts, totalNodes, averageDrift);
        
        // 3. Calcular ajustes para cada cliente y el servidor
        System.out.println("\n[SERVER] Ajustes calculados:");
        
        // Ajuste del servidor
        double serverAdjustment = averageDrift - 0; // Desfase del servidor es 0
        double serverNewTime = serverCoordinatorTimeSec + serverAdjustment;
        System.out.printf("   Servidor: %.2f - 0 = %+.2f seg ? Nueva hora: %s%n", 
                          averageDrift, serverAdjustment, secondsOfDayToHHMMSS(serverNewTime));
        
        // Ajustes de los clientes - GUARDAR PARA TODOS
        for (Map.Entry<String, Double> entry : clientDrifts.entrySet()) {
            String client = entry.getKey();
            double drift = entry.getValue();
            
            // Ajuste = Promedio - Desfase
            double adjustment = averageDrift - drift;
            double clientCurrentTime = serverCoordinatorTimeSec + drift; // Tiempo actual del cliente
            double clientNewTime = clientCurrentTime + adjustment;
            
            System.out.printf("   %s: %.2f - %.2f = %+.2f seg ? Nueva hora: %s%n", 
                              client, averageDrift, drift, adjustment, secondsOfDayToHHMMSS(clientNewTime));
            
            // Crear mapa de actualización para este cliente
            Map<String, Double> clientUpdate = new HashMap<>();
            clientUpdate.put("adjustment", adjustment);
            clientUpdate.put("newTime", clientNewTime);
            
            // Guardar la actualización para que todos los clientes la puedan recibir
            pendingSyncUpdates.put(client, clientUpdate);
        }
        
        System.out.println("\n**************************************************");
        System.out.println("Sincronizacion completada.");
        System.out.println("**************************************************\n");
        
        // Devolver el resultado para el cliente que inició la sincronización
        if (pendingSyncUpdates.containsKey(clientName)) {
            result = new HashMap<>(pendingSyncUpdates.get(clientName));
        }
        
        // Limpiar desfases para próxima sincronización
        clientDrifts.clear();
        
        return result;
    }
    
    // Nuevo método para que los clientes consulten si hay actualizaciones de sincronización
    @Override
    public synchronized Map<String, Double> checkForSyncUpdate(String clientName) throws RemoteException {
        if (pendingSyncUpdates.containsKey(clientName)) {
            Map<String, Double> update = pendingSyncUpdates.remove(clientName);
            return update;
        }
        return new HashMap<>();
    }
}