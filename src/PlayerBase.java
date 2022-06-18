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
    static String[] logFilters = null;
    
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
            StringBuilder sb = new StringBuilder();
            for (Object o : objects) {
                sb.append("" + o + " ");
            }
            String logText = sb.toString();
            if (logFilters != null) {
                boolean select = false;
                for (String filter : logFilters) {
                    if (logText.indexOf(filter) > -1) select = true; 
                }
                if (!select) return;
            }
            System.err.println("* "+logText);
        }
    }
    
    static void activateLogDebug() {
        doLogDebug = true;
    }
    
}


