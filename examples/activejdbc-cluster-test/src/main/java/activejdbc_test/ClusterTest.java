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


package activejdbc_test;

import org.javalite.activejdbc.Base;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * @author Igor Polevoy
 */
public class ClusterTest {


    public void work() {
        Scanner s = new Scanner(System.in);

        print("Hint: commands are: 'i' for insert, 's' for select");
        System.out.print("command$ ");

        while (s.hasNext()) {

            String command = s.next();
            if (!command.equalsIgnoreCase("i") && !command.equalsIgnoreCase("s")) {
                print("wrong command, use 's' or 'i'");
            } else if (command.equalsIgnoreCase("i")) {
                insert();
            } else {
                select();
            }
            System.out.print("$ ");
        }
    }


    private void print(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) throws SQLException {

        ClusterTest ct = new ClusterTest();
        ct.work();

    }

    public void insert() {
        Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/activejdbc", "root", "p@ssw0rd");

        Account account = new Account();
        account.set("account", "account: " + String.valueOf(System.currentTimeMillis()));
        account.set("description", "description: " + String.valueOf(System.currentTimeMillis()));
        account.set("amount", 10.3);
        account.set("total", 444.4);
        account.saveIt();
        Base.close();
    }

    public void select() {

        Base.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/activejdbc", "root", "p@ssw0rd");

        System.out.println("Total accounts: " + Account.count());
        Base.close();
    }


}
