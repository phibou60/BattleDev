/*******
 * Read input from System.in
 * Use: System.out.println to ouput your result to STDOUT.
 * Use: System.err.println to ouput debugging information to STDERR.
 * ***/
package com.isograd.exercise;
import java.util.*;
/* input:
5
1 13
27 13
14 13
42 14
15 13 
*/
public class IsoContest01LignesDeProduction {
	public static void main( String[] argv ) throws Exception {
		String  line;
		int count = 0;
		int n = 2;
		Map<String, Integer> os = new HashMap<>(); 
		Map<String, String> lignes = new HashMap<>(); 
		Scanner sc = new Scanner(System.in);
		while(count <= n && sc.hasNextLine()) {
			line = sc.nextLine();
			System.err.println("line: "+line);
			/* Lisez les données et effectuez votre traitement */
			if (count == 0) {
				n = Integer.parseInt(line);
			} else {
				String[] splits = line.split(" ");
				if (os.get(splits[1]) == null) os.put(splits[1], 1);
				else os.put(splits[1], os.get(splits[1])+1);
				lignes.put(splits[1], splits[0]);
				System.err.println("os.size(): "+os.size());
			}
			count++;
			System.err.println("count: "+count+", n: "+n);
		}
		System.err.println("a");
		sc.close();
		System.err.println("b");
		for (String k: os.keySet()) {
			System.err.println("k: "+k+" = "+os.get(k));
			if (os.get(k) == 1) System.out.println(lignes.get(k));
		}

	/* Vous pouvez aussi effectuer votre traitement une fois que vous avez lu toutes les données.*/
	}
}