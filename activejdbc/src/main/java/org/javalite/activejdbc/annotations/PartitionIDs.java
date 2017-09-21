package org.javalite.activejdbc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies columns that must be used to form an UPDATE and DELETE statements in addition to a primary key.
 * Some databases (MySQL, MariaDB) provide a <a href="https://dev.mysql.com/doc/refman/5.7/en/partitioning.html">partitioning</a>
 * feature. In order to take advantage of it, UPDATE and DELETE statements need to include columns which were used for
 * partitioning, otherwise such queries will not be efficient.
 * <p>Usage:</p>
 * <pre>
 *     &#64;PartitionIDs("client_id", "dob")
 *     public class User{ }
 * </pre>
 *
 * @author igor on 9/2/17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PartitionIDs {
    String[] value();
}
