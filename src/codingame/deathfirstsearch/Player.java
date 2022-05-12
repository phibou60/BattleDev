package codingame.deathfirstsearch;

import java.io.InputStream;
import java.util.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {

    static int nbNoeuds = 0;
    static int nbArretes = 0;
    static int[][] graphe = null;
    static int[] distances = null; 
    
    static int nbExits = 0;
    static int[] exits = null; 
    
    void joue(InputStream inStream) {
        boolean debug = true;
        
        Scanner in = new Scanner(System.in);
        nbNoeuds = in.nextInt(); // the total number of nodes in the level, including the gateways
        nbArretes = in.nextInt(); // the number of links
        nbExits = in.nextInt(); // the number of exit gateways
        if (debug) System.err.println(""+nbNoeuds+" "+nbArretes+" "+nbExits);
        
        graphe = new int[nbArretes][4];
        exits = new int[nbExits];
        
        for (int i = 0; i < nbArretes; i++) {
            int n1 = in.nextInt(); // N1 and N2 defines a link between these nodes
            int n2 = in.nextInt();
            if (debug) System.err.println(""+n1+" "+n2);
            graphe[i] = new int[] {n1, n2, 1, 1};
        }
        
        for (int i = 0; i < nbExits; i++) {
            exits[i] = in.nextInt(); // the index of a gateway node
            if (debug) System.err.println(""+exits[i]);
        }

        // game loop
        while (true) {
            int n = in.nextInt(); // The index of the node on which the Skynet agent is positioned this turn
            if (debug) System.err.println(""+n);
            
            int from = 0;
            int to = 0;
            int min = Integer.MAX_VALUE;
            
            for (int i = 0; i < nbExits; i++) {
                ArrayList<Integer> parcours = calculParcoursLePlusCourt(n, exits[i]);
                if (parcours != null) {
                    if (debug) System.err.println("parcours: "+toString(parcours));
                    if (parcours.size() < min) {
                        min = parcours.size();
                        from = parcours.get(parcours.size()-2);
                        to = parcours.get(parcours.size()-1);
                    }
                }
            }
            
            // Supprimer l'arrete dans le graphe
            for (int i=0; i<nbArretes; i++) {
                if ((graphe[i][0] == from && graphe[i][1] == to)
                 || (graphe[i][1] == from && graphe[i][0] == to)) {
                    graphe[i][2] = -1;
                    graphe[i][3] = -1;
                }
            }

            System.out.println(from+" "+to);
        }
        
    }
    
    static ArrayList<Integer> calculParcoursLePlusCourt(int depart, int dest) {
        distances = new int[nbNoeuds];
        Arrays.fill(distances, Integer.MAX_VALUE);

        ArrayList<Integer> parcours = new ArrayList<>();
        parcours.add(depart);
        return calculParcoursLePlusCourt(parcours, dest);
    }
    
    // Attention considère distance = 1
    static ArrayList<Integer> calculParcoursLePlusCourt(ArrayList<Integer> parcours, int dest) {
        boolean debug = false;
        if (debug) System.err.println("Parcours: "+toString(parcours));
        int dernierNoeud = parcours.get(parcours.size()-1);
        ArrayList<int[]> noeudsSuivants = getNoeudsSuivantsAvecLaDistance(dernierNoeud);
        if (debug) System.err.println(" > noeudsSuivants: "+toString(noeudsSuivants));

        for (int[] suiv : noeudsSuivants) {
            if (suiv[0] == dest) {
                parcours.add(dest);
                return parcours;
            }
        }
        
        ArrayList<Integer> meilleurParcours = null;
        for (int[] suiv : noeudsSuivants) {
            if (!parcours.contains(suiv[0])) { // Pas très utile sauf pour le debug !
                if (parcours.size() < distances[suiv[0]]) {
                    distances[suiv[0]] = parcours.size();
                    if (debug) System.err.println(" > min distance du noeud ("+suiv[0]+") = "+parcours.size());

                    ArrayList<Integer> nouvParcours = clone(parcours);
                    nouvParcours.add(suiv[0]);
                    ArrayList<Integer> trouveParcours = calculParcoursLePlusCourt(nouvParcours, dest);
                    if (trouveParcours != null && trouveParcours.get(trouveParcours.size()-1) == dest) {
                        if (meilleurParcours == null || meilleurParcours.size() > trouveParcours.size()) {
                            meilleurParcours = trouveParcours;
                        }
                    }

                } else {
                    if (debug) System.err.println(" > deja passé par le noeud : "+suiv[0]+" avec une distance de "+distances[suiv[0]]);
                }
            } else {
                if (debug) System.err.println(" > noeud "+suiv[0]+" déjà dans le parcours");
            }
        }
        
        return meilleurParcours;
    }
    
    static ArrayList<int[]> getNoeudsSuivantsAvecLaDistance(int n) {
        ArrayList<int[]> ret = new ArrayList<>();
        for (int i=0; i<nbArretes; i++) {
            if (graphe[i][2] > -1 && graphe[i][0] == n) ret.add(new int[] {graphe[i][1], graphe[i][2]});
            if (graphe[i][3] > -1 && graphe[i][1] == n) ret.add(new int[] {graphe[i][0], graphe[i][3]});
        }
        return ret;
    }
    
    static ArrayList<Integer> clone(ArrayList<Integer> list) {
        ArrayList<Integer> ret = new ArrayList<>();
        for (Integer i : list) {
            ret.add(i);
        }
        return ret;
    }
    
    static String toString(ArrayList<int[]> list) {
        if (list == null) return "null";
        
        String ret = "("+list.size()+")";
        for (int[] o : list) {
            ret += (ret.length()>0?"; ":"")+Arrays.toString(o);
        }
        return ret;
    }
    
    static String toString(List list) {
        if (list == null) return "null";
    
        String ret = "";
        for (Object i : list) {
            ret += (ret.length()>0?"; ":"")+i;
        }
        return ret;
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