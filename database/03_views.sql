USE shoppingmngmntsystem
GO

SELECT u.UserID,
       u.Username,
       u.Email,
       r.RoleName,
       u.IsActive,
       u.CreatedAt
FROM Users u
JOIN Roles r ON r.RoleID = u.RoleID;

SELECT p.ProductID,
       p.ProductName,
       p.Price,
       p.IsActive,
       c.CategoryName,
       s.StoreName,
       i.QuantityInStock
FROM Products p
JOIN Categories c ON c.CategoryID = p.CategoryID
JOIN Sellers    s ON s.SellerID   = p.SellerID
JOIN Inventory  i ON i.ProductID  = p.ProductID;

SELECT o.OrderID,
       o.OrderDate,
       o.Status AS OrderStatus,
       c.CustomerID,
       c.FirstName + ' ' + c.LastName AS CustomerName,
       a.City,
       cp.Code AS CouponCode,
       o.DiscountAmount,
       o.TotalAmount
FROM Orders o
JOIN Customers c  ON c.CustomerID = o.CustomerID
JOIN Addresses a  ON a.AddressID  = o.AddressID
LEFT JOIN Coupons cp ON cp.CouponID = o.CouponID;

SELECT oi.OrderItemID,
       oi.OrderID,
       o.OrderDate,
       o.Status AS OrderStatus,
       c.FirstName + ' ' + c.LastName AS CustomerName,
       p.ProductID,
       p.ProductName,
       oi.Quantity,
       oi.UnitPrice,
       oi.Quantity * oi.UnitPrice AS LineTotal
FROM OrderItems oi
JOIN Orders    o ON o.OrderID    = oi.OrderID
JOIN Customers c ON c.CustomerID = o.CustomerID
JOIN Products  p ON p.ProductID  = oi.ProductID;

SELECT pay.PaymentID,
       o.OrderID,
       c.FirstName + ' ' + c.LastName AS CustomerName,
       pay.Method,
       pay.Amount,
       pay.Status AS PaymentStatus,
       pay.PaidAt
FROM Payments pay
JOIN Orders    o ON o.OrderID    = pay.OrderID
JOIN Customers c ON c.CustomerID = o.CustomerID;

SELECT s.ShipmentID,
       s.OrderID,
       c.FirstName + ' ' + c.LastName AS CustomerName,
       a.City,
       sc.CompanyName,
       s.TrackingNumber,
       s.Status AS ShipmentStatus,
       s.ShippedDate,
       s.DeliveredDate
FROM Shipments s
JOIN Orders            o  ON o.OrderID           = s.OrderID
JOIN Customers         c  ON c.CustomerID        = o.CustomerID
JOIN Addresses         a  ON a.AddressID         = o.AddressID
JOIN ShippingCompanies sc ON sc.ShippingCompanyID = s.ShippingCompanyID;

SELECT r.ReviewID,
       p.ProductID,
       p.ProductName,
       c.FirstName + ' ' + c.LastName AS CustomerName,
       r.Rating,
       r.Comment,
       r.CreatedAt
FROM Reviews r
JOIN Products  p ON p.ProductID  = r.ProductID
JOIN Customers c ON c.CustomerID = r.CustomerID;

SELECT rt.ReturnID,
       rt.OrderID,
       c.FirstName + ' ' + c.LastName AS CustomerName,
       p.ProductName,
       rt.Quantity,
       rt.Reason,
       rt.Status AS ReturnStatus,
       rt.RequestDate,
       rt.RefundAmount
FROM Returns rt
JOIN Orders    o ON o.OrderID    = rt.OrderID
JOIN Customers c ON c.CustomerID = o.CustomerID
JOIN Products  p ON p.ProductID  = rt.ProductID;

SELECT ca.CartID,
       c.FirstName + ' ' + c.LastName AS CustomerName,
       p.ProductName,
       ci.Quantity,
       p.Price,
       ci.Quantity * p.Price AS LineTotal
FROM Carts ca
JOIN Customers c  ON c.CustomerID = ca.CustomerID
JOIN CartItems ci ON ci.CartID    = ca.CartID
JOIN Products  p  ON p.ProductID  = ci.ProductID
WHERE ca.Status = 'Active';