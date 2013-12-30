CREATE FUNCTION hello (s CHAR(20)) RETURNS CHAR(50) DETERMINISTIC RETURN CONCAT('Hello, ',s,'!');

DELIMITER //
CREATE FUNCTION weighted_average (n1 INT, n2 INT, n3 INT, n4 INT) RETURNS INT DETERMINISTIC
BEGIN
    DECLARE avg INT;
    SET avg = (n1+n2+n3*2+n4*4)/8;
    RETURN avg;
END //

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