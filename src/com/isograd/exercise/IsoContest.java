/*******
 * Read input from System.in
 * Use: System.out.println to ouput your result to STDOUT.
 * Use: System.err.println to ouput debugging information to STDERR.
 * ***/
package com.isograd.exercise;
import java.util.*;
public class IsoContest {

	public static void main( String[] argv ) throws Exception {
		System.err.println("--------------------------------------------------");
		int count = 0; // ligne en cours -1
		int countMax = 2; // nb lignes max
		String ret = "";

		HashMap<String, Long> chiffres = new HashMap<>();
		
		String prev = "*";
		long serie = 0;
		
		String line = "";
		Scanner sc = new Scanner(System.in);
		while(count < countMax && sc.hasNextLine()) {
			line = sc.nextLine();
			System.err.println("line("+(count+1)+"): "+line);
			if (count == 0) {
				countMax = Integer.parseInt(line)+1;
			} else {
				if (line.equals(prev)) {
					serie++;
				} else {
					if (!prev.equals("*")) {
						if (chiffres.get(prev) == null || serie > chiffres.get(prev)) {
							chiffres.put(prev, serie);
						}
					}
					serie = 1;
					prev = line;
				}
			}
			count++;
		}
		sc.close();
		if (!prev.equals("*")) {
			if (chiffres.get(prev) == null || serie > chiffres.get(prev)) {
				chiffres.put(prev, serie);
			}
		}

		
		// Calcul ...

		long max = 0;
		ret = "";
		for (String k : chiffres.keySet()) {
			if (chiffres.get(k) > max) {
				max = chiffres.get(k);
				ret = ""+max;
			}
		}
 
		System.err.println("ret: "+ret);
		System.out.println(ret);
	}
	//----------------------------------------------------------------------
	
	
  
}