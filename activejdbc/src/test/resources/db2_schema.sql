
CALL dropTable('people');
CREATE TABLE people (id int primary key GENERATED ALWAYS AS IDENTITY, name VARCHAR(56) NOT NULL, last_name VARCHAR(56), dob DATE, graduation_date DATE, created_at TIMESTAMP, updated_at TIMESTAMP);

CALL dropTable('accounts');
CREATE TABLE accounts (id int primary key GENERATED ALWAYS AS IDENTITY, account VARCHAR(56), description VARCHAR(56), amount DECIMAL(10,2), total DECIMAL(10,2));

CALL dropTable('temperatures');
CREATE TABLE temperatures (id int primary key GENERATED ALWAYS AS IDENTITY, temp SMALLINT);

CALL dropTable('shard1_temperatures');
CREATE TABLE shard1_temperatures (id int primary key GENERATED ALWAYS AS IDENTITY, temp SMALLINT);

CALL dropTable('shard2_temperatures');
CREATE TABLE shard2_temperatures (id int primary key GENERATED ALWAYS AS IDENTITY, temp SMALLINT);

CALL dropTable('salaries');
CREATE TABLE salaries (id int primary key GENERATED ALWAYS AS IDENTITY, salary DECIMAL(7, 2));

CALL dropTable('users');
CREATE TABLE users (id int primary key GENERATED ALWAYS AS IDENTITY, first_name VARCHAR(56), last_name VARCHAR(56), email VARCHAR(56));

CALL dropTable('shard1_users');
CREATE TABLE shard1_users (id int primary key GENERATED ALWAYS AS IDENTITY, first_name VARCHAR(56), last_name VARCHAR(56), email VARCHAR(56));

CALL dropTable('addresses');
CREATE TABLE addresses (id int primary key GENERATED ALWAYS AS IDENTITY, address1 VARCHAR(56), address2 VARCHAR(56), city VARCHAR(56), state VARCHAR(56), zip VARCHAR(56), user_id int);

CALL dropTable('shard1_addresses');
CREATE TABLE shard1_addresses (id int primary key GENERATED ALWAYS AS IDENTITY, address1 VARCHAR(56), address2 VARCHAR(56), city VARCHAR(56), state VARCHAR(56), zip VARCHAR(56), user_id INT);

CALL dropTable('rooms');
CREATE TABLE rooms (id int primary key GENERATED ALWAYS AS IDENTITY, name VARCHAR(56), address_id int);

CALL dropTable('legacy_universities');
CREATE TABLE legacy_universities (id int primary key GENERATED ALWAYS AS IDENTITY, univ_name VARCHAR(56), address1 VARCHAR(56), address2 VARCHAR(56), city VARCHAR(56), state VARCHAR(56), zip VARCHAR(56));

CALL dropTable('libraries');
CREATE TABLE libraries (id int primary key GENERATED ALWAYS AS IDENTITY, address VARCHAR(56), city VARCHAR(56), state VARCHAR(56));

CALL dropTable('books');
CREATE TABLE books (id int primary key GENERATED ALWAYS AS IDENTITY, title VARCHAR(56), author VARCHAR(56), isbn VARCHAR(56), lib_id int);

CALL dropTable('readers');
CREATE TABLE readers (id int primary key GENERATED ALWAYS AS IDENTITY, first_name VARCHAR(56), last_name VARCHAR(56), book_id int);

CALL dropTable('animals');
CREATE TABLE animals (animal_id int primary key GENERATED ALWAYS AS IDENTITY, animal_name VARCHAR(56));

CALL dropTable('patients');
CREATE TABLE patients (id int primary key GENERATED ALWAYS AS IDENTITY, first_name VARCHAR(56), last_name VARCHAR(56));

CALL dropTable('shard1_patients');
CREATE TABLE shard1_patients (id int primary key GENERATED ALWAYS AS IDENTITY, first_name VARCHAR(56), last_name VARCHAR(56));

CALL dropTable('prescriptions');
CREATE TABLE prescriptions (id int primary key GENERATED ALWAYS AS IDENTITY, name VARCHAR(56), patient_id int);

CALL dropTable('doctors');
CREATE TABLE doctors (id int primary key GENERATED ALWAYS AS IDENTITY, first_name VARCHAR(56), last_name VARCHAR(56), discipline varchar(56));

