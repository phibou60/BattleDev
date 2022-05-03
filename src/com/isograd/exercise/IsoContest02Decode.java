/*******
 * Read input from System.in

 * Use: System.out.println to ouput your result to STDOUT.
 * Use: System.err.println to ouput debugging information to STDERR.
 * ***/

/* input:
3
RFsarAbi1Dt4eeh
*/
package com.isograd.exercise;
import java.util.*;

public class IsoContest02Decode {
	public static void main( String[] argv ) throws Exception {
		int count = 0;
		int countMax = 2; // nb lignes max
		String ret = "";
		
		int n = 0;
		String code = "";
		
		String  line;
		Scanner sc = new Scanner(System.in);
		while(count < countMax && sc.hasNextLine()) {
			line = sc.nextLine();
			System.err.println("line: "+line);
			if (count == 0) {
				n = Integer.parseInt(line);
			} else {
				code = line;
			}
			count++;
		}
		
		char[] chars = code.toCharArray();

		for (int i=0; i<n; i++) {
			for (int j=i; j<code.length(); j+=n) {
				ret += chars[j];
			}
		}
		System.out.println(ret);
		
	}
}