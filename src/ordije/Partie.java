package ordije;

import java.util.*;

class Partie {

    Map<String, Integer> positionsDejaJou�s = new HashMap<>();
    Moteur moteur;
    
    static int timeOut = 50;
    
    Partie() {
        reset();
    }
    
    String analyseFen(String fen) {

        Echiquier e = Echiquier.ofFen(fen);
        
        Evaluateur evaluateur = new Evaluateur(e);
    
        Coup coup = moteur.meilleurCoup(e, timeOut, evaluateur, positionsDejaJou�s);

        ajoutPosition(e, coup);
        
        return coup.toUci();
    }
    
    Partie reset() {
        positionsDejaJou�s = new HashMap<>();
        moteur = new Moteur(2);
        return this;
    }
    
    void ajoutPosition(Echiquier e, Coup coup) {
        
        e.jouer(coup);
        String fen1 = e.getFen1();
                
        if (positionsDejaJou�s.get(fen1) == null) {
            positionsDejaJou�s.put(fen1, 1);
        } else {
            int nb = positionsDejaJou�s.get(fen1);
            positionsDejaJou�s.put(fen1, nb + 1);
        }
        //System.err.println("ajoutPosition: " + fen1 + " = " + positionsDejaJou�s.get(fen1));

    }
    
}
