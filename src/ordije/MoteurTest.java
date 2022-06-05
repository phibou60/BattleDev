package ordije;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class MoteurTest {

    /**
     * 1k6/8/2Q4R/1p1B4/1P2P1P1/8/P5P1/R2KN1N1 w A - 0 26
     * en jouant dame c6c5, les noirs n'ont plus de coups à jouer.
     */
    @Test
    public void testMoteur() {
        Evaluation evaluation = new Evaluation();
        
        Moteur moteur = new Moteur(evaluation, 4, 999999);
        //moteur.activateLog();
        
        Echiquier e = Echiquier.ofFen("2k5/1p2b1Np/3p3n/3PK3/r7/8/8/7N w - - 0 53");
        
        System.out.println("eval: "+evaluation.evalue(e));
        
        System.out.println(e.printTable());
        
        System.out.println("liste des coups:");
        List<Coup> coups = e.listeCoups();
        coups.forEach(c -> System.out.println("> "+c.toUci()));

        //moteur.adaptProfondeur(e, new int[] {9999, 9999, 9999, 9999, 500, 300});
        
        Coup coup = moteur.meilleurCoup(e);
       
        System.out.println("Meilleur Coup:" + coup.toUci());
    }

}
