import java.util.concurrent.locks.ReentrantLock;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;
import java.util.*;

public class servidor {

    private static WebServer webServer;
    private static volatile boolean servidorDetenido = false; // NUEVO

    public static class Mensajes {
         
        private static final ReentrantLock lock = new ReentrantLock(true);
        private static List<String> clientesConectados = new ArrayList<>();
        private static Random random = new Random();
        private static String ultimoCliente = "";
        private static int contadorPeticiones = 0;
        private static final int MAX_PETICIONES = 20;

        // Variables para control del temporizador
        private static Timer timerCierre = null;
        private static final int TIEMPO_MAXIMO_MS = 20000; // 20 segundos

        public String recibir(String nombreCliente) {
            lock.lock();
            try {
                // Si alcanzó el máximo de peticiones, cerrar servidor
                if (contadorPeticiones >= MAX_PETICIONES) {
                    System.out.println("Servidor alcanzó las 100 peticiones. Cerrando...");
                    
                    detenerServidor();
                    return "Servidor cerrado";
                }

                // Registrar nuevo cliente si no existe
                if (!clientesConectados.contains(nombreCliente)) {
                    clientesConectados.add(nombreCliente);
                    System.out.println("Nuevo cliente registrado: " + nombreCliente);
                    manejarTemporizador(); // revisar si hay que iniciar o detener el temporizador
                }

                // Si hay menos de 2 clientes, esperar
                if (clientesConectados.size() < 2) {
                    System.out.print("Esperando clientes (" + clientesConectados.size() + " conectados)\n");
                    try {
                        Thread.sleep(1000); // Simular tiempo de atención
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "Esperando";
                }

                // Elegir cliente aleatorio distinto del último
                String elegido;
                do {
                    elegido = clientesConectados.get(random.nextInt(clientesConectados.size()));
                } while (elegido.equals(ultimoCliente));

                
                
                // Si el que llamó no fue el elegido, se le rechaza
                if (!nombreCliente.equals(elegido)) {
                    System.out.println("Cliente " + nombreCliente + " en espera...");
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

        private static void manejarTemporizador() {
            if (clientesConectados.size() < 2) {
                // Si hay menos de 2 clientes y no hay temporizador activo, iniciarlo
                if (timerCierre == null) {
                    timerCierre = new Timer();
                    timerCierre.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            lock.lock();
                            try {
                                if (clientesConectados.size() < 2) {
                                    System.out.println("⏰ Tiempo máximo alcanzado (" + TIEMPO_MAXIMO_MS / 1000 + " segundos).");
                                    detenerServidor();
                                } else {
                                    System.out.println("Temporizador cancelado automáticamente (ya hay 2 clientes).");
                                }
                            } finally {
                                lock.unlock();
                            }
                        }
                    }, TIEMPO_MAXIMO_MS);
                    System.out.println("⏳ Temporizador de cierre iniciado (" + TIEMPO_MAXIMO_MS / 1000 + " segundos).");
                }
            } else {
                // Si hay 2 o más clientes, cancelar el temporizador si está activo
                if (timerCierre != null) {
                    timerCierre.cancel();
                    timerCierre = null;
                    System.out.println("✅ Temporizador de cierre cancelado (ya hay 2+ clientes).");
                }
            }
        }
    }

    public static synchronized void detenerServidor() {
        if (servidorDetenido) return; // Si ya se detuvo, no hacer nada

        try {
            servidorDetenido = true; // marcar como detenido
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

            webServer.start();

            System.out.println("Servidor XML-RPC iniciado correctamente.");
            System.out.println("Esperando llamadas RPC en: http://localhost:" + puerto + "/");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
