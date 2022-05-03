/*******
 * Read input from System.in
 * Use: System.out.println to ouput your result to STDOUT.
 * Use: System.err.println to ouput debugging information to STDERR.
 * ***/
/*
10 8
2 9
9 3
3 7
7 4
9 6
9 1
1 8
10 5

ret: 2
 */

package commons.graphes;
import java.util.*;

public class IsoContest05ReseauNbEnfants {
	static int nbNoeuds = 0;
	static int nbArretes = 0;
	static int[][] graphe = null;
	static int[] nbEnfants = null; 

	public static void main( String[] argv ) throws Exception {
		System.err.println("--------------------------------------------------");
		int count = 0; // ligne en cours -1
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
				countMax = nbArretes+1;
				graphe = new int[nbArretes][3];
				nbEnfants = new int[nbNoeuds];
			} else {
				String[] splits = line.split(" ");
				int from = Integer.parseInt(splits[0]);
				int to = Integer.parseInt(splits[1]);
				graphe[count-1] = new int[] {from, to, 1};
			}
			count++;
		}
		sc.close();
		
		// Calcul ...
		Arrays.fill(nbEnfants, -1);

		int max = 0;
		for (int i=0; i<nbEnfants.length; i++) {
			if (nbEnfants[i] == -1) calculNbNoeudsEnfants(i+1);
			System.err.println("nbEnfants["+i+"] ="+nbEnfants[i]);
			if (nbEnfants[i] > max) {
				max = nbEnfants[i];
				ret = ""+(i+1);
			}
		}
		
		System.err.println("ret: "+ret);
		System.out.println(ret);
	}
	//----------------------------------------------------------------------
	
	static int calculNbNoeudsEnfants(int n) {
		boolean debug = false;
		if (debug) System.err.println("Noeud: "+n);
		if (nbEnfants[n-1] > -1) return nbEnfants[n-1]; 
		ArrayList<Integer> noeudsSuivants = getNoeudsSuivants(n);
		if (debug) System.err.println(" > noeudsSuivants: "+toString(noeudsSuivants));
		
		int ret = 0;
		for (Integer s : noeudsSuivants) {
			ret += calculNbNoeudsEnfants(s)+1;
		}
		nbEnfants[n-1] = ret;
		return ret;
	}
	
	static ArrayList<Integer> getNoeudsSuivants(int n) {
		ArrayList<Integer> ret = new ArrayList<>();
		for (int i=0; i<nbArretes; i++) {
			if (graphe[i][0] == n) ret.add(graphe[i][1]);
		}
		return ret;
	}
	
	static String toString(List list) {
		if (list == null) return "null";
		
		String ret = "";
		for (Object value : list) {
			ret += (ret.length()>0?"; ":"")+value;
		}
		return ret;
	}
	
}