package codingame.spreadingfire;
import java.util.*;
import java.util.stream.IntStream;
import java.io.*;

/**
 * Read the constant data of the map before the main loop, then read the state of the fire and give an action at each turn
 **/
class Player {
    
    int treeTreatmentDuration; // cooldown for cutting a "tree" cell
    int treeFireDuration; // number of turns for the fire to propagate on adjacent cells from a "tree" cell
    int treeValue; // value lost if a "tree" cell is burnt or cut
    
    int houseTreatmentDuration; // cooldown for cutting a "house" cell
    int houseFireDuration; // number of turns for the fire to propagate on adjacent cells from a "house" cell
    int houseValue; // value lost if a "house" cell is burnt or cut

    int width; // number of columns in the grid
    int height; // number of rows in the grid

    int fireStartX; // column where the fire starts
    int fireStartY; // row where the fire starts

    int[] fireDuration;
    int[] cutDuration;
    long[] value;
    
    int[] direction;
    
    // Etat
    Solution planEnCours; // = new Solution(new int[] {18, 25, 19, 32, 20, 39, 21, 46, 22, 53});
    
    
    public static void main(String args[]) {
        Player player = new Player();
        player.joue(System.in);
    }
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);
        treeTreatmentDuration = in.nextInt(); // cooldown for cutting a "tree" cell
        treeFireDuration = in.nextInt(); // number of turns for the fire to propagate on adjacent cells from a "tree" cell
        treeValue = in.nextInt(); // value lost if a "tree" cell is burnt or cut
        
        System.err.println(treeTreatmentDuration+" "+treeFireDuration+" "+treeValue);
        
        houseTreatmentDuration = in.nextInt(); // cooldown for cutting a "house" cell
        houseFireDuration = in.nextInt(); // number of turns for the fire to propagate on adjacent cells from a "house" cell
        houseValue = in.nextInt(); // value lost if a "house" cell is burnt or cut

        System.err.println(houseTreatmentDuration+" "+houseFireDuration+" "+houseValue);

        width = in.nextInt(); // number of columns in the grid
        height = in.nextInt(); // number of rows in the grid

        System.err.println(width+" "+height);
        
        fireDuration = new int[width * height];
        cutDuration = new int[width * height];
        value = new long[width * height];
        
        Etat monEtat = new Etat(width, height);
        
        direction = new int[] {-1, 1, -width, width}; 
        
        fireStartX = in.nextInt(); // column where the fire starts
        fireStartY = in.nextInt(); // row where the fire starts

        System.err.println(fireStartX+" "+fireStartY);

        for (int i = 0; i < height; i++) {
            String gridLine = in.next();
            System.err.println(gridLine);
            
            for (int j = 0; j < width; j++) {
                int k = (i * width) + j;
                
                if (gridLine.charAt(j) == '#') {
                    fireDuration[k] = 0;
                    cutDuration[k] = 0;
                    value[k] = 0;
                    monEtat.cells[k] = -2;
                } else if (gridLine.charAt(j) == '.') {
                    fireDuration[k] = treeFireDuration;
                    cutDuration[k] = treeTreatmentDuration;
                    value[k] = treeValue;
                    monEtat.cells[k] = -1;
                } if (gridLine.charAt(j) == 'X') {
                    fireDuration[k] = houseFireDuration;
                    cutDuration[k] = houseTreatmentDuration;
                    value[k] = houseValue;
                    monEtat.cells[k] = -1;
                }
            }
        }
        monEtat.cells[(fireStartY * width) + fireStartX] = 0;

        // game loop
        while (true) {
            
            Etat etat = new Etat(width, height);
            
            etat.cooldown = in.nextInt(); // number of turns remaining before you can cut a new cell
            System.err.println(etat.cooldown);
            
            if (etat.cooldown > -1) {
                int k = 0;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        byte fireProgress = in.nextByte(); // state of the fire in this cell (-2: safe, -1: no fire, 0<=.<fireDuration: fire, fireDuration: burnt)
                        System.err.print(fireProgress+" ");
                        etat.cells[k] = fireProgress;
                        k++;
                    }
                    System.err.println();
                }
            
            } else {
                // Only for debuging purpose
                etat = monEtat;
            }
            
            long timeStart = System.nanoTime();

            String commande = strategie(etat);
            
            System.err.println("Duree: "+Math.floorDiv(System.nanoTime()-timeStart, 1_000_000)+"ms");

            // WAIT if your intervention cooldown is not zero, else position [x] [y] of your intervention.
            System.out.println(commande);
        }
    }
    
    private String strategie(Etat etat) {
        
        if (planEnCours == null ) {
            planEnCours = strategieEncerclement(etat);
        }
        
        return suiviDuPlan(etat);
    }
    
    private String suiviDuPlan(Etat etat) {
        if (etat.cooldown > 0 || planEnCours.coups.isEmpty()) {
            return "WAIT";
        }
        
        int k = planEnCours.coups.pollFirst();
        int x = k % width;
        int y = Math.floorDiv(k, width);
        return x + " " + y;
    }

    private Solution strategieEncerclement(Etat etatOrigine) {
        Etat etat = etatOrigine.clone();
        Solution solutionTrouve = null;
        int nbTours = 0;
        
        while (solutionTrouve == null && !etat.terminated) {

            logDebug("----", nbTours);
            etat.dump();
            
            // Si le feu va s'étendre au prochain tour
            if (etat.expansionNextTime) {
                // Faire la liste des cellules qui l'encerclent
                Solution solution = new Solution();
                long sumCutDuration = 0;
                for (int k = 0; k < etat.cells.length; k++) {
                    if (etat.cells[k] == -1
                    && (etat.cells[k + direction[0]] > -1 || etat.cells[k + direction[1]] > -1
                        || etat.cells[k + direction[2]] > -1 || etat.cells[k + direction[3]] > -1)) {
                        sumCutDuration += solution.coups.isEmpty() ? 1 :  cutDuration[k];
                        if (sumCutDuration > nbTours) {
                            logDebug("---- Depassement : break");
                            break;
                        }
                        logDebug("ajout", k, "sumCutDuration", sumCutDuration);
                        solution.coups.add(k);
 
                    }
                }
                
                if (sumCutDuration <= nbTours) {
                    solutionTrouve = solution;
                    logDebug("Solution validée");
                    // Solution validée : calculer le score
                    // On rase les cellules pour calculer le score final
                    solution.coups.forEach(k -> etat.cells[k] = -2);
                    solution.value = etat.calculateValue();
                } else {
                    logDebug("Solution NON validée");
                }
            }
            
            nbTours++;
            etat.tourDePlus();
        }
        
        
        return solutionTrouve;
    }

    class Etat {
        int cooldown = 0;
        byte[] cells;
        boolean terminated = false;
        boolean expansionNextTime = false;
        
        Etat() {
        }
       
        Etat(int width, int heigth) {
            cells = new byte[width * heigth];
        }
        
        protected Etat clone() {
            Etat newEtat = new Etat();
            newEtat.cooldown = cooldown;
            newEtat.terminated = terminated;
            newEtat.expansionNextTime = expansionNextTime;
            newEtat.cells = new byte[cells.length];
            for (int k = 0; k < cells.length; k++) newEtat.cells[k] = cells[k];
            return newEtat;
        }
        
        void tourDePlus() {
            terminated = true;
            expansionNextTime = false;
            for (int k = 0; k < cells.length; k++) {
                if (cells[k] > -1 && cells[k] < fireDuration[k]) {
                    cells[k]++;
                    terminated = false;
                    if (cells[k] == fireDuration[k] - 1) expansionNextTime = true;
                }
            }
            for (int k = 0; k < cells.length; k++) {
                if (cells[k] == fireDuration[k]) {
                    for (int n = 0; n < 4; n++) {
                        int c = k + direction[n];
                        if (cells[c] == -1) cells[c] = 0; 
                    }
                }
            }
            
            cooldown = Math.max(0, cooldown - 1);
       }
        
        void dump() {
            log("cooldown", cooldown, "terminated", terminated, "expansionNextTime", expansionNextTime);
            for (int i = 0; i < height; i++) {
                log("dump", Arrays.toString(Arrays.copyOfRange(cells, i * width, (i + 1) * width)));
            }
        }
        
        long calculateValue() {
            long sumValue = 0; 
            for (int k = 0; k < cells.length; k++) {
                if (cells[k] == -1) sumValue += value[k];
            }
            return sumValue;
        }
        
    }
    
    class Solution {
        LinkedList<Integer> coups = new LinkedList<>();
        long value = Long.MAX_VALUE;

        Solution() {}
        
        Solution(int[] liste) {
            IntStream.of(liste).forEach(coups::add);
        }

    }
    
    /* Codingame common */

    static boolean doLog = true;
    static boolean doLogDebug = false;
    static String[] logFilters = null;
    
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