USE shoppingmngmntsystem;
GO

SELECT SUM(TotalAmount) AS TotalRevenue
FROM Orders
WHERE Status != 'Cancelled';

SELECT c.FirstName, c.LastName,
       COUNT(o.OrderID)   AS OrdersCount,
       SUM(o.TotalAmount) AS TotalSpent
FROM Customers c
JOIN Orders o ON o.CustomerID = c.CustomerID
WHERE o.Status != 'Cancelled'
GROUP BY c.FirstName, c.LastName
ORDER BY TotalSpent DESC;

SELECT TOP 5 p.ProductName,
       SUM(oi.Quantity) AS UnitsSold
FROM Products p
JOIN OrderItems oi ON oi.ProductID = p.ProductID
JOIN Orders o      ON o.OrderID = oi.OrderID
WHERE o.Status != 'Cancelled'
GROUP BY p.ProductID, p.ProductName
ORDER BY UnitsSold DESC;

SELECT s.StoreName,
       SUM(oi.Quantity * oi.UnitPrice) AS Revenue
FROM Sellers s
JOIN Products p    ON p.SellerID = s.SellerID
JOIN OrderItems oi ON oi.ProductID = p.ProductID
JOIN Orders o      ON o.OrderID = oi.OrderID
WHERE o.Status != 'Cancelled'
GROUP BY s.SellerID, s.StoreName
ORDER BY Revenue DESC;

SELECT p.ProductName,
       COUNT(r.ReviewID) AS ReviewsCount,
       AVG(r.Rating * 1.0) AS AvgRating
FROM Products p
JOIN Reviews r ON r.ProductID = p.ProductID
GROUP BY p.ProductID, p.ProductName
ORDER BY AvgRating DESC;

SELECT p.ProductID, p.ProductName
FROM Products p
LEFT JOIN Reviews r ON r.ProductID = p.ProductID
WHERE r.ReviewID IS NULL;

SELECT p.ProductName, i.QuantityInStock
FROM Products p
JOIN Inventory i ON i.ProductID = p.ProductID
WHERE i.QuantityInStock < 50
ORDER BY i.QuantityInStock;

SELECT Status, COUNT(*) AS OrdersCount
FROM Orders
GROUP BY Status;

SELECT cp.Code,
       COUNT(o.OrderID)     AS OrdersUsed,
       SUM(o.DiscountAmount) AS TotalDiscount
FROM Coupons cp
JOIN Orders o ON o.CouponID = cp.CouponID
GROUP BY cp.CouponID, cp.Code;

SELECT p.ProductName,
       COUNT(r.ReturnID)    AS ReturnRequests,
       SUM(r.RefundAmount)  AS TotalRefunded
FROM Products p
JOIN Returns r ON r.ProductID = p.ProductID
GROUP BY p.ProductID, p.ProductName;

SELECT p.PaymentID, p.OrderID, p.Method, p.Amount, p.Status
FROM Payments p
WHERE p.Status != 'Completed';