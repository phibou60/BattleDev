package codingame.codevszombies;

import java.util.*;
import java.io.*;

/**
 * Save humans, destroy zombies!
 **/
class Player {

    int MAX_X = 16_000;
    int MAX_Y = 9_000;
    
    Coords ash = new Coords();
    LinkedList<Pion> humans;
    LinkedList<Pion> zombies;
    
    Coords coup;
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);

        // game loop
        while (true) {
            ash.x = in.nextInt();
            ash.y = in.nextInt();
            
            System.err.printf("%d %d%n", (int) ash.x, (int) ash.y);
            
            int humanCount = in.nextInt();
            System.err.println(""+humanCount);

            humans = new LinkedList<>();
            for (int i = 0; i < humanCount; i++) {
                int humanId = in.nextInt();
                int humanX = in.nextInt();
                int humanY = in.nextInt();
                humans.add(new Pion(humanId, humanX, humanY, 0, 0)); 
                System.err.printf("%d %d %d%n", humanId, humanX, humanY);
            }
            
            int zombieCount = in.nextInt();
            System.err.println(""+zombieCount);
            
            zombies = new LinkedList<>();
            for (int i = 0; i < zombieCount; i++) {
                int zombieId = in.nextInt();
                int zombieX = in.nextInt();
                int zombieY = in.nextInt();
                int zombieXNext = in.nextInt();
                int zombieYNext = in.nextInt();
                zombies.add(new Pion(zombieId, zombieX, zombieY, zombieXNext, zombieYNext));
                System.err.printf("%d %d %d %d %d%n", zombieId, zombieX, zombieY, zombieXNext, zombieYNext);
            }
            
            coup = null;
            strategie();
            
            System.out.printf("%d %d%n", (int) coup.x, (int) coup.y);
        }
    }

    private void strategie() {
        strategiePanic();
        strategieTranquille();
        
    }

    private void strategiePanic() {
        if (zombies.size() == 0) return;
        
        // Chercher un humain en danger mais sauvable
        
        double minRatio = Double.MAX_VALUE;
        
        for (Pion human : humans) {
            Pion zombie = zombies.stream().sorted(duPlusProcheAuPlusLoin(human)).findFirst().get();
            double ratio = human.distance(ash) / human.distance(zombie);
            log("ratio:", ratio);
            if (ratio >= 2D && ratio < 4.1D) {
                if (ratio < minRatio) {
                    coup = human;
                    minRatio = ratio;
                    log("coup:", coup);
                }
            }
        }
        
    }

    private void strategieTranquille() {
        if (coup != null) return;
        
        Optional<Pion> zombie = zombies.stream().sorted(duPlusProcheAuPlusLoin(ash))
                .findFirst();
        
        if (zombie.isPresent()) {
            coup = zombie.get();
        }
    }

    /**
     * Classe permettant d'avoir des fonctions de gestion des points et des
     * déplacements.
     *
     */

    /* Coords staticCoords = new Coords(); */

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
            if (this instanceof Pion) {
                ((Pion) this).distanceToTarget = dist;
            }
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
        
        public String showVector() {
            return String.format("Coords [x=%.2f, y=%.2f, angle=%.2f, norme=%.2f]", x, y, getVAngle(), getVNorme());
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
    
    public class Pion extends Coords {
        int id;
        Coords nextCoords;
        double distanceToTarget;

        public Pion(int id, double x, double y, int nextX, int nextY) {
            super(x, y);
            this.id = id;
            this.nextCoords = new Coords(nextX, nextY);
        }

    }
    
    /* Codingame common */
    
    static boolean doLog = false;
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