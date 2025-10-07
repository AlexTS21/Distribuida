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
            

            while (seguir) {
                System.out.println("Calculadora-Servidor");
                System.out.println("1.-Sumar");
                System.out.println("2.-Restar");
                System.out.println("3.-Multiplicar");
                System.out.println("4.-Dividir");
                System.out.println("5.-Salir");
                
                opc = Integer.parseInt(sc.next());
                switch(opc){
                    
                    case 1:
                        System.out.println("Suma");
                        System.out.println("Ingresa el primer numero");
                        a = sc.nextInt();
                        
                        System.out.println("Ingresa el segundo numero");
                        b = sc.nextInt();
                        try{
                           Object[] params1 = new Object[]{a, b};
                           Integer resultado1 = (Integer) client.execute("MiServidorRPC_Suma.sumar", params1);
                           System.out.println("Suma: " + resultado1);  
                        }catch (Exception e) {
                           System.out.println("ERROR: Servidor desconectado");
                           seguir = false;
    }
                       
                       break;
                        
                    case 2:
                         System.out.println("Resta");
                         System.out.println("Ingresa el primer numero");
                         a = sc.nextInt();
                        
                         System.out.println("Ingresa el segundo numero");
                         b = sc.nextInt();
                        try{
                             Object[] params2 = new Object[]{a, b};
                             Integer resultado2 = (Integer) client.execute("MiServidorRPC_Resta.resta", params2);
                              System.out.println("Resta: " + resultado2);
                        }catch (Exception e){
                            System.out.println("ERROR: Servidor desconectado");
                            seguir = false;
                        }
                         
                        break;
                        
                    case 3:
                         System.out.println("multiplicacion");
                         System.out.println("Ingresa el primer numero");
                         a = sc.nextInt();
                        
                         System.out.println("Ingresa el segundo numero");
                         b = sc.nextInt();
                        try{
                             Object[] params3 = new Object[]{a, b};
                             Integer resultado3 = (Integer) client.execute("MiServidorRPC_Multiplicacion.multiplicacion", params3);
                             System.out.println("Resta: " + resultado3);
                        }catch (Exception e){
                            System.out.println("ERROR: Servidor desconectado");
                            seguir = false;
                        }
                         
                         break;
                         
                    case 4:
                         System.out.println("Divicion");
                         System.out.println("Ingresa el primer numero");
                         a = sc.nextInt();
                       
                         System.out.println("Ingresa el segundo numero");
                         b = sc.nextInt();
                          if(b != 0){
                            try{
                                Object[] params4 = new Object[]{a, b};
                                Integer resultado4 = (Integer) client.execute("MiServidorRPC_Divicion.divicion", params4);
                                System.out.println("Divicion: " + resultado4);
                            }catch (Exception e){
                            System.out.println("ERROR: Servidor desconectado");
                            seguir = false;
                        }
                            
                            
                        }else{
                         System.out.println("El Divisor No puede ser 0 ");
                         }
                          break;
                    case 5:
                        
                        seguir = false;
                        try {
                             Object[] params = new Object[]{"Cliente Desconectado"};
                              client.execute("Mensajes.recibir", params);
                                } catch (Exception e) {
                               System.out.println("Fallo de Desconexion");
                              }
                       

                        break;
                    
                    default:
                        System.out.println("Opcion No Valida");
                }   
                
            }
sc.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
