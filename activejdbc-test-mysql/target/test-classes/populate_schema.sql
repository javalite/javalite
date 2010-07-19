
DROP TABLE IF EXISTS people;
CREATE TABLE people (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, name VARCHAR(56), last_name VARCHAR(56), dob DATE, graduation_date DATE, created_at DATETIME, updated_at DATETIME);
INSERT INTO people (id, name, last_name, dob, graduation_date, created_at, updated_at) VALUES(1, 'John', 'Smith', '1934-12-01', '1954-12-01', now(), now());
insert into people (id, name, last_name, dob, graduation_date, created_at, updated_at) values(2, 'Leylah', 'Jonston', '1954-04-03', '1974-04-03', now(), now());
insert into people (id, name, last_name, dob, graduation_date, created_at, updated_at) values(3, 'Muhammad', 'Ali', '1943-01-04', '1963-01-04', now(), now());
insert into people (id, name, last_name, dob, graduation_date, created_at, updated_at) values(4, 'Joe', 'Pesci', '1944-02-23','1964-02-23', now(), now());


DROP TABLE IF EXISTS accounts;
CREATE TABLE accounts (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, account VARCHAR(56), description VARCHAR(56), amount DECIMAL(10,2), total DECIMAL(10,2));
INSERT INTO accounts VALUES(1, '123', 'checking', 9999.99, 1234.32);


DROP TABLE IF EXISTS temperatures;
CREATE TABLE temperatures (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, temp SMALLINT);
INSERT INTO temperatures VALUES(1, 30);


DROP TABLE IF EXISTS salaries;
CREATE TABLE salaries (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, salary DECIMAL(7, 2));
INSERT INTO salaries VALUES(1, 50000.00);



DROP TABLE IF EXISTS users;
CREATE TABLE users (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), email VARCHAR(56));
INSERT INTO users VALUES(1, 'Marilyn', 'Monroe', 'mmonroe@yahoo.com');
INSERT INTO users VALUES(2, 'John', 'Doe', 'jdoe@gmail.com');


DROP TABLE IF EXISTS addresses;
CREATE TABLE addresses (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, address1 VARCHAR(56), address2 VARCHAR(56), city VARCHAR(56), state VARCHAR(56), zip VARCHAR(56), user_id int(11));
INSERT INTO addresses VALUES(1, '123 Pine St.', 'apt 31', 'Springfield', 'IL', '60606', 1);
INSERT INTO addresses VALUES(2, '456 Brook St.', 'apt 21', 'Springfield', 'IL', '60606', 1);
INSERT INTO addresses VALUES(3, '23 Grove St.', 'apt 32', 'Springfield', 'IL', '60606', 1);
INSERT INTO addresses VALUES(4, '143 Madison St.', 'apt 34', 'Springfield', 'IL', '60606', 2);
INSERT INTO addresses VALUES(5, '153 Creek St.', 'apt 35', 'Springfield', 'IL', '60606', 2);
INSERT INTO addresses VALUES(6, '163 Gorge St.', 'apt 36', 'Springfield', 'IL', '60606', 2);
INSERT INTO addresses VALUES(7, '173 Far Side.', 'apt 37', 'Springfield', 'IL', '60606', 2);



DROP TABLE IF EXISTS legacy_universities;
CREATE TABLE legacy_universities (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, univ_name VARCHAR(56), address1 VARCHAR(56), address2 VARCHAR(56), city VARCHAR(56), state VARCHAR(56), zip VARCHAR(56));
INSERT INTO legacy_universities  VALUES(1, 'DePaul', '123 Pine St.', 'apt 3B', 'Springfield', 'IL', '60606');



DROP TABLE IF EXISTS libraries;
CREATE TABLE libraries (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, address VARCHAR(56), city VARCHAR(56), state VARCHAR(56));
INSERT INTO libraries VALUES(1, '124 Pine Street', 'St. Raphael', 'California');
INSERT INTO libraries VALUES(2, '345 Burlington Blvd', 'Springfield', 'Il');


