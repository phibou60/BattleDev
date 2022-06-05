package ordije;

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
