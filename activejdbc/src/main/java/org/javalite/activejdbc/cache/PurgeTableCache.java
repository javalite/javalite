package org.javalite.activejdbc.cache;

import org.javalite.activejdbc.MetaModel;
import org.javalite.activejdbc.Registry;

import java.util.HashSet;
import java.util.Set;

import static org.javalite.activejdbc.ModelDelegate.metaModelFor;

public class PurgeTableCache implements AutoCloseable {

    private static class State {

        private PurgeTableCache owner;
        private Set<String> tables;

        private State(PurgeTableCache owner) {
            this.owner = owner;
            tables = new HashSet<>();
        }

    }

    private static final ThreadLocal<State> stateTL = new ThreadLocal<>();

    private String codePoint() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        return "[" + elements[3].toString() + ", " + elements[4].toString() + "]";
    }
    
    public PurgeTableCache() {
        State state = stateTL.get();
        if (state == null) {
            state = new State(this);
            System.err.println("\t\t\t\t" + state.hashCode() + " PurgeTableCache create new! " + codePoint());
//            new Exception().printStackTrace();
            stateTL.set(state);
        }
    }

    public PurgeTableCache add(String tableName) {
        return add(metaModelFor(tableName));
    }

    public PurgeTableCache add(MetaModel metaModel) {
        if (metaModel.cached()) {
            System.err.println("\t\t\t\t" + stateTL.get().hashCode() + " PurgeTableCache add '" + metaModel.getTableName() + "' " + codePoint());
            stateTL.get().tables.add(metaModel.getTableName());
        } else {
            System.err.println("\t\t\t\t" + stateTL.get().hashCode() + " PurgeTableCache skip '" + metaModel.getTableName() + "' " + codePoint());
        }
        return this;
    }

    public static void purge(MetaModel metaModel) {
        if (metaModel.cached()) {
            try (PurgeTableCache ptc = new PurgeTableCache()) {
                ptc.add(metaModel);
            }
        }
    }

    public void close() {
        State state = stateTL.get();
        if (state.owner == this) { //TODO should be NPE if not initialized (for DEV DEBUG)
            stateTL.remove();
            CacheManager cacheManager = Registry.cacheManager();
            System.err.println("\t\t\t\t" + state.hashCode() + " PurgeTableCache close and purge " + state.tables + " " + codePoint());
            for(String table : state.tables) {
                cacheManager.flush(new CacheEvent(table, getClass().getName()));
            }
        }
    }


}
