package codingame.madpodracing;


import java.util.*;
import java.io.*;

/**
 * Player de la ligue argent qui est passé en ligue d'or.
 **/
class PlayerArgent2 {
    static boolean doLog = true;
    static boolean doLogDebug = false;
    private static final int MAX_X = 16_000;
    private static final int MAX_Y = 9_000;
    private static final double FROTTEMENT = 0.8491D;
    private static final int MAX_ANGLE = 80;
 
    boolean boostFait = false;
    ArrayList<Coords> checkpoints = new ArrayList<>();
    boolean checkpointsOK = false;
   
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);
        
        Pod pod;
        Pod podPrec = null;
     
        while (true) {
            pod = new Pod();
            pod.x = in.nextInt();
            pod.y = in.nextInt();
            pod.nextCheckpoint.x = in.nextInt(); // x position of the next check point
            pod.nextCheckpoint.y = in.nextInt(); // y position of the next check point
            pod.nextCheckpointDist = in.nextInt(); // distance to the next checkpoint
            pod.nextCheckpointAngle = in.nextInt(); // angle between your pod orientation and the direction of the next checkpoint
            pod.opponent.x = in.nextInt();
            pod.opponent.y = in.nextInt();
            
            System.err.println((int) pod.x + " " + (int) pod.y + " "  + (int) pod.nextCheckpoint.x
                    + " " + (int) pod.nextCheckpoint.y + " "  + (int) pod.nextCheckpointDist
                    + " "  + (int) pod.nextCheckpointAngle + " " + (int) pod.opponent.x + " " + (int) pod.opponent.y);
            pod.ordre = new Ordre();
            
            strategie(pod, podPrec);
            
            System.out.println(pod.ordre.toCommand());
            podPrec = pod;
            
        }
    }
    
    private void strategie(Pod pod, Pod podPrec) {
        
        // Neurones couche 1 = Enrichissement
        recupInfosCircuit(pod, podPrec);
        enrichissement(pod, podPrec);
        
        // Neurones couche 2 = Stratégie élaborée    
        strategieBoostEnLigneDroite(pod, podPrec);
        strategieCoupureThrustSurCheckpoint(pod, podPrec);
        strategieCorrectionDerive(pod, podPrec);
        strategieEpauleContreEpaule(pod, podPrec);
        // TODO : strategieDemiTour
        // TODO : strategieBumperPostCheckpoint
        
        // Neurones couche 3 = Stratégies minimalistes (par défaut)
        strategieCibleParDefaut(pod, podPrec);
        defautThrust(pod, podPrec);
        
        // Neurone de sortie
        ordreThrust(pod, podPrec);
        
    }

    private void recupInfosCircuit(Pod pod, Pod podPrec) {
        for (int i=0; i<checkpoints.size(); i++) {
            if (checkpoints.get(i).equals(pod.nextCheckpoint)) {
                if (i < checkpoints.size()-1) {
                    pod.futureCheckpoint = checkpoints.get(i+1);
                } else {
                    pod.futureCheckpoint = checkpoints.get(0);
                }
                break;
            }
        }
        pod.angleFutureCheckpoint = pod.nextCheckpoint.angleVecteurs(pod, pod.futureCheckpoint);
        log(">> future checkpoint:", pod.futureCheckpoint, "angleFutureCheckpoint:", pod.angleFutureCheckpoint);
    }

    private void enrichissement(Pod pod, Pod podPrec) {
        
        pod.distOpponentAuCheckpoint = pod.opponent.distance(pod.nextCheckpoint);
        pod.distOpponentAuPod = pod.opponent.distance(pod);
        
        // Validation des conditions d'activation du neurone
        if (podPrec != null) /* continue */;
        else return;
        
        pod.vitesse = podPrec.distance(pod);
        
        //---- Calcul vitesse relative au nextCheckpoint
        
        double distCheckPoint = pod.distance(pod.nextCheckpoint);
        double distCheckPointPrec = podPrec.distance(pod.nextCheckpoint);
        
        logDebug("distCheckPoint:", distCheckPoint, "distCheckPointPrec:", distCheckPointPrec);
        
        pod.vitesseRelative = distCheckPointPrec - distCheckPoint;
        log("vitesse:", pod.vitesse, "vitesseRelative:", pod.vitesseRelative);
    }

    private void strategieBoostEnLigneDroite(Pod pod, Pod podPrec) {
        // Validation des conditions d'activation du neurone
        if (boostFait) return;
        
        double distPodToOponent = pod.distance(pod.opponent);
        log("distPodToOponent:", distPodToOponent, "distPodToCheckpoint:", pod.nextCheckpointDist);
        
        if (distPodToOponent > 2_000
            && pod.nextCheckpointDist > 5_000
            && Math.abs(pod.nextCheckpointAngle) < 3) /* continue */;
        else return;
        
        log("---- Strategie Boost En Ligne Droite ----");
        
        // Calculs
        
        // Données en sortie
        pod.ordre.boost = true;
    }

    private void strategieCoupureThrustSurCheckpoint(Pod pod, Pod podPrec) {
        // Validation des conditions d'activation du neurone
        if (podPrec != null
                && pod.angleFutureCheckpoint != null
                && Math.abs(pod.angleFutureCheckpoint.angle) < 80) /* continue */;
        else {
            log("return");
            return;
        }
        int t100 = tempsArriveeCheckpoint(pod, pod.vitesse, pod.nextCheckpoint, 100);
        int t0 = tempsArriveeCheckpoint(pod, pod.vitesse, pod.nextCheckpoint, 0);

        log("Temps d'arrivee pour thrust = 100 :", t100);
        log("Temps d'arrivee pour thrust = 0 :", t0);
        
        if (t100 == t0 || t0 < 3) {
            log("---- Strategie Coupure Thrust ----");
            pod.newThrust = 0;
            log("Arret des thrusts", (t100 >= t0), (t0 < 3));
            
            if (pod.futureCheckpoint != null) {
                log("---- bascule vers checkpoint future:", pod.futureCheckpoint);
                pod.ordre.cible = pod.futureCheckpoint;
            }
        }
    }
         
    private void strategieCorrectionDerive(Pod pod, Pod podPrec) {
        // Validation des conditions d'activation du neurone
        if (podPrec != null && pod.nextCheckpointDist > 2_000) /* continue */;
        else return;
        
        Coords vecteurDirection = pod.createVecteurVers(pod.nextCheckpoint);
        logDebug("vecteurDirection", vecteurDirection.showVector());
        logDebug("dir pod", pod.nextCheckpointAngle);
        
        Coords vecteurVitesse = podPrec.createVecteurVers(pod);
        logDebug("vecteurVitesse", vecteurVitesse.showVector());

        double ecart = vecteurDirection.getVAngleDegres().retire(vecteurVitesse.getVAngleDegres()).angle;
        logDebug("Ecart", ecart);
        if (Math.abs(ecart) < 6) return;
        
        Coords vecteurDeCorrection = new Coords(-vecteurVitesse.x*2, -vecteurVitesse.y*2);
        Coords cibleCorrige = pod.nextCheckpoint.doVtranslation(vecteurDeCorrection);
        Coords vecteurOptimal = pod.createVecteurVers(cibleCorrige, pod.nextCheckpointDist);
        Coords newCible = pod.doVtranslation(vecteurOptimal);
        double distCibleVsNextCheckpoint = newCible.distance(pod.nextCheckpoint);
        
        logDebug("vecteurDeCorrection", vecteurDeCorrection.showVector());
        logDebug("cibleCorrige", cibleCorrige);
        logDebug("vecteurOptimal", vecteurOptimal.showVector());
        logDebug("Dist cible vs nextCheckpoint", distCibleVsNextCheckpoint);
        if (distCibleVsNextCheckpoint < 100) return;

        log("---- Strategie Correction Derive ----");
        
        pod.ordre.cible = newCible;
    }

    private void strategieEpauleContreEpaule(Pod pod, Pod podPrec) {
        logDebug("** Distance nextCheckpoint:", pod.nextCheckpointDist);
        logDebug("** Distance opponent nextCheckpoint:", pod.distOpponentAuCheckpoint);
        logDebug("** Distance opponent//pod:", pod.distOpponentAuPod);
        
        double angle = pod.angleVecteurs(pod.nextCheckpoint, pod.opponent).angle;
        log("** Angle:", angle);
        
        double deltaDistanceVersCheckpoint = Math.abs(pod.nextCheckpointDist - pod.distOpponentAuCheckpoint);
        log("** deltaDistanceVersCheckpoint:", deltaDistanceVersCheckpoint);
        log("** cible actuelle:", ""+pod.ordre.cible);
        
        if (pod.ordre.cible == null
            && Math.abs(angle) > 40
            && Math.abs(pod.nextCheckpointDist - pod.distOpponentAuCheckpoint) < 1_000) /* continue */;
        else return; 

        log("---- Strategie Epaule Contre Epaule ----");
                        
        Coords v = pod.nextCheckpoint.createVecteurVers(pod, 400);
        if (angle > 0) 
            v = v.rotation(-Math.PI / 2);
        else v = v.rotation(Math.PI / 2);
        pod.ordre.cible = v.doVtranslation(pod.nextCheckpoint);
    }
    
    private void strategieCibleParDefaut(Pod pod, Pod podPrec) {
        // Validation des conditions d'activation du neurone
        if (pod.ordre.cible == null) /* continue */;
        else return;

        log("---- Strategie Cible Par Defaut ----");
        
        // Calculs
        
        // Données en sortie
        log("cible par defaut:", pod.nextCheckpoint);
        pod.ordre.cible = pod.nextCheckpoint;
    }

    private void defautThrust(Pod pod, Pod podPrec) {
        // Validation des conditions d'activation du neurone
        if (pod.ordre.boost) {
            return;
        }
        
        if (Double.isNaN(pod.newThrust)) {
            log("---- Strategie Defaut Thrust ----");
            pod.newThrust = 100;
        }
        
    }

    private void ordreThrust(Pod pod, Pod podPrec) {
        // Validation des conditions d'activation du neurone
        if (pod.ordre.boost) {
            boostFait = true;
            return;
        }
          
        pod.ordre.thrust = Math.max(Math.min((int)pod.newThrust, 100), 0);
        if (Math.abs(pod.nextCheckpointAngle) > MAX_ANGLE) pod.ordre.thrust = 0;
        log("thrust brut:", pod.newThrust, " > ", pod.ordre.thrust);
        
    }

    int tempsArriveeCheckpoint(Coords from, double vit, Coords cible, int thrust) {
        int ret = 0;
        double distance = from.distance(cible);
        double newVit = vit;
        while (ret < 40 && Math.abs(distance) > 600) {
            newVit = thrust + newVit * FROTTEMENT;
            distance = distance - newVit;
            ret++; 
        }
        
        return ret;
    }
    
    class Pod extends Coords {
        Coords nextCheckpoint = new Coords();
        int nextCheckpointDist; // distance to the next checkpoint
        int nextCheckpointAngle; // angle between your pod orientation and the direction of the next checkpoint

        Coords futureCheckpoint = null;
        AngleEnDegres angleFutureCheckpoint = null;
        
        double vitesse = Double.NaN;
        double vitesseRelative = Double.NaN;
        double newThrust = Double.NaN;

        Coords opponent = new Coords();
        double distOpponentAuCheckpoint = Double.NaN; 
        double distOpponentAuPod; 

        Ordre ordre = new Ordre();
    }
    
    class Ordre {
        Coords cible = null;
        boolean boost = false;
        int thrust;
        
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

        /* Constructeur a partir de radian */
        /* static */ public AngleEnDegres ofRadian(double radian) {
            double temp = (radian / (2 * Math.PI)) * 360; 
            return new AngleEnDegres(temp);
        }
        
        public double toRadian() {
            return (angle / 360) * (2 * Math.PI); 
        }

        int getIntAngle() {
            return new Double(angle).intValue();
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

        public AngleEnDegres ajoute(AngleEnDegres angle2) {
            return ajoute(angle2.angle);
        }

        public AngleEnDegres retire(double angle2) {
            angle -= angle2;
            normalize();
            return this;
        }

        public AngleEnDegres retire(AngleEnDegres angle2) {
            return retire(angle2.angle);
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
        PlayerArgent2 player = new PlayerArgent2();
        player.joue(System.in);
    }
    
}