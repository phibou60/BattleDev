/*******
 * Read input from System.in
 * Use: System.out.println to ouput your result to STDOUT.
 * Use: System.err.println to ouput debugging information to STDERR.
 * ***/
/*
10 12 4
1 2
2 7
4 7
4 6
5 6
5 7
2 10
3 8
9 10
3 9
7 8
5 8

ret: 9
 */
package commons.graphes;
import java.util.*;

public class IsoContest05ReseauLePlusLoin {
	static int nbNoeuds = 0;
	static int nbArretes = 0;
	static int[][] graphe = null;
	static int[] distances = null; 

	static int posteAgent = 0;
	
	public static void main( String[] argv ) throws Exception {
		System.err.println("--------------------------------------------------");
		int count = 0;
		int countMax = 2; // nb lignes max
		String ret = "";
		
		String  line;
		Scanner sc = new Scanner(System.in);
		while(count < countMax && sc.hasNextLine()) {
			line = sc.nextLine();
			System.err.println("line("+(count+1)+"): "+line);
			if (count == 0) {
				String[] splits = line.split(" ");
				nbNoeuds = Integer.parseInt(splits[0]);
				nbArretes = Integer.parseInt(splits[1]);
				posteAgent = Integer.parseInt(splits[2]);
				countMax = nbArretes + 1;
				graphe = new int[nbArretes][3];
			} else {
				String[] splits = line.split(" ");
				graphe[count-1] = new int[] {Integer.parseInt(splits[0]), Integer.parseInt(splits[1]), 1};
			}
			count++;
		}
		sc.close();
		
		// Calcul ...
		
		distances = new int[nbNoeuds];
		Arrays.fill(distances, Integer.MAX_VALUE);
		distances[posteAgent-1] = 0;

		ArrayList<Integer> parcours = new ArrayList<>();
		parcours.add(posteAgent);
		calculNoeudLePlusLoin(parcours);
		
		int distanceMax = 0;
		for (int i=0; i<distances.length; i++) {
			if (distances[i] > distanceMax) distanceMax = distances[i];
		}
		System.err.println("distanceMax: "+distanceMax);
		for (int i=0; i<distances.length; i++) {
			if (distances[i] == distanceMax) ret += (ret.length()>0?" ":"")+(i+1);
		}
		
		System.err.println("ret: "+ret);
		System.out.println(ret);

	}
	
	static void calculNoeudLePlusLoin(ArrayList<Integer> parcours) {
		boolean debug = false;
		if (debug) System.err.println("Parcours arbre: "+toString(parcours));
		int dernierNoeud = parcours.get(parcours.size()-1);
		ArrayList<Integer> noeudsSuivants = getNoeudsSuivants(dernierNoeud);
		if (debug) System.err.println(" > noeudsSuivants: "+toString(noeudsSuivants));
		
		ArrayList<Integer> noeudsSuivantsNonVisites = new ArrayList<>();
		for (Integer s : noeudsSuivants) {
			if (!parcours.contains(s)) {
				if (distances[s-1] > parcours.size()) {
					distances[s-1] = parcours.size();
					if (debug) System.err.println(" > min distance du noeud ("+s+") = "+parcours.size());
					noeudsSuivantsNonVisites.add(s);
				} else {
					if (debug) System.err.println(" > deja passé par le noeud : "+s+" avec une distance de "+distances[s-1]);
				}
			} else {
				if (debug) System.err.println(" > noeud "+s+" déjà dans le parcours");
			}
		}
		for (Integer i : noeudsSuivantsNonVisites) {
			ArrayList<Integer> cloneParcours = clone(parcours);
			cloneParcours.add(i);
			calculNoeudLePlusLoin(cloneParcours);
		}
	}
	
	static ArrayList<Integer> getNoeudsSuivants(int n) {
		ArrayList<Integer> ret = new ArrayList<>();
		for (int i=0; i<nbArretes; i++) {
			if (graphe[i][0] == n) ret.add(graphe[i][1]);
			if (graphe[i][1] == n) ret.add(graphe[i][0]);
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
	
	static String toString(List list) {
		if (list == null) return "null";
		
		String ret = "";
		for (Object i : list) {
			ret += (ret.length()>0?"; ":"")+i;
		}
		return ret;
	}
}