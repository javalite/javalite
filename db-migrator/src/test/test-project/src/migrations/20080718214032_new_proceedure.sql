DELIMITER $$
CREATE PROCEDURE payment(payment_amount DECIMAL(6,2), payment_seller_id INT)
BEGIN
    DECLARE n DECIMAL(6,2);
    SET n = payment_amount - 1.00;
    INSERT INTO Moneys VALUES (n, CURRENT_DATE);
    IF payment_amount > 1.00 THEN
        UPDATE Sellers SET commission = commission + 1.00 WHERE seller_id = payment_seller_id;
    END IF;
END
$$