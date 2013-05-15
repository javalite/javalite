
--SQL for MySQL
DROP TABLE IF EXISTS employees;
CREATE TABLE employees (
      id  int(11) NOT NULL  auto_increment PRIMARY KEY,
      first_name VARCHAR(56),
      last_name VARCHAR(56)
  );


--SQL for Oracle - execute one statement at the time.
CREATE TABLE students (id  NUMBER NOT NULL, first_name VARCHAR(56), last_name VARCHAR(56), dob DATE);

ALTER TABLE students ADD CONSTRAINT students_pk PRIMARY KEY ( id );

CREATE SEQUENCE students_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER students_trigger
    BEFORE INSERT ON students REFERENCING
    NEW AS new
    OLD AS old
    FOR EACH ROW
    begin
select coalesce(:new.id, students_seq.nextval) into :new.id from dual;
end;


