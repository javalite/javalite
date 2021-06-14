package org.javalite.activeweb;


import java.util.List;

import static org.javalite.common.Collections.list;

public enum Format {

    JSON(","), YAML("\n\r");

    private String delimiter;

    Format(String delimiter){
         this.delimiter = delimiter;
    }

    public boolean matches(String format){

        if(this.equals(JSON) && format.equalsIgnoreCase("json")){
            return true;
        }else if(this.equals(YAML) && (format.equalsIgnoreCase("yaml") || format.equalsIgnoreCase("yml"))){
            return true;
        }
        return false;
    }

    public String getDelimiter() {
        return delimiter;
    }
}
