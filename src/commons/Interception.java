package commons;

/**
 * Calcule une interception optimale vers un pion qui se déplace
 * @author philippe b
 *
 */

class Interception extends Coords {
    int round;
    
    Interception(Coords h, Pion m) {
    	x = m.x + m.v.x;
        y = m.y + m.v.y;
        int maxDistanceVsMonstre = 800;
        round = 0;
        //log("I: x: "+x+", y: "+y+", maxDistanceVsMonstre: "+maxDistanceVsMonstre+", dist: "+h.distance(this));
        
        while (h.distance(this) > maxDistanceVsMonstre && dansLaCarte() && round < 99) {
            x += m.v.x;
            y += m.v.y;
            maxDistanceVsMonstre += 800;
            round++;
            //log("I: x: "+x+", y: "+y+", maxDistanceVsMonstre: "+maxDistanceVsMonstre+", dist: "+h.distance(this));
        }
        if (!dansLaCarte()) round = 99;
    }
}
