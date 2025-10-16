/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package servidor;

import common.ClockSyncService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;

public class ClockSyncServer extends JFrame {
    
    private static final int RMI_PORT = 1099;
    private static final String SERVICE_NAME = "ClockSyncService";
    
    private ClockSyncServiceImpl service;
    private JTextArea logArea;
    private JButton syncButton;
    private JList<String> clientList;
    private DefaultListModel<String> listModel;
    private JLabel statusLabel;

    public ClockSyncServer() {
        initComponents();
        startServerRMI();
        // Timer para actualizar la lista de clientes
        new Timer(2000, e -> updateClientList()).start();
    }
    
    private void startServerRMI() {
        try {
            // Inicializar el servicio, pasando el JTextArea para el log
            service = new ClockSyncServiceImpl(logArea);
            
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            registry.rebind(SERVICE_NAME, service);
            
            logArea.append("Servidor RMI de Sincronización de Reloj iniciado en puerto " + RMI_PORT + ".\n");
            logArea.append("Servicio '" + SERVICE_NAME + "' listo.\n");
            statusLabel.setText("Estado: ONLINE");
            syncButton.setEnabled(true);
            
        } catch (Exception e) {
            logArea.append("Error en el servidor: " + e.toString() + "\n");
            statusLabel.setText("Estado: ERROR");
            e.printStackTrace();
        }
    }
    
    private void updateClientList() {
        if (service == null) return;
        try {
            Set<String> currentClients = service.getRegisteredClients();
            
            // Limpiar y volver a llenar el modelo de lista
            listModel.clear();
            for (String client : currentClients) {
                listModel.addElement(client);
            }
        } catch (Exception e) {
            // Omitir error de RMI si un cliente se desconecta
        }
    }
    
    private void synchronizeSelectedClients() {
        new Thread(() -> {
            try {
                // Obtener los clientes seleccionados
                java.util.List<String> selectedClients = clientList.getSelectedValuesList();
                Set<String> clientsToSync = new HashSet<>(selectedClients);
                
                if (clientsToSync.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Debe seleccionar al menos un cliente.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                syncButton.setEnabled(false);
                logArea.append("\n--- INICIANDO SINCRONIZACIÓN ---\n");
                
                // Llamar al método RMI
                Map<String, Double> serverAdjustment = service.synchronizeSelectedClients(clientsToSync);
                
                // Mostrar ajuste del servidor (opcional)
                if (serverAdjustment.containsKey("adjustment")) {
                    logArea.append("AJUSTE DEL SERVIDOR: " + String.format("%+.2f", serverAdjustment.get("adjustment")) + " seg\n");
                }
                
                syncButton.setEnabled(true);

            } catch (Exception e) {
                logArea.append("Error al sincronizar: " + e.getMessage() + "\n");
                syncButton.setEnabled(true);
            }
        }).start();
    }

    private void initComponents() {
        setTitle("Servidor RMI (Coordinador)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 700);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 1. Status Label
        statusLabel = new JLabel("Estado: Iniciando...");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        add(statusLabel, gbc);

        // 2. Título de la lista
        JLabel listTitle = new JLabel("Clientes Registrados (Seleccione para ajustar):");
        gbc.gridy = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        add(listTitle, gbc);
        
        // 3. JList de Clientes
        listModel = new DefaultListModel<>();
        clientList = new JList<>(listModel);
        clientList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane listScrollPane = new JScrollPane(clientList);
        gbc.gridy = 2; gbc.gridheight = 2; gbc.weighty = 0.2; gbc.gridwidth = 2;
        add(listScrollPane, gbc);
        
        // 4. Botón Sincronizar
        syncButton = new JButton("SINCRONIZAR CLIENTES SELECCIONADOS");
        syncButton.addActionListener(e -> synchronizeSelectedClients());
        syncButton.setEnabled(false);
        gbc.gridy = 4; gbc.gridheight = 1; gbc.weighty = 0.0; gbc.gridwidth = 2;
        add(syncButton, gbc);
        
        // 5. Título del Log
        JLabel logTitle = new JLabel("Registro de Eventos del Servidor:");
        gbc.gridy = 5; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.HORIZONTAL;
        add(logTitle, gbc);
        
        // 6. Log Area
        logArea = new JTextArea(15, 60);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        gbc.gridy = 6; gbc.gridwidth = 2; gbc.weighty = 0.8;
        add(logScrollPane, gbc);

        pack();
        setLocationRelativeTo(null); // Centrar
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClockSyncServer::new);
    }
}