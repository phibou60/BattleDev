package commons;

/**
 * Personnage du jeu.
 * Ce personne est suppos? avanc? en ligne droite et ? la m?me vitesse.
 * Il faudra ajouter les autres informations selon les jeux.
 *
 */

public class Pion extends Coords {
    Coords v = new Coords();
    
    //Monsters fields
    double distanceToTarget;
    
    boolean seRapprocheDe(Coords cible) {
        boolean ret = (Math.abs(x + v.x - cible.x) <= Math.abs(x - cible.x)
                && Math.abs(y + v.y - cible.y) <= Math.abs(y - cible.y));
        //log(" >>> se raproche de "+cible+": "+ret);
        return ret;
    }
    
    Coords getProjeteOrthogonal(Coords base) {
        // Pb (cas limitte) :
        // v?rifier que le monstre ne sort pas de la carte avant d'entrer dans le puits de gravit? 
        Droite thisLine = getLine();
        Droite orthogonalLine = thisLine.getLineOrthogonale(base);
        Coords inters = thisLine.getIntersection(orthogonalLine);
        //log(" >>> inters: "+inters);
        return inters;
    }
    
    boolean willFallIntoBase(Coords base) {
        Coords inters = getProjeteOrthogonal(base);
        double distance = base.distance(inters);
        boolean ret = (distance < 5_050D && seRapprocheDe(inters));
        //log(" >>> distance: "+distance+", will fall: "+ret);
        return ret;
    }
    
    Droite getLine() {
        double a = v.y;
        double b = -v.x;
        double c = -( a * x + b * y);
        return new Droite(a, b, c);
        
    }
    
    Coords nextCoords() {
        return v.doVtranslation(this);
    }
       
}
