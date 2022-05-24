package commons;

import java.util.Comparator;

/**
 * Classe permettant d'avoir des fonctions de gestion des points et des
 * déplacements.
 *
 */

/* Coords staticCoords = new Coords(); */

class Coords {
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
/*
        if (this instanceof Pion) {
            ((Pion) this).distanceToTarget = dist;
        }
*/
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
        Coords vector2d = createVecteurVers(point2);
        return vector2d.doVtranslation(point2, distance);
    }

    /**
     * Retourne un point opposé au point2.
     */
    
    Coords getPointOppose(Coords point2, double distance) {
        return point2.getAuDelaDe(this, distance);
    }
    
    /**
     * Retourne un point vers le point2 à une certaine distance.
     */
    
    Coords getPointVers(Coords point2, double distance) {
        return this.doVtranslation(createVecteurVers(point2, distance));
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

    int getIntX() {
        return (int) x;
    }
    
    int getIntY() {
        return (int) y;
    }
    
    @Override
    public String toString() {
        return String.format("Coords [x=%.2f, y=%.2f]", x, y);
    }
    
    // ---- Fonctions vectorielles (on considère ici que l'objet Coords est un vecteur)

    /* static */ Coords createVFromFormeTrigono(double angle, double norme) {
        return new Coords((int) Math.floor(Math.cos(angle) * norme), (int) Math.floor(Math.sin(angle) * norme));
    }
    
    /**
     * Retourne le vecteur qui fait la translation vers le point2.
     */
            
    Coords createVecteurVers(Coords to) {
        return new Coords(to.x - x, to.y - y);
    }
    
    /**
     * Retourne le vecteur qui fait la translation vers le point2 à une certaine distance.
     */
    
    Coords createVecteurVers(Coords point2, double distance) {
        Coords vectorVersPoint2 = createVecteurVers(point2);
        return new Coords().createVFromFormeTrigono(vectorVersPoint2.getVAngle(), distance);
    }
    
    Coords createVecteurAPartirDe(Coords from) {
        return new Coords(x - from.x, y - from.y);
    }

    double getVNorme() {
        return Math.hypot(x, y);
    }

    double getVAngle() {
        return Math.atan2(y, x);
    }
    
    AngleEnDegres getVAngleDegres() {
        return new AngleEnDegres((360 * getVAngle()) / (2 * Math.PI));
    }
    
    Coords doVtranslation(Coords from) {
        return new Coords(from.x + x, from.y + y);
    }

    Coords doVtranslation(Coords from, double distance) {
        double angle = getVAngle();
        return new Coords(from.x + Math.cos(angle) * distance, from.y + Math.sin(angle) * distance);
    }
    
    Coords ajouteVecteur(Coords v2) {
        return new Coords(this.x + v2.x, this.y + v2.y);
    }
    
    Coords soustraitVecteur(Coords v2) {
        return new Coords(this.x - v2.x, this.y - v2.y);
    }
    
    /**
     * Calcule l'angle entre les vecteurs l'un vers A, l'autre vers B.<br>
     * Le résultat est positif si le segment vers B est dans le sens horaire vis-à-vis de celui
     * vers A.
     * 
     */
    
    AngleEnDegres angleVecteurs(Coords A, Coords B) {
        Coords vA = createVecteurVers(A);
        Coords vB = createVecteurVers(B);
        return new AngleEnDegres().ofRadian(vB.getVAngle()-vA.getVAngle()); 
    }
    
    Coords rotation(double angleAjout) {
        return new Coords().createVFromFormeTrigono(getVAngle()+angleAjout, getVNorme());
    }
   
    public String showVector() {
        return String.format("Coords [x=%.2f, y=%.2f, angle=%.2f(%s°), norme=%.2f]", x, y, getVAngle(), getVAngleDegres(), getVNorme());
    }
    
    /* static */ Comparator<Coords> duPlusProcheAuPlusLoin(Coords base) {
        return new DistanceToBaseComparator(base, 1);
    }

    /* static */ Comparator<Coords> duPlusLoinAuPlusProche(Coords base) {
        return new DistanceToBaseComparator(base, -1);
    }

} 
