CREATE TABLE books (
  id  int(11) DEFAULT NULL auto_increment PRIMARY KEY,
  author VARCHAR(128),
  title VARCHAR(128),
  isbn VARCHAR(128),
  created_at DATETIME,
  updated_at DATETIME
)ENGINE=InnoDB;;
