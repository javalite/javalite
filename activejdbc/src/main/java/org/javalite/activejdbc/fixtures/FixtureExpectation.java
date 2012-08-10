package org.javalite.activejdbc.fixtures;

import org.javalite.activejdbc.LazyList;
import org.javalite.activejdbc.Model;
import org.javalite.common.Util;
import org.javalite.test.jspec.Expectation;
import org.javalite.test.jspec.JSpec;
import org.javalite.test.jspec.TestException;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Some useful extensions to JSpec for working with Fixtures
 * @param <T>
 */
public class FixtureExpectation<T extends Model> extends Expectation<T> {

    T actual;
    AbstractFixtures fixtures;

    FixtureExpectation(T actual, AbstractFixtures fixtures) {
        super(actual);
        this.actual = actual;
        this.fixtures = fixtures;
    }

    public void shouldBeFixture(String name) {
        final Model expected = fixtures.getFixture(name);

        if(!modelsEqual(actual, expected)) {
            throw new TestException("Fixture were not the same. Their ids and types did not match.");
        }
    }

    public static boolean modelsEqual(Model m1, Object m2) {
        if(m1 == m2) return true;
        if(!(m2 instanceof Model)) return false;
        final boolean sameType = m1.getClass().equals(m2.getClass());
        final boolean idsEqual = compareIds(m1.getId(), ((Model) m2).getId());
        return sameType && idsEqual;
    }

    public static boolean compareIds(Object id1, Object id2) {
        return ((Number) id1).longValue() == ((Number) id2).longValue();
    }

    public <M extends Model> void shouldHave(int size, Class<M> modelClass) {
        final LazyList<M> children = actual.getAll(modelClass);
        JSpec.the(children.size()).shouldEqual(size);
    }

    public <M extends Model> FixtureExpectation<M> parent(Class<M> modelClass) {
        final M parent = actual.parent(modelClass);
        return new FixtureExpectation<M>(parent, fixtures);
    }

    public Expectation<Object> call(String methodName, Object... args) {
        Class[] classes = new Class[args.length];
        for(int i=0; i < args.length; i++) {
            classes[i] = args[i].getClass();
        }

        Object result;
        try {
            Method m = getMethod(actual, methodName, classes);

            if(m == null && isSingleString(args)) {
                final Model fixture = fixtures.getFixture((String) args[0]);
                m = tryArgAsFixtureName(actual, methodName, fixture);
                if(m != null) args = new Object[]{fixture};
            }

            if (m == null) {
                final String classNames = Util.join(Arrays.asList(classes), ",");
                throw new IllegalArgumentException("failed to find a matching method for class: "
                        + actual.getClass() + ", named: " + methodName + " with "+args.length+" arguments >>" +classNames );
            }

            result = m.invoke(actual, args);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new Expectation<Object>(result);
    }

    private static Method tryArgAsFixtureName(Model model, String methodName, Model fixture) {
        if(fixture != null) {
            return getMethod(model, methodName, new Class[]{fixture.getClass()});
        }
        return null;
    }

    private static boolean isSingleString(Object[] args) {
        return args.length == 1 && args[0] instanceof String;
    }

    private static Method getMethod(Model model, String method, Class[] classes) {
        Method m = null;

        try {m = model.getClass().getMethod(method, classes);} catch (NoSuchMethodException ignore) {}

        if(m == null) {
            try {
                m = model.getClass().getSuperclass().getMethod(method, classes);
            } catch(NoSuchMethodException ignore) {
            }
        }

        if (m == null & classes.length == 1) {
            //Try interfaces
            Class c = classes[0];
            final Class[] interfaces = c.getInterfaces();
            for(Class i : interfaces) {
                m = getMethod(model, method, new Class[]{i});
                if(m != null) break;
            }
        }

        return m;
    }
}
