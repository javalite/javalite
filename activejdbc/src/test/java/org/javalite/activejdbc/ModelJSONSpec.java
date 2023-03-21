package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Address;
import org.javalite.activejdbc.test_models.Person;
import org.javalite.activejdbc.test_models.User;
import org.javalite.json.JSONHelper;
import org.junit.Test;

import java.util.List;

public class ModelJSONSpec extends ActiveJDBCTest {

    @Test
    public void serializeAndDeserializeModel() {
        Person p = Person.findOrCreateIt("name", "yakka", "last_name", "newbie", "dob", getDate(1990, 8, 3));
        var json = JSONHelper.toPrettyJSON(p);
        Person ppp = new Person().fromMap(JSONHelper.toMap(JSONHelper.toPrettyJSON(p.toMap())));
        Person pp = JSONHelper.toObject(json, Person.class);
        a(ppp.getId()).shouldBeEqual(pp.getId());
        a(ppp.get("updated_at")).shouldBeEqual(pp.get("updated_at"));
        a(ppp.get("created_at")).shouldBeEqual(pp.get("created_at").toString());
        a(ppp.get("dob")).shouldBeEqual(pp.get("dob"));
        a(ppp.get("name")).shouldBeEqual(pp.get("name"));
        System.out.println(p.toJson(true));
    }


    @Test
    public void shouldBeAbleToIncludeParentOne2Many() {
        deleteAndPopulateTables("users", "addresses");
        List<Address> addresses = Address.where("city = ?", "Springfield").orderBy("id").include(User.class);
        //ensure that the parent is actually cached
        User u1 = addresses.get(0).parent(User.class);
        User u2 = addresses.get(0).parent(User.class);
        a(u1).shouldBeTheSameAs(u2);

        a(addresses.get(0).get("user")).shouldNotBeNull();
        User user = (User) addresses.get(0).get("user");
        a(user.get("first_name")).shouldBeEqual("Marilyn");

        user = (User)addresses.get(6).get("user");
        a(user.get("first_name")).shouldBeEqual("John");
    }

}
