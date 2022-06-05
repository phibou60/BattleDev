package ordije;

import java.util.List;

class Moteur {

    static boolean doLog = false;
    
    private Evaluation evaluation;
    private int maxProf;
    private Coup coupRetenu;
    
    private long timeoutMs;
    private long timeStart;
    private long timeMax;
    private boolean hasTimeouted;
    private long duration; 
        
    public Moteur(Evaluation evaluation, int maxProf, long timeoutMs) {
        super();
        this.evaluation = evaluation;
        this.maxProf = maxProf;
        this.timeoutMs = timeoutMs;
    }
    
    public void adaptProfondeur(Echiquier e, int[] seuils) {
        List<Coup> coups1 = e.listeCoups();
        long nbCoups1 = coups1.size();

        e.inverseJoueur();
        List<Coup> coups = e.listeCoups();
        e.inverseJoueur();
        long nbCoups2 = coups.size();

        long nbCoups = nbCoups1 * nbCoups2;
        for (int i=0; i<seuils.length; i++) {
            if (nbCoups < seuils[i]) maxProf = i;
        }

        System.err.println("nbCoup par niveau: "+nbCoups+", max prof:"+maxProf);
    }
    
    Coup meilleurCoup(Echiquier e) {
        
        timeStart = System.nanoTime();
        timeMax = timeStart + timeoutMs * 1_000_000; 
        
        alphaBeta(e, null, -100_000, +100_000, 0);
        
        duration = System.nanoTime() - timeStart;
        double tpsParEval = 0;
        if (evaluation.getCount() > 0) tpsParEval = Math.floorDiv(duration, evaluation.getCount());

        System.err.println("Duration: "+Math.floorDiv(duration, 1_000_000)+"ms" +
                ", timeout: " + hasTimeouted +
                ", nb Eval: "+evaluation.getCount() +
                ", tps par eval:"+tpsParEval+"ns") ;        
        return coupRetenu;
    } 
    
    int alphaBeta(Echiquier e, Coup coupPrec, int alpha, int beta, int prof) {
        String chevrons = "> > > > > > >".substring(0, (prof+1) * 2);
        log(chevrons, "alpha:", alpha, "beta:", beta);
        
        if (prof == maxProf) {
            int eval = evaluation.evalue(e);
            log(chevrons+" eval: "+eval);
            return eval;
        }
    
        int meilleureEval = -100_000;
        
        List<Coup> coups = e.listeCoups();
        if (coups.isEmpty()) {
            log(chevrons, "pat");
            return 40_000;
        }
        
        // Sortie immédiate si prise du roi
        for (Coup coup : coups) {
            if (e.table[coup.to] == 'k' || e.table[coup.to] == 'K') {
                log(chevrons, " echecs: ", coup.toUci());
                return 40_000;
            }
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (doLog) {
            for (int i=0; i<coups.size(); i++) {
                Coup coup = coups.get(i);
                sb.append(coup.toUci()+",");
            }
            log(chevrons+" coups: "+sb);
        }
        
        for (Coup coup : coups) {
            log(chevrons, "coup:", coup.toUci(), "/", coups.size());
            
            e.jouer(coup);
            int eval = - alphaBeta(e, coup, -beta, -alpha, prof + 1);
            log(chevrons, " eval: ", eval, "coup:", coup.toUci());
            e.dejouer(coup);
            
            if (eval > -25_000 && eval > meilleureEval) {
                log(chevrons, " meilleureEval", "coup:", coup.toUci());
                meilleureEval = eval;
                if (prof == 0) coupRetenu = coup;
            }
            
            if (eval >= beta) {
                log(chevrons, " coupe beta: ", beta, "coup:", coup.toUci());
                return meilleureEval;
            }
            
            alpha = Math.max(alpha, eval);
            
            if (System.nanoTime() > timeMax) {
                log(chevrons, "timeout");
                hasTimeouted = true;
                return meilleureEval;
            }
        }
        
        return meilleureEval;
        
    }
    
    static void activateLog() {
        doLog = true;
    }
    
    public long getDuration() {
        return duration;
    }

    static void log(Object... objects) {
        if (doLog) {
            for (Object o : objects) {
                System.out.print(o.toString() + " ");
            }
            System.out.println();
        }
    }
    
}