CALL dropTable('shard1_doctors');
CREATE TABLE shard1_doctors (id int primary key GENERATED ALWAYS AS IDENTITY, first_name VARCHAR(56), last_name VARCHAR(56), discipline varchar(56));

CALL dropTable('doctors_patients');
CREATE TABLE doctors_patients (id int primary key GENERATED ALWAYS AS IDENTITY, doctor_id int, patient_id int);

CALL dropTable('students');
CREATE TABLE students (id int primary key GENERATED ALWAYS AS IDENTITY, first_name VARCHAR(56), last_name VARCHAR(56), dob DATE, enrollment_date TIMESTAMP);

CALL dropTable('courses');
CREATE TABLE courses (id int primary key GENERATED ALWAYS AS IDENTITY, course_name VARCHAR(56));

CALL dropTable('registrations');
CREATE TABLE registrations (id int primary key GENERATED ALWAYS AS IDENTITY, astudent_id int, acourse_id int);

CALL dropTable('items');
CREATE TABLE items (id int primary key GENERATED ALWAYS AS IDENTITY, item_number int, item_description VARCHAR(56), lock_version int);

CALL dropTable('articles');
CREATE TABLE articles (id int primary key GENERATED ALWAYS AS IDENTITY, title VARCHAR(56), content CLOB);

CALL dropTable('shard1_articles');
CREATE TABLE shard1_articles (id int primary key GENERATED ALWAYS AS IDENTITY, title VARCHAR(56), content CLOB);

CALL dropTable('posts');
CREATE TABLE posts (id int primary key GENERATED ALWAYS AS IDENTITY, title VARCHAR(56), post CLOB);

CALL dropTable('shard1_posts');
CREATE TABLE shard1_posts (id int primary key GENERATED ALWAYS AS IDENTITY, title VARCHAR(56), post CLOB);

CALL dropTable('comments');
CREATE TABLE comments (id int primary key GENERATED ALWAYS AS IDENTITY, author VARCHAR(56), content CLOB, parent_id int, parent_type VARCHAR(256));

CALL dropTable('shard1_comments');
CREATE TABLE shard1_comments (id int primary key GENERATED ALWAYS AS IDENTITY, author VARCHAR(56), content CLOB, parent_id INT, parent_type VARCHAR(256));

CALL dropTable('tags');
CREATE TABLE tags (id int primary key GENERATED ALWAYS AS IDENTITY, content CLOB, parent_id int, parent_type VARCHAR(256));

CALL dropTable('fruits');
CREATE TABLE fruits (id int primary key GENERATED ALWAYS AS IDENTITY, fruit_name VARCHAR(56), category VARCHAR(56), created_at TIMESTAMP, updated_at TIMESTAMP);

CALL dropTable('vegetables');
CREATE TABLE vegetables (id int primary key GENERATED ALWAYS AS IDENTITY, vegetable_name VARCHAR(56), category VARCHAR(56), created_at TIMESTAMP, updated_at TIMESTAMP);

CALL dropTable('plants');
CREATE TABLE plants (id int primary key GENERATED ALWAYS AS IDENTITY, plant_name VARCHAR(56), category VARCHAR(56), created_at TIMESTAMP, updated_at TIMESTAMP);

CALL dropTable('pages');
CREATE TABLE pages ( id int primary key GENERATED ALWAYS AS IDENTITY, description VARCHAR(56), word_count int );

CALL dropTable('watermelons');
CREATE TABLE watermelons ( id int primary key GENERATED ALWAYS AS IDENTITY, melon_type VARCHAR(56), record_version INT, created_at TIMESTAMP, updated_at TIMESTAMP);

CALL dropTable('schools');
CREATE TABLE schools ( id int primary key GENERATED ALWAYS AS IDENTITY, school_name VARCHAR(56), school_type VARCHAR(56), email VARCHAR(56), created_at TIMESTAMP, updated_at TIMESTAMP);

CALL dropTable('programmers');
CREATE TABLE programmers ( id int primary key GENERATED ALWAYS AS IDENTITY, first_name VARCHAR(56), last_name VARCHAR(56), email VARCHAR(56), created_at TIMESTAMP, updated_at TIMESTAMP);

CALL dropTable('projects');
CREATE TABLE projects ( id int primary key GENERATED ALWAYS AS IDENTITY, project_name VARCHAR(56), created_at TIMESTAMP, updated_at TIMESTAMP);

