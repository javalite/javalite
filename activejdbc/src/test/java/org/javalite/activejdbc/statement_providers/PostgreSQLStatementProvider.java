package org.javalite.activejdbc.statement_providers;

import org.javalite.activejdbc.StatementProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Polevoy
 */
public class PostgreSQLStatementProvider implements StatementProvider {
    public List<String> getPopulateStatements(String table) {

        List<String> statements = new ArrayList<>();
        if (table.equals("people")) {
            statements =  Arrays.asList(
                    "INSERT INTO people ( name, last_name, dob, graduation_date, created_at, updated_at) VALUES('John', 'Smith', '1934-12-01', '1954-12-01', now(), now());",
                    "INSERT INTO people (name, last_name, dob, graduation_date, created_at, updated_at) values('Leylah', 'Jonston', '1954-04-03', '1974-04-03', now(), now());",
                    "INSERT INTO people (name, last_name, dob, graduation_date, created_at, updated_at) values('Muhammad', 'Ali', '1943-01-04', '1963-01-04', now(), now());",
                    "INSERT INTO people (name, last_name, dob, graduation_date, created_at, updated_at) values('Joe', 'Pesci', '1944-02-23','1964-02-23', now(), now());"
            );
        } else if (table.equals("accounts")) {
            statements =  Arrays.asList(
                    "INSERT INTO accounts VALUES(1, '123', 'checking', 9999.99, 1234.32);"
            );
        } else if (table.equals("temperatures")) {
            statements =  Arrays.asList(
                    "INSERT INTO temperatures VALUES(1, 30);"
            );
        } else if (table.equals("salaries")) {
            statements =  Arrays.asList(
                    "INSERT INTO salaries VALUES(1, 50000.00);",
                    "INSERT INTO salaries VALUES(2, 0);"
            );
        } else if (table.equals("users")) {
            statements =  Arrays.asList(
                    "INSERT INTO users (first_name, last_name, email) VALUES('Marilyn', 'Monroe', 'mmonroe@yahoo.com');",
                    "INSERT INTO users (first_name, last_name, email) VALUES('John', 'Doe', 'jdoe@gmail.com');",
                    "INSERT INTO users (first_name, last_name, email) VALUES('James', 'Dean', 'jdean@hotmail.com');"

            );
        } else if (table.equals("addresses")) {
            statements =  Arrays.asList(
                    "INSERT INTO addresses (address1, address2, city, state, zip, user_id) VALUES('123 Pine St.', 'apt 31', 'Springfield', 'IL', '60606', 1);",
                    "INSERT INTO addresses (address1, address2, city, state, zip, user_id) VALUES('456 Brook St.', 'apt 21', 'Springfield', 'IL', '60606', 1);",
                    "INSERT INTO addresses (address1, address2, city, state, zip, user_id ) VALUES('23 Grove St.', 'apt 32', 'Springfield', 'IL', '60606', 1);",
                    "INSERT INTO addresses (address1, address2, city, state, zip, user_id ) VALUES('143 Madison St.', 'apt 34', 'Springfield', 'IL', '60606', 2);",
                    "INSERT INTO addresses (address1, address2, city, state, zip, user_id ) VALUES('153 Creek St.', 'apt 35', 'Springfield', 'IL', '60606', 2);",
                    "INSERT INTO addresses (address1, address2, city, state, zip, user_id ) VALUES('163 Gorge St.', 'apt 36', 'Springfield', 'IL', '60606', 2);",
                    "INSERT INTO addresses (address1, address2, city, state, zip, user_id ) VALUES('173 Far Side.', 'apt 37', 'Springfield', 'IL', '60606', 2);"
            );
        } else if (table.equals("rooms")) {
            statements =  Arrays.asList(
                    "INSERT INTO rooms (name, address_id) VALUES('bathroom', 1);",
                    "INSERT INTO rooms (name, address_id) VALUES('conference room', 1);",
                    "INSERT INTO rooms (name, address_id) VALUES('ball room', 7);",
                    "INSERT INTO rooms (name, address_id) VALUES('basement', 7);"
            );
        } else if (table.equals("legacy_universities")) {
            statements =  Arrays.asList(

                    "INSERT INTO legacy_universities  VALUES(1, 'DePaul', '123 Pine St.', 'apt 3B', 'Springfield', 'IL', '60606');"
            );
        } else if (table.equals("libraries")) {
            statements =  Arrays.asList(

                    "INSERT INTO libraries (address, city, state ) VALUES('124 Pine Street', 'St. Raphael', 'California');",
                    "INSERT INTO libraries (address, city, state ) VALUES('345 Burlington Blvd', 'Springfield', 'Il');"
            );
        } else if (table.equals("books")) {
            statements =  Arrays.asList(
                    "INSERT INTO books (title, author, isbn, lib_id ) VALUES('All Quiet on Western Front', 'Eric Remarque', '123', 1);",
                    "INSERT INTO books (title, author, isbn, lib_id ) VALUES('12 Chairs', 'Ilf, Petrov', '122', 1);"
            );
        } else if (table.equals("readers")) {
            statements =  Arrays.asList(
                    "INSERT INTO readers VALUES(1, 'John', 'Smith', 1);",
                    "INSERT INTO readers VALUES(2, 'John', 'Doe', 1);",
                    "INSERT INTO readers VALUES(3, 'Igor', 'Polevoy', 2);"
            );
        } else if (table.equals("animals")) {
            statements =  Arrays.asList(
                    "INSERT INTO animals VALUES(1, 'frog');"
            );
        } else if (table.equals("patients")) {
            statements =  Arrays.asList(
                    "INSERT INTO patients (first_name , last_name ) VALUES('Jim', 'Cary');",
                    "INSERT INTO patients (first_name , last_name ) VALUES('John', 'Carpenter');",
                    "INSERT INTO patients (first_name , last_name ) VALUES('John', 'Krugg');"
            );
        } else if (table.equals("prescriptions")) {
            statements =  Arrays.asList(
                    "INSERT INTO prescriptions (name, patient_id) VALUES('Viagra', 1);",
                    "INSERT INTO prescriptions (name, patient_id) VALUES('Prozac', 1);",
                    "INSERT INTO prescriptions (name, patient_id) VALUES('Valium', 2);",
                    "INSERT INTO prescriptions (name, patient_id) VALUES('Marijuana (medicinal) ', 2);",
                    "INSERT INTO prescriptions (name, patient_id) VALUES('CML treatment', 3);"
            );
        } else if (table.equals("doctors")) {
            statements =  Arrays.asList(
                    "INSERT INTO doctors (first_name, last_name, discipline) VALUES('John', 'Doe', 'otolaryngology');",
                    "INSERT INTO doctors (first_name, last_name, discipline) VALUES('Hellen', 'Hunt', 'dentistry');",
                    "INSERT INTO doctors (first_name, last_name, discipline) VALUES('John', 'Druker', 'oncology');",
                    "INSERT INTO doctors (id, first_name, last_name, discipline) VALUES(4, 'Henry', 'Jekyll', 'pathology');"

            );
        } else if (table.equals("doctors_patients")) {
            statements =  Arrays.asList(
                    "INSERT INTO doctors_patients (doctor_id, patient_id ) VALUES(1, 2);",
                    "INSERT INTO doctors_patients (doctor_id, patient_id ) VALUES(1, 1);",
                    "INSERT INTO doctors_patients (doctor_id, patient_id ) VALUES(2, 1);",
                    "INSERT INTO doctors_patients (doctor_id, patient_id ) VALUES(3, 3);"
            );
        } else if (table.equals("students")) {
            statements =  Arrays.asList(
                    "INSERT INTO students (first_name, last_name, dob, enrollment_date) VALUES ('Jim', 'Cary', DATE '1965-12-01', TIMESTAMP '1973-01-20 11:00:00');",
                    "INSERT INTO students (first_name, last_name, dob, enrollment_date) VALUES ('John', 'Carpenter', DATE '1979-12-01', TIMESTAMP '1987-01-29 13:00:00');"
            );
        } else if (table.equals("courses")) {
            statements =  Arrays.asList(
                    "INSERT INTO courses  VALUES(1, 'Functional programming 101');",
                    "INSERT INTO courses  VALUES(2, 'data structures 415');"
            );
        } else if (table.equals("registrations")) {
            statements =  Arrays.asList(
                    "INSERT INTO registrations VALUES(1, 1, 2);",
                    "INSERT INTO registrations VALUES(2, 1, 1);",
                    "INSERT INTO registrations VALUES(3, 2, 1);"
            );
        } else if (table.equals("items")) {
            statements =  Arrays.asList(

            );
        } else if (table.equals("articles")) {
            statements =  Arrays.asList(
                    "INSERT INTO articles VALUES(1, 'ActiveJDBC basics', 'this is a test content of the article');",
                    "INSERT INTO articles VALUES(2, 'ActiveJDBC polymorphic associations', 'Polymorphic associations are...');"
            );
        } else if (table.equals("posts")) {
            statements =  Arrays.asList(
                    "INSERT INTO posts VALUES(1, 'Who gets up early in the morning... is tired all day', 'this is to explain that ...sleeping in is actually really good...');",
                    "INSERT INTO posts VALUES(2, 'Thou shalt not thread', 'Suns strategy for threading inside J2EE is a bit... insane...');"
            );
        } else if (table.equals("comments")) {
            statements =  Arrays.asList();
        } else if (table.equals("fruits")) {
            statements =  Arrays.asList();
        } else if (table.equals("vegetables")) {
            statements =  Arrays.asList();
        } else if (table.equals("plants")) {
            statements =  Arrays.asList();
        } else if (table.equals("pages")) {
            statements = Arrays.asList();
        } else if (table.equals("watermelons")) {
            statements = Arrays.asList();
        } else if (table.equals("motherboards")) {
            statements = Arrays.asList(
                    "INSERT INTO motherboards (description) VALUES('motherboardOne');"
            );
        } else if (table.equals("keyboards")) {
            statements = Arrays.asList(
                    "INSERT INTO keyboards (description) VALUES('keyboard-us');"
            );
        } else if (table.equals("computers")) {
            statements = Arrays.asList(
                    "INSERT INTO computers (description, mother_id, key_id) VALUES('ComputerX',1,1);"
            );
        } else if (table.equals("ingredients_recipes")) {
            statements = Arrays.asList();
        } else if (table.equals("ingredients")) {
            statements = Arrays.asList();
        } else if (table.equals("recipes")) {
            statements = Arrays.asList();
        } else if (table.equals("vehicles")) {
            statements = Arrays.asList();
        } else if (table.equals("mammals")) {
            statements = Arrays.asList();
        } else if (table.equals("nodes")) {
            statements =  Arrays.asList(
                    "INSERT INTO nodes VALUES (1, 'Parent', NULL);",
                    "INSERT INTO nodes VALUES (2, 'Self', 1);",
                    "INSERT INTO nodes VALUES (3, 'Sibling', 1);",
                    "INSERT INTO nodes VALUES (4, 'Child', 2);");
        } else {
            statements = Arrays.asList();
        }

        ArrayList<String> all = new ArrayList<>();
        all.add("DELETE FROM " + table + ";");
        if(table.equals("animals")){
            all.add("UPDATE dual SET next_val = SETVAL('animals_animal_id_seq', 1, FALSE);");
        }else{
            all.add("UPDATE dual SET next_val = SETVAL('" + table + "_id_seq', 1, FALSE);");
        }

        all.addAll(statements);
        return all;
    }

    public String getDeleteStatement(String table){
        return "DELETE FROM " + table + ";";
    }
}
