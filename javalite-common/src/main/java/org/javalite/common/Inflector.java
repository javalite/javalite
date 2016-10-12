/*
Copyright 2009-2016 Igor Polevoy

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


package org.javalite.common;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class Inflector {

    private static final List<String[]> singulars;
    private static final List<String[]> plurals;
    private static final List<String[]> irregulars;
    private static final List<String> uncountables;

    static {
        singulars = new ArrayList<String[]>();
        plurals = new ArrayList<String[]>();
        irregulars = new ArrayList<String[]>();

        addPlural("$", "s");
        addPlural("s$", "s");
        addPlural("(ax|test)is$", "$1es");
        addPlural("(octop|vir)us$", "$1i");
        addPlural("(alias|status)$", "$1es");
        addPlural("(bu)s$", "$1ses");
        addPlural("(buffal|tomat)o$", "$1oes");
        addPlural("([ti])um$", "$1a");
        addPlural("sis$", "ses");
        addPlural("(?:([^f])fe|([lr])f)$", "$1$2ves");
        addPlural("(hive)$", "$1s");
        addPlural("([^aeiouy]|qu)y$", "$1ies");
        addPlural("(x|ch|ss|sh)$", "$1es");
        addPlural("(matr|vert|ind)(?:ix|ex)$", "$1ices");
        addPlural("([m|l])ouse$", "$1ice");
        addPlural("^(ox)$", "$1en");
        addPlural("(quiz)$", "$1zes");


        addSingular("s$", "");
        addSingular("(n)ews$", "$1ews");
        addSingular("([ti])a$", "$1um");
        addSingular("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "$1sis");
        addSingular("(^analy)ses$", "$1sis");
        addSingular("([^f])ves$", "$1fe");
        addSingular("(hive)s$", "$1");
        addSingular("(tive)s$", "$1");
        addSingular("([lr])ves$", "$1f");
        addSingular("([^aeiouy]|qu)ies$", "$1y");
        addSingular("(s)eries$", "$1eries");
        addSingular("(m)ovies$", "$1ovie");
        addSingular("(x|ch|ss|sh)es$", "$1");
        addSingular("([m|l])ice$", "$1ouse");
        addSingular("(bus)es$", "$1");
        addSingular("(o)es$", "$1");
        addSingular("(shoe)s$", "$1");
        addSingular("(cris|ax|test)es$", "$1is");
        addSingular("(octop|vir)i$", "$1us");
        addSingular("(alias|status)es$", "$1");
        addSingular("^(ox)en", "$1");
        addSingular("(vert|ind)ices$", "$1ex");
        addSingular("(matr)ices$", "$1ix");
        addSingular("(quiz)zes$", "$1");
        addSingular("(database)s$", "$1");

        addIrregular("person", "people");
        addIrregular("man", "men");
        addIrregular("child", "children");
        addIrregular("sex", "sexes");
        addIrregular("move", "moves");

        uncountables = Arrays.asList("equipment", "information", "rice", "money", "species", "series", "fish", "sheep");
    }
    
    private Inflector() {
        
    }

    public static void addPlural(String rule, String replacement){
        plurals.add(0, new String[]{rule, replacement});
    }

    public static void addSingular(String rule, String replacement){
        singulars.add(0, new String[]{rule, replacement});
    }

    public static void addIrregular(String rule, String replacement){
        irregulars.add(new String[]{rule, replacement});
    }




    /**
     * Replaces a found pattern in a word and returns a transformed word.
     * @param word
     * @param rule
     * @param replacement
     * @return Replaces a found pattern in a word and returns a transformed word. Null is pattern does not match.
     */
    public static String gsub(String word, String rule, String replacement) {
        Pattern pattern = Pattern.compile(rule, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(word);
        return matcher.find() ? matcher.replaceFirst(replacement) : null;
    }

    public static String pluralize(String word) {

        if(uncountables.contains(word)) return word;

        for (String[] irregular : irregulars) {
            if (irregular[0].equalsIgnoreCase(word)) {
                return irregular[1];
            }
        }

        for (String[] pair: plurals) {
            String plural = gsub(word, pair[0], pair[1]);
            if (plural != null)
                return plural;
        }

        return word;
    }


     public static String singularize(String word) {

        if(uncountables.contains(word)) return word;

        for (String[] irregular : irregulars) {
            if (irregular[1].equalsIgnoreCase(word)) {
                return irregular[0];
            }
        }

        for (String[] pair: singulars) {
            String singular = gsub(word, pair[0], pair[1]);
            if (singular != null)
                return singular;
        }

        return word;
    }

    /**
     * Converts a camel case to underscore and then pluralizes.
     *
     * Example: "GrayDuck" is converted to "gray_ducks".
     * 
     * @param camelCase any CamelCase phrase.
     * @return pluralized version of underscored CamelCase.
     */
    public static String tableize(String camelCase) {
        return pluralize(underscore(camelCase));
    }

    /**
     * Converts a CamelCase string to underscores: "AliceInWonderLand" becomes:
     * "alice_in_wonderland"
     *
     * @param camel camel case input
     * @return result converted to underscores.
     */
    public static String underscore(String camel) {

        List<Integer> upper = new ArrayList<Integer>();
        byte[] bytes = camel.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            if (b < 97 || b > 122) {
                upper.add(i);
            }
        }

        StringBuilder b = new StringBuilder(camel);
        for (int i = upper.size() - 1; i >= 0; i--) {
            Integer index = upper.get(i);
            if (index != 0)
                b.insert(index, "_");
        }

        return b.toString().toLowerCase();
    }


    /**
     * Generates a camel case version of a phrase from underscore.
     *
     * @param underscore underscore version of a word to converted to camel case.
     * @return camel case version of underscore.
     */
    public static String camelize(String underscore){
        return camelize(underscore, true);
    }


    /**
     * Generates a camel case version of a phrase from underscore.
     *
     * @param underscore underscore version of a word to converted to camel case.
     * @param capitalizeFirstChar set to true if first character needs to be capitalized, false if not.
     * @return camel case version of underscore.
     */
    public static String camelize(String underscore, boolean capitalizeFirstChar){
        StringBuilder result = new StringBuilder();
        StringTokenizer st = new StringTokenizer(underscore, "_");
        while (st.hasMoreTokens()) {
            result.append(capitalize(st.nextToken()));
        }
        return capitalizeFirstChar ? result.toString() : result.substring(0, 1).toLowerCase() + result.substring(1);
    }

    /**
     * Capitalizes a word  - only a first character is converted to upper case.
     *
     * @param word word/phrase to capitalize.
     * @return same as input argument, but the first character is capitalized.
     */
    public static String capitalize(String word){
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }


    /**
     * @deprecated Use <tt>clazz.getSimpleName()</tt> instead of <tt>Inflector.shortName(clazz.getName())</tt>.
     */
    @Deprecated
    public static String shortName(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    public static String getIdName(String tableName) {
        return Inflector.singularize(tableName).toLowerCase() + "_id";
    }

    /**
     * If a table name is made of two other table names (as is typical for many to many relationships),
     * this method retrieves a name of "another" table from a join table name.
     * For instance, if a source table is "payer" and the target is "player_game", then the returned value
     * will be "game".
     *
     * @param source known table name. It may or may not exist in the target table name.
     * @param target this is a potential "join" table name.
     * @return a name of "another" table from a join table name.
     */
    public static String getOtherName(String source, String target){

        String other;
        if (target.contains(source) && !target.equals(source)) {

            int start = target.indexOf(source);
            other = start == 0 ? target.substring(source.length()) : target.substring(0, start);
        }
        else{
            return null;
        }
        if(other.startsWith("_")){
            other = other.replaceFirst("_", " ");
        }
        if(other.endsWith("_")){
            byte[] otherb = other.getBytes();
            otherb[otherb.length - 1] = ' ';
            other = new String(otherb);
        }
        return other.trim();
    }
}
