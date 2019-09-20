-- Books
CREATE TABLE books (
    id INT NOT NULL PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(1000) NOT NULL,
    published_date TIMESTAMP,
    inventory INT
);

# Single line comment
CREATE TABLE authors (
    id INT NOT NULL PRIMARY KEY,
    first_name varchar(64) not null,
    last_name varchar(64) not null
);
