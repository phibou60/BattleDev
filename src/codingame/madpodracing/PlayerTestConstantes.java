package codingame.madpodracing;

import java.util.Scanner;

public class PlayerTestConstantes {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        int ANGLE_MIN = 90;
        int ANGLE_MAX_THURST = 20;
        // game loop

        boolean boostDone = false;

        int round = 0;
        int prevX = 0;
        int prevY = 0;

        while (true) {
            int x = in.nextInt();
            int y = in.nextInt();
            int nextCheckpointX = in.nextInt(); // x position of the next check point
            int nextCheckpointY = in.nextInt(); // y position of the next check point
            int nextCheckpointDist = in.nextInt(); // distance to the next checkpoint
            int nextCheckpointAngle = in.nextInt(); // angle between your pod orientation and the direction of the next checkpoint
            int opponentX = in.nextInt();
            int opponentY = in.nextInt();
            

            int dx = Math.abs(x-prevX);
            int dy = Math.abs(y-prevY);

            double vit = Math.sqrt(dx * dx + dy * dy);

            System.err.printf("round: %d, dx: %d, dy: %d, vitesse: %f.2%n", round, dx, dy, vit);
            prevX = x;
            prevY = y;

            System.err.println(nextCheckpointX + " " + nextCheckpointY + " "  + nextCheckpointDist + " "  + nextCheckpointAngle);
            
            // Write an action using System.out.println()
            // To debug: System.err.println("Debug messages...");


            // You have to output the target position
            // followed by the power (0 <= thrust <= 100)
            // i.e.: "x y thrust"
            int thurst = 0;
            /*
            if (nextCheckpointAngle > ANGLE_MIN || nextCheckpointAngle < -ANGLE_MIN) {
                thurst = 0;        
            } else if (nextCheckpointAngle > ANGLE_MAX_THURST || nextCheckpointAngle < -ANGLE_MAX_THURST) {
                thurst = 100;        
            } else {
                if (nextCheckpointAngle == 0 && nextCheckpointDist > 4000 && !boostDone) {
                    thurst = 999;
                    boostDone = true;
                } else {
                    thurst = 100;
                }
            }
            */
            if (round < 11) thurst = 100;
            System.out.println(nextCheckpointX + " " + nextCheckpointY + " "  + thurst);
            round++;            
        }
    }
}