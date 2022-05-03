package commons;

public class Line {
    // Sous forme d'équation cartésienne : ax + by + c = 0
    double a;
    double b;
    double c;
    
    public Line(double a, double b, double c) {
        super();
        this.a = a;
        this.b = b;
        this.c = c;
    }
    
    Line getLineOrthogonale(Coords point) {
        return new Line(b, -a, -(b * point.x + -a * point.y));
    }
    
    // Peut être null si les lignes sont parallèles
    Coords getIntersection(Line l2) {
        Coords inters = new Coords();
        //log("Intersection: "+toString()+" vs "+l2);
        
        if ((isZero(a) && isZero(l2.a)) || (isZero(b) && isZero(l2.b)) ) {
            // Lines are parallel
            return null;
        }
        
        if (!isZero(a)) {
            inters.y = (int) Math.round((-l2.c + (l2.a * c) / a) / (((-l2.a * b) / a) + l2.b));
            inters.x = (int) Math.round((-c - (b * inters.y)) / a);
        } else {
            inters.y = (int) Math.round(-c / b);
            inters.x = (int) Math.round((-l2.b * inters.y - l2.c) / l2.a);
        }
        return inters;
    }
    
    boolean isZero(double val) {
        return Math.abs(val) < 0.001D;
    }
    
    @Override
    public String toString() {
        return "Line [a=" + a + ", b=" + b + ", c=" + c + "]";
    }
    
}
