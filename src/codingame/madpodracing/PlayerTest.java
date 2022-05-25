package codingame.madpodracing;

import java.io.InputStream;
import java.util.Arrays;

public class PlayerTest {

    public static void main(String args[]) {
        try {
            Player player = new Player();
            //test(player); System.exit(0);
            
            InputStream inStream = player.getClass().getClassLoader()
                    .getResourceAsStream("codingame/madpodracing/input.txt");
            player.activateLog();
            player.joue(inStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testCoupureThrust(Player player) throws Exception {
        
        int[] temps = new int[10];
        
        for (int noThrust = 0; noThrust<temps.length; noThrust++) {
            int t = Player.tempsArriveeCheckpoint(
                    new Coords(6988, 7044) /* position */,
                    new Coords(-432,  182) /* vecteur vitesse */, 
                    195 /* anglePod */,
                    new Coords(6026, 5359) /* checkpoint */,
                    new Coords(6026, 5359) /* cible */,
                    noThrust /* noThrust */,
                    30 /* profMax */);
            temps[noThrust] = t;
        }

        
        int tempsAvecFullThrust = 999;
        
        int noThrust = 0;
        boolean continuer = true;
        
        while (noThrust < 10 && continuer) {
            int t = Player.tempsArriveeCheckpoint(
                    new Coords(6988, 7044) /* position */,
                    new Coords(-432,  182) /* vecteur vitesse */, 
                    195 /* anglePod */,
                    new Coords(6026, 5359) /* checkpoint */,
                    new Coords(6026, 5359) /* cible */,
                    noThrust /* noThrust */,
                    30 /* profMax */);
            if (noThrust == 0) {
                tempsAvecFullThrust = t;
            } else {
                if (t < tempsAvecFullThrust) {
                    System.out.println("Meilleur de couper le thrust");
                    continuer = false;
                }
            }
            noThrust++;
        }
        
        System.out.println("ret:"+Arrays.toString(temps));
    }

    private static void assertEquals(int v1, int v2) throws Exception {
        if (v1 != v2) {
            throw new Exception("Assertion Exception: " + v1 + " != " + v2);
        }
    }

    private static void assertEquals(boolean v1, boolean v2) throws Exception {
        if (v1 != v2) {
            throw new Exception("Assertion Exception: " + v1 + " != " + v2);
        }
    }

    private static void assertEquals(double v1, double v2) throws Exception {
        String s1 = ("" + v1).substring(0, 4);
        String s2 = ("" + v2).substring(0, 4);
        if (!s1.equals(s2)) {
            throw new Exception("Assertion Exception: " + v1 + " != " + v2);
        }
    }

}
