package org.javalite.activeweb;

import io.github.classgraph.ScanResult;

import java.io.Closeable;
import java.util.ArrayList;

/**
 * Exists only to make code less spaghetti-like.
 */
public class CloseableList<T> extends ArrayList<T> implements Closeable {
    @Override
    public void close(){
        ScanResult.closeAll();
    }
}
