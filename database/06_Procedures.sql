USE shoppingmngmntsystem;
GO

/* 1. Returns all orders of a given customer */
CREATE PROCEDURE GetCustomerOrders
@CustomerID INT
AS
BEGIN
    SELECT *
    FROM Orders
    WHERE CustomerID = @CustomerID;
END;
GO

/* 2. Returns products whose price is between two given values */
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

/* 3. Returns all orders placed after a given date */
CREATE PROCEDURE GetOrdersAfterDate
@OrderDate DATE
AS
BEGIN
    SELECT *
    FROM Orders
    WHERE OrderDate > @OrderDate;
END;
GO

/* 4. Inserts a new category
      (CategoryID is an IDENTITY column, so SQL Server gives it automatically) */
CREATE PROCEDURE InsertCategory
@CategoryName VARCHAR(100),
@Description VARCHAR(255)
AS
BEGIN
    INSERT INTO Categories (CategoryName, Description)
    VALUES (@CategoryName, @Description);
END;
GO


/* 5. Returns the number of products in each category (no parameter) */
CREATE PROCEDURE CountProductsPerCategory
AS
BEGIN
    SELECT c.CategoryName, COUNT(p.ProductID) AS NumberOfProducts
    FROM Categories c
    LEFT JOIN Products p ON p.CategoryID = c.CategoryID
    GROUP BY c.CategoryName;
END;
GO

/* 6. Returns all orders shipped to a given city and the customers who placed them */
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

/* 7. Takes a category name and returns the products with the id and store name of their seller */
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