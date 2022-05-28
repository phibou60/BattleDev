package commons.graphes;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import org.junit.Test;

public class PlanResolveurTest {

    @Test
    public void testPlanResolveur() {
        PlanResolveur plan = new PlanResolveur(new String[] {
          /* 0123456789 */      
            "##########", /* 0 */
            "#....#...#", /* 1 */
            "#....#.#.#", /* 2 */
            "#......#.#", /* 3 */
            "#......#.#", /* 4 */
            "########.#", /* 5 */
            "#........#", /* 6 */
            "##########"  /* 7 */
        });
        
        assertEquals(0, plan.getIdNoeud(0, 0));
        assertEquals(12, plan.getIdNoeud(1, 2));
        assertEquals("[1, 2]", Arrays.toString(plan.reverseIdNoeud(12)));
        
        assertEquals(73, plan.getIdNoeud(7, 3));
        assertEquals("[7, 3]", Arrays.toString(plan.reverseIdNoeud(73)));
        assertEquals('#', plan.points[7][3]);
        
        LinkedList<int[]> chemin = plan.cheminLePlusCourt(1, 2, 6, 3);
        chemin.forEach(noeud -> System.out.println(Arrays.toString(noeud)));
        assertEquals(21, chemin.size());
        assertEquals("[1, 2]", Arrays.toString(chemin.getFirst()));
        assertEquals("[6, 3]", Arrays.toString(chemin.getLast()));
        
        LinkedList<String> ordres = plan.listeOrdres(chemin);
        ordres.forEach(System.out::println);
        
        assertTrue(plan.estAtteint(1, 2, 6, 3));
        assertFalse(plan.estAtteint(1, 2, 4, 7));
        
        ArrayList<int[]> sommetsAtteignables = plan.sommetsAtteignables(1, 2);
        sommetsAtteignables.forEach(s -> System.out.println(Arrays.toString(s)));
        
    }

}
