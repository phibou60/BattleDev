/*******
 * Read input from System.in
 * Use: System.out.println to ouput your result to STDOUT.
 * Use: System.err.println to ouput debugging information to STDERR.
 * ***/
package commons.graphes;
import java.util.*;
/*
38 79 3
28 36
0 2
3 34
29 21
37 35
28 32
0 10
37 2
4 5
13 14
34 35
27 19
28 34
30 31
18 26
0 9
7 8
18 24
18 23
0 5
16 17
29 30
10 11
0 12
15 16
0 11
0 17
18 22
23 24
0 7
35 23
22 23
1 2
0 13
18 27
25 26
32 33
28 31
24 25
28 35
21 22
4 33
28 29
36 22
18 25
37 23
18 21
5 6
19 20
0 14
35 36
9 10
0 6
20 21
0 3
33 34
14 15
28 33
11 12
12 13
17 1
18 19
36 29
0 4
0 15
0 1
18 20
2 3
0 16
8 9
0 8
26 27
28 30
3 4
31 32
6 7
37 1
37 24
35 2
0
18
28
37

ret: 2 0
 */
public class CodinGameSkynetRevolution {

	static int nbNoeuds = 0;
	static int nbArretes = 0;
	static int[][] graphe = null;
	static int[] distances = null; 
	
	static int nbExits = 0;
	static int[] exits = null; 
	
    public static void main(String args[]) {
    	boolean debug = true;
    	
        Scanner in = new Scanner(System.in);
        nbNoeuds = in.nextInt(); // the total number of nodes in the level, including the gateways
        nbArretes = in.nextInt(); // the number of links
        nbExits = in.nextInt(); // the number of exit gateways
        if (debug) System.err.println("ligne 1 : "+nbNoeuds+" "+nbArretes+" "+nbExits);
        
        graphe = new int[nbArretes][4];
        exits = new int[nbExits];
        
        for (int i = 0; i < nbArretes; i++) {
            int n1 = in.nextInt(); // N1 and N2 defines a link between these nodes
            int n2 = in.nextInt();
            if (debug) System.err.println("ligne A : "+n1+" "+n2);
            graphe[i] = new int[] {n1, n2, 1, 1};
        }
        
        for (int i = 0; i < nbExits; i++) {
        	exits[i] = in.nextInt(); // the index of a gateway node
        	if (debug) System.err.println("ligne B : "+exits[i]);
        }

        // game loop
        while (true) {
            int n = in.nextInt(); // The index of the node on which the Skynet agent is positioned this turn
            if (debug) System.err.println("ligne C : "+n);
            
            String ret = "";
            int min = Integer.MAX_VALUE;
            
            for (int i = 0; i < nbExits; i++) {
            	ArrayList<Integer> parcours = calculParcoursLePlusCourt(n, exits[i]);
            	if (parcours != null) {
            		if (debug) System.err.println("parcours: "+toString(parcours));
            		if (parcours.size() < min) {
            			min = parcours.size();
            			ret = ""+parcours.get(parcours.size()-2)+" "+parcours.get(parcours.size()-1);
            		}
            	}
            }

            System.out.println(ret);
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
   
}