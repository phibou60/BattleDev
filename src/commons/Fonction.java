package commons;

/**
 * Classe permettant de faire une fonction polynomiale de degrés 1 ou 2.
 * 
 */
public class Fonction {
    double a;
    double b;
    double c;
    
    public Fonction(double a, double b, double c) {
        super();
        this.a = a;
        this.b = b;
        this.c = c;
    }
   
    /**
     * Creation d'une fonction affine à partir de 2 points.
     */
    public Fonction(Coords pt1, Coords pt2) {
        super();
        this.a = 0;
        
        this.b = (pt2.y - pt1.y) / (pt2.x - pt1.x);
        this.c = pt1.y - (b * pt1.x);
    }
   
    double predict(double x) {
        return a * x * x + b * x + c; 
    }
    
}
