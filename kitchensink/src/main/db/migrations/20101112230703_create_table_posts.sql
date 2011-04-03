CREATE TABLE posts (
  id  int(11) DEFAULT NULL auto_increment PRIMARY KEY,
  author VARCHAR(128),
  title VARCHAR(128),
  content TEXT,
  created_at DATETIME,
  updated_at DATETIME
)TYPE=InnoDB;;