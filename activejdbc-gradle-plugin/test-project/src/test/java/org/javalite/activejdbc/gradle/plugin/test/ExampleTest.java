package org.javalite.activejdbc.gradle.plugin.test;

import org.javalite.activejdbc.Base;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.SafeVarargs;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ExampleTest {

    @Test
    public void testOk() {
        Base.open("org.h2.Driver", "jdbc:h2:file:./build/testdb/db", null, null);
        for (String name : Arrays.asList("foo", "bar", "baz")) {
            Example e = new Example();
            e.set("name", name);
            e.saveIt();
        }
        assertEquals(3, Example.findAll().size());
        Base.close();
    }

}
