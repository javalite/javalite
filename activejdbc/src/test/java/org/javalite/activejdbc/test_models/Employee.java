package org.javalite.activejdbc.test_models;

import org.javalite.activejdbc.Model;

/**
 * @author igor on 4/7/18.
 */
public class Employee extends Model {
    static {
        addScope("developers", "position = 'Developer'");
        addScope("active", "active = 1");
        addScope("IT", "department = 'IT'");
    }
}
