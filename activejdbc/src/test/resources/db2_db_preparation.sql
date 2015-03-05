--#SET TERMINATOR @@
create or replace procedure dropTable(IN table_name VARCHAR(50))
language SQL
p:begin
	IF EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE TYPE = 'T' AND NAME = UPPER(table_name)) THEN
		EXECUTE IMMEDIATE 'DROP TABLE ' || table_name;
	END IF;
end p
@@
--#SET TERMINATOR ;
