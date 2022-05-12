package commons.graphes;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GrapheResolveur {

    int nbSommets;
    int from;
    int to;
    
    LinkedList<int[]> arcs = new LinkedList<>();

    int[] minDistance;
    int[] predecesseurs;

    public GrapheResolveur(int nbSommets) {
        super();
        this.nbSommets = nbSommets;
    }
    
    public void ajoutArc(int from, int to) {
        int[] arc = new int[4];
        arc[0] = from;
        arc[1] = to;
        arc[2] = 1;
        arc[3] = 1;
        arcs.add(arc);
    }
    
    public void ajoutArc(int from, int to, int length) {
        int[] arc = new int[4];
        arc[0] = from;
        arc[1] = to;
        arc[2] = length;
        arc[3] = length;
        arcs.add(arc);
    }
    
    public void ajoutArc(int from, int to, int length, int lengthReturn) {
        int[] arc = new int[4];
        arc[0] = from;
        arc[1] = to;
        arc[2] = length;
        arc[3] = lengthReturn;
        arcs.add(arc);
    }
    
    public int cheminLePlusCourt(int from, int to) {
        this.from = from;
        this.to = to;

        boolean[] dejaVu = new boolean[nbSommets];
        minDistance = new int[nbSommets];
        predecesseurs = new int[nbSommets];
        
        for (int i = 0; i < nbSommets; i++) {
            dejaVu[i] = false;
            minDistance[i] = Integer.MAX_VALUE;
            predecesseurs[i] = -1;
        }
        minDistance[from] = 0;
        int a = -1;
        
        while ((a = choisirNodePlusPetiteDistancePasDejaVu(dejaVu, minDistance)) != -1) {
            dejaVu[a] = true;
            List<int[]> arcsVersS = arcsAPartirDe(a);
            arcsVersS.forEach(arc -> {
                int b = arc[1];
                if (minDistance[b] > minDistance[arc[0]] + arc[2]) {
                    minDistance[b] = minDistance[arc[0]] + arc[2];
                    predecesseurs[b] = arc[0];
                }
            });
        }

        return minDistance[to];
    }

    LinkedList<Integer> chemin() {
    
        LinkedList<Integer> ret = new LinkedList<>();
        ret.add(to);
        int s = to;
        while (s != -1 && s != from) {
            System.out.println("sommets: "+s+" -> "+predecesseurs[s]);
            s = predecesseurs[s];
            ret.addFirst(s);
        }
        return ret;
        
    }

    int choisirNodePlusPetiteDistancePasDejaVu(boolean[] dejaVu, int[] minDistance) {
        int selection = -1;
        int distance = Integer.MAX_VALUE;
        
        for (int i = 0; i < nbSommets; i++) {
            if (!dejaVu[i] && minDistance[i] < distance) {
                selection = i;
                distance = minDistance[i];
            }
        }
        
        return selection;
    }
    
    private List<int[]> arcsAPartirDe(int s) {
        return arcs.stream()
            .filter(arc -> (arc[0] == s && arc[2] > 0) || (arc[1] == s && arc[3] > 0))
            .map(arc -> {
                if (arc[0] == s) {
                    return arc;
                } else {
                    int[] newArc = new int[3];
                    newArc[0] = arc[1];
                    newArc[1] = arc[0];
                    newArc[0] = arc[3];
                    return newArc;
                }
            })
            .collect(Collectors.toList());
    }
   
}
