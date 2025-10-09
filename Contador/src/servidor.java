import java.util.concurrent.locks.ReentrantLock;
import org.apache.xmlrpc.server.PropertyHandlerMapping; // Registra mapeos entre nombres de servicios (strings) y clases Java
import org.apache.xmlrpc.server.XmlRpcServer; // Clase principal que maneja la lógica del servidor RPC basado en XML
import org.apache.xmlrpc.webserver.WebServer; // Servidor web embebido simple que puede escuchar peticiones HTTP en un puerto específico
import java.util.*;
import java.util.ArrayList;

public class servidor {
    
     // Variables compartidas
    private static final ReentrantLock lock = new ReentrantLock(true); // 'true' => justo
    private static String ultimoCliente = "";
    private static int contadorPeticiones = 0;
    private static final int MAX_PETICIONES = 100;
    private static WebServer webServer;
    
    
    public static class Mensajes {
        private static final ReentrantLock lock = new ReentrantLock(true);
        private static List<String> clientesConectados = new ArrayList<>();
        private static Random random = new Random();
        private static String ultimoCliente = "";
        private static int contadorPeticiones = 0;
        private static final int MAX_PETICIONES = 100;

        public String recibir(String nombreCliente) {
            lock.lock();
            try {
                if (contadorPeticiones >= MAX_PETICIONES) {
                    System.out.println("Servidor alcanzó las 100 peticiones. Cerrando...");
                    new Thread(() -> detenerServidor()).start();
                    return "Servidor cerrado";
                }

                // Si el cliente no está registrado, lo agregamos a la lista
                if (!clientesConectados.contains(nombreCliente)) {
                    clientesConectados.add(nombreCliente);
                    System.out.println("Nuevo cliente registrado: " + nombreCliente);
                }

                // Elegir cliente aleatorio distinto del último
                String elegido;
                if (clientesConectados.size() < 2) {
                    System.out.print("Esperando clientes"+clientesConectados.size()+" clientes conectados \n");
                    try {
                        Thread.sleep(1000); // Simular tiempo de atención
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    // Hilo que detiene el servidor después de cierto tiempo
                    new Thread(() -> {
                        try {
                            Thread.sleep(20000);
                            System.out.println("⏰ Tiempo máximo alcanzado 2000 segundos).");
                            detenerServidor();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                    return "Esperando";// si hay solo uno, no hay más opción
                } else {
                    do {
                        elegido = clientesConectados.get(random.nextInt(clientesConectados.size()));
                    } while (elegido.equals(ultimoCliente)); // evitar repetir
                }

                
                // Si el que llamó no fue el elegido, se le rechaza
                if (!nombreCliente.equals(elegido)) {
                    System.out.println( "Cliente " + nombreCliente + " en espera..." );
                    try {
                        Thread.sleep(1000); // Simular tiempo de atención
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "Ocupado";
                }

                // Atender al cliente elegido
                contadorPeticiones++;
                ultimoCliente = elegido;

                System.out.println("[" + contadorPeticiones + "] Atendiendo al cliente: " + elegido);

                try {
                    Thread.sleep(1000); // Simular tiempo de atención
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                return "[" + contadorPeticiones + "] - " + elegido;
            } finally {
                lock.unlock();
            }
        }
    }
    public static void detenerServidor() {
        try {
            System.out.println("Deteniendo servidor...");
            webServer.shutdown();
            System.out.println("Servidor detenido correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        try {
            int puerto = 8080;
            System.out.println("Iniciando servidor en el puerto " + puerto + "...");

            webServer = new WebServer(puerto);
            XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
            PropertyHandlerMapping phm = new PropertyHandlerMapping();

            phm.addHandler("Mensajes", Mensajes.class);
            xmlRpcServer.setHandlerMapping(phm);
        
            // Iniciamos el servidor web, que comienza a escuchar y atender peticiones RPC
            webServer.start();

            System.out.println("Servidor XML-RPC iniciado correctamente.");
            System.out.println("Esperando llamadas RPC en: http://localhost:" + puerto + "/");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
