package org.javalite.common.test;

import java.util.List;

/**
 * @author Igor Polevoy: 1/18/12 3:49 PM
 */
public class Room {

    public boolean hasWalls() {
        return true;
    }

    public Boolean hasChildren() {
        return Boolean.FALSE;
    }

    public List children() {
        return null;
    }
}
