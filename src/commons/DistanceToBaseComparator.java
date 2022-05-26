package commons;

import java.util.Comparator;

class DistanceToBaseComparator implements Comparator<Coords> {
    public static final int ASC = 1;
    public static final int DESC = -1;
    
    Coords base;
    int order;
       
    public DistanceToBaseComparator(Coords base, int order) {
        super();
        this.base = base;
        this.order = order;
    }

    @Override
    public int compare(Coords arg0, Coords arg1) {
        return arg0.distance2(base) < arg1.distance2(base) ? -1 * order : 1 * order;
    }

}