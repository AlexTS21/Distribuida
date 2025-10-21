/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cliente;

import common.ClockSyncService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Random;
import javax.swing.table.DefaultTableModel; 
import java.awt.GridBagLayout; // NECESARIO
import java.awt.GridBagConstraints; // NECESARIO

public class ClockSyncClientGUI extends JFrame {

    private static final int RMI_PORT = 1099;
    private static final String SERVICE_NAME = "ClockSyncService";
    
    // Variables de Estado del Cliente
    private long clockDrift;
    private final String clientName;
    private final int simulatedRTTSeg = 2; 
    private ClockSyncService remoteService;
    private long timeSentDuringRegistration; 
    
    // SE ELIMINA: private boolean timeRegistered = false; // NO ES NECESARIA CON EL NUEVO MODELO

    // Componentes GUI
    private JTable tableResultados;         
    private DefaultTableModel tableModel;   
    private JTextArea logArea;              
    private JButton connectButton;
    private JButton syncButton;
    private JButton disconnectButton; 
    private JLabel statusLabel;
    private JLabel timeLabel;

    public ClockSyncClientGUI(String name, int initialDriftSeg) {
        this.clientName = name;
        this.clockDrift = initialDriftSeg * 1000L;
        setTitle("Cliente RMI: " + clientName); 
        initComponents();
        updateClientTimeDisplay();
        this.setVisible(true);
    }
    
    // --- Métodos de Lógica de Tiempo ---

    private long getClientCurrentTimeMillis() {
        return System.currentTimeMillis() + clockDrift;
    }
    
    private String formatMillisToHHMMSS(long millis) {
        LocalTime time = LocalTime.ofInstant(java.time.Instant.ofEpochMilli(millis), java.time.ZoneId.systemDefault());
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }
    
    private String formatSecondsToHHMMSS(double totalSeconds) {
        long seconds = (long) totalSeconds;
        LocalTime time = LocalTime.ofInstant(java.time.Instant.ofEpochMilli(seconds * 1000L), ZoneId.systemDefault());
        double fractionalSeconds = totalSeconds - seconds;
        String ssFractional = String.format("%02d.%02d", time.getSecond(), (int)(fractionalSeconds * 100)); 
        
        return String.format("%02d:%02d:%s", time.getHour(), time.getMinute(), ssFractional);
    }
    
    private void updateClientTimeDisplay() {
        new Timer(100, (ActionEvent e) -> {
            timeLabel.setText("Hora Local: " + formatMillisToHHMMSS(getClientCurrentTimeMillis()));
        }).start();
    }
    
    // --- Lógica RMI y Sincronización ---
    
    private void connectToServer() {
        try {
            statusLabel.setText("Conectando...");
            Registry registry = LocateRegistry.getRegistry("localhost", RMI_PORT);
            remoteService = (ClockSyncService) registry.lookup(SERVICE_NAME);
            
            statusLabel.setText("Conectado");
            logArea.append("[" + clientName + "] Conexión RMI exitosa.\n");
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            syncButton.setEnabled(true);
            
        } catch (Exception e) {
            statusLabel.setText("ERROR");
            logArea.append("[" + clientName + "] Error de conexión: " + e.getMessage() + "\n");
        }
    }
    
    private void disconnectClient() {
        if (remoteService != null) {
            logArea.append("[" + clientName + "] Desconectando y liberando recursos...\n");
            remoteService = null; 
        }
        
        statusLabel.setText("Desconectado");
        dispose();
    }
    
    // --- LÓGICA DE SINCRONIZACIÓN SIMPLIFICADA (USO DE UN SOLO CLIC) ---
    private void startSynchronization() {
        new Thread(() -> {
            try {
                syncButton.setEnabled(false);
                statusLabel.setText("Registrando Hora...");
                logArea.append("--------------------------------------------------\n");
                
                long timeSent = getClientCurrentTimeMillis(); 
                
                // 1. Enviar tiempo y obtener la respuesta del servidor (Ajustes o mapa vacío)
                Map<String, Long> adjustments = remoteService.registerTimeAndGetAdjustment(
                    timeSent, 
                    simulatedRTTSeg * 1000L, 
                    clientName
                );
                
                // 2. PROCESAR RESPUESTA
                SwingUtilities.invokeLater(() -> {
                    // Muestra la tabla de cálculo inicial (T_ajustado = T_local + RTT/2)
                    printConversionTable(timeSent, simulatedRTTSeg * 1000L); 
                    
                    if (adjustments.isEmpty()) {
                        // El servidor aún espera (N=3 está en el servidor)
                        statusLabel.setText("Esperando Nodos (N=3)");
                        logArea.append("[" + clientName + "] Hora registrada. Esperando a los demás clientes...\n");
                        syncButton.setEnabled(true); // Se habilita para que el usuario pueda volver a intentarlo
                    } else {
                        // El servidor devolvió el cálculo final
                        Long adjustmentMillis = adjustments.get(clientName);
                        
                        if (adjustmentMillis != null) {
                             applyAdjustment(adjustmentMillis);
                             statusLabel.setText("Sincronizado Correctamente");
                        } else {
                            statusLabel.setText("Sincronización Terminada");
                        }
                        syncButton.setEnabled(false); // Sincronización terminada
                    }
                });

            } catch (Exception e) {
                handleError(e, "sincronización");
            }
        }).start();
    }
    
