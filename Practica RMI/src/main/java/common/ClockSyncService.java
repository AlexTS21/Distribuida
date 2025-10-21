/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface ClockSyncService extends Remote {

    /**
     * Método para registrar el tiempo ajustado del cliente. 
     * Retorna un mapa de ajustes (<Nombre Cliente, Ajuste en Milisegundos>) si se ha alcanzado
     * el número mínimo de clientes (N=3) para la sincronización. 
     * Retorna un mapa vacío si aún está esperando clientes.
     */
    Map<String, Long> registerTimeAndGetAdjustment(long clientTimeMillis, long rtt, String clientName) throws RemoteException;
}