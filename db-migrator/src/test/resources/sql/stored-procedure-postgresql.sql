DELIMITER ;;

CREATE FUNCTION getQtyOrders(customerID int) RETURNS int AS $$
DECLARE
    qty int;
BEGIN
  SELECT COUNT(*) INTO qty
    FROM Orders
      WHERE accnum = customerID;
  RETURN qty;
END;
$$ LANGUAGE plpgsql;

;;

CREATE FUNCTION one() RETURNS integer AS '
    SELECT 1 AS result;
' LANGUAGE SQL;

;;

CREATE FUNCTION emp_stamp() RETURNS trigger AS $emp_stamp$
  BEGIN
    -- Check that empname and salary are given
    IF NEW.empname IS NULL THEN
        RAISE EXCEPTION 'empname cannot be null';
    END IF;
    IF NEW.salary IS NULL THEN
        RAISE EXCEPTION '% cannot have null salary', NEW.empname;
    END IF;

    -- Who works for us when she must pay for it?
    IF NEW.salary < 0 THEN
        RAISE EXCEPTION '% cannot have a negative salary', NEW.empname;
    END IF;

    -- Remember who changed the payroll when
    NEW.last_date := current_timestamp;
    NEW.last_user := current_user;
    RETURN NEW;
  END;
$emp_stamp$ LANGUAGE plpgsql;

;;

SELECT one();