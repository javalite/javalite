package org.javalite.activejdbc.statement_providers;

import org.javalite.activejdbc.StatementProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.javalite.common.Collections.list;

/**
 * @author Igor Polevoy
 */
public class H2StatementProvider implements StatementProvider {
    public List<String> getPopulateStatements(String table) {

        List<String> statements = new ArrayList<>();
        if (table.equals("people")) {
            statements =  Arrays.asList(
                    "INSERT INTO people (name, last_name, dob, graduation_date, created_at, updated_at) VALUES('John', 'Smith', '1934-12-01', '1954-12-01', now(), now());",
                    "INSERT INTO people (name, last_name, dob, graduation_date, created_at, updated_at) values('Leylah', 'Jonston', '1954-04-03', '1974-04-03', now(), now());",
                    "INSERT INTO people (name, last_name, dob, graduation_date, created_at, updated_at) values('Muhammad', 'Ali', '1943-01-04', '1963-01-04', now(), now());",
                    "INSERT INTO people (name, last_name, dob, graduation_date, created_at, updated_at) values('Joe', 'Pesci', '1944-02-23','1964-02-23', now(), now());"
            );
        } else if (table.equals("accounts")) {
            statements =  Arrays.asList(
                    "INSERT INTO accounts ( account, description, amount, total) VALUES('123', 'checking', 9999.99, 1234.32);"
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
                    "INSERT INTO addresses (address1, address2, city, state, zip, user_id) VALUES('23 Grove St.', 'apt 32', 'Springfield', 'IL', '60606', 1);",
                    "INSERT INTO addresses (address1, address2, city, state, zip, user_id) VALUES('143 Madison St.', 'apt 34', 'Springfield', 'IL', '60606', 2);",
                    "INSERT INTO addresses (address1, address2, city, state, zip, user_id) VALUES('153 Creek St.', 'apt 35', 'Springfield', 'IL', '60606', 2);",
                    "INSERT INTO addresses (address1, address2, city, state, zip, user_id) VALUES('163 Gorge St.', 'apt 36', 'Springfield', 'IL', '60606', 2);",
                    "INSERT INTO addresses (address1, address2, city, state, zip, user_id) VALUES('173 Far Side.', 'apt 37', 'Springfield', 'IL', '60606', 2);"
            );
        } else if (table.equals("rooms")) {
            statements =  Arrays.asList(
                    "INSERT INTO rooms (name, address_id) VALUES('bathroom', 1);",
                    "INSERT INTO rooms (name, address_id) VALUES('conference room', 1);",
                    "INSERT INTO rooms (name, address_id) VALUES('ball room', 7);",
                    "INSERT INTO rooms (name, address_id) VALUES('basement', 7);"
            );
        } else if (table.equals("legacy_universities")) {
            statements =  list(
                    "INSERT INTO legacy_universities  (univ_name, address1, address2, city, state, zip) VALUES('DePaul', '123 Pine St.', 'apt 3B', 'Springfield', 'IL', '60606');"
            );
        } else if (table.equals("libraries")) {
            statements =  list(
                    "INSERT INTO libraries (address, city, state) VALUES('124 Pine Street', 'St. Raphael', 'California');",
                    "INSERT INTO libraries (address, city, state) VALUES('345 Burlington Blvd', 'Springfield', 'Il');"
            );
        } else if (table.equals("books")) {
            statements =  Arrays.asList(
                    "INSERT INTO books (title, author, isbn, lib_id) VALUES('All Quiet on Western Front', 'Eric Remarque', '123', 1);",
                    "INSERT INTO books (title, author, isbn, lib_id) VALUES('12 Chairs', 'Ilf, Petrov', '122', 1);"
            );
        } else if (table.equals("readers")) {
            statements =  Arrays.asList(
                    "INSERT INTO readers (first_name, last_name, book_id) VALUES('John', 'Smith', 1);",
                    "INSERT INTO readers (first_name, last_name, book_id) VALUES('John', 'Doe', 1);",
                    "INSERT INTO readers (first_name, last_name, book_id) VALUES('Igor', 'Polevoy', 2);"
            );
        } else if (table.equals("animals")) {
            statements =  Arrays.asList(
                    "INSERT INTO animals (animal_name) VALUES('frog');"
            );
        } else if (table.equals("patients")) {
            statements =  Arrays.asList(
                    "INSERT INTO patients (first_name, last_name) VALUES('Jim', 'Cary');",
                    "INSERT INTO patients (first_name, last_name) VALUES('John', 'Carpenter');",
                    "INSERT INTO patients (first_name, last_name) VALUES('John', 'Krugg');"
            );
        } else if (table.equals("patient_cards")) {
            statements = Arrays.asList(
                    "INSERT INTO patient_cards (info, patient_id) VALUES('Jim', 1);",
                    "INSERT INTO patient_cards (info, patient_id) VALUES('John', 2);",
                    "INSERT INTO patient_cards (info, patient_id) VALUES('John', 3);"
            );
        } else if (table.equals("prescriptions")) {
            statements =  Arrays.asList(
                    "INSERT INTO prescriptions (name, patient_id, doctor_id) VALUES('Viagra', 1, 1);",
                    "INSERT INTO prescriptions (name, patient_id, doctor_id)  VALUES('Prozac', 1, 2);",
                    "INSERT INTO prescriptions (name, patient_id, doctor_id) VALUES('Valium', 2, 1);",
                    "INSERT INTO prescriptions (name, patient_id, doctor_id) VALUES('Marijuana (medicinal) ', 2, 1);",
                    "INSERT INTO prescriptions (name, patient_id, doctor_id) VALUES('CML treatment', 3, 3);"
            );
        } else if (table.equals("doctors")) {
            statements =  Arrays.asList(
                    "INSERT INTO doctors (first_name, last_name, discipline) VALUES('John', 'Doe', 'otolaryngology');",
                    "INSERT INTO doctors (first_name, last_name, discipline) VALUES('Hellen', 'Hunt', 'dentistry');",
                    "INSERT INTO doctors (first_name, last_name, discipline) VALUES('John', 'Druker', 'oncology');",
                    "INSERT INTO doctors (first_name, last_name, discipline) VALUES('Henry', 'Jekyll', 'pathology');"
            );
        } else if (table.equals("doctors_patients")) {
            statements =  Arrays.asList(
                    "INSERT INTO doctors_patients (doctor_id, patient_id) VALUES(1, 2);",
                    "INSERT INTO doctors_patients (doctor_id, patient_id) VALUES(1, 1);",
                    "INSERT INTO doctors_patients (doctor_id, patient_id) VALUES(2, 1);",
                    "INSERT INTO doctors_patients (doctor_id, patient_id) VALUES(3, 3);"            );
        } else if (table.equals("students")) {
            statements =  Arrays.asList(
                    "INSERT INTO students (first_name, last_name, dob, enrollment_date) VALUES ('Jim', 'Cary', '1965-12-01', '1973-01-20 11:00:00');",
                    "INSERT INTO students (first_name, last_name, dob, enrollment_date) VALUES ('John', 'Carpenter', '1979-12-01', '1987-01-29 13:00:00');"
            );
        } else if (table.equals("courses")) {
            statements =  Arrays.asList(
                    "INSERT INTO courses (course_name) VALUES('Functional programming 101');",
                    "INSERT INTO courses (course_name) VALUES('data structures 415');"
            );
        } else if (table.equals("registrations")) {
            statements =  Arrays.asList(
                    "INSERT INTO registrations (astudent_id, acourse_id ) VALUES(1, 2);",
                    "INSERT INTO registrations (astudent_id, acourse_id ) VALUES(1, 1);",
                    "INSERT INTO registrations (astudent_id, acourse_id ) VALUES(2, 1);"
            );
        } else if (table.equals("items")) {
            statements =  Arrays.asList(

            );
        } else if (table.equals("articles")) {
            statements =  Arrays.asList(
                    "INSERT INTO articles ( title, content ) VALUES('ActiveJDBC basics', 'this is a test content of the article');",
                    "INSERT INTO articles ( title, content ) VALUES('ActiveJDBC polymorphic associations', 'Polymorphic associations are...');"
            );
        } else if (table.equals("posts")) {
            statements =  Arrays.asList(
                    "INSERT INTO posts (title, post) VALUES('Who gets up early in the morning... is tired all day', 'this is to explain that ...sleeping in is actually really good...');",
                    "INSERT INTO posts (title, post) VALUES('Thou shalt not thread', 'Suns strategy for threading inside J2EE is a bit... insane...');"
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
            statements =  Arrays.asList();
        } else if (table.equals("watermelons")) {
            statements =  Arrays.asList();
        } else if (table.equals("programmers")) {
            statements =  Arrays.asList();
        } else if (table.equals("projects")) {
            statements =  Arrays.asList();
        } else if (table.equals("programmers_projects")) {
            statements =  Arrays.asList();
        } else if (table.equals("motherboards")){
        	statements =  Arrays.asList(
                    "INSERT INTO motherboards (description) VALUES('motherboardOne');"
            );
        } else if (table.equals("keyboards")){
        	statements =  Arrays.asList(
                    "INSERT INTO keyboards (description) VALUES('keyboard-us');"
            );
        } else if (table.equals("computers")){
        	statements =  Arrays.asList(
                    "INSERT INTO computers (description, mother_id, key_id) VALUES('ComputerX',1,1);"
            );
        }else if (table.equals("ingredients_recipes")) {
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
                    "INSERT INTO nodes (name, parent_id) VALUES ('Parent', NULL);",
                    "INSERT INTO nodes (name, parent_id) VALUES ('Self', 1);",
                    "INSERT INTO nodes (name, parent_id) VALUES ('Sibling', 1);",
                    "INSERT INTO nodes (name, parent_id) VALUES ('Child', 2);");
        } else if (table.equals("teams")) {
          statements =  Arrays.asList(
              "INSERT INTO teams (name) VALUES ('New England Patriots');",
              "INSERT INTO teams (name) VALUES ('Philadelphia Eagles');"
          );
        } else if (table.equals("players")) {
          statements =  Arrays.asList(
              "INSERT INTO players (first_name, last_name, team_id) VALUES ('Tom', 'Brady', 1);",
              "INSERT INTO players (first_name, last_name, team_id) VALUES ('Dany', 'Amendola', 1);",
              "INSERT INTO players (first_name, last_name, team_id) VALUES ('Nick', 'Foles', 2);",
              "INSERT INTO players (first_name, last_name, team_id) VALUES ('Trey', 'Burton', 2);"
          );
        } else if (table.equals("bands")) {
            statements = Arrays.asList();
        } else if (table.equals("genres")) {
            statements = Arrays.asList();
        } else if (table.equals("musicians")) {
            statements = Arrays.asList();
        } else if (table.equals("bands_genres")) {
            statements = Arrays.asList();
        } else if (table.equals("bands_musicians")) {
            statements = Arrays.asList();
        } else {
            statements = Arrays.asList();
        }

        ArrayList<String> all = new ArrayList<>();

        //https://groups.google.com/forum/#!searchin/h2-database/reset$20auto_increment/h2-database/PqkE1-tK_M4/I7MBEpHOZFQJ
        if(table.equals("animals")){
            all.add("ALTER TABLE " + table + " ALTER COLUMN animal_id RESTART WITH 1;");
        } else if (table.equals("teams")) {
            all.add("ALTER TABLE " + table + " ALTER COLUMN team_id RESTART WITH 1;");
        } else if (table.equals("customers")) {
            all.add("ALTER TABLE " + table + " ALTER COLUMN customer_id RESTART WITH 1;");
        } else {
            all.add("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH 1;");
        }
        all.addAll(statements);
        return all;
    }

    public String getDeleteStatement(String table){
        return "DELETE FROM " + table + ";";
    }
}
