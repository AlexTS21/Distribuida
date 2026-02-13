import org.apache.xmlrpc.webserver.WebServer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel; 
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author aleja
 */


public class ServerGUI extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ServerGUI.class.getName());

    /**
     * Creates new form ServerGUI
     */
    private Timer timer;
    private int currentTime = 0;
    private WebServer rpcServer;
    private DefaultTableModel tablePlanificator;
    private DefaultTableModel tableProcess;
    private DefaultTableModel tableQueue;
    private DefaultTableModel tableWaitingTime;
    private DefaultTableModel tableCompletionTime;
    
    private DefaultTableModel tablePenalty;

    
    private final List<String> processNames = List.of("A", "B", "C", "D", "E");
    private int antTime = 10;
    private final String[] planificatorHeaders = new String[11];
    private final int actualizationTime = 1000;
    private int desconectionInactivityTime = 5;
    
    private Map<String, Integer> processArrivalTime = new HashMap<>();
    private Map<String, Integer> processStartTime = new HashMap<>();
    private Map<String, Integer> processDuration = new HashMap<>();
    private List<String> completedProcesses = new ArrayList<>();
    
    
   
    //Inicializadores de interfaz grafica
    public ServerGUI() {
        initComponents();
        initPlanificatorTable();
        initProcessTable();
        initQueueTable();
        initWaitingTimeTable();
        initCompletionTimeTable();
        initPenaltyTable();

        
    }
 
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new ServerGUI().setVisible(true));
    }

    //Server functions-----------------------------------------------------------------------
    private void startServer() {
        try {
            int port = 8080;
            messageLabel.setText("Iniciando servidor en el puerto " + port + "...");
            rpcServer = new WebServer(port);
            XmlRpcServer xmlRpcServer = rpcServer.getXmlRpcServer();

            PropertyHandlerMapping phm = new PropertyHandlerMapping();

            // Pasar la instancia del GUI a la clase estática
            ProcessHandler.setGUI(this);
            phm.addHandler("process", ProcessHandler.class);
           

            xmlRpcServer.setHandlerMapping(phm);

            rpcServer.start();
            desconectionInactivityTime = 5;
            messageLabel.setText("Servidor RPC iniciado en el puerto " + port);

            startTimer();
            initServerButton.setEnabled(false);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al iniciar el servidor RPC", e);
            messageLabel.setText("Error al iniciar servidor");
        }
    }
    
     public String ping() {
    return "OK";
}
    
    private void stopServer() {
        if (rpcServer != null) {
            rpcServer.shutdown();
            logger.info("Servidor detenido.");
        }
        if (timer != null) {
            timer.cancel();
        }
        messageLabel.setText("Servidor detenido");
    }
    
    //Manejo de tiempo------------------------------------------------------------------------
    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (shouldContinueTimer()) {
                        executeMainLogic();
                    } else {
                        handleTimerCompletion();
                    }
                });
            }
        }, 0, actualizationTime); // Every  second
    }
    
    //Actualizar headers
    //updatePlanificatorTableHeaders();
    //Actualizar contenido de la tabla del planificador
    //Actualizar tabla de procesos
    //Intentar sacar procesos de la cola
    //Actualizar timer aqui
    private void executeMainLogic() {
        System.out.println("CONTADOR DE INACTIVIAD: " +desconectionInactivityTime);
        // 1. Actualizar headers de la tabla
        updatePlanificatorTable();

        // 2. Procesar cola de espera
        processQueue();
        updateWaitingTimeTable();

        // 3. Actualizar otras tablas
        updateCompletionTimeTable();
        
        updatePenaltyTable();


        // 4. Incrementar tiempo
        currentTime++;
    }
    
    private void handleTimerCompletion() {
        // Detener timer y servidor
        timer.cancel();
        stopServer();

        // Habilitar botón de inicio
        initServerButton.setEnabled(true);

        // Extender el tiempo para el próximo ciclo
        //antTime += currentTime;

        // Mostrar mensaje al usuario
        messageLabel.setText("Servidor desconectado.");

        // Log para debugging
        logger.info("Timer completado. CurrentTime: " + currentTime);
    }
    
    //Esperar 5 tiempos antes de cerrar el servidor
    private boolean shouldContinueTimer() {
        //Revisar si la tabla ya esta vacia
        //Esperar 5 unidades de tiempo para desconectar al servidor si tabla y cola estan vaicias decrementar desconectionInactivityTime
        
        if (isQueueTableEmpy() && isPlanificatorTableEmpy()){
            messageLabel.setText("Esperando procesos antes de desconectar.");
            desconectionInactivityTime = desconectionInactivityTime-1;
        }
        if (desconectionInactivityTime<0){
            System.out.println("Cerro server");
            return false;
        }
        
        
        return true;
    }

    //Manejo de cola de procesos-----------------------------------------------------------------------
    public void processQueue(){
        if (!isQueueTableEmpy()){
            String processQueueName = (String) tableQueue.getValueAt(0, 1);
            int processQueueStartTime = (int) tableQueue.getValueAt(0, 2);
            int processQueueDuration = (int) tableQueue.getValueAt(0, 3);
            if(checkProcess(processQueueName ,processQueueStartTime , processQueueDuration)){
                
                processStartTime.put(processQueueName, currentTime + processQueueStartTime);
                //Draw new process
                int index = processNames.indexOf(processQueueName);
                //Draw proceess if thre resource is avalible
                System.out.println("START TIME INDEX: " + processQueueStartTime);
                for (int i=1+processQueueStartTime; i<processQueueStartTime+1+processQueueDuration; i++ ){
                    tablePlanificator.setValueAt("o", index, i);
                }
                //Delete for queue table 
                popProcessQueueTable();
                //Incert in process table
                updateProcessTable( processQueueName, processQueueStartTime, processQueueDuration);
                
                calculateWaitingTime(processQueueName);
            }
        }
    }
    
    //Revisar la logica si un procesos puede entrar a la tabla
    public boolean checkProcess(String processName, int initTime, int durationTime){
        Object[][] dataTable = getPlanificatorTableData();
        int index = processNames.indexOf(processName);
        //Check if currentTime + init time is avalible (null)
        if (dataTable[index][initTime+1] != null){
            System.out.println("INDEX OF END TIME: " + initTime+2);
            return false;
        //Check if duration dont excede planificator resource
        }else if(initTime+durationTime > 10){
            messageLabel.setText("Proceso " + processName + " excede la capacidad del servidor" + "c: " + initTime + "d: "+ durationTime);
            System.out.println("DURATION PLUS INIT: "+ initTime + " "+ durationTime);
            return false;
        }
        return true;
    }
    
    
    private void calculateWaitingTime(String processName) {
    if (processArrivalTime.containsKey(processName) && 
        processStartTime.containsKey(processName) && 
        processDuration.containsKey(processName)) {
        
        //Variables de la formula 
        int arrival = processArrivalTime.get(processName);
        int start = processStartTime.get(processName);    
        int t = processDuration.get(processName);          
        
        //Tiempo de espera
        int E = start - arrival;
        
        // Agregar a lista de procesos completados
        if (!completedProcesses.contains(processName)) {
            completedProcesses.add(processName);
        }
    }
}
    
    private void updateWaitingTimeTable() {
    // Limpiar tabla
    for (int i = 0; i < tableWaitingTime.getRowCount(); i++) {
        for (int j = 0; j < tableWaitingTime.getColumnCount(); j++) {
            tableWaitingTime.setValueAt(null, i, j);
        }
    }
    
    //Variables para calcular promedio
    double totalEspera = 0;
    int contadorProcesos = 0;
    
    //Llenar tabla con E = Inicio - Llegada
    for (int i = 0; i < completedProcesses.size(); i++) {
        String proceso = completedProcesses.get(i);
        
        if (processArrivalTime.containsKey(proceso) && 
            processStartTime.containsKey(proceso)) {
            
            //Cálculo simple y correcto
            int arrival = processArrivalTime.get(proceso);
            int start = processStartTime.get(proceso);     
            int E = start - arrival;  // Espera = Inicio - Llegada
            
            totalEspera += E;
            contadorProcesos++;
            
            //Escribir en la tabla
            if (i < tableWaitingTime.getRowCount()) {
                tableWaitingTime.setValueAt(proceso, i, 0);
                tableWaitingTime.setValueAt(E, i, 1);
            }
        }
    }
    
    //Calcular y mostrar la media
    if (contadorProcesos > 0) {
        double media = totalEspera / contadorProcesos;
        
        if (contadorProcesos < tableWaitingTime.getRowCount()) {
            tableWaitingTime.setValueAt("Media", contadorProcesos, 0);
            tableWaitingTime.setValueAt(String.format("%.2f", media), contadorProcesos, 1);
        }
    }
}
    
    //Tiempo de finalizacion
    private void updateCompletionTimeTable() {
        // 1. Limpiar la tabla (Borramos las 18 filas para repintar)
        for (int i = 0; i < tableCompletionTime.getRowCount(); i++) {
            tableCompletionTime.setValueAt(null, i, 0);
            tableCompletionTime.setValueAt(null, i, 1);
        }
        
        double totalFinalizacion = 0;
        int contadorProcesos = 0;
    
       // 2. Recorrer TODOS los procesos completados
        for (int i = 0; i < completedProcesses.size(); i++) {
            String proceso = completedProcesses.get(i);
        
            if (processArrivalTime.containsKey(proceso) && 
                processStartTime.containsKey(proceso) && 
                processDuration.containsKey(proceso)) {
            
                // --- CÁLCULOS ---
                int arrival = processArrivalTime.get(proceso);
                int start = processStartTime.get(proceso);
                int duration = processDuration.get(proceso); // t
            
                int E = start - arrival; // Tiempo de espera
                int F = duration + E;    // F = t + E
            
                // Sumar para el promedio
                totalFinalizacion += F;
                contadorProcesos++;
            
                // Escribir en la tabla (Fila i)
                // Aseguramos no salirnos del rango de la tabla
                if (i < tableCompletionTime.getRowCount()) {
                    tableCompletionTime.setValueAt(proceso, i, 0);
                    tableCompletionTime.setValueAt(F, i, 1);
                }
            }
        }
        
        // 3. Calcular y mostrar la Media
        if (contadorProcesos > 0) {
            double media = totalFinalizacion / contadorProcesos;
        
            // Colocamos la media en la fila siguiente al último proceso
            // Verificamos que no nos salgamos del límite de la tabla
            if (contadorProcesos < tableCompletionTime.getRowCount()) {
                tableCompletionTime.setValueAt("Media", contadorProcesos, 0);
                tableCompletionTime.setValueAt(String.format("%.2f", media), contadorProcesos, 1);
            }
        }
    }
    
    private void initPenaltyTable() {
        String[] columns = new String[2];
        columns[0] = "Proceso";
        columns[1] = "Penalización (P)";

        Object[][] data = new Object[6][2];
        tablePenalty = new DefaultTableModel(data, columns);
        jTable2.setModel(tablePenalty);
    }

    private void updatePenaltyTable() {

        // Limpiar tabla
        for (int i = 0; i < tablePenalty.getRowCount(); i++) {
            tablePenalty.setValueAt(null, i, 0);
            tablePenalty.setValueAt(null, i, 1);
        }

        double totalP = 0;
        int count = 0;

        for (int i = 0; i < completedProcesses.size(); i++) {

            String proceso = completedProcesses.get(i);

            if (processArrivalTime.containsKey(proceso) &&
                processStartTime.containsKey(proceso) &&
                processDuration.containsKey(proceso)) {

                int arrival = processArrivalTime.get(proceso);
                int start = processStartTime.get(proceso);
                int t = processDuration.get(proceso);

                int E = start - arrival;   // Tiempo de espera
                int F = t + E;             // Finalización
                double P = (double) F / t; // Penalización

                totalP += P;
                count++;

                if (i < tablePenalty.getRowCount()) {
                    tablePenalty.setValueAt(proceso, i, 0);
                    tablePenalty.setValueAt(String.format("%.2f", P), i, 1);
                }
            }
        }

        // MEDIA
        if (count > 0) {
            double media = totalP / count;

            if (count < tablePenalty.getRowCount()) {
                tablePenalty.setValueAt("Media", count, 0);
                tablePenalty.setValueAt(String.format("%.2f", media), count, 1);
            }
        }
    }

    
    //Clase para el manejo de la recepcion de procesos
    public static class ProcessHandler {
        
       
        private static ServerGUI gui;

        public static void setGUI(ServerGUI instance) {
            gui = instance;
        }
        
          public String ping() {
            return "OK";
                }
        
        public String sendProcess(String name, int startTime, int duration) {
            if (startTime + duration > 10){
                return "Proceso excede las capacidades del planificador";
            }
            int serverTime = gui.currentTime;
            int initTime = serverTime + startTime;
            int endTime = initTime + duration -1;
            gui.processArrivalTime.put(name, serverTime);
            gui.processDuration.put(name, duration);
            
            gui.messageLabel.setText("Proceso recibido (" + serverTime + "): " + name + " inicia en: " + initTime
            + " termina en: " + endTime);
            System.out.println("Proceso recibido: " + name +
                    " | Llega en " + serverTime +
                    " | Inicia en " + initTime +
                    " | Termina en " + endTime);
            
            //check if process can be in planificator
            if (gui.checkProcess(name, startTime, duration)){
                gui.desconectionInactivityTime = 5;
                gui.processStartTime.put(name, initTime);
                gui.updateProcessTable(name, startTime, duration);
                
            
                SwingUtilities.invokeLater(() -> {
                    int index = gui.processNames.indexOf(name);
                    //Draw proceess if thre resource is avalible
                    gui.tablePlanificator.setValueAt("x", index, 1);
                    System.out.println("START TIME INDEX: " +startTime);
                    for (int i=2+startTime; i<startTime+2+duration; i++ ){
                        gui.tablePlanificator.setValueAt("o", index, i);
                    }
                });
                gui.calculateWaitingTime(name);
                return "Proceso " + name + " recibido. Inicia en " + initTime + ", termina en " + endTime;
            }//Check if queue of process if avalible to insert
            else{
                if (! gui.isQueueTableFull()){
                    gui.pushProcessQueueTable(name, startTime, duration);
                    return "Proceso " + name + " entro en la cola de espera del planificador";
                }
            }
            return "Proceso " + name + " no pudo ser despachado por el palnificador porque no hay recurso";
        }
    }
    
    //Inicializadores de tabla-----------------------------------------------------------------
    private void initProcessTable(){
        String[] columns = new String[3];
        columns[0] = "Proceso";
        columns[1] = "C";
        columns[2] = "T";
        Object[][] data = new Object[processNames.size()][3];
        tableProcess = new DefaultTableModel(data, columns);
        jTable3.setModel(tableProcess);
        for (int i=0; i<processNames.size(); i++){
           tableProcess.setValueAt(processNames.get(i), i, 0);
        }
    }
    
    private void initQueueTable(){
        String[] columns = new String[4];
        columns[0] = "I";
        columns[1] = "Proceso";
        columns[2] = "C";
        columns[3] = "T";
        Object[][] data = new Object[7][4];
        for (int i=0; i<7; i++){
            data[i][0] = i+1;
        }
        tableQueue = new DefaultTableModel(data, columns);
        jTable4.setModel(tableQueue);
    }
    
    
    private void initWaitingTimeTable() {
    String[] columns = new String[2];
    columns[0] = "Proceso";
    columns[1] = "T. Espera";
    Object[][] data = new Object[6][2]; 
    tableWaitingTime = new DefaultTableModel(data, columns);
    jTable5.setModel(tableWaitingTime);
}
    
    private void initCompletionTimeTable() {
    String[] columns = new String[2];
    columns[0] = "Proceso";
    columns[1] = "Tiempo de finalización"; // <--- Etiqueta corregida
    
    // Inicializamos con 6 filas vacías
    Object[][] data = new Object[6][2]; 
    tableCompletionTime = new DefaultTableModel(data, columns);
    jTable6.setModel(tableCompletionTime);
    }
    
    private void initPlanificatorTable() {
        
        planificatorHeaders[0] = "P";
        for (int i = 1; i < 11; i++) {
            planificatorHeaders[i] = String.valueOf(i-1);
        }

        Object[][] data = new Object[5][11];
        tablePlanificator = new DefaultTableModel(data, planificatorHeaders);
        jTable1.setModel(tablePlanificator);
        
        for (int i=0; i<processNames.size(); i++){
            tablePlanificator.setValueAt(processNames.get(i), i, 0);
        }
    }
    
    //Actalizadores de tablas-----------------------------------------------------------------
    private void updateProcessTable(String name, int initTime, int durationTime){
        int index = processNames.indexOf(name);
        tableProcess.setValueAt(initTime, index, 1);
        tableProcess.setValueAt(durationTime, index, 2);
    }
    
    //Incerta un proceso en la ultima posicion disponible de la tabla de cola
    private void pushProcessQueueTable(String name, int initTime, int durationTime){
        for(int i =0; i<7; i++){
            if (tableQueue.getValueAt(i, 1) == null ){
                tableQueue.setValueAt(name, i, 1);
                tableQueue.setValueAt(initTime, i, 2);
                tableQueue.setValueAt(durationTime, i, 3);
                break;
            }
        }
    }
    
    private void popProcessQueueTable() {
        // Move all elents up
        for (int i = 0; i < 6; i++) {
            // Mover datos de la fila i+1 a la fila i
            tableQueue.setValueAt(tableQueue.getValueAt(i + 1, 1), i, 1);
            tableQueue.setValueAt(tableQueue.getValueAt(i + 1, 2), i, 2);
            tableQueue.setValueAt(tableQueue.getValueAt(i + 1, 3), i, 3);
        }

        // clean last row
        tableQueue.setValueAt(null, 6, 1);
        tableQueue.setValueAt(null, 6, 2);
        tableQueue.setValueAt(null, 6, 3);
    }
    
    private boolean isQueueTableFull(){
        if (tableQueue.getValueAt(6, 1) == null ){
            return false;
        }
        return true;
    }
    
    private boolean isQueueTableEmpy(){
        if (tableQueue.getValueAt(0, 1) == null ){
            return true;
        }
        return false;
    }
    
    private boolean isPlanificatorTableEmpy(){
        Object[][] tableData = getPlanificatorTableData();
        for (int row = 0; row < tableData.length; row++) {
            for (int col = 1; col < tableData[row].length; col++) {
                if (tableData[row][col] != null){
                    return false;
                }
            }
        }
        return true;
    }
    
    private void updatePlanificatorTable(){
        planificatorHeaders[0] = "P";
        for (int i = 1; i < 11; i++) {
            planificatorHeaders[i] = String.valueOf((currentTime + i-1));
        }
        Object[][] tableData = getPlanificatorTableData();
        for (int row = 0; row < tableData.length; row++) {
            for (int col = 1; col < tableData[row].length; col++) {
            //Eschange the data in the next column
                if (col == tableData[row].length-1){
                    tableData[row][col] = null;
                }else{
                   tableData[row][col] = tableData[row][col+1];
                }
            }
        }
        
        tablePlanificator = new DefaultTableModel(tableData,  planificatorHeaders);
        jTable1.setModel(tablePlanificator);
        printTableData(tableData);
        
    }
    
    //Utilities para tablas
    public void printTableData(Object[][] tableData) {
        if (tableData == null) {
            System.out.println("Los datos de la tabla son nulos");
            return;
        }

        System.out.println("=== CONTENIDO DE LA TABLA DEL PLANIFICADOR "+currentTime+" ===");
        System.out.print("HEADERS: \t");
        for (int i = 1; i < 11; i++) {
                System.out.print(currentTime + i-1 + "  \t");
            }
        System.out.println("");
        for (int row = 0; row < tableData.length; row++) {
            System.out.print("Fila " + row + ": ");
            for (int col = 0; col < tableData[row].length; col++) {
                Object value = tableData[row][col];
                System.out.print((value != null ? value.toString() : "null") + "\t");
            }
            System.out.println();
        }
        System.out.println("=============================================");
    }
    
    public Object[][] getPlanificatorTableData() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        int rowCount = model.getRowCount();
        int colCount = model.getColumnCount();

        Object[][] tableData = new Object[rowCount][colCount];

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                tableData[row][col] = model.getValueAt(row, col);
            }
        }

        return tableData;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        initServerButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable5 = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable6 = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        messageLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));

        jLabel2.setFont(new java.awt.Font("MS Gothic", 0, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Planificador FIFO");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(373, 373, 373)
                .addComponent(jLabel2)
                .addContainerGap(385, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        initServerButton.setBackground(new java.awt.Color(0, 0, 0));
        initServerButton.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N
        initServerButton.setForeground(new java.awt.Color(255, 255, 255));
        initServerButton.setText("Iniciar Servidor");
        initServerButton.setBorder(null);
        initServerButton.setBorderPainted(false);
        initServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                initServerButtonActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setEnabled(false);
        jTable1.setRequestFocusEnabled(false);
        jTable1.setRowHeight(25);
        jTable1.setSelectionBackground(new java.awt.Color(255, 255, 255));
        jTable1.setShowGrid(true);
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N
        jLabel1.setText("Planificador");

        jLabel3.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N
        jLabel3.setText("Procesos");

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable3.setRowHeight(25);
        jScrollPane3.setViewportView(jTable3);

        jLabel4.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N
        jLabel4.setText("Cola de espera");

        jLabel5.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N
        jLabel5.setText("Tiempo de espera");

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable4.setRowHeight(24);
        jScrollPane4.setViewportView(jTable4);

        jTable5.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane5.setViewportView(jTable5);

        jTable6.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane6.setViewportView(jTable6);

        jLabel6.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N
        jLabel6.setText("Penalización");

        jLabel7.setFont(new java.awt.Font("MS Gothic", 0, 12)); // NOI18N
        jLabel7.setText("Tiempo de finalización");

        messageLabel.setFont(new java.awt.Font("MS Gothic", 0, 14)); // NOI18N
        messageLabel.setText("MENSAJE");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(messageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 585, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(153, 153, 153)
                        .addComponent(initServerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5))
                                .addGap(56, 56, 56)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7))
                                .addGap(56, 56, 56)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 671, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(initServerButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messageLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        
    
    //Buttons actions
    private void initServerButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        // TODO add your handling code here:
        initServerButton.setEnabled(false); // Desactivar
        startServer();
    }                                                
    
    // Variables declaration - do not modify                     
    private javax.swing.JButton initServerButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTable jTable5;
    private javax.swing.JTable jTable6;
    private javax.swing.JLabel messageLabel;
    // End of variables declaration                   
}
