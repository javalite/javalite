package org.javalite.activejdbc.statement_providers;

import org.javalite.activejdbc.StatementProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Polevoy
 */
public class OracleStatementProvider implements StatementProvider {
    @Override
    public List<String> getPopulateStatements(String table) {
        List<String> statements = new ArrayList<>();
        if (table.equals("people")) {
            statements =  Arrays.asList(
                    "INSERT INTO people (id, name, last_name, dob, graduation_date, created_at, updated_at) VALUES(1, 'John', 'Smith', '01-Dec-1934', '01-Dec-1954', SYSDATE, SYSDATE)",
                    "INSERT INTO people (id, name, last_name, dob, graduation_date, created_at, updated_at) VALUES(2, 'Leylah', 'Jonston', '03-Apr-1954', '03-Apr-1974', SYSDATE, SYSDATE)",
                    "INSERT INTO people (id, name, last_name, dob, graduation_date, created_at, updated_at) VALUES(3, 'Muhammad', 'Ali', '04-Jan-1943', '04-Jan-1963', SYSDATE, SYSDATE)",
                    "INSERT INTO people (id, name, last_name, dob, graduation_date, created_at, updated_at) VALUES(4, 'Joe', 'Pesci', '23-Feb-1944', '23-Feb-1964', SYSDATE, SYSDATE)"

            );
        } else if (table.equals("accounts")) {
            statements =  Arrays.asList(
                    "INSERT INTO accounts VALUES(1, '123', 'checking', 9999.99, 1234.32)"
            );
        } else if (table.equals("temperatures")) {
            statements =  Arrays.asList(
                    "INSERT INTO temperatures VALUES(1, 30)"
            );
        } else if (table.equals("salaries")) {
            statements =  Arrays.asList(
                    "INSERT INTO salaries VALUES(1, 50000.00)",
                    "INSERT INTO salaries VALUES(2, 0)"
            );
        } else if (table.equals("users")) {
            statements =  Arrays.asList(
                    "INSERT INTO users VALUES(1, 'Marilyn', 'Monroe', 'mmonroe@yahoo.com')",
                    "INSERT INTO users VALUES(2, 'John', 'Doe', 'jdoe@gmail.com')",
                    "INSERT INTO users VALUES(3, 'James', 'Dean', 'jdean@hotmail.com')"
            );
        } else if (table.equals("addresses")) {
            statements =  Arrays.asList(
                    "INSERT INTO addresses VALUES(1, '123 Pine St.', 'apt 31', 'Springfield', 'IL', '60606', 1)",
                    "INSERT INTO addresses VALUES(2, '456 Brook St.', 'apt 21', 'Springfield', 'IL', '60606', 1)",
                    "INSERT INTO addresses VALUES(3, '23 Grove St.', 'apt 32', 'Springfield', 'IL', '60606', 1)",
                    "INSERT INTO addresses VALUES(4, '143 Madison St.', 'apt 34', 'Springfield', 'IL', '60606', 2)",
                    "INSERT INTO addresses VALUES(5, '153 Creek St.', 'apt 35', 'Springfield', 'IL', '60606', 2)",
                    "INSERT INTO addresses VALUES(6, '163 Gorge St.', 'apt 36', 'Springfield', 'IL', '60606', 2)",
                    "INSERT INTO addresses VALUES(7, '173 Far Side.', 'apt 37', 'Springfield', 'IL', '60606', 2)"
            );
        } else if (table.equals("rooms")) {
            statements =  Arrays.asList(
                    "INSERT INTO rooms VALUES(1, 'bathroom', 1)",
                    "INSERT INTO rooms VALUES(2, 'conference room', 1)",
                    "INSERT INTO rooms VALUES(3, 'ball room', 7)",
                    "INSERT INTO rooms VALUES(4, 'basement', 7)"
            );

        } else if (table.equals("legacy_universities")) {
            statements =  Arrays.asList(
                    "INSERT INTO legacy_universities  VALUES(1, 'DePaul', '123 Pine St.', 'apt 3B', 'Springfield', 'IL', '60606')"
            );
        } else if (table.equals("libraries")) {
            statements =  Arrays.asList(

                    "INSERT INTO libraries VALUES(1, '124 Pine Street', 'St. Raphael', 'California')",
                    "INSERT INTO libraries VALUES(2, '345 Burlington Blvd', 'Springfield', 'Il')"
            );
        } else if (table.equals("books")) {
            statements =  Arrays.asList(
                    "INSERT INTO books VALUES(1, 'All Quiet on Western Front', 'Eric Remarque', '123', 1)",
                    "INSERT INTO books VALUES(2, '12 Chairs', 'Ilf, Petrov', '122', 1)"
            );
        } else if (table.equals("readers")) {
            statements =  Arrays.asList(
                    "INSERT INTO readers VALUES(1, 'John', 'Smith', 1)",
                    "INSERT INTO readers VALUES(2, 'John', 'Doe', 1)",
                    "INSERT INTO readers VALUES(3, 'Igor', 'Polevoy', 2)"
            );
        } else if (table.equals("animals")) {
            statements =  Arrays.asList(
                    "INSERT INTO animals VALUES(1, 'frog')"
            );
        } else if (table.equals("patients")) {
            statements =  Arrays.asList(
                    "INSERT INTO patients VALUES(1, 'Jim', 'Cary')",
                    "INSERT INTO patients VALUES(2, 'John', 'Carpenter')",
                    "INSERT INTO patients VALUES(3, 'John', 'Krugg')"
            );
        } else if (table.equals("prescriptions")) {
            statements =  Arrays.asList(
                    "INSERT INTO prescriptions VALUES(1, 'Viagra', 1)",
                    "INSERT INTO prescriptions VALUES(2, 'Prozac', 1)",
                    "INSERT INTO prescriptions VALUES(3, 'Valium', 2)",
                    "INSERT INTO prescriptions VALUES(4, 'Marijuana (medicinal) ', 2)",
                    "INSERT INTO prescriptions VALUES(5, 'CML treatment', 3)"
            );
        } else if (table.equals("doctors")) {
            statements =  Arrays.asList(
                    "INSERT INTO doctors VALUES(1, 'John', 'Doe', 'otolaryngology')",
                    "INSERT INTO doctors VALUES(2, 'Hellen', 'Hunt', 'dentistry')",
                    "INSERT INTO doctors VALUES(3, 'John', 'Druker', 'oncology')",
                    "INSERT INTO doctors VALUES(4, 'Henry', 'Jekyll', 'pathology')"
            );
        } else if (table.equals("doctors_patients")) {
            statements =  Arrays.asList(
                    "INSERT INTO doctors_patients VALUES(1, 1, 2)",
                    "INSERT INTO doctors_patients VALUES(2, 1, 1)",
                    "INSERT INTO doctors_patients VALUES(3, 2, 1)",
                    "INSERT INTO doctors_patients VALUES(4, 3, 3)"
            );
        } else if (table.equals("students")) {
            statements =  Arrays.asList(
                    "INSERT INTO students (id, first_name, last_name, dob, enrollment_date) VALUES (1, 'Jim', 'Cary', DATE '1965-12-01', TIMESTAMP '1973-01-20 11:00:00')",
                    "INSERT INTO students (id, first_name, last_name, dob, enrollment_date) VALUES (2, 'John', 'Carpenter', DATE '1979-12-01', TIMESTAMP '1987-01-29 13:00:00')"
            );
        } else if (table.equals("courses")) {
            statements =  Arrays.asList(
                    "INSERT INTO courses  VALUES(1, 'Functional programming 101')",
                    "INSERT INTO courses  VALUES(2, 'data structures 415')"
            );
        } else if (table.equals("registrations")) {
            statements =  Arrays.asList(
                    "INSERT INTO registrations VALUES(1, 1, 2)",
                    "INSERT INTO registrations VALUES(2, 1, 1)",
                    "INSERT INTO registrations VALUES(3, 2, 1)"
            );
        } else if (table.equals("items")) {
            statements =  Arrays.asList(

            );
        } else if (table.equals("articles")) {
            statements =  Arrays.asList(
                    "INSERT INTO articles VALUES(1, 'ActiveJDBC basics', 'this is a test content of the article')",
                    "INSERT INTO articles VALUES(2, 'ActiveJDBC polymorphic associations', 'Polymorphic associations are...')"
            );
        } else if (table.equals("posts")) {
            statements =  Arrays.asList(
                    "INSERT INTO posts VALUES(1, 'Who gets up early in the morning... is tired all day', 'this is to explain that ...sleeping in is actually really good...')",
                    "INSERT INTO posts VALUES(2, 'Thou shalt not thread', 'Suns strategy for threading inside J2EE is a bit... insane...')"
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
        }else if (table.equals("motherboards")){
        	statements =  Arrays.asList(
                    "INSERT INTO motherboards VALUES(1,'motherboardOne')"
            );
        }else if (table.equals("keyboards")){
        	statements =  Arrays.asList(
                    "INSERT INTO keyboards VALUES(1,'keyboard-us')"
            );
        } else if (table.equals("computers")){
        	statements =  Arrays.asList(
                    "INSERT INTO computers VALUES(1,'ComputerX',1,1)"
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
                    "INSERT INTO nodes VALUES (1, 'Parent', NULL)",
                    "INSERT INTO nodes VALUES (2, 'Self', 1)",
                    "INSERT INTO nodes VALUES (3, 'Sibling', 1)",
                    "INSERT INTO nodes VALUES (4, 'Child', 2)");
        } else{
            statements = Arrays.asList();
        }

        ArrayList<String> all = new ArrayList<>();
        all.add("DROP TRIGGER " + table + "_trigger");
        all.add("DROP SEQUENCE " + table + "_seq");
        all.add("CREATE  SEQUENCE " + table + "_seq START WITH 1 INCREMENT BY 1" );
        if(table.equals("animals")){
            all.add("CREATE OR REPLACE TRIGGER animals_trigger\n" +
                    "    BEFORE INSERT ON animals REFERENCING\n" +
                    "    NEW AS new\n" +
                    "    OLD AS old\n" +
                    "    FOR EACH ROW\n" +
                    "    begin\n" +
                    "select coalesce(:new.animal_id, animals_seq.nextval) into :new.animal_id from dual;\n" +
                    "end;");
        }else{
        all.add("CREATE OR REPLACE TRIGGER " + table + "_trigger\n" +
                "    BEFORE INSERT ON " + table + " REFERENCING\n" +
                "    NEW AS new\n" +
                "    OLD AS old\n" +
                "    FOR EACH ROW\n" +
                "    begin\n" +
                "select coalesce(:new.id, " + table + "_seq.nextval) into :new.id from dual;\n" +
                "end;");
        }
        all.addAll(statements);
        return all;
    }

    @Override
    public String getDeleteStatement(String table){
        return "DELETE FROM " + table;
    }
}
