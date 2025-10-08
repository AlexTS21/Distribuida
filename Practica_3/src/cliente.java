import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.URL;
import java.util.Scanner;

public class cliente {
    public static void main(String[] args) {
        try {   
            // Configuración del cliente RPC
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://localhost:8080/"));

            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            try {
             Object[] params = new Object[]{"Cliente conectado exitosamente"};
             client.execute("Mensajes.recibir", params);
                } catch (Exception e) {
                   System.out.println("Fallo de Conexion");
                   return;
                    }
            Scanner sc = new Scanner(System.in);
            boolean seguir = true;
            int opc =0;
            int a,b,c,num1,num2;

            System.out.println("Cliente conectado al servidor RPC.");
            
sc.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
