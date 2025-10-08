import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;
import javax.swing.SwingWorker;


public class clientevista extends javax.swing.JFrame {

     private XmlRpcClient client;
    private boolean intentandoReconectar = false;
     private String operacionPendienteId = null; // Para recuperación
    private Thread hiloOperacionActual = null; // Para poder interrumpir
    
    private String nombreCliente;
     private boolean corriendo = false;
    private XmlRpcClient[] clientes = new XmlRpcClient[4];
    private SwingWorker<?, ?>[] workers = new SwingWorker[4];
    
    public clientevista() {
        initComponents();
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        Conectar = new javax.swing.JButton();
        Desconectar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TextCliente3 = new javax.swing.JTextArea();
        EtiquetaEstado = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        TextServidor1 = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        TextCliente1 = new javax.swing.JTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        TextCliente2 = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        TextCliente4 = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Contador del 1- 100");

        Conectar.setText("Conectar");
        Conectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConectarActionPerformed(evt);
            }
        });

        Desconectar.setText("Desconectar");
        Desconectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DesconectarActionPerformed(evt);
            }
        });

        jLabel2.setText("Estado");

        jLabel5.setText("Servidor:");

        TextCliente3.setColumns(20);
        TextCliente3.setRows(5);
        jScrollPane1.setViewportView(TextCliente3);

        EtiquetaEstado.setText("Desconectado");

        jLabel8.setText("Cliente 2:");

        jLabel9.setText("Cliente 1:");

        jLabel10.setText("Cliente 3:");

        jLabel11.setText("Cliente 4:");

        TextServidor1.setColumns(20);
        TextServidor1.setRows(5);
        jScrollPane2.setViewportView(TextServidor1);

        TextCliente1.setColumns(20);
        TextCliente1.setRows(5);
        jScrollPane3.setViewportView(TextCliente1);

        TextCliente2.setColumns(20);
        TextCliente2.setRows(5);
        jScrollPane4.setViewportView(TextCliente2);

        TextCliente4.setColumns(20);
        TextCliente4.setRows(5);
        jScrollPane5.setViewportView(TextCliente4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(Conectar)
                        .addGap(18, 18, 18)
                        .addComponent(Desconectar)
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addGap(6, 6, 6)
                                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(EtiquetaEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(11, 11, 11)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(76, 76, 76))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Conectar)
                    .addComponent(Desconectar)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6)
                    .addComponent(EtiquetaEstado))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(88, 88, 88)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(96, 96, 96)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane1)
                            .addComponent(jScrollPane3)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void iniciarCliente(int indice, String nombre, javax.swing.JTextArea areaTexto) {
        workers[indice] = new SwingWorker<Void, String>() {
            
            private int contador = 0;
            
            @Override
            protected Void doInBackground() {
                try {
                    while (corriendo) {
                        Object[] params = new Object[]{nombre};
                        String respuesta = (String) clientes[indice].execute("Mensajes.recibir", params);

                        contador++;
                        
                        publish(respuesta);

                        if (respuesta.contains("Servidor cerrado")) {
                            corriendo = false;
                            break;
                        }

                        //Thread.sleep(500 + (int) (Math.random() * 1000));
                    }
                } catch (Exception e) {
                    publish("Error: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> mensajes) {
                for (String msg : mensajes) {
                    areaTexto.append(contador + " - " +msg + "\n");
                    TextServidor1.append("Server - "+msg + "\n");
                    
                }
            }

            @Override
            protected void done() {
                areaTexto.append("Cliente detenido.\n");
            }
        };
        workers[indice].execute();
    }
    
    private void ConectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConectarActionPerformed
    try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://localhost:8081"));
            config.setConnectionTimeout(3000);

            // Crear 4 clientes independientes
            for (int i = 0; i < 4; i++) {
                clientes[i] = new XmlRpcClient();
                clientes[i].setConfig(config);
            }

            corriendo = true;
            EtiquetaEstado.setText("Conectado");
            EtiquetaEstado.setForeground(new java.awt.Color(0, 153, 0));

            // Iniciar los 4 hilos de envío continuo
            iniciarCliente(0, "Cliente 1", TextCliente1);
            iniciarCliente(1, "Cliente 2", TextCliente2);
            iniciarCliente(2, "Cliente 3", TextCliente3);
            iniciarCliente(3, "Cliente 4", TextCliente4);

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No se pudo conectar al servidor",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            TextCliente1.append("Error de conexión\n");
            
            TextCliente2.append("Error de conexión\n");
            TextCliente3.append("Error de conexión\n");
            TextCliente4.append("Error de conexión\n");
        }
    
                        
    
    }//GEN-LAST:event_ConectarActionPerformed

    private void DesconectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DesconectarActionPerformed
        corriendo = false;
        TextServidor1.append("Clientes detenidos.\n");
    }//GEN-LAST:event_DesconectarActionPerformed

    
    // Habilitar/deshabilitar operaciones
    private void habilitarOperaciones(boolean habilitar) {
        Conectar.setEnabled(!habilitar);
    }

    
    private void intentarReconexionAutomatica() {
    if (intentandoReconectar) return;
    
    intentandoReconectar = true;
    EtiquetaEstado.setText("Reconectando...");
    EtiquetaEstado.setForeground(java.awt.Color.ORANGE);
    
    new Thread(() -> {
        while (intentandoReconectar) {
            try {
                Thread.sleep(3000);
                
                XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
                config.setServerURL(new URL("http://localhost:8081/"));
                config.setConnectionTimeout(2000);
                
                XmlRpcClient nuevoClient = new XmlRpcClient();
                nuevoClient.setConfig(config);
                
                Object[] params = new Object[]{"Cliente reconectado"};
                nuevoClient.execute("Mensajes.recibir", params);
                
                client = nuevoClient;
                intentandoReconectar = false;
                
                javax.swing.SwingUtilities.invokeLater(() -> {
                    EtiquetaEstado.setText("Conectado");
                    EtiquetaEstado.setForeground(new java.awt.Color(0, 153, 0));
                    TextCliente3.append("Reconexión exitosa\n");
                    habilitarOperaciones(true);
                });
                break;
                
            } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    TextCliente3.append("Servidor aún no disponible reintentando...\n");
                });
            }
        }
    }).start();
}
    
    
    
    
    public static void main(String args[]) {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new clientevista().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Conectar;
    private javax.swing.JButton Desconectar;
    private javax.swing.JLabel EtiquetaEstado;
    private javax.swing.JTextArea TextCliente1;
    private javax.swing.JTextArea TextCliente2;
    private javax.swing.JTextArea TextCliente3;
    private javax.swing.JTextArea TextCliente4;
    private javax.swing.JTextArea TextServidor1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    // End of variables declaration//GEN-END:variables
}
