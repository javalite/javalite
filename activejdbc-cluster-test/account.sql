CREATE TABLE accounts (
  id  int(11) NOT NULL auto_increment PRIMARY KEY,
  account VARCHAR(56),
  description VARCHAR(56),
  amount DECIMAL(10,2),
  total DECIMAL(10,2)
);