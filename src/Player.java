import java.util.*;
import java.util.stream.IntStream;
import java.io.*;

/**
 * Complete the hackathon before your opponent by following the principles of Green IT
 **/
class Player {

    int releaseCount = 0;
    
    int[][] proche = new int[][] {
        {1, 1, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 0, 0, 0, 0, 0},
        {0, 1, 1, 1, 0, 0, 0, 0},
        {0, 0, 1, 1, 1, 0, 0, 0},
        {0, 0, 0, 1, 1, 1, 0, 0},
        {0, 0, 0, 0, 1, 1, 1, 0},
        {0, 0, 0, 0, 0, 1, 1, 1},
        {0, 0, 0, 0, 0, 0, 1, 1}        
    }; 
        
    public static void main(String args[]) {
        Player player = new Player();
        player.joue(System.in);
    }
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);

        // game loop
        while (true) {
            Etat etat = new Etat();
            
            etat.gamePhase = in.next(); // can be MOVE, GIVE_CARD, THROW_CARD, PLAY_CARD or RELEASE
            System.err.println(etat.gamePhase);
            
            int applicationsCount = in.nextInt();
            System.err.println(""+applicationsCount);
            
            for (int i = 0; i < applicationsCount; i++) {
                Application application = new Application(); 
                String objectType = in.next();
                
                application.id = in.nextInt();
                
                String output = " " + application.id;
                for (int j = 0; j < 8; j++) {
                    application.tasks[j] = in.nextInt();
                    output += " " + application.tasks[j];
                }
                /*
                application.id = in.nextInt();
                application.trainingNeeded = in.nextInt(); // number of TRAINING skills needed to release this application
                application.codingNeeded = in.nextInt(); // number of CODING skills needed to release this application
                application.dailyRoutineNeeded = in.nextInt(); // number of DAILY_ROUTINE skills needed to release this application
                application.taskPrioritizationNeeded = in.nextInt(); // number of TASK_PRIORITIZATION skills needed to release this application
                application.architectureStudyNeeded = in.nextInt(); // number of ARCHITECTURE_STUDY skills needed to release this application
                application.continuousDeliveryNeeded = in.nextInt(); // number of CONTINUOUS_DELIVERY skills needed to release this application
                application.codeReviewNeeded = in.nextInt(); // number of CODE_REVIEW skills needed to release this application
                application.refactoringNeeded = in.nextInt(); // number of REFACTORING skills needed to release this application
                */
                etat.applications.add(application);
                
                System.err.println(objectType+output);
            }
            
            for (int i = 0; i < 2; i++) {
                Joueur joueur = new Joueur(); 
                
                joueur.location = in.nextInt(); // id of the zone in which the player is located
                joueur.score = in.nextInt();
                joueur.permanentDailyRoutineCards = in.nextInt(); // number of DAILY_ROUTINE the player has played. It allows them to take cards from the adjacent zones
                joueur.permanentArchitectureStudyCards = in.nextInt(); // number of ARCHITECTURE_STUDY the player has played. It allows them to draw more cards
                
                etat.joueurs.add(joueur);
                
                System.err.println(""+joueur.location+" "+joueur.score+" "+joueur.permanentDailyRoutineCards+" "+
                        joueur.permanentArchitectureStudyCards);
            }
            
            int cardLocationsCount = in.nextInt();
            System.err.println(""+cardLocationsCount);
            
            for (int i = 0; i < cardLocationsCount; i++) {
                int[] cards = new int[10];
                
                String location = in.next(); // the location of the card list. It can be HAND, DRAW, DISCARD or OPPONENT_CARDS (AUTOMATED and OPPONENT_AUTOMATED will appear in later leagues)
                
                String output = "";
                for (int j = 0; j < 10; j++) {
                    cards[j] = in.nextInt();
                    output += " " + cards[j];
                }
                
                /*
                cardsLocation.trainingCardsCount = in.nextInt();
                cardsLocation.codingCardsCount = in.nextInt();
                cardsLocation.dailyRoutineCardsCount = in.nextInt();
                cardsLocation.taskPrioritizationCardsCount = in.nextInt();
                cardsLocation.architectureStudyCardsCount = in.nextInt();
                cardsLocation.continuousDeliveryCardsCount = in.nextInt();
                cardsLocation.codeReviewCardsCount = in.nextInt();
                cardsLocation.refactoringCardsCount = in.nextInt();
                cardsLocation.bonusCardsCount = in.nextInt();
                cardsLocation.technicalDebtCardsCount = in.nextInt();
                */
                
                etat.cardsLocations.put(location, cards);
                
                System.err.println(location+output);

            }
            int possibleMovesCount = in.nextInt();
            System.err.println(""+possibleMovesCount);
            if (in.hasNextLine()) {
                in.nextLine();
            }
            
            for (int i = 0; i < possibleMovesCount; i++) {
                String possibleMove = in.nextLine();
                System.err.println(possibleMove);
                etat.possibleMoves.add(possibleMove);
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");


            // In the first league: RANDOM | MOVE <zoneId> | RELEASE <applicationId> | WAIT; In later leagues: | GIVE <cardType> | THROW <cardType> | TRAINING | CODING | DAILY_ROUTINE | TASK_PRIORITIZATION <cardTypeToThrow> <cardTypeToTake> | ARCHITECTURE_STUDY | CONTINUOUS_DELIVERY <cardTypeToAutomate> | CODE_REVIEW | REFACTORING;
            
            String ordre = strategie(etat);
            System.out.println(ordre);
        }
    }
    
    private String strategie(Etat etat) {
        logDebug("etat.gamePhase", etat.gamePhase);
        
        if (etat.gamePhase.equals("MOVE")) return strategiePourMove(etat);
        else if (etat.gamePhase.equals("RELEASE")) return strategiePourRelease(etat);
        else if (etat.gamePhase.equals("GIVE_CARD")) return "RANDOM";
        return "RANDOM";
    }

    private String strategiePourMove(Etat etat) {
        
        // Rechercher le poste ayant une tâche spécifique nécessaire à une application
        
        int meilleureDette = 999;
        int moveSelect = -1;
        OrdreRelease ordreSelect = null;
        
        CardsTool cardsTool = new CardsTool(etat.cardsLocations.get("HAND"));
        
        for (int i = 0; i < 8; i++) {
            //logDebug("> poste:", i);
            if (etat.joueurs.get(0).location != i) {
                
                int[] newMain = cardsTool.dupplique();
                newMain[i]++;
                logDebug("-------- move ", i, "newMain", Arrays.toString(newMain));
                
                OrdreRelease ordre = meilleureGiveCard(etat, newMain, i);
                
                if (ordre.detteTechnique < meilleureDette) {
                    moveSelect = i;
                    meilleureDette = ordre.detteTechnique;
                    ordreSelect = ordre;
                }

            }
        }
        
        if (moveSelect > -1) {
            return "MOVE "+moveSelect + " app("+ordreSelect.id+") dette:"+ordreSelect.detteTechnique; 
        }
        return "RANDOM";
    }

    OrdreRelease meilleureGiveCard(Etat etat, int[] main, int poste) {
        
        OrdreRelease ordre = meilleureRelease(etat, main);
        
        if (etat.joueurs.get(1).location > -1) {
            logDebug("proche", poste, etat.joueurs.get(1).location, "=", proche[poste][etat.joueurs.get(1).location]);
            if (proche[poste][etat.joueurs.get(1).location] == 1) {
                ordre.detteTechnique += 2;
            }
        } else {
            logDebug("proche", poste, etat.joueurs.get(1).location);
        }
        
        logDebug("Appli selected modif", ordre.id, "dette", ordre.detteTechnique);
        
        return ordre;
    }

    private String strategiePourRelease(Player.Etat etat) {
        logDebug("strategiePourRelease");
        
        // Recherche meilleure appli à jouer 
        
        int[] main = etat.cardsLocations.get("HAND");
        OrdreRelease ordre = meilleureRelease(etat, main);

        if (ordre.id > -1
                && ((releaseCount == 4 && ordre.detteTechnique == 0)
                 || (releaseCount < 4 && ordre.detteTechnique <= 2))) {
            releaseCount++;
            logDebug("appliSelected", ordre.id);
            return "RELEASE "+ordre.id + " dette:"+ordre.detteTechnique;
        }

        return "WAIT";
    }

    OrdreRelease meilleureRelease(Etat etat, int[] main) {

        OrdreRelease ordre = new OrdreRelease();
        
        CardsTool cardsTool = new CardsTool(main);
        int cardsTaskCount = cardsTool.getTaskCount();
        logDebug("hand ", cardsTool, "tasksCount", cardsTaskCount);
        
        int meilleureDette = 999;
        Application appliSelected = null;
        
        for (Application application : etat.applications) {
            logDebug(application);
            if (cardsTaskCount >= application.getTaskCount()) {
                int cardsOk = 0;
            
                for (int i = 0; i < 8; i++) {
                    cardsOk += Math.min(application.tasks[i], main[i]);
                }
                int dette = application.getTaskCount() - (2 * cardsOk) - main[8];
                dette = Math.max(dette,  0);
                logDebug(" > cardsOk", cardsOk, "dette", dette);
                
                if (dette < meilleureDette) {
                    appliSelected = application;
                    meilleureDette = dette;
                }
            }
        }

        if (appliSelected != null) {
            ordre.id = appliSelected.id;
            ordre.detteTechnique = meilleureDette;
            logDebug("Appli selected", appliSelected.id, "dette", meilleureDette);
        }
        return ordre;
        
    }
    
    //-----------------------------------------------------------------------------
    
    class Etat {
        String gamePhase;
        List<Application> applications = new ArrayList<>();
        List<Joueur> joueurs = new ArrayList<>(2);

        // the location of the card list. It can be HAND, DRAW, DISCARD
        //or OPPONENT_CARDS (AUTOMATED and OPPONENT_AUTOMATED will appear in later leagues)
        Map<String, int[]> cardsLocations = new HashMap<>();
        
        List<String> possibleMoves = new ArrayList<>();
    }

    class OrdreRelease {
        int id = -1;
        int detteTechnique = 0;
    }
    
    class Application {
        int id;
        int[] tasks = new int[8];
        
        int getTaskCount() {
            return IntStream.of(tasks).sum();
        }
        
        @Override
        public String toString() {
            return "Appli [id=" + id + ", tasks=" + Arrays.toString(tasks) + "]";
        }
        
    }
    
    class Joueur {
        int location; // id of the zone in which the player is located
        int score;
        int permanentDailyRoutineCards; // number of DAILY_ROUTINE the player has played. It allows them to take cards from the adjacent zones
        int permanentArchitectureStudyCards; // number of ARCHITECTURE_STUDY the player has played. It allows them to draw more cards
    }
  
    class CardsTool { 
        int[] cards = new int[10];

        public CardsTool(int[] cards) {
            super();
            this.cards = cards;
        }

        int getTaskCount() {
            int count = 0;
            for (int i = 0; i < 8; i++) {
                count += cards[i] * 4; 
            }
            count += cards[8] * 2;
            return count;
        }
        
        int[] dupplique() {
            int[] newCards = new int[10];
            for (int i = 0; i < newCards.length; i++) {
                newCards[i] = cards[i]; 
            }
            return newCards;
        }
        
        @Override
        public String toString() {
            return "Cards [cards=" + Arrays.toString(cards) + "]";
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