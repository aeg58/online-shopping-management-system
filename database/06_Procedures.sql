/* =====================================================================
   Online Shopping Management System - Stored Procedures (SQL Server)
   File : database/06_Procedures.sql
   Run  : AFTER 01_schema.sql and 02_seed.sql

   A STORED PROCEDURE is a saved block of SQL that we run with EXEC.
     @Name      = a parameter (the value we send in)
     DECLARE    = creates a local variable inside the procedure
     SET / SELECT @x = ...   puts a value into a variable
     IF ... ELSE             makes a decision
     PRINT                   shows a message
     RETURN                  stops the procedure
   CREATE OR ALTER lets you re-run this file without errors.
   ===================================================================== */

USE shoppingmngmntsystem;
GO

/* ---------------------------------------------------------------------
   1. sp_GetCustomerOrders : show all orders of one customer
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE sp_GetCustomerOrders
    @CustomerID INT
AS
BEGIN
    SELECT OrderID, OrderDate, OrderStatus, CouponCode, DiscountAmount, TotalAmount
    FROM vw_OrderSummary
    WHERE CustomerID = @CustomerID
    ORDER BY OrderDate;
END;
GO

/* ---------------------------------------------------------------------
   2. sp_GetProductsByCategory : list the products of one category
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE sp_GetProductsByCategory
    @CategoryName VARCHAR(100)
AS
BEGIN
    SELECT ProductName, Price, StoreName, QuantityInStock
    FROM vw_ProductCatalog
    WHERE CategoryName = @CategoryName
      AND IsActive = 1;
END;
GO

/* ---------------------------------------------------------------------
   3. sp_GetOrderTotal : calculate the total of an order from its items
      (uses DECLARE for two variables)
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE sp_GetOrderTotal
    @OrderID INT
AS
BEGIN
    DECLARE @ItemsTotal DECIMAL(10,2);
    DECLARE @Discount   DECIMAL(10,2);

    SELECT @ItemsTotal = SUM(Quantity * UnitPrice)
    FROM OrderItems
    WHERE OrderID = @OrderID;

    SELECT @Discount = DiscountAmount
    FROM Orders
    WHERE OrderID = @OrderID;

    IF @ItemsTotal IS NULL
    BEGIN
        PRINT 'Order not found or it has no items.';
        RETURN;
    END

    SELECT @ItemsTotal             AS ItemsTotal,
           @Discount               AS Discount,
           @ItemsTotal - @Discount AS FinalTotal;
END;
GO

/* ---------------------------------------------------------------------
   4. sp_AddStock : add units to the stock of a product
      (uses DECLARE + IF checks)
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE sp_AddStock
    @ProductID INT,
    @Quantity  INT
AS
BEGIN
    DECLARE @CurrentStock INT;

    SELECT @CurrentStock = QuantityInStock
    FROM Inventory
    WHERE ProductID = @ProductID;

    IF @CurrentStock IS NULL
    BEGIN
        PRINT 'Product not found.';
        RETURN;
    END

    IF @Quantity <= 0
    BEGIN
        PRINT 'Quantity must be greater than zero.';
        RETURN;
    END

    UPDATE Inventory
    SET QuantityInStock = QuantityInStock + @Quantity,
        LastUpdated     = GETDATE()
    WHERE ProductID = @ProductID;

    PRINT 'Stock updated: ' + CAST(@CurrentStock AS VARCHAR(10))
        + ' -> ' + CAST(@CurrentStock + @Quantity AS VARCHAR(10));
END;
GO

/* ---------------------------------------------------------------------
   5. sp_CancelOrder : cancel a Pending or Paid order
      - put the products back into stock
      - mark the order as Cancelled
      - Completed payment -> Refunded, Pending payment -> Failed
      The 3 updates run inside one transaction (all or nothing).
   --------------------------------------------------------------------- */
CREATE OR ALTER PROCEDURE sp_CancelOrder
    @OrderID INT
AS
BEGIN
    SET XACT_ABORT ON;      -- if any statement fails, everything is rolled back

    DECLARE @Status VARCHAR(20);

    SELECT @Status = Status
    FROM Orders
    WHERE OrderID = @OrderID;

    IF @Status IS NULL
    BEGIN
        PRINT 'Order not found.';
        RETURN;
    END

    IF @Status NOT IN ('Pending', 'Paid')
    BEGIN
        PRINT 'Only Pending or Paid orders can be cancelled. This order is: ' + @Status;
        RETURN;
    END

    BEGIN TRANSACTION;

        -- 1. give the products back to the stock
        UPDATE i
        SET i.QuantityInStock = i.QuantityInStock + oi.Quantity,
            i.LastUpdated     = GETDATE()
        FROM Inventory i
        JOIN OrderItems oi ON oi.ProductID = i.ProductID
        WHERE oi.OrderID = @OrderID;

        -- 2. cancel the order
        UPDATE Orders
        SET Status = 'Cancelled'
        WHERE OrderID = @OrderID;

        -- 3. refund or fail the payments
        UPDATE Payments
        SET Status = CASE WHEN Status = 'Completed' THEN 'Refunded' ELSE 'Failed' END
        WHERE OrderID = @OrderID
          AND Status IN ('Pending', 'Completed');

    COMMIT TRANSACTION;

    PRINT 'Order ' + CAST(@OrderID AS VARCHAR(10)) + ' was cancelled.';
END;
GO

/* ---------------------- How to run them (EXEC) ----------------------- */

-- Read-only procedures
EXEC sp_GetCustomerOrders @CustomerID = 1;
EXEC sp_GetProductsByCategory @CategoryName = 'Electronics';
EXEC sp_GetOrderTotal @OrderID = 1;
GO

-- These two CHANGE data: uncomment one line at a time to test
-- EXEC sp_AddStock @ProductID = 16, @Quantity = 25;   -- Face Moisturizer was out of stock
-- EXEC sp_CancelOrder @OrderID = 4;                   -- order 4 is Pending
-- EXEC sp_CancelOrder @OrderID = 1;                   -- order 1 is Delivered -> refused with a message
GO
