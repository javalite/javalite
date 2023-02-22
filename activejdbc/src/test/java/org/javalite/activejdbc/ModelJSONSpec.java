package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.javalite.json.JSONHelper;
import org.junit.Test;

import java.util.Objects;

public class ModelJSONSpec extends ActiveJDBCTest {

    @Test
    public void serializeAndDeserializeModel() {
        Person p = Person.findOrCreateIt("name", "yakka", "last_name", "newbie", "dob", getDate(1990, 8, 3));
        var json = JSONHelper.toJSON(p, true);
        Person ppp = new Person().fromMap(JSONHelper.toMap(JSONHelper.toJSON(p.toMap(), true)));
        ppp.saveIt();
        Person pppp = Person.findById(p.getId());
        Person pp = JSONHelper.toObject(json, Person.class);
        pp.refresh();
        a(ppp.getId()).shouldBeEqual(pp.getId());
        a(ppp.get("updated_at")).shouldBeEqual(pp.get("updated_at"));
        a(ppp.get("created_at")).shouldBeEqual(pp.get("created_at"));
        a(ppp.get("dob")).shouldBeEqual(pp.get("dob"));
        a(ppp.get("name")).shouldBeEqual(pp.get("name"));
    }

}
