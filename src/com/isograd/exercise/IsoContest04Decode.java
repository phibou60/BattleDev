/*******
 * Read input from System.in
 * Use: System.out.println to ouput your result to STDOUT.
 * Use: System.err.println to ouput debugging information to STDERR.
 * ***/
/*
9
cyxcrclqj
petitchat

 */
package com.isograd.exercise;
import java.util.*;

public class IsoContest04Decode {
	public static void main( String[] argv ) throws Exception {
		System.err.println("--------------------------------------------------");
		int count = 0;
		int countMax = 3; // nb lignes max
		String ret = "";
		
		int n = 0;
		int[] code = null;
		int[] approx = null;
		
		String  line;
		Scanner sc = new Scanner(System.in);
		while(count < countMax && sc.hasNextLine()) {
			line = sc.nextLine();
			System.err.println("line("+(count+1)+"): "+line);
			if (count == 0) {
				n = Integer.parseInt(line);
			} else if (count == 1) {
				code = getValues(line.toCharArray());
				dump(code);
			} else if (count == 2) {
				approx = getValues(line.toCharArray());
				dump(approx);
			}
			count++;
		}
		System.err.println("code: "+intsToString(code));
		System.err.println("approx: "+intsToString(approx));
		
		// Calcul ...
		int best = -1;
		int[] zape = code;
		for (int k=0; k<26; k++) {
			zape = zap(zape);
			int[] decallage = clone(zape);
			
			for (int d=0; d<n; d++) {
				int score = getScore(decallage, approx);
				if (score > best) {
					ret = intsToString(decallage);
					System.err.println("Best: "+ret+", score: "+score);
					best = score;
				}
				decallage = decall(decallage);
			}
		}
		
		System.err.println("ret: "+ret);
		System.out.println(ret);
		
	}

	static int[] clone(int[] table) {
		int[] ret = new int[table.length];
		for (int i=0; i< table.length; i++) {
			ret[i] = table[i];
		}
		return ret;
	}

	static int[] zap(int[] table) {
		int[] ret = new int[table.length];
		for (int i=0; i< table.length; i++) {
			ret[i] = table[i]-1;
			if (ret[i] < 0) ret[i] = 25; 
		}
		return ret;
	}

	static int[] decall(int[] table) {
		int[] ret = new int[table.length];
		int inter = table[0];
		for (int i=1; i< table.length; i++) {
			ret[i-1] = table[i];
		}
		ret[table.length-1] = inter;
		return ret;
	}

	static int getScore(int[] table1, int[] table2) {
		int ret = 0;
		for (int i=0; i< table1.length; i++) {
			if (table1[i] == table2[i]) ret++;
		}
		return ret;
	}
	static int getValue(char c) {
		return c - 'a';
	}
	static int[] getValues(char[] chars) {
		int[] ret = new int[chars.length];
		for (int i=0; i< chars.length; i++) {
			ret[i] = getValue(chars[i]);
		}
		return ret;
	}
	static void dump(int[] table) {
		String ret = "";
		for (int i=0; i< table.length; i++) {
			ret += table[i]+";";
		}
		System.err.println(ret);
	}
	static String intsToString(int[] table) {
		char[] tableau = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		String ret = "";
		for (int i=0; i< table.length; i++) {
			ret += tableau[table[i]];
		}
		return ret;
	}

}