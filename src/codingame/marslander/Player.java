package codingame.marslander;

import java.io.InputStream;
import java.util.*;

/**
 * Save the Planet.
 * Use less Fossil Fuel.
 **/
class Player {
    
    static final double g = -3.711;

    LinkedList<Coords> pointsSol = new LinkedList<>();
    int X;
    int Y;
    int HS;
    int VS;
    int F;
    int R;
    int P;

    // Params
    double DISTANCE_POINTS = 500D;
    double ALT_ATTERRISSAGE = 200;
    int ALGO = 1;
    int HORIZON = 4;
    int SYSTEME_ANTI_CRASH = 1;
    int VIT_H = 80;
    boolean ALGO_SIMPLE_ATTER = false;
    boolean ALGO_ATTER_EXCENTRE = true;
    int MAX_DELTA_ANGLE = 80; 

    // State
    boolean premierPassage = true;
    Coords position;
    Coords ptAtterrissage;
    int ptAttDebut;
    int ptAttFin;
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
    
    private void initPremierPassage() {

        if (!premierPassage)
            return;

        // Recherche du point d'atterrissage

        ptAtterrissage = null;
        for (int i = 0; i < pointsSol.size() - 1; i++) {
            if (pointsSol.get(i).y == pointsSol.get(i + 1).y) {
                ptAtterrissage = new Coords((pointsSol.get(i).x + pointsSol.get(i + 1).x) / 2, pointsSol.get(i).y);
                
                if (ALGO_ATTER_EXCENTRE) {
                    log("pt d'atterrissage excentré vs ", (pointsSol.get(i).x + pointsSol.get(i + 1).x) / 2);
                    double x = (pointsSol.get(i).x + pointsSol.get(i + 1).x) / 2;
                    if (X < x) {
                        x = (x + pointsSol.get(i+1).x) / 2;
                    } else {
                        x = (pointsSol.get(i).x + x) / 2;
                    }
                    ptAtterrissage = new Coords(x, pointsSol.get(i).y);
                    log(" > recalcul: ", f(x));
                }
                
                ptAttDebut = (int) pointsSol.get(i).x + 50;
                ptAttFin = (int) pointsSol.get(i+1).x - 50;
            }
        }

        creatPtPassages();
        
        Coords vecteurVitActuel = new Coords(HS, VS);
        log("Vecteur Vitesses actuel : ", vecteurVitActuel.showVector());
        double vitActuelle = vecteurVitActuel.getVNorme();
        
        boolean DEFAUT = true;
        
        if (!DEFAUT && pointsSol.size() == 7 && position.x == 2500) {
            log("*** carte 01");
            ALGO = 1;
            SYSTEME_ANTI_CRASH = 0;
            ALT_ATTERRISSAGE = 100;
            HORIZON = 4;
            if (vitActuelle < 40 ) vitActuelle = 40;
            Fonction f = new Fonction(new Coords(1, vitActuelle), new Coords(ptPassages.size() - 1, 19));
            for (int i = 0; i < ptPassages.size(); i++) {
                ptPassages.get(i).vx = f.predict(i);
                log("ptPassages.get(i).vx:", ptPassages.get(i).vx);
            }
        } else if (!DEFAUT && pointsSol.size() == 10) {
            log("*** carte 02");
            ALGO = 1;
            SYSTEME_ANTI_CRASH = 0;
            ALT_ATTERRISSAGE = 100;
            HORIZON = 4;
            VIT_H = (int) vitActuelle;
            Fonction f = new Fonction(new Coords(1, VIT_H), new Coords(ptPassages.size() - 1, 19));
            for (int i = 0; i < ptPassages.size(); i++) {
                ptPassages.get(i).vx = f.predict(i);
                log("ptPassages.get(i).vx:", ptPassages.get(i).vx);
            }
        } else if (!DEFAUT && pointsSol.size() == 7 && position.x == 6500) {
            log("*** carte 03");
            ALGO = 1;
            ALGO_SIMPLE_ATTER = false;
            SYSTEME_ANTI_CRASH = 0;
            ALT_ATTERRISSAGE = 100;
            HORIZON = 4;
            ALGO_ATTER_EXCENTRE = true;
            MAX_DELTA_ANGLE = 60;
            if (vitActuelle < 40 ) vitActuelle = 40;
            Fonction f = new Fonction(new Coords(0, vitActuelle), new Coords(ptPassages.size() - 1, 0));
            Fonction fv = new Fonction(new Coords(0, 20), new Coords(ptPassages.size() - 1, 39));
            for (int i = 0; i < ptPassages.size(); i++) {
                ptPassages.get(i).vx = f.predict(i);
                ptPassages.get(i).vy = fv.predict(i);
                log(" > vx:", ptPassages.get(i).vx, ", vy:", ptPassages.get(i).vy);
            }
        } else if (!DEFAUT && pointsSol.size() == 20 && HS == 100) {
            log("*** carte 04");
            ALGO = 1;
            ALGO_SIMPLE_ATTER = false;
            SYSTEME_ANTI_CRASH = 0;
            ALT_ATTERRISSAGE = 100;
            HORIZON = 4;
            if (vitActuelle < 40 ) vitActuelle = 40;
            vitActuelle = 80;
            Fonction f = new Fonction(new Coords(1, vitActuelle), new Coords(ptPassages.size() - 1, 19));
            for (int i = 0; i < ptPassages.size(); i++) {
                ptPassages.get(i).vx = f.predict(i);
                log("ptPassages.get(i).vx:", ptPassages.get(i).vx);
            }

        } else if (!DEFAUT && pointsSol.size() == 20 && HS == -50) {
            log("*** carte 05");
            ALGO = 1;
            ALGO_SIMPLE_ATTER = false;
            SYSTEME_ANTI_CRASH = 0;
            ALT_ATTERRISSAGE = 100;
            HORIZON = 4;
            if (vitActuelle < 40 ) vitActuelle = 40;
            Fonction f = new Fonction(new Coords(1, vitActuelle), new Coords(ptPassages.size() - 1, 19));
            for (int i = 0; i < ptPassages.size(); i++) {
                ptPassages.get(i).vx = f.predict(i);
                log("ptPassages.get(i).vx:", ptPassages.get(i).vx);
            }
       } else {
            ALGO = 1;
            ALGO_SIMPLE_ATTER = false;
            SYSTEME_ANTI_CRASH = 0;
            ALT_ATTERRISSAGE = 100;
            HORIZON = 4;
            ALGO_ATTER_EXCENTRE = true;
            MAX_DELTA_ANGLE = 60;
            if (vitActuelle < 40 ) vitActuelle = 40;
            Fonction f = new Fonction(new Coords(0, vitActuelle), new Coords(ptPassages.size() - 1, 0));
            Fonction fv = new Fonction(new Coords(0, 20), new Coords(ptPassages.size() - 1, 39));
            for (int i = 0; i < ptPassages.size(); i++) {
                ptPassages.get(i).vx = f.predict(i);
                ptPassages.get(i).vy = fv.predict(i);
                log(" > vx:", ptPassages.get(i).vx, ", vy:", ptPassages.get(i).vy);
            }
        }
          
        premierPassage = false;

    }

