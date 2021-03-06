package commons;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class JUnitTest {

    @Test
    public void testCoords() throws Exception {

        Coords staticCoords = new Coords();
        
        Coords from = new Coords(50, 100);
        assertEquals(50, from.getIntX());
        assertEquals(100, from.getIntY());
        assertTrue(from.equals(new Coords(50, 100)));
        
        Coords to = new Coords(916, 600);
        
        assertEquals(999, from.distance(to));
        
        Coords vector1 = from.createVecteurVers(to);
        assertEquals(866, vector1.x);
        assertEquals(500, vector1.y);
        assertEquals(999, vector1.getVNorme());
        assertEquals(0.52D, vector1.getVAngle());
        
        {// Test createVecteurAPartirDe
            Coords vector1b = to.createVecteurAPartirDe(from);
            assertTrue(vector1.equals(vector1b));
        }
                            
        Coords to2 = vector1.doVtranslation(from);
        assertEquals(to.x, to2.x);
        assertEquals(to.y, to2.y);
        
        Coords to3 = vector1.doVtranslation(from, 500);
        assertEquals(483, to3.x);
        assertEquals(350, to3.y);

        Coords to4b = from.getAuDelaDe(to, 250);
        assertEquals(1132, to4b.x);
        assertEquals(725, to4b.y);

        Coords to4 = from.getPointOppose(to, 500);
        assertEquals(-383, to4.x);
        assertEquals(-150, to4.y);

        Coords vector2 = staticCoords.createVFromFormeTrigono(vector1.getVAngle(), 1_000D);
        assertEquals(866, vector2.x);
        assertEquals(500, vector2.y);
        
        { // test dansLaCarte
            assertTrue(new Coords(100, 100).dansLaCarte());
            assertTrue(new Coords(staticCoords.MAX_X, staticCoords.MAX_Y).dansLaCarte());
            assertFalse(new Coords(-1, -1).dansLaCarte());
            assertFalse(new Coords(100, -1).dansLaCarte());
            assertFalse(new Coords(-1, 100).dansLaCarte());
            assertFalse(new Coords(staticCoords.MAX_X+1, 100).dansLaCarte());
            assertFalse(new Coords(100, staticCoords.MAX_Y+1).dansLaCarte());
        }

        { // test getAuDelaDe
            Coords base = new Coords(1200, 400);
            Coords dela = base.getAuDelaDe(new Coords(1450, 650));
            assertEquals(1700, dela.x);
            assertEquals(900, dela.y);
            Coords dela2 = base.getAuDelaDe(new Coords(1450, 650), 353.55);
            assertEquals(1699, dela2.x);
            assertEquals(899, dela2.y);
            
         // test getPointOppose
            Coords op = base.getPointOppose(new Coords(1450, 650));
            assertEquals(950, op.x);
            assertEquals(150, op.y);
            Coords op2 = base.getPointOppose(new Coords(1450, 650), 353.55);
            assertEquals(950, op2.x);
            assertEquals(150, op2.y);
        }

        { // test getPointVers
            Coords base = new Coords(1200, 400);
            Coords dela = base.getPointVers(new Coords(1950, 1150), 353);
            assertEquals(1449, dela.x);
            assertEquals(649, dela.y);
        }

        { // test rotation
            Coords v = new Coords(250, 150);
            Coords quartAH = v.rotationQuartTourAntiHoraire();
            assertEquals(-150, quartAH.x);
            assertEquals(250, quartAH.y);
            Coords quartH = v.rotationQuartTourHoraire();
            assertEquals(150, quartH.x);
            assertEquals(-250, quartH.y);
            
            Coords quartAH2 = v.rotation(Math.PI/2);
            assertEquals(-150, quartAH2.x);
            assertEquals(250, quartAH2.y);
            Coords quartH2 = v.rotation(-Math.PI/2);
            assertEquals(149, quartH2.x);
            assertEquals(-250, quartH2.y);
        }

        { // test getPointVers
            Coords base = new Coords(1200, 400);
            Coords dela = base.getPointVers(new Coords(1950, 1150), 353);
            assertEquals(1449, dela.x);
            assertEquals(649, dela.y);
        }

        { // test compareDistance (a est plus proche que b de la base)
            Coords base = new Coords(1200, 400);
            Coords a = new Coords(1450, 650);
            Coords b = new Coords(900, 100);
            assertTrue(base.compareDistance(a, b) < 0);
            assertTrue(base.compareDistance(b, a) > 0);
            assertTrue(base.compareDistance(a, 400) < 0);
            assertTrue(base.compareDistance(b, 400) > 0);
        }

        { // test pointSurSegment
            Coords base = new Coords(1200, 400);
            Coords a = new Coords(1400, 600);
            
            Coords mi = base.pointSurSegment(a, 0.5);
            assertEquals(1300, mi.x);
            assertEquals(500, mi.y);

            Coords quart = base.pointSurSegment(a, 0.25);
            assertEquals(1250, quart.x);
            assertEquals(450, quart.y);
        }
        
        {// Test getVAngleDegres
            Coords v = new Coords(200, 200);
            assertEquals(45, v.getVAngleDegres().angle);

            v = new Coords(-200, 200);
            assertEquals(90+45, v.getVAngleDegres().angle);
        }
        
        {// Test ajoute et soustrait vecteurs
            Coords v1 = new Coords(200, 200);
            Coords v2 = new Coords(300, 100);
            
            Coords v3 = v1.ajouteVecteur(v2);
            assertEquals(500, v3.x);
            assertEquals(300, v3.y);
            
            Coords v4 = v3.soustraitVecteur(v2); 
            assertTrue(v4.equals(v1));
        }
        
        {// Test angleVecteurs
            Coords base = new Coords(0, 0);
            Coords a = new Coords(200, 200);
            Coords b = new Coords(-200, 200);
            
            assertEquals(90, base.angleVecteurs(a, b).angle);
            assertEquals(-90, base.angleVecteurs(b, a).angle);
        }
             
        { // Comparators
            List<Coords> list = new ArrayList<>();
            list.add(new Coords(100, 100));
            list.add(new Coords(300, 300));
            list.add(new Coords(200, 200));
            list.add(new Coords(400, 400));
            Coords base = new Coords(220, 220);
            
            List<Coords> listAsc = list.stream()
                    .sorted(staticCoords.duPlusProcheAuPlusLoin(base))
                    .collect(Collectors.toList());
            assertEquals(200, listAsc.get(0).x);
            assertEquals(300, listAsc.get(1).x);
            assertEquals(100, listAsc.get(2).x);
            assertEquals(400, listAsc.get(3).x);
            
            List<Coords> listDesc = list.stream()
                    .sorted(staticCoords.duPlusLoinAuPlusProche(base))
                    .collect(Collectors.toList());
            assertEquals(200, listDesc.get(3).x);
            assertEquals(300, listDesc.get(2).x);
            assertEquals(100, listDesc.get(1).x);
            assertEquals(400, listDesc.get(0).x);
        }
    }

    /*
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
    */
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
    public void testDroite() throws Exception {
        {
            Pion m = new Pion();
            m.x = 0;
            m.y = 1;
            m.v.x = 3;
            m.v.y = 1;
            
            Droite l1 = m.getLine();
            assertEquals(1, l1.a);
            assertEquals(-3, l1.b);
            assertEquals(3, l1.c);
            
            Coords base = new Coords(2, 5);
            
            Droite l2 = l1.getLineOrthogonale(base);
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
        {
            Droite l1 = new Droite(1, 0, -50);
            Droite l2 = new Droite(1, -1, 0);
            Coords inters = l1.getIntersection(l2);
            assertEquals(50, inters.x);
            assertEquals(50, inters.y);
            
            inters = l2.getIntersection(l1);
            assertEquals(50, inters.x);
            assertEquals(50, inters.y);
           
            Droite l3 = new Droite(0, 1, -50);
            inters = l1.getIntersection(l3);
            assertEquals(50, inters.x);
            assertEquals(50, inters.y);
            
            inters = l3.getIntersection(l1);
            assertEquals(50, inters.x);
            assertEquals(50, inters.y);

            inters = l2.getIntersection(l3);
            assertEquals(50, inters.x);
            assertEquals(50, inters.y);
            
            inters = l3.getIntersection(l2);
            assertEquals(50, inters.x);
            assertEquals(50, inters.y);
            
            // Lignes parall?le
            Droite l1b = new Droite(1, 0, -500);
            inters = l1.getIntersection(l1b);
            assertNull(inters);
            inters = l1b.getIntersection(l1);
            assertNull(inters);

            Droite l3b = new Droite(0, 1, -500);
            inters = l3.getIntersection(l3b);
            assertNull(inters);
            inters = l3b.getIntersection(l3);
            assertNull(inters);
        }
        
        
    }
    
    @Test
    public void testAngleEnDegres() throws Exception {
        assertEquals(45, new AngleEnDegres(90).ajoute(-45).angle);
        assertEquals(-170, new AngleEnDegres(100).ajoute(90).angle);
        
        assertEquals(-45, new AngleEnDegres(-90).ajoute(45).angle);
        assertEquals(-135, new AngleEnDegres(-90).ajoute(-45).angle);

        assertEquals(170, new AngleEnDegres(-100).retire(90).angle);
        assertEquals(170, new AngleEnDegres(-100).ajoute(-90).angle);
        
        assertTrue(new AngleEnDegres(70).estProcheDe(60, 20));
        assertTrue(new AngleEnDegres(-110).estProcheDe(-120, 20));
        assertFalse(new AngleEnDegres(70).estProcheDe(-60, 40));
        
        assertEquals(Math.PI/4, new AngleEnDegres(45).toRadian());
        assertEquals(45, new AngleEnDegres().ofRadian(Math.PI/4).angle);
    }
    
    @Test
    public void testCourbeDeBezierOrdre2() throws Exception {
        CourbeDeBezierOrdre2 cdb = new CourbeDeBezierOrdre2(new Coords(2500, 2700), new Coords(4750, 2700), new Coords(4750, 150));
        List<Coords> points = cdb.getPoints(10);
        points.forEach(System.out::println);
        assertEquals(11, points.size());
        
        System.out.println("hypothenuse: "+new Coords(2500, 2700).distance(new Coords(4750, 150)));
        assertEquals(4100, cdb.getLongueur());
        assertEquals(9, cdb.nbSegmentsPourDistanceMax(500));
        
        List<Coords> points2 = cdb.getPointsPourDistanceMax(500);
        assertEquals(10, points2.size());
        points2.forEach(System.out::println);
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
