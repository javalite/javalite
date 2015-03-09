package org.javalite.templator;

import java.util.Map;

/**
 * @author Eric Nielsen
 */
class Comparison extends Exp {
    enum Operator {
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
        },
        invalid {
            @Override boolean resultWith(Object obj1, Object obj2) {
                throw new UnsupportedOperationException();
            }

        };

        abstract boolean resultWith(Object obj1, Object obj2);

        int compare(Object obj1, Object obj2) {
            return ((Comparable) obj1).compareTo(obj2);
        }
    }

    private final ChainedIds leftOperand;
    private final Operator operator;
    private final ChainedIds rightOperand;

    Comparison(ChainedIds leftOperand, Operator operator, ChainedIds rightOperand) {
        this.leftOperand = leftOperand;
        this.operator = operator;
        this.rightOperand = rightOperand;
    }

    @Override
    boolean resultWith(Map values) {
        return operator.resultWith(leftOperand.valueFrom(values), rightOperand.valueFrom(values));
    }
}
