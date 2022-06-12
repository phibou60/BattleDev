
import java.io.InputStream;

public class PlayerTest {

    public static void main(String args[]) {
        try {
            Player player = new Player();
            //test(player); System.exit(0);
            
            InputStream inStream = player.getClass().getClassLoader()
                    .getResourceAsStream("input.txt");
            player.activateLog();
            player.activateLogDebug();
            player.joue(inStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
