package commons.graphes;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Resolveur de plan à l'aide du GrapheResolveur.
 *
 */
class PlanResolveur {
    
    private static final char MUR = '#';
    private static final char LIBRE = '.';
    
    char[][] points;
    private int nbPoints;
    private int hauteur;
    private int largeur;
    private GrapheResolveur graphe = null;
    

    public PlanResolveur(String[] lignes) {
        points = new char[lignes.length][lignes[0].length()];
        hauteur = lignes.length;
        largeur = lignes[0].length();
        
        for (int i = 0; i < hauteur; i++) {
            points[i] = lignes[i].toCharArray();
        }

        nbPoints = hauteur * largeur;
    }
    
    private void generationGraphe() {
        
        graphe = new GrapheResolveur(nbPoints);
        
        for (int l = 0; l < hauteur-1; l++) {
            for (int c = 0; c < largeur-1; c++) {
                if (points[l][c] != MUR) {
                    if (points[l+1][c] != MUR) graphe.ajoutArc(getIdNoeud(l, c), getIdNoeud(l+1, c));
                    if (points[l][c+1] != MUR) graphe.ajoutArc(getIdNoeud(l, c), getIdNoeud(l, c+1));
                }
            }
        }
        
    }
    
    public int getIdNoeud(int l, int c) {
        return l * largeur + c; 
    }
    
    public int[] reverseIdNoeud(int i) {
        int c = i % largeur;
        int l = (i - c) / largeur;
        return new int[] {l, c}; 
    }
    
    public LinkedList<int[]> cheminLePlusCourt(int fromL, int fromC, int toL, int toC) {
        
        if (graphe == null) generationGraphe();
        LinkedList<Integer> chemin = graphe.cheminLePlusCourt(getIdNoeud(fromL, fromC), getIdNoeud(toL, toC));
        
        LinkedList<int[]> ret = new LinkedList<>();
        chemin.forEach(noeud -> ret.add(reverseIdNoeud(noeud)));
        return ret;

    }
    
    public LinkedList<String> listeOrdres(LinkedList<int[]> chemin) {
        
        LinkedList<String> ordres = new LinkedList<>();
        for (int i=0; i<chemin.size()-1; i++) {
            if (chemin.get(i)[0] + 1 == chemin.get(i+1)[0]) ordres.add("DOWN");
            if (chemin.get(i)[0] - 1 == chemin.get(i+1)[0]) ordres.add("UP");
            if (chemin.get(i)[1] - 1 == chemin.get(i+1)[1]) ordres.add("LEFT");
            if (chemin.get(i)[1] + 1 == chemin.get(i+1)[1]) ordres.add("RIGHT");
        }
        return ordres;
        
    }
    
    /**
     * Répond vrai si le sommet peut être atteint à partir d'un autre. 
     */
    public boolean estAtteint(int fromL, int fromC, int toL, int toC) {
            
        if (graphe == null) generationGraphe();
        return graphe.estAtteint(getIdNoeud(fromL, fromC), getIdNoeud(toL, toC));
        
    }
    
    /**
     * Distance pour aller d'un sommet à un autre. 
     */
    public int distance(int fromL, int fromC, int toL, int toC) {
        
        if (graphe == null) generationGraphe();
        return graphe.distance(getIdNoeud(fromL, fromC), getIdNoeud(toL, toC));
        
    }
    
    /**
     * Liste des sommets atteignables. 
     */
    public ArrayList<int[]> sommetsAtteignables(int fromL, int fromC) {
        
        if (graphe == null) generationGraphe();
        ArrayList<Integer> sommets = graphe.sommetsAtteignables(getIdNoeud(fromL, fromC));
        ArrayList<int[]> ret = new ArrayList<>();
        sommets.forEach(s -> ret.add(reverseIdNoeud(s)));
        return ret;
        
    }

}