import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;


public class Client {

    public static void main(String[] args) {
        try {
            // Configuración del cliente RPC
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://localhost:8080/"));

            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            System.out.println("Cliente conectado al servidor de procesos (localhost:8080)");

            try {
                Object[] params = new Object[]{"A", 2, 5};
                String response = (String) client.execute("process.sendProcess", params);

                System.out.println("Servidor respondió: " + response);
            } catch (Exception e) {
                System.out.println("Error al enviar el proceso al servidor.");
                e.printStackTrace();
            }
            System.out.println("Cliente finalizado.");
        } catch (Exception e) {
            System.out.println("Error al conectar con el servidor RPC.");
            e.printStackTrace();
        }
    }
}
