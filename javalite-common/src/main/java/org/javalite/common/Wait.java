package org.javalite.common;

import java.util.concurrent.TimeUnit;


/**
 * Borrowed from Apache Artemis. Can wait for a condition.
 *
 * <br>
 * Example  of usage:
 * <pre>
 *
 *   <code>Wait.waitFor(() -> messageService.getMessageCount() == 1);</code>
 * </pre>
 *
 *
 * <h3>Defaults:</h3>
 * <uil>
 *      <li>MAX_WAIT_MILLIS = 30 * 1000</li>
 *      <li>SLEEP_MILLIS = 100</li>
 * </uil>
 *
 */
public class Wait {

    private static final long MAX_WAIT_MILLIS = 30 * 1000;
    private static final int SLEEP_MILLIS = 100;

    public interface Condition {
        boolean isSatisfied() throws Exception;
    }

    /**
     * Same as {@link #waitFor(long, long, Condition)}
     *
     * @param condition condition to check from time to time;
     * @return true if condition was  satisfied, false if not.
     */
    public static boolean waitFor(Condition condition){
        return waitFor(MAX_WAIT_MILLIS, condition);
    }

    /**
     * Waits for a condition to be satisfied or till <code>durationMillis</code>, whichever is sooner.
     *
     * @param durationMillis max time to wait in millis.
     * @param condition condition to check from time to time;
     * @return true if condition was  satisfied, false if not.
     */
    public static boolean waitFor(final long durationMillis, final Condition condition){
        return waitFor(durationMillis, SLEEP_MILLIS, condition);
    }

    /**
     *
     * Same as {@link #waitFor(long, Condition)}.
     *
     * @param sleepMillis time to sleep between checks.
     * @param durationMillis max time to wait in millis.
     * @param condition condition to check from time to time;
     * @return true if condition was satisfied, false if not.
     */
    public static boolean waitFor(final long durationMillis, final long sleepMillis, final Condition condition) {
        try {
            final long expiry = System.currentTimeMillis() + durationMillis;
            boolean conditionSatisified = condition.isSatisfied();
            while (!conditionSatisified && System.currentTimeMillis() < expiry) {
                if (sleepMillis == 0) {
                    Thread.yield();
                } else {
                    TimeUnit.MILLISECONDS.sleep(sleepMillis);
                }
                conditionSatisified = condition.isSatisfied();
            }
            return conditionSatisified;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
