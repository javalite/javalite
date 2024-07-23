docker run --name javalite-oracle -d -p 51521:1521 -p 55500:5500 -e ORACLE_PWD=p@ssw0rd -e ORACLE_CHARACTERSET=AL32UTF8 oracle/database:18.4.0-xe
