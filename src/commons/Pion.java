package commons;

/**
 * Personnage du jeu.
 * Ce personne est supposé avancé en ligne droite et à la même vitesse.
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
		// vérifier que le monstre ne sort pas de la carte avant d'entrer dans le puits de gravité 
		Line thisLine = getLine();
    	Line orthogonalLine = thisLine.getLineOrthogonale(base);
    	Coords inters = thisLine.getIntersection(orthogonalLine);
    	//log(" >>> inters: "+inters);
    	return inters;
	}
	
	boolean willFallIntoBase(Coords base) {
		try {
			Coords inters = getProjeteOrthogonal(base);
			double distance = base.distance(inters);
			boolean ret = (distance < 5_050D && seRapprocheDe(inters));
			//log(" >>> distance: "+distance+", will fall: "+ret);
			return ret;
		} catch (Exception e) {
			//log("Exception: "+e);
			return true;
		}
	}
	
	Line getLine() {
		int a = v.y;
		int b = -v.x;
		int c = -( a * x + b * y);
		return new Line(a, b, c);
		
	}
	
	Coords nextCoords() {
		return v.doVtranslation(this);
	}
	   
}
