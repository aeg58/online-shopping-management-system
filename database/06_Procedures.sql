USE shoppingmngmntsystem;
GO

CREATE PROCEDURE GetCustomerOrders
@CustomerID INT
AS
BEGIN
    SELECT *
    FROM Orders
    WHERE CustomerID = @CustomerID;
END;
GO

CREATE PROCEDURE GetProductsByPriceRange
@MinPrice DECIMAL(10,2),
@MaxPrice DECIMAL(10,2)
AS
BEGIN
    SELECT *
    FROM Products
    WHERE Price BETWEEN @MinPrice AND @MaxPrice;
END;
GO

CREATE PROCEDURE GetOrdersAfterDate
@OrderDate DATE
AS
BEGIN
    SELECT *
    FROM Orders
    WHERE OrderDate > @OrderDate;
END;
GO

CREATE PROCEDURE InsertCategory
@CategoryName VARCHAR(100),
@Description VARCHAR(255)
AS
BEGIN
    INSERT INTO Categories (CategoryName, Description)
    VALUES (@CategoryName, @Description);
END;
GO


CREATE PROCEDURE CountProductsPerCategory
AS
BEGIN
    SELECT c.CategoryName, COUNT(p.ProductID) AS NumberOfProducts
    FROM Categories c
    LEFT JOIN Products p ON p.CategoryID = c.CategoryID
    GROUP BY c.CategoryName;
END;
GO

CREATE PROCEDURE GetOrdersByCity
@City VARCHAR(50)
AS
BEGIN
    SELECT a.City, o.OrderID, o.OrderDate, o.TotalAmount,
           cu.FirstName, cu.LastName
    FROM Addresses a
    JOIN Orders o     ON o.AddressID = a.AddressID
    JOIN Customers cu ON cu.CustomerID = o.CustomerID
    WHERE a.City = @City;
END;
GO

CREATE PROCEDURE GetProductsWithSeller
@CategoryName VARCHAR(100)
AS
BEGIN
    SELECT p.ProductName, s.SellerID, s.StoreName
    FROM Categories c
    JOIN Products p ON p.CategoryID = c.CategoryID
    JOIN Sellers s  ON s.SellerID = p.SellerID
    WHERE c.CategoryName = @CategoryName;
END;
GO

EXEC GetCustomerOrders @CustomerID = 1;
EXEC GetProductsByPriceRange @MinPrice = 15, @MaxPrice = 60;
EXEC GetOrdersAfterDate @OrderDate = '2026-10-15';
EXEC InsertCategory @CategoryName ='noor' , @Description = 'humanbeing'
EXEC CountProductsPerCategory
EXEC GetOrdersByCity @City = 'Istanbul';
EXEC GetProductsWithSeller @CategoryName = 'Clothing';