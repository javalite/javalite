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

