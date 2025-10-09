import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;
import javax.swing.SwingWorker;



public class Contador_Cliente extends javax.swing.JFrame {
    
     private XmlRpcClient client;
    private boolean intentandoReconectar = false;
     private String operacionPendienteId = null; // Para recuperación
    private Thread hiloOperacionActual = null; // Para poder interrumpir
    
    private String nombreCliente;
     private boolean corriendo = false;
    private XmlRpcClient[] clientes = new XmlRpcClient[4];
    private SwingWorker<?, ?>[] workers = new SwingWorker[4];
    
    public Contador_Cliente() {
        initComponents();
    }
  
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        TextCliente = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        Conectar = new javax.swing.JButton();
        EtiquetaEstado = new javax.swing.JLabel();
        Detener = new javax.swing.JButton();
        Nombre = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        TextCliente.setColumns(20);
        TextCliente.setRows(5);
        jScrollPane2.setViewportView(TextCliente);

        jLabel2.setText("Cliente");

        Conectar.setText("Conectar");
        Conectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConectarActionPerformed(evt);
            }
        });

        EtiquetaEstado.setText("Desconectado");

        Detener.setText("Desconectar");
        Detener.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DetenerActionPerformed(evt);
            }
        });

        Nombre.setText("Nombre...");
        Nombre.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NombreActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(Conectar)
                        .addGap(18, 18, 18)
                        .addComponent(Nombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(32, 32, 32)
                        .addComponent(Detener))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(EtiquetaEstado)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(EtiquetaEstado)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Conectar)
                            .addComponent(Nombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2))
                    .addComponent(Detener))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ConectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConectarActionPerformed
        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://localhost:8080"));
            config.setConnectionTimeout(3000);

            client = new XmlRpcClient();
            client.setConfig(config);

            nombreCliente = Nombre.getText(); // puedes cambiar esto según tu interfaz
            corriendo = true;

            EtiquetaEstado.setText("Conectado");
            EtiquetaEstado.setForeground(new java.awt.Color(0, 153, 0));
            TextCliente.append("Conectado al servidor\n");

            // Lanzamos las peticiones en un hilo aparte
            iniciarEnvioContinuo();

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "No se pudo conectar al servidor",
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            TextCliente.append("Error de conexión\n");
        }
    }//GEN-LAST:event_ConectarActionPerformed

    private void DetenerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DetenerActionPerformed
        corriendo = false;
        TextCliente.append("Desconectando cliente...\n");        // TODO add your handling code here:
    }//GEN-LAST:event_DetenerActionPerformed

    private void NombreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NombreActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NombreActionPerformed

    private void iniciarEnvioContinuo() {
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            
            private int contador = 0;
            
            @Override
            protected Void doInBackground() {
                try {
                    while (corriendo) {
                        Object[] params = new Object[]{nombreCliente};
                        String respuesta = (String) client.execute("Mensajes.recibir", params);
                        if (!respuesta.contains("Ocupado")){
                        
                            contador++;

                            publish(respuesta);

                        }

                        if (respuesta.contains("Servidor cerrado")) {
                            corriendo = false;
                            break;
                        }

                    }
                } catch (Exception e) {
                    publish("Error de conexión: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> mensajes) {
                for (String msg : mensajes) {
                    TextCliente.append(contador +" - "+ msg + "\n");
                }
            }

            @Override
            protected void done() {
                TextCliente.append("Cliente detenido.\n");
                EtiquetaEstado.setText("Desconectado");
                EtiquetaEstado.setForeground(java.awt.Color.RED);
            }
        };

        worker.execute();
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
       
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Contador_Cliente().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Conectar;
    private javax.swing.JButton Detener;
    private javax.swing.JLabel EtiquetaEstado;
    private javax.swing.JTextField Nombre;
    private javax.swing.JTextArea TextCliente;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables
}
