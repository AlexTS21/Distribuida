import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;

public class clientevista extends javax.swing.JFrame {

     private XmlRpcClient client;
    private boolean intentandoReconectar = false;
     private String operacionPendienteId = null; // Para recuperación
    private Thread hiloOperacionActual = null; // Para poder interrumpir
    public clientevista() {
        initComponents();
        habilitarOperaciones(false);
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Calculadora Cliente");

        jButton1.setText("Conectar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Desconectar");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel2.setText("Estado");

        jLabel4.setText("Numero 1:");

        jLabel5.setText("Numero 2:");

        jButton3.setText("Suma");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Resta");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Multiplicacion");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setText("Divicion");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jLabel7.setText("jLabel7");

        jButton7.setText("Limpiar");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton9.setText("jButton9");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jButton1)
                                    .addGap(18, 18, 18)
                                    .addComponent(jButton2))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(18, 18, 18)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(6, 6, 6)
                                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(100, 100, 100)
                                    .addComponent(jButton9))))
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(jButton3)
                            .addGap(18, 18, 18)
                            .addComponent(jButton4)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jButton5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jButton6)
                            .addGap(0, 0, Short.MAX_VALUE))))
                .addContainerGap(30, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addGap(130, 130, 130))
            .addGroup(layout.createSequentialGroup()
                .addGap(141, 141, 141)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(jButton7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9))
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton3)
                    .addComponent(jButton4)
                    .addComponent(jButton5)
                    .addComponent(jButton6))
                .addGap(35, 35, 35)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
      try {
        int num1 = Integer.parseInt(jTextField1.getText());
        int num2 = Integer.parseInt(jTextField2.getText());

        habilitarOperaciones(false);
        jTextArea1.append("Enviando petición de suma: " + num1 + " + " + num2 + "\n");
        jLabel7.setText("Procesando...");
        jLabel7.setForeground(java.awt.Color.ORANGE);

        final String operationId = "SUM_" + System.currentTimeMillis();
        operacionPendienteId = operationId;

        // Guardar el hilo actual para poder interrumpirlo al desconectar
        hiloOperacionActual = new Thread(() -> {
            try {
                XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
                config.setServerURL(new URL("http://localhost:8080/"));
                config.setConnectionTimeout(3000);
                config.setReplyTimeout(10000);

                XmlRpcClient clienteTemporal = new XmlRpcClient();
                clienteTemporal.setConfig(config);

                // Usar el nuevo método con ID de operación
                Object[] params = new Object[]{num1, num2, operationId};
                String respuesta = (String) clienteTemporal.execute("MiServidorRPC_Suma.sumarConId", params);

                // Verificar si el hilo fue interrumpido (desconexión)
                if (Thread.currentThread().isInterrupted()) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        jTextArea1.append("DESCONECTADO DURANTE LA OPERACIÓN\n");
                        jTextArea1.append("El servidor calculó " + num1 + "+" + num2 + " pero no recibimos el resultado\n");
                    });
                    return;
                }

                // Si no fue interrumpido, procesar respuesta normal
                if (respuesta.startsWith("EXITO_")) {
                    int resultado = Integer.parseInt(respuesta.substring(6));
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        jTextArea1.append("Respuesta recibida: " + num1 + " + " + num2 + " = " + resultado + "\n");
                        jLabel7.setText("Conectado");
                        jLabel7.setForeground(new java.awt.Color(0, 153, 0));
                        habilitarOperaciones(true);
                        operacionPendienteId = null; // Limpiar
                    });
                }

            } catch (org.apache.xmlrpc.client.XmlRpcClientException ex) {
                if (!Thread.currentThread().isInterrupted()) { // Solo si no fue desconexión manual
                    if (ex.getMessage().contains("Timeout")) {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            jTextArea1.append("Tiempo acabado - No se recibió respuesta\n");
                            jTextArea1.append("Pero el servidor pudo haber calculado el resultado\n");
                            jLabel7.setText("Timeout");
                            jLabel7.setForeground(java.awt.Color.ORANGE);
                        });
                    } else {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            jTextArea1.append("Error: " + ex.getMessage() + "\n");
                            jLabel7.setText("Error");
                            jLabel7.setForeground(java.awt.Color.RED);
                        });
                    }
                    habilitarOperaciones(true);
                }
                
            } catch (Exception ex) {
                if (!Thread.currentThread().isInterrupted()) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        jTextArea1.append("Error inesperado: " + ex.getMessage() + "\n");
                        jLabel7.setText("Error");
                        jLabel7.setForeground(java.awt.Color.RED);
                        habilitarOperaciones(true);
                    });
                }
            }
        });
        
        hiloOperacionActual.start();

    } catch (NumberFormatException e) {
        jTextArea1.append("Error: Ingrese números válidos\n");
        habilitarOperaciones(true);
    } 
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://localhost:8080/"));
            config.setConnectionTimeout(3000);

            client = new XmlRpcClient();
            client.setConfig(config);

            // Enviar mensaje de conexión
            Object[] params = new Object[]{"Cliente  2"};
            client.execute("Mensajes.recibir", params);
            
            jLabel7.setText("Conectado");
            jLabel7.setForeground(new java.awt.Color(0, 153, 0));
            habilitarOperaciones(true);
            jTextArea1.append("Conectado al servidor\n");
            
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, 
                "No se pudo conectar al servidor", 
                "Error", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
            jTextArea1.append(" Error de conexión\n");
        }
    
                        
    
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
          // Interrumpir operación en curso antes de enviarla
    if (hiloOperacionActual != null && hiloOperacionActual.isAlive()) {
        hiloOperacionActual.interrupt();
        jTextArea1.append("Interrumpiendo operación en curso...\n");
        jTextArea1.append("El servidor puede haber calculado el resultado\n");
        jTextArea1.append("Usa 'Limpiar/Recuperar' para obtener el resultado perdido\n");
    }
    
    intentandoReconectar = false; 
    try {
        if (client != null) {
            Object[] params = new Object[]{"Cliente desconectado"};
            client.execute("Mensajes.recibir", params);
        }
    } catch (Exception e) {
        // Ignoraramos losa errores
    }
    
    client = null;
    jLabel7.setText("Desconectado");
    jLabel7.setForeground(java.awt.Color.RED);
    habilitarOperaciones(false);
    jTextArea1.append("Desconectado del servidor\n");
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
           try {
        int num1 = Integer.parseInt(jTextField1.getText());
        int num2 = Integer.parseInt(jTextField2.getText());

        // Deshabilitar los botones mientras se procesa
        habilitarOperaciones(false);
        jTextArea1.append("Enviando petición de resta al servidor...\n");
        jLabel7.setText("Enviando...");
        jLabel7.setForeground(java.awt.Color.ORANGE);

        // Ejecutar la llamada en un hilo separado
        new Thread(() -> {
            try {
                // Configuración con tiempo 
                XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
                config.setServerURL(new URL("http://localhost:8080/"));
                config.setConnectionTimeout(3000);   // tiempo para conectar
                config.setReplyTimeout(15000);       

                XmlRpcClient clienteTemporal = new XmlRpcClient();
                clienteTemporal.setConfig(config);

                Object[] params = new Object[]{num1, num2};
                Integer resultado = (Integer) clienteTemporal.execute("MiServidorRPC_Resta.resta", params);

                 // klegamos aqui si se pudo reconectar el servidor 
                javax.swing.SwingUtilities.invokeLater(() -> {
                    jTextArea1.append("Resta: " + num1 + " - " + num2 + " = " + resultado + "\n");
                    jLabel7.setText("Conectado");
                    jLabel7.setForeground(new java.awt.Color(0, 153, 0));
                    habilitarOperaciones(true);
                });
            } catch (Exception ex) {
                
                
                
                 // Aquí capturamos la excepción cuando el servidor se apaga
                javax.swing.SwingUtilities.invokeLater(() -> {
                    jTextArea1.append("Error: fallo de comunicación con el servidor: " 
                            + ex.getClass().getSimpleName() + " - " + ex.getMessage() + "\n");
                    jTextArea1.append("No se encontró el servidor o se interrumpió la conexión.\n");
                    jLabel7.setText("Desconectado");
                    jLabel7.setForeground(java.awt.Color.RED);
                    habilitarOperaciones(false);
                    intentarReconexionAutomatica();
                });
            }
        }).start();

    } catch (NumberFormatException e) {
        jTextArea1.append("Error: Ingrese números válidos\n");
        habilitarOperaciones(true);
    }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
           try {
        int num1 = Integer.parseInt(jTextField1.getText());
        int num2 = Integer.parseInt(jTextField2.getText());

        // Deshabilitar botones mientras se procesa
        habilitarOperaciones(false);
        jTextArea1.append("Enviando petición de multiplicación al servidor...\n");
        jLabel7.setText("Enviando...");
        jLabel7.setForeground(java.awt.Color.ORANGE);

        // Ejecutar la llamada RPC en un hilo separado
        new Thread(() -> {
            try {
                XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
                config.setServerURL(new URL("http://localhost:8080/"));
                config.setConnectionTimeout(3000);
                config.setReplyTimeout(15000);

                XmlRpcClient clienteTemporal = new XmlRpcClient();
                clienteTemporal.setConfig(config);

                Object[] params = new Object[]{num1, num2};
                Integer resultado = (Integer) clienteTemporal.execute("MiServidorRPC_Multiplicacion.multiplicacion", params);

                javax.swing.SwingUtilities.invokeLater(() -> {
                    jTextArea1.append("Multiplicación: " + num1 + " * " + num2 + " = " + resultado + "\n");
                    jLabel7.setText("Conectado");
                    jLabel7.setForeground(new java.awt.Color(0, 153, 0));
                    habilitarOperaciones(true);
                });
            } catch (Exception ex) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    jTextArea1.append("Error: fallo de comunicación con el servidor: " 
                            + ex.getClass().getSimpleName() + " - " + ex.getMessage() + "\n");
                    jTextArea1.append("No se encontró el servidor o se interrumpió la conexión.\n");
                    jLabel7.setText("Desconectado");
                    jLabel7.setForeground(java.awt.Color.RED);
                    habilitarOperaciones(false);
                    intentarReconexionAutomatica();
                });
            }
        }).start();

    } catch (NumberFormatException e) {
        jTextArea1.append("Error: Ingrese números válidos\n");
        habilitarOperaciones(true);
    }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
      try {
        int num1 = Integer.parseInt(jTextField1.getText());
        int num2 = Integer.parseInt(jTextField2.getText());

        // Validar división entre 0
        if (num2 == 0) {
            jTextArea1.append("Error: No se puede dividir entre cero.\n");
            return;
        }

        // Deshabilitar botones mientras se procesa
        habilitarOperaciones(false);
        jTextArea1.append("Enviando petición de división al servidor...\n");
        jLabel7.setText("Enviando...");
        jLabel7.setForeground(java.awt.Color.ORANGE);

        new Thread(() -> {
            try {
                XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
                config.setServerURL(new URL("http://localhost:8080/"));
                config.setConnectionTimeout(3000);
                config.setReplyTimeout(15000);

                XmlRpcClient clienteTemporal = new XmlRpcClient();
                clienteTemporal.setConfig(config);

                Object[] params = new Object[]{num1, num2};
                Double resultado = (Double) clienteTemporal.execute("MiServidorRPC_Divicion.divicion", params);

                javax.swing.SwingUtilities.invokeLater(() -> {
                    jTextArea1.append("División: " + num1 + " / " + num2 + " = " + resultado + "\n");
                    jLabel7.setText("Conectado");
                    jLabel7.setForeground(new java.awt.Color(0, 153, 0));
                    habilitarOperaciones(true);
                });
            } catch (Exception ex) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    jTextArea1.append("Error: fallo de comunicación con el servidor: " 
                            + ex.getClass().getSimpleName() + " - " + ex.getMessage() + "\n");
                    jTextArea1.append("No se encontró el servidor o se interrumpió la conexión.\n");
                    jLabel7.setText("Desconectado");
                    jLabel7.setForeground(java.awt.Color.RED);
                    habilitarOperaciones(false);
                    intentarReconexionAutomatica();
                });
            }
        }).start();

    } catch (NumberFormatException e) {
        jTextArea1.append("Error: Ingrese números válidos\n");
        habilitarOperaciones(true);
    }
    
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
 // Si hay operación pendiente las recuperar. Sino, limpiar campos.
    if (operacionPendienteId != null) {
        recuperarResultadoPerdido();
    } else {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField1.requestFocus();
        jTextArea1.append("Campos limpiados\n");
    }
}

