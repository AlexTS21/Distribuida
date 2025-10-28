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
    private static final DefaultListModel<String> listModel =  new DefaultListModel<>();
    private static final JList<String> clientList  = new JList<>(listModel);;

   // private listModel =
   
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
            // 1. Obtener los clientes actualmente registrados en el servicio RMI
            Set<String> newClients = service.getRegisteredClients();
        
            // 2. Crear un Set con los clientes actualmente en la GUI (listModel)
            Set<String> currentClientsInModel = new HashSet<>();
            for (int i = 0; i < listModel.getSize(); i++) {
                currentClientsInModel.add(listModel.getElementAt(i));
            }
        
            // 3. Identificar clientes a ELIMINAR (Están en el modelo pero ya no en el RMI)
            Set<String> clientsToRemove = new HashSet<>(currentClientsInModel);
            clientsToRemove.removeAll(newClients); // Deja solo los que están en el modelo y no en el RMI
        
            for (String client : clientsToRemove) {
                listModel.removeElement(client);
            }
        
            // 4. Identificar clientes a AÑADIR (Están en el RMI pero no en el modelo)
            Set<String> clientsToAdd = new HashSet<>(newClients);
            clientsToAdd.removeAll(currentClientsInModel); // Deja solo los que están en el RMI y no en el modelo
        
            for (String client : clientsToAdd) {
                // Se añaden nuevos elementos al final de la lista
                listModel.addElement(client);
            }
        
            // Nota: Las selecciones persisten porque los objetos de la lista no se eliminan y recrean a menos que sea estrictamente necesario.
        
        } catch (Exception e) {
            // Esto podría ser un error de RMI temporal. Es mejor logearlo y no detener el Timer.
            // logArea.append("Error de RMI al actualizar lista: " + e.getMessage() + "\n");
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
                
                //Borrar seleccion
                for (String client : selectedClients) {
                    listModel.removeElement(client);
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