CALL dropTable('programmers_projects');
CREATE TABLE programmers_projects ( id int primary key GENERATED ALWAYS AS IDENTITY, duration_weeks int, project_id int, programmer_id int, created_at TIMESTAMP, updated_at TIMESTAMP);

CALL dropTable('keyboards');
CREATE TABLE keyboards ( id int primary key GENERATED ALWAYS AS IDENTITY, description VARCHAR(56));

CALL dropTable('motherboards');
CREATE TABLE motherboards ( id int primary key GENERATED ALWAYS AS IDENTITY, description VARCHAR(56));

CALL dropTable('computers');
CREATE TABLE computers ( id int primary key GENERATED ALWAYS AS IDENTITY, description VARCHAR(56), mother_id int, key_id int, constraint fk_computer_mother foreign key (mother_id) references motherboards(id), constraint fk_computer_key foreign key (key_id) references keyboards(id) );

CALL dropTable('ingredients');
CREATE TABLE ingredients (ingredient_id int primary key GENERATED ALWAYS AS IDENTITY, ingredient_name VARCHAR(56));

CALL dropTable('recipes');
CREATE TABLE recipes (recipe_id int primary key GENERATED ALWAYS AS IDENTITY, recipe_name VARCHAR(56));

CALL dropTable('ingredients_recipes');
CREATE TABLE ingredients_recipes (the_id int primary key GENERATED ALWAYS AS IDENTITY, recipe_id int, ingredient_id int);

CALL dropTable('vehicles');
CREATE TABLE vehicles (id int primary key GENERATED ALWAYS AS IDENTITY, name VARCHAR(56));

CALL dropTable('mammals');
CREATE TABLE mammals (id int primary key GENERATED ALWAYS AS IDENTITY, name VARCHAR(56));

CALL dropTable('classifications');
CREATE TABLE classifications (id int primary key GENERATED ALWAYS AS IDENTITY, name VARCHAR(56), parent_id int, parent_type VARCHAR(56));

CALL dropTable('sub_classifications');
CREATE TABLE sub_classifications (id int primary key GENERATED ALWAYS AS IDENTITY, name VARCHAR(56), classification_id int);

CALL dropTable('content_groups');
create table content_groups ( id int primary key GENERATED ALWAYS AS IDENTITY, group_name int );

CALL dropTable('cakes');
CREATE TABLE cakes (id int primary key GENERATED ALWAYS AS IDENTITY, name VARCHAR(56) NOT NULL);

CALL dropTable('swords');
CREATE TABLE swords (id int primary key GENERATED ALWAYS AS IDENTITY, name VARCHAR(56) NOT NULL);

CALL dropTable('meals');
CREATE TABLE meals (id int primary key GENERATED ALWAYS AS IDENTITY, name VARCHAR(56) NOT NULL);

CALL dropTable('Member');
CREATE TABLE Member (id int primary key GENERATED ALWAYS AS IDENTITY, name VARCHAR(56) NOT NULL);

CALL dropTable('nodes');
CREATE TABLE nodes (id int primary key GENERATED ALWAYS AS IDENTITY, name VARCHAR(56) NOT NULL, parent_id int);

CALL dropTable('images');
CREATE TABLE images (id int primary key GENERATED ALWAYS AS IDENTITY, name VARCHAR(56) NOT NULL, content BLOB);

CALL dropTable('apples');
CREATE TABLE apples (id int NOT NULL PRIMARY KEY, apple_type VARCHAR(56) NOT NULL );

CALL dropTable('alarms');
CREATE TABLE alarms (id int primary key GENERATED ALWAYS AS IDENTITY, alarm_time TIME NOT NULL);

CALL dropTable('developers');
CREATE TABLE developers (first_name VARCHAR(56) NOT NULL, last_name VARCHAR(56) NOT NULL, email VARCHAR(56) NOT NULL, address VARCHAR(56), CONSTRAINT developers_uq UNIQUE (first_name, last_name, email));

CALL dropTable('boxes');
CREATE TABLE boxes (id int primary key GENERATED ALWAYS AS IDENTITY, "color" VARCHAR(56) NOT NULL, fruit_id INT);

CALL dropTable('passengers');
CREATE TABLE passengers (id int primary key GENERATED ALWAYS AS IDENTITY, user_id INT NOT NULL, vehicle VARCHAR(10),mode VARCHAR(10));

