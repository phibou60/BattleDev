package codingame.marslander;

import java.io.InputStream;
import java.util.*;

/**
 * Version qui stabilise la vitesse.
 **/
class Player1a {
    static boolean doLog = false;
    
    static final double g = -3.711;
    int X;
    int Y;
    int HS;
    int VS;
    int F;
    int R;
    int P;
    
    // Result

    long newThrust;
    
    public static void main(String args[]) {
        Player1a player = new Player1a();
        player.joue(System.in);
    }
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);
        
        int N = in.nextInt(); // the number of points used to draw the surface of Mars.
        System.err.format("%s%n", N);
        
        for (int i = 0; i < N; i++) {
            int landX = in.nextInt(); // X coordinate of a surface point. (0 to 6999)
            int landY = in.nextInt(); // Y coordinate of a surface point. By linking all the points together in a sequential fashion, you form the surface of Mars.
            System.err.format("%s %s%n", landX, landY);
        }
        
        while (true) {
            X = in.nextInt();
            Y = in.nextInt();
            HS = in.nextInt(); // the horizontal speed (in m/s), can be negative.
            VS = in.nextInt(); // the vertical speed (in m/s), can be negative.
            F = in.nextInt(); // the quantity of remaining fuel in liters.
            R = in.nextInt(); // the rotation angle in degrees (-90 to 90).
            P = in.nextInt(); // the thrust power (0 to 4).
            System.err.format("%s %s %s %s %s %s %s%n", X, Y, HS, VS, F, R, P);
            
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");
            strategie();
            
            // R P. R is the desired rotation angle. P is the desired thrust power.
            System.out.println("0 "+newThrust);
        }
        
    }
    
    private void strategie() {
        EquationHoraire deplX = new EquationHoraire(Y, VS, g);
        System.err.format("next Y= %s, Vitesse= %s%n",
                deplX.getPositionEnTemps(1),
                deplX.getVitesseEnTemps(1));
        
        double a = deplX.quelAccellerationPourVitesse(-39, 4);
        setNewThrust(a - g);   
    }

    void setNewThrust(double a) {
        System.err.format("setNewThrust %s%n", a);
        newThrust = Math.round(a);

        newThrust = Long.min(Long.max(newThrust, P-1), P+1);
        newThrust = Long.min(Long.max(newThrust, 0), 4);
        System.err.format("newThrust= %s%n", newThrust);
    }
    
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
                
    }
}