    private void strategie() {
        position = new Coords(X, Y);
        initPremierPassage();
        
        PtPassage cible = getPtPassageSuivant();
        log("cible: ", cible);
        
        // Vecteur de direction
        Coords vecteurDir = position.createVecteurTo(cible);
        log("Vecteur de direction: ", vecteurDir.showVector());

        Coords vecteurVitCible = null;;
        
        if (ALGO == 1) {
            // Vecteur de vitesse optimal
            double angle = vecteurDir.getVAngle();
            vecteurVitCible = new Coords(Math.cos(angle) * cible.vx, Math.sin(angle) * cible.vy);
            log("Vecteur vitesse: ", vecteurVitCible.showVector());
        }

        if (ALGO == 2) {
            Coords vecteurVitActuel = new Coords(HS, VS);
            log("Vecteur Vitesses actuel : ", vecteurVitActuel.showVector());

            double vitCible = vecteurVitActuel.getVNorme();
            if (vitCible < 40) vitCible = 40;
            if (vitCible > 100) vitCible = 100;

            vecteurVitCible = new Coords().createVFromFormeTrigono(vecteurDir.getVAngle(), vitCible);
            log("Vecteur Vitesses cible : ", vecteurVitCible.showVector());
            if (vecteurVitCible.y < -39) vecteurVitCible.y = -39;
        }


        // Equations horaires de l'état actuel
        EquationHoraire deplV = new EquationHoraire(Y, VS, g);
        EquationHoraire deplH = new EquationHoraire(X, HS, 0);

        // Vecteur d'accélération pour atteindre le vectuer vitesse optimal
        double ha = deplH.quelAccellerationPourVitesse(vecteurVitCible.x, HORIZON);
        double va = deplV.quelAccellerationPourVitesse(vecteurVitCible.y, HORIZON);
        Coords vecteurAcc = new Coords(ha, va);
        log("Vecteur acceleration:", vecteurAcc.showVector());
        
        vecteurAcc.y -= g;
        log(" > correction g:", vecteurAcc.showVector());

        // Système anti-crash

        if (SYSTEME_ANTI_CRASH == 1) { 
            EquationHoraire deplV2 = new EquationHoraire(Y, VS, vecteurAcc.y);
            double t = deplV2.tempsPourAtteindre(ptAtterrissage.y);
            log("Temps pour atteindre le sol:", f(t));
            double v = deplV2.getVitesseEnTemps(t);
            log(" > Vitesse:", f(v));
        }

        if (SYSTEME_ANTI_CRASH == 2) {
            double t = deplV.quelTempsPourRalentissement(-39, 4+g);
            log("TempsPourRalentissement:", f(t));
            double posFuture = deplV.getPositionEnTemps(t);
            log("position future + 10s:", f(posFuture+10));
        
            if (t > 0 && posFuture < ptAtterrissage.y) {
                vecteurAcc.x = 0;
                vecteurAcc.y = 4;
                log("!!!! Risque de crash !! : correction:", vecteurAcc.showVector());
            }
        }

        commandesSuivantes(vecteurAcc);   
    }
    
