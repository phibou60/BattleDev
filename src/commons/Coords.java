package commons;

import java.util.Comparator;

/**
 * Classe permettant d'avoir des fonctions de gestion des points et des
 * déplacements.
 *
 */

/* Coords staticCoords = new Coords(); */

public class Coords {
    /* static */ int MAX_X = 17_630;
    /* static */ int MAX_Y = 9_000;

    double x;
    double y;

    public Coords() {}

    public Coords(double x, double y) {
        super();
        this.x = x;
        this.y = y;
    }

    double distance(Coords c2) {
        double dist = Math.hypot(x - c2.x, y - c2.y);
        if (this instanceof Pion) {
            ((Pion) this).distanceToTarget = dist;
        }
        return dist;
    }

    boolean dansLaCarte() {
        boolean ret = x >= 0 && x <= MAX_X && y >= 0 && y <= MAX_Y;
        return ret;
    }

    Interception getInterception(Pion m) {
        return new Interception(this, m);
    }

    /**
     * Retourne un point au dela du point2.
     * TODO : ajout d'un controle si on sort de la carte ou pas
     */

    Coords getAuDelaDe(Coords point2, double distance) {
        Coords vector2d = createVecteurTo(point2);
        return vector2d.doVtranslation(point2, distance);
    }

    /**
     * Retourne un point opposé au point2.
     */
    
    Coords getPointOppose(Coords point2, double distance) {
        return point2.getAuDelaDe(this, distance);
    }
    
    /**
     * Calcul un point entre 2 points ou en dehors dans une direction ou une autre.
     * 
     * @param 2ème point du segment
     * @param p Si entre 0 et le point est entre les bornes du segment.
     *          si 0.5, alors le point est au millieu.
     *          si 2 alors le point est à une fois la longueur du segment dans la direction de c2 opposé à this..
     *          si -2 alors le point est à une fois la longueur du segment dans la direction de this opposé à c2.
     * @return
     */
     Coords pointSurSegment(Coords c2, double p) {
        return new Coords (this.x + (c2.x - this.x) * p, this.y + (c2.y - this.y) * p);
    }
     
    boolean equals(Coords c2) {
        return x == c2.x && y == c2.y; 
    }

    @Override
    public String toString() {
        return String.format("Coords [x=%.2f, y=%.2f]", x, y);
    }
    
    // ---- Fonctions vectorielles (on considère ici que l'objet Coords est un vecteur)

    /* static */ Coords createVFromFormeTrigono(double angle, double norme) {
        return new Coords((int) Math.floor(Math.cos(angle) * norme), (int) Math.floor(Math.sin(angle) * norme));
    }
    
    Coords createVecteurTo(Coords to) {
        return new Coords(to.x - x, to.y - y);
    }
    
    Coords createVecteurFrom(Coords from) {
        return new Coords(x - from.x, y - from.y);
    }

    double getVNorme() {
        return Math.hypot(x, y);
    }

    double getVAngle() {
        return Math.atan2(y, x);
    }

    Coords doVtranslation(Coords from) {
        return new Coords(from.x + x, from.y + y);
    }

    Coords doVtranslation(Coords from, double distance) {
        double angle = getVAngle();
        return new Coords(from.x + Math.cos(angle) * distance, from.y + Math.sin(angle) * distance);
    }

    /* static */ Comparator<Coords> duPlusProcheAuPlusLoin(Coords base) {
        return new DistanceToBaseComparator(base, 1);
    }

    /* static */ Comparator<Coords> duPlusLoinAuPlusProche(Coords base) {
        return new DistanceToBaseComparator(base, -1);
    }
    
    public String showVector() {
        return String.format("Coords [x=%.2f, y=%.2f, angle=%.2f, norme=%.2f]", x, y, getVAngle(), getVNorme());
    }

}
