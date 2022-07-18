package ordije;

import java.util.LinkedList;
import java.util.List;

class Echiquier {

    char[] table = new char[64];
    char couleur;
    String roques;
    String enPassant;
    int demiCoupsLimite;
    int nbCoups;
    
    public Echiquier(char[] table, char couleur, String roques, String enPassant, int demiCoupsLimite, int nbCoups) {
        super();
        this.table = table;
        this.couleur = couleur;
        this.roques = roques;
        this.enPassant = enPassant;
        this.demiCoupsLimite = demiCoupsLimite;
        this.nbCoups = nbCoups;
    }
    
    void jouer(Coup coup) {
        
        coup.pieceAv = table[coup.to];
        coup.pieceMvt = table[coup.from];
        
        if (coup.promo == Coup.Promo.NON) table[coup.to] = table[coup.from];
        else if (coup.promo == Coup.Promo.Q)
            if (couleur == 'w') table[coup.to] = 'Q'; else table[coup.to] = 'q';
        else if (coup.promo == Coup.Promo.N)
            if (couleur == 'w') table[coup.to] = 'N'; else table[coup.to] = 'n';
        else if (coup.promo == Coup.Promo.R)
            if (couleur == 'w') table[coup.to] = 'R'; else table[coup.to] = 'r';
        else if (coup.promo == Coup.Promo.B)
            if (couleur == 'w') table[coup.to] = 'B'; else table[coup.to] = 'b';

        table[coup.from] = ' ';
        inverseJoueur();
        nbCoups++;
        
    }
    
    void dejouer(Coup coup) {
        
        table[coup.from] = coup.pieceMvt;
        table[coup.to] = coup.pieceAv;
        inverseJoueur();
        nbCoups--;
        
    }
    
    /**
     * Parsing d'un code FEN
     */
    
    static Echiquier ofFen(String fen) {
        
        String[] splits = fen.split(" ");
        
        char[] table = new char[64];
        for (byte k=0; k<64; k++) table[k] = ' ';
        
        String positions = splits[0];
        byte k = 0;
        for (byte i=0; i<positions.length(); i++) {
            if (positions.charAt(i) >= '1' && positions.charAt(i) <= '9') {
                k += positions.charAt(i) - '0';
            } else if (positions.charAt(i) == '/') {
                /* rien */
            } else {
                table[k] = positions.charAt(i);
                k++;
            }
        }
        
        return new Echiquier(table, splits[1].charAt(0), splits[2], splits[3], Integer.parseInt(splits[4]), Integer.parseInt(splits[5]));
    }
    
