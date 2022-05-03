package commons;

/**
 * Classe permettant d'avoir des fonctions de gestion des points et des déplacements.
 *
 */

public class Coords {
    int MAX_X = 17_630;
    int MAX_Y = 9_000;

    int x;
    int y;

	public Coords() {}
	
	public Coords(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	double distance(Coords c2) {
    	double dist = Math.hypot(x - c2.x, y - c2.y);
    	if (this instanceof Pion) {
    		((Pion) this).distanceToTarget = dist;
    	}
    	return dist;
    }
    
    boolean dansLaCarte() {
        boolean ret = x >= 0 && x <= MAX_X && y >= 0 && y <= MAX_Y;
        return ret;
    }
    
    Interception getInterception(Pion m) {
    	return new Interception(this, m);
    }
	
	/**
	 * Pour cette instance de Coords, retourne un point opposé à la base à une certaine distance
	 * TODO : ajout d'un controle si on sort de la carte ou pas
	 * 
	 * @param base Point opposé à celui qu'on veut calculer
	 * @param distance Distance du point que l'on veut à partir d'ici
	 */
    Coords getPointOppose(Coords base, double distance) {
		Coords vector2d = createVFromPoints(base, this);  
    	return vector2d.doVtranslation(this, distance);
	}
    
    Coords getPointVers(Coords base, double distance) {
    	return getPointOppose(base, -distance);
	}
    
	@Override
	public String toString() {
		return "Coords [x=" + x + ", y=" + y + "]";
	}
   
    //---- Fonctions vectorielles (on considère ici que l'objet Coords est un vecteur)
    
	/* static */ Coords createVFromPoints(Coords from, Coords to) {
    	return new Coords(to.x - from.x, to.y - from.y);
    }
    
    /* static */ Coords createVFromFormeTrigono(double angle, double norme) {
    	return new Coords((int) Math.floor(Math.cos(angle) * norme),
    			(int) Math.floor(Math.sin(angle) * norme));
    }
    
    double getVNorme() {
    	return Math.hypot(x, y);
    }
    
    double getVAngle() {
    	return Math.atan2(y, x);
    }
    
	Coords doVtranslation(Coords from) {
    	return new Coords(from.x + x, from.y + y);
	}
    
	Coords doVtranslation(Coords from, double distance) {
		double angle = getVAngle();
    	return new Coords(from.x + (int) Math.floor(Math.cos(angle)*distance),
    			from.y + (int) Math.floor(Math.sin(angle)*distance));
	}

}
