import java.io.InputStream;
import java.util.Scanner;

public class PlayerBase {

    public static void main(String args[]) {
        Player player = new Player();
        player.joue(System.in);
    }
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);
        
        long timeStart = System.nanoTime();
        /* ... */
        System.err.println("Duree: "+Math.floorDiv(System.nanoTime()-timeStart, 1_000_000)+"ms");
        
    }

    /* Codingame common */

    static boolean doLog = true;
    static boolean doLogDebug = false;
    
    static void log(Object... objects) {
        if (doLog) {
            for (Object o : objects) {
                System.err.print("" + o + " ");
            }
            System.err.println();
        }
    }
    
    static void activateLog() {
        doLog = true;
    }
    
    static void logDebug(Object... objects) {
        if (doLogDebug) {
            System.err.print("*");
            for (Object o : objects) {
                System.err.print("" + o + " ");
            }
            System.err.println();
        }
    }
    
    static void activateLogDebug() {
        doLogDebug = true;
    }
    
}


