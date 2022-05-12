package commons;

/**
 * Classe de gestion des opérations sur les angles qui maintient l'angle entre -180 et +180.
 * @author philippe b
 *
 */
public class AngleEnDegres {

    public double angle;

    public AngleEnDegres(double angle) {
        this.angle = angle;
        normalize();
    }

    public AngleEnDegres() {
        this.angle = 0;
    }

    /* Constructeur a partir de radian */
    public AngleEnDegres ofRadian(double radian) {
        double temp = (radian / (2 * Math.PI)) * 360; 
        return new AngleEnDegres(temp);
    }
    
    public double toRadian() {
        return (angle / 360) * (2 * Math.PI); 
    }

    int getIntAngle() {
        return new Double(angle).intValue();
    }
    
    
    @Override
    public AngleEnDegres clone() {
        return new AngleEnDegres(angle);
    }
    
    private void normalize() {
        if (angle < -180) angle = 360 + angle;
        if (angle > 180) angle = angle - 360;
    }

    public AngleEnDegres ajoute(double angle2) {
        angle += angle2;
        normalize();
        return this;
    }

    public AngleEnDegres retire(double angle2) {
        angle -= angle2;
        normalize();
        return this;
    }

    public boolean estProcheDe(double angle2, double delta) {
        AngleEnDegres temp = new AngleEnDegres(angle - angle2);
        return delta > temp.angle && temp.angle > -delta;
    }

    @Override
    public String toString() {
        return String.format("%.2f", angle);
    }
    
}
