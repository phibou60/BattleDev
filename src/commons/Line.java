package commons;

public class Line {
	// Sous forme d'équation cartésienne : ax + y + c = 0
	int a;
	int b;
	int c;
	
	public Line(int a, int b, int c) {
		super();
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	Line getLineOrthogonale(Coords point) {
		return new Line(b, -a, -(b * point.x + -a * point.y));
	}
	
	Coords getIntersection(Line l2) {
		Coords inters = new Coords();
		//log("Intersection: "+toString()+" vs "+l2);
		inters.y = (int) Math.round(((double) -l2.c + (l2.a * c) / a) / (((-l2.a * b) / a) + l2.b));
		inters.x = (int) Math.round(((double) -c - (b * inters.y)) / a);
		return inters;
	}
	
	@Override
	public String toString() {
		return "Line [a=" + a + ", b=" + b + ", c=" + c + "]";
	}
	
}
