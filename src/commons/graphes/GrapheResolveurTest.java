package commons.graphes;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class GrapheResolveurTest {

    @Test
    public void testGrapheResolveur() {
        // voir vidéo : https://www.youtube.com/watch?v=rHylCtXtdNs
        // et doc graphes.docx.
        
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
        
        LinkedList<Integer> chemin = gr.cheminLePlusCourt(0, 6);
        
        assertEquals(6, gr.distance(0, 6));
        
        System.out.println("chemin:");
        chemin.forEach(System.out::println);
        assertEquals(0, chemin.get(0).intValue());
        assertEquals(1, chemin.get(1).intValue());
        assertEquals(3, chemin.get(2).intValue());
        assertEquals(6, chemin.get(3).intValue());
        
        // Si on supprime dg :
        
        gr.suppressArc(3, 6);
        
        chemin = gr.cheminLePlusCourt(0, 6);
        assertEquals(8, gr.distance(0, 6));
        
        //System.out.println("chemin:");
        //chemin.forEach(System.out::println);
        assertEquals(0, chemin.get(0).intValue());
        assertEquals(1, chemin.get(1).intValue());
        assertEquals(5, chemin.get(2).intValue());
        assertEquals(6, chemin.get(3).intValue());
        
        List<Integer> sommetsAtteignables = gr.sommetsAtteignables(0);
        System.out.println("sommetsAtteignables:");
        sommetsAtteignables.forEach(System.out::println);
        
    }

}
