package codingame.divers;
import java.util.*;
import java.util.stream.IntStream;
import java.io.*;

/**
 * Principe Général : Brut Force combinatoire et ajout d'un calcul de score de la valeur stratégique
 * de la position.
 *
 * Il n'y a pas de stratégie hard-codée à part des cas très particulier sur les cartes CODING et TRAINING.
 
 * On fait un calcul de la combinatoire de tous les coups possibles pour obtenir toutes les positions
 * possibles et choisir la plus intéressantes.
 * 
 * Un algorithme de valorisation de la position avait été développé pour évaluer la position sur la 
 * probabilité de faire une application mais cela a donné de mauvais résultats. Le code se défaussait
 * de ses cartes bonus car il calculait qu'en réduisant le nb de cartes, il y avait plus de probabilité
 * d'obtenir des cartes compétences qui vallent le double.
 * 
 * Comme l'algorithme prend de mauvaises décisions au début de la partie, ajout d'une bibliothèque d'ouverture.
 * Elle n'a pas été très développée dans ce code mais a permis de gagner quelques places.
 * 
 **/

class GreenCircle {
    private static final int NB_TACHES = 8;
    private static final int INFAISABLE = 99;

    private static final int MAX_AJOUT_DETTE_CAS_NORMAL = 2;
    private static final int MAX_AJOUT_DETTE_SI_RETARD = 4;
    private static final boolean OPT_FUSION_PIOCHE_ET_DEFAUSSE = true;
    
    String commande;
    Ordre planEnCours = null;
    Deplacement deplEnCours = null;
    
    // --- Stats sur les cartes
    
    // Cartes demandées par les applications restantes
    Cards cartesDemandees;
    // Vrai si cartesDemandees > 0
    boolean[] cartesUtiles;
    // Prochaine carte sur le poste
    Cards prochaineCarte;
    
    // Stats algorithme : nb de calcul de feuilles de la combinatoire. 
    long cptAnaRelease = 0;
    
    // Gestion time out
    long TIME_OUT = 45;
    boolean hasTimedOut;
    long timeStart; 
    
    public static void main(String args[]) {
        GreenCircle player = new GreenCircle();
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
            
            etat.pioche = new Cards();
            if (etat.cardsLocations.get("DRAW") != null) {
                etat.pioche = etat.cardsLocations.get("DRAW");
            }

            etat.defausse = new Cards();
            if (etat.cardsLocations.get("DISCARD") != null) {
                etat.defausse = etat.cardsLocations.get("DISCARD");
            }

            etat.automated = new Cards();
            if (etat.cardsLocations.get("AUTOMATED") != null) {
                etat.automated = etat.cardsLocations.get("AUTOMATED");
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
           
            commande = null;
            timeStart = System.nanoTime();
            hasTimedOut = false;
            cptAnaRelease = 0;
            
            strategie(etat);
            
            System.err.println("Duree: "+Math.floorDiv(System.nanoTime()-timeStart, 1_000_000)+"ms "
                    + ", cptAnaRelease: " +cptAnaRelease + (hasTimedOut ? " hasTimedOut" : ""));
            System.out.println(commande);
        }
    }
    
    private void strategie(Etat etat) {
        logDebug("etat.gamePhase", etat.gamePhase);
        
        // Enrichissement
        statsCartes(etat);
        statsJoueurs(etat);
        ajoutDeplacementsPossibles(etat);
        
        // S'il y a un plan en cours alors on l'applique (bêtement).
        applicationDuPlanEnCours(etat);
        if (commande != null) return;
        
        if (etat.gamePhase.equals("MOVE")) {
            strategieBibliothequeOuvertures(etat);
            strategieRechercheReleaseParLaCombinatoire(etat);

        } else if (etat.gamePhase.equals("THROW_CARD")) {
            strategieThrowCombinatoire(etat);
            strategieThrowIntuitive(etat);
            if (commande == null) commande =  "RANDOM";
            
        } else if (etat.gamePhase.equals("GIVE_CARD")) {
            strategieGiveCardCombinatoire(etat);
            strategieGiveCardIntuitive(etat);
            if (commande == null) commande = "RANDOM";
        
        } else if (etat.gamePhase.equals("PLAY_CARD")) {
            strategiePlayCardCombinatoire(etat);
            //strategiePlayCardIntuitive(etat);
            if (commande == null) commande =  "WAIT";
        
        } else if (etat.gamePhase.equals("RELEASE")) {
            strategiePourReleaseFinaleRapide(etat);
            strategiePourRelease(etat);
            if (commande == null) commande = "WAIT";
        
        } else {
            commande =  "RANDOM bug";
        }

    }

