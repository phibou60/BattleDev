package commons;

import java.util.ArrayList;
import java.util.List;

public class CourbeDeBezierOrdre2 {

    Coords a1;
    Coords a2;
    Coords a3;
    
    public CourbeDeBezierOrdre2(Coords a1, Coords a2, Coords a3) {
        super();
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
    }
    
    public List<Coords> getPoints(int nbSegments) {
        List<Coords> ret = new ArrayList<>();
        double step = 1D / nbSegments;
        double t = 0;
        
        for (int i = 0; i <= nbSegments; i++) { 
            double x = ((1-t) * (1-t) * a1.x) + (2 * t * (1-t) * a2.x) + (t * t * a3.x);
            double y = ((1-t) * (1-t) * a1.y) + (2 * t * (1-t) * a2.y) + (t * t * a3.y);
            ret.add(new Coords(x, y));
            
            t += step;
        }
        return ret;
    }
    
    /**
     * Estimation de la longueur de manière empirique. 
     */
    public double getLongueur() {
        return (a1.distance(a2) + a2.distance(a3) + a1.distance(a3)) / 2;
    }
    
    public int nbSegmentsPourDistanceMax(double distance) {
        return (int) Math.floor(getLongueur() / distance) + 1; 
    }
    
    public List<Coords> getPointsPourDistanceMax(double distance) {
        int nbSegments = nbSegmentsPourDistanceMax(distance);
        return getPoints(nbSegments);
    }
    
}
