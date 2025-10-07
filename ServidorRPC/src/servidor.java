import org.apache.xmlrpc.server.PropertyHandlerMapping; // Registra mapeos entre nombres de servicios (strings) y clases Java
import org.apache.xmlrpc.server.XmlRpcServer; // Clase principal que maneja la lógica del servidor RPC basado en XML
import org.apache.xmlrpc.webserver.WebServer; // Servidor web embebido simple que puede escuchar peticiones HTTP en un puerto específico

public class servidor {
     public static class Mensajes {
        public String recibir(String mensaje) {
            System.out.println("MENSAJE DEL CLIENTE: " + mensaje);
            return "OK";
        }
    }
    public static void main(String[] args) {
        try {
            int puerto = 8080;
            System.out.println("Iniciando servidor en el puerto " + puerto + "...");

            WebServer webServer = new WebServer(puerto);
            XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
            

            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            phm.addHandler("MiServidorRPC_Suma", suma.class);
            phm.addHandler("MiServidorRPC_Resta", resta.class);
            phm.addHandler("MiServidorRPC_Multiplicacion", multiplicacion.class);
            phm.addHandler("MiServidorRPC_Divicion", divicion.class);
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
