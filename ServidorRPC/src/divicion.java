
public class divicion {
     public double divicion(int a, int b){
      System.out.println("Llamada para dividir dos datos (" + a + ", " + b + ")");

        // servidor ocupado
        try {
            Thread.sleep(8000); // 8 segundos 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        double result = a/b;
        System.out.println("Divicion completada: " + a + " / " + b + " = " + result);
      return result;
    }
}


