package commons.graphes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestion des graphes avec algo de Dijkstra.
 *
 */
class GrapheResolveur {
    public static final int PAS_ATTEINT = Integer.MAX_VALUE;
    int nbSommets;
    LinkedList<int[]> arcs = new LinkedList<>();
    
    // Données de recherche du chemin le plus court
    int from = -1;
    int[] minDistance = null;
    int[] predecesseurs = null;

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
        this.from = -1;
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
        this.from = -1;
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
        this.from = -1;
    }
    
    /**
     * Suppression d'un arc (dans les 2 sens).
     */
    public void suppressArc(int from, int to) {
        
        int selection = -1;
        
        for (int i=0; i<arcs.size(); i++) {
            int[] arc = arcs.get(i);
            if ((arc[0] == from && arc[1] == to) || (arc[0] == to && arc[1] == from)) {
                selection = i;
            }
        }
        arcs.remove(selection); 
        this.from = -1;
   }
    
    /**
     * Algo de Dijkstra.
     */
    private void exploreProfondeurAPartirDe(int from) {

        // Initialisation
        
        boolean[] dejaVu = new boolean[nbSommets];
        minDistance = new int[nbSommets];
        predecesseurs = new int[nbSommets];
        
        for (int i = 0; i < nbSommets; i++) {
            dejaVu[i] = false;
            minDistance[i] = PAS_ATTEINT;
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
        this.from = from;
    }
    
    /**
     * Récupération de la liste de sommets qui forment le chemin le plus court. 
     */
    public LinkedList<Integer> cheminLePlusCourt(int from, int to) {
        
        if (minDistance == null || this.from != from) exploreProfondeurAPartirDe(from);
            
        LinkedList<Integer> result = new LinkedList<>();
        if (predecesseurs[to] != -1) { 
            result.add(to);
            int s = to;
            while (s != -1 && s != from) {
                s = predecesseurs[s];
                result.addFirst(s);
            }
        }
        return result;        
    }
    
    /**
     * Répond vrai si le sommet peut être atteint à partir d'un autre. 
     */
    public boolean estAtteint(int from, int to) {
        
        if (minDistance == null || this.from != from) exploreProfondeurAPartirDe(from);
            
        return minDistance[to] != PAS_ATTEINT;
        
    }
    
    /**
     * Distance pour aller d'un sommet à un autre. 
     */
    public int distance(int from, int to) {
        
        if (minDistance == null || this.from != from) exploreProfondeurAPartirDe(from);
            
        return minDistance[to];
        
    }
    
    /**
     * Liste des sommets atteignables. 
     */
    public ArrayList<Integer> sommetsAtteignables(int from) {
        
        if (minDistance == null || this.from != from) exploreProfondeurAPartirDe(from);
        
        ArrayList<Integer> ret = new ArrayList<>();
        for (int i=0; i<minDistance.length; i++) {
            if (minDistance[i] != PAS_ATTEINT) ret.add(i);
        }
        
        return ret;
        
    }
    
    /**
     * Methode interne.<br>
     * Sélectionner le sommet de plus petite distance pas déjà visité.  
     */
    private int choisirSommetPlusPetiteDistancePasDejaVu(boolean[] dejaVu, int[] minDistance) {
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
     * Methode interne.<br>
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
   
}
