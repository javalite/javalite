DROP TABLE IF EXISTS employees;
CREATE TABLE employees (
      id  int(11) DEFAULT NULL auto_increment PRIMARY KEY,
      first_name VARCHAR(56),
      last_name VARCHAR(56)
  );
