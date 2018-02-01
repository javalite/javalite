package org.javalite.activejdbc.statement_providers;

import org.javalite.activejdbc.StatementProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DB2StatementProvider implements StatementProvider {
    public List<String> getPopulateStatements(String table) {

        List<String> statements;
        String ID_COLUMN = "ID";
        if (table.equals("people")) {
            statements = Arrays.asList(
                    "INSERT INTO people (id, name, last_name, dob, graduation_date, created_at, updated_at) VALUES(1, 'John', 'Smith', '1934-12-01', '1954-12-01', CURRENT TIMESTAMP, CURRENT TIMESTAMP)",
                    "INSERT INTO people (id, name, last_name, dob, graduation_date, created_at, updated_at) values(2, 'Leylah', 'Jonston', '1954-04-03', '1974-04-03', CURRENT TIMESTAMP, CURRENT TIMESTAMP)",
                    "INSERT INTO people (id, name, last_name, dob, graduation_date, created_at, updated_at) values(3, 'Muhammad', 'Ali', '1943-01-04', '1963-01-04', CURRENT TIMESTAMP, CURRENT TIMESTAMP)",
                    "INSERT INTO people (id, name, last_name, dob, graduation_date, created_at, updated_at) values(4, 'Joe', 'Pesci', '1944-02-23','1964-02-23', CURRENT TIMESTAMP, CURRENT TIMESTAMP)"
            );
        } else if (table.equals("accounts")) {
            statements = Arrays.asList(
                    "INSERT INTO accounts (id, account, description, amount, total) VALUES(1, '123', 'checking', 9999.99, 1234.32)"
            );
        } else if (table.equals("temperatures")) {
            statements = Arrays.asList(
                    "INSERT INTO temperatures (id, temp) VALUES(1, 30)"
            );
        } else if (table.equals("salaries")) {
            statements = Arrays.asList(
                    "INSERT INTO salaries (id, salary) VALUES(1, 50000.00)",
                    "INSERT INTO salaries (id, salary) VALUES(2, 0)"
            );
        } else if (table.equals("users")) {
            statements = Arrays.asList(
                    "INSERT INTO users (id, first_name, last_name, email) VALUES(1, 'Marilyn', 'Monroe', 'mmonroe@yahoo.com')",
                    "INSERT INTO users (id, first_name, last_name, email) VALUES(2, 'John', 'Doe', 'jdoe@gmail.com')",
                    "INSERT INTO users (id, first_name, last_name, email) VALUES(3, 'James', 'Dean', 'jdean@hotmail.com')"
            );
        } else if (table.equals("addresses")) {
            statements = Arrays.asList(
                    "INSERT INTO addresses (id, address1, address2, city, state, zip, user_id) VALUES(1, '123 Pine St.', 'apt 31', 'Springfield', 'IL', '60606', 1)",
                    "INSERT INTO addresses (id, address1, address2, city, state, zip, user_id) VALUES(2, '456 Brook St.', 'apt 21', 'Springfield', 'IL', '60606', 1)",
                    "INSERT INTO addresses (id, address1, address2, city, state, zip, user_id) VALUES(3, '23 Grove St.', 'apt 32', 'Springfield', 'IL', '60606', 1)",
                    "INSERT INTO addresses (id, address1, address2, city, state, zip, user_id) VALUES(4, '143 Madison St.', 'apt 34', 'Springfield', 'IL', '60606', 2)",
                    "INSERT INTO addresses (id, address1, address2, city, state, zip, user_id) VALUES(5, '153 Creek St.', 'apt 35', 'Springfield', 'IL', '60606', 2)",
                    "INSERT INTO addresses (id, address1, address2, city, state, zip, user_id) VALUES(6, '163 Gorge St.', 'apt 36', 'Springfield', 'IL', '60606', 2)",
                    "INSERT INTO addresses (id, address1, address2, city, state, zip, user_id) VALUES(7, '173 Far Side.', 'apt 37', 'Springfield', 'IL', '60606', 2)"
            );
        } else if (table.equals("rooms")) {
            statements = Arrays.asList(
                    "INSERT INTO rooms (id, name, address_id) VALUES(1, 'bathroom', 1)",
                    "INSERT INTO rooms (id, name, address_id) VALUES(2, 'conference room', 1)",
                    "INSERT INTO rooms (id, name, address_id) VALUES(3, 'ball room', 7)",
                    "INSERT INTO rooms (id, name, address_id) VALUES(4, 'basement', 7)"
            );
        } else if (table.equals("legacy_universities")) {
            statements = Arrays.asList(

                    "INSERT INTO legacy_universities (id, univ_name, address1, address2, city, state, zip) VALUES(1, 'DePaul', '123 Pine St.', 'apt 3B', 'Springfield', 'IL', '60606')"
            );
        } else if (table.equals("libraries")) {
            statements = Arrays.asList(

                    "INSERT INTO libraries (id, address, city, state) VALUES(1, '124 Pine Street', 'St. Raphael', 'California')",
                    "INSERT INTO libraries (id, address, city, state) VALUES(2, '345 Burlington Blvd', 'Springfield', 'Il')"
            );
        } else if (table.equals("books")) {
            statements = Arrays.asList(
                    "INSERT INTO books (id, title, author, isbn, lib_id) VALUES(1, 'All Quiet on Western Front', 'Eric Remarque', '123', 1)",
                    "INSERT INTO books (id, title, author, isbn, lib_id) VALUES(2, '12 Chairs', 'Ilf, Petrov', '122', 1)"
            );
        } else if (table.equals("readers")) {
            statements = Arrays.asList(
                    "INSERT INTO readers (id, first_name, last_name, book_id) VALUES(1, 'John', 'Smith', 1)",
                    "INSERT INTO readers (id, first_name, last_name, book_id) VALUES(2, 'John', 'Doe', 1)",
                    "INSERT INTO readers (id, first_name, last_name, book_id) VALUES(3, 'Igor', 'Polevoy', 2)"
            );
        } else if (table.equals("animals")) {
            statements = Arrays.asList(
                    "INSERT INTO animals (animal_id, animal_name) VALUES(1, 'frog')"
            );
            ID_COLUMN = "ANIMAL_ID";
        } else if (table.equals("patients")) {
            statements = Arrays.asList(
                    "INSERT INTO patients (id, first_name, last_name) VALUES(1, 'Jim', 'Cary')",
                    "INSERT INTO patients (id, first_name, last_name) VALUES(2, 'John', 'Carpenter')",
                    "INSERT INTO patients (id, first_name, last_name) VALUES(3, 'John', 'Krugg')"
            );
        } else if (table.equals("prescriptions")) {
            statements = Arrays.asList(
                    "INSERT INTO prescriptions (id, name, patient_id) VALUES(1, 'Viagra', 1)",
                    "INSERT INTO prescriptions (id, name, patient_id) VALUES(2, 'Prozac', 1)",
                    "INSERT INTO prescriptions (id, name, patient_id) VALUES(3, 'Valium', 2)",
                    "INSERT INTO prescriptions (id, name, patient_id) VALUES(4, 'Marijuana (medicinal) ', 2)",
                    "INSERT INTO prescriptions (id, name, patient_id) VALUES(5, 'CML treatment', 3)"
            );
        } else if (table.equals("doctors")) {
            statements = Arrays.asList(
                    "INSERT INTO doctors (id, first_name, last_name, discipline) VALUES(1, 'John', 'Doe', 'otolaryngology')",
                    "INSERT INTO doctors (id, first_name, last_name, discipline) VALUES(2, 'Hellen', 'Hunt', 'dentistry')",
                    "INSERT INTO doctors (id, first_name, last_name, discipline) VALUES(3, 'John', 'Druker', 'oncology')",
                    "INSERT INTO doctors (id, first_name, last_name, discipline) VALUES(4, 'Henry', 'Jekyll', 'pathology')"
            );
        } else if (table.equals("doctors_patients")) {
            statements = Arrays.asList(
                    "INSERT INTO doctors_patients (id, doctor_id, patient_id) VALUES(1, 1, 2)",
                    "INSERT INTO doctors_patients (id, doctor_id, patient_id) VALUES(2, 1, 1)",
                    "INSERT INTO doctors_patients (id, doctor_id, patient_id) VALUES(3, 2, 1)",
                    "INSERT INTO doctors_patients (id, doctor_id, patient_id) VALUES(4, 3, 3)"
            );
        } else if (table.equals("students")) {
            statements = Arrays.asList(
                    "INSERT INTO students (id, first_name, last_name, dob, enrollment_date) VALUES (1, 'Jim', 'Cary', '1965-12-01', '1973-01-20 11:00:00')",
                    "INSERT INTO students (id, first_name, last_name, dob, enrollment_date) VALUES (2, 'John', 'Carpenter', '1979-12-01', '1987-01-29 13:00:00')"
            );
        } else if (table.equals("courses")) {
            statements = Arrays.asList(
                    "INSERT INTO courses (id, course_name) VALUES(1, 'Functional programming 101')",
                    "INSERT INTO courses (id, course_name) VALUES(2, 'data structures 415')"
            );
        } else if (table.equals("registrations")) {
            statements = Arrays.asList(
                    "INSERT INTO registrations (id, astudent_id, acourse_id) VALUES(1, 1, 2)",
                    "INSERT INTO registrations (id, astudent_id, acourse_id) VALUES(2, 1, 1)",
                    "INSERT INTO registrations (id, astudent_id, acourse_id) VALUES(3, 2, 1)"
            );
        } else if (table.equals("items")) {
            statements = Arrays.asList(

            );
        } else if (table.equals("articles")) {
            statements = Arrays.asList(
                    "INSERT INTO articles (id, title, content) VALUES(1, 'ActiveJDBC basics', 'this is a test content of the article')",
                    "INSERT INTO articles (id, title, content) VALUES(2, 'ActiveJDBC polymorphic associations', 'Polymorphic associations are...')"
            );
        } else if (table.equals("posts")) {
            statements = Arrays.asList(
                    "INSERT INTO posts (id, title, post) VALUES(1, 'Who gets up early in the morning... is tired all day', 'this is to explain that ...sleeping in is actually really good...')",
                    "INSERT INTO posts (id, title, post) VALUES(2, 'Thou shalt not thread', 'Suns strategy for threading inside J2EE is a bit... insane...')"
            );
        } else if (table.equals("comments")) {
            statements = Arrays.asList();
        } else if (table.equals("fruits")) {
            statements = Arrays.asList();
        } else if (table.equals("vegetables")) {
            statements = Arrays.asList();
        } else if (table.equals("plants")) {
            statements = Arrays.asList();
        } else if (table.equals("pages")) {
            statements = Arrays.asList();
        } else if (table.equals("watermelons")) {
            statements = Arrays.asList();
        } else if (table.equals("programmers")) {
            statements = Arrays.asList();
        } else if (table.equals("projects")) {
            statements = Arrays.asList();
        } else if (table.equals("programmers_projects")) {
            statements = Arrays.asList();
        } else if (table.equals("motherboards")) {
            statements = Arrays.asList(
                    "INSERT INTO motherboards (id, description) VALUES(1,'motherboardOne')"
            );
        } else if (table.equals("keyboards")) {
            statements = Arrays.asList(
                    "INSERT INTO keyboards (id, description) VALUES(1,'keyboard-us')"
            );
        } else if (table.equals("computers")) {
            statements = Arrays.asList(
                    "INSERT INTO computers (id, description, mother_id, key_id) VALUES(1,'ComputerX',1,1)"
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
            statements = Arrays.asList(
                    "INSERT INTO nodes VALUES (1, 'Parent', NULL)",
                    "INSERT INTO nodes VALUES (2, 'Self', 1)",
                    "INSERT INTO nodes VALUES (3, 'Sibling', 1)",
                    "INSERT INTO nodes VALUES (4, 'Child', 2)");
        } else {
            statements = Arrays.asList();
        }

        ArrayList<String> all = new ArrayList<String>();
        if (ID_COLUMN != null) {
            all.add("ALTER TABLE " + table + " ALTER COLUMN " + ID_COLUMN + " DROP IDENTITY");
        }
        all.addAll(statements);
        if (ID_COLUMN != null) {
            int statementCount = statements.size();
            all.add("ALTER TABLE " + table + " ALTER COLUMN " + ID_COLUMN + " SET GENERATED ALWAYS AS IDENTITY (start with "+(statementCount+1)+")");
        }
        return all;
    }

    public String getDeleteStatement(String table) {
        return "DELETE FROM " + table;
    }
}
