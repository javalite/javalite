package org.javalite.templator;

/**
 * @author Eric Nielsen
 */
enum Op {
    eq {
        @Override boolean resultWith(Object obj1, Object obj2) {
            return obj1 == null ? obj2 == null : (obj2 == null ? false : obj1.equals(obj2));
        }
    },
    gt {
        @Override boolean resultWith(Object obj1, Object obj2) {
            return compare(obj1, obj2) > 0;
        }
    },
    gte {
        @Override boolean resultWith(Object obj1, Object obj2) {
            return compare(obj1, obj2) >= 0;
        }
    },
    lt {
        @Override boolean resultWith(Object obj1, Object obj2) {
            return compare(obj1, obj2) < 0;
        }
    },
    lte {
        @Override boolean resultWith(Object obj1, Object obj2) {
            return compare(obj1, obj2) <= 0;
        }
    },
    neq {
        @Override boolean resultWith(Object obj1, Object obj2) {
            return obj1 == null ? obj2 != null : (obj2 == null ? true : !obj1.equals(obj2));
        }
    };

    boolean resultWith(Object obj1, Object obj2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    int compare(Object obj1, Object obj2) {
        return ((Comparable) obj1).compareTo(obj2);
    }
}
