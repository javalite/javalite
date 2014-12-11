package org.javalite.activejdbc.statistics;

import java.util.Comparator;

/**
 * @author stas
 */
enum SortBy {
    total(new Comparator<QueryStats>() {
        @Override public int compare(QueryStats o1, QueryStats o2) {
            return compareLongs(o2.getTotal(), o1.getTotal());
        }
    }),
    avg(new Comparator<QueryStats>() {
        @Override public int compare(QueryStats o1, QueryStats o2) {
            return compareLongs(o2.getAvg(), o1.getAvg());
        }
    }),
    min(new Comparator<QueryStats>() {
        @Override public int compare(QueryStats o1, QueryStats o2) {
            return compareLongs(o2.getMin(), o1.getMin());
        }
    }),
    max (new Comparator<QueryStats>() {
        @Override public int compare(QueryStats o1, QueryStats o2) {
            return compareLongs(o2.getMax(), o1.getMax());
        }
    }),
    count(new Comparator<QueryStats>() {
        @Override public int compare(QueryStats o1, QueryStats o2) {
            return compareLongs(o2.getCount(), o1.getCount());
        }
    });

    private static int compareLongs(long v2, long v1) {
        return v2 > v1 ? 1 : (v2 == v1 ? 0 : -1);
    }

    private final Comparator<? super QueryStats> comparator;

    private SortBy(Comparator<? super QueryStats> comparator) {
        this.comparator = comparator;
    }

    public Comparator<? super QueryStats> getComparator() {
        return comparator;
    }
}