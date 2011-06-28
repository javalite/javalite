
DROP TABLE IF EXISTS people;
CREATE TABLE people (id serial PRIMARY KEY, name VARCHAR(56) NOT NULL, last_name VARCHAR(56), dob DATE, graduation_date DATE, created_at TIMESTAMP, updated_at TIMESTAMP);

DROP TABLE IF EXISTS accounts;
CREATE TABLE accounts (id  serial PRIMARY KEY, account VARCHAR(56), description VARCHAR(56), amount DECIMAL(10,2), total DECIMAL(10,2));

DROP TABLE IF EXISTS temperatures;
CREATE TABLE temperatures (id  serial PRIMARY KEY, temp SMALLINT);

DROP TABLE IF EXISTS salaries;
CREATE TABLE salaries (id  serial PRIMARY KEY, salary DECIMAL(7, 2));

DROP TABLE IF EXISTS users;
CREATE TABLE users (id  serial PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), email VARCHAR(56));

DROP TABLE IF EXISTS addresses;
CREATE TABLE addresses (id  serial PRIMARY KEY, address1 VARCHAR(56), address2 VARCHAR(56), city VARCHAR(56), state VARCHAR(56), zip VARCHAR(56), user_id INT);

DROP TABLE IF EXISTS legacy_universities;
CREATE TABLE legacy_universities (id  serial PRIMARY KEY, univ_name VARCHAR(56), address1 VARCHAR(56), address2 VARCHAR(56), city VARCHAR(56), state VARCHAR(56), zip VARCHAR(56));

DROP TABLE IF EXISTS libraries;
CREATE TABLE libraries (id  serial PRIMARY KEY, address VARCHAR(56), city VARCHAR(56), state VARCHAR(56));

DROP TABLE IF EXISTS books;
CREATE TABLE books (id  serial PRIMARY KEY, title VARCHAR(56), author VARCHAR(56), isbn VARCHAR(56), lib_id INT);

DROP TABLE IF EXISTS readers;
CREATE TABLE readers (id  serial PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), book_id INT);

DROP TABLE IF EXISTS animals;
CREATE TABLE animals (animal_id  serial PRIMARY KEY, animal_name VARCHAR(56));

DROP TABLE IF EXISTS patients;
CREATE TABLE patients (id  serial PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56));

DROP TABLE IF EXISTS doctors;
CREATE TABLE doctors (id  serial PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), discipline varchar(56));

DROP TABLE IF EXISTS doctors_patients;
CREATE TABLE doctors_patients (id  serial PRIMARY KEY, doctor_id INT, patient_id INT);

DROP TABLE IF EXISTS students;
CREATE TABLE students (id  serial PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), dob DATE);

DROP TABLE IF EXISTS courses;
CREATE TABLE courses (id  serial PRIMARY KEY, course_name VARCHAR(56));

DROP TABLE IF EXISTS registrations;
CREATE TABLE registrations (id  serial PRIMARY KEY, astudent_id INT, acourse_id INT);


DROP TABLE IF EXISTS items;
CREATE TABLE items (id  serial PRIMARY KEY, item_number INT, item_description VARCHAR(56));

DROP TABLE IF EXISTS articles;
CREATE TABLE articles (id  serial PRIMARY KEY, title VARCHAR(56), content TEXT);

DROP TABLE IF EXISTS posts;
CREATE TABLE posts (id  serial PRIMARY KEY, title VARCHAR(56), post TEXT);

DROP TABLE IF EXISTS comments;
CREATE TABLE comments (id  serial PRIMARY KEY, author VARCHAR(56), content TEXT, parent_id INT, parent_type VARCHAR(256));

DROP TABLE IF EXISTS fruits;
CREATE TABLE fruits (id  serial PRIMARY KEY, fruit_name VARCHAR(56), category VARCHAR(56), created_at TIMESTAMP, updated_at TIMESTAMP);

DROP TABLE IF EXISTS vegetables;
CREATE TABLE vegetables (id  serial PRIMARY KEY, vegetable_name VARCHAR(56), category VARCHAR(56), created_at TIMESTAMP, updated_at TIMESTAMP);

DROP TABLE IF EXISTS plants;
CREATE TABLE plants (id  serial PRIMARY KEY, plant_name VARCHAR(56), category VARCHAR(56), created_at TIMESTAMP, updated_at TIMESTAMP);

DROP TABLE IF EXISTS pages;
CREATE TABLE pages ( id serial PRIMARY KEY, description VARCHAR(56), word_count INT);


DROP TABLE IF EXISTS watermelons;
CREATE TABLE watermelons ( id serial PRIMARY KEY, melon_type VARCHAR(56), record_version INT, created_at TIMESTAMP, updated_at TIMESTAMP);

DROP TABLE IF EXISTS schools;
CREATE TABLE schools ( id serial PRIMARY KEY, school_name VARCHAR(56), school_type VARCHAR(56), email VARCHAR(56), created_at TIMESTAMP, updated_at TIMESTAMP);

DROP TABLE IF EXISTS dual;
CREATE TABLE dual ( id serial PRIMARY KEY, next_val BIGINT);
INSERT INTO dual (next_val) VALUES (0);

DROP TABLE IF EXISTS programmers;
CREATE TABLE programmers ( id serial PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), email VARCHAR(56), created_at TIMESTAMP, updated_at TIMESTAMP);

DROP TABLE IF EXISTS projects;
CREATE TABLE projects ( id serial PRIMARY KEY, project_name VARCHAR(56), created_at TIMESTAMP, updated_at TIMESTAMP);

DROP TABLE IF EXISTS programmers_projects;
CREATE TABLE programmers_projects ( id serial PRIMARY KEY, duration_weeks INT, project_id INT, programmer_id INT, created_at TIMESTAMP, updated_at TIMESTAMP);


DROP TABLE IF EXISTS computers;
CREATE TABLE computers ( id serial PRIMARY KEY, description VARCHAR(56), mother_id INT, key_id INT);

DROP TABLE IF EXISTS keyboards;
CREATE TABLE keyboards ( id serial PRIMARY KEY, description VARCHAR(56));

DROP TABLE IF EXISTS motherboards;
CREATE TABLE motherboards ( id serial PRIMARY KEY, description VARCHAR(56));



DROP TABLE IF EXISTS ingredients;
CREATE TABLE ingredients (ingredient_id  serial PRIMARY KEY, ingredient_name VARCHAR(56));

DROP TABLE IF EXISTS recipes;
CREATE TABLE recipes (recipe_id  serial PRIMARY KEY, recipe_name VARCHAR(56));

DROP TABLE IF EXISTS ingredients_recipes;
CREATE TABLE ingredients_recipes (the_id  serial PRIMARY KEY, recipe_id INT, ingredient_id INT);


DROP TABLE IF EXISTS vehicles;
CREATE TABLE vehicles (id  serial PRIMARY KEY, name VARCHAR(56));

DROP TABLE IF EXISTS mammals;
CREATE TABLE mammals (id  serial PRIMARY KEY, name VARCHAR(56));

DROP TABLE IF EXISTS classifications;
CREATE TABLE classifications (id  serial PRIMARY KEY, name VARCHAR(56), parent_id INT, parent_type VARCHAR(56));

