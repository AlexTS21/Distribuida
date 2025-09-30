public class suma {
    public int sumar(int a, int b) {
        System.out.println("Llamada para sumar dos datos (" + a + ", " + b + ")");
        
        // Enviar mensaje de "ocupado" al cliente
        enviarMensajeAlCliente("Servidor ocupado - Procesando suma, espere...");
        
        try {
            Thread.sleep(3000); // Espera 3 segundos
        } catch (InterruptedException e) {}
        
        // Enviar mensaje de completado
        enviarMensajeAlCliente("Suma completada: " + a + " + " + b + " = " + (a + b));
        
        return a + b;
    }
    
    private void enviarMensajeAlCliente(String mensaje) {
        // Esta es una simulación - en realidad necesitarías un callback
        System.out.println("Para cliente: " + mensaje);
    }
}
