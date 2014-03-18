-- Books
CREATE TABLE books (
    id INT NOT NULL PRIMARY KEY,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(1000) NOT NULL,
    published_date DATETIME
);
