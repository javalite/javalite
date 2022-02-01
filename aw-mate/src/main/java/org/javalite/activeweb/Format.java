package org.javalite.activeweb;


public enum Format {

    JSON, YAML;
    public boolean matches(String format){

        if(this.equals(JSON) && format.equalsIgnoreCase("json")){
            return true;
        }else if(this.equals(YAML) && (format.equalsIgnoreCase("yaml") || format.equalsIgnoreCase("yml"))){
            return true;
        }
        return false;
    }
}