    /**
     *  Calcul de statistiques sur les cartes.
     */
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
    }

    /**
     * Calcul de statistiques sur les joueurs.
     */
    
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

    /**
     * Calcul de toutes les combinaisons de déplacement possible et ajout dans l'objet Etat. 
     */
    
    private void ajoutDeplacementsPossibles(Etat etat) {
        
        for (int i = 1; i < 9; i++) {
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
    
    /**
     * Calculette pour ajouter un décallage sur le poste pour obtenir le poste cible.
     * Gère le problème des postes circulaires.
     */
    
    int addPoste(int poste, int delta) {
        return (poste + delta + 8) % 8;
    }
    
    /**
     * Stratégie d'application du plan en cours.
     */
    
    void applicationDuPlanEnCours(Etat etat) {
        if (etat.gamePhase.equals("MOVE") || planEnCours == null) {
            return;
        }
        
        log("Plan en cours", planEnCours);
        
        if (etat.gamePhase.equals("THROW_CARD")) {
            commande = "THROW " + planEnCours.throwCards[0] + " a"+planEnCours.idAppli+"d"+planEnCours.dette;
            planEnCours.throwCards[0] = planEnCours.throwCards[1];

        } else if (etat.gamePhase.equals("GIVE_CARD")) {
            commande = "GIVE " + planEnCours.giveCard + " a"+planEnCours.idAppli+"d"+planEnCours.dette;
        
        } else if (etat.gamePhase.equals("PLAY_CARD")) {
            
            // Cas particulier des training et Coding, si le plan ne semble pas impacté par
            // leur perte alors on les joue. On recalculera un plan ensuite ...
            // TODO : imprécision : il peut y avoir une release prévue mais le coding/training n'est pas utilisée.

            if (etat.main.c[COD] > 0 && planEnCours.playCard[1] != COD && planEnCours.idAppli == -1) {
                commande = "CODING pif";
                planEnCours = null;
                return;
            }

            if (etat.main.c[TRA] > 0 && planEnCours.playCard[1] != TRA && planEnCours.idAppli == -1) {
                commande = "TRAINING pif";
                planEnCours = null;
                return;
            }
            
            // Sinon on applique le plan
            
            if (planEnCours.playCard[0] > -1) {
                commande = titreCartes[planEnCours.playCard[0]];
                if (planEnCours.playCard[1] > -1) commande += " " + planEnCours.playCard[1];
                if (planEnCours.playCard[2] > -1) commande += " " + planEnCours.playCard[2];
                commande += " a"+planEnCours.idAppli+"d"+planEnCours.dette;
            } else {
                commande = "WAIT a"+planEnCours.idAppli+"d"+planEnCours.dette;
            }
    
        } else if (etat.gamePhase.equals("RELEASE")) {
            if (planEnCours.idAppli > -1) {
                commande = "RELEASE "+planEnCours.idAppli+" d"+planEnCours.dette;
            // } else { commande = "WAIT a"+planEnCours.idAppli+"d"+planEnCours.dette;
            }
        }
        
    }

    /**
     * Application d'une bibliothèque d'ouvertures.
     * N'a pas été très développé ici car on n'est pas sur qu'il y ait un gain énorme.
     */
    
    private void strategieBibliothequeOuvertures(Etat etat) {
        if (commande != null) return;
        
        if (etat.adv.location > -1 && etat.moi.location == -1) {
            int [] moves = new int[] {5, 5, 5, 5, 2, 2, 2, 5};
            int poste = moves[etat.adv.location];
            
            Deplacement depl = etat.deplacements.stream().filter(d -> d.poste == poste).findAny().get();
            
            commande = "MOVE " + poste + " bo";
            deplEnCours = depl;
        }
        
    }

    private void strategieRechercheReleaseParLaCombinatoire(Etat etat) {
        if (commande != null) return;
        
        Ordre ordre = new Ordre();
        calculScoreIntuitif(etat, ordre);
        log("Score actuel", ordre.score);

        planEnCours = null;
        Ordre ordreSelect = meilleurMoveCombinatoire(etat);

        log("Ordre/comb", ordreSelect.score, ordreSelect);
        
        if (ordreSelect != null) {
            if (detteAcceptable(etat, ordreSelect.dette)) {
                commande = "MOVE "+ordreSelect.depl.poste + " "+ordreSelect.depl.carte + " a"+ordreSelect.idAppli+"d"+ordreSelect.dette;
            } else {
                // Il ne faut pas jouer l'appli
                ordreSelect.idAppli = -1;
                ordreSelect.dette = INFAISABLE;
                commande = "MOVE "+ordreSelect.depl.poste + " "+ordreSelect.depl.carte + " s"+ordreSelect.score;
            }
            planEnCours = ordreSelect;
            deplEnCours = ordreSelect.depl;
            log("Plan a venir", planEnCours);
        }

    }

    /**
     * Détermine si la dette générée par la release est acceptable.
     */
    
    boolean detteAcceptable(Etat etat, int detteOrdre) {

        if ( ((etat.moi.score == 4 && detteOrdre == 0)
            || (etat.moi.score < 4
                    && (detteOrdre <= MAX_AJOUT_DETTE_CAS_NORMAL
                        || (detteOrdre <= MAX_AJOUT_DETTE_SI_RETARD
                                && etat.moi.score < etat.adv.score
                                && etat.moi.dette + detteOrdre < etat.adv.dette))))) {
            return true;
        }
        return false;
    }

    private void strategieThrowCombinatoire(Etat etat) {
        if (commande != null) return;
        
        Ordre ordre = meilleurThrowCardCombinatoire(etat, deplEnCours);
        
        commande = "THROW " + ordre.throwCards[0] + " combi";
    }
    
    private void strategieThrowIntuitive(Etat etat) {
        if (commande != null) return;
        
        int carteSelect = plusMauvaiseCarte(etat.main);
        if (carteSelect > -1) {
            commande = "THROW " + carteSelect;
            return;
        }
    }
    
    private void strategieGiveCardCombinatoire(Etat etat) {
        if (commande != null) return;
        
        Ordre ordre = meilleurGiveCardCombinatoire(etat, deplEnCours);
        commande = "GIVE " + ordre.giveCard + " combi";

    }
    
    private void strategieGiveCardIntuitive(Etat etat) {
        if (commande != null) return;
        
        int carteSelect = plusMauvaiseCarte(etat.main);
        if (carteSelect > -1) {
            commande = "GIVE " + carteSelect;
            return;
        }
    }

    private void strategiePlayCardCombinatoire(Etat etat) {
        if (commande != null) return;
        
        Ordre ordre = new Ordre();
        calculScoreIntuitif(etat, ordre);
        log("Score sans play card", ordre.score);
        
        Ordre meilleurOrdre = meilleurPlayCardCombinatoire(etat);
        log("Score avec play card", meilleurOrdre.score, meilleurOrdre);
        
        if (!detteAcceptable(etat, meilleurOrdre.dette)) {
            log("Dette non acceptable: effacement release");
            meilleurOrdre.idAppli = -1;
            meilleurOrdre.dette = INFAISABLE;
        }
        
        // Cas particulier des training et Coding, si le plan ne semble pas impacté par
        // leur perte alors on les joue. On recalculera un plan ensuite ...
        // TODO : imprécision : il peut y avoir une release prévue mais le coding/training n'est pas utilisée.

        if (etat.main.c[COD] > 0 && meilleurOrdre.playCard[1] != COD && meilleurOrdre.idAppli == -1) {
            commande = "CODING pif";
            return;
        }

        if (etat.main.c[TRA] > 0 && meilleurOrdre.playCard[1] != TRA && meilleurOrdre.idAppli == -1) {
            commande = "TRAINING pif";
            return;
        }
        
        // On jour le play card prévu
        
        if (meilleurOrdre.playCard[0] > -1) {
            commande = titreCartes[meilleurOrdre.playCard[0]];
            if (meilleurOrdre.playCard[1] > -1) commande += " " + meilleurOrdre.playCard[1];
            if (meilleurOrdre.playCard[2] > -1) commande += " " + meilleurOrdre.playCard[2];
            commande += " combi";
            return;
        }
        
        // TODO : pas sur que ces CODING et TRAINING soient utiles. 
        
        // CODING : L’équipe pioche 1 carte de sa pioche et peut jouer deux cartes de plus.
        if (etat.main.c[1] > 0) {
            commande = "CODING icombi ";
            return;
        }
        
        // Training : L’équipe pioche 2 cartes de sa pioche et peut jouer une carte de plus
        if (etat.main.c[0] > 0) {
            commande = "TRAINING icombi";
            return;
        }
        
        commande = "WAIT i";
    }
    
    /**
     * Play Card avec des stratégie hard-codées.
     * @deprecated
     */
    
    @Deprecated
    private void strategiePlayCardIntuitive(Etat etat) {
        if (commande != null) return;
        
        Cards main = etat.cardsLocations.get("HAND");
        log("strategiePlayCard: cartesDemandees", cartesDemandees);
        
        // On regarde si on a une release possible
        strategiePourRelease(etat);
        if (commande != null) {
            log("A venir:", commande);
            commande = "WAIT i";
            return;
        } else {
            log("Pas de release à venir");
        }

        // On fait ces 2 cartes en premier si elle ne sont pas utiles
        // Sinon on les fera à nouveau à la fin
        
        // Training : L’équipe pioche 2 cartes de sa pioche et peut jouer une carte de plus
        if (main.c[0] > 0 && cartesDemandees.c[0] == 0) {
            commande = "TRAINING i";
            return;
        }
        
        // CODING : L’équipe pioche 1 carte de sa pioche et peut jouer deux cartes de plus.
        if (main.c[1] > 0 && cartesDemandees.c[1] == 0) {
            commande = "CODING i";
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
                    commande = "CONTINUOUS_INTEGRATION " + i + " i";
                    return;
                }
            }
        }
        
        // DAILY_ROUTINE (2) : Cette compétence est permanente : une fois jouée, elle reste active
        // jusqu’à ce que l’équipe ait livré une application.
        // Après son déplacement, l’équipe pourra récupérer une carte compétence d’un poste de travail
        // éloigné de 1. L’effet peut être cumulatif.
        
        // On cherche à avoir au moins le niveau 1.
        // On montera plus haut si la carte est inutile. 

        
        if (main.c[2] > 0
                && (cartesDemandees.c[2] == 0 || etat.moi.permanentDailyRoutineCards == 0)) {
            log("DAILY_ROUTINE", cartesDemandees.c[2] == 0, etat.moi.permanentDailyRoutineCards == 0);
            commande = "DAILY_ROUTINE i";
            return;
        }
        
        // ARCHITECTURE_STUDY (4) : Cette compétence est permanente : une fois jouée, elle reste active
        // jusqu’à ce que l’équipe ait livré une application.
        // L’équipe piochera une carte de plus au début de son tour. L’effet peut être cumulatif.
        
        // On cherche à avoir au moins le niveau 1.
        // On montera plus haut si la carte est inutile. 
        
        if (main.c[4] > 0
                && (cartesDemandees.c[4] == 0 || etat.moi.permanentArchitectureStudyCards == 0)) {
            log("ARCHITECTURE_STUDY", cartesDemandees.c[4], etat.moi.permanentArchitectureStudyCards);
            commande = "ARCHITECTURE_STUDY i";
            return;
        }
        
        // TASK_PRIORITIZATION (3) : L’équipe se débarrasse d’une carte compétence de sa main et récupère
        // une carte compétence disponible sur le plateau de jeu.
        
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
                commande = "TASK_PRIORITIZATION "+carteInutile+" "+carteUtile + " i";
                return;
            }
        }
        
        // REFACTORING (7). L’équipe se débarrasse définitivement d’une carte Dette Technique de sa main.
        if (main.c[7] > 0 && main.c[DET] > 0) {
            commande = "REFACTORING i";
            return;
        }
        
        // CODE_REVIEW : L’équipe récupère 2 nouvelles cartes compétence BONUS et les met dans sa défausse.
        if (main.c[6] > 0 /* && cartesDemandees.c[6] == 0*/) {
            commande = "CODE_REVIEW i";
            return;
        }

        // Training : L’équipe pioche 2 cartes de sa pioche et peut jouer une carte de plus
        if (main.c[0] > 0 /* && cartesDemandees.c[0] == 0*/) {
            commande = "TRAINING i";
            return;
        }
        
        // CODING : L’équipe pioche 1 carte de sa pioche et peut jouer deux cartes de plus.
        if (main.c[1] > 0 /* && cartesDemandees.c[1] == 0*/) {
            commande = "CODING i";
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
        
        Ordre ordreRelease = meilleureRelease(etat);

        logDebug("ordreRelease", ordreRelease);
        
        if (ordreRelease.idAppli > -1 && detteAcceptable(etat, ordreRelease.dette)) {
            logDebug("appliSelected", ordreRelease.idAppli);
            commande = "RELEASE "+ordreRelease.idAppli + " d"+ordreRelease.dette+"b";
        }
        doLogDebug = prevDoLogDebug;

    }

    /**
     * Calcul de la combinatoire de tous les coups possible pour obtenir toutes les positions
     * possibles et choisir la plus intéressantes.
     */
    Ordre meilleurMoveCombinatoire(Etat etat) {
        String chevrons = "#1:";
         
        Ordre ordreSelect = null;
        
        for (Deplacement depl : etat.deplacements) {
            long duration = Math.floorDiv(System.nanoTime()-timeStart, 1_000_000);
            if (duration > TIME_OUT) {
                hasTimedOut = true;
                break;
            }
            
            logDebug(chevrons, "{ -------- move ", depl, "prochaineCarte", prochaineCarte.c[depl.carte], etat.main);
            
            Ordre ordre = meilleurThrowCardCombinatoire(etat, depl);
            ordre.depl = depl;
            
            ordreSelect = choixMeilleurOrdre(ordre, ordreSelect);
            logDebug(chevrons, "}", "ordreSelect", ordreSelect);
        }
        
        logDebug(chevrons, "meilleurOrdre", ordreSelect);
        return ordreSelect;
    }
    
    Ordre meilleurThrowCardCombinatoire(Etat etat, Deplacement depl) {
        String chevrons = "##2:";
        
        Ordre ordreSelect = null;
                
        if (depl.throwCard) {
            logDebug(chevrons, "{ passageAdmin ++", depl.poste, "<", etat.moi.location);

            List<int[]> combinaisons = combinaison2Cartes(etat.main);
            
            for (int[] throwCards : combinaisons) {

                if (throwCards[0] > -1) etat.main.c[throwCards[0]]--; else etat.defausse.c[DET] += 2;
                if (throwCards[1] > -1) etat.main.c[throwCards[1]]--; else etat.defausse.c[DET] += 2;
                
                logDebug(chevrons, "throw cards", Arrays.toString(throwCards), "sauveMain", etat.main, "nouvMain", etat.main);
               
                Ordre ordre = meilleurGiveCardCombinatoire(etat, depl);
                ordre.throwCards = throwCards;
                
                if (throwCards[0] > -1) etat.main.c[throwCards[0]]++; else etat.defausse.c[DET] -= 2;
                if (throwCards[1] > -1) etat.main.c[throwCards[1]]++; else etat.defausse.c[DET] -= 2;

                ordreSelect = choixMeilleurOrdre(ordre, ordreSelect);
            }

        } else {
            
            logDebug(chevrons, "{ pas passageAdmin", depl.poste, ">=", etat.moi.location);
            ordreSelect = meilleurGiveCardCombinatoire(etat, depl);

        }
       
        logDebug(chevrons, "} meilleurOrdre", ordreSelect);
        return ordreSelect;
    }

    Ordre meilleurGiveCardCombinatoire(Etat etat, Deplacement depl) {
        String chevrons = "###3:";
        Ordre ordreSelect = null;
        
        if (depl.giveCard) {
            
            logDebug(chevrons, "{ proche");
            
            boolean auMoinsUneCarte = false;
            for (int i = 0; i < 9; i++) {
                if (etat.main.c[i] > 0) {
                    logDebug(chevrons, "GiveCard", i);
                    auMoinsUneCarte = true;
                    etat.main.c[i]--;
                    
                    Ordre ordre = jouerDeplacementEtReceptionCarte(etat, depl);
                    ordre.giveCard = i;
                    
                    etat.main.c[i]++;
                    
                    ordreSelect = choixMeilleurOrdre(ordre, ordreSelect);
                }
            }
            if (!auMoinsUneCarte) {
                logDebug(chevrons, "pas carte = dette +2");
                            
                etat.defausse.c[DET] += 2;
                ordreSelect = jouerDeplacementEtReceptionCarte(etat, depl);
                etat.defausse.c[DET] -= 2;
            }
            
        } else {
            logDebug(chevrons, "{ pas proche");
            ordreSelect = jouerDeplacementEtReceptionCarte(etat, depl);
        }
         
        logDebug(chevrons, "} GiveCard select", ordreSelect.giveCard, "Appli selected", ordreSelect.idAppli, "dette", ordreSelect.dette);
        
        return ordreSelect;
    }

    Ordre jouerDeplacementEtReceptionCarte(Etat etat, Deplacement depl) {
        String chevrons = "####4:";

        etat.main.c[prochaineCarte.c[depl.carte]]++;
        int sauvLocation = etat.moi.location;
        etat.moi.location = depl.poste;
        
        logDebug(chevrons, "Reception carte", depl.carte, "main", etat.main);
        
        Ordre ordre = meilleurPlayCardCombinatoire(etat);
        
        etat.main.c[prochaineCarte.c[depl.carte]]--;
        etat.moi.location = sauvLocation;
        
        return ordre;
    }

    Ordre meilleurPlayCardCombinatoire(Etat etat) {
        String chevrons = "#####5:";
        
        logDebug(chevrons, "{ Valorisation sans play card", "main", etat.main, "pioche", etat.pioche, "defausse", etat.defausse);
        Ordre ordreSelect = meilleureRelease(etat);

        // Priorisation des tâches TASK_PRIORITIZATION (3). L’équipe se débarrasse d’une carte
        // compétence de sa main et récupère une carte compétence disponible sur le plateau de jeu.
        
        if (etat.main.c[PRI] > 0) {
            logDebug(chevrons, "TaskPriorisation");
            List<int[]> priorisations = listePriorisations(etat.main);
            
            for (int[] prio : priorisations) {
                
                etat.main.c[PRI]--;
                etat.defausse.c[PRI]++;
                etat.main.c[prio[0]]--;
                etat.main.c[prio[1]]++;
                
                logDebug(chevrons, "> prio", Arrays.toString(prio), "nouvelleMain", etat.main, "pioche", etat.pioche, "defausse", etat.defausse);
                
                Ordre ordre = meilleureRelease(etat);
                ordre.playCard = new int[] {PRI, prio[0], prio[1]};
                
                etat.main.c[PRI]++;
                etat.defausse.c[PRI]--;
                etat.main.c[prio[0]]++;
                etat.main.c[prio[1]]--;
                
                ordreSelect = choixMeilleurOrdre(ordre, ordreSelect);
            }
        }   
          
        // CONTINUOUS_INTEGRATION (5) : L’équipe automatise une de ses compétences.
        
        if (etat.main.c[CI] > 0) {
            for (int i = 0; i < 9; i++) {
                if (etat.main.c[i] > 0 && (i != 5 || etat.main.c[5] > 1)) {
                    
                    etat.main.c[CI]--;
                    etat.defausse.c[CI]++;
                    etat.main.c[i]--;
                    etat.automated.c[i]++; 
                    
                    logDebug(chevrons, "> automated", i, "nouvelleMain", etat.main, "pioche", etat.pioche, "defausse", etat.defausse);
                    
                    Ordre ordre = meilleureRelease(etat);
                    ordre.playCard = new int[] {CI, i, -1};
                    
                    etat.main.c[CI]++;
                    etat.defausse.c[CI]--;
                    etat.main.c[i]++;
                    etat.automated.c[i]--;
                    
                    ordreSelect = choixMeilleurOrdre(ordre, ordreSelect);
                }
            }
        }
        
        // DAILY_ROUTINE (2) : Cette compétence est permanente : une fois jouée, elle reste active
        // jusqu’à ce que l’équipe ait livré une application.
        // Après son déplacement, l’équipe pourra récupérer une carte compétence d’un poste de travail
        // éloigné de 1. L’effet peut être cumulatif.
        
        if (etat.main.c[DAI] > 0) {
            
            etat.main.c[DAI]--;
            etat.moi.permanentDailyRoutineCards++;
            
            logDebug(chevrons, "> daily routine", "nouvelleMain", etat.main, "pioche", etat.pioche, "defausse", etat.defausse);
            
            Ordre ordre = meilleureRelease(etat);
            ordre.playCard = new int[] {DAI, -1, -1};
            
            etat.main.c[DAI]++;
            etat.moi.permanentDailyRoutineCards--;
            
            ordreSelect = choixMeilleurOrdre(ordre, ordreSelect);
        }            
        
        // Etude d'architecture ARCHITECTURE_STUDY (4). Cette compétence est permanente : une fois
        // jouée, elle reste active jusqu’à ce que l’équipe ait livré une application.
        // L’équipe piochera une carte de plus au début de son tour. L’effet peut être cumulatif.
        
        if (etat.main.c[ARC] > 0) {
            
            etat.main.c[ARC]--;
            etat.moi.permanentArchitectureStudyCards++;
            
            logDebug(chevrons, "> archi study", "nouvelleMain", etat.main, "pioche", etat.pioche, "defausse", etat.defausse);
            
            Ordre ordre = meilleureRelease(etat);
            ordre.playCard = new int[] {ARC, -1, -1};
            
            etat.main.c[ARC]++;
            etat.moi.permanentArchitectureStudyCards--;
            
            ordreSelect = choixMeilleurOrdre(ordre, ordreSelect);
        }            
                
        // REFACTORING (7). L’équipe se débarrasse définitivement d’une carte Dette Technique de sa main.
        
        if (etat.main.c[REF] > 0 && etat.main.c[DET] > 0) {
            
            etat.main.c[REF]--;
            etat.defausse.c[REF]++;
            etat.main.c[DET]--;
            
            logDebug(chevrons, "> REFACTORING", "nouvelleMain", etat.main, "pioche", etat.pioche, "defausse", etat.defausse);
            
            Ordre ordre = meilleureRelease(etat);
            ordre.playCard = new int[] {REF, -1, -1};
            
            etat.main.c[REF]++;
            etat.defausse.c[REF]--;
            etat.main.c[DET]++;
            
            ordreSelect = choixMeilleurOrdre(ordre, ordreSelect);
        }
        
        // CODE_REVIEW : L’équipe récupère 2 nouvelles cartes compétence BONUS et les met dans sa défausse.
        
        if (etat.main.c[REV] > 0) {
            
            etat.main.c[REV]--;
            etat.defausse.c[REV]++;
            etat.defausse.c[BON] += 2;
            
            logDebug(chevrons, "> CODE_REVIEW", "nouvelleMain", etat.main, "pioche", etat.pioche, "defausse", etat.defausse);
            
            Ordre ordre = meilleureRelease(etat);
            ordre.playCard = new int[] {REV, -1, -1};
            
            etat.main.c[REV]++;
            etat.defausse.c[REV]--;
            etat.defausse.c[BON] -= 2;
            
            ordreSelect = choixMeilleurOrdre(ordre, ordreSelect);
        }
        
        logDebug(chevrons, "} meilleurOrdre", ordreSelect);
        return ordreSelect;
    }

    /**
     * Liste de toutes les combinaisons de priorisations possibles.
     */
    
    List<int[]> listePriorisations(Cards main) {
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

    Ordre meilleureRelease(Etat etat) {
        String chevrons = "######6:";
        cptAnaRelease++;
        
        Cards mainReelle = etat.main.duplique().ajoute(etat.automated);
        
        Ordre ordre = new Ordre();
        ordre.dette = INFAISABLE;
        
        int meilleureDette = INFAISABLE;
        int meilleureAppliId = -1;
                
        logDebug(chevrons, "mainReelle ", mainReelle);
        
        for (Application application : etat.applications) {
            logDebug(chevrons, application);
            int dette = calculAjoutDette(mainReelle, application);
            if (dette != INFAISABLE) {
                logDebug(chevrons, "dette", dette);
                if (dette < meilleureDette || meilleureDette == INFAISABLE) {
                    meilleureAppliId = application.id;
                    meilleureDette = dette;
                }
                if (dette == 0) {
                    break;
                }
            } else {
                logDebug(chevrons, " > infaisable");
            }
        }
        
        ordre.idAppli = meilleureAppliId;
        ordre.dette = meilleureDette;


        //calculScoreBaseProba(etat, ordre);
        calculScoreIntuitif(etat, ordre);
        
        logDebug(chevrons, "ordre", ordre);
        return ordre;
        
    }

    private void calculScoreIntuitif(Etat etat, Ordre ordre) {
        logDebug("~", "calculScoreIntuitif", etat);
        
        // Scores (dette) 1000, 750, 500, 250, 0
        // Scores (bonus) 600, 1200, 1800, 2400
        
        if (ordre.dette < INFAISABLE && detteAcceptable(etat, ordre.dette)) {
            ordre.score += 1_000 - ordre.dette * 250;
            logDebug("=", "faisable", ordre.score);
        }

        if (etat.moi.score == 4 && ordre.dette == 0) {
            int scoreFinDePartie = 10_000;
            logDebug("=", "scoreFinDePartie", scoreFinDePartie);
            ordre.score += scoreFinDePartie;
        }
        
        Cards paquet = etat.pioche.duplique().ajoute(etat.defausse).ajoute(etat.main);
        int cpt = paquet.getCount();
        logDebug("~", "fusion paquet", paquet, cpt);

        for (int i = 0; i < 8; i++) {
            int nbCartesAutomated = Math.min(etat.automated.c[i], cartesDemandees.c[i]);
            int nbCartesUtilesRestantes = cartesDemandees.c[i] - nbCartesAutomated;
            int nbCartesUtilesDansPaquet = Math.min(paquet.c[i], nbCartesUtilesRestantes);

            // Ne pas automatiser de cartes de compétence avant d'avoir 4 cartes bonus automatisées.
            int scoreCartesAutomated = nbCartesAutomated * (etat.automated.c[BON] < 4 ? 0 : 50);
            int scoreCartesUtilesDansPaquet = nbCartesUtilesDansPaquet * 10;
            int scoreCartesPresentesDansPaquet = paquet.c[i] * 10;
            logDebug("+", i, "automated", scoreCartesAutomated, "utiles", scoreCartesUtilesDansPaquet, "intraseque", scoreCartesPresentesDansPaquet);
            ordre.score += scoreCartesAutomated + scoreCartesUtilesDansPaquet + scoreCartesPresentesDansPaquet;
        }
        
        int scoreCartesBonusDansPaquet = paquet.c[BON] * 20;
        int scoreCartesBonusAutomated = Math.min(4, etat.automated.c[BON]) * 600;
        int scoreDette = paquet.c[DET] * -20;
        
        int scorePermArchi = Math.min(etat.moi.permanentArchitectureStudyCards, 1) * 50;
        int scorePermCI = Math.min(etat.moi.permanentDailyRoutineCards, 1) * 50;
        
        // Bonus pour l'arrêt en début de cercle
        int scoreBonusTraining = etat.main.c[TRA] > 0 ? 15 : 0;
        int scoreBonusCoding = etat.main.c[COD] > 0 ? 10 : 0;
        if (etat.adv.score == 5) {
            scoreBonusTraining = etat.main.c[TRA] > 0 ? 300 : 0;
            scoreBonusCoding = etat.main.c[COD] > 0 ? 500 : 0;
        }
       
        logDebug("+", "scoreCartesBonusDansPaquet", scoreCartesBonusDansPaquet, "scoreCartesBonusAutomated", scoreCartesBonusAutomated,
                "dettte", scoreDette, "scorePermArchi", scorePermArchi, "scorePermCI", scorePermCI,
                "bonusTraining", scoreBonusTraining, "bonusCoding", scoreBonusCoding);
        
        ordre.score += scoreCartesBonusDansPaquet + scoreCartesBonusAutomated + scorePermArchi
                + scorePermCI + scoreDette + scoreBonusTraining + scoreBonusCoding;
        
        logDebug("= score", ordre.score);
        
    }

    /**
     * Choix du meilleur ordre.
     */
    Ordre choixMeilleurOrdre(Ordre ordre1, Ordre ordre2) {
        
        // S'il n'y a qu'un seul ordre, on le choisit d'office.
        
        if (ordre1 == null) {
            return ordre2;
        } else if (ordre2 == null) {
            return ordre1;
        }
        
        if (ordre1.score > ordre2.score) {
            return ordre1;
        } else {
            return ordre2;
        }
    }
    
    /**
     * Calcul de l'ajout de dette si on fait l'application avec la main en cours.
     */
    
    private int calculAjoutDette(Cards main, Application application) {
        
        int dette = INFAISABLE;

        int tasksOk = main.c[8];
        int tasksBaclees = main.c[8];
         
        for (int i = 0; i < 8; i++) {
            tasksOk += Math.min(application.tasks.c[i], main.c[i] * 2);
            tasksBaclees += main.c[i] * 2;
        }
        
        if (tasksOk + tasksBaclees >= NB_TACHES) {
            dette = NB_TACHES - tasksOk;
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
    
    /**
     * Etat de la position du jeu.
     *
     */
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
        Cards pioche = null;
        Cards defausse = null;
        Cards automated;
        
        List<String> actionsPossibles = new ArrayList<>();
        List<Deplacement> deplacements = new ArrayList<>();
        
        @Override
        public String toString() {
            return "Etat [moi=" + moi + ", main=" + main + ", pioche=" + pioche
                    + ", defausse=" + defausse + ", automated=" + automated + "]";
        }
        
    }

    /**
     * Ordre (et plan) à jouer.
     *
     */
    class Ordre {
        
        Deplacement depl = null;
        int idAppli = -1;
        int dette = INFAISABLE;
        int score = 0;

        // Ces 2 champs ne sont pas utilisés car ils viennent des méthodes basées sur la probabilité
        // de faire l'appli suivantes.
        double probaAppliSuiv = 0D;
        int appliSuivId = -1;
        
        int[] throwCards = new int[] {-2, -2};
        int[] playCard = new int[] {-1, -1, -1};
        int giveCard = -1;
        
        @Override
        public String toString() {
            return "Ordre [depl=" + depl + ", idAppli=" + idAppli + ", dette=" + dette
                    + ", score=" + score
                    + ", probaAppliSuiv=" + probaAppliSuiv + ", appliSuivId=" + appliSuivId
                    + ", throwCards=" + Arrays.toString(throwCards)
                    + ", giveCard=" + giveCard + ", playCard=" + Arrays.toString(playCard) + "]";
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

        int getCount() {
            return IntStream.of(c).sum();
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

    static String[] titreCartes = new String[] {"TRAINING", "CODING", "DAILY_ROUTINE", "TASK_PRIORITIZATION", 
        "ARCHITECTURE_STUDY", "CONTINUOUS_INTEGRATION", "CODE_REVIEW", "REFACTORING"};
    
    private static final int TRA = 0;
    private static final int COD = 1;
    private static final int DAI = 2;
    private static final int PRI = 3;
    private static final int ARC = 4;
    private static final int CI = 5;
    private static final int REV = 6;
    private static final int REF = 7;
    private static final int BON = 8;
    private static final int DET = 9;

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
    
    static int NO_APPLI = -1;
    
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

    
    
    Map<Integer, ArrayList<Cards>> combinaisonsApplis;
    
    /**
     * Tentative de calculer le score d'une position sur la base de la probabilité d'avoir
     * la release suivante.
     */
    
    @Deprecated
    private void calculCombinaisonsApplis(Etat etat) {
        if (combinaisonsApplis != null) return;
        combinaisonsApplis = new HashMap<>();
        
        etat.applications.forEach(appli -> {
            logDebug("% appli", appli);
            ArrayList<Cards> combinaisons = new ArrayList<>();
            combinaisonsApplis.put(appli.id, combinaisons);
            
            Cards main = new Cards();
            for (int i = 0; i < 8; i++) {
                main.c[i] = appli.tasks.c[i] / 2;
            }
            combinaisons.add(main);
            logDebug("% +", main);
            
            // Combinaisons en remplaçant une carte compétence par 2 cartes bonus
            
            for (int i = 0; i < 8; i++) {
                if (main.c[i] > 0) {
                    Cards nouvMain = main.duplique();
                    nouvMain.c[i]--;
                    nouvMain.c[BON] += 2;
                    combinaisons.add(nouvMain);
                    
                    logDebug("% +", nouvMain);
                }
            }
            
            // Combinaisons en remplaçant 2 cartes compétences identiques par 4 cartes bonus
            
            for (int i = 0; i < 8; i++) {
                if (main.c[i] > 0) {
                    Cards nouvMain = main.duplique();
                    nouvMain.c[i] -= 2;
                    nouvMain.c[BON] += 4;
                    combinaisons.add(nouvMain);
                    
                    logDebug("% +", nouvMain);
                }
            }
            
            // Combinaisons en remplaçant 1 carte de chaque compétence par 4 cartes bonus
            
            Cards nouvMain = main.duplique();
            nouvMain.c[BON] += 4;
            
            for (int i = 0; i < 8; i++) {
                if (main.c[i] > 0) {
                    nouvMain.c[i]--;
                }
            }
            combinaisons.add(nouvMain);
            logDebug("% +", nouvMain);
            
        }); 
    }

    @Deprecated
    Ordre calculProbaApplicationSuivante(Etat etat) {
        
        logDebug("~", "calculProbaApplicationSuivante", etat);
        
        LinkedList<Double>[] probasParCartes = new LinkedList[9];
        for (int i = 0; i < 9; i++) {
            probasParCartes[i] = new LinkedList<>();
        }
        
        // Ajout cartes sur la table
        // TODO : Pas sur de ce coup la si on boucle par l'admin ...
        // TODO : Prendre en compte le permanent daily routine
        // TODO : on vient peut être de prendre la carte ...
        //for (int i = etat.moi.location + 1; i < 8; i++) {
        for (int i = 0; i < 8; i++) {
            if (prochaineCarte.c[i] == i) {
                probasParCartes[i].add(1D);
                logDebug("~", "Ajout carte sur la table", i);
            }
        }
        
        // Ajout cartes en automated
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < etat.automated.c[i]; j++) {
                probasParCartes[i].add(1D);
                logDebug("~", "Ajout carte automated", i);
            }
        }
        
        int tailleMain = 4 + etat.moi.permanentArchitectureStudyCards;
        
        if (OPT_FUSION_PIOCHE_ET_DEFAUSSE) {
            Cards fusion = etat.pioche.duplique().ajoute(etat.defausse).ajoute(etat.main);
            int cpt = fusion.getCount();
            logDebug("~", "fusion", fusion, cpt);
            
            for (int i = 0; i < 9; i++) {
                for (int j = fusion.c[i]; j > 0; j--) {
                    // TODO : pas vrai : s'il y a n cartes, il y a n fois plus de chance d'en avoir une
                    double proba = Math.min((double) tailleMain / cpt, 1D);
                    probasParCartes[i].add(proba);
                    logDebug("~", "Ajout carte fusion", i, proba);
                }
            }
           
        } else {
            
            // Ajout cartes en pioche
            
            int cptPioche = etat.pioche.getCount();
            
            for (int i = 0; i < 9; i++) {
                for (int j = etat.pioche.c[i]; j > 0; j--) {
                    // TODO : pas vrai : s'il y a n cartes, il y a n fois plus de chance d'en avoir une
                    double proba = Math.min((double) tailleMain / cptPioche, 1D);
                    probasParCartes[i].add(proba);
                    logDebug("~", "Ajout carte pioche", i, proba);
                }
            }
            
            // Ajout cartes en défausse
            
            if (cptPioche < tailleMain) {
                Cards defausseReelle = etat.defausse.duplique().ajoute(etat.main);
                logDebug("~", "defausseReelle", defausseReelle);
                
                int cptDefausse = defausseReelle.getCount();
                logDebug("~", "cptDefausse", cptDefausse);
                int cptDefaussePrise = tailleMain - cptPioche;
                logDebug("~", "defausse prises", cptDefaussePrise);
                
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < defausseReelle.c[i]; j++) {
                        // TODO : pas vrai : s'il y a 2 cartes, il y a 2 fois plus de chance d'en avoir une
                        probasParCartes[i].add((double) cptDefaussePrise / cptDefausse);
                        logDebug("~", "Ajout carte defausse", i);
                    }
                }
            }
        }

        // On remplit par des probas nulles
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 8; j++) {
                probasParCartes[i].add(0D);
            }
            logDebug("~", "Probas carte", i, "=", probasParCartes[i]);
        }

        // ----- Balayage des applications
        
        double meilleureProba = 0D;
        int meilleureAppli = -1;
        
        for (Application application : etat.applications) {
            logDebug("~", "---- appli", application.id);
            double meilleureProbaAppli = 0D;
            
            for (Cards cards : combinaisonsApplis.get(application.id)) {
                double proba = 0D;
                int cpt = cards.getCount();
                
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < cards.c[i]; j++) {
                        proba += probasParCartes[i].get(j) / cpt; 
                    }
                }
                logDebug("~", "combi", cards, "proba", proba);
                meilleureProbaAppli = Math.max(meilleureProbaAppli, proba);
            }

            logDebug("~", "meilleur proba de l'appli", meilleureProbaAppli);
            
            if (meilleureProbaAppli > meilleureProba) {
                meilleureProba = meilleureProbaAppli;
                meilleureAppli = application.id;
            }
            
        }
        
        logDebug("~", "meilleure proba : appli", meilleureAppli, "proba", meilleureProba);
        
        Ordre ordreSelect = new Ordre();
        ordreSelect.appliSuivId = meilleureAppli;
        ordreSelect.probaAppliSuiv = meilleureProba;
            
        return ordreSelect;
    }

    @Deprecated
    private void calculScoreBaseProba(Etat etat, Ordre ordre) {
        
        if (ordre.dette < INFAISABLE && detteAcceptable(etat, ordre.dette)) {
            ordre.score += 1_000;            
            ordre.score += 1_000 - ordre.dette * 100;
            logDebug("=", "faisable", ordre.score);
        } else {
            Ordre ordreAvecProba = calculProbaApplicationSuivante(etat);
            ordre.appliSuivId = ordreAvecProba.appliSuivId;
            ordre.probaAppliSuiv = ordreAvecProba.probaAppliSuiv;
            ordre.score += ordre.probaAppliSuiv * 100;
        }
                
    }

    /* Codingame common */
    void setTimeOut(long newValue) {
        TIME_OUT = newValue;
    }
    
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