DROP TABLE IF EXISTS libraries;
CREATE TABLE libraries (id  int NOT NULL  auto_increment PRIMARY KEY, address VARCHAR(56), city VARCHAR(56), state VARCHAR(56));

DROP TABLE IF EXISTS books;
CREATE TABLE books (id  int NOT NULL  auto_increment PRIMARY KEY, title VARCHAR(56), author VARCHAR(56), isbn VARCHAR(56), lib_id int);

DROP TABLE IF EXISTS readers;
CREATE TABLE readers (id  int NOT NULL  auto_increment PRIMARY KEY, first_name VARCHAR(56), last_name VARCHAR(56), book_id int);
