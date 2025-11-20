import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;


public class Client {

    public static void main(String[] args) {
        try {
            // Configuración del cliente RPC
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL("http://localhost:8080/"));

            XmlRpcClient client = new XmlRpcClient();
            client.setConfig(config);

            System.out.println("Cliente conectado al servidor de procesos (localhost:8080)");
            Scanner sc = new Scanner(System.in);
            boolean seguir = true;
            int opc =0;
            while(seguir){
                System.out.println("Procesos");
                System.out.println("1. A");
                System.out.println("2. B");
                System.out.println("3. C");
                System.out.println("4. D");
                System.out.println("5. salir");
                
                opc = Integer.parseInt(sc.next());
                
                int c = ThreadLocalRandom.current().nextInt(1, 5 + 1);
                int t = ThreadLocalRandom.current().nextInt(1, 6 + 1);
                System.out.println("C: " + c + " duracion: " +t);
                switch (opc) {
                    case 1 -> {
                        try {
                             Object[] params = new Object[]{"A", c, t};
                            String response = (String) client.execute("process.sendProcess", params);
                            System.out.println("Servidor respondió: " + response);
                        } catch (Exception e) {
                            System.out.println("Error al enviar el proceso al servidor.");
                            e.printStackTrace();
                        }
                        
                    }
                    case 2 -> {
                        try {
                             Object[] params = new Object[]{"B", c, t};
                            String response = (String) client.execute("process.sendProcess", params);
                            System.out.println("Servidor respondió: " + response);
                        } catch (Exception e) {
                            System.out.println("Error al enviar el proceso al servidor.");
                            e.printStackTrace();
                        }
                    }
                    case 3 -> {
                        try {
                             Object[] params = new Object[]{"C", c, t};
                            String response = (String) client.execute("process.sendProcess", params);
                            System.out.println("Servidor respondió: " + response);
                        } catch (Exception e) {
                            System.out.println("Error al enviar el proceso al servidor.");
                            e.printStackTrace();
                        }
                    }
                    case 4 -> {
                        try {
                             Object[] params = new Object[]{"D", c, t};
                            String response = (String) client.execute("process.sendProcess", params);
                            System.out.println("Servidor respondió: " + response);
                        } catch (Exception e) {
                            System.out.println("Error al enviar el proceso al servidor.");
                            e.printStackTrace();
                        }
                    }
                    case 5 -> seguir = false;
                    default -> System.out.println("Escriba bien chavo");
                }
            }
          
            System.out.println("Cliente finalizado.");
        } catch (Exception e) {
            System.out.println("Error al conectar con el servidor RPC.");
            e.printStackTrace();
        }
    }
}
