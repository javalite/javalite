package org.javalite.activejdbc.test_models;

/**
* Created by igor on 4/3/14.
*/
public enum PermissionType {

    PLAYER(1, "Player"),
    GROUP(2, "Group"),
    REGION(3, "Region"),
    WORLD(4, "World"),
    SERVER(5, "Server"),
    /*LADDER(8, "Ladder"),*/
    CONSOLE(9, "Console");

    private final Integer type;
    private final String displayName;

    PermissionType(Integer type, String displayName) {
        this.type = type;
        this.displayName = displayName;
    }

    public static PermissionType getPermissionTypeForId(Integer type) {
        for (PermissionType pt : PermissionType.values()) {
            if(pt.getType() == type) {
                return pt;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
