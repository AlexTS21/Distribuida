import java.util.concurrent.locks.ReentrantLock;
import org.apache.xmlrpc.server.PropertyHandlerMapping; // Registra mapeos entre nombres de servicios (strings) y clases Java
import org.apache.xmlrpc.server.XmlRpcServer; // Clase principal que maneja la lógica del servidor RPC basado en XML
import org.apache.xmlrpc.webserver.WebServer; // Servidor web embebido simple que puede escuchar peticiones HTTP en un puerto específico

public class servidor {
    
     // Variables compartidas
    private static final ReentrantLock lock = new ReentrantLock(true); // 'true' => justo
    private static String ultimoCliente = "";
    private static int contadorPeticiones = 0;
    private static final int MAX_PETICIONES = 100;
    private static WebServer webServer;
    
    public static class Mensajes {
        public String recibir(String mensaje) {
            lock.lock();
            try {
                if (contadorPeticiones >= MAX_PETICIONES) {
                    System.out.println("Servidor alcanzó las 100 peticiones. Cerrando...");
                    new Thread(() -> detenerServidor()).start();
                    return "Servidor cerrado";
                }

                // Verificar que no sea el mismo cliente que el anterior
                if (mensaje.equals(ultimoCliente)) {
                    return "Rechazado: el servidor no puede atender dos veces seguidas al mismo cliente (" + mensaje + ")";
                }

                // Aceptar al cliente
                contadorPeticiones++;
                ultimoCliente = mensaje;

                System.out.println("[" + contadorPeticiones + "] Atendiendo al cliente: " + mensaje);
                try {
                    Thread.sleep(500); // Simular tiempo de atención
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                return "[" + contadorPeticiones + "]"+ " - " + mensaje;
            } finally {
                lock.unlock();
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
    }
    public static void main(String[] args) {
        try {
            int puerto = 8081;
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
