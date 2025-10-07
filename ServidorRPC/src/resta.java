
public class resta {
      public int resta(int a, int b) {
        System.out.println("Llamada para restar dos datos (" + a + ", " + b + ")");

        // servidor ocupado
        try {
            Thread.sleep(8000); // 8 segundos 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("resta completada: " + a + " - " + b + " = " + (a - b));
        return a - b;
    }
}


