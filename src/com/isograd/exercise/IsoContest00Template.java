/*******
 * Read input from System.in
 * Use: System.out.println to ouput your result to STDOUT.
 * Use: System.err.println to ouput debugging information to STDERR.
 * ***/
package com.isograd.exercise;
import java.util.*;

public class IsoContest00Template {
	static int aaaa = 0;
	static int bbbb = 0;

	public static void main( String[] argv ) throws Exception {
		System.err.println("--------------------------------------------------");
		int count = 0; // ligne en cours -1
		int countMax = 2; // nb lignes max
		String ret = "";
		
		int n = 0;
		
		String  line;
		Scanner sc = new Scanner(System.in);
		while(count < countMax && sc.hasNextLine()) {
			line = sc.nextLine();
			System.err.println("line("+(count+1)+"): "+line);
			if (count == 0) {
				n = Integer.parseInt(line);
				// si plusieurs données 
				String[] splits = line.split(" ");
				aaaa = Integer.parseInt(splits[0]);
				bbbb = Integer.parseInt(splits[1]);
			} else {
				
			}
			count++;
		}
		sc.close();
		
		// Calcul ...

		System.err.println("ret: "+ret);
		System.out.println(ret);
	}
	//----------------------------------------------------------------------
	
	
}