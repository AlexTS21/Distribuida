/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface ClockSyncService extends Remote {
    

    void notifyClientConnected(String clientName) throws RemoteException;
    

    void notifyClientDisconnected(String clientName) throws RemoteException;
    
    double registerClientTime(double clientTimeSec, int rttSec, String clientName) throws RemoteException;
    
    Map<String, Double> synchronizeAllClients(String clientName) throws RemoteException;
    
    Map<String, Double> checkForSyncUpdate(String clientName) throws RemoteException;
}