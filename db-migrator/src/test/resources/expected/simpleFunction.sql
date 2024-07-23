CREATE FUNCTION simpleFunction() RETURNS varchar(100) READS SQL DATA
begin
  	declare message varchar(100) default 'Hello Word';
  	return message;
end