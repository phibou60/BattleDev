package commons;

import static org.junit.Assert.*;

import org.junit.Test;

public class JUnitTest {

	@Test
	public void testCoords() throws Exception {

		Coords from = new Coords(50, 100);
		Coords to = new Coords(916, 600);
		
		assertTrue(new Coords(100, 100).dansLaCarte());
		assertTrue(new Coords(from.MAX_X, from.MAX_Y).dansLaCarte());
		assertFalse(new Coords(-1, -1).dansLaCarte());
		assertFalse(new Coords(100, -1).dansLaCarte());
		assertFalse(new Coords(-1, 100).dansLaCarte());
		assertFalse(new Coords(from.MAX_X+1, 100).dansLaCarte());
		assertFalse(new Coords(100, from.MAX_Y+1).dansLaCarte());
		
		assertEquals(999, from.distance(to));
		
		Coords vector1 = from.createVFromPoints(from, to);
		assertEquals(866, vector1.x);
		assertEquals(500, vector1.y);
		assertEquals(999, vector1.getVNorme());
		assertEquals(0.52D, vector1.getVAngle());
					
		Coords to2 = vector1.doVtranslation(from);
		assertEquals(to.x, to2.x);
		assertEquals(to.y, to2.y);
		
		Coords to3 = vector1.doVtranslation(from, 500);
		assertEquals(483, to3.x);
		assertEquals(350, to3.y);

		Coords to4 = from.getPointOppose(to, 500);
		assertEquals(-384, to4.x);
		assertEquals(-151, to4.y);

		Coords to4b = from.getPointVers(to, 250);
		assertEquals(266, to4b.x);
		assertEquals(225, to4b.y);

		Coords vector2 = from.createVFromFormeTrigono(vector1.getVAngle(), 1_000D);
		assertEquals(866, vector2.x);
		assertEquals(500, vector2.y);

	}

	@Test
	public void testInterception() throws Exception {
	    Pion m = new Pion();
	    m.x = 0;
	    m.y = 0;
	    m.v.x = 280;
	    m.v.y = 284;
	    
	    Pion h = new Pion();
	    h.x = 5000;
	    h.y = 0;
	    
	    Interception interc = h.getInterception(m);
	    
	    assertEquals(1400, interc.x);
	    assertEquals(1420, interc.y);
	    assertEquals(4, interc.round);

	}
	
	@Test
	public void testPion() throws Exception {

		Pion p = new Pion();
	   	p.x = 4000;
		p.y = 5000;
		p.v.x = 40;
		p.v.y = 50;
		
		Coords nextCoords = p.nextCoords(); 
		
		assertEquals(4040, nextCoords.x);
		assertEquals(5050, nextCoords.y);

		Pion m = new Pion();
	   	m.x = 5000;
		m.y = 5000;
		m.v.x = 50;
		m.v.y = 50;
		
		assertTrue(m.seRapprocheDe(new Coords(10_000, 10_000)));
		assertTrue(m.seRapprocheDe(new Coords(6_000, 10_000)));
		assertTrue(m.seRapprocheDe(new Coords(10_000, 6_000)));
		
		assertFalse(m.seRapprocheDe(new Coords(1_000, 10_000)));
		assertFalse(m.seRapprocheDe(new Coords(10_000, 1_000)));
		assertFalse(m.seRapprocheDe(new Coords(1_000, 1_000)));
	}
	
	@Test
	public void testLine() throws Exception {
		Pion m = new Pion();
	   	m.x = 0;
		m.y = 1;
		m.v.x = 3;
		m.v.y = 1;
		
		Line l1 = m.getLine();
		assertEquals(1, l1.a);
		assertEquals(-3, l1.b);
		assertEquals(3, l1.c);
		
		Coords base = new Coords(2, 5);
		
		Line l2 = l1.getLineOrthogonale(base);
		assertEquals(-3, l2.a);
		assertEquals(-1, l2.b);
		assertEquals(11, l2.c);

		Coords inters = l1.getIntersection(l2);
		assertEquals(3, inters.x);
		assertEquals(2, inters.y);

		Coords inters2 = m.getProjeteOrthogonal(base);
		assertEquals(3, inters2.x);
		assertEquals(2, inters2.y);
		
		assertTrue(m.willFallIntoBase(base));
	}
	
	private static void assertEquals(double v1, double v2) throws Exception {
		String s1 = ""+v1;
		if (s1.length() > 3) s1 = s1.substring(0, 4);
		String s2 = ""+v2;
		if (s2.length() > 3) s2 = s2.substring(0, 4);
		if (!s1.equals(s2)) {
			throw new Exception ("Assertion Exception: " + v1 + " != " + v2);
		}
	}

}
