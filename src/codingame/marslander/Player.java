package codingame.marslander;

import java.io.InputStream;
import java.util.*;

/**
 * Save the Planet.
 * Use less Fossil Fuel.
 **/
class Player {

    // Params
    private static double DISTANCE_POINTS_DE_PASSAGE = 1_000D; // Fonctionne de 500 à 2000
    private static double ALTITUDE_FORCAGE_ATTERRISSAGE_LIGNE_DROITE = 100;
    private static double VITESSE_MINIMALE = 50;
    private static int ALGO_CALCUL_VECTEUR_VITESSE = 1;
    private static int HORIZON = 4;
    private static int MAX_DELTA_ANGLE_POUR_AUTORISER_LE_THRUST = 60; 
    private static boolean ALGO_CALCUL_VITESSE_PAR_SEGMENT = false;
    
    static final double g = -3.711;

    LinkedList<Coords> pointsSol = new LinkedList<>();
    int X;
    int Y;
    int HS;
    int VS;
    int F;
    int R;
    int P;
    
    // State
    boolean premierPassage = true;
    Coords position;
    Coords ptAtterrissage;
    LinkedList<PtPassage> ptPassages = null;
    
    // Result
    long newThrust;
    long newAngle;
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);
        
        int N = in.nextInt(); // the number of points used to draw the surface of Mars.
        System.err.format("%s%n", N);
        
        for (int i = 0; i < N; i++) {
            int landX = in.nextInt(); // X coordinate of a surface point. (0 to 6999)
            int landY = in.nextInt(); // Y coordinate of a surface point. By linking all the points together in a sequential fashion, you form the surface of Mars.
            System.err.format("%s %s%n", landX, landY);
            pointsSol.add(new Coords(landX, landY));
        }
        
        while (true) {
            X = in.nextInt();
            Y = in.nextInt();
            HS = in.nextInt(); // the horizontal speed (in m/s), can be negative.
            VS = in.nextInt(); // the vertical speed (in m/s), can be negative.
            F = in.nextInt(); // the quantity of remaining fuel in liters.
            R = in.nextInt(); // the rotation angle in degrees (-90 to 90).
            P = in.nextInt(); // the thrust power (0 to 4).
            System.err.format("%s %s %s %s %s %s %s%n", X, Y, HS, VS, F, R, P);
            
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            strategie();
            
            // R P. R is the desired rotation angle. P is the desired thrust power.
            System.out.println(""+newAngle+" "+newThrust);
        }
        
    }

    private void strategie() {
        position = new Coords(X, Y);
        initPremierPassage();
        
        PtPassage cible = getPtPassageSuivant();
        log("cible: ", cible);
        
        // Vecteur de direction vers la cible
        Coords vecteurDir = position.createVecteurTo(cible);
        log("Vecteur de direction: ", vecteurDir.showVector());

        Coords vecteurVitCible = null;;
        
        if (ALGO_CALCUL_VECTEUR_VITESSE == 1) {
            // Vecteur de vitesse optimal
            double angle = vecteurDir.getVAngle();
            vecteurVitCible = new Coords(Math.cos(angle) * cible.vx, Math.sin(angle) * cible.vy);
            log("Vecteur vitesse: ", vecteurVitCible.showVector());
        }

        if (ALGO_CALCUL_VECTEUR_VITESSE == 2) {
            /*
             * Cet algorithme, pourtant plus logique combiné à ALGO_CALCUL_VITESSE_PAR_SEGMENT,
             * ne fonctionne pas car contrairement 
             * à l'algo 1, il permet des vitesses verticales trop grandes que le vaisseau
             * n'arrive pas à corriger par la suite. 
             */
            Coords vecteurVitActuel = new Coords(HS, VS);
            log("Vecteur Vitesses actuel : ", vecteurVitActuel.showVector());
            double vitesseCible = Math.sqrt(cible.vx * cible.vx + cible.vy * cible.vy);  
            
            vecteurVitCible = new Coords().createVFromFormeTrigono(vecteurDir.getVAngle(), vitesseCible);
            log("Vecteur Vitesses cible : ", vecteurVitCible.showVector());
            if (vecteurVitCible.y < -39) vecteurVitCible.y = -39;
        }

        double ha = (vecteurVitCible.x - HS) / HORIZON;
        double va = (vecteurVitCible.y - VS) / HORIZON;
        
        Coords vecteurAcc = new Coords(ha, va);
        log("Vecteur acceleration:", vecteurAcc.showVector());
        
        vecteurAcc.y -= g;
        log(" > correction g:", vecteurAcc.showVector());

        commandesSuivantes(vecteurAcc);   
    }
    
    private void initPremierPassage() {

        if (!premierPassage)
            return;

        // Recherche du point d'atterrissage
        // Il n'est pas au millieu du segment mais a 3/4 en opposition de l'arrivée.

        ptAtterrissage = null;
        for (int i = 0; i < pointsSol.size() - 1; i++) {
            if (pointsSol.get(i).y == pointsSol.get(i + 1).y) {
                double p = 0.25;
                if (X < pointsSol.get(i).x) p = 0.75;
                ptAtterrissage = pointsSol.get(i).pointSurSegment(pointsSol.get(i+1), p);
            }
        }
        
        // Détermination vitesse initiale pour déterminer les pts de passage et la vitesse
        // qui leur est associée.
        
        Coords vecteurVitActuel = new Coords(HS, VS);
        log("Vecteur Vitesses actuel : ", vecteurVitActuel.showVector());
        double vitActuelle = vecteurVitActuel.getVNorme();
        if (vitActuelle < VITESSE_MINIMALE) vitActuelle = VITESSE_MINIMALE;

        creatPtPassages(vitActuelle);
         
        premierPassage = false;

    }
    
    void commandesSuivantes(Coords vecteurAcc) {
        // On ne peut pas mettre les réacteurs vers le bas
        if (vecteurAcc.y < 0) vecteurAcc.y = 0;
        // Inversion du sens de la réaction vs la direction
        Coords vecteurReact = new Coords(-vecteurAcc.x, -vecteurAcc.y);
        log("Reaction: ", vecteurReact.showVector());
         
        // Détermination de l'angle
        
        AngleEnDegres angleDegre;
        if (Y - ptAtterrissage.y < ALTITUDE_FORCAGE_ATTERRISSAGE_LIGNE_DROITE) {
            // Forçage atterrisage en ligne droite
            log("Ground ", Y - ptAtterrissage.y);
            angleDegre = new AngleEnDegres(0);
        } else {
            angleDegre = new AngleEnDegres().ofRadian(vecteurReact.getVAngle());
            log("reaction angle(degres): ", angleDegre);
            // Remise dans le référentiel du jeu
            angleDegre.ajoute(90);
        }
        newAngle = angleDegre.getIntAngle();
        normalizeAngle();
        
        // Détermination de la puissance
        
        newThrust = Math.round(vecteurReact.getVNorme());
        normalizeThrust();
        
        if (!angleDegre.estProcheDe(newAngle, MAX_DELTA_ANGLE_POUR_AUTORISER_LE_THRUST) && newThrust > 0) {
            log("Pas de thrust car l'angle est trop éloigné de l'idéal.", newAngle, "pour", angleDegre);
            newThrust = 0;
            normalizeThrust();
        } else if (vecteurReact.getVNorme() < 0.5) {
            log("Remise a zero de l'angle car le thrust est proche de zéro");
            // Quand il n'y a pas de thrust antant remettre l'angle à zéro.
            // Pas sur que ce cas soit utilisé en réalité.
            newAngle = 0;
            normalizeAngle();
        }

        log("newThrust: ", newThrust, "angleD: ", angleDegre);
    }

    private void normalizeAngle() {
        newAngle = Long.min(Long.max(newAngle, R-15), R+15);
        newAngle = Long.min(Long.max(newAngle, -90), 90);
    }
    
    void normalizeThrust() {
        newThrust = Long.min(Long.max(newThrust, P-1), P+1);
        newThrust = Long.min(Long.max(newThrust, 0), 4);
    }
    
    private PtPassage getPtPassageSuivant() {
        
        double DISTANCE_CIBLE_ATTEINTE= 100D;
        double RATIO_ABANDON_CIBLE= 1.2D;
        
        PtPassage ptPassageSuivant = ptPassages.peek();
        
        // Abandon du point de passage en cours et choix du point de passage suivant
        // si l'actuel est atteint ou si le suivant est plus proche.
        
        if (ptPassages.size() > 1) {
            if (position.distance(ptPassageSuivant) < DISTANCE_CIBLE_ATTEINTE
                    || position.distance(ptPassages.get(1)) < ptPassages.get(0).distance(ptPassages.get(1)) * RATIO_ABANDON_CIBLE) {
                ptPassages.removeFirst();
                ptPassageSuivant = ptPassages.getFirst();
                log("Changement target:", ptPassageSuivant);
            }
        }

        return ptPassageSuivant;
    }

    private void creatPtPassages(double vitesseInit) {
        ptPassages = new LinkedList<>();
        
        CourbeDeBezierOrdre2 cdb = new CourbeDeBezierOrdre2(position, new Coords(ptAtterrissage.x, position.y), ptAtterrissage);
        List<Coords> pointsCdb = cdb.getPointsPourDistanceMax(DISTANCE_POINTS_DE_PASSAGE);
        
        // Forçage "artificiel" de la vitesse horizontale pour qu'elle descende à zéro 
        Fonction fh = new Fonction(new Coords(1, vitesseInit), new Coords(pointsCdb.size() - 1, 0));
        
        // Forçage "artificiel" de la vitesse vertical à 20 et montée progressive à 39. 
        Fonction fv = new Fonction(new Coords(0, 20), new Coords(pointsCdb.size() - 1, 39));
        
        Fonction f = new Fonction(new Coords(1, vitesseInit), new Coords(pointsCdb.size() - 1, 39));
        
        for (int i = 0; i < pointsCdb.size(); i++) {
            Coords p = pointsCdb.get(i);
            double sh = fh.predict(i);
            double sv = fv.predict(i);
            if (ALGO_CALCUL_VITESSE_PAR_SEGMENT && i > 0) {
                Coords vecteur = pointsCdb.get(i-1).createVecteurTo(pointsCdb.get(i));
                sh = Math.abs(Math.cos(vecteur.getVAngle()) * f.predict(i));
                sv = Math.abs(Math.sin(vecteur.getVAngle()) * f.predict(i));
            }
            ptPassages.add(new PtPassage(p.x, p.y, sh, sv));
        }
        ptPassages.forEach(Player::log);
    }
    
    String f(double value) {
        return String.format("%.2f", value);
    }
    
    
    public class Coords {

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
            return dist;
        }

        /**
         * Pour cette instance de Coords, retourne un point opposé à la base à une
         * certaine distance
         * TODO : code pas clair. Ne faudrait-il pas inverser le calcul entre les 2 méthodes ?
         * TODO : ajout d'un controle si on sort de la carte ou pas
         * 
         * @param base     Point opposé à celui qu'on veut calculer
         * @param distance Distance du point que l'on veut à partir d'ici
         */
        Coords getPointOppose(Coords base, double distance) {
            Coords vector2d = base.createVecteurTo(this);
            return vector2d.doVtranslation(this, distance);
        }

        Coords getAuDelaDe(Coords base, double distance) {
            return getPointOppose(base, -distance);
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
 
        @Override
        public String toString() {
            return String.format("Coords [x=%.2f, y=%.2f]", x, y);
        }

        // ---- Fonctions vectorielles (on considère ici que l'objet Coords est un vecteur)

        /* static */ Coords createVFromFormeTrigono(double angle, double norme) {
            return new Coords(Math.cos(angle) * norme, Math.sin(angle) * norme);
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
            return new Coords(from.x + (int) Math.floor(Math.cos(angle) * distance),
                    from.y + (int) Math.floor(Math.sin(angle) * distance));
        }
        
        public String showVector() {
            return String.format("Coords [x=%.2f, y=%.2f, angle=%.2f, norme=%.2f]", x, y, getVAngle(), getVNorme());
        }

    }

    class PtPassage extends Coords {
        double vx;
        double vy;
        
        public PtPassage(double x, double y, double vx, double vy) {
            super(x, y);
            this.vx = vx;
            this.vy = vy;
        }
        
        String f(double value) {
            return String.format("%.2f", value);
        }
        
        @Override
        public String toString() {
            return "PtPassage [x=" + f(x) + ", y=" + f(y) + ", vx=" + f(vx) + ", vy=" + f(vy) + "]";
        }
        
    }
    
    public class AngleEnDegres {

        public double angle;

        public AngleEnDegres(double angle) {
            this.angle = angle;
            normalize();
        }

        public AngleEnDegres() {
            this.angle = 0;
        }

        /* static */ public AngleEnDegres ofRadian(double radian) {
            double temp = (radian / (2 * Math.PI)) * 360; 
            return new AngleEnDegres(temp);
        }

        int getIntAngle() {
            return new Double(angle).intValue();
        }
        
        public double toRadian() {
            return (angle / 360) * (2 * Math.PI); 
        }
        
        
        @Override
        public AngleEnDegres clone() {
            return new AngleEnDegres(angle);
        }
        
        private void normalize() {
            if (angle < -180) angle = 360 + angle;
            if (angle > 180) angle = angle - 360;
        }

        public AngleEnDegres ajoute(double angle2) {
            angle += angle2;
            normalize();
            return this;
        }

        public AngleEnDegres retire(double angle2) {
            angle -= angle2;
            normalize();
            return this;
        }

        public boolean estProcheDe(double angle2, double delta) {
            AngleEnDegres temp = new AngleEnDegres(angle - angle2);
            return delta > temp.angle && temp.angle > -delta;
        }

        @Override
        public String toString() {
            return String.format("%.2f", angle);
        }
        
    }
    
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
    
    /* Codingame common */
    
    static boolean doLog = true;
    static void log(Object... objects) {
        if (doLog) {
            for (Object o : objects) {
                System.err.print(o.toString() + " ");
            }
            System.err.println();
        }
    }

    static void activateLog() {
        doLog = true;
    }
    
    public static void main(String args[]) {
        Player player = new Player();
        player.joue(System.in);
    }
   
}