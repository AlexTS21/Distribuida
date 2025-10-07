
public class divicion {
     public int divicion(int a, int b){
      System.out.println("Llamada para sumar dos datos (" + a + ", " + b + ")");

        // servidor ocupado
        try {
            Thread.sleep(8000); // 8 segundos 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("divicion completada: " + a + " + " + b + " = " + (a + b));
      return a / b;
    }
}


