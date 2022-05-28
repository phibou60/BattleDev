
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
    static boolean doLog = true;
    static boolean doLogDebug = false;

    int rowsNb;
    int columnsNb;
    int alarmRoundsNb;
    LinkedList<String> ordres = null;
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);

        rowsNb = in.nextInt(); // number of rows.
        columnsNb = in.nextInt(); // number of columns.
        alarmRoundsNb = in.nextInt(); // number of rounds between the time the alarm countdown is activated and the time the alarm goes off.
        System.err.println(rowsNb + " " + columnsNb + " " + alarmRoundsNb);
        
        // game loop
        while (true) {
            Etat etat = new Etat(rowsNb);
            etat.rickL = in.nextInt(); // row where Rick is located.
            etat.rickC = in.nextInt(); // column where Rick is located.
            System.err.println(etat.rickL + " " + etat.rickC);
            
            for (int i = 0; i < rowsNb; i++) {
                etat.plan[i] = in.next(); // C of the characters in '#.TC?' (i.e. one line of the ASCII maze).
                System.err.println(etat.plan[i]);
            }

            String[] plan2 = new String[rowsNb];
            for (int i = 0; i < rowsNb; i++) plan2[i] = etat.plan[i].replace('?', '#');
            
            etat.resolver = new PlanResolveur(plan2);
            
            strategie(etat);
            
            System.out.println(etat.ordre); // Rick's next move (UP DOWN LEFT or RIGHT).
        }
    }
    
    private void strategie(Etat etat) {
        
        // Y a-t-il une liste d'ordres à effectuer ?
        if (ordres != null && ordres.size() > 0) {
            log("depille ordre");
            etat.ordre = ordres.poll();
            return;
        }
        
        int[] tCoords = recherche(etat, 'T');

        // Est-on sur le 'C' ?
        // Si oui alors faire le liste d'ordres vers le 'T'
        
        if (etat.resolver.points[etat.rickL][etat.rickC] == 'C') {
            log("retour en T");
            LinkedList<int[]> chemin = etat.resolver.cheminLePlusCourt(etat.rickL, etat.rickC, tCoords[0], tCoords[1]);
            chemin.forEach(noeud -> logDebug(Arrays.toString(noeud)));
            ordres = etat.resolver.listeOrdres(chemin);
            ordres.forEach(Player::logDebug);
            etat.ordre = ordres.poll();
            return;
        }
        
        // l'objectif est-il dans le plan ?
        int[] cCoords = recherche(etat, 'C');
        
        // Si non alors il faut le chercher
        if (cCoords == null) {
            strategieRecherche(etat, null);
            return;
        }
        
        // Si oui, est-il atteignable ?
        LinkedList<int[]> chemin = etat.resolver.cheminLePlusCourt(etat.rickL, etat.rickC, cCoords[0], cCoords[1]);
        if (chemin.size() == 0) {
            // Il n'est pas atteignable alors il faut chercher une route vers lui
            strategieRecherche(etat, cCoords);
            return;
        } else {
            // S'il est atteignable alors faire la route et suivre
            
            // Il faut d'abord vérifier que l'on pourra faire le trajet retour dans le temps imparti
            // sinon il faut continuer à explorer le labyrinthe.
            
            int distanceCversT = etat.resolver.distance(cCoords[0], cCoords[1], tCoords[0], tCoords[1]);
            log("distanceCversT:", distanceCversT, "vs alarmRoundsNb:", alarmRoundsNb);
            if (distanceCversT > alarmRoundsNb) {
                strategieRecherche(etat, null);
                return;
            }

            log("chemin vers C:", chemin.size());
            chemin.forEach(noeud -> logDebug(Arrays.toString(noeud)));
            
            ordres = etat.resolver.listeOrdres(chemin);
            ordres.forEach(Player::logDebug);
            etat.ordre = ordres.poll();
            return;

        }
        
    }

    private void strategieRecherche(Etat etat, int[] cCoords) {
        log("strategieRecherche");
        
        ArrayList<int[]> sommetsAtteignables = etat.resolver.sommetsAtteignables(etat.rickL, etat.rickC);
        
        int[] distanceTo;
        if (cCoords != null) {
            distanceTo = cCoords;
        } else {
            distanceTo = new int[] {etat.rickL, etat.rickC};
        }
        
        double minDistance = Double.MAX_VALUE;
        int[] coordsLesPlusProches = null;
        
        for (int i=0; i<sommetsAtteignables.size(); i++) {
            logDebug("---- sommet atteignable:", Arrays.toString(sommetsAtteignables.get(i)));
            
            int nbInterrogations = calculNbInterrogations(etat, sommetsAtteignables.get(i));
            
            logDebug("> nbInterrogations:", nbInterrogations);
            
            if (nbInterrogations == 0) continue;
            
            int deltaL = etat.rickL - sommetsAtteignables.get(i)[0];
            int deltaC = etat.rickC - sommetsAtteignables.get(i)[1];
            
            double distance = deltaL * deltaL + deltaC * deltaC;
            
            logDebug("> distance:", distance);

            if (distance < minDistance) {
                minDistance = distance;
                coordsLesPlusProches = sommetsAtteignables.get(i);
                if (distance == 1) {
                    logDebug("> break");
                    break;
                }
            }
        }

        LinkedList<int[]> chemin = etat.resolver.cheminLePlusCourt(etat.rickL, etat.rickC, coordsLesPlusProches[0], coordsLesPlusProches[1]);
        chemin.forEach(noeud -> logDebug(Arrays.toString(noeud)));
        ordres = etat.resolver.listeOrdres(chemin);
        ordres.forEach(Player::logDebug);
        etat.ordre = ordres.poll();
        
    }
    
    int calculNbInterrogations(Etat etat, int[] coords) {
        int cumul = 0;
        for (int l=Math.max(0, coords[0]-2); l<=Math.min(rowsNb-1, coords[0]+2); l++) {
            for (int c=Math.max(0, coords[1]-2); c<=Math.min(columnsNb-1, coords[1]+2); c++) {
                if (etat.plan[l].charAt(c) == '?') cumul++; 
            }
        }
        return cumul;
    }

    /**
     * Recherche un caractère dans le plan
     * @return Les coordonnées du point sinon null.
     */
    int[] recherche(Etat etat, char ch) {
        for (int l = 0; l < rowsNb; l++) {
            for (int c = 0; c < columnsNb; c++) {
                if (etat.plan[l].charAt(c) == ch) {
                    return new int[] {l, c};
                }
            }
        }
        return null;
    }
    
    class Etat {
        int rickL;
        int rickC;
        String[] plan;
        PlanResolveur resolver;
        String ordre = null;
        
        Etat(int rowsNb) {
            plan = new String[rowsNb];
        }
    }

    /* Codingame common */
    
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
    
    static void logDebug(Object... objects) {
        if (doLogDebug) {
            System.err.print("*");
            for (Object o : objects) {
                System.err.print(o.toString() + " ");
            }
            System.err.println();
        }
    }
    
    static void activateLogDebug() {
        doLogDebug = true;
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
        
        System.err.println("+++++++generationGraphe");
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
