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
            String[] procesos = new String[5];
            procesos[0] = "A";
            procesos[1] = "B";
            procesos[2] = "C";
            procesos[3] = "D";
            procesos[4] = "E";
            
            boolean seguir = true;
            int opc = 0;
            
            while(seguir) {
                System.out.println("\nProcesos");
                System.out.println("1. Datos");
                System.out.println("2. Automatico");
                System.out.println("3. Salir");
                System.out.print("Seleccione una opción: ");
                
                try {
                    opc = Integer.parseInt(sc.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Por favor, ingrese un número válido.");
                    continue;
                }
                
                switch (opc) {
                    case 1 -> {
                        System.out.print("Ingrese nombre del proceso: ");
                        String name = sc.nextLine().trim();
                        
                        System.out.print("Ingrese valor de C: ");
                        int c;
                        try {
                            c = Integer.parseInt(sc.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Valor de C inválido.");
                            break;
                        }
                        
                        System.out.print("Ingrese valor de T: ");
                        int t;
                        try {
                            t = Integer.parseInt(sc.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Valor de T inválido.");
                            break;
                        }
                        
                        try {
                            Object[] params = new Object[]{name, c, t};
                            String response = (String) client.execute("process.sendProcess", params);
                            System.out.println("Servidor respondió: " + response);
                        } catch (Exception e) {
                            System.out.println("Error al enviar el proceso al servidor.");
                            e.printStackTrace();
                        }
                    }
                    case 2 -> {
                        int c = ThreadLocalRandom.current().nextInt(1, 6); // 1-5
                        int t = ThreadLocalRandom.current().nextInt(1, 7); // 1-6
                        int i = ThreadLocalRandom.current().nextInt(0, 5); // 0-4 (índices válidos)
                        
                        System.out.println("Proceso: " + procesos[i] + " C: " + c + " Duración: " + t);
                        
                        try {
                            Object[] params = new Object[]{procesos[i], c, t};
                            String response = (String) client.execute("process.sendProcess", params);
                            System.out.println("Servidor respondió: " + response);
                        } catch (Exception e) {
                            System.out.println("Error al enviar el proceso al servidor.");
                            e.printStackTrace();
                        }
                    }
                    case 3 -> {
                        seguir = false;
                        System.out.println("Saliendo del programa...");
                    }
                    default -> System.out.println("Opción no válida. Por favor, seleccione 1, 2 o 3.");
                }
            }
          
            sc.close();
            System.out.println("Cliente finalizado.");
        } catch (Exception e) {
            System.out.println("Error al conectar con el servidor RPC.");
            e.printStackTrace();
        }
    }
}