package org.javalite.activejdbc.cache;

import org.javalite.activejdbc.MetaModel;
import org.javalite.activejdbc.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.javalite.activejdbc.ModelDelegate.metaModelFor;

/**
 * This class exists in order to collapse multiple similar cache purge events into one.
 * This is especially crucial in cases of cascade deletes. In the absence of this  class, the system would
 * generate unnecessary duplicate cache purge events.
 *
 * @author yanchevsky
 */
public class CacheEventSquasher implements AutoCloseable {

    /**
     * Exist to have a single set on a single thread.
     */
    private static class State {

        private CacheEventSquasher owner;
        private Set<String> tables;

        private State(CacheEventSquasher owner) {
            this.owner = owner;
            tables = new HashSet<>();
        }

    }

    private static final ThreadLocal<State> stateTL = new ThreadLocal<>();

    /**
     * Exists for debugging,   see usage in comments below inside a COnstructor.
     */
    private String codePoint() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        return "[" + elements[3].toString() + ", " + elements[4].toString() + "]";
    }
    
    public CacheEventSquasher() {
        State state = stateTL.get();
        if (state == null) {
            state = new State(this);
//            System.err.println("\t\t\t\t" + state.hashCode() + " CacheEventSquasher create new! " + codePoint());
//            new Exception().printStackTrace();
            stateTL.set(state);
        }
    }

    public CacheEventSquasher add(String tableName) {
        return add(metaModelFor(tableName));
    }

    public CacheEventSquasher add(MetaModel metaModel) {
        if (metaModel.cached()) {
            //System.err.println("\t\t\t\t" + stateTL.get().hashCode() + " CacheEventSquasher add '" + metaModel.getTableName() + "' " + codePoint()); //  for debug only
            stateTL.get().tables.add(metaModel.getTableName());
        }
        //debug only:
//        else {
//            //System.err.println("\t\t\t\t" + stateTL.get().hashCode() + " CacheEventSquasher skip '" + metaModel.getTableName() + "' " + codePoint());//  for debug only
//        }
        return this;
    }

    public static void purge(MetaModel metaModel) {
        if (metaModel.cached()) {
            try (CacheEventSquasher ces = new CacheEventSquasher()) {
                ces.add(metaModel);
            }
        }
    }

    public void close() {
        State state = stateTL.get();
        if (state.owner == this) { //TODO should be NPE if not initialized (for DEV DEBUG)
            stateTL.remove();
            CacheManager cacheManager = Registry.cacheManager();
//            System.err.println("\t\t\t\t" + state.hashCode() + " CacheEventSquasher close and purge " + state.tables + " " + codePoint());
            for(String table : state.tables) {
                cacheManager.flush(new CacheEvent(table, getClass().getName()));
            }
        }
    }


}
