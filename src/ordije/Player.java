package ordije;

import java.io.*;
import java.util.*;
import java.util.stream.*;

class Player {
    
    static boolean doLog = true;

    public static void main(String args[]) {
        Player player = new Player();
        player.joue(System.in);
    }
    
    void joue(InputStream inStream) {
        Scanner in = new Scanner(inStream);

        int constantsCount = in.nextInt();
        System.err.println(constantsCount);
        
        for (int i = 0; i < constantsCount; i++) {
            String name = in.next();
            String value = in.next();
            System.err.println(name+" "+value);
        }
        
        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");

        System.out.println("fen");

        long sumDuration = 0;
        long sumEvaluations = 0;
        
        Partie partie = new Partie();
        
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

            String coup = partie.analyseFen(fen);

            System.out.println(coup);
        }
    }
    
}