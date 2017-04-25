CREATE FUNCTION emp_stamp() RETURNS trigger AS $emp_stamp$
  BEGIN
    IF NEW.empname IS NULL THEN
        RAISE EXCEPTION 'empname cannot be null';
    END IF;
    IF NEW.salary IS NULL THEN
        RAISE EXCEPTION '% cannot have null salary', NEW.empname;
    END IF;

    IF NEW.salary < 0 THEN
        RAISE EXCEPTION '% cannot have a negative salary', NEW.empname;
    END IF;

    NEW.last_date := current_timestamp;
    NEW.last_user := current_user;
    RETURN NEW;
  END;
$emp_stamp$ LANGUAGE plpgsql;