    private void handleError(Exception e, String phase) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("ERROR RMI");
            logArea.append("[" + clientName + "] Error en la fase de " + phase + ": " + e.getMessage() + "\n");
            syncButton.setEnabled(true);
        });
        e.printStackTrace();
    }

    // --- Tablas de Cálculo y Ajuste ---
    
    private void applyAdjustment(long adjustmentMillis) {
        this.clockDrift += adjustmentMillis; 
        double adjustmentSeg = adjustmentMillis / 1000.0;
        
        logArea.append("\n============================================\n");
        logArea.append(String.format("AJUSTE FINAL RECIBIDO: %+7.2f segundos\n", adjustmentSeg));
        logArea.append(String.format("NUEVA HORA AJUSTADA:   %s\n", formatMillisToHHMMSS(getClientCurrentTimeMillis())));
        logArea.append("============================================\n");
    }

    private void printConversionTable(long initialTime, long rttActual) {
        
        tableModel.setRowCount(0); 

        double rttSimuladoSeg = simulatedRTTSeg;
        double timeSentSeg = initialTime / 1000.0;
        double adjustedTimeSec = timeSentSeg + (rttSimuladoSeg / 2.0);
        
        // ------------------------------------------
        // CARGAR DATOS EN EL MODELO DE TABLA (JTable)
        // ------------------------------------------

        // Fila 1: Reloj de entrada
        tableModel.addRow(new Object[]{
            "Reloj de entrada", 
            formatMillisToHHMMSS(initialTime),
            String.format("%.2f", timeSentSeg)
        });
                          
        // Fila 2: Compensación RTT
        tableModel.addRow(new Object[]{
            "Compensación RTT (RTT/2)", 
            "",
            String.format("+%.2f", rttSimuladoSeg / 2.0)
        });
        
        // Fila 3: Tiempo ajustado (FINAL)
        tableModel.addRow(new Object[]{
            "Tiempo pre-ajustado (NTP)", 
            formatSecondsToHHMMSS(adjustedTimeSec),
            String.format("%.2f", adjustedTimeSec)
        });
    }

    // --- Inicialización de Componentes (MODIFICADO A GridBagLayout) ---
    private void initComponents() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                disconnectClient();
            }
        });
        
        // Usamos GridBagLayout para un control de posición preciso
        setLayout(new GridBagLayout()); 
        setSize(650, 600); 
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(5, 5, 5, 5); // Espaciado
        
        // Inicialización de Componentes
        statusLabel = new JLabel("Desconectado");
        timeLabel = new JLabel("Hora Local: --");
        connectButton = new JButton("Conectar al Servidor"); 
        syncButton = new JButton("Enviar Hora y Sincronizar"); 
        disconnectButton = new JButton("Desconectar y Salir"); 

        // --- 1. CONFIGURACIÓN DE LA TABLA (JTable) ---
        String[] columnNames = {"CONCEPTO", "HORA (HH:MM:SS.ss)", "TOTAL (SEG.)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableResultados = new JTable(tableModel);
        tableResultados.setFillsViewportHeight(true);
        // Reducir la altura visible de la tabla
        JScrollPane tableScrollPane = new JScrollPane(tableResultados);
        tableScrollPane.setPreferredSize(new java.awt.Dimension(580, 150)); 
        
        // --- 2. CONFIGURACIÓN DEL ÁREA DE LOG ---
        logArea = new JTextArea(10, 50); // MÁS FILAS PARA EL LOG
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);

        // --- 3. AÑADIR COMPONENTES CON GridBagLayout ---
        
        // Fila 0: Info Cliente
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; 
        add(new JLabel(clientName + " (Desfase Inicial: " + (clockDrift / 1000L) + " seg)"), gbc);
        
        // Fila 1: Estado y Hora
        gbc.gridy = 1; gbc.gridwidth = 1; 
        gbc.anchor = GridBagConstraints.WEST;
        add(statusLabel, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.EAST;
        add(timeLabel, gbc);
        
        // Fila 2: Botones de Conexión/Desconexión
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        add(connectButton, gbc);
        
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        add(disconnectButton, gbc); // MOVIDO A LA DERECHA
        
        // Fila 3: Botón de Sincronización (Solo en el centro/abajo de los anteriores)
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        add(syncButton, gbc); // MOVIDO MÁS ABAJO
        
        // Fila 4: Título de la Tabla
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 3; gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("--- TABLA DE CÁLCULOS DEL CLIENTE ---"), gbc);
        
        // Fila 5: JTable
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.2; // La tabla toma menos espacio vertical
        add(tableScrollPane, gbc); 
        
        // Fila 6: Título del Log
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 3; gbc.weighty = 0; gbc.anchor = GridBagConstraints.WEST;
        add(new JLabel("--- Registro de Eventos (Log) ---"), gbc);
        
        // Fila 7: Log Area
        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 3; gbc.weighty = 1.0; // El log toma todo el espacio restante
        add(logScrollPane, gbc); 

        // Event Listeners
        connectButton.addActionListener(e -> connectToServer());
        syncButton.addActionListener(e -> startSynchronization());
        syncButton.setEnabled(false);
        disconnectButton.addActionListener(e -> disconnectClient());
        
        pack();
    }

    // --- Método Principal para Iniciar UNA Sola Ventana (ID ALEATORIO 1-10) ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Random rand = new Random();
            
            int randomId = rand.nextInt(10) + 1; 
            String clientName = "Cliente " + randomId; 
            
            int initialDriftSeg = rand.nextInt(101) - 50; 
            
            new ClockSyncClientGUI(clientName, initialDriftSeg);
        });
    }
}