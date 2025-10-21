/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servidor;

import common.ClockSyncService;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ClockSyncServiceImpl extends UnicastRemoteObject implements ClockSyncService {

    // NUEVO: Definición de cuántos clientes se necesitan para activar la sincronización.
    private static final int REQUIRED_CLIENTS = 3; 
    
    private final Map<String, Double> clientAdjustedTimes = new HashMap<>();
    private final Map<String, Double> adjustments = new HashMap<>(); 
    private final double serverCoordinatorTimeSec;

    public ClockSyncServiceImpl() throws RemoteException {
        super();
        // Hora base del servidor (Coordinador)
        serverCoordinatorTimeSec = System.currentTimeMillis() / 1000.0;
        System.out.printf("[SERVER] Hora base del coordinador (T0): %s (%.2f seg)%n", 
                          formatSecondsToHHMMSS(serverCoordinatorTimeSec), serverCoordinatorTimeSec);
    }
    
    // --- Métodos Auxiliares ---

    private String formatSecondsToHHMMSS(double totalSeconds) {
        long seconds = (long) totalSeconds;
        LocalTime time = LocalTime.ofInstant(java.time.Instant.ofEpochMilli(seconds * 1000L), ZoneId.systemDefault());
        double fractionalSeconds = totalSeconds - seconds;
        String ssFractional = String.format("%02d.%02d", time.getSecond(), (int)(fractionalSeconds * 100)); 
        
        return String.format("%02d:%02d:%s", time.getHour(), time.getMinute(), ssFractional);
    }
    
    private void calculateAndStoreAdjustments() {
        adjustments.clear();
        
        // Agregar el tiempo del servidor al conjunto de tiempos a promediar
        Map<String, Double> allTimes = new HashMap<>(clientAdjustedTimes);
        allTimes.put("Servidor", serverCoordinatorTimeSec);
        
        // 1. Calcular el promedio (Berkeley: Suma de todos / N total de nodos)
        double sumOfTimes = allTimes.values().stream().mapToDouble(Double::doubleValue).sum();
        double averageTimeSec = sumOfTimes / allTimes.size();
        
        System.out.printf("[SERVER] Tiempo promedio calculado: %s (%.2f seg)%n", 
                          formatSecondsToHHMMSS(averageTimeSec), averageTimeSec);

        // 2. Calcular los ajustes y almacenarlos
        for (Map.Entry<String, Double> entry : allTimes.entrySet()) {
            double adjustment = averageTimeSec - entry.getValue();
            adjustments.put(entry.getKey(), adjustment);
            
            System.out.printf("   > Ajuste para %s: %+7.2f seg%n", entry.getKey(), adjustment);
        }
    }

    // --- Método Remoto Central (MODIFICADO) ---
    @Override
    public synchronized Map<String, Long> registerTimeAndGetAdjustment(long clientTimeMillis, long rtt, String clientName) throws RemoteException {
        
        // 1. Cálculo del tiempo ajustado del cliente (similar a la Línea 3 de la tabla)
        double rttSec = rtt / 1000.0;
        double clientTimeSec = clientTimeMillis / 1000.0;
        double adjustedClientTimeSec = clientTimeSec + (rttSec / 2.0);
        
        // 2. Guardar el tiempo ajustado del cliente
        clientAdjustedTimes.put(clientName, adjustedClientTimeSec);

        System.out.printf("[SERVER] Recibido de %s (T_ajustado: %s). Clientes Registrados: %d/%d%n", 
                          clientName, formatSecondsToHHMMSS(adjustedClientTimeSec), 
                          clientAdjustedTimes.size(), REQUIRED_CLIENTS);
        
        // 3. LÓGICA DE CONTROL: Si alcanzamos N clientes, calcular y devolver ajustes
        if (clientAdjustedTimes.size() == REQUIRED_CLIENTS) {
            System.out.println("\n**************************************************");
            System.out.printf("[SERVER] SINCRONIZACIÓN ACTIVADA. Calculando promedio de %d nodos.%n", clientAdjustedTimes.size() + 1);
            
            // a) Realiza el cálculo y guarda en 'adjustments' (Double en segundos)
            calculateAndStoreAdjustments(); 
            
            // b) Convertir el mapa de ajustes (en segundos) a milisegundos para el cliente
            Map<String, Long> resultMillis = new HashMap<>();
            for (Map.Entry<String, Double> entry : adjustments.entrySet()) {
                resultMillis.put(entry.getKey(), (long) (entry.getValue() * 1000));
            }
            
            // c) Limpiar las listas para la próxima ronda de sincronización (opcional pero recomendado)
            clientAdjustedTimes.clear();
            
            System.out.println("**************************************************\n");
            
            // d) Devolver el mapa global de ajustes
            return resultMillis;
            
        } 
        
        // 4. Si no se alcanza N, devuelve un mapa vacío para indicar al cliente que espere.
        return new HashMap<>(); 
    }
}