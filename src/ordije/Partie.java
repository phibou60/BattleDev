package ordije;

import java.util.*;

class Partie {

    Map<String, Integer> positionsDejaJoués = new HashMap<>();
    Moteur moteur;
    
    static int timeOut = 50;
    
    Partie() {
        reset();
    }
    
    String analyseFen(String fen) {

        Echiquier e = Echiquier.ofFen(fen);
        
        Evaluateur evaluateur = new Evaluateur(e);
    
        Coup coup = moteur.meilleurCoup(e, timeOut, evaluateur, positionsDejaJoués);

        ajoutPosition(e, coup);
        
        return coup.toUci();
    }
    
    Partie reset() {
        positionsDejaJoués = new HashMap<>();
        moteur = new Moteur(2);
        return this;
    }
    
    void ajoutPosition(Echiquier e, Coup coup) {
        
        e.jouer(coup);
        String fen1 = e.getFen1();
                
        if (positionsDejaJoués.get(fen1) == null) {
            positionsDejaJoués.put(fen1, 1);
        } else {
            int nb = positionsDejaJoués.get(fen1);
            positionsDejaJoués.put(fen1, nb + 1);
        }
        //System.err.println("ajoutPosition: " + fen1 + " = " + positionsDejaJoués.get(fen1));

    }
    
}