    String getFen1() {
        
        StringBuilder out = new StringBuilder();
        
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                char piece = table[l * 8 + c];
                if (piece == ' ') piece = '1';
                out.append(piece);
            }
            out.append("/");
        }
        
        String result = out.toString();
        
        result = result.replace("11111111", "8");
        result = result.replace("1111111", "7");
        result = result.replace("111111", "6");
        result = result.replace("11111", "5");
        result = result.replace("1111", "4");
        result = result.replace("111", "3");
        result = result.replace("11", "2");
                
        return result;
    }
    
    /**
     * Liste des coups possibles
     */
    
    private static final int[] vecteursTour = new int[] {-10, 10, -1, 1};
    private static final int[] vecteursFou = new int[] {-11, -9, 11, 9};
    private static final int[] deplCavalier = new int[] {-12, -21, -19, -8, 12, 21, 19, 8};
    private static final int[] deplRoi = new int[] {-11, -10, -9, -1, 1, 11, 10, 9};
   
    List<Coup> listeCoups() {
        // Tableau des prises possibles
        char[] prises = new char[64];
        for (int k=0; k<64; k++) {
            if (table[k] > 'A' && table[k] < 'Z') prises[k] = 'b';
            else if (table[k] > 'a' && table[k] < 'z') prises[k] = 'w';
            else prises[k] = ' ';
        }
         
        List<Coup> coups = new LinkedList<>();
        for (int k=0; k<64; k++) {
            if (couleur == 'b' && table[k] == 'p') {
                mouvementPion(k, 1, coups, prises);
            }
            if (couleur == 'w' && table[k] == 'P') {
                mouvementPion(k, -1, coups, prises);
            }
            if ((couleur == 'b' && (table[k] == 'r' || table[k] == 'q'))
             || (couleur == 'w' && (table[k] == 'R' || table[k] == 'Q'))) {
                mouvementParVecteur(k, vecteursTour, coups, prises);
            }
            if ((couleur == 'b' && (table[k] == 'b' || table[k] == 'q'))
             || (couleur == 'w' && (table[k] == 'B' || table[k] == 'Q'))) {
                mouvementParVecteur(k, vecteursFou, coups, prises);
            }
            if ((couleur == 'b' && table[k] == 'n')
             || (couleur == 'w' && table[k] == 'N')) {
                mouvementSurCases(k, deplCavalier, coups, prises);
            }
            if ((couleur == 'b' && table[k] == 'k')
             || (couleur == 'w' && table[k] == 'K')) {
                mouvementSurCases(k, deplRoi, coups, prises);
            }
       }
        return coups;
    }
    
    private void mouvementPion(int k, int sens, List<Coup> coups, char[] prises) {
        int cible = cible(k, sens*10);
        if (cible > -1 && table[cible] == ' ')
            ajoutCoupPionAvecPromotions(k, cible, Coup.Special.NON, coups);
        
        cible = cible(k, sens*9);
        if (cible > -1 && prises[cible] == couleur)
            ajoutCoupPionAvecPromotions(k, cible, Coup.Special.PRISE, coups);
        
        cible = cible(k, sens*11);
        if (cible > -1 && prises[cible] == couleur)
            ajoutCoupPionAvecPromotions(k, cible, Coup.Special.PRISE, coups);
        
        if (((sens == 1 && k >= 8 && k <= 15) || (sens == -1 && k >= 48 && k <= 55))) {
            
            if (table[k+sens*8] == ' ' && table[k+sens*16] == ' ') {
                ajoutCoupPionAvecPromotions(k, k+sens*16, Coup.Special.NON, coups);
            }
            
        }
    }
    
    private void ajoutCoupPionAvecPromotions(int k, int cible, Coup.Special spec, List<Coup> coups) {
        if ((cible >= 0 && cible <= 7) || (cible >= 56 && cible <= 63)) {
            coups.add(new Coup(k, cible, spec, Coup.Promo.B));
            coups.add(new Coup(k, cible, spec, Coup.Promo.N));
            coups.add(new Coup(k, cible, spec, Coup.Promo.R));
            coups.add(new Coup(k, cible, spec, Coup.Promo.Q));
        } else {
            coups.add(new Coup(k, cible, spec, Coup.Promo.NON));
        }
        
    }
    
    private void mouvementParVecteur(int k, int[] vecteurs, List<Coup> coups, char[] prises) {

        for (int i=0; i<vecteurs.length; i++) {
            int vecteur = vecteurs[i];
            
            int cible = k;
                        
            // On boucle jusqu'à ce que la cible soit en dehors du plateau ou soit une prise.
            while (cible != -1) {
                cible = cible(cible, vecteur);
                if (cible != -1) {
                    if (table[cible] == ' ') {
                        coups.add(new Coup(k, cible));
                    } else {
                        if (prises[cible] == couleur) {
                            coups.add(new Coup(k, cible, Coup.Special.PRISE));
                        }
                        cible = -1; // On n'ira pas plus loin
                    }
                }
            }
        }
        
    }
    
    private void mouvementSurCases(int k, int[] depls, List<Coup> coups, char[] prises) {

        for (int i=0; i<depls.length; i++) {
            int depl = depls[i];
            
            int cible = cible(k, depl);
            if (cible != -1) {
                if (table[cible] == ' ') {
                    coups.add(new Coup(k, cible));
                } else if (prises[cible] == couleur) {
                    coups.add(new Coup(k, cible, Coup.Special.PRISE));
                }
            }
        }
        
    }
   
    private int[] tab120 = new int[] {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1,  0,  1,  2,  3,  4,  5,  6,  7, -1,
            -1,  8,  9, 10, 11, 12, 13, 14, 15, -1,
            -1, 16, 17, 18, 19, 20, 21, 22, 23, -1,
            -1, 24, 25, 26, 27, 28, 29, 30, 31, -1,
            -1, 32, 33, 34, 35, 36, 37, 38, 39, -1,
            -1, 40, 41, 42, 43, 44, 45, 46, 47, -1,
            -1, 48, 49, 50, 51, 52, 53, 54, 55, -1,
            -1, 56, 57, 58, 59, 60, 61, 62, 63, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };
    
    private int[] tab64 = new int[] {
            21, 22, 23, 24, 25, 26, 27, 28,
            31, 32, 33, 34, 35, 36, 37, 38,
            41, 42, 43, 44, 45, 46, 47, 48,
            51, 52, 53, 54, 55, 56, 57, 58,
            61, 62, 63, 64, 65, 66, 67, 68,
            71, 72, 73, 74, 75, 76, 77, 78,
            81, 82, 83, 84, 85, 86, 87, 88,
            91, 92, 93, 94, 95, 96, 97, 98
    };
    
    private int cible(int k, int depl) {
        return tab120[tab64[k] + depl];
    }
    
    public void inverseJoueur() {
        if (couleur == 'b') couleur = 'w'; else couleur = 'b';
    }
    
    /**
     * Affichage de l'échiquier sur une console
     */
    
    String printTable() {
        StringBuilder out = new StringBuilder();
        
        for (byte l=0; l<8; l++) {
            out.append((8-l)+" .");
            for (byte c=0; c<8; c++) {
                out.append(table[l * 8 + c] + ".");
            }
            out.append("\n");
        }
        out.append("   a b c d e f g h");
        
        return out.toString();
    }
    
}
