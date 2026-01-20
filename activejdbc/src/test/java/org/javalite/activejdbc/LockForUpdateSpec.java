package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Person;
import org.javalite.test.SystemStreamUtil;
import org.junit.Test;

public class LockForUpdateSpec extends ActiveJDBCTest {

    @Test
    public void shouldGenerateLockForUpdateQuery() {
        SystemStreamUtil.replaceOut();

        deleteAndPopulateTable("people");
        Person p = Person.<Person>findAll().get(0);
        Person p2 = Person.findById(p.getId(), true); // deprecated API - defaults to FOR_UPDATE (with WAIT)
        the(p2.getId()).shouldBeEqual(p.getId());
        
        if (Base.connection().getClass().getName().contains("sqlserver")) {
            the(SystemStreamUtil.getSystemOut()).shouldContain("SELECT TOP 1 * FROM people WITH (UPDLOCK) WHERE id = ?");
        } else if (Base.connection().getClass().getName().contains("oracle")) {
            the(SystemStreamUtil.getSystemOut())
                    .shouldContain("SELECT * FROM (SELECT t2.* FROM (SELECT * FROM people WHERE id = ?) t2) WHERE ROWNUM <= 1 FOR UPDATE");
        } else if (Base.connection().getClass().getName().contains("mysql")
                || Base.connection().getClass().getName().contains("mariadb")
                || Base.connection().getClass().getName().contains("h2")
        ) {
            the(SystemStreamUtil.getSystemOut()).shouldContain("SELECT * FROM people WHERE id = ? LIMIT 1 FOR UPDATE");
        }

        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void shouldGenerateLockForUpdateWithWait() {
        SystemStreamUtil.replaceOut();

        deleteAndPopulateTable("people");
        Person p = Person.<Person>findAll().get(0);
        Person p2 = Person.findById(p.getId(), LockMode.FOR_UPDATE);
        the(p2.getId()).shouldBeEqual(p.getId());

        String output = SystemStreamUtil.getSystemOut();

        if (Base.connection().getClass().getName().contains("sqlserver")) {
            the(output).shouldContain("WITH (UPDLOCK)");
        } else if (Base.connection().getClass().getName().contains("oracle")) {
            the(output).shouldContain("FOR UPDATE");
            the(output).shouldNotContain("NOWAIT");
        } else if (Base.connection().getClass().getName().contains("postgres")) {
            the(output).shouldContain("FOR UPDATE");
            the(output).shouldNotContain("NOWAIT");
        } else if (Base.connection().getClass().getName().contains("mysql")
                || Base.connection().getClass().getName().contains("mariadb")
                || Base.connection().getClass().getName().contains("h2")
        ) {
            the(output).shouldContain("FOR UPDATE");
            the(output).shouldNotContain("NOWAIT");
        }

        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void shouldGenerateLockForUpdateNoWait() {
        SystemStreamUtil.replaceOut();

        deleteAndPopulateTable("people");
        Person p = Person.<Person>findAll().get(0);
        Person p2 = Person.findById(p.getId(), LockMode.FOR_UPDATE_NOWAIT);
        the(p2.getId()).shouldBeEqual(p.getId());

        String output = SystemStreamUtil.getSystemOut();

        if (Base.connection().getClass().getName().contains("sqlserver")) {
            // SQL Server doesn't distinguish between FOR_UPDATE and FOR_UPDATE_NOWAIT
            the(output).shouldContain("WITH (UPDLOCK)");
        } else if (Base.connection().getClass().getName().contains("oracle")) {
            the(output).shouldContain("FOR UPDATE NOWAIT");
        } else if (Base.connection().getClass().getName().contains("postgres")) {
            the(output).shouldContain("FOR UPDATE NOWAIT");
        } else if (Base.connection().getClass().getName().contains("mysql")
                || Base.connection().getClass().getName().contains("mariadb")
        ) {
            the(output).shouldContain("FOR UPDATE NOWAIT");
        } else if (Base.connection().getClass().getName().contains("h2")) {
            // H2 doesn't support NOWAIT, just FOR UPDATE
            the(output).shouldContain("FOR UPDATE");
        }

        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void shouldGenerateLockForUpdateSkipLocked() {
        SystemStreamUtil.replaceOut();

        deleteAndPopulateTable("people");
        Person p = Person.<Person>findAll().get(0);

        String output;

        if (Base.connection().getClass().getName().contains("h2")) {
            // H2 doesn't support SKIP LOCKED, should throw exception
            try {
                Person.findById(p.getId(), LockMode.FOR_UPDATE_SKIP_LOCKED);
                the(true).shouldBeFalse(); // Should not reach here
            } catch (UnsupportedOperationException e) {
                the(e.getMessage()).shouldContain("FOR_UPDATE_SKIP_LOCKED");
            }
        } else {
            Person p2 = Person.findById(p.getId(), LockMode.FOR_UPDATE_SKIP_LOCKED);
            the(p2.getId()).shouldBeEqual(p.getId());

            output = SystemStreamUtil.getSystemOut();

            if (Base.connection().getClass().getName().contains("sqlserver")) {
                the(output).shouldContain("WITH (UPDLOCK, READPAST)");
            } else if (Base.connection().getClass().getName().contains("oracle")) {
                the(output).shouldContain("FOR UPDATE SKIP LOCKED");
            } else if (Base.connection().getClass().getName().contains("postgres")) {
                the(output).shouldContain("FOR UPDATE SKIP LOCKED");
            } else if (Base.connection().getClass().getName().contains("mysql")
                    || Base.connection().getClass().getName().contains("mariadb")
            ) {
                the(output).shouldContain("FOR UPDATE SKIP LOCKED");
            }
        }

        SystemStreamUtil.restoreSystemOut();
    }

    @Test
    public void shouldUseLockModeWithLazyList() {
        SystemStreamUtil.replaceOut();

        deleteAndPopulateTable("people");

        Person p = (Person) Person.where("id > ?", 0)
                .lockMode(LockMode.FOR_UPDATE_NOWAIT)
                .limit(1)
                .get(0);

        the(p).shouldNotBeNull();

        String output = SystemStreamUtil.getSystemOut();

        if (Base.connection().getClass().getName().contains("postgres")) {
            the(output).shouldContain("FOR UPDATE NOWAIT");
        } else if (Base.connection().getClass().getName().contains("mysql")
                || Base.connection().getClass().getName().contains("mariadb")
        ) {
            the(output).shouldContain("FOR UPDATE NOWAIT");
        } else if (Base.connection().getClass().getName().contains("oracle")) {
            the(output).shouldContain("FOR UPDATE NOWAIT");
        }

        SystemStreamUtil.restoreSystemOut();
    }
}