private void recuperarResultadoPerdido() {
    if (operacionPendienteId == null) {
        jTextArea1.append("No hay operaciones pendientes por recuperar\n");
        return;
    }
    
    jTextArea1.append("\nIntentando recuperar resultado perdido...\n");
    
    new Thread(() -> {
        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://localhost:8080/"));
            config.setConnectionTimeout(3000);
            config.setReplyTimeout(5000);

            XmlRpcClient clienteTemporal = new XmlRpcClient();
            clienteTemporal.setConfig(config);

            Object[] params = new Object[]{operacionPendienteId};
            String resultadoRecuperado = (String) clienteTemporal.execute("MiServidorRPC_Suma.obtenerResultadoNoEnviado", params);
            
            if (resultadoRecuperado.startsWith("RECUPERADO_")) {
                int resultado = Integer.parseInt(resultadoRecuperado.substring(11));
                javax.swing.SwingUtilities.invokeLater(() -> {
                    jTextArea1.append("RESULTADO RECUPERADO = " + resultado + "\n");
                    jTextArea1.append("El servidor tenía el resultado guardado\n");
                    jLabel7.setText("Conectado");
                    jLabel7.setForeground(new java.awt.Color(0, 153, 0));
                    operacionPendienteId = null; // Limpiar
                    habilitarOperaciones(true);
                });
            } else {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    jTextArea1.append("No se pudo recuperar el resultado\n");
                    operacionPendienteId = null;
                    habilitarOperaciones(true);
                });
            }
            
        } catch (Exception e) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                jTextArea1.append("Error al recuperar: " + e.getMessage() + "\n");
                habilitarOperaciones(true);
            });
        }
    }).start();

    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton9ActionPerformed

    
    // Habilitar/deshabilitar operaciones
    private void habilitarOperaciones(boolean habilitar) {
        jButton3.setEnabled(habilitar);
        jButton4.setEnabled(habilitar);
        jButton5.setEnabled(habilitar);
        jButton6.setEnabled(habilitar);
        
        jButton1.setEnabled(!habilitar);
    }

    
    private void intentarReconexionAutomatica() {
    if (intentandoReconectar) return;
    
    intentandoReconectar = true;
    jLabel7.setText("Reconectando...");
    jLabel7.setForeground(java.awt.Color.ORANGE);
    
    new Thread(() -> {
        while (intentandoReconectar) {
            try {
                Thread.sleep(3000);
                
                XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
                config.setServerURL(new URL("http://localhost:8080/"));
                config.setConnectionTimeout(2000);
                
                XmlRpcClient nuevoClient = new XmlRpcClient();
                nuevoClient.setConfig(config);
                
                Object[] params = new Object[]{"Cliente reconectado"};
                nuevoClient.execute("Mensajes.recibir", params);
                
                client = nuevoClient;
                intentandoReconectar = false;
                
                javax.swing.SwingUtilities.invokeLater(() -> {
                    jLabel7.setText("Conectado");
                    jLabel7.setForeground(new java.awt.Color(0, 153, 0));
                    jTextArea1.append("Reconexión exitosa\n");
                    habilitarOperaciones(true);
                });
                break;
                
            } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    jTextArea1.append("Servidor aún no disponible reintentando...\n");
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
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
