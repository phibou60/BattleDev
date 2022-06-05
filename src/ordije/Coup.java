package ordije;

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
