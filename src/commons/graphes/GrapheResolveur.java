package commons.graphes;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
