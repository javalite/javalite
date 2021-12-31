-- noinspection SqlNoDataSourceInspectionForFile
DROP TABLE IF EXISTS people;
CREATE TABLE people (id  int auto_increment PRIMARY KEY, name VARCHAR(56) NOT NULL, last_name VARCHAR(56), dob DATE, graduation_date DATE, created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS accounts;
CREATE TABLE accounts (id  int NOT NULL auto_increment PRIMARY KEY, account VARCHAR(56), description VARCHAR(56), amount DECIMAL(10,2), total DECIMAL(10,2));

DROP TABLE IF EXISTS temperatures;
CREATE TABLE temperatures (id  int NOT NULL  auto_increment PRIMARY KEY, temp SMALLINT);

DROP TABLE IF EXISTS shard1_temperatures;
CREATE TABLE shard1_temperatures (id  int NOT NULL  auto_increment PRIMARY KEY, temp SMALLINT);

DROP TABLE IF EXISTS shard2_temperatures;
CREATE TABLE shard2_temperatures (id  int NOT NULL  auto_increment PRIMARY KEY, temp SMALLINT);

DROP TABLE IF EXISTS salaries;
CREATE TABLE salaries (id  int NOT NULL  auto_increment PRIMARY KEY, salary DECIMAL(7, 2));

DROP TABLE IF EXISTS users;
CREATE TABLE users (id  int NOT NULL  auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), email VARCHAR(56));

DROP TABLE IF EXISTS shard1_users;
CREATE TABLE shard1_users (id  int NOT NULL  auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), email VARCHAR(56));

DROP TABLE IF EXISTS addresses;
CREATE TABLE addresses (id  int NOT NULL  auto_increment PRIMARY KEY, address1 VARCHAR(56), address2 VARCHAR(56), city VARCHAR(56), state VARCHAR(56), zip VARCHAR(56), user_id int);

DROP TABLE IF EXISTS shard1_addresses;
CREATE TABLE shard1_addresses (id  int NOT NULL  auto_increment PRIMARY KEY, address1 VARCHAR(56), address2 VARCHAR(56), city VARCHAR(56), state VARCHAR(56), zip VARCHAR(56), user_id int);



DROP TABLE IF EXISTS rooms;
CREATE TABLE rooms (id  int NOT NULL  auto_increment PRIMARY KEY, name VARCHAR(56), address_id int);

DROP TABLE IF EXISTS legacy_universities;
CREATE TABLE legacy_universities (id  int NOT NULL  auto_increment PRIMARY KEY, univ_name VARCHAR(56), address1 VARCHAR(56), address2 VARCHAR(56), city VARCHAR(56), state VARCHAR(56), zip VARCHAR(56));

DROP TABLE IF EXISTS libraries;
CREATE TABLE libraries (id  int NOT NULL  auto_increment PRIMARY KEY, address VARCHAR(56), city VARCHAR(56), state VARCHAR(56));

DROP TABLE IF EXISTS books;
CREATE TABLE books (id  int NOT NULL  auto_increment PRIMARY KEY, title VARCHAR(56), author VARCHAR(56), isbn VARCHAR(56), lib_id int);

DROP TABLE IF EXISTS readers;
CREATE TABLE readers (id  int NOT NULL  auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), book_id int);

DROP TABLE IF EXISTS animals;
CREATE TABLE animals (animal_id  int NOT NULL  auto_increment PRIMARY KEY, animal_name VARCHAR(56));

DROP TABLE IF EXISTS patients;
CREATE TABLE patients (id  int NOT NULL  auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56));

DROP TABLE IF EXISTS shard1_patients;
CREATE TABLE shard1_patients (id  int NOT NULL  auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56));

DROP TABLE IF EXISTS prescriptions;
CREATE TABLE prescriptions (id  int NOT NULL  auto_increment PRIMARY KEY, name VARCHAR(56), patient_id int, doctor_id int);

DROP TABLE IF EXISTS patient_cards;
CREATE TABLE patient_cards (id  int NOT NULL  auto_increment PRIMARY KEY, info VARCHAR(56), patient_id int);

DROP TABLE IF EXISTS doctors;
CREATE TABLE doctors (id  int NOT NULL  auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), discipline varchar(56));

DROP TABLE IF EXISTS shard1_doctors;
CREATE TABLE shard1_doctors (id  int NOT NULL  auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), discipline varchar(56));

DROP TABLE IF EXISTS doctors_patients;
CREATE TABLE doctors_patients (id  int NOT NULL  auto_increment PRIMARY KEY, doctor_id int, patient_id int);

DROP TABLE IF EXISTS students;
CREATE TABLE students (id  int NOT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), dob DATE, enrollment_date DATETIME);

DROP TABLE IF EXISTS courses;
CREATE TABLE courses (id  int NOT NULL  auto_increment PRIMARY KEY, course_name VARCHAR(56));

DROP TABLE IF EXISTS registrations;
CREATE TABLE registrations (id  int NOT NULL  auto_increment PRIMARY KEY, astudent_id int, acourse_id int);


