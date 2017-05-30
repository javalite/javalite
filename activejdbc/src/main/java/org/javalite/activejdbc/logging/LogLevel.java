package org.javalite.activejdbc.logging;

/**
 * @author igor on 5/29/17.
 */
public enum LogLevel {
    DEBUG(0), INFO(1), WARNING(2), ERROR(3);
    LogLevel(int level) {
        this.level = level;
    }
    private int level;
    public int getLevel(){
        return level;
    }
}
