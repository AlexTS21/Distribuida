/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cliente;

import common.ClockSyncService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Random;
import javax.swing.table.DefaultTableModel; 
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

public class ClockSyncClientGUI extends JFrame {

    private static final int RMI_PORT = 8080;
    private static final String SERVICE_NAME = "ClockSyncService";
    
    // Variables de Estado del Cliente
    private long clockDrift = 0; // Inicialmente sin desfase
    private final String clientName;
    private final int simulatedRTTSeg = 2; 
    private ClockSyncService remoteService;
    private boolean timeRegistered = false;

    // Componentes GUI
    private JTable tableResultados;         
    private DefaultTableModel tableModel;   
    private JTextArea logArea;              
    private JButton connectButton;
    private JButton sendTimeButton;
    // Eliminado: private JButton synchronizeButton;
    private JButton disconnectButton; 
    private JLabel statusLabel;
    private JLabel timeLabel;
    private JLabel driftLabel;
    
    private Timer syncCheckTimer; // NUEVO: Timer para checar actualizaciones

    public ClockSyncClientGUI(String name) {
        this.clientName = name;
        setTitle("Cliente RMI: " + clientName); 
        initComponents();
        actualizaTiempoCliente();
        this.setVisible(true);
    }
    
    // --- Métodos de Lógica de Tiempo ---

    private long getClientCurrentTimeMillis() {
        return System.currentTimeMillis() + clockDrift;
    }
    
    // Convierte milisegundos a segundos del día
    private double miliAsegundos(long millis) {
        LocalTime time = LocalTime.ofInstant(
            java.time.Instant.ofEpochMilli(millis), 
            java.time.ZoneId.systemDefault()
        );
        return time.toSecondOfDay() + (millis % 1000) / 1000.0;
    }
    
    // Convierte segundos del día a formato HH:MM:SS.ss
    private String segundosDelDiaAHHMMSS(double totalSeconds) {
        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) ((totalSeconds % 3600) / 60);
        double seconds = totalSeconds % 60;
        