DROP TABLE IF EXISTS books;
CREATE TABLE books (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, title VARCHAR(56), author VARCHAR(56), isbn VARCHAR(56), lib_id int(11));
INSERT INTO books VALUES(1, 'All Quiet on Western Front', 'Eric Remarque', '123', 1);
INSERT INTO books VALUES(2, '12 Chairs', 'Ilf, Petrov', '122', 1);

DROP TABLE IF EXISTS readers;
CREATE TABLE readers (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), book_id int(11));
INSERT INTO readers VALUES(1, 'John', 'Smith', 1);
INSERT INTO readers VALUES(2, 'John', 'Doe', 1);
INSERT INTO readers VALUES(3, 'Igor', 'Polevoy', 2);



DROP TABLE IF EXISTS animals;
CREATE TABLE animals (animal_id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, animal_name VARCHAR(56));
INSERT INTO animals VALUES(1, 'frog');




DROP TABLE IF EXISTS patients;
CREATE TABLE patients (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56));
INSERT INTO patients VALUES(1, 'Jim', 'Cary');
INSERT INTO patients VALUES(2, 'John', 'Carpenter');

DROP TABLE IF EXISTS doctors;
CREATE TABLE doctors (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), discipline varchar(56));
INSERT INTO doctors VALUES(1, 'John', 'Doe', 'otholaringology');
INSERT INTO doctors VALUES(2, 'Hellen', 'Hunt', 'dentistry');


DROP TABLE IF EXISTS doctors_patients;
CREATE TABLE doctors_patients (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, doctor_id int(11), patient_id int(11));
INSERT INTO doctors_patients VALUES(1, 1, 2);
INSERT INTO doctors_patients VALUES(2, 1, 1);
INSERT INTO doctors_patients VALUES(3, 2, 1);



DROP TABLE IF EXISTS students;
CREATE TABLE students (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), dob DATE);
INSERT INTO students VALUES(1, 'Jim', 'Cary', '1965-12-01');
INSERT INTO students VALUES(2, 'John', 'Carpenter', '1979-12-01');

DROP TABLE IF EXISTS courses;
CREATE TABLE courses (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, course_name VARCHAR(56));
INSERT INTO courses  VALUES(1, 'Functional programming 101');
INSERT INTO courses  VALUES(2, 'data structures 415');


DROP TABLE IF EXISTS registrations;
CREATE TABLE registrations (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, astudent_id int(11), acourse_id int(11));
INSERT INTO registrations VALUES(1, 1, 2);
INSERT INTO registrations VALUES(2, 1, 1);
INSERT INTO registrations VALUES(3, 2, 1);



DROP TABLE IF EXISTS items;
CREATE TABLE items (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, item_number int(11), item_description VARCHAR(56));





DROP TABLE IF EXISTS articles;
CREATE TABLE articles (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, title VARCHAR(56), content TEXT);
INSERT INTO articles VALUES(1, 'ActiveJDBC basics', 'this is a test content of the article');
INSERT INTO articles VALUES(2, 'ActiveJDBC plimorphic associations', 'Pollymorphic associations are...');
--
--
DROP TABLE IF EXISTS posts;
CREATE TABLE posts (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, title VARCHAR(56), post TEXT);
INSERT INTO posts VALUES(1, 'Who gets up early in the morning... is tired all day', 'this is to explain that ...sleeping in is actually really good...');
INSERT INTO posts VALUES(2, 'Thou shalt not thread', 'Sun\'s strategy for threading inside J2EE is a bit... insane...');
--
--
DROP TABLE IF EXISTS comments;
CREATE TABLE comments (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, author VARCHAR(56), comment TEXT, parent_id int(11), parent_type VARCHAR(256));


DROP TABLE IF EXISTS fruits;
CREATE TABLE fruits (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, fruit_name VARCHAR(56), category VARCHAR(56), created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS vegetables;
CREATE TABLE vegetables (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, vegetable_name VARCHAR(56), category VARCHAR(56), created_at DATETIME, updated_at DATETIME);


DROP TABLE IF EXISTS plants;
CREATE TABLE plants (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, plant_name VARCHAR(56), category VARCHAR(56), created_at DATETIME, updated_at DATETIME);
