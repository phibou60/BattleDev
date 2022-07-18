package ordije;

import java.util.*;

class BibliothequeOuverturesChess360 {
    
    static Map<String, String> ouvs;
    static {
        ouvs = new HashMap<>();
        
        //---- Blancs
        
        // Debut
        ouvs.put("pppppppp/8/8/8/8/PPPPPPPP/w", "d2d4,e2e4");
        
        // Après d2d4
        ouvs.put("p1pppppp/1n6/8/3P4/8/PPP1PPPP/w", "e2e4");
        ouvs.put("p1pppppp/1p6/8/3P4/8/PPP1PPPP/w", "e2e4");
        
        //---- Noir
        
        ouvs.put("pppppppp/8/8/8/3P4/8/PPP1PPPP/b", "d7d5,f7f5");
        ouvs.put("pppppppp/8/8/8/4P3/8/PPPP1PPP/b", "e7e5,c7c5");
    }
    
    static Coup meilleurCoup(Echiquier e) {
        String speudoFen = getPseudoFen(e);
        logDebug("speudoFen", speudoFen);
        
        String coups = ouvs.get(speudoFen);
        
        if (coups == null) return null;
        
        String[] splits = coups.split(",");
        
        int i = (int) Math.floor(Math.random() * splits.length);
        
        return new Coup(splits[i]);
    }

    static String getPseudoFen(Echiquier e) {
        StringBuilder out = new StringBuilder();
        
        for (byte l=1; l<7; l++) {
            for (byte c=0; c<8; c++) {
                char piece = e.table[l * 8 + c];
                if (piece == ' ') piece = '1';
                out.append(piece);
            }
            out.append("/");
        }
        
        out.append(e.couleur);
        String result = out.toString();
        
        result = result.replace("11111111", "8");
        result = result.replace("1111111", "7");
        result = result.replace("111111", "6");
        result = result.replace("11111", "5");
        result = result.replace("1111", "4");
        result = result.replace("111", "3");
        result = result.replace("11", "2");
                
        return result;
    }
    
    //---------------------------------------------------------------------------------
    // Gestion de la log
    //---------------------------------------------------------------------------------
    
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
