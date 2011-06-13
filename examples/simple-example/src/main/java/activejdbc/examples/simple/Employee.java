package activejdbc.examples.simple;

import activejdbc.Model;

public class Employee extends Model {
    static{
        validatePresenceOf("first_name", "last_name");
    }
}
