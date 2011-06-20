/*
Copyright 2009-2010 Igor Polevoy

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
package activejdbc.examples.multidb;

import activejdbc.DB;

public class Main {
    public static void main(String[] args) {
        new DB("corporation").open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test", "root", "p@ssw0rd");
        new DB("university").open("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@localhost:1521:xe", "activejdbc", "activejdbc");

        Employee.deleteAll();
        Student.deleteAll();

        Employee.createIt("first_name", "John", "last_name", "Doe");
        Employee.createIt("first_name", "Jane", "last_name", "Smith");

        Student.createIt("first_name", "Mike", "last_name", "Myers");
        Student.createIt("first_name", "Steven", "last_name", "Spielberg");

        System.out.println("*** Employees ***");
        Employee.findAll().dump();
        System.out.println("*** Students ***");
        Student.findAll().dump();

        new DB("corporation").close();
        new DB("university").close();
    }
}
