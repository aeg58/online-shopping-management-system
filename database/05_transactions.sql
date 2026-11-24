-- 1. Place a simple order and update product stock (All-or-Nothing)
BEGIN TRANSACTION;

INSERT INTO Orders (CustomerID, OrderDate, Status, TotalAmount)
VALUES (1, GETDATE(), 'Pending', 100.00);

UPDATE Products
SET Stock = Stock - 1
WHERE ProductID = 10 AND Stock >= 1;

IF @@ERROR <> 0
    ROLLBACK;
ELSE
    COMMIT;


-- 2. Record a payment and mark an order as Paid
BEGIN TRANSACTION;

INSERT INTO Payments (OrderID, PaymentDate, Amount, PaymentMethod, Status)
VALUES (101, GETDATE(), 100.00, 'Card', 'Completed');

UPDATE Orders
SET Status = 'Paid'
WHERE OrderID = 101;

IF @@ERROR <> 0
    ROLLBACK;
ELSE
    COMMIT;


-- 3. Cancel an order and restore product stock
BEGIN TRANSACTION;

UPDATE Orders
SET Status = 'Cancelled'
WHERE OrderID = 101;

UPDATE Products
SET Stock = Stock + 1
WHERE ProductID = 10;

IF @@ERROR <> 0
    ROLLBACK;
ELSE
    COMMIT;


-- 4. Update product price and category, rolling back only the category update if needed
BEGIN TRANSACTION;

UPDATE Products
SET Price = 120.00
WHERE ProductID = 10;

SAVE TRANSACTION sp_category_update;

UPDATE Products
SET CategoryID = 999
WHERE ProductID = 10;

-- Example: undo only the savepoint step if invalid, then commit the price update
ROLLBACK TRANSACTION sp_category_update;

COMMIT;


-- 5. Safe delete of a category only if it has no products
BEGIN TRANSACTION;

DECLARE @ProductCount INT;
SELECT @ProductCount = COUNT(*) FROM Products WHERE CategoryID = 5;

IF @ProductCount > 0
    ROLLBACK;
ELSE
BEGIN
    DELETE FROM Categories WHERE CategoryID = 5;
    IF @@ERROR <> 0
        ROLLBACK;
    ELSE
        COMMIT;
END;
