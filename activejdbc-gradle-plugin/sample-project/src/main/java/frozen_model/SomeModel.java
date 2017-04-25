package frozen_model;

import org.javalite.activejdbc.Model;

public class SomeModel extends Model {

    // Just some random method.
    public SomeModel findByName(String name) {
        return findFirst("name = ?", name);
    }
}
