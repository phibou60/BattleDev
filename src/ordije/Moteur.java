package ordije;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class Moteur {
    
    private int maxProf;
    private int maxProfEchange = 8;
    
    private int hausseConsecutive = 0;
    
    // Variables utilisées pour une analyses
    private Evaluateur evaluateur;
    private Map<String, Integer> positionsDejaJoués;
    private Coup coupRetenu;
    private long timeMax;
    private boolean hasTimeouted;

    /**
     * Création du moteur d'analyse avec l'évaluateur et la profondeur max initiale.
     * La profondeur max sera recalculée automatiquement par la suite.
     */
    public Moteur(int maxProf) {
        super();
        this.maxProf = maxProf;
    }
    
    Coup meilleurCoup(Echiquier e, long timeoutMs, Evaluateur evaluateur,
            Map<String, Integer> positionsDejaJoués) {
        this.evaluateur = evaluateur;
        this.positionsDejaJoués = positionsDejaJoués;
        
        long timeStart = System.nanoTime();
        timeMax = timeStart + (timeoutMs * 950_000L); 
        hasTimeouted = false;
        
        //---- Recherche dans une bibliotheque d'ouverture
        
        Coup coupBib = BibliothequeOuverturesChess360.meilleurCoup(e);

        // On appelle le moteur même si on a un coup d'ouverture à partir de la bibiliothèque
        // car cela permet d'auto-configurer la profondeur.
        
        int nbCoupsMax = getNbCoupsMax(e);
        System.err.println("prof: " + maxProf + ", hConsec: " + hausseConsecutive + ", coupsMax: " + nbCoupsMax);
        
        alphaBeta(e, null, -100_000, +100_000, 1, /* analyseEchange */ false);
        
        //---- Stats de traitement et auto-configuration du maxProf
        
        long duration = System.nanoTime() - timeStart;
        double durationMs = duration / 1_000_000D;

        //double seuilAugmenteProf = (double) timeoutMs / nbCoupsMax;
        double seuilAugmenteProf = (double) timeoutMs / 30D;
        double seuilDiminueProf = timeoutMs / 2D;

        if (durationMs > seuilDiminueProf || hasTimeouted) {
            hausseConsecutive = 0;
            maxProf = Math.max(2, maxProf - 1);
            System.err.println("diminue prof: "+maxProf);
        } else if (durationMs < seuilAugmenteProf) {
            hausseConsecutive++;
            System.err.println("hausseConsecutive: "+hausseConsecutive);
            if (hausseConsecutive > 3) {
                maxProf++;
                System.err.println("Augmente prof: "+maxProf);
                hausseConsecutive = 0;
            }
        } else {
            hausseConsecutive = 0;
        }
        
        double tpsParEval = 0;
        if (evaluateur.getCount() > 0) tpsParEval = Math.floorDiv(duration, evaluateur.getCount());
        
        System.err.println("d: " + Math.floor(durationMs) + "ms" +
                (hasTimeouted ? ", timeout" : "") +
                ", nbEval: "+evaluateur.getCount() +
                ", tps par eval:"+tpsParEval+"ns") ;        
        
        if (coupBib != null) {
            System.err.println("Bib");
            return coupBib;
        }

        return coupRetenu;
    } 
    
    int getNbCoupsMax(Echiquier e) {
        int nbCoups1 = e.listeCoups().size();
        e.inverseJoueur();
        int nbCoups2 = e.listeCoups().size();
        e.inverseJoueur();
        System.err.println("nbCoups: "+nbCoups1+" x "+nbCoups2);
        return Math.max(nbCoups1, nbCoups2);
    }
    
    int alphaBeta(Echiquier e, Coup coupPrec, int alpha, int beta, int prof, boolean analyseEchange) {
        String chevrons = "#######################".substring(0, prof) + ":" + prof;
        logDebug(chevrons, "{ alpha:", alpha, "beta:", beta, "analyseEchange", analyseEchange);
    
        int meilleureEval = -100_000;
        Coup meilleurCoup = null;
        
        //---- Calculer la liste des coups possibles
        
        List<Coup> coups = e.listeCoups();
        if (doLogDebug) dumpListeDesCoups(chevrons, coups);
        
        //---- Sortie immédiate si prise du roi
        
        for (Coup coup : coups) {
            if (e.table[coup.to] == 'k' || e.table[coup.to] == 'K') {
                logDebug(chevrons, "} echecs: ", coup);
                return 40_000;
            }
        }
        
        if (analyseEchange) {
            coups = coups.stream().filter(coup -> coup.spec == Coup.Special.PRISE).collect(Collectors.toList());
            if (coups.isEmpty()) {
                int eval = evaluateur.evalue(e);
                logDebug(chevrons, "} fin de l'echange:", eval);
                return eval;
            }
        }

        if (prof == 1) suppressionPositionsDejaJoués(e, coups);
        
        int nbCoups = 0;
        for (Coup coup : coups) {
            
            e.jouer(coup);
            
            int eval = 0;
            boolean prise = coup.spec == Coup.Special.PRISE;
            if ((prof < maxProf || prise || analyseEchange) && prof < maxProfEchange) {
                logDebug(chevrons, "coup:", coup, "/", coups.size());
                eval = - alphaBeta(e, coup, -beta, -alpha, prof + 1, prise && prof >= maxProf);
            } else {
                eval = - evaluateur.evalue(e);
            }
            if (eval != -40_000) nbCoups++;
            
            e.dejouer(coup);
            logDebug(chevrons, "coup:", coup, "/", coups.size(), "eval:", eval);
            
            if (/* eval > -25_000 && */ eval > meilleureEval) {
                logDebug(chevrons, " meilleureEval", "coup:", coup);
                meilleureEval = eval;
                meilleurCoup = coup;
                if (prof == 1) coupRetenu = coup;
            }
            
            if (eval >= beta) {
                logDebug(chevrons, "} coupe beta: ", beta, "coup:", coup);
                return meilleureEval;
            }
            
            alpha = Math.max(alpha, eval);
            
            if (nbCoups > 1 && System.nanoTime() > timeMax) {
                logDebug(chevrons, "} timeout");
                hasTimeouted = true;
                return meilleureEval;
            }
        }

        if (nbCoups == 0) {
            logDebug(chevrons, "Pas de coups possibles : pat");
            meilleureEval = 0;
        }
        
        logDebug(chevrons, "} meilleureEval: ", meilleureEval, "meilleurCoup", meilleurCoup);
        
        return meilleureEval;
        
    }

    private void suppressionPositionsDejaJoués(Echiquier e, List<Coup> coups) {

        /*
        positionsDejaJoués.forEach((k, v) -> {
            System.err.println("Positions Deja Joués: " + k + " = " + v);
        });
        */
        
        LinkedList<Integer> ASupprimer = new LinkedList<>();
        
        for (int i = 0; i < coups.size(); i++) {
            Coup coup = coups.get(i);
            e.jouer(coup);
            String fen1 = e.getFen1();
            e.dejouer(coup);
            
            if (positionsDejaJoués.containsKey(fen1) && positionsDejaJoués.get(fen1) > 1) {
                ASupprimer.addFirst(i);
            }
        }
        
        ASupprimer.forEach(i -> {
            System.err.println("Suppression Positions Deja Joués:["+i+"] " + coups.get(i));
            coups.remove((int) i);
        });
        
    }

    private void dumpListeDesCoups(String chevrons, List<Coup> coups) {
        StringBuilder sb = new StringBuilder();
        
        if (doLogDebug) {
            for (int i=0; i<coups.size(); i++) {
                sb.append(coups.get(i));
                sb.append(",");
            }
            logDebug(chevrons, "coups:", sb);
        }
    }
    
    //---------------------------------------------------------------------------------
    // Gestion de la log
    //---------------------------------------------------------------------------------
    
    static boolean doLog = true;
    static boolean doLogDebug = false;
    
    static void activateLog() {
        doLog = true;
    }
    
    static void activateLogDebug() {
        doLogDebug = true;
    }
    
    static void log(Object... objects) {
        if (doLog) {
            StringBuilder sb = new StringBuilder();
            for (Object o : objects) {
                sb.append("" + o + " ");
            }
            String logText = sb.toString();
            System.err.println("* "+logText);
        }
    }
     
    static void logDebug(Object... objects) {
        if (doLogDebug) {
            StringBuilder sb = new StringBuilder();
            for (Object o : objects) {
                sb.append("" + o + " ");
            }
            String logText = sb.toString();
            System.err.println("* "+logText);
        }
    }
    
}