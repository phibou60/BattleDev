package ordije;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class EchiquierTest {

    @Test
    public void testEchiquier() {
        Echiquier e = Echiquier.ofFen("nrbbnkqr/1ppppppp/8/8/5P2/Pp6/1PPPP1PP/1RBBNKQR w BHbh - 0 4");
        System.out.println(e.printTable());
        
        System.out.println("liste des coups:");
        List<Coup> coups = e.listeCoups();
        coups.forEach(c -> System.out.println("> "+c.toUci()));
    }

    @Test
    public void testMvtPion() {
        Echiquier e1 = Echiquier.ofFen("8/8/8/3p1p2/4P3/2P5/1P5P/8 w BHbh - 0 4");
        List<Coup> coups1 = e1.listeCoups();
        assertEquals(8, coups1.size());

        Echiquier e2 = Echiquier.ofFen("8/8/8/3p1p2/4P3/2P5/1P5P/8 b BHbh - 0 4");
        List<Coup> coups2 = e2.listeCoups();
        assertEquals(4, coups2.size());
    }

    @Test
    public void testMvtTour() {
        Echiquier e1 = Echiquier.ofFen("8/8/8/3R4/8/8/8/8 w BHbh - 0 4");
        List<Coup> coups1 = e1.listeCoups();
        assertEquals(14, coups1.size());

        Echiquier e2 = Echiquier.ofFen("8/3p4/8/2PR4/8/8/8/8 w BHbh - 0 4");
        List<Coup> coups2 = e2.listeCoups();
        assertEquals(11, coups2.size());
    }

    @Test
    public void testMvtFou() {
        Echiquier e1 = Echiquier.ofFen("8/8/8/3B4/8/8/8/8 w BHbh - 0 4");
        List<Coup> coups1 = e1.listeCoups();
        assertEquals(13, coups1.size());
    }

    @Test
    public void testMvtDame() {
        Echiquier e1 = Echiquier.ofFen("8/8/8/3q4/8/8/8/8 b BHbh - 0 4");
        List<Coup> coups1 = e1.listeCoups();
        assertEquals(27, coups1.size());
    }

    @Test
    public void testMvtCavalier() {
        Echiquier e = Echiquier.ofFen("8/8/8/3n4/8/2x5/8/8 b BHbh - 0 4");
        System.out.println(e.printTable());
        
        System.out.println("liste des coups:");
        List<Coup> coups = e.listeCoups();
        assertEquals(7, coups.size());
        coups.forEach(c -> System.out.println("> "+c.toUci()));
    }

    @Test
    public void testMvtRoi() {
        Echiquier e = Echiquier.ofFen("8/8/8/3kx3/8/8/8/8 b BHbh - 0 4");
        System.out.println(e.printTable());
        
        System.out.println("liste des coups:");
        List<Coup> coups = e.listeCoups();
        assertEquals(7, coups.size());
        coups.forEach(c -> System.out.println("> "+c.toUci()));
    }

}
