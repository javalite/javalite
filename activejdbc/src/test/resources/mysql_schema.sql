
DROP TABLE IF EXISTS people;
CREATE TABLE people (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, name VARCHAR(56), last_name VARCHAR(56), dob DATE, graduation_date DATE, created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS accounts;
CREATE TABLE accounts (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, account VARCHAR(56), description VARCHAR(56), amount DECIMAL(10,2), total DECIMAL(10,2));

DROP TABLE IF EXISTS temperatures;
CREATE TABLE temperatures (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, temp SMALLINT);

DROP TABLE IF EXISTS salaries;
CREATE TABLE salaries (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, salary DECIMAL(7, 2));

DROP TABLE IF EXISTS users;
CREATE TABLE users (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), email VARCHAR(56));

DROP TABLE IF EXISTS addresses;
CREATE TABLE addresses (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, address1 VARCHAR(56), address2 VARCHAR(56), city VARCHAR(56), state VARCHAR(56), zip VARCHAR(56), user_id int(11));

DROP TABLE IF EXISTS legacy_universities;
CREATE TABLE legacy_universities (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, univ_name VARCHAR(56), address1 VARCHAR(56), address2 VARCHAR(56), city VARCHAR(56), state VARCHAR(56), zip VARCHAR(56));

DROP TABLE IF EXISTS libraries;
CREATE TABLE libraries (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, address VARCHAR(56), city VARCHAR(56), state VARCHAR(56));

DROP TABLE IF EXISTS books;
CREATE TABLE books (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, title VARCHAR(56), author VARCHAR(56), isbn VARCHAR(56), lib_id int(11));

DROP TABLE IF EXISTS readers;
CREATE TABLE readers (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), book_id int(11));

DROP TABLE IF EXISTS animals;
CREATE TABLE animals (animal_id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, animal_name VARCHAR(56));

DROP TABLE IF EXISTS patients;
CREATE TABLE patients (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56));

DROP TABLE IF EXISTS doctors;
CREATE TABLE doctors (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), discipline varchar(56));

DROP TABLE IF EXISTS doctors_patients;
CREATE TABLE doctors_patients (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, doctor_id int(11), patient_id int(11));

DROP TABLE IF EXISTS students;
CREATE TABLE students (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), dob DATE);

DROP TABLE IF EXISTS courses;
CREATE TABLE courses (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, course_name VARCHAR(56));

DROP TABLE IF EXISTS registrations;
CREATE TABLE registrations (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, astudent_id int(11), acourse_id int(11));


DROP TABLE IF EXISTS items;
CREATE TABLE items (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, item_number int(11), item_description VARCHAR(56));

DROP TABLE IF EXISTS articles;
CREATE TABLE articles (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, title VARCHAR(56), content TEXT);

DROP TABLE IF EXISTS posts;
CREATE TABLE posts (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, title VARCHAR(56), post TEXT);

DROP TABLE IF EXISTS comments;
CREATE TABLE comments (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, author VARCHAR(56), content TEXT, parent_id int(11), parent_type VARCHAR(256));

DROP TABLE IF EXISTS fruits;
CREATE TABLE fruits (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, fruit_name VARCHAR(56), category VARCHAR(56), created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS vegetables;
CREATE TABLE vegetables (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, vegetable_name VARCHAR(56), category VARCHAR(56), created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS plants;
CREATE TABLE plants (id  int(11) DEFAULT NULL auto_increment PRIMARY KEY, plant_name VARCHAR(56), category VARCHAR(56), created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS pages;
CREATE TABLE pages ( id int(11) DEFAULT NULL auto_increment PRIMARY KEY, description VARCHAR(56));