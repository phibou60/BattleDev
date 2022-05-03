/*******
 * Read input from System.in
 * Use: System.out.println to ouput your result to STDOUT.
 * Use: System.err.println to ouput debugging information to STDERR.
 * ***/
package commons.divers;
import java.util.*;
/*
3
03:27-04:18
11:45-17:48
14:53-21:44
 */
public class IsoContest03EmploiDuTemps {
	public static void main( String[] argv ) throws Exception {
		System.err.println("--------------------------------------------------");
		int count = 0;
		int countMax = 2; // nb lignes max
		String ret = "";
		
		byte[] min = new byte[24*60];
		for (int i=0; i< min.length; i++) min[i] = 1;
		
		String  line;
		Scanner sc = new Scanner(System.in);
		while(count < countMax && sc.hasNextLine()) {
			line = sc.nextLine();
			System.err.println("line("+(count+1)+"): "+line);
			if (count == 0) {
				countMax = Integer.parseInt(line) + 1;
			} else {
				String[] splits = line.split("-|:");
				//System.err.println("splits.length: "+splits.length);
				int h1 = Integer.parseInt(splits[0]);
				int m1 = Integer.parseInt(splits[1]);
				int start = h1*60+m1;
				int h2 = Integer.parseInt(splits[2]);
				int m2 = Integer.parseInt(splits[3]);
				int end = h2*60+m2;
				if (end > start)
					for (int i=start; i<= end; i++) min[i] = 0;
				else {
					for (int i=0; i<= end; i++) min[i] = 0;
					for (int i=start; i< min.length; i++) min[i] = 0;
				}
			}
			count++;
		}
		
		dumpMin(min);
		ret = "IMPOSSIBLE";
		// Calcul ...
		int bestStart = -1; int bestEnd = -1; 
		int start = -1;
		int end = -1;
		for (int i=0; i< min.length; i++) {
			if (min[i] == 1 && start == -1) {
				start = i; end = i;
			} else if (min[i] == 1 && start > -1) {
				end = i;
			} else if (min[i] == 0 && start > -1) {
				String creneau = getHour(start)+"-"+getHour(end);
				System.err.println(">> creneau: "+creneau);
				if (end - start > bestEnd - bestStart) {
					ret = creneau;
					System.err.println("BEST: "+(end - start)+" vs "+(bestEnd - bestStart));
					bestStart = start;
					bestEnd = end;
				}
				start = -1;
				end = -1;
			}
		}
		if (start > -1) {
			System.err.println("Recherche si on deborde");
			for (int i=0; i< min.length; i++) {
				if (min[i] == 1) end = min.length+i;
				if (min[i] == 0) break;
			}
			String creneau = getHour(start)+"-"+getHour(end);
			System.err.println(">> creneau: "+creneau);
			if (end - start > bestEnd - bestStart) {
				ret = creneau;
				System.err.println("BEST: "+(end - start)+" vs "+(bestEnd - bestStart));
				bestStart = start;
				bestEnd = end;
			}
		}

		System.err.println("ret: "+ret);
		System.out.println(ret);
		
	}
	static String getHour(int i) {
		int h = i / 60;
		if (h > 23) h = h - 24;
		int m = i % 60;
		return (h<10?"0":"")+h+":"+(m<10?"0":"")+m;
	}
	static void dumpMin(byte[] min) {
		for (int h=0; h<24; h++) {
			String dump = "";
			for (int m=0; m<60; m++) {
				dump += ""+min[h*60+m];
			}
			System.err.println("dump: "+dump);
		}
	}
	
}