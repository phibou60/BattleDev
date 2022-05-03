/*******
 * Read input from System.in
 * Use: System.out.println to ouput your result to STDOUT.
 * Use: System.err.println to ouput debugging information to STDERR.
 * ***/
package commons.divers;
import java.util.*;
/*
5 11
;*_\^^^^&_"
}_@*\\^&&;;
+[[*\\'&&;;
-=,*\\$[!;;
"=>{\\}:$;;

 */
public class IsoContest06ReconnaissanceForme {
	static int forme1[][] = {{0,0},{0,1},{0,2},{0,3},{1,0},{1,1},{1,2},{1,3}};
	static int contour1[][] = {{-1,0},{-1,1},{-1,2},{-1,3}, {0,-1},{0,4}, {1,-1},{1,4}, {2,0},{2,1},{2,2},{2,3}};
	static int forme2[][] = {{0,0},{0,1},{1,0},{1,1},{2,0},{2,1},{3,0},{3,1}};
	static int contour2[][] = {{-1,0},{-1,1}, {0,-1},{0,2}, {1,-1},{1,2}, {2,-1},{2,2}, {3,-1},{3,2}, {4,0},{4,1}};
	static char[] acceptedChars = "!\"#$%&'()*+,-./:;<=>?@[]\\^_`{|}~".toCharArray();

	static int nbLignes = 0;
	static int nbCols = 0;
	static char[][] cases = null;
	
	public static void main( String[] argv ) throws Exception {
		System.err.println("--------------------------------------------------");
		int count = 0; // ligne en cours -1
		int countMax = 2; // nb lignes max
			
		String line;
		Scanner sc = new Scanner(System.in);
		while(count < countMax && sc.hasNextLine()) {
			line = sc.nextLine();
			System.err.println("line("+(count+1)+"): "+line);
			if (count == 0) {
				String[] splits = line.split(" ");
				nbLignes = Integer.parseInt(splits[0]);
				nbCols = Integer.parseInt(splits[1]);
				countMax = nbLignes+1;
				cases = new char[nbLignes][nbCols];
			} else {
				cases[count-1] = line.toCharArray();
			}
			count++;
		}
		sc.close();
		
		// Calcul ...
		
		int found = 0;
		StringBuffer ret = new StringBuffer();
		
		Arrays.sort(acceptedChars);
		for (int l=0; l<nbLignes-1; l++) {
			for (int c=0; c<nbCols-1; c++) {
				char ch = cases[l][c];
				boolean accepted = (Arrays.binarySearch(acceptedChars, ch)>-1?true:false);
				//System.err.println("ligne: "+l+", col:"+c+" = "+cases[l][c]+", accepted: "+accepted);

				if (accepted
				&& ( (formeContientUniquementChar(cases, l, c, forme1, ch) && contourNeContientPasChar(cases, l, c, contour1, ch))
				  || (formeContientUniquementChar(cases, l, c, forme2, ch) && contourNeContientPasChar(cases, l, c, contour2, ch))
					)) {
					ret.append(""+(l+1)+" "+(c+1)+"\n");
					found++;
					System.err.println(""+(l+1)+" "+(c+1));
				}
			}
		}
		
		System.out.println(found);
		System.out.println(ret.toString());
	}
	
	static boolean formeContientUniquementChar(char[][] cases, int l, int c, int[][] forme, char ch) {
		for (int i = 0; i<forme.length; i++) {
			int ll = l + forme[i][0];
			int cc = c + forme[i][1];
			if (ll >= 0 && ll < cases.length && cc >= 0 && cc < cases[l].length) {
				if (cases[ll][cc] != ch) return false;
			} else  return false;
		}
		return true;
	}
	
	static boolean contourNeContientPasChar(char[][] cases, int l, int c, int[][] forme, char ch) {
		for (int i = 0; i<forme.length; i++) {
			int ll = l + forme[i][0];
			int cc = c + forme[i][1];
			if (ll >= 0 && ll < cases.length && cc >= 0 && cc < cases[l].length) {
				if (cases[ll][cc] == ch) return false;
			}
		}
		return true;
	}
	
}