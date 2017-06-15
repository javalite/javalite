package org.javalite.activejdbc.statistics;

import java.util.Comparator;

/**
 * @author stas
 */
enum SortBy {
    total((o1, o2) -> compareLongs(o2.getTotal(), o1.getTotal())),
    avg((o1, o2) -> compareLongs(o2.getAvg(), o1.getAvg())),
    min((o1, o2) -> compareLongs(o2.getMin(), o1.getMin())),
    max ((o1, o2) -> compareLongs(o2.getMax(), o1.getMax())),
    count((o1, o2) -> compareLongs(o2.getCount(), o1.getCount()));

    private static int compareLongs(long v2, long v1) {
        return v2 > v1 ? 1 : (v2 == v1 ? 0 : -1);
    }

    private final Comparator<? super QueryStats> comparator;

    SortBy(Comparator<? super QueryStats> comparator) {
        this.comparator = comparator;
    }

    public Comparator<? super QueryStats> getComparator() {
        return comparator;
    }
}