DROP TABLE IF EXISTS items;
CREATE TABLE items (id  int NOT NULL  auto_increment PRIMARY KEY, item_number int, item_description VARCHAR(56), lock_version int);

DROP TABLE IF EXISTS articles;
CREATE TABLE articles (id  int NOT NULL  auto_increment PRIMARY KEY, title VARCHAR(56), content TEXT);

DROP TABLE IF EXISTS shard1_articles;
CREATE TABLE shard1_articles (id  int NOT NULL  auto_increment PRIMARY KEY, title VARCHAR(56), content TEXT);

DROP TABLE IF EXISTS posts;
CREATE TABLE posts (id  int NOT NULL  auto_increment PRIMARY KEY, title VARCHAR(56), post TEXT);

DROP TABLE IF EXISTS shard1_posts;
CREATE TABLE shard1_posts (id  int NOT NULL  auto_increment PRIMARY KEY, title VARCHAR(56), post TEXT);

DROP TABLE IF EXISTS comments;
CREATE TABLE comments (id  int NOT NULL  auto_increment PRIMARY KEY, author VARCHAR(56), content TEXT, parent_id int, parent_type VARCHAR(256));

DROP TABLE IF EXISTS shard1_comments;
CREATE TABLE shard1_comments (id  int NOT NULL  auto_increment PRIMARY KEY, author VARCHAR(56), content TEXT, parent_id int, parent_type VARCHAR(256));

DROP TABLE IF EXISTS tags;
CREATE TABLE tags (id  int NOT NULL  auto_increment PRIMARY KEY, content TEXT, parent_id int, parent_type VARCHAR(256));