        return String.format("%02d:%02d:%05.2f", hours, minutes, seconds);
    }
    
    private String formatMillisToHHMMSS(long millis) {
        LocalTime time = LocalTime.ofInstant(
            java.time.Instant.ofEpochMilli(millis), 
            java.time.ZoneId.systemDefault()
        );
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }
    
    private void actualizaTiempoCliente() {
        new Timer(100, (ActionEvent e) -> {
            timeLabel.setText("Hora Local: " + formatMillisToHHMMSS(getClientCurrentTimeMillis()));
        }).start();
    }
    
    private void connectToServer() {
        try {
            statusLabel.setText("Conectando...");
            Registry registry = LocateRegistry.getRegistry("localhost", RMI_PORT); //172.31.10.21 | localhost
            remoteService = (ClockSyncService) registry.lookup(SERVICE_NAME);
            
            // Notificar al servidor que este cliente se ha conectado
            remoteService.notifyClientConnected(clientName);
            
            statusLabel.setText("Conectado");
            logArea.append("[" + clientName + "] Conexion RMI exitosa.\n");
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            sendTimeButton.setEnabled(true);
            
            // Iniciar el chequeo periódico de sincronización
            startSyncCheckTimer();
            
        } catch (Exception e) {
            statusLabel.setText("ERROR");
            logArea.append("[" + clientName + "] Error de conexion: " + e.getMessage() + "\n");
        }
    }
    
    private void disconnectClient() {
        if (remoteService != null) {
            try {
                remoteService.notifyClientDisconnected(clientName);
                logArea.append("[" + clientName + "] Desconectando...\n");
            } catch (Exception e) {
                logArea.append("[" + clientName + "] Error al desconectar: " + e.getMessage() + "\n");
            }
            remoteService = null; 
        }
        
        if (syncCheckTimer != null) {
            syncCheckTimer.stop();
        }
        
        statusLabel.setText("Desconectado");
        dispose();
    }
    
    // NUEVO: Timer para checar si hay actualización pendiente del servidor
    private void startSyncCheckTimer() {
        if (syncCheckTimer != null) {
            syncCheckTimer.stop();
        }
        
        // Chequea cada 3 segundos
        syncCheckTimer = new Timer(3000, e -> checkForSyncUpdate());
        syncCheckTimer.start();
        logArea.append("[" + clientName + "] Iniciando chequeo de sincronización (polling).\n");
    }
    
    // NUEVO: Método para recibir el ajuste desde el servidor
    private void checkForSyncUpdate() {
        new Thread(() -> {
            if (remoteService == null) return;
            try {
                // Llama al método RMI
                Map<String, Double> result = remoteService.checkForSyncUpdate(clientName);
                
                if (result != null && result.containsKey("adjustment")) {
                    SwingUtilities.invokeLater(() -> {
                        double adjustmentSec = result.get("adjustment");
                        double newTimeSec = result.get("newTime");
                        
                        // Aplicar ajuste
                        long adjustmentMillis = (long) (adjustmentSec * 1000);
                        clockDrift += adjustmentMillis;
                        
                        logArea.append("\n*** ACTUALIZACIÓN RECIBIDA DEL SERVIDOR ***\n");
                        logArea.append(String.format("Ajuste aplicado: %+.2f segundos\n", adjustmentSec));
                        logArea.append(String.format("Nueva hora sincronizada: %s\n", 
                            segundosDelDiaAHHMMSS(newTimeSec)));
                        logArea.append("********************************************\n");
                        
                        statusLabel.setText("Sincronizado");
                        driftLabel.setText(String.format("Último Ajuste: %+.2f seg", adjustmentSec));
                    });
                }
            } catch (Exception e) {
                // Ignorar si es error temporal de RMI
                // System.err.println("[" + clientName + "] Error en polling: " + e.getMessage());
            }
        }).start();
    }
    
    // Solo enviar hora al servidor
    private void sendTimeToServer() {
        new Thread(() -> {
            try {
                sendTimeButton.setEnabled(false);
                statusLabel.setText("Enviando hora...");
                logArea.append("\n");
                
                long timeSent = getClientCurrentTimeMillis(); 
                double clientTimeSec = miliAsegundos(timeSent);
                
                // Enviar hora al servidor (sin sincronizar todavía)
                double serverTimeSec = remoteService.registerClientTime(clientTimeSec, simulatedRTTSeg, clientName);
                
                SwingUtilities.invokeLater(() -> {
                    // Calcular desfase inicial con RTT/2
                    double rttHalf = simulatedRTTSeg / 2.0;
                    double clientTimeWithRTT = clientTimeSec + rttHalf;
                    double drift = clientTimeWithRTT - serverTimeSec;
                    
                    // Mostrar tabla de cálculo
                    imprimirConversionTabla(timeSent, serverTimeSec, drift, rttHalf);
                    
                    logArea.append(String.format("[%s] Hora enviada: %s (%.2f seg)\n", 
                        clientName, formatMillisToHHMMSS(timeSent), clientTimeSec));
                    logArea.append(String.format("[%s] Hora servidor: %.2f seg\n", clientName, serverTimeSec));
                    logArea.append(String.format("[%s] Desfase calculado: %.2f seg\n", clientName, drift));
                    
                    driftLabel.setText(String.format("Desfase Inicial: %.2f seg", drift));
                    statusLabel.setText("Hora Registrada");
                    timeRegistered = true;
                    // synchronizeButton.setEnabled(true); // Ya no existe
                });

            } catch (Exception e) {
                handleError(e, "envío de hora");
            }
        }).start();
    }
    
    private void handleError(Exception e, String phase) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Error RMI");
            logArea.append("[" + clientName + "] Error en " + phase + ": " + e.getMessage() + "\n");
            sendTimeButton.setEnabled(true);
        });
        e.printStackTrace();
    }

    private void imprimirConversionTabla(long initialTime, double serverTimeSec, double drift, double rttHalf) {
        tableModel.setRowCount(0); 

        double tiempoClienteSec = miliAsegundos(initialTime);
        double tiempoClienteRTT = tiempoClienteSec + rttHalf;
        
        // Fila 1: Reloj del cliente
        tableModel.addRow(new Object[]{
            "Hora del Cliente", 
            formatMillisToHHMMSS(initialTime),
            String.format("%.2f", tiempoClienteSec)
        });
                          
        // Fila 2: Compensación RTT
        tableModel.addRow(new Object[]{
            "Compensación RTT/2", 
            "",
            String.format("+%.2f", rttHalf)
        });
        
        // Fila 3: Tiempo con RTT
        tableModel.addRow(new Object[]{
            "Cliente + RTT/2", 
            segundosDelDiaAHHMMSS(tiempoClienteRTT),
            String.format("%.2f", tiempoClienteRTT)
        });
        
        // Fila 4: Hora del servidor
        tableModel.addRow(new Object[]{
            "Hora del Servidor", 
            segundosDelDiaAHHMMSS(serverTimeSec),
            String.format("%.2f", serverTimeSec)
        });
        
        // Fila 5: Desfase
        tableModel.addRow(new Object[]{
            "Desfase (Cliente-Servidor)", 
            "",
            String.format("%.2f", drift)
        });
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                disconnectClient();
            }
        });
        
        setLayout(new GridBagLayout()); 
        setSize(650, 650); 
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        
        // Inicialización de Componentes
        statusLabel = new JLabel("Desconectado");
        timeLabel = new JLabel("Hora Local: --");
        driftLabel = new JLabel("Desfase: No calculado");
        connectButton = new JButton("Conectar al Servidor"); 
        sendTimeButton = new JButton("Enviar Hora"); 
        // Eliminado: synchronizeButton
        disconnectButton = new JButton("Desconectar y Salir");

        // Configuración de la tabla
        String[] columnNames = {"Concepto", "Hora (HH:MM:SS.ss)", "Total (SEG.)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableResultados = new JTable(tableModel);
        tableResultados.setFillsViewportHeight(true);
        JScrollPane tableScrollPane = new JScrollPane(tableResultados);
        tableScrollPane.setPreferredSize(new java.awt.Dimension(580, 180)); 
        
        // Configuración del área de log
        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // AÑADIR COMPONENTES
        
        // Fila 0: Info Cliente
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; 
        add(new JLabel(clientName), gbc);
        
        // Fila 1: Estado y Hora
        gbc.gridy = 1; gbc.gridwidth = 1; 
        gbc.anchor = GridBagConstraints.WEST;
        add(statusLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST;
        add(timeLabel, gbc);
        
        // Fila 2: Desfase
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 3; 
        gbc.anchor = GridBagConstraints.CENTER;
        add(driftLabel, gbc);
        
        // Fila 3: Botones de Conexión/Desconexión
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        add(connectButton, gbc);
        
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        add(disconnectButton, gbc);
        
        // Fila 4: Botón Enviar Hora
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        add(sendTimeButton, gbc);
        
        // Fila 5: Título de la Tabla
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel(" TABLA DE CALCULOS DEL CLIENTE"), gbc);
        
        // Fila 6: JTable
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.3;
        add(tableScrollPane, gbc); 
        
        // Fila 7: Título del Log
        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 3; gbc.weighty = 0; gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel(" Registro de Eventos log (Esperando ajuste del Servidor) "), gbc);
        
        // Fila 8: Log Area
        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 3; gbc.weighty = 0.7;
        add(logScrollPane, gbc); 
        
        // Event Listeners
        connectButton.addActionListener(e -> connectToServer());
        sendTimeButton.addActionListener(e -> sendTimeToServer());
        sendTimeButton.setEnabled(false);
        disconnectButton.addActionListener(e -> disconnectClient());
        
        pack();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Random rand = new Random();
            
            int randomId = rand.nextInt(20) + 1; 
            String clientName = "Cliente " + randomId; 
            
            new ClockSyncClientGUI(clientName);
        });
    }
}