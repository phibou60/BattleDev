import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

class Player {

    static boolean doLog = false;
    
    int nbFloors;
    int width;
    int nbRounds;
    int exitFloor;
    int exitPos;
    int nbTotalClones;
    int nbAdditionalElevators;
    int nbElevators;
    
    List<Elevator> elevators;

    public static void main(String args[]) {
        Player player = new Player();
        player.joue(System.in);
    }
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);

        nbFloors = in.nextInt(); // number of floors
        width = in.nextInt(); // width of the area
        nbRounds = in.nextInt(); // maximum number of rounds
        exitFloor = in.nextInt(); // floor on which the exit is found
        exitPos = in.nextInt(); // position of the exit on its floor
        nbTotalClones = in.nextInt(); // number of generated clones
        nbAdditionalElevators = in.nextInt(); // number of additional elevators that you can build
        nbElevators = in.nextInt(); // number of elevators
        
        elevators = new ArrayList<>(nbElevators);
                
        System.err.println(nbFloors+" "+width+" "+nbRounds+" "+exitFloor+" "+exitPos+" "
                +nbTotalClones+" "+nbAdditionalElevators+" "+nbElevators);
                
        for (int i = 0; i < nbElevators; i++) {
            
            int elevatorFloor = in.nextInt(); // floor on which this elevator is found
            int elevatorPos = in.nextInt(); // position of the elevator on its floor
            
            System.err.println(elevatorFloor+" "+elevatorPos);
            
            elevators.add(new Elevator(elevatorFloor, elevatorPos));
        }

        // game loop
        while (true) {
            long timeStart = System.nanoTime();
            
            int cloneFloor = in.nextInt(); // floor of the leading clone
            int clonePos = in.nextInt(); // position of the leading clone on its floor
            String direction = in.next(); // direction of the leading clone: LEFT or RIGHT

            Etat etat = new Etat(cloneFloor, clonePos, direction, nbRounds, nbTotalClones, nbAdditionalElevators);

            System.err.println(cloneFloor+" "+clonePos+" "+direction);
            
            if (cloneFloor == -1) {
                System.out.println("WAIT");
            } else {
                Coup coup = strategie(etat);

                if (coup.ordre.equals("ELEVATOR")) {
                    elevators.add(new Elevator(cloneFloor, clonePos));
                    nbAdditionalElevators--;
                }

                if (coup.ordre.equals("BLOCK")) {
                    nbTotalClones--;
                }
               
                System.err.println("Duree: "+Math.floorDiv(System.nanoTime()-timeStart, 1_000_000)+"ms");
                
                System.out.println(coup.ordre);
            }
            nbRounds--;
        }
    }

    private Coup strategie(Etat etat) {

        log("Etat:", etat);
        Coup coup = calculDistanceRecursive(etat, exitFloor);
        
        Coup coupSuiv = coup;
        while (coupSuiv != null) {
            log("coup suiv:", coupSuiv);
            coupSuiv = coupSuiv.coupSuivant;
        }
        
        return coup;
        
    }

    Coup calculDistanceRecursive(Etat etat, int floorFin) {
        String chevrons = "###############################".substring(0, (etat.floor +1)*2);
        
        List<Coup> coups = getCoups(etat);
        
        int dureeLaPlusBasse = 99999;
        Coup coupSelect = null;
        for (Coup coup : coups) {
            log(chevrons, "coup:", coup);
            if (etat.floor +1 < floorFin) {
                Coup coupSuiv = calculDistanceRecursive(coup.etatFinal, floorFin);
                if (coupSuiv == null) continue;
                coup.coupSuivant = coupSuiv;
                coup.duree += coupSuiv.duree;
                coup.nbClones += coupSuiv.nbClones;
                log(chevrons, "> nouvelle duree:", coup.duree, "nbClones:", coup.nbClones);
            }
            if (coup.duree < dureeLaPlusBasse) {
                coupSelect = coup;
                dureeLaPlusBasse = coup.duree;
            }
        }
        log(chevrons, "> meilleur coup:", coupSelect, "duree:", dureeLaPlusBasse);

        return coupSelect;
        
        
    }
    
    List<Coup> getCoups(Etat etat) {
        List<Coup> results = new ArrayList<>();
        List<Elevator> floorElevators = new ArrayList<>();
                
        if (etat.floor == exitFloor) {
            floorElevators.add(new Elevator(exitFloor, exitPos));
        } else {
            floorElevators = elevators.stream()
                .filter(e -> e.floor == etat.floor)
                .collect(Collectors.toList());
        }
        
        if (etat.nbAdditionalElevators > 0 && etat.nbRounds > 3 && etat.nbTotalClones > 1) {
            Etat etatFinal = new Etat(etat.floor+1, etat.pos, etat.direction, etat.nbRounds-3,
                    etat.nbTotalClones-1, etat.nbAdditionalElevators-1);
            
            results.add(new Coup(3, 1, 1, etatFinal, "ELEVATOR"));
        }
            
        for (Elevator elevator : floorElevators) {
            log(" > elevator:", elevator);
            
            Coup coup = new Coup(); 
            coup.ordre = "WAIT";
            coup.etatFinal = new Etat(etat.floor+1, elevator.pos, etat.direction);

            coup.duree = Math.abs(etat.pos - elevator.pos) + 1;
            
            if ((etat.pos < elevator.pos && etat.direction.equals("LEFT"))
                    || (elevator.pos < etat.pos && etat.direction.equals("RIGHT"))) {
                coup.duree += 3;
                coup.nbClones++;
                coup.ordre = "BLOCK";
                coup.etatFinal.direction = etat.direction.equals("LEFT") ? "RIGHT" : "LEFT";
            }

            // Sacrifier un clone sur les ascenseurs traversés
            long ascTraverse = elevators.stream()
                .filter(e -> e.floor == etat.floor)
                .filter(e -> e.pos != elevator.pos)
                .filter(e -> e.pos >= Math.min(etat.pos, elevator.pos))
                .filter(e -> e.pos <= Math.max(etat.pos, elevator.pos))
                .count();
            coup.duree += ascTraverse*3;
            coup.nbClones += ascTraverse;
            
            coup.etatFinal.nbAdditionalElevators = etat.nbAdditionalElevators;
            coup.etatFinal.nbRounds = etat.nbRounds - coup.duree;
            coup.etatFinal.nbTotalClones = etat.nbTotalClones - coup.nbClones;
            
            if (coup.etatFinal.nbRounds > 0 && coup.etatFinal.nbTotalClones > 0) {
                results.add(coup);
            }
        }

        return results;        
        
    }
    
    class Elevator {
        int floor;
        int pos;
        
        public Elevator(int floor, int pos) {
            super();
            this.floor = floor;
            this.pos = pos;
        }
        
        @Override
        public String toString() {
            return "Elevator [floor=" + floor + ", pos=" + pos + "]";
        }
    }
    
    class Etat extends Elevator {
       String direction;
       int nbRounds;
       int nbTotalClones;
       int nbAdditionalElevators;
       
       public Etat(int floor, int pos, String direction) {
           super(floor, pos);
           this.direction = direction;
       }
       
       public Etat(int floor, int pos, String direction, int nbRounds, int nbTotalClones, int nbAdditionalElevators) {
           super(floor, pos);
           this.direction = direction;
           this.nbRounds = nbRounds;
           this.nbTotalClones = nbTotalClones;
           this.nbAdditionalElevators = nbAdditionalElevators;
       }
       
       @Override
       public String toString() {
           return "Etat [floor=" + floor + ", pos=" + pos + ", direction=" + direction + ", nbRounds=" + nbRounds
                   + ", nbTotalClones=" + nbTotalClones + ", nbAddElevators=" + nbAdditionalElevators + "]";
       }
    }
    
    class Coup {
        int duree = 0;
        int nbClones = 0;
        int nbElevators = 0;
        Etat etatFinal;
        String ordre;
        Coup coupSuivant = null;
        
        public Coup() {
            super();
        }
       
        public Coup(int duree, int nbClones, int nbElevators, Player.Etat etatFinal, String ordre) {
            super();
            this.duree = duree;
            this.nbClones = nbClones;
            this.nbElevators = nbElevators;
            this.etatFinal = etatFinal;
            this.ordre = ordre;
        }

        @Override
        public String toString() {
            return "Coup [duree=" + duree + ", nbClones=" + nbClones + ", nbElevators="
                    + nbElevators + ", etatFinal="
                    + etatFinal + ", ordre=" + ordre + "]";
        }
                
    }

    /* Codingame common */

    static boolean doLogDebug = false;
    
    static void log(Object... objects) {
        if (doLog) {
            for (Object o : objects) {
                System.err.print("" + o + " ");
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
                System.err.print("" + o + " ");
            }
            System.err.println();
        }
    }
    
    static void activateLogDebug() {
        doLogDebug = true;
    }
    
}