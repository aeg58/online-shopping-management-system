USE shoppingmngmntsystem;
GO

-- Q1: What is the total revenue of all orders that were not cancelled?
SELECT SUM(TotalAmount) AS TotalRevenue
FROM Orders
WHERE Status != 'Cancelled';

-- Q2: How many orders did each customer place and how much did they spend?
SELECT c.FirstName, c.LastName,
       COUNT(o.OrderID)   AS OrdersCount,
       SUM(o.TotalAmount) AS TotalSpent
FROM Customers c
JOIN Orders o ON o.CustomerID = c.CustomerID
WHERE o.Status != 'Cancelled'
GROUP BY c.FirstName, c.LastName
ORDER BY TotalSpent DESC;

-- Q3: Which are the 5 best-selling products by units sold?
SELECT TOP 5 p.ProductName,
       SUM(oi.Quantity) AS UnitsSold
FROM Products p
JOIN OrderItems oi ON oi.ProductID = p.ProductID
JOIN Orders o      ON o.OrderID = oi.OrderID
WHERE o.Status != 'Cancelled'
GROUP BY p.ProductID, p.ProductName
ORDER BY UnitsSold DESC;

-- Q4: How much revenue did each seller (store) make?
SELECT s.StoreName,
       SUM(oi.Quantity * oi.UnitPrice) AS Revenue
FROM Sellers s
JOIN Products p    ON p.SellerID = s.SellerID
JOIN OrderItems oi ON oi.ProductID = p.ProductID
JOIN Orders o      ON o.OrderID = oi.OrderID
WHERE o.Status != 'Cancelled'
GROUP BY s.SellerID, s.StoreName
ORDER BY Revenue DESC;

-- Q5: What is the average rating and number of reviews of each reviewed product?
SELECT p.ProductName,
       COUNT(r.ReviewID) AS ReviewsCount,
       AVG(r.Rating * 1.0) AS AvgRating
FROM Products p
JOIN Reviews r ON r.ProductID = p.ProductID
GROUP BY p.ProductID, p.ProductName
ORDER BY AvgRating DESC;

-- Q6: Which products have never been reviewed?
SELECT p.ProductID, p.ProductName
FROM Products p
LEFT JOIN Reviews r ON r.ProductID = p.ProductID
WHERE r.ReviewID IS NULL;

-- Q7: Which products have low stock (fewer than 50 units)?
SELECT p.ProductName, i.QuantityInStock
FROM Products p
JOIN Inventory i ON i.ProductID = p.ProductID
WHERE i.QuantityInStock < 50
ORDER BY i.QuantityInStock;

-- Q8: How many orders are there in each status?
SELECT Status, COUNT(*) AS OrdersCount
FROM Orders
GROUP BY Status;

-- Q9: How many orders used each coupon and how much discount did it give?
SELECT cp.Code,
       COUNT(o.OrderID)     AS OrdersUsed,
       SUM(o.DiscountAmount) AS TotalDiscount
FROM Coupons cp
JOIN Orders o ON o.CouponID = cp.CouponID
GROUP BY cp.CouponID, cp.Code;

-- Q10: How many times was each product returned and how much was refunded?
SELECT p.ProductName,
       COUNT(r.ReturnID)    AS ReturnRequests,
       SUM(r.RefundAmount)  AS TotalRefunded
FROM Products p
JOIN Returns r ON r.ProductID = p.ProductID
GROUP BY p.ProductID, p.ProductName;

-- Q11: Which payments are not completed (pending, failed or refunded) and for which order?
SELECT p.PaymentID, p.OrderID, p.Method, p.Amount, p.Status
FROM Payments p
WHERE p.Status != 'Completed';