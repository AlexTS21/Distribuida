
public class multiplicacion {
    public int multiplicacion(int a, int b){
      System.out.println("Llamada para multiplicar dos datos (" + a + ", " + b + ")");

        // servidor ocupado
        try {
            Thread.sleep(8000); // 8 segundos 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Multiplicacion completada " + a + " * " + b + " = " + (a * b));
      return a * b;
    }
}


