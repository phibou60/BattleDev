package commons.graphes;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Test;

public class GrapheResolveurTest {

    @Test
    public void testGrapheResolveur() {
        // voir vidéo : https://www.youtube.com/watch?v=rHylCtXtdNs
        GrapheResolveur gr = new GrapheResolveur(7);
        gr.ajoutArc(0, 1, 1); // ab1
        gr.ajoutArc(0, 2, 2); // ac2
        gr.ajoutArc(1, 5, 3); // bf3
        gr.ajoutArc(1, 3, 2); // bd2
        gr.ajoutArc(2, 3, 3); // cd3
        gr.ajoutArc(2, 4, 4); // ce4
        gr.ajoutArc(3, 5, 3); // df3 
        gr.ajoutArc(3, 4, 2); // de2
        gr.ajoutArc(5, 6, 4); // fg4 
        gr.ajoutArc(3, 6, 3); // dg3 
        gr.ajoutArc(4, 6, 5); // eg5
        
        int lg = gr.cheminLePlusCourt(0, 6);
        assertEquals(6, lg);
        
        System.out.println("chemin:");
        LinkedList<Integer> chemin = gr.chemin();
        chemin.forEach(System.out::println);
        assertEquals(0, chemin.get(0).intValue());
        assertEquals(2, chemin.get(1).intValue());
        assertEquals(3, chemin.get(2).intValue());
        assertEquals(6, chemin.get(3).intValue());
    }

}
