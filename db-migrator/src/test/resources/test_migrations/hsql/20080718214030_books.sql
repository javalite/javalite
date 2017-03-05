CREATE TABLE books (
    id INT NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    title VARCHAR(1000) NOT NULL,
    published_date TIMESTAMP,
    CONSTRAINT isbn_unique UNIQUE (isbn) 
);
