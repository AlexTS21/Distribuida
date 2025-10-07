import java.util.concurrent.ConcurrentHashMap;

public class suma {
    private static ConcurrentHashMap<String, Integer> resultadosListos = new ConcurrentHashMap<>();
    
    public String sumarConId(int a, int b, String operationId) {
        System.out.println("Petición SUMAR recibida - ID: " + operationId);
        
        try {
          
            Thread.sleep(3000);
            
            int resultado = a + b;
            System.out.println("Resultado calculado: " + a + " + " + b + " = " + resultado);
            
            //  aqui guardamos los datos antes de mandarlos
            resultadosListos.put(operationId, resultado);
            System.out.println("Resultado guardado para recuperación: " + operationId);
            
            // aqui damos el tiempo para poder desconectar al cliente
            System.out.println("Esperando para enviar respuesta");
            Thread.sleep(5000); // tiempp para desconetar 
            
            // si pasa aqui no se desconecto
            System.out.println("Enviando respuesta al cliente: " + resultado);
            return "EXITO_" + resultado;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "ERROR";
        }
    }
    
    // Método para recuperar resultados que no se pudieron enviar
    public String obtenerResultadoNoEnviado(String operationId) {
        Integer resultado = resultadosListos.get(operationId);
        if (resultado != null) {
            System.out.println("Recuperando resultado no enviado para: " + operationId + " = " + resultado);
            // Limpiar después de recuperar
            resultadosListos.remove(operationId);
            return "RECUPERADO_" + resultado;
        } else {
            return "NO_ENCONTRADO";
        }
    }
    
    // Método original para compatibilidad
    public int sumar(int a, int b) {
        System.out.println("Llamada para sumar dos datos (" + a + ", " + b + ")");
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        int resultado = a + b;
        System.out.println("Suma completada: " + a + " + " + b + " = " + resultado);
        return resultado;
    }
}