package ordije;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class MoteurTest {

    /**
     * 1k6/8/2Q4R/1p1B4/1P2P1P1/8/P5P1/R2KN1N1 w A - 0 26
     * en jouant dame c6c5, les noirs n'ont plus de coups à jouer.
     */
    
    @Test
    public void testMoteur() {
        BibliothequeOuverturesChess360.activateLogDebug();
        BibliothequeOuverturesChess360.activateLog();
        Moteur.activateLogDebug();
        Moteur.activateLog();

        Echiquier e = Echiquier.ofFen("1q2r1kb/2n1p3/1p6/1P2Q3/6P1/2P1BR2/7P/NR2K2B w - - 1 27");
       
        Evaluateur evaluateur = new Evaluateur(e);
        
        Moteur moteur = new Moteur(2);
        //moteur.activateLog();
        
        System.out.println("eval: "+evaluateur.evalue(e)); //, evaluation, null);
        
        System.out.println(e.printTable());
        
        System.out.println("liste des coups:");
        List<Coup> coups = e.listeCoups();
        coups.forEach(c -> System.out.println("> "+c.toUci()));

        Map<String, Integer> positionsDejaJoués = new HashMap<>();
        Coup coup = moteur.meilleurCoup(e, 5000, evaluateur, positionsDejaJoués);
       
        System.out.println("Meilleur Coup:" + coup.toUci());
    }

}
