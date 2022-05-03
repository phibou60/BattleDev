package codingame.spring22;

import java.io.InputStream;

public class PlayerTest {

    public static void main(String args[]) {
        try {
        	Player player = new Player();
        	Player.activateLog();
        	test(player);

            player.forceStrategie(Player.STRATEGIE_ATT_1);
            
            InputStream in = player.getClass().getClassLoader()
                    .getResourceAsStream("codingame/spring22/codingamespring22-input.txt");
            
        	player.joue(in);
        	
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	private static void test(Player player) throws Exception {
		{
			Player.Pion m = player.getNewPion();
		   	m.x = 5000;
			m.y = 5000;
			m.vx = 50;
			m.vy = 50;
			
			Player.Coords base = player.getNewPion();
			base.x = 10_000;
			base.y = 10_000;
			assertEquals(true, m.seRapprocheDe(base));
			System.out.println("test1 ok");
			
		}
		{
			Player.Pion m = player.getNewPion();
		   	m.x = 0;
			m.y = 1;
			m.vx = 3;
			m.vy = 1;
			
			Player.Line l1 = m.getLine();
			assertEquals(1, l1.a);
			assertEquals(-3, l1.b);
			assertEquals(3, l1.c);
			
			Player.Coords base = player.getNewCoords();
			base.x = 2;
			base.y = 5;
			
			Player.Line l2 = l1.getLineOrthogonale(base);
			assertEquals(-3, l2.a);
			assertEquals(-1, l2.b);
			assertEquals(11, l2.c);

			Player.Coords inters = l1.getIntersection(l2);
			assertEquals(3, inters.x);
			assertEquals(2, inters.y);

			Player.Coords inters2 = m.getProjeteOrthogonal(base);
			assertEquals(3, inters2.x);
			assertEquals(2, inters2.y);
			
			assertEquals(true, m.willFallIntoBase(base));

			System.out.println("test2 ok");
		}
		{
		    Player.Pion m = player.getNewPion();
		    m.x = 0;
		    m.y = 0;
		    m.vx = 280;
		    m.vy = 284;
		    
		    Player.Coords h = player.getNewCoords();
		    h.x = 5000;
		    h.y = 0;
		    
		    Player.Interception interc = player.getNewInterception(h, m);
		    System.out.println("Interception: x: "+interc.x+", y: "+interc.y+", round: "+interc.round);
		    
		    assertEquals(1400, interc.x);
		    assertEquals(1420, interc.y);
		    assertEquals(4, interc.round);
		}
		{
			Player.Coords from = player.getNewCoords(); from.x = 50; from.y = 100;
			Player.Coords to = player.getNewCoords(); to.x = 916; to.y = 600;
			Player.Coords vector1 = from.createVFromPoints(from, to);
			assertEquals(866, vector1.x);
			assertEquals(500, vector1.y);
			assertEquals(999, vector1.getVNorme());
			assertEquals(0.52D, vector1.getVAngle());
						
			Player.Coords to2 = vector1.doVtranslation(from);
			assertEquals(to.x, to2.x);
			assertEquals(to.y, to2.y);
			
			Player.Coords to3 = vector1.doVtranslation(from, 500);
			assertEquals(483, to3.x);
			assertEquals(350, to3.y);

			Player.Coords to4 = from.getPointOppose(to, 500);
			assertEquals(-384, to4.x);
			assertEquals(-151, to4.y);

			Player.Coords to4b = from.getPointVers(to, 250);
			assertEquals(266, to4b.x);
			assertEquals(225, to4b.y);

			Player.Coords vector2 = from.createVFromFormeTrigono(vector1.getVAngle(), 1_000D);
			assertEquals(866, vector2.x);
			assertEquals(500, vector2.y);
			
		}
	}

	private static void assertEquals(int v1, int v2) throws Exception {
		if (v1 != v2) {
			throw new Exception ("Assertion Exception: " + v1 + " != " + v2);
		}
	}

	private static void assertEquals(boolean v1, boolean v2) throws Exception {
		if (v1 != v2) {
			throw new Exception ("Assertion Exception: " + v1 + " != " + v2);
		}
	}
	
	private static void assertEquals(double v1, double v2) throws Exception {
		String s1 = (""+v1).substring(0, 4);
		String s2 = (""+v2).substring(0, 4);
		if (!s1.equals(s2)) {
			throw new Exception ("Assertion Exception: " + v1 + " != " + v2);
		}
	}
	

}
