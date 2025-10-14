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
        // Hora del servidor
        serverCoordinatorTimeSec = miliAsegundosDeDia(System.currentTimeMillis());
        System.out.printf(" Hora base del coordinador: %s (%.2f seg)%n", 
                          segundosDelDiaAHHMMSS(serverCoordinatorTimeSec), serverCoordinatorTimeSec);
    }
    
    
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

    
    @Override
    public synchronized double registerClientTime(double clientTimeSec, int rttSec, String clientName) throws RemoteException {
        // Calcular tiempo del cliente con compensación RTT/2
        double rttHalf = rttSec / 2.0;
        double clientTimeWithRTT = clientTimeSec + rttHalf;
        
        // Calcular desfase
        double drift = clientTimeWithRTT - serverCoordinatorTimeSec;
        
        // Guardar el desfase
        clientDrifts.put(clientName, drift);
        
        System.out.printf(" %s registro su hora:%n", clientName);
        System.out.printf("   Cliente: %s (%.2f seg)%n", 
                          segundosDelDiaAHHMMSS(clientTimeSec), clientTimeSec);
        System.out.printf("   RTT/2: %.2f seg%n", rttHalf);
        System.out.printf("   Cliente+RTT/2: %.2f seg%n", clientTimeWithRTT);
        System.out.printf("   Desfase calculado: %.2f seg%n", drift);
        System.out.printf("   Clientes registrados: %d/%d%n%n", 
                          clientDrifts.size(), connectedClients.size());
        
        return serverCoordinatorTimeSec;
    }

    @Override
    public synchronized Map<String, Double> synchronizeAllClients(String clientName) throws RemoteException {
        
        Map<String, Double> result = new HashMap<>();
        
        // Verificar que todos los clientes conectados hayan registrado su hora
        if (clientDrifts.size() != connectedClients.size()) {
            System.out.printf("Sincronización solicitada por %s, pero faltan clientes. (%d/%d registrados)%n", 
                              clientName, clientDrifts.size(), connectedClients.size());
            return result;
        }
        
        System.out.printf("Sincronizacion activada por %s%n", clientName);
        System.out.printf("Sincronizando %d clientes conectados.%n", connectedClients.size());
        
        //Calcular la suma de todos los desfases
        double sumOfDrifts = clientDrifts.values().stream().mapToDouble(Double::doubleValue).sum();
        System.out.printf("Suma de desfases: %.2f seg%n", sumOfDrifts);
        
        //Calcular el promedio (desfases / número total de nodos)
        int totalNodes = connectedClients.size() + 1; // Clientes + Servidor
        double averageDrift = sumOfDrifts / totalNodes;
        System.out.printf("Promedio: %.2f / %d = %.2f seg%n", sumOfDrifts, totalNodes, averageDrift);
        
        //Calcular ajustes para cada cliente y el servidor
        System.out.println("\nAjustes calculados:");
        
        // Ajuste del servidor
        double serverAdjustment = averageDrift - 0; // Desfase del servidor es 0
        double serverNewTime = serverCoordinatorTimeSec + serverAdjustment;
        System.out.printf("   Nodos:        Desface                      Nueva Hora\n");
        System.out.printf("   Servidor:     %.2f - 0 = %+.2f seg       %s%n", 
                          averageDrift, serverAdjustment, segundosDelDiaAHHMMSS(serverNewTime));
        
        // Ajustes de los clientes 
        for (Map.Entry<String, Double> entry : clientDrifts.entrySet()) {
            String client = entry.getKey();
            double drift = entry.getValue();
            
            // Ajuste = Promedio - Desfase
            double adjustment = averageDrift - drift;
            double clientCurrentTime = serverCoordinatorTimeSec + drift; 
            double clientNewTime = clientCurrentTime + adjustment;
            
            System.out.printf("   %s:    %.2f - %.2f = %+.2f seg    %s%n", 
                              client, averageDrift, drift, adjustment, segundosDelDiaAHHMMSS(clientNewTime));
            
            // Crear mapa de actualización para este cliente
            Map<String, Double> clientUpdate = new HashMap<>();
            clientUpdate.put("adjustment", adjustment);
            clientUpdate.put("newTime", clientNewTime);
            
            // Guardar la actualización para que todos los clientes la puedan recibir
            pendingSyncUpdates.put(client, clientUpdate);
        }
        
        System.out.println("\nSincronizacion completada.\n");
        // Devolver el resultado para el cliente que inició la sincronización
        if (pendingSyncUpdates.containsKey(clientName)) {
            result = new HashMap<>(pendingSyncUpdates.get(clientName));
        }
        
        clientDrifts.clear();
        
        return result;
    }
    
    @Override
    public synchronized Map<String, Double> checkForSyncUpdate(String clientName) throws RemoteException {
        if (pendingSyncUpdates.containsKey(clientName)) {
            Map<String, Double> update = pendingSyncUpdates.remove(clientName);
            return update;
        }
        return new HashMap<>();
    }
}