    void commandesSuivantes(Coords vecteurAcc) {
        // On ne peut pas mettre les réacteurs vers le bas
        if (vecteurAcc.y < 0) vecteurAcc.y = 0;
        // Iversion du sens de la réaction
        Coords vecteurReact = new Coords(-vecteurAcc.x, -vecteurAcc.y);
        log("Reaction: ", vecteurReact.showVector());
         
        // Détermination de l'angle
        
        AngleEnDegres angleDegre;
        if (Y - ptAtterrissage.y < ALT_ATTERRISSAGE) {
            log("Ground ", Y - ptAtterrissage.y);
            angleDegre = new AngleEnDegres(0);
        } else {
            angleDegre = new AngleEnDegres().ofRadian(vecteurReact.getVAngle());
            log("reaction angle(degres): ", angleDegre);
            angleDegre.ajoute(90);
            //log("remise dans le ref du jeu: ", angleDegre);
        }
        newAngle = angleDegre.getIntAngle();
        normalizeAngle();
        
        // Détermination de la puissance
        
        newThrust = Math.round(vecteurReact.getVNorme());
        normalizeThrust();
        
        if (!angleDegre.estProcheDe(newAngle, MAX_DELTA_ANGLE) && newThrust > 0) {
            log("Pas de thrust car l'angle n'est pas le bon.", newAngle, "pour", angleDegre);
            newThrust = 0;
        } else if (vecteurReact.getVNorme() < 0.5) {
            log("Remise a zero de l'angle car le thrust est proche de zéro");
            newAngle = angleDegre.getIntAngle();
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
        
        if (ALGO_SIMPLE_ATTER) {
            if (X >= ptAttDebut && X <= ptAttFin && Y < ptAtterrissage.y + 1500) {
                log("!! Forçage atterissage");
                //ALGO_SIMPLE_ATTER = false;
                return new PtPassage(X, ptAtterrissage.y, 20, 59);
            }
        }
        
        PtPassage ptPassageSuivant = ptPassages.peek();
        
        // Abandon du point de passage en cours et choix du point de passage suivant
        // si l'actuel est atteint ou si le suivant est plus proche.
        
        if (ptPassages.size() > 1) {
            if (position.distance(ptPassageSuivant) < DISTANCE_CIBLE_ATTEINTE
                    || position.distance(ptPassages.get(1)) < ptPassages.get(0).distance(ptPassages.get(1)) * RATIO_ABANDON_CIBLE) {
                ptPassages.poll();
                ptPassageSuivant = ptPassages.peek();
                log("Changement target:", ptPassageSuivant);
            }
        }

        return ptPassageSuivant;
    }

    private void creatPtPassages() {
        ptPassages = new LinkedList<>();
        
        CourbeDeBezierOrdre2 cdb = new CourbeDeBezierOrdre2(position, new Coords(ptAtterrissage.x, position.y), ptAtterrissage);
        List<Coords> points = cdb.getPointsPourDistanceMax(DISTANCE_POINTS);
        
        points.stream().skip(1).forEach(p ->
            ptPassages.add(new PtPassage(p.x, p.y, VIT_H, 39))
        );
        
        double somDistance = 0;
        for (int i=1; i <points.size(); i++) {
            log("Distance:", f(points.get(i-1).distance(points.get(i))));
            somDistance += points.get(i-1).distance(points.get(i));
        }
        log("somDistance:", f(somDistance));
        log("Distance cdb:", f(cdb.getLongueur()));
        
        ptPassages.forEach(Player::log);
    }
    
    String f(double value) {
        return String.format("%.2f", value);
    }
    
    class EquationHoraire {
        double X;
        double V;
        double A;
        
        public EquationHoraire(double x, double v, double a) {
            super();
            X = x;
            V = v;
            A = a;
        }
        
        double getPositionEnTemps(double t) {
            return (A * t * t / 2) + (V * t) + X;
        }
        
        double getVitesseEnTemps(double t) {
            return (A * t) + V;
        }
        
        double quelAccellerationPourVitesse(double v, double t) {
            return (v - V) / t; 
        }
        
        double tempsPourAtteindre(double cible) {
            double derniereDist = Double.MAX_VALUE;
            for (int t=1; t<=300; t++) {
                double position = getPositionEnTemps(t);
                if ((X > cible && position < cible) || (X < cible && position > cible)) {
                    return t;
                }
            }
            return -1;
        }
        
        /**
         * Calcul du temps nécessaire pour ralentir.
         * @param v Vitesse cible
         * @param a Puissance de freinage maximum que l'on dispose
         * @return temps en secondes
         */
        double quelTempsPourRalentissement(double v, double a) {
            log("V: ", V, ", v:", v, ", a:", a);
            double deltaV = v - V;
            return deltaV / a;
        }
        
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
         * certaine distance TODO : ajout d'un controle si on sort de la carte ou pas
         * 
         * @param base     Point opposé à celui qu'on veut calculer
         * @param distance Distance du point que l'on veut à partir d'ici
         */
        Coords getPointOppose(Coords base, double distance) {
            Coords vector2d = createVFromPoints(base, this);
            return vector2d.doVtranslation(this, distance);
        }

        Coords getPointVers(Coords base, double distance) {
            return getPointOppose(base, -distance);
        }
        
        String f(double value) {
            return String.format("%.2f", value);
        }

        @Override
        public String toString() {
            return "Coords [x=" + f(x) + ", y=" + f(y) + "]";
        }

        // ---- Fonctions vectorielles (on considère ici que l'objet Coords est un
        // vecteur)
        
        Coords createVecteurTo(Coords to) {
            return new Coords(to.x - x, to.y - y);
        }
        
        Coords createVecteurFrom(Coords from) {
            return new Coords(x - from.x, y - from.y);
        }

        /* static */ Coords createVFromPoints(Coords from, Coords to) {
            return new Coords(to.x - from.x, to.y - from.y);
        }

        /* static */ Coords createVFromFormeTrigono(double angle, double norme) {
            return new Coords(Math.cos(angle) * norme, Math.sin(angle) * norme);
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
            return "Vecteur [x=" + f(x) + ", y=" + f(y) + ", angle=" + f(getVAngle()) + ", dist=" + f(getVNorme()) + "]";
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