DROP TABLE IF EXISTS fruits;
CREATE TABLE fruits (id  int NOT NULL  auto_increment PRIMARY KEY, fruit_name VARCHAR(56), category VARCHAR(56), created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS vegetables;
CREATE TABLE vegetables (id  int NOT NULL  auto_increment PRIMARY KEY, vegetable_name VARCHAR(56), category VARCHAR(56), created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS plants;
CREATE TABLE plants (id  int NOT NULL  auto_increment PRIMARY KEY, plant_name VARCHAR(56), category VARCHAR(56), created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS pages;
CREATE TABLE pages ( id int NOT NULL  auto_increment PRIMARY KEY, description VARCHAR(56), word_count int );


DROP TABLE IF EXISTS watermelons;
CREATE TABLE watermelons ( id int NOT NULL  auto_increment PRIMARY KEY, melon_type VARCHAR(56), record_version INT, created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS schools;
CREATE TABLE schools ( id int NOT NULL  auto_increment PRIMARY KEY, school_name VARCHAR(56), school_type VARCHAR(56), email VARCHAR(56), created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS programmers;
CREATE TABLE programmers ( id int NOT NULL  auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), email VARCHAR(56), created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS projects;
CREATE TABLE projects ( id int NOT NULL  auto_increment PRIMARY KEY, project_name VARCHAR(56), created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS programmers_projects;
CREATE TABLE programmers_projects ( id int NOT NULL  auto_increment PRIMARY KEY, duration_weeks int, project_id int, programmer_id int, created_at DATETIME, updated_at DATETIME);


DROP TABLE IF EXISTS computers;

DROP TABLE IF EXISTS keyboards;
CREATE TABLE keyboards ( id int NOT NULL  auto_increment PRIMARY KEY, description VARCHAR(56));

DROP TABLE IF EXISTS motherboards;
CREATE TABLE motherboards ( id int NOT NULL  auto_increment PRIMARY KEY, description VARCHAR(56));

CREATE TABLE computers ( id int NOT NULL  auto_increment PRIMARY KEY, description VARCHAR(56), mother_id int, key_id int, constraint fk_computer_mother foreign key (mother_id) references motherboards(id), constraint fk_computer_key foreign key (key_id) references keyboards(id) );


DROP TABLE IF EXISTS ingredients;
CREATE TABLE ingredients (ingredient_id  int NOT NULL  auto_increment PRIMARY KEY, ingredient_name VARCHAR(56));

DROP TABLE IF EXISTS recipes;
CREATE TABLE recipes (recipe_id  int NOT NULL  auto_increment PRIMARY KEY, recipe_name VARCHAR(56));

DROP TABLE IF EXISTS ingredients_recipes;
CREATE TABLE ingredients_recipes (the_id  int NOT NULL  auto_increment PRIMARY KEY, recipe_id int, ingredient_id int);

DROP TABLE IF EXISTS vehicles;
CREATE TABLE vehicles (id  int NOT NULL  auto_increment PRIMARY KEY, name VARCHAR(56));

DROP TABLE IF EXISTS mammals;
CREATE TABLE mammals (id  int NOT NULL  auto_increment PRIMARY KEY, name VARCHAR(56));

DROP TABLE IF EXISTS classifications;
CREATE TABLE classifications (id  int NOT NULL  auto_increment PRIMARY KEY, name VARCHAR(56), parent_id int, parent_type VARCHAR(56));

DROP TABLE IF EXISTS sub_classifications;
CREATE TABLE sub_classifications (id  int NOT NULL  auto_increment PRIMARY KEY, name VARCHAR(56), classification_id int);

DROP TABLE IF EXISTS content_groups;
create table content_groups ( id  int NOT NULL  auto_increment PRIMARY KEY, group_name int );

DROP TABLE IF EXISTS cakes;
CREATE TABLE cakes (id int NOT NULL auto_increment PRIMARY KEY, name VARCHAR(56) NOT NULL);

DROP TABLE IF EXISTS swords;
CREATE TABLE swords (id int NOT NULL auto_increment PRIMARY KEY, name VARCHAR(56) NOT NULL);

DROP TABLE IF EXISTS meals;
CREATE TABLE meals (id int NOT NULL auto_increment PRIMARY KEY, name VARCHAR(56) NOT NULL);

DROP TABLE IF EXISTS Member;
CREATE TABLE Member (id int NOT NULL auto_increment PRIMARY KEY, name VARCHAR(56) NOT NULL);


DROP TABLE IF EXISTS nodes;
CREATE TABLE nodes (id int NOT NULL auto_increment PRIMARY KEY, name VARCHAR(56) NOT NULL, parent_id int);

DROP TABLE IF EXISTS images;
CREATE TABLE images (id int NOT NULL auto_increment PRIMARY KEY, name VARCHAR(56) NOT NULL, content BLOB);

DROP TABLE IF EXISTS apples;
CREATE TABLE apples (id int NOT NULL PRIMARY KEY, apple_type VARCHAR(56) NOT NULL );

DROP TABLE IF EXISTS alarms;
CREATE TABLE alarms (id int NOT NULL auto_increment PRIMARY KEY, alarm_time TIME NOT NULL);

DROP TABLE IF EXISTS developers;
CREATE TABLE developers (first_name VARCHAR(56) NOT NULL, last_name VARCHAR(56) NOT NULL, email VARCHAR(56) NOT NULL, address VARCHAR(56), CONSTRAINT developers_uq PRIMARY KEY (first_name, last_name, email));


DROP TABLE IF EXISTS "Wild Animals";
CREATE TABLE "Wild Animals" (id int auto_increment PRIMARY KEY, "Name" VARCHAR(56) NOT NULL);

DROP TABLE IF EXISTS boxes;
CREATE TABLE boxes (id  int NOT NULL auto_increment PRIMARY KEY, color VARCHAR(56) NOT NULL, fruit_id int, created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS passengers;
CREATE TABLE passengers (id  int NOT NULL auto_increment PRIMARY KEY, user_id int NOT NULL, vehicle VARCHAR(10), transportation_mode VARCHAR(10), created_at DATETIME, updated_at DATETIME);

DROP TABLE IF EXISTS teams;
CREATE TABLE teams (team_id int NOT NULL auto_increment PRIMARY KEY, name VARCHAR(56) NOT NULL);

DROP TABLE IF EXISTS players;
CREATE TABLE players (id int NOT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56) NOT NULL, last_name VARCHAR(56) NOT NULL, team_id int);

DROP TABLE IF EXISTS bands;
CREATE TABLE bands (band_id int NOT NULL auto_increment PRIMARY KEY, name VARCHAR(56) NOT NULL);

DROP TABLE IF EXISTS genres;
CREATE TABLE genres (genre_id int NOT NULL auto_increment PRIMARY KEY, name VARCHAR(56) NOT NULL);

DROP TABLE IF EXISTS musicians;
CREATE TABLE musicians (musician_id int NOT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56) NOT NULL, last_name VARCHAR(56) NOT NULL);

DROP TABLE IF EXISTS bands_genres;
CREATE TABLE bands_genres (the_id  int NOT NULL  auto_increment PRIMARY KEY, band_id int, genre_id int);

DROP TABLE IF EXISTS bands_musicians;
CREATE TABLE bands_musicians (the_id  int NOT NULL  auto_increment PRIMARY KEY, band_id int, musician_id int);


DROP TABLE IF EXISTS employees;
CREATE TABLE employees (
  id  int NOT NULL auto_increment PRIMARY KEY,
  first_name VARCHAR(56) NOT NULL,
  last_name VARCHAR(56),
  position  VARCHAR(56),
  active int,
  department VARCHAR(56),
  created_at DATETIME,
  updated_at DATETIME);

DROP TABLE IF EXISTS customers;
CREATE TABLE customers (customer_id int NOT NULL auto_increment PRIMARY KEY, first_name VARCHAR(56) NOT NULL, last_name VARCHAR(56) NOT NULL, salutation VARCHAR(56) NOT NULL);

DROP TABLE IF EXISTS postal_addresses;
CREATE TABLE postal_addresses (id  int NOT NULL  auto_increment PRIMARY KEY, address1 VARCHAR(56), address2 VARCHAR(56), city VARCHAR(56), zip VARCHAR(56), country VARCHAR(56), scope VARCHAR(56), customer_id int);

DROP TABLE IF EXISTS phone_numbers;
CREATE TABLE phone_numbers (id  int NOT NULL  auto_increment PRIMARY KEY, the_number VARCHAR(56), type VARCHAR(56), scope VARCHAR(56), customer_id int);

