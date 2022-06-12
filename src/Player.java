import java.io.InputStream;
import java.util.*;

/**
 * Principe : au premier passage on calcule le plan entier en établissant un arbre de tous les cas possible 
 * à l'aide d'une fonction récursive.
 */

 // TODO : Le code fonctionne mais sur Piège (test 08), il calcule de réussir le puzzle en 64 rounds alors
 // qu'il le fait en réalité en 67.

class Player {

    static boolean doLog = false;
    static String[] logFilters = null; // new String[] {"##11:", "####w10:", "##x####2:", "coup suiv"};
    
    int nbFloors;
    int width;
    int nbRounds;
    int exitFloor;
    int exitPos;
    int nbTotalClones;
    int nbAdditionalElevators;
    int nbElevators;
    
    Coup planComplet = null;
    
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
        int i = 1;
        while (true) {
            long timeStart = System.nanoTime();
            
            int cloneFloor = in.nextInt(); // floor of the leading clone
            int clonePos = in.nextInt(); // position of the leading clone on its floor
            String direction = in.next(); // direction of the leading clone: LEFT or RIGHT

            System.err.println(cloneFloor+" "+clonePos+" "+direction);
            System.err.println("Round: "+i);

            if (cloneFloor == -1) {
                // Coup forcé
                System.out.println("WAIT");
            } else {
                Etat etat = new Etat(cloneFloor, clonePos, direction, nbRounds, nbTotalClones, nbAdditionalElevators);
                Coup coup = strategie(etat);

                if (coup.ordre.equals("ELEVATOR")) {
                    elevators.add(new Elevator(cloneFloor, clonePos));
                }
               
                System.err.println("Duree: "+Math.floorDiv(System.nanoTime()-timeStart, 1_000_000)+"ms");
                System.out.println(coup.ordre);
            }
            i++;
        }

    }

    private Coup strategie(Etat etat) {

        if (planComplet == null) {
            recherchePlanComplet(etat);
            return planComplet;
        } else {
            return appliquerLePlan(etat);
        }
        
    }

    /**
     * Ce code ne fait qu'appliquer le plan déjà défini.
     */
    private Coup appliquerLePlan(Player.Etat etat) {
        
        // Recherche de l'ascenceur à prendre
        
        Elevator elevator = null;
        Coup coupSuiv = planComplet;
        while (coupSuiv != null) {
            if (coupSuiv.elevator.floor == etat.floor) {
                elevator = coupSuiv.elevator;
                logDebug("Ascenceurt a prendre:", elevator);
                break;
            }
            logDebug("coup suiv:", coupSuiv);
            coupSuiv = coupSuiv.coupSuivant;
        }
        
        boolean surUnElevator = estOnSurUnElevator(etat);
        
        Coup coup = new Coup();
        coup.ordre = "WAIT";
        
        if (etat.pos == elevator.pos && !surUnElevator) {
            coup.ordre = "ELEVATOR"; // On est sur le bon elevator mais il n'y en a pas encore 
        } else  if (etat.pos == elevator.pos) {
            coup.ordre = "WAIT"; // On est sur le bon elevator
        } else if ((etat.pos < elevator.pos && etat.direction.equals("LEFT"))
                || (elevator.pos < etat.pos && etat.direction.equals("RIGHT"))) {
            coup.ordre = "BLOCK";
        }
        
        return coup;
    }

    /**
     * Check si on est actuellement sur un ascenceur.
      */
    private boolean estOnSurUnElevator(Etat etat) {
        return elevators.stream()
                .anyMatch(e -> e.floor == etat.floor && e.pos == etat.pos);
    }
    
    /**
     * Calcul du plan complet.
     */
    private void recherchePlanComplet(Etat etat) {
        log("Etat:", etat);
        planComplet = calculDistanceRecursive(etat, exitFloor);
        
        // log du plan complet
        Coup coupSuiv = planComplet;
        while (coupSuiv != null) {
            System.err.println("coup suiv: " + coupSuiv);
            coupSuiv = coupSuiv.coupSuivant;
        }
    }

    /**
     * Genère les coups possibles d'un niveau et appelle pour chaque coups la même fonction pour le niveau suivant.
     */
    Coup calculDistanceRecursive(Etat etat, int floorFin) {
        String chevrons = "###############################".substring(0, (etat.floor +1)*2);
        chevrons += etat.floor + ":";
        logDebug(chevrons, "etat:", etat);
        
        List<Coup> coups = getCoups(etat);
        
        int dureeLaPlusBasse = 99999;
        Coup coupSelect = null;
        int i = 1;
        for (Coup coup : coups) {
            logDebug(chevrons, "> coup: (", i, "/", coups.size(), ")", coup);
            if (etat.floor < floorFin) {
                Coup coupSuiv = calculDistanceRecursive(coup.etatFinal, floorFin);
                if (coupSuiv == null) continue;
                coup.coupSuivant = coupSuiv;
                coup.duree += coupSuiv.duree;
                coup.nbClones += coupSuiv.nbClones;
                logDebug(chevrons, "> nouvelle duree:", coup.duree, "nbClones:", coup.nbClones);
            }
            if (coup.duree < dureeLaPlusBasse) {
                coupSelect = coup;
                dureeLaPlusBasse = coup.duree;
            }
            i++;
        }
        logDebug(chevrons, "> meilleur coup:", coupSelect, "duree:", dureeLaPlusBasse);

        return coupSelect;
    }
    
    List<Coup> getCoups(Etat etat) {
        if (etat.floor == 10 && etat.pos == 23 && etat.nbAdditionalElevators > 1) {
            log("on y est");
        }
                
        List<Coup> results = new ArrayList<>();
        List<Elevator> floorElevators = new ArrayList<>();

        Optional<Elevator> elevatorPileDessus = elevators.stream()
                    .filter(e -> e.floor == etat.floor && e.pos == etat.pos)
                    .findAny();

        // On peut prendre soit le plus proche ascenseur à droite soit le plus proche à gauche
        
        Optional<Elevator> leftElevator = elevators.stream()
            .filter(e -> e.floor == etat.floor)
            .filter(e -> e.pos < etat.pos)
            .reduce((e1, e2) -> e1.pos > e2.pos ? e1 : e2);
        
        Optional<Elevator> rightElevator = elevators.stream()
                .filter(e -> e.floor == etat.floor)
                .filter(e -> e.pos > etat.pos)
                .reduce((e1, e2) -> e1.pos < e2.pos ? e1 : e2);
        
        if (etat.floor == exitFloor) {
            
            // On est sur l'étage de la sortie
            // Vérifier que l'on peut l'atteindre
            
            // TODO : si on est sur un ascenseur, on ne peut atteindre la sortie que si on
            // est dans la direction inverse sinon on est aspiré par l'ascenseur
            // C'est le cas pour le test 08. 
            
            if (elevatorPileDessus.isPresent()
             || (exitPos < etat.pos && leftElevator.isPresent() && leftElevator.get().pos > exitPos)
             || (exitPos > etat.pos && rightElevator.isPresent() && rightElevator.get().pos < exitPos)) {
                // La sortie n'est pas atteignable
                return results;
            } else {
                floorElevators.add(new Elevator(exitFloor, exitPos));
            }

        // Ca ou on est sur un ascenceur : on ne peut en sortir que si on va dans la direction opposée. 
        } else if (elevatorPileDessus.isPresent()) {
            floorElevators.add(elevatorPileDessus.get());
            // Il faut ajouter celui qui est dans le sens oposé
            if (etat.direction.equals("RIGHT")) {
                if (leftElevator.isPresent()) floorElevators.add(leftElevator.get());
            } else {
                if (rightElevator.isPresent()) floorElevators.add(rightElevator.get());
            }
        } else {
            // On peut prendre soit le plus proche ascenseur à droite soit le plus proche à gauche
            if (leftElevator.isPresent()) floorElevators.add(leftElevator.get());
            if (rightElevator.isPresent()) floorElevators.add(rightElevator.get());
        }
            
        for (Elevator elevator : floorElevators) {
            
            Coup coup = new Coup();
            coup.elevator = elevator;
            coup.ordre = "WAIT";
            coup.etatFinal = new Etat(etat.floor+1, elevator.pos, etat.direction);

            coup.duree = Math.abs(etat.pos - elevator.pos);
            if (etat.floor < exitFloor) coup.duree += 1;
            
            if ((etat.pos < elevator.pos && etat.direction.equals("LEFT"))
                    || (elevator.pos < etat.pos && etat.direction.equals("RIGHT"))) {
                coup.duree += 3;
                coup.nbClones++;
                coup.ordre = "BLOCK";
                coup.etatFinal.direction = etat.direction.equals("LEFT") ? "RIGHT" : "LEFT";
            }
            
            coup.etatFinal.nbAdditionalElevators = etat.nbAdditionalElevators - coup.nbElevators;
            coup.etatFinal.nbRounds = etat.nbRounds - coup.duree;
            coup.etatFinal.nbTotalClones = etat.nbTotalClones - coup.nbClones;
            
            if (coup.etatFinal.nbRounds >= 0 && coup.etatFinal.nbTotalClones >= 0
                    && coup.etatFinal.nbAdditionalElevators >= 0) {
                results.add(coup);
            }
        }
        
        // Création d'ascenseurs
        
        if (etat.floor < exitFloor &&
                etat.nbAdditionalElevators > 0 && etat.nbRounds > 3 && etat.nbTotalClones > 1) {
            
            // Création d'un ascenseur à l'endroit ou on se trouve (s'il n'y en a pas déjà un)
            
            if (!elevatorPileDessus.isPresent()) {
                Etat etatFinal = new Etat(etat.floor+1, etat.pos, etat.direction, etat.nbRounds-3,
                        etat.nbTotalClones-1, etat.nbAdditionalElevators-1);
                Elevator elevator = new Elevator(etat.floor, etat.pos);
                results.add(new Coup(elevator, 3, 1, 1, etatFinal, "ELEVATOR"));
            }
            
            // Création d'un ascenseur sous la sortie (si c'est possible)
            
            // Ce n'est pas possible s'il existe un ascenceur avant la colonne de la sortie.
            
            // Si on est sur un ascenseur, on ne peut se diriger vers la sortie que si on
            // est dans la direction inverse sinon on est aspiré par l'ascenseur.
            
            boolean leftPossible = exitPos < etat.pos
                    && (!leftElevator.isPresent() || leftElevator.get().pos < exitPos)
                    && (!elevatorPileDessus.isPresent() || etat.direction.equals("RIGHT"));
            boolean rightPossible = exitPos > etat.pos
                    && (!rightElevator.isPresent() || rightElevator.get().pos > exitPos)
                    && (!elevatorPileDessus.isPresent() || etat.direction.equals("LEFT"));
            
            if (leftPossible || rightPossible) {
                
                Coup coup = new Coup();
                coup.elevator = new Elevator(etat.floor, exitPos);
                coup.ordre = "WAIT";
                coup.etatFinal = new Etat(etat.floor+1, exitPos, etat.direction);

                coup.duree = Math.abs(etat.pos - exitPos);
                if (etat.floor < exitFloor) coup.duree += 1;
                
                // Cout de l'ascenseur
                coup.duree += 3;
                coup.nbClones++;
                coup.nbElevators = 1;
                
                if ((etat.pos < exitPos && etat.direction.equals("LEFT"))
                        || (exitPos < etat.pos && etat.direction.equals("RIGHT"))) {
                    coup.duree += 3;
                    coup.nbClones++;
                    coup.ordre = "BLOCK";
                    coup.etatFinal.direction = etat.direction.equals("LEFT") ? "RIGHT" : "LEFT";
                }
                
                coup.etatFinal.nbAdditionalElevators = etat.nbAdditionalElevators - coup.nbElevators;
                coup.etatFinal.nbRounds = etat.nbRounds - coup.duree;
                coup.etatFinal.nbTotalClones = etat.nbTotalClones - coup.nbClones;
                
                if (coup.etatFinal.nbRounds >= 0 && coup.etatFinal.nbTotalClones >= 0 && coup.etatFinal.nbAdditionalElevators >= 0) {
                    results.add(coup);
                }

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
        Elevator elevator;
        
        public Coup() {
            super();
        }
       
        public Coup(Elevator elevator, int duree, int nbClones, int nbElevators, Player.Etat etatFinal, String ordre) {
            super();
            this.elevator = elevator;
            this.duree = duree;
            this.nbClones = nbClones;
            this.nbElevators = nbElevators;
            this.etatFinal = etatFinal;
            this.ordre = ordre;
        }

        @Override
        public String toString() {
            return "Coup [" + elevator + ", duree=" + duree + ", nbClones=" + nbClones + ", nbElevators="
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
            StringBuilder sb = new StringBuilder();
            for (Object o : objects) {
                sb.append("" + o + " ");
            }
            String logText = sb.toString();
            if (logFilters != null) {
                boolean select = false;
                for (String filter : logFilters) {
                    if (logText.indexOf(filter) > -1) select = true; 
                }
                if (!select) return;
            }
            System.err.println("* "+logText);
        }
    }
    
    static void activateLogDebug() {
        doLogDebug = true;
    }
    
}