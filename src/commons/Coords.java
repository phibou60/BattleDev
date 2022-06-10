package commons;

import java.util.Comparator;

/**
 * Classe permettant d'avoir des fonctions de gestion des points et des
 * déplacements.
 *
 */

class Coords {
    static final int MAX_X = 17_630;
    static final int MAX_Y = 9_000;

    double x;
    double y;

    public Coords() {}

    public Coords(double x, double y) {
        this.x = x;
        this.y = y;
    }

    double distance(Coords c2) {
        double dist = Math.hypot(x - c2.x, y - c2.y);
        return dist;
    }

    /**
     * Distance au carré (permet d'économiser la racine carrée)
     */
    double distance2(Coords c2) {
        double distX = x - c2.x;
        double distY = y - c2.y;
        return distX * distX + distY * distY;
    }

    /**
     * Compare si la distance à la cible est inférieur (<0) ou supérieure (>0) à une valeur.<br>
     * N'utilise pas la racine carrée.
     */
    public int compareDistance(Coords cible, double val) {
        double dist2 = distance2(cible);
        double val2 = val * val;
        if (dist2 == val2) return 0;
        return dist2 < val2 ? -1 : 1;
    }

    /**
     * Compare si la distance à C1 est plus proche que C2<br>
     * Si négatif, elle est plus proche.<br>
     * N'utilise pas la racine carrée.
     */

    public int compareDistance(Coords c1, Coords c2) {
        double dist1 = distance2(c1);
        double dist2 = distance2(c2);
        if (dist1 == dist2) return 0;
        return dist1 < dist2 ? -1 : 1;
    }

    boolean dansLaCarte() {
        boolean ret = x >= 0 && x <= MAX_X && y >= 0 && y <= MAX_Y;
        return ret;
    }
    
    /**
     * Retourne un point au dela du point2.<br>
     * Attention, on peut sortir de la carte.
     */

    Coords getAuDelaDe(Coords point2) {
         return new Coords(point2.x + point2.x - x, point2.y + point2.y - y);
    }
    
    Coords getAuDelaDe(Coords point2, double distance) {
        Coords vector2d = createVecteurVers(point2);
        return vector2d.doVtranslation(point2, distance);
    }

    /**
     * Retourne un point opposé au point2.<br>
     * Attention, on peut sortir de la carte.
     */
    
    Coords getPointOppose(Coords point2) {
        return new Coords(x + x - point2.x, y + y - point2.y);
    }
    
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
     * @param p Si entre 0 et 1, le point est entre les bornes du segment.
     *          si 0.5, alors le point est au millieu.
     *          si 2 alors le point est à une fois la longueur du segment dans la direction de c2 opposé à this.
     *          si -2 alors le point est à une fois la longueur du segment dans la direction de this opposé à c2.
     * @return
     */
     Coords pointSurSegment(Coords c2, double p) {
        return new Coords (this.x + (c2.x - this.x) * p, this.y + (c2.y - this.y) * p);
    }
     
    /**
     * TODO : permettre une marge d'erreur de 1. 
     */
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

    static Coords createVFromFormeTrigono(double angle, double norme) {
        return new Coords((int) Math.floor(Math.cos(angle) * norme), (int) Math.floor(Math.sin(angle) * norme));
    }
    
    /**
     * Retourne le vecteur qui fait la translation vers le point "to".
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

    /**
     * Retourne le vecteur qui fait la translation à partir du point "from".
     */
    
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
     * Le résultat est négatif si le segment vers B est dans le sens horaire
     * vis-à-vis de celui vers A car on utilise le sens trigonométrique.
     * 
     */
    
    AngleEnDegres angleVecteurs(Coords A, Coords B) {
        Coords vA = createVecteurVers(A);
        Coords vB = createVecteurVers(B);
        return new AngleEnDegres().ofRadian(vB.getVAngle()-vA.getVAngle()); 
    }
    
    /**
     * Rotation de vecteurs.
     */
    Coords rotationQuartTourAntiHoraire() {
        return new Coords(-y, x);
    }

    Coords rotationQuartTourHoraire() {
        return new Coords(y, -x);
    }
    
    Coords rotation(double angleAjout) {
        return new Coords().createVFromFormeTrigono(getVAngle()+angleAjout, getVNorme());
    }
   
    public String showVector() {
        return String.format("Coords [x=%.2f, y=%.2f, angle=%.2f(%s°), norme=%.2f]", x, y, getVAngle(), getVAngleDegres(), getVNorme());
    }
    
    static Comparator<Coords> duPlusProcheAuPlusLoin(Coords base) {
        return new DistanceToBaseComparator(base, 1);
    }

    static Comparator<Coords> duPlusLoinAuPlusProche(Coords base) {
        return new DistanceToBaseComparator(base, -1);
    }

} 
