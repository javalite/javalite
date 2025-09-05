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
        Person p2 = Person.findById(p.getId(), true); // causes the "no wait" to be added to the query
        the(p2.getId()).shouldBeEqual(p.getId());

        if (Base.connection().getClass().getName().contains("sqlserver")) {
            the(SystemStreamUtil.getSystemOut()).shouldContain("SELECT TOP 1 * FROM people WITH (UPDLOCK)  WHERE id = ?");
        } else if (Base.connection().getClass().getName().contains("oracle")) {
            the(SystemStreamUtil.getSystemOut())
                    .shouldContain("SELECT * FROM (SELECT t2.* FROM (SELECT * FROM people WHERE id = ? ) t2) WHERE ROWNUM <= 1 FOR UPDATE ");
        }else if (Base.connection().getClass().getName().contains("mysql") 
                || Base.connection().getClass().getName().contains("mariadb")
                || Base.connection().getClass().getName().contains("h2")
        ) {
            the(SystemStreamUtil.getSystemOut()).shouldContain("SELECT * FROM people WHERE id = ?  LIMIT 1 FOR UPDATE");
        }

        SystemStreamUtil.restoreSystemOut();

    }
}
