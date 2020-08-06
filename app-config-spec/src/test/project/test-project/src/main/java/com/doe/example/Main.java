package com.doe.example;

import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.connection_config.DBConfiguration;

public class Main {
    public static void main(String[] args){
        DBConfiguration.loadConfiguration("/database.properties");
        Base.open();
    }
}
