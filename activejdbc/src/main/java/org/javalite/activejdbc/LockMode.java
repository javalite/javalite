/*
Copyright 2009-(CURRENT YEAR) Igor Polevoy

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
package org.javalite.activejdbc;

/**
 * Defines row-level locking behavior for SELECT queries.
 * Different databases support different locking modes and syntax.
 *
 * <p>Example usage:</p>
 * <pre>
 * // Lock with WAIT (blocks until lock is available)
 * Person p = Person.findById(123, LockMode.FOR_UPDATE);
 *
 * // Lock with NOWAIT (fails immediately if already locked)
 * Person p = Person.findById(123, LockMode.FOR_UPDATE_NOWAIT);
 *
 * // Skip locked rows (useful for job queues)
 * List&lt;Task&gt; tasks = Task.where("status = ?", "pending")
 *                        .lockMode(LockMode.FOR_UPDATE_SKIP_LOCKED)
 *                        .limit(10);
 * </pre>
 *
 * @author ActiveJDBC Team
 */
public enum LockMode {
    /**
     * No locking. Standard SELECT query without any locking clause.
     */
    NONE,

    /**
     * Lock selected rows for update. If rows are already locked by another transaction,
     * wait until they become available.
     * <p>
     * Generates database-specific syntax:
     * <ul>
     *   <li>PostgreSQL, MySQL, Oracle, H2: <code>FOR UPDATE</code></li>
     *   <li>SQL Server: <code>WITH (UPDLOCK)</code></li>
     * </ul>
     */
    FOR_UPDATE,

    /**
     * Lock selected rows for update. If rows are already locked by another transaction,
     * fail immediately with an exception rather than waiting.
     * <p>
     * Generates database-specific syntax:
     * <ul>
     *   <li>PostgreSQL: <code>FOR UPDATE NOWAIT</code></li>
     *   <li>MySQL 8.0+: <code>FOR UPDATE NOWAIT</code></li>
     *   <li>Oracle: <code>FOR UPDATE NOWAIT</code></li>
     *   <li>SQL Server: <code>WITH (UPDLOCK)</code> (no NOWAIT support)</li>
     *   <li>H2: <code>FOR UPDATE</code> (no NOWAIT support)</li>
     * </ul>
     *
     * <p><strong>Note:</strong> Some databases (SQL Server, H2) do not distinguish between
     * FOR_UPDATE and FOR_UPDATE_NOWAIT and will behave the same for both modes.</p>
     */
    FOR_UPDATE_NOWAIT,

    /**
     * Lock selected rows for update. If rows are already locked by another transaction,
     * skip them and continue with the next available rows.
     * <p>
     * This is particularly useful for job queue processing where multiple workers
     * should pick up different unlocked jobs without blocking each other.
     * <p>
     * Generates database-specific syntax:
     * <ul>
     *   <li>PostgreSQL 9.5+: <code>FOR UPDATE SKIP LOCKED</code></li>
     *   <li>MySQL 8.0+: <code>FOR UPDATE SKIP LOCKED</code></li>
     *   <li>Oracle 11g+: <code>FOR UPDATE SKIP LOCKED</code></li>
     *   <li>SQL Server: <code>WITH (UPDLOCK, READPAST)</code></li>
     * </ul>
     *
     * <p><strong>Note:</strong> Older database versions may not support this mode
     * and will throw an UnsupportedOperationException.</p>
     */
    FOR_UPDATE_SKIP_LOCKED
}
