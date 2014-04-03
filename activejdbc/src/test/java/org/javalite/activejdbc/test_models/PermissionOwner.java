package org.javalite.activejdbc.test_models;

/**
* Created by igor on 4/3/14.
*/
public interface PermissionOwner extends Comparable<PermissionOwner> {
    public Integer getId();
    public String getName();
    public PermissionType getType();
    public String getPrefix();
    public String getSuffix();
    public String getDisplayString();
}
