package commons;

class EquationHoraire {
    double X;
    double V;
    double A;
    
    public EquationHoraire(double x, double v, double a) {
        super();
        X = x;
        V = v;
        A = a;
    }
    
    double getPositionEnTemps(double t) {
        return (A * t * t / 2) + (V * t) + X;
    }
    
    double getVitesseEnTemps(double t) {
        return (A * t) + V;
    }
    
    double quelAccellerationPourVitesse(double v, double t) {
        return (v - V) / t; 
    }
    
    double tempsPourAtteindre(double cible) {
        for (int t=1; t<=300; t++) {
            double position = getPositionEnTemps(t);
            if ((X > cible && position < cible) || (X < cible && position > cible)) {
                return t;
            }
        }
        return -1;
    }
    
    /**
     * Calcul du temps nécessaire pour ralentir.
     * @param v Vitesse cible
     * @param a Puissance de freinage maximum que l'on dispose
     * @return temps en secondes
     */
    double quelTempsPourRalentissement(double v, double a) {
        double deltaV = v - V;
        return deltaV / a;
    }
    
}
