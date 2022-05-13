package codingame.deathfirstsearch;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    int nbSommets = 0;
    int nbArcs = 0;
    
    int nbExits = 0;
    int[] exits = null;
    
    GrapheResolveur gr;
    
    // Solution
    int from;
    int to;
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);
        boolean debug = true;
        
        nbSommets = in.nextInt(); // the total number of nodes in the level, including the gateways
        nbArcs = in.nextInt(); // the number of links
        nbExits = in.nextInt(); // the number of exit gateways
        System.err.println(""+nbSommets+" "+nbArcs+" "+nbExits);
        
        gr = new GrapheResolveur(nbSommets);
        
        for (int i = 0; i < nbArcs; i++) {
            int n1 = in.nextInt(); // N1 and N2 defines a link between these nodes
            int n2 = in.nextInt();
            System.err.println(""+n1+" "+n2);
            
            gr.ajoutArc(n1, n2);
        }
        
        exits = new int[nbExits];
        
        for (int i = 0; i < nbExits; i++) {
            exits[i] = in.nextInt(); // the index of a gateway node
            System.err.println(""+exits[i]);
        }

        // game loop
        while (true) {
            int n = in.nextInt(); // The index of the node on which the Skynet agent is positioned this turn
            System.err.println(""+n);
            
            strategie(n);
            
            System.out.println(from+" "+to);
            gr.suppressArc(from, to);
        }
        
    }
        
    private void strategie(int n) {
        
        // Recherche de la sortie la plus proche pour le virus et blocage du dernier arc
        // qui permet d'atteindre cette sortie.
        
        int lgLePlusCourt = Integer.MAX_VALUE;
        LinkedList<Integer> cheminLePlusCourt = null;
        
        for (int i = 0; i < exits.length; i++) {
            int lg = gr.cheminLePlusCourt(n, exits[i]);
            if (lg < lgLePlusCourt) {
                log("cheminLePlusCourt:", lg);
                cheminLePlusCourt = gr.chemin();
                lgLePlusCourt = lg;
            }
        }
        
        log("chemin:");
        cheminLePlusCourt.forEach(Player::log);
        
        to = cheminLePlusCourt.pollLast();
        from = cheminLePlusCourt.pollLast();
    }
    
    /* Codingame common */
    
    static boolean doLog = true;
    static void log(Object... objects) {
        if (doLog) {
            for (Object o : objects) {
                System.err.print(o.toString() + " ");
            }
            System.err.println();
        }
    }

    static void activateLog() {
        doLog = true;
    }
    
    public static void main(String args[]) {
        Player player = new Player();
        player.joue(System.in);
    }

}

/**
 * Gestion des graphes avec algo de Dijkstra.
 *
 */
class GrapheResolveur {

    int nbSommets;
    LinkedList<int[]> arcs = new LinkedList<>();
    
    // Données de recherche du chemin le plus court
    int from;
    int to;
    int[] minDistance;
    int[] predecesseurs;

    public GrapheResolveur(int nbSommets) {
        super();
        this.nbSommets = nbSommets;
    }
    
    /**
     * Ajout arc bidirectionnel de longueur 1.
     */
    public void ajoutArc(int from, int to) {
        int[] arc = new int[4];
        arc[0] = from;
        arc[1] = to;
        arc[2] = 1;
        arc[3] = 1;
        arcs.add(arc);
    }

    /**
     * Ajout arc bidirectionnel de longueur "length".
     */
    public void ajoutArc(int from, int to, int length) {
        int[] arc = new int[4];
        arc[0] = from;
        arc[1] = to;
        arc[2] = length;
        arc[3] = length;
        arcs.add(arc);
    }
    
    /**
     * Ajout arc bidirectionnel avec un longueur "length" à l'aller et "lengthReturn" au retour.
     */
    public void ajoutArc(int from, int to, int length, int lengthReturn) {
        int[] arc = new int[4];
        arc[0] = from;
        arc[1] = to;
        arc[2] = length;
        arc[3] = lengthReturn;
        arcs.add(arc);
    }
    
    /**
     * Algo de Dijkstra.
     */
    public int cheminLePlusCourt(int from, int to) {
        this.from = from;
        this.to = to;

        // Initialisation
        
        boolean[] dejaVu = new boolean[nbSommets];
        minDistance = new int[nbSommets];
        predecesseurs = new int[nbSommets];
        
        for (int i = 0; i < nbSommets; i++) {
            dejaVu[i] = false;
            minDistance[i] = Integer.MAX_VALUE;
            predecesseurs[i] = -1;
        }
        
        // Départ du sommet "from"
        
        minDistance[from] = 0;
        int a = -1;
        
        // Boucle d'étude du sommet "a"
        
        while ((a = choisirSommetPlusPetiteDistancePasDejaVu(dejaVu, minDistance)) != -1) {
            dejaVu[a] = true;
            List<int[]> arcsVersS = arcsAPartirDe(a);
            arcsVersS.forEach(arc -> {
                int b = arc[1];
                if (minDistance[b] > minDistance[arc[0]] + arc[2]) {
                    minDistance[b] = minDistance[arc[0]] + arc[2];
                    predecesseurs[b] = arc[0];
                }
            });
        }

        return minDistance[to];
    }

    /**
     * Récupération de la liste de sommets du plus petit chemin. 
     */
    LinkedList<Integer> chemin() {
    
        LinkedList<Integer> ret = new LinkedList<>();
        ret.add(to);
        int s = to;
        while (s != -1 && s != from) {
            s = predecesseurs[s];
            ret.addFirst(s);
        }
        return ret;
        
    }
    
    /**
     * Sélectionner le sommet de plus petite distance pas déjà visité.  
     */
    int choisirSommetPlusPetiteDistancePasDejaVu(boolean[] dejaVu, int[] minDistance) {
        int selection = -1;
        int distance = Integer.MAX_VALUE;
        
        for (int i = 0; i < nbSommets; i++) {
            if (!dejaVu[i] && minDistance[i] < distance) {
                selection = i;
                distance = minDistance[i];
            }
        }
        
        return selection;
    }
    
    /**
     * Recherche des arcs qui partent d'un sommet.
     */
    
    private List<int[]> arcsAPartirDe(int s) {
        return arcs.stream()
            .filter(arc -> (arc[0] == s && arc[2] > 0) || (arc[1] == s && arc[3] > 0))
            .map(arc -> {
                if (arc[0] == s) {
                    return arc;
                } else {
                    int[] newArc = new int[3];
                    newArc[0] = arc[1];
                    newArc[1] = arc[0];
                    newArc[2] = arc[3];
                    return newArc;
                }
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Suppression d'un arc (dans les 2 sens).
     */
    void suppressArc(int from, int to) {
        
        int selection = -1;
        
        for (int i=0; i<arcs.size(); i++) {
            int[] arc = arcs.get(i);
            if ((arc[0] == from && arc[1] == to) || (arc[0] == to && arc[1] == from)) {
                selection = i;
            }
        }
        arcs.remove(selection); 
   }
   
}
