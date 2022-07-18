package ordije;

class Evaluateur {
    private static final int STRATEGIE_DEPART = 0;
    private static final int STRATEGIE_FIN = 10;
    private long count = 0;
    private long strategie = STRATEGIE_DEPART;
    
    private static int[] valPosition = new int[] {
             0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  4,  4,  4,  4,  0,  0,
             0,  2,  4,  5,  5,  4,  2,  0,
             0,  2,  4,  5,  5,  4,  2,  0,
             0,  0,  4,  4,  4,  4,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0
    };
    
    private static int[] valPosition_K = new int[] {
             0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0,
             5,  5,  5,  5,  5,  5,  5,  5
    };
    
    // Valeur des position de pions pour la fin de partie
    private static int[] valPosition_fin = new int[] {
             0,  0,  0,  0,  0,  0,  0,  0,
            25, 25, 25, 25, 25, 25, 25, 25,
            13, 13, 13, 13, 13, 13, 13, 13,
             8,  8,  8,  8,  8,  8,  8,  8,
             4,  4,  4,  4,  4,  4,  4,  4,
             1,  1,  1,  1,  1,  1,  1,  1,
             0,  0,  0,  0,  0,  0,  0,  0,
             0,  0,  0,  0,  0,  0,  0,  0
    };
   
    public Evaluateur(Echiquier echiquierDepart) {
        super();
        
        if (strategie == STRATEGIE_DEPART) {
            int ctr = 0;
            for (int i = 0; i < 64; i++) {
                if (echiquierDepart.table[i] != ' ') ctr++;
            }
            if (ctr < 16) {
                strategie = STRATEGIE_FIN;
            }
        }
        System.err.println("strategie: " + strategie);
        
    }

    int evalue(Echiquier e) {
        int result = 0;
        
        for (byte k=0; k<64; k++) {
            
            // Valeur intrasèque des pièces
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
            
            // Valeur de la position de la pièce
            
            if (strategie == STRATEGIE_DEPART) {
                if (e.table[k] == ' ') /* rien */;
                else {
                    if (e.table[k] == 'k') {
                        result -= valPosition_K[63 - k];
                    } else if (e.table[k] == 'K') {
                        result += valPosition_K[k];
                    } else {
                        if (e.table[k] < 'Z') result += valPosition[k];
                        else result -= valPosition[k];
                    }
                }
            }
            
            if (strategie == STRATEGIE_FIN) {
                if (e.table[k] == ' ') /* rien */;
                else {
                    if (e.table[k] == 'p') {
                        result -= valPosition_fin[63 - k];
                    } else if (e.table[k] == 'P') {
                        result += valPosition_fin[k];
                    }
                }
            }
            
        }
        
        if (e.couleur == 'b') result = -result;
        
        count++;
        return result;
    }

    public long getCount() {
        return count;
    }
    
}
