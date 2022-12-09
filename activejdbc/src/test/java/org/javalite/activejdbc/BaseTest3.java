package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Animal;
import org.javalite.test.jspec.ExceptionExpectation;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;


public class BaseTest3 extends ActiveJDBCTest {

    @Before
    public void before1() {
        Base.resetAutoCommit();
        deleteAndPopulateTable("animals");
    }

    @Test
    public void shouldBeginAndCommitTransactionSilently() {

        Long countBefore = Animal.count();

        AtomicInteger exceptionCounter = new AtomicInteger(0);
        Base.doInTransactionSilently(
            ()->{
                Animal a = new Animal();
                a.set("animal_name","BULL");
                a.saveIt();
            },
            ex->exceptionCounter.incrementAndGet()
        );

        the(Animal.count()).shouldEqual(countBefore+1);
        the(exceptionCounter.get()).shouldBeEqual(0);
    }

    @Test
    public void shouldBeginAndRollbackTransactionSilently() {

        Long countBefore = Animal.count();

        AtomicInteger exceptionCounter = new AtomicInteger(0);
        Base.doInTransactionSilently(
            ()->{
                Animal a = new Animal();
                a.set("animal_name","BULL");
                a.saveIt();
                Base.findAll("SYNTAX ERROR QUERY THAT CAUSES ROLLBACK");
            },
            ex->exceptionCounter.incrementAndGet()
        );

        the(Animal.count()).shouldEqual(countBefore);
        the(exceptionCounter.get()).shouldBeEqual(1);
    }

    @Test
    public void shouldBeginAndCommitTransactionSilentlyWithResult() {
        final Integer WRONG_VALUE = -100;
        final String NEW_ANIMAL_NAME = "Bull";

        Long countBefore = Animal.count();

        AtomicInteger exceptionCounter = new AtomicInteger(0);
        Object resultId = Base.doInTransactionSilently(
            ()->{
                Animal a = new Animal();
                a.set("animal_name",NEW_ANIMAL_NAME);
                a.saveIt();
                return a.getId();
            },
            ex->{
                exceptionCounter.incrementAndGet();
                return WRONG_VALUE;
            }
        );

        the(Animal.count()).shouldEqual(countBefore+1);
        the(exceptionCounter.get()).shouldBeEqual(0);
        the(resultId).shouldNotBeNull();
        the(resultId).shouldNotBeEqual(WRONG_VALUE);

        Animal newAnimal = Animal.findById(resultId);
        the(newAnimal).shouldNotBeNull();
        the(newAnimal.get("animal_name")).shouldBeEqual(NEW_ANIMAL_NAME);
    }

    @Test
    public void shouldBeginAndRollbackTransactionSilentlyWithResult() {

        final Integer WRONG_VALUE = -100;
        final String NEW_ANIMAL_NAME = "Bull";

        Long countBefore = Animal.count();

        AtomicInteger exceptionCounter = new AtomicInteger(0);
        Object resultId = Base.doInTransactionSilently(
                ()->{
                    Animal a = new Animal();
                    a.set("animal_name",NEW_ANIMAL_NAME);
                    a.saveIt();
                    Base.findAll("SYNTAX ERROR QUERY THAT CAUSES ROLLBACK");
                    return a.getId();
                },
                ex->{
                    exceptionCounter.incrementAndGet();
                    return WRONG_VALUE;
                }
        );

        the(Animal.count()).shouldEqual(countBefore);
        the(exceptionCounter.get()).shouldBeEqual(1);
        the(resultId).shouldNotBeNull();
        the(resultId).shouldBeEqual(WRONG_VALUE);
    }

    @Test
    public void shouldBeginAndCommitTransactionWithResult() throws Throwable{
        final String NEW_ANIMAL_NAME = "Bull";

        Long countBefore = Animal.count();

        AtomicInteger exceptionCounter = new AtomicInteger(0);
        AtomicInteger finallyCounter = new AtomicInteger(0);
        Object resultId = Base.doInTransaction (
            ()->{
                Animal a = new Animal();
                a.set("animal_name",NEW_ANIMAL_NAME);
                a.saveIt();
                return a.getId();
            },
            ex->exceptionCounter.incrementAndGet(),
            finallyCounter::incrementAndGet
        );

        the(Animal.count()).shouldEqual(countBefore+1);
        the(exceptionCounter.get()).shouldBeEqual(0);
        the(finallyCounter.get()).shouldBeEqual(1);
        the(resultId).shouldNotBeNull();

        Animal newAnimal = Animal.findById(resultId);
        the(newAnimal).shouldNotBeNull();
        the(newAnimal.get("animal_name")).shouldBeEqual(NEW_ANIMAL_NAME);
    }

    @Test
    public void shouldBeginAndRollbackTransactionWithResult() {

        final String NEW_ANIMAL_NAME = "Bull";

        Long countBefore = Animal.count();

        AtomicInteger exceptionCounter = new AtomicInteger(0);
        AtomicInteger finallyCounter = new AtomicInteger(0);

        expect(new ExceptionExpectation<>(DBException.class) {
            @Override
            public void exec() throws Exception {
                @SuppressWarnings("unused")
                Object ignored = Base.doInTransaction(
                    () -> {
                        Animal a = new Animal();
                        a.set("animal_name", NEW_ANIMAL_NAME);
                        a.saveIt();
                        Base.findAll("SYNTAX ERROR QUERY THAT CAUSES ROLLBACK");
                        return a.getId();
                    },
                    ex -> exceptionCounter.incrementAndGet(),
                    finallyCounter::incrementAndGet
                );
            }
        });

        the(Animal.count()).shouldEqual(countBefore);
        the(exceptionCounter.get()).shouldBeEqual(1);
        the(finallyCounter.get()).shouldBeEqual(1);
    }

}
