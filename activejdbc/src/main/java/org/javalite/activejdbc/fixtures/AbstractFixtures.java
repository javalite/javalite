/*
Copyright (c) 2012 Evan Leonard

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.

 */

package org.javalite.activejdbc.fixtures;

import org.javalite.activejdbc.Model;
import org.javalite.test.jspec.Expectation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Evan Leonard
 *         Date: 7/8/12
 */
abstract public class AbstractFixtures<T extends AbstractFixtures> {
    private static ThreadLocal<AbstractFixtures> current_fixtures = new ThreadLocal<AbstractFixtures>();
    private Map<String, Model> fixtureMap = new HashMap<String, Model>();
    private Model last_fixture, last_parent, last_child;

    //-- Public Static interface

    public static <T extends AbstractFixtures> T given(Class<T> fixture_type)
    {
        final T fixture;
        try {
            fixture = fixture_type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("There was an error trying to create your fixtures instance");
        }

        fixture.private_cleanup();
        current_fixtures.set(fixture);
        return fixture;
    }

    public static <T extends AbstractFixtures> T andGiven(Class<T> fixture_type) {
        final T fixture = getCurrentFixtures();
        if(fixture == null) {
            throw new IllegalStateException("You can't extend an empty fixtures");
        }
        fixture.and();
        return fixture;
    }

    public static Model fixture(String name) {
        final AbstractFixtures fixtures = getCurrentFixtures();
        final Model model = fixtures.getFixture(name);
        if(model==null) {
            throw new IllegalArgumentException("No fixtures found with name: "+ name);
        }
        return model;
    }

    @SuppressWarnings("unchecked")
    public static <T> T fixture(String name, Class<T> fixture_type) {
        final Model fixture = fixture(name);
        boolean good = fixture_type.isAssignableFrom(fixture.getClass());
        if(!good) {
            throw new IllegalArgumentException("No fixtures found with name: "+name + "and type:" + fixture_type.getCanonicalName());
        }
        return (T) fixture;
    }

    public static Model when(String name) {
        return fixture(name);
    }

    public static Object idFor(String name) {
        return fixture(name).getId();
    }

    public static FixtureExpectation<Model> then(String name) {
        final AbstractFixtures fixtures = getCurrentFixtures();
        final Model model = fixtures.getFixture(name);
        if(model == null) {
            throw new IllegalArgumentException("No fixtures found with name: "+ name);
        }
        return new FixtureExpectation<Model>(model, fixtures);
    }

    public static Expectation then(String name, String methodName, Object... args) {
        final AbstractFixtures fixtures = getCurrentFixtures();
        final Model model = fixtures.getFixture(name);

        if(model == null) {
            throw new IllegalArgumentException("No fixtures found with name: "+ name);
        }

        final FixtureExpectation fixtureExpectation = new FixtureExpectation<Model>(model, fixtures);
        return fixtureExpectation.call(methodName, args);
    }

    //-- public instance interface

    public T with() {
        if(last_fixture == null) {
            throw new IllegalStateException("Cannot call with on an empty fixtures");
        }
        last_parent = last_fixture;
        last_child = null;
        return castThis();
    }

    public T and() {
        last_parent = last_child = last_fixture = null;
        return castThis();
    }

    public T and(String name) {
        and();
        final Model fixture = getFixture(name);
        addFixture(name, fixture);
        return castThis();
    }

    public T on() {
        return in();
    }

    public T in() {
        if(last_fixture == null) {
            throw new IllegalStateException("Cannot call in on an empty fixtures");
        }
        last_parent = null;
        last_child = last_fixture;
        return castThis();
    }

    //-- private

    @SuppressWarnings("unchecked")
    private static <T extends AbstractFixtures> T getCurrentFixtures() {
        return (T) current_fixtures.get();
    }

    private static boolean hasCurrentFixtures() {
        return current_fixtures.get() != null;
    }

    private void private_cleanup() {
//        cleanup_database();
        current_fixtures.set(null);
        cleanup_fixtures();
    }

//    @SuppressWarnings("unchecked")
//    private void cleanup_database() {
//        if(!hasCurrentFixtures()) {
//            return;
//        }
//
//        final Collection<Model> values = getCurrentFixtures().fixtureMap.values();
//        final Set<Class> deletedClasses = new HashSet<Class>(values.size());
//        for(Model m: values) {
//            try {
//                final Class<? extends Model> c = m.getClass();
//                if(!deletedClasses.contains(c)) {
//                    final Method deleteAll = c.getMethod("deleteAll");
//                    deleteAll.invoke(m);
//                    deletedClasses.add(c);
//                }
//            } catch(Exception e) {
//                throw new RuntimeException("Failed to cleanup database", e);
//            }
//        }
//    }

    @SuppressWarnings("unchecked")
    private T castThis() {
        return (T) this;
    }


    //-- protected instance

    /**
     * Override this to do any additional cleanup necessary.
     * Models will automatically be deleted.
     */
    abstract void cleanup_fixtures();

    protected Model getFixture(String name) {
        return fixtureMap.get(name);
    }

    protected void addFixture(String name, Model m) {
        fixtureMap.put(name, m);
        last_fixture = m;
        if(last_parent != null) {
            last_parent.add(m);
        }
        if(last_child != null) {
            m.add(last_child);
        }
    }

    protected T addFixture(String name, FixtureFactory b) {
        Model m = getFixture(name);
        if(m == null) {
            m = b.newFixture();
        }
        addFixture(name, m);
        return castThis();
    }

}
