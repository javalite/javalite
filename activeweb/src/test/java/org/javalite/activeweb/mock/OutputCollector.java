package org.javalite.activeweb.mock;

import java.util.ArrayList;
import java.util.List;

/**
 * @author igor on 6/30/17.
 */
public class OutputCollector {
    private static List<String> lines =  new ArrayList<>();

    public static void addLine(String line){
        lines.add(line);
    }

    public static String  getLine(int index){
        return lines.get(index);
    }

    public static List<String> getLines(){
        return lines;
    }

    public static void reset(){
        lines  = new ArrayList<>();
    }
}
