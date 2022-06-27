import java.util.*;
import java.util.stream.IntStream;
import java.io.*;

/**
 * Complete the hackathon before your opponent by following the principles of Green IT
 **/
class Player {
    private static final int MAX_DETTE = 99;
    private static final int INFAISABLE = 99;

    private static final int MAX_AJOUT_DETTE_CAS_NORMAL = 2;
    private static final int MAX_AJOUT_DETTE_SI_RETARD = 4;

    String commande;
    Ordre planEnCours = null;
    Deplacement deplEnCours = null;
    
    // --- Stats sur les cartes
    Cards cartesDemandees;
    boolean[] cartesUtiles;
    Cards prochaineCarte;
    Cards cartesManquantes;
    
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
                    application.tasks.c[j] = in.nextInt();
                    output += " " + application.tasks.c[j];
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
            etat.moi = etat.joueurs.get(0);
            etat.adv = etat.joueurs.get(1);
            
            int cardLocationsCount = in.nextInt();
            System.err.println(""+cardLocationsCount);
            
            for (int i = 0; i < cardLocationsCount; i++) {
                Cards cards = new Cards();
                
                String location = in.next(); // the location of the card list. It can be HAND, DRAW, DISCARD or OPPONENT_CARDS (AUTOMATED and OPPONENT_AUTOMATED will appear in later leagues)
                
                String output = "";
                for (int j = 0; j < 10; j++) {
                    cards.c[j] = in.nextInt();
                    output += " " + cards.c[j];
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
                
                if (location.equals("HAND")) {
                    etat.main = cards;
                }

            }
            int possibleMovesCount = in.nextInt();
            System.err.println(""+possibleMovesCount);
            if (in.hasNextLine()) {
                in.nextLine();
            }
            
            for (int i = 0; i < possibleMovesCount; i++) {
                String possibleMove = in.nextLine();
                System.err.println(possibleMove);
                etat.actionsPossibles.add(possibleMove);
            }

            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");


            // In the first league: RANDOM | MOVE <zoneId> | RELEASE <applicationId> | WAIT; In later leagues: | GIVE <cardType> | THROW <cardType> | TRAINING | CODING | DAILY_ROUTINE | TASK_PRIORITIZATION <cardTypeToThrow> <cardTypeToTake> | ARCHITECTURE_STUDY | CONTINUOUS_DELIVERY <cardTypeToAutomate> | CODE_REVIEW | REFACTORING;
            
            commande = null;
            strategie(etat);
            System.out.println(commande);
        }
    }
    
    private void strategie(Etat etat) {
        logDebug("etat.gamePhase", etat.gamePhase);
        
        // Enrichissement
        statsCartes(etat);
        statsJoueurs(etat);
        ajoutDeplacements(etat);
        
        applicationDuPlanEnCours(etat);
        if (commande != null) return;
        
        if (etat.gamePhase.equals("MOVE")) {
            strategieRechercheReleaseParLaCombinatoire(etat);
            // Capture CONTINUOUS_INTEGRATION
            strategieCaptureCarte(etat, 5);
            //// Capture Daily routine
            if (etat.moi.permanentDailyRoutineCards == 0) strategieCaptureCarte(etat, 2);
            strategieRechercheCartesManquantes(etat);
            strategieMovePourAvoirUneCarte(etat);
            strategieMoveSurSuivant(etat);
            
        } else if (etat.gamePhase.equals("THROW_CARD")) {
            strategieThrowIntuitive(etat);
            //strategieThrowCombinatoire(etat); TODO : à supprimer
            if (commande == null) commande =  "RANDOM";
            if (commande.equals("GIVE -1")) commande =  "RANDOM";
            
        } else if (etat.gamePhase.equals("GIVE_CARD")) {
            strategieGiveCardIntuitive(etat);
            //strategieGiveCardCombinatoire(etat);
            if (commande == null) commande = "RANDOM";
            //if (commande == null) commande = "RANDOM";
        
        } else if (etat.gamePhase.equals("PLAY_CARD")) {
            strategiePlayCardIntuitive(etat);
            if (commande == null) commande =  "WAIT";
        
        } else if (etat.gamePhase.equals("RELEASE")) {
            strategiePourReleaseFinaleRapide(etat);
            strategiePourRelease(etat);
            if (commande == null) commande = "WAIT";
        
        } else {
            commande =  "RANDOM";
        }

    }

    private void statsCartes(Etat etat) {
        if (etat.gamePhase.equals("MOVE") || etat.gamePhase.equals("PLAY_CARD")) {
            // OK
        } else {
            return;
        }
        
        cartesDemandees = new Cards();
        etat.applications.forEach(a -> cartesDemandees.ajoute(a.tasks));
        cartesDemandees.divise(2);
        log("applisEnCours", cartesDemandees);
        
        cartesUtiles = new boolean[8];
        for (int i = 0; i < 8; i++) {
            if (cartesDemandees.c[i] == 0) {
                cartesUtiles[i] = false;
            } else {
                cartesUtiles[i] = true;
            }
        }
        // La continuous integration est toujours utile
        cartesUtiles[5] = true;
        log("cartesUtiles", Arrays.toString(cartesUtiles));
        
        Cards cartesEnMain = new Cards();
        etat.cardsLocations.values().forEach(cartesEnMain::ajoute);
        cartesEnMain.c[2] += etat.moi.permanentDailyRoutineCards;
        cartesEnMain.c[2] += etat.adv.permanentDailyRoutineCards;
        
        cartesEnMain.c[4] += etat.moi.permanentArchitectureStudyCards;
        cartesEnMain.c[4] += etat.adv.permanentArchitectureStudyCards;
        
        log("cartesEnMain", cartesEnMain);
        
        // ---- Détermination de la prochaine carte sur le paquet
        
        prochaineCarte = new Cards();
        for (int i = 0; i < 8; i++) {
            if (cartesEnMain.c[i] < 5)
                prochaineCarte.c[i] = i;
            else prochaineCarte.c[i] = 8;
        }
        log("prochaineCarte", prochaineCarte);
        
        // ---- Détermination cartes prirotaires
        
        Cards cartesEnMaMain = new Cards();
        etat.cardsLocations.entrySet().stream()
                .filter(e -> !e.getKey().startsWith("OPPONENT"))
                .forEach(e -> cartesEnMaMain.ajoute(e.getValue()));
        log("cartesMaMain", cartesEnMaMain);
        
        cartesManquantes = new Cards();
        int plusPetiteDistance = 99;
        
        for (Application appli : etat.applications) {
            Cards cartesManquantesAppli = new Cards();
            int distance = 0;
            for (int i = 0; i < 8; i++) {
                int distanceCarte = Math.max(0, (appli.tasks.c[i] / 2) - cartesEnMaMain.c[i]);
                cartesManquantesAppli.c[i] = distanceCarte;
                distance += distanceCarte;
            }
            logDebug("Distance", distance, "appli", appli, "cartesManquantesAppli", cartesManquantesAppli);
            
            if (distance < plusPetiteDistance) {
                cartesManquantes.reset();
                plusPetiteDistance = distance;
            }
            cartesManquantes.ajoute(cartesManquantesAppli);
        }
        
        logDebug("cartesManquantes", cartesManquantes);

    }

    private void statsJoueurs(Etat etat) {
        etat.cardsLocations.forEach((k, cards) -> {
            if (k.equals("OPPONENT_CARDS")) {
                etat.adv.dette += cards.c[9];
            } else {
                etat.moi.dette += cards.c[9];
            }
        });
        log("dette: moi", etat.moi.dette, "adv", etat.adv.dette);
    }

    private void ajoutDeplacements(Player.Etat etat) {
        
        for (int i = 1; i < 8; i++) {
            int poste = addPoste(etat.moi.location, i);
            logDebug("$ deplacement poste", poste);
            
            if (poste != etat.moi.location) {
                logDebug("$ select");
                
                for (int k = -etat.moi.permanentDailyRoutineCards; k <= etat.moi.permanentDailyRoutineCards; k++) {
                    int carte = addPoste(poste, k);
                
                    Deplacement depl = new Deplacement(poste, carte, i, poste < etat.moi.location, 
                            (etat.adv.location > -1 && proche[poste][etat.adv.location] == 1));
                    etat.deplacements.add(depl);
                    logDebug("$ ajout", depl);
                    
                }
                
            }
        }
        
    }

    void applicationDuPlanEnCours(Etat etat) {
        if (etat.gamePhase.equals("MOVE") || planEnCours == null) {
            return;
        }
        
        log("Plan en cours", planEnCours);
        
        if (etat.gamePhase.equals("THROW_CARD")) {
            commande = "THROW " + planEnCours.throwCards[0] + " plan";
            planEnCours.throwCards[0] = planEnCours.throwCards[1];

        } else if (etat.gamePhase.equals("GIVE_CARD")) {
            commande = "GIVE " + planEnCours.giveCard + " plan";
        
        } else if (etat.gamePhase.equals("PLAY_CARD")) {
            if (planEnCours.prio[0] > -1) {
                commande = "TASK_PRIORITIZATION "+planEnCours.prio[0]+" "+planEnCours.prio[1] + " plan";
            } else {
                commande = "WAIT plan";
            }
    
        } else if (etat.gamePhase.equals("RELEASE")) {
            commande = "RELEASE "+planEnCours.id + " dette:"+planEnCours.dette + " plan";
        }
        
    }

    private void strategieRechercheReleaseParLaCombinatoire(Etat etat) {
    
        planEnCours = null;
        Ordre ordreSelect = meuilleurMoveCombinatoire(etat);

        log("Meilleur ordre par combinatoire", ordreSelect);
        
        if (ordreSelect != null) {
            
            logDebug("A", (etat.moi.score == 4 && ordreSelect.dette == 0));
            logDebug("B", (etat.moi.score < 4));
            logDebug("C", (ordreSelect.dette <= 2));
            logDebug("D", (etat.moi.score < etat.adv.score && etat.moi.dette + ordreSelect.dette <= etat.adv.dette));
            logDebug("A ou (B et (C ou D))");
            
            if ( ((etat.moi.score == 4 && ordreSelect.dette == 0)
                 || (etat.moi.score < 4
                         && (ordreSelect.dette <= MAX_AJOUT_DETTE_CAS_NORMAL
                             || (ordreSelect.dette <= MAX_AJOUT_DETTE_SI_RETARD && etat.moi.score < etat.adv.score && etat.moi.dette + ordreSelect.dette < etat.adv.dette))))) {

                commande = "MOVE "+ordreSelect.depl.poste + " "+ordreSelect.depl.carte + " app("+ordreSelect.id+") dette:"+ordreSelect.dette;
                planEnCours = ordreSelect;
                deplEnCours = ordreSelect.depl;
                log("Plan a venir", planEnCours);
            }
        }

    }

    private void strategieRechercheCartesManquantes(Player.Etat etat) {
        if (commande != null) return;
        
        Optional<Deplacement> optDepl = etat.deplacements.stream()
                .filter(d -> prochaineCarte.c[d.carte] == d.carte)
                .filter(d -> cartesManquantes.c[d.carte] > 0)
                .filter(d -> !d.throwCard)
                .filter(d -> !d.giveCard)
                .reduce((d1, d2) -> (d1.distance < d2.distance ? d1 : d2));
        
         if (optDepl.isPresent()) {
             Deplacement depl = optDepl.get();
             commande = "MOVE "+depl.poste + " "+depl.carte + " manq";
             deplEnCours = depl;
             log("Manque carte", depl);
         }
        
    }

    private void strategieCaptureCarte(Etat etat, int carte) {
        if (commande != null) return;
        if (prochaineCarte.c[carte] != carte) return;
        if (etat.main.c[carte] > 0) return;
        
        Optional<Deplacement> optDepl = etat.deplacements.stream()
                .filter(d -> d.carte == carte)
                .filter(d -> !d.giveCard)
                .reduce((d1, d2) -> (d1.distance < d2.distance ? d1 : d2));
        
         if (optDepl.isPresent()) {
             Deplacement depl = optDepl.get();
             commande = "MOVE "+depl.poste + " "+depl.carte + " capt";
             deplEnCours = depl;
             log("Capture carte", depl);
         }
        
    }
   
    private void strategieMovePourAvoirUneCarte(Etat etat) {
        if (commande != null) return;
        
        Optional<Deplacement> optDepl = etat.deplacements.stream()
                .filter(d -> prochaineCarte.c[d.carte] == d.carte)
                .filter(d -> cartesUtiles[d.carte])
                .filter(d -> !d.giveCard)
                .reduce((d1, d2) -> (d1.distance < d2.distance ? d1 : d2));
        
        if (optDepl.isPresent()) {
            Deplacement depl = optDepl.get();
            commande = "MOVE "+depl.poste + " "+depl.carte + " +cart";
            deplEnCours = depl;
            log("Carte", depl);
        }
        
    }
    
    private void strategieMoveSurSuivant(Etat etat) {
        if (commande != null) return;
        
        int poste = addPoste(etat.moi.location, 1);
        
        commande = "MOVE " + poste + " " + poste + " suiv";
        deplEnCours = new Deplacement(poste, poste, 1, false, false);
        
    }
    
    int addPoste(int poste, int delta) {
        return (poste + delta + 8) % 8;
    }
    
    private void strategieThrowIntuitive(Etat etat) {
        if (commande != null) return;
        
        int carteSelect = plusMauvaiseCarte(etat.main);
        if (carteSelect > -1) {
            commande = "THROW " + carteSelect;
            return;
        }
    }
    
    private void strategieGiveCardIntuitive(Etat etat) {
        if (commande != null) return;
        
        int carteSelect = plusMauvaiseCarte(etat.main);
        if (carteSelect > -1) {
            commande = "GIVE " + carteSelect;
            return;
        }
    }
    
    private void strategieGiveCardCombinatoire(Etat etat) {
        if (commande != null) return;
        
        Ordre ordre = meilleurGiveCardCombinatoire(etat, etat.cardsLocations.get("HAND"), deplEnCours);
        commande = "GIVE " + ordre.giveCard;

    }

    private void strategiePlayCardIntuitive(Player.Etat etat) {
        if (commande != null) return;
        
        Cards main = etat.cardsLocations.get("HAND");
        log("strategiePlayCard: cartesDemandees", cartesDemandees);
        
        // On regarde si on a une release possible
        strategiePourRelease(etat);
        if (commande != null) {
            log("A venir:", commande);
            commande = "WAIT";
            return;
        } else {
            log("Pas de release à venir");
        }

        // On fait ces 2 cartes en premier si elle ne sont pas utiles
        // Sinon on les fera à nouveau à la fin
        
        // Training : L’équipe pioche 2 cartes de sa pioche et peut jouer une carte de plus
        if (main.c[0] > 0 && cartesDemandees.c[0] == 0) {
            commande = "TRAINING";
            return;
        }
        
        // CODING : L’équipe pioche 1 carte de sa pioche et peut jouer deux cartes de plus.
        if (main.c[1] > 0 && cartesDemandees.c[1] == 0) {
            commande = "CODING";
            return;
        }
        
        // CONTINUOUS_INTEGRATION (5) : L’équipe automatise une de ses compétences.
        if (main.c[5] > 0) {
            Cards automated = new Cards();
            if (etat.cardsLocations.containsKey("AUTOMATED")) {
                automated = etat.cardsLocations.get("AUTOMATED");
            }
            
            for (int i = 0; i < 9; i++) {
                if (main.c[i] > 0 && (i != 5 || main.c[5] > 1) 
                        && automated.c[i] < 2
                        && (cartesDemandees.c[i] > 0 || i == 8)) {
                    commande = "CONTINUOUS_INTEGRATION " + i;
                    return;
                }
            }
        }
        
        // DAILY_ROUTINE (2) : Cette compétence est permanente : une fois jouée, elle reste active
        // jusqu’à ce que l’équipe ait livré une application.
        // Après son déplacement, l’équipe pourra récupérer une carte compétence d’un poste de travail
        // éloigné de 1. L’effet peut être cumulatif.
        if (main.c[2] > 0
                && (cartesDemandees.c[2] == 0 || etat.moi.permanentDailyRoutineCards == 0)) {
            commande = "DAILY_ROUTINE";
            return;
        }
        
        // ARCHITECTURE_STUDY (4) : Cette compétence est permanente : une fois jouée, elle reste active
        // jusqu’à ce que l’équipe ait livré une application.
        // L’équipe piochera une carte de plus au début de son tour. L’effet peut être cumulatif.
        if (main.c[4] > 0 && cartesDemandees.c[4] == 0
                && etat.moi.permanentArchitectureStudyCards < 3) {
            commande = "ARCHITECTURE_STUDY";
            return;
        }
        
        // TASK_PRIORITIZATION (3) : L’équipe se débarrasse d’une carte compétence de sa main et récupère
        // une carte compétence disponible sur le plateau de jeu.
        
        // TODO : utiliser la combinatoire ?
        if (main.c[PRI] > 0) {
            int carteUtile = -1;
            int carteInutile = -1;
            
            for (int i = 0; i < 8; i++) {
                int carte = PRIORITE[7 - i];
                if (main.c[carte] > 0 && !cartesUtiles[carte] && (carte != PRI || main.c[PRI] > 1)) {
                    carteInutile = carte;
                }
                if (main.c[carte] > 0 && cartesUtiles[carte] && prochaineCarte.c[carte] == carte) {
                    carteUtile = carte;
                }
            }
            
            if (carteUtile > -1 && carteInutile > -1) {
                commande = "TASK_PRIORITIZATION "+carteInutile+" "+carteUtile;
                return;
            }
        }
        
        // REFACTORING (7). L’équipe se débarrasse définitivement d’une carte Dette Technique de sa main.
        if (main.c[7] > 0) {
            commande = "REFACTORING";
            return;
        }
        
        // CODE_REVIEW : L’équipe récupère 2 nouvelles cartes compétence BONUS et les met dans sa défausse.
        if (main.c[6] > 0 /* && cartesDemandees.c[6] == 0*/) {
            commande = "CODE_REVIEW";
            return;
        }

        // Training : L’équipe pioche 2 cartes de sa pioche et peut jouer une carte de plus
        if (main.c[0] > 0 /* && cartesDemandees.c[0] == 0*/) {
            commande = "TRAINING";
            return;
        }
        
        // CODING : L’équipe pioche 1 carte de sa pioche et peut jouer deux cartes de plus.
        if (main.c[1] > 0 /* && cartesDemandees.c[1] == 0*/) {
            commande = "CODING";
            return;
        }
         
    }

    private void strategiePourReleaseFinaleRapide(Etat etat) {
                
        if (etat.moi.score == 4) {
            Optional<String> releasePossible = etat.actionsPossibles.stream()
                    .filter(m -> m.startsWith("RELEASE"))
                    .findAny();
            if (releasePossible.isPresent()) {
                commande = releasePossible.get() + " Quick";
            }
        }
        
    }

    private void strategiePourRelease(Etat etat) {
        if (commande != null) return;
        
        boolean prevDoLogDebug = doLogDebug;
        doLogDebug = true;
        
        logDebug("strategiePourRelease");
        
        // Recherche meilleure appli à jouer 
        
        Cards main = etat.cardsLocations.get("HAND");
        Ordre ordreRelease = meilleureRelease(etat, main);

        logDebug("ordreRelease", ordreRelease);
        
        logDebug("0", (ordreRelease.id > -1));
        logDebug("A", (etat.moi.score == 4 && ordreRelease.dette == 0));
        logDebug("B", (etat.moi.score < 4));
        logDebug("C", (ordreRelease.dette <= 2));
        logDebug("D", (etat.moi.score < etat.adv.score && etat.moi.dette < etat.adv.dette));
        logDebug("A ou (B et (C ou D))");
        
        if (ordreRelease.id > -1
                && ((etat.moi.score == 4 && ordreRelease.dette == 0)
                 || (etat.moi.score < 4
                         && (ordreRelease.dette <= MAX_AJOUT_DETTE_CAS_NORMAL
                             || (ordreRelease.dette <= MAX_AJOUT_DETTE_SI_RETARD && etat.moi.score < etat.adv.score && etat.moi.dette + ordreRelease.dette < etat.adv.dette))))) {
            logDebug("appliSelected", ordreRelease.id);
            commande = "RELEASE "+ordreRelease.id + " dette:"+ordreRelease.dette;
        }
        doLogDebug = prevDoLogDebug;

    }

    Ordre meuilleurMoveCombinatoire(Etat etat) {
        String chevrons = "#1:";
         
        int meilleurDette = MAX_DETTE;
        Ordre ordreSelect = null;
        
        for (Deplacement depl : etat.deplacements) {
                 
            logDebug(chevrons, "-------- move ", depl, "prochaineCarte", prochaineCarte.c[depl.carte], etat.main);
            
            Ordre ordre = meilleurThrowCardCombinatoire(etat, etat.main, depl);
                    
            if (ordre.dette < meilleurDette || meilleurDette == MAX_DETTE) {
                meilleurDette = ordre.dette;
                ordreSelect = ordre;
                ordreSelect.depl = depl;
            }

        }
        
        logDebug(chevrons, "meilleurOrdre", ordreSelect);
        return ordreSelect;
    }
    
    Ordre meilleurThrowCardCombinatoire(Etat etat, Cards main, Deplacement depl) {
        String chevrons = "##2:";
        
        Ordre meilleurOrdre = null;
        
        if (depl.throwCard) {
            logDebug(chevrons, "passageAdmin ++", depl.poste, "<", etat.moi.location);

            List<int[]> combinaisons = combinaison2Cartes(main);
            
            int meilleureDette = MAX_DETTE;
            
            for (int[] throwCards : combinaisons) {
                
                Cards nouvMain = main.duplique();
                if (throwCards[0] > -1) nouvMain.c[throwCards[0]]--;
                if (throwCards[1] > -1) nouvMain.c[throwCards[1]]--;
                
                logDebug(chevrons, "throw cards", Arrays.toString(throwCards), "main", main, "nouvMain", nouvMain);
               
                Ordre ordre = meilleurGiveCardCombinatoire(etat, nouvMain, depl);
                
                if (ordre.dette < meilleureDette || meilleureDette == MAX_DETTE) {
                    meilleureDette = ordre.dette;
                    meilleurOrdre = ordre;
                    meilleurOrdre.throwCards = throwCards;
                }
                
            }

        } else {
            logDebug(chevrons, "pas passageAdmin", depl.poste, ">=", etat.moi.location);
            
            meilleurOrdre = meilleurGiveCardCombinatoire(etat, main, depl);

        }
       
        logDebug(chevrons, "meilleurOrdre", meilleurOrdre);
        return meilleurOrdre;
    }

    Ordre meilleurGiveCardCombinatoire(Etat etat, Cards main, Deplacement depl) {
        String chevrons = "###3:";
        Ordre meilleurOrdre = null;
        
        if (depl.giveCard) {
            
            logDebug(chevrons, "proche");
            
            int meilleureDette = MAX_DETTE;
            
            boolean auMoinsUneCarte = false;
            for (int i = 0; i < 9; i++) {
                if (main.c[i] > 0) {
                    logDebug(chevrons, "GiveCard", i);
                    auMoinsUneCarte = true;
                    main.c[i]--;
                    Ordre ordre = receptionCarte(etat, main, depl);
                    main.c[i]++;
                    if (ordre.dette < meilleureDette || meilleureDette == MAX_DETTE) {
                        meilleureDette = ordre.dette;
                        meilleurOrdre = ordre;
                        meilleurOrdre.giveCard = i;
                    }
                }
            }
            if (!auMoinsUneCarte) {
                logDebug(chevrons, "pas carte = dette -2");
                meilleurOrdre = receptionCarte(etat, main, depl);
                //meilleurOrdre.dette += 2;
            }
            
        } else {
            logDebug(chevrons, "pas proche");
            meilleurOrdre = receptionCarte(etat, main, depl);
        }
         
        logDebug(chevrons, "GiveCard select", meilleurOrdre.giveCard, "Appli selected", meilleurOrdre.id, "dette", meilleurOrdre.dette);
        
        return meilleurOrdre;
    }

    Ordre receptionCarte(Etat etat, Cards main, Deplacement depl) {
        String chevrons = "####4:";
        Cards nouvelleMain = main.duplique();
        nouvelleMain.c[prochaineCarte.c[depl.carte]]++;
        logDebug(chevrons, "Reception carte", depl.carte, "mainOrigine", main, "nouvelleMain", nouvelleMain);
        
        return combinatoireTaskPriorisation(etat, nouvelleMain);
    }

    Ordre combinatoireTaskPriorisation(Etat etat, Cards main) {
        String chevrons = "#####5:";
        
        logDebug(chevrons, "Check sans priorisation");
        Ordre meilleurOrdre = meilleureRelease(etat, main);
        
        if (main.c[PRI] > 0) {
            logDebug(chevrons, "TaskPriorisation");
            List<int[]> priorisations = listePriorisations(main);
            
            for (int[] prio : priorisations) {
                
                Cards nouvelleMain = main.duplique();
                nouvelleMain.c[prio[0]]--;
                nouvelleMain.c[prio[1]]++;
                
                logDebug(chevrons, "> prio", Arrays.toString(prio), "nouvelleMain", nouvelleMain);
                
                Ordre ordre = meilleureRelease(etat, nouvelleMain);
                
                if (ordre.dette < meilleurOrdre.dette) {
                    meilleurOrdre = ordre;
                    meilleurOrdre.prio = prio;
                }
            }
            logDebug(chevrons, "meilleurOrdre", meilleurOrdre);
        
        } else {
            logDebug(chevrons, "No TaskPriorisation");
        }

        return meilleurOrdre;
    }

    private List<int[]> listePriorisations(Player.Cards main) {
        List<int[]> results = new ArrayList<>();
        
        for (int i = 0; i < 9; i++) {
            if (main.c[i]  > 0  && (i != PRI || main.c[PRI] > 1)) {
                for (int j = 0; j < 8; j++) {
                    if (i != j && prochaineCarte.c[j] == j) {
                        results.add(new int[] {i, j});
                    }
                }
            }
        }
        
        return results;
    }

    Ordre meilleureRelease(Etat etat, Cards mainOrigine) {
        String chevrons = "######6:";
        
        Cards mainReelle = mainOrigine.duplique();
        if (etat.cardsLocations.get("AUTOMATED") != null) {
            mainReelle.ajoute(etat.cardsLocations.get("AUTOMATED"));
        }
        
        Ordre ordre = new Ordre();
        ordre.dette = MAX_DETTE;
                
        logDebug(chevrons, "mainReelle ", mainReelle, "mainOrigine", mainOrigine);
        
        int meilleureDette = MAX_DETTE;
        Application appliSelected = null;
        
        for (Application application : etat.applications) {
            logDebug(chevrons, application);
            int dette = calculAjoutDette(mainReelle, application);
            if (dette != INFAISABLE) {
                logDebug(chevrons, "dette", dette);
                if (dette < meilleureDette || meilleureDette == MAX_DETTE) {
                    appliSelected = application;
                    meilleureDette = dette;
                }
            } else {
                logDebug(chevrons, " > infaisable");
            }
        }

        if (appliSelected != null) {
            ordre.id = appliSelected.id;
            ordre.dette = meilleureDette;
            logDebug(chevrons, "Appli selected", appliSelected.id, "dette", meilleureDette);
        }
        return ordre;
        
    }

    private int calculAjoutDette(Cards mainReelle, Application application) {
        
        int dette = INFAISABLE;

        int tasksOk = mainReelle.c[8];
        int tasksBaclees = mainReelle.c[8];
         
        for (int i = 0; i < 8; i++) {
            tasksOk += Math.min(application.tasks.c[i], mainReelle.c[i] * 2);
            tasksBaclees += mainReelle.c[i] * 2;
        }
        
        if (tasksOk + tasksBaclees >= application.getTaskCount()) {
            dette = application.getTaskCount() - tasksOk;
        }
        
        //logDebug("dette", dette, "tasksOk", tasksOk, "tasksBaclees", tasksBaclees, "appli", application.getTaskCount());
        
        return Math.max(dette, 0);
    }
    
    private int plusMauvaiseCarte(Cards main) {
        int carteSelect = -1;
        
        if (main.c[8] > 0) {
            carteSelect = 8;
        } else {
    
            int plusBas = 99;
            
            for (int i = 0; i < 8; i++) {
                int carte = PRIORITE[i];
                if (main.c[carte] > 0 && cartesDemandees.c[carte] < plusBas) {
                    plusBas = cartesDemandees.c[carte];
                    carteSelect = carte;
                }
            }
            
        }
        log("plusMauvaiseCarte", main, "=", carteSelect);
        return carteSelect;
    }
    
    //-----------------------------------------------------------------------------
    
    class Etat {
        String gamePhase;
        List<Application> applications = new ArrayList<>();
        List<Joueur> joueurs = new ArrayList<>();
        Joueur moi;
        Joueur adv;

        // the location of the card list. It can be HAND, DRAW, DISCARD
        //or OPPONENT_CARDS (AUTOMATED and OPPONENT_AUTOMATED will appear in later leagues)
        Map<String, Cards> cardsLocations = new HashMap<>();
        Cards main = null;
        
        List<String> actionsPossibles = new ArrayList<>();
        List<Deplacement> deplacements = new ArrayList<>();
    }

    class Ordre {
        Deplacement depl = null;
        int id = -1;
        int dette = 0;
        int[] throwCards = new int[] {-2, -2};
        int[] prio = new int[] {-1, -1};
        int giveCard = -1;
        
        @Override
        public String toString() {
            return "Ordre [depl=" + depl + ", id=" + id + ", dette=" + dette + ", throwCards=" +
                    Arrays.toString(throwCards) + ", giveCard=" + giveCard + ", prios=" +
                    Arrays.toString(prio) + "]";
        }
        
    }
    
    class Deplacement {
        int poste;
        int carte;
        int distance;
        boolean throwCard = false;
        boolean giveCard = false;

        public Deplacement(int poste, int carte, int distance, boolean throwCard, boolean giveCard) {
            super();
            this.poste = poste;
            this.carte = carte;
            this.distance = distance;
            this.throwCard = throwCard;
            this.giveCard = giveCard;
        }
        
        @Override
        public String toString() {
            return "Deplacement [poste=" + poste + ", carte=" + carte + ", distance=" + distance
                    + ", throwCard=" + throwCard + ", giveCard=" + giveCard + "]";
        }
        
    }
    
    class Application {
        int id;
        Cards tasks = new Cards();
        
        int getTaskCount() {
            return IntStream.of(tasks.c).sum();
        }
        
        @Override
        public String toString() {
            return "Appli [id=" + id + ", tasks=" + Arrays.toString(tasks.c) + "]";
        }
        
    }
    
    class Joueur {
        int location; // id of the zone in which the player is located
        int score;
        int permanentDailyRoutineCards; // number of DAILY_ROUTINE the player has played. It allows them to take cards from the adjacent zones
        int permanentArchitectureStudyCards; // number of ARCHITECTURE_STUDY the player has played. It allows them to draw more cards
        int dette = 0;
    }
  
    class Cards { 
        int[] c = new int[10];

        public Cards() {
            reset();
        }

        public Cards(int[] cards) {
            this.c = cards;
        }
         
        public Cards reset() {
            for (int i = 0; i < c.length; i++) {
                c[i] = 0; 
            }
            return this;
        }

        Cards duplique() {
            int[] newCards = new int[10];
            for (int i = 0; i < newCards.length; i++) {
                newCards[i] = c[i]; 
            }
            return new Cards(newCards);
        }
        
        Cards ajoute(Cards ajout) {
            for (int i = 0; i < c.length; i++) {
                c[i] += ajout.c[i]; 
            }
            return this;
        }
        
        Cards soustrait(Cards soustrait) {
            for (int i = 0; i < c.length; i++) {
                c[i] -= soustrait.c[i]; 
            }
            return this;
        }
        
        Cards divise(int diviseur) {
            for (int i = 0; i < c.length; i++) {
                c[i] /= diviseur; 
            }
            return this;
        }
       
        @Override
        public String toString() {
            return "Cards [" + Arrays.toString(c) + "]";
        }
        
    }

    List<int[]> combinaison2Cartes(Cards cards) {
        //logDebug("combinaison2Cartes", cards);
        
        List<int[]> results = new LinkedList<>();
        List<Integer> cardsList = new LinkedList<>();
        
        for (int i = 0; i < 9; i++) {
            if (cards.c[i] > 0) {
                cardsList.add(i);
                if (cards.c[i] > 1) { 
                    results.add(new int[] {i, i});
                }
            }
        }
        
        if (cardsList.isEmpty()) {
            results.add(new int[] {-1, -1});
        } else if (cardsList.size() == 1 && !results.isEmpty()) {
            // result est un couple unique
        } else if (cardsList.size() == 1 && results.isEmpty()) {
            results.add(new int[] {cardsList.get(0), -1});
        } else {
            
            //logDebug("cardsList", Arrays.toString(cardsList.toArray()));
            
            for (int i = 0; i < cardsList.size()-1; i++) {
                for (int j = i+1; j < cardsList.size(); j++) {
                    results.add(new int[] {cardsList.get(i), cardsList.get(j)});
                }
            }
        }
        
        //results.forEach(couple -> {logDebug("++", Arrays.toString(couple));});
        
        return results;
        
    }

    /*
    Développement CODING (1). L’équipe pioche 1 carte de sa pioche et peut jouer deux cartes de plus.

    Formation TRAINING (0). L’équipe pioche 2 cartes de sa pioche et peut jouer une carte de plus.

    Revue de code CODE_REVIEW (6). L’équipe récupère 2 nouvelles cartes compétence BONUS et les met dans sa défausse.

    REFACTORING(7). L’équipe se débarrasse définitivement d’une carte Dette Technique de sa main.

    Etude d'architecture ARCHITECTURE_STUDY (4). Cette compétence est permanente : une fois jouée, elle reste active jusqu’à ce que l’équipe ait livré une application.
    L’équipe piochera une carte de plus au début de son tour. L’effet peut être cumulatif.

    Priorisation des tâches TASK_PRIORITIZATION (3). L’équipe se débarrasse d’une carte compétence de sa main et récupère une carte compétence disponible sur le plateau de jeu.

    Point d'équipe quotidien DAILY_ROUTINE (2). Cette compétence est permanente : une fois jouée, elle reste active jusqu’à ce que l’équipe ait livré une application.
    Après son déplacement, l’équipe pourra récupérer une carte compétence d’un poste de travail éloigné de 1. L’effet peut être cumulatif.

    Intégration Continue CONTINUOUS_INTEGRATION (5). L’équipe automatise une de ses compétences disponibles dans sa main.
    Cette carte ne sera pas défaussée à la fin du tour (elle sera toujours disponible) et ne pourra servir que pour livrer une application.
    */
    
    // De la plus faible à la plus forte
    private static final int[] PRIORITE = new int[] {1, 0, 6, 7, 4, 3, 2, 5};
    
    int[][] proche = new int[][] {
        {1, 1, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 0, 0, 0, 0, 0},
        {0, 1, 1, 1, 0, 0, 0, 0},
        {0, 0, 1, 1, 1, 0, 0, 0},
        {0, 0, 0, 1, 1, 1, 0, 0},
        {0, 0, 0, 0, 1, 1, 1, 0},
        {0, 0, 0, 0, 0, 1, 1, 1},
        {1, 0, 0, 0, 0, 0, 1, 1}        
    }; 
    
    private static final int PRI = 3;
    
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