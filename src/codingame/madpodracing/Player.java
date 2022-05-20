package codingame.madpodracing;

import java.util.*;
import java.io.*;

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/
class Player {
    static boolean doLog = true;
    static boolean doLogDebug = false;
    private static final int MAX_X = 16_000;
    private static final int MAX_Y = 9_000;
    private static final double FROTTEMENT = 0.8491D;
    private static final int MAX_ANGLE = 40;
    
    private static final int VITESSE_MAX = 500;

    boolean boostFait = false;
    
    Etat etat;
    Etat etatPrec = null;
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);
        
        while (true) {
            etat = new Etat();
            etat.pod.x = in.nextInt();
            etat.pod.y = in.nextInt();
            etat.nextCheckpoint.x = in.nextInt(); // x position of the next check point
            etat.nextCheckpoint.y = in.nextInt(); // y position of the next check point
            etat.nextCheckpointDist = in.nextInt(); // distance to the next checkpoint
            etat.nextCheckpointAngle = in.nextInt(); // angle between your pod orientation and the direction of the next checkpoint
            etat.opponent.x = in.nextInt();
            etat.opponent.y = in.nextInt();
            
            System.err.println(etat.pod.x + " " + etat.pod.y + " "  + etat.nextCheckpoint.x + " " + etat.nextCheckpoint.y + " "  + etat.nextCheckpointDist + " "  + etat.nextCheckpointAngle + " " + etat.opponent.x + " " + etat.opponent.y);
            etat.ordre = new Ordre();
            
            strategie();
            
            System.out.println(etat.ordre.toCommand());
            etatPrec = etat;
            
        }
    }
    
    private void strategie() {
        
        // Neurones couche 1 = Enrichissement
        enrichissement();
        
        // Neurones couche 2 = Stratégie élaborée    
        strategieBoostEnLigneDroite();
        strategieCoupureThrustSurCheckpoint();
        strategieCorrectionDerive();
        // TODO : strategieDemiTour();
        // TODO : strategieBumperPostCheckpoint
        
        // Neurones couche 3 = Stratégies minimalistes (par défaut)
        strategieCibleParDefaut();
        defautThrust();
        
        // Neurone de sortie
        ordreThrust();
        
    }

    private void enrichissement() {
        // Validation des conditions d'activation du neurone
        if (etatPrec != null) /* continue */;
        else return;
        
        etat.vitesse = etatPrec.pod.distance(etat.pod);
        
        // Calcul vitesse relative au nextpoint
        
        double distCheckPoint = etat.pod.distance(etat.nextCheckpoint);
        double distCheckPointPrec = etatPrec.pod.distance(etat.nextCheckpoint);
        
        logDebug("distCheckPoint:", distCheckPoint, "distCheckPointPrec:", distCheckPointPrec);
        
        etat.vitesseRelative = distCheckPointPrec - distCheckPoint;
        log("vitesse:", etat.vitesse, "vitesseRelative:", etat.vitesseRelative);
    }

    private void strategieBoostEnLigneDroite() {
        // Validation des conditions d'activation du neurone
        if (boostFait) return;
        
        double distPodToOponent = etat.pod.distance(etat.opponent);
        double distPodToCheckpoint = etat.pod.distance(etat.nextCheckpoint);
        log("distPodToOponent:", distPodToOponent, "distPodToCheckpoint:", distPodToCheckpoint);
        
        if (distPodToOponent > 2_000
            && distPodToCheckpoint > 5_000
            && Math.abs(etat.nextCheckpointAngle) < 3) /* continue */;
        else return;
        
        log("---- Strategie Boost En Ligne Droite ----");
        
        // Calculs
        
        // Données en sortie
        etat.ordre.boost = true;
    }

    private void strategieCoupureThrustSurCheckpoint() {
        // Validation des conditions d'activation du neurone
        if (etatPrec != null) /* continue */;
        else return;
        
        int t100 = tempsArriveeCheckpoint(etat.pod, etat.vitesse, etat.nextCheckpoint, 100);
        int t0 = tempsArriveeCheckpoint(etat.pod, etat.vitesse, etat.nextCheckpoint, 0);

        log("Temps d'arrivee pour thrust = 100 :", t100);
        log("Temps d'arrivee pour thrust = 0 :", t0);
        
        if (t100 == t0 || t0 < 3) {
            log("---- Strategie Coupure Thrust ----");
            etat.newThrust = 0;
            log("Arret des thrusts", (t100 >= t0), (t0 < 3));
        }
    }
        
    private void strategieCorrectionDerive() {
        // Validation des conditions d'activation du neurone
        if (etatPrec != null) /* continue */;
        else return;
        
        log("---- Strategie Correction Derive ----");
        
        Coords vecteurDirection = etat.pod.createVecteurTo(etat.nextCheckpoint);
        log("vecteurDirection", vecteurDirection.showVector());
        log("dir pod", etat.nextCheckpointAngle);
        
        Coords vecteurVitesse = etatPrec.pod.createVecteurTo(etat.pod);
        Coords vecteurDeCorrection = new Coords(-vecteurVitesse.x, -vecteurVitesse.y);
        Coords cible = etat.nextCheckpoint.doVtranslation(vecteurDeCorrection);
        
        Coords vecteurOptimal = etat.pod.getVecteurVers(cible, etat.nextCheckpointDist);
        etat.ordre.cible = etat.pod.doVtranslation(vecteurOptimal);

        logDebug("vecteurVitesse", vecteurVitesse.showVector());
        logDebug("vecteurDeCorrection", vecteurDeCorrection.showVector());
        logDebug("cible", cible);
        logDebug("vecteurOptimal", vecteurOptimal.showVector());
        
    }

    private void strategieCibleParDefaut() {
        // Validation des conditions d'activation du neurone
        if (etat.ordre.cible == null) /* continue */;
        else return;

        log("---- Strategie Cible Par Defaut ----");
        
        // Calculs
        
        // Données en sortie
        log("cible par defaut:", etat.nextCheckpoint);
        etat.ordre.cible = etat.nextCheckpoint;
    }

    private void defautThrust() {
        // Validation des conditions d'activation du neurone
        if (etat.ordre.boost) {
            return;
        }
        
        if (Double.isNaN(etat.newThrust)) {
            log("---- Strategie Defaut Thrust ----");
            etat.newThrust = 100;
        }
        
    }

    private void ordreThrust() {
        // Validation des conditions d'activation du neurone
        if (etat.ordre.boost) {
            boostFait = true;
            return;
        }
          
        etat.ordre.thrust = Math.max(Math.min((int)etat.newThrust, 100), 0);
        if (Math.abs(etat.nextCheckpointAngle) > MAX_ANGLE) etat.ordre.thrust = 0;
        log("thrust brut:", etat.newThrust, " > ", etat.ordre.thrust);
        
    }

    int tempsArriveeCheckpoint(Coords from, double vit, Coords cible, int thrust) {
        int ret = 0;
        double distance = from.distance(cible);
        double newVit = vit;
        while (ret < 40 && Math.abs(distance) > 400) {
            newVit = thrust + newVit * FROTTEMENT;
            distance = distance - newVit;
            ret++; 
        }
        
        return ret;
    }
    
    class Etat {
        Coords pod = new Coords();
        Coords nextCheckpoint = new Coords();
        int nextCheckpointDist; // distance to the next checkpoint
        int nextCheckpointAngle; // angle between your pod orientation and the direction of the next checkpoint
        Coords opponent = new Coords();
        double vitesse = Double.NaN;
        double vitesseRelative = Double.NaN;
        double newThrust = Double.NaN;
        Ordre ordre = new Ordre();
    }
    
    class Ordre {
        Coords cible = null;
        boolean boost = false;
        int thrust = 50;
        
        String toCommand() {
            return ""+cible.getIntX()+" "+cible.getIntY()+" "+(boost?"BOOST":thrust);
        }
    }
    
    class Coords {
    
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
/*    
        Interception getInterception(Pion m) {
            return new Interception(this, m);
        }
*/    
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
         * Retourne un point vers le point2 à une certaine distance.
         */
        
        Coords getPointVers(Coords point2, double distance) {
            return this.doVtranslation(getVecteurVers(point2, distance));
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
        
        double getVAngleDegres() {
            return (360 * getVAngle()) / (2 * Math.PI);
        }
   
        Coords doVtranslation(Coords from) {
            return new Coords(from.x + x, from.y + y);
        }
    
        Coords doVtranslation(Coords from, double distance) {
            double angle = getVAngle();
            return new Coords(from.x + Math.cos(angle) * distance, from.y + Math.sin(angle) * distance);
        }
        
        /**
         * Retourne un point vers le point2 à une certaine distance.
         */
        
        Coords getVecteurVers(Coords point2, double distance) {
            Coords vectorVersPoint2 = createVecteurTo(point2);
            return new Coords().createVFromFormeTrigono(vectorVersPoint2.getVAngle(), distance);
        }
        
        Coords ajouteVecteur(Coords v2) {
            return new Coords(this.x + v2.x, this.y + v2.y);
        }
        
        Coords soustraitVecteur(Coords v2) {
            return new Coords(this.x - v2.x, this.y - v2.y);
        }
        
        public String showVector() {
            return String.format("Coords [x=%.2f, y=%.2f, angle=%.2f(%.2f°), norme=%.2f]", x, y, getVAngle(), getVAngleDegres(), getVNorme());
        }
    
    }    

    public class DistanceToBaseComparator implements Comparator<Coords> {
        int ASC = 1;
        int DESC = -1;
        
        Coords base;
        int order;
           
        public DistanceToBaseComparator(Coords base, int order) {
            super();
            this.base = base;
            this.order = order;
        }

        @Override
        public int compare(Coords arg0, Coords arg1) {
            return arg0.distance(base) < arg1.distance(base) ? -1 * order : 1 * order;
        }

    }
    
    /* static */ Comparator<Coords> duPlusProcheAuPlusLoin(Coords base) {
        return new DistanceToBaseComparator(base, 1);
    }

    /* static */ Comparator<Coords> duPlusLoinAuPlusProche(Coords base) {
        return new DistanceToBaseComparator(base, -1);
    }
    
    /* Codingame common */
    
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
    
    static void logDebug(Object... objects) {
        if (doLogDebug) {
            System.err.print("*");
            for (Object o : objects) {
                System.err.print(o.toString() + " ");
            }
            System.err.println();
        }
    }
    
    static void activateLogDebug() {
        doLogDebug = true;
    }
    
    public static void main(String args[]) {
        Player player = new Player();
        player.joue(System.in);
    }
    
}