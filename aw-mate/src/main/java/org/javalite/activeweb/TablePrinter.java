package org.javalite.activeweb;

import org.javalite.common.Util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This code was stolen... ehhr borrowed from: https://itsallbinary.com/java-printing-to-console-in-table-format-simple-code-with-flexible-width-left-align-header-separator-line/
 */
public class TablePrinter {


    /*
     * Table to print in console in 2-dimensional array. Each sub-array is a row.
     */
    static String[][] table = new String[][] { { "id", "First Name", "Last Name", "Age" },
            { "1", "John", "Johnson", "45" }, { "2", "Tom", "", "35" }, { "3", "Rose", "Johnson asdfg asd fgafgr ar rg", "23"},
            { "4", "Jimmy", "Kimmel", "" } };


    //leaving here as an example
    public static void main(String[] args) {
        printTable(table);
    }

    /**
     * Expects a two-dimensional array of strings. Does no tolerate null values. ALl cells must be String instances.
     */
    public static void printTable(String[][] table) {
        /*
         * leftJustifiedRows - If true, it will add "-" as a flag to format string to
         * make it left justified. Otherwise right justified.
         */
        boolean leftJustifiedRows = true;


        /*
         * Calculate appropriate Length of each column by looking at width of data in
         * each column.
         *
         * Map columnLengths is <column_number, column_length>
         */
        Map<Integer, Integer> columnLengths = new HashMap<>();
        Arrays.stream(table).forEach(a -> Stream.iterate(0, (i -> i < a.length), (i -> ++i)).forEach(i -> {
            if (columnLengths.get(i) == null) {
                columnLengths.put(i, 0);
            }
            if (columnLengths.get(i) < a[i].length()) {
                columnLengths.put(i, a[i].length());
            }
        }));

        /*
         * Prepare format String
         */
        final StringBuilder formatString = new StringBuilder("");
        String flag = leftJustifiedRows ? "-" : "";
        columnLengths.entrySet().stream().forEach(e -> formatString.append("| %" + flag + e.getValue() + "s "));
        formatString.append("|\n");


        /*
         * Prepare line for top, bottom & below header row.
         */
        String line = columnLengths.entrySet().stream().reduce("", (ln, b) -> {
            String templn = "+-";
            templn = templn + Stream.iterate(0, (i -> i < b.getValue()), (i -> ++i)).reduce("", (ln1, b1) -> ln1 + "-",
                    (a1, b1) -> a1 + b1);
            templn = templn + "-";
            return ln + templn;
        }, (a, b) -> a + b);
        line = line + "+\n";

        /*
         * Print table
         */
        System.out.print(line);
        Arrays.stream(table).limit(1).forEach(a -> System.out.printf(formatString.toString(), a));
        System.out.print(line);

        Stream.iterate(1, (i -> i < table.length), (i -> ++i))
                .forEach(a -> System.out.printf(formatString.toString(), table[a]));
        System.out.print(line);
    }



    static void printEndpointDefinitions(String title, List<EndPointDefinition> endPointDefinitions){
        System.out.println(String.format("""
                                  
                **
                **  %s
                **""", title));
        String[][] table  = new String[endPointDefinitions.size() + 1][5];
        table[0][0] = "Number";
        table[0][1] = "Path";
        table[0][2] = "HTTP Methods";
        table[0][3] = "Controller";
        table[0][4] = "Method";



        for (int row = 0; row < endPointDefinitions.size() ; row++) {
            EndPointDefinition endPointDefinition = endPointDefinitions.get(row);
            table[row + 1][0] = Integer.toString(row + 1);
            table[row + 1][1] = endPointDefinition.getPath();
            table[row + 1][2] = Util.join(endPointDefinition.getHTTPMethods(), ",");
            table[row + 1][3] = endPointDefinition.getControllerClassName();
            table[row + 1][4] = endPointDefinition.getDisplayControllerMethod();

        }
        printTable(table);
    }


}
