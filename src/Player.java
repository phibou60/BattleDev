import java.io.InputStream;
import java.util.*;

/**
 * 
 **/
class Player {
    
    static boolean doLog = true;

    public static void main(String args[]) {
        Player player = new Player();
        player.joue(System.in);
    }
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);

        int constantsCount = in.nextInt();
        System.err.println("constantsCount: "+constantsCount);
        
        for (int i = 0; i < constantsCount; i++) {
            String name = in.next();
            String value = in.next();
            System.err.println(name+"="+constantsCount);
        }

        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        System.out.println("fen");

        long sumDuration = 0;
        long sumEvaluations = 0;
        
        // game loop
        while (true) {
            String board = in.next();
            String color = in.next();
            String castling = in.next();
            String enPassant = in.next();
            int halfMoveClock = in.nextInt();
            int fullMove = in.nextInt();

            String fen = board+" "+color+" "+castling+" "+enPassant+" "+halfMoveClock+" "+fullMove;
            System.err.println(fen);

            Echiquier e = Echiquier.ofFen(fen);
            
            Evaluation evaluation = new Evaluation();
            
            Moteur moteur = new Moteur(evaluation, 3, 50);
            
            moteur.adaptProfondeur(e, new int[] {9999, 9999, 9999, 9999, 500, 300});
            
            Coup coup = moteur.meilleurCoup(e);
            System.err.println("meilleur coup: " + coup.toUci());
            
            sumDuration += moteur.getDuration();
            sumEvaluations += evaluation.getCount();
            System.err.println("tps par eval:"+Math.floorDiv(sumDuration, sumEvaluations)+"ns") ;
            
            
            System.out.println(coup.toUci());
        }
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

class Coup {
    enum Special {NON, PRISE, ROQUE_K, ROQUE_Q, PAT};
    enum Promo {NON, Q, B, R, N};
        
    int from;
    int to;
    
    //---- Informations de retour arrière
    // Mémorisation de la pièce prise
    char pieceAv;
    // Mémorisation de la pièce qui a bougé (car un pion peut devenir une Dame)
    char pieceMvt;
    // Mouvement spécial
    Special spec;
    // Promotion du pion
    Promo promo;
    
    public Coup(int from, int to) {
        super();
        this.from = from;
        this.to = to;
        this.spec = Special.NON;
        this.promo = Promo.NON;
    }
    
    public Coup(int from, int to, Special spec) {
        super();
        this.from = from;
        this.to = to;
        this.spec = spec;
        this.promo = Promo.NON;
    }
    
    public Coup(int from, int to, Special spec, Promo promo) {
        super();
        this.from = from;
        this.to = to;
        this.spec = spec;
        this.promo = promo;
    }
    
    String[] coords = new String [] {
           "a8","b8","c8","d8","e8","f8","g8","h8",
           "a7","b7","c7","d7","e7","f7","g7","h7",
           "a6","b6","c6","d6","e6","f6","g6","h6",
           "a5","b5","c5","d5","e5","f5","g5","h5",
           "a4","b4","c4","d4","e4","f4","g4","h4",
           "a3","b3","c3","d3","e3","f3","g3","h3",
           "a2","b2","c2","d2","e2","f2","g2","h2",
           "a1","b1","c1","d1","e1","f1","g1","h1"
    };
    
    String toUci() {
        String uci = coords[from]+coords[to];
        if (promo == Promo.B) uci += 'b';
        if (promo == Promo.Q) uci += 'q';
        if (promo == Promo.N) uci += 'n';
        if (promo == Promo.R) uci += 'r';
        return uci;
    }
    
}

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
         
        List<Coup> coups = new ArrayList<>();
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

class Evaluation {

    private long count = 0;
    
    private int[] valPosition = new int[] {
             0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  4,  4,  4,  4,  0,  0,
             0,  2,  4,  5,  5,  4,  2,  0,
             0,  2,  4,  5,  5,  4,  2,  0,
             0,  0,  4,  4,  4,  4,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0
    };
    
    int evalue(Echiquier e) {
        int result = 0;
        
        for (byte k=0; k<64; k++) {
            if (e.table[k] == ' ') /* rien */;
            else if (e.table[k] == 'p') result -= 100;
            else if (e.table[k] == 'n') result -= 300;
            else if (e.table[k] == 'b') result -= 300;
            else if (e.table[k] == 'r') result -= 500;
            else if (e.table[k] == 'q') result -= 1_000;
            else if (e.table[k] == 'k') result -= 30_000;
            
            else if (e.table[k] == 'P') result += 100;
            else if (e.table[k] == 'N') result += 300;
            else if (e.table[k] == 'B') result += 300;
            else if (e.table[k] == 'R') result += 500;
            else if (e.table[k] == 'Q') result += 1_000;
            else if (e.table[k] == 'K') result += 30_000;
            
            if (e.table[k] == ' ') /* rien */;
            else if (e.table[k] < 'Z') result += valPosition[k];
            else result -= valPosition[k];
        }
        
        if (e.couleur == 'b') result = -result;
        
        count++;
        return result;
    }

    public long getCount() {
        return count;
    }
    
}

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

        System.err.println("Duration: "+Math.floorDiv(duration, 1_000_000)+"ms" +
                ", timeout: " + hasTimeouted +
                ", nb Eval: "+evaluation.getCount() +
                ", tps par eval:"+Math.floorDiv(duration, evaluation.getCount())+"ns") ;
        
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
            log(chevrons+" pat");
            return 40_000;
        }
        
        // Sortie immédiate si prise du roi
        for (Coup coup : coups) {
            if (e.table[coup.to] == 'k' || e.table[coup.to] == 'K') {
                log(chevrons+" echecs: "+coup.toUci());
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
            log(chevrons+" coup: "+coup.toUci(), "/", coups.size());
            
            e.jouer(coup);
            int eval = - alphaBeta(e, coup, -beta, -alpha, prof + 1);
            log(chevrons+" eval: "+eval, "coup:", coup.toUci());
            e.dejouer(coup);
            
            if (eval > meilleureEval) {
                log(chevrons+" meilleureEval", "coup:", coup.toUci());
                meilleureEval = eval;
                if (prof == 0) coupRetenu = coup;
            }
            
            if (eval >= beta) {
                log(chevrons+" coupe beta: "+beta, "coup:", coup.toUci());
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
