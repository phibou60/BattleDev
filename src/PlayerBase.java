import java.io.InputStream;
import java.util.Scanner;

public class PlayerBase {

    public static void main(String args[]) {
        PlayerBase player = new PlayerBase();
        player.joue(System.in);
    }
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);
        
        long timeStart = System.nanoTime();
        /* ... */
        System.err.println("Duree: "+Math.floorDiv(System.nanoTime()-timeStart, 1_000_000)+"ms");
        
    }

    /* Gestion log */
    
    static boolean doLog = true;
    static boolean doLogDebug = false;
    
    static void activateLog() {
        doLog = true;
    }
    
    static void activateLogDebug() {
        doLogDebug = true;
    }
    
    static void log(Object... objects) {
        if (doLog) {
            StringBuilder sb = new StringBuilder();
            for (Object o : objects) {
                sb.append("" + o + " ");
            }
            String logText = sb.toString();
            System.err.println("* "+logText);
        }
    }
     
    static void logDebug(Object... objects) {
        if (doLogDebug) {
            StringBuilder sb = new StringBuilder();
            for (Object o : objects) {
                sb.append("" + o + " ");
            }
            String logText = sb.toString();
            System.err.println("* "+logText);
        }
    }
    
}


