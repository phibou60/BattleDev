package commons;

import java.util.Comparator;

public class DistanceToBaseComparator implements Comparator<Coords> {
    int ASC = 1;
    int DESC = -1;
    
    Coords base;
    int order;
       
    public DistanceToBaseComparator(Coords base, int order) {
        super();
        this.base = base;
        this.order = order;
    }

    @Override
    public int compare(Coords arg0, Coords arg1) {
        return arg0.distance(base) < arg1.distance(base) ? -1 * order : 1 * order;
    }

}
