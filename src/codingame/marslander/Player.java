package codingame.marslander;

import java.io.InputStream;
import java.util.*;

/**
 * Tout est dans le calcul des points de passage.
 **/
class Player {

    // Params

    private static final int DISTANCE_AU_LARGE_DU_PT_D_INFLEXION = 500;
    private static final int ALTITUDE_SURVOL = 300;

    private static double ALTITUDE_FORCAGE_ATTERRISSAGE_LIGNE_DROITE = 100;
    private static double VITESSE_MINIMALE = 20;
    private static int ALGO_CALCUL_VECTEUR_VITESSE = 1;
    private static int HORIZON = 4;
    private static int MAX_DELTA_ANGLE_POUR_AUTORISER_LE_THRUST = 60; 
    
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
    private Coords coordsPlat1;
    private Coords coordsPlat2;
    private int dirDepl;
    
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
                ptAtterrissage = pointsSol.get(i).pointSurSegment(pointsSol.get(i+1), 0.5);
                coordsPlat1 = pointsSol.get(i);
                coordsPlat2 = pointsSol.get(i+1);
            }
        }
        log("ptAtterrissage:", ptAtterrissage);
        
        dirDepl = 1;
        if (X > ptAtterrissage.x) dirDepl = -1;
            
        // Détermination vitesse initiale pour déterminer les pts de passage et la vitesse
        // qui leur est associée.
        
        Coords vecteurVitActuel = new Coords(HS, VS);
        log("Vecteur Vitesses actuel : ", vecteurVitActuel.showVector());
        double vitActuelle = vecteurVitActuel.getVNorme();
        if (vitActuelle < VITESSE_MINIMALE) vitActuelle = VITESSE_MINIMALE;

        creatPtPassages(vitActuelle);
         
        premierPassage = false;

    }

    private void creatPtPassages(double vitesseInit) {
        ptPassages = new LinkedList<>();

        Coords ptInflexion= null; // Pt a partir duquel on est en vue du pt d'atterrissage
        int altitudeSurvol = 0; // Altitude de survol du plus au point avant le pt d'inflexion
        int dirAtter = 0; // Direction pt d'atterrissage (1 pour x croissant, -1 sinon) 

        // Droite directe entre le pt étudié et le pt d'atterrissage pour savoir
        // s'il y a des points du sol au dessus.
        Fonction vueSurPtAtterrissage = new Fonction(position, ptAtterrissage); 
        
        // Boucle sur les points du sol
       
        Iterator<Coords> iterator = pointsSol.iterator();
        if (dirDepl == -1) iterator = pointsSol.descendingIterator();
        
        while (iterator.hasNext()) {
            Coords pointSol = iterator.next();
            log(" > pointSol:", pointSol);
    
            // On saute les points du sol avant la position du vaisseau
            if ((pointSol.x - X) * dirDepl < 0) continue;  
            
            // A-t-on atteint le segment plat du pt d'atterrissage ?
            if (pointSol.equals(coordsPlat1) || pointSol.equals(coordsPlat2)) {
                break;
            }
            
            // Recherche du plus au point et calcul de l'altitude de survol
            if (pointSol.y + ALTITUDE_SURVOL > altitudeSurvol) {
                altitudeSurvol = (int) pointSol.y + ALTITUDE_SURVOL;
            }

            // Choix du point d'inflexion.
            // S'il n'y en a pas encore, par défaut on prend le premier qui vient.
            // Sinon on cherche à savoir si le point du sol étudié est au dessus de la droite
            // qui part du pt d'inflexion et qui va vers le pt d'atterrissage.
            // Attention, dans le cas ou la caverne est inversée vis-à-vis du déplacement
            // dirDepl et dirAtter sont inversés et le test s'inverse.
            // On vérifie alors que les points sont au-dessus de la droite.
            
            if (ptInflexion == null
                    || (pointSol.y - vueSurPtAtterrissage.predict(pointSol.x)) * dirDepl * dirAtter > 0) {
                ptInflexion = pointSol;
                vueSurPtAtterrissage = new Fonction(ptInflexion, ptAtterrissage);
                dirAtter = (ptInflexion.x < ptAtterrissage.x) ? 1 : -1;
            }
        }
        
        log("ptInflexion:", ptInflexion);
                
        ptPassages.add(new PtPassage(X, Y, 0, 0));
        ptPassages.add(new PtPassage(ptInflexion.x, altitudeSurvol, 0, 0));
        ptPassages.add(new PtPassage(ptInflexion.x+DISTANCE_AU_LARGE_DU_PT_D_INFLEXION*dirDepl, ptInflexion.y, 0, 0));
        ptPassages.add(new PtPassage(ptAtterrissage.x, ptAtterrissage.y, 0, 0));
        
        // Forçage "artificiel" de la vitesse horizontale pour qu'elle descende à zéro 
        Fonction fh = new Fonction(new Coords(1, vitesseInit), new Coords(ptPassages.size() - 1, 20));
        
        // Forçage "artificiel" de la vitesse vertical à 20 et montée progressive à 39. 
        Fonction fv = new Fonction(new Coords(0, 20), new Coords(ptPassages.size() - 1, 39));
        
        for (int i = 0; i < ptPassages.size(); i++) {
            PtPassage ptPassage = ptPassages.get(i);
            ptPassage.vx = fh.predict(i);
            ptPassage.vy = fv.predict(i);
        }
        ptPassages.forEach(Player::log);
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
                    //|| position.distance(ptPassages.get(1)) < ptPassages.get(0).distance(ptPassages.get(1)) * RATIO_ABANDON_CIBLE
                    ) {
                ptPassages.removeFirst();
                ptPassageSuivant = ptPassages.getFirst();
                log("Changement target:", ptPassageSuivant);
            }
        }

        return ptPassageSuivant;
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
         
        boolean equals(Coords c2) {
            return x == c2.x && y == c2.y; 
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