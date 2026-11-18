-- Sample data: Online Shopping Management System
-- Insert order respects foreign keys. Works in MySQL / PostgreSQL / SQL Server
-- (if a table name like Returns or Orders is rejected, wrap it in quotes/backticks/brackets).

-- 1. Roles
INSERT INTO Roles (RoleName) VALUES ('Admin'), ('Seller'), ('Customer');

-- 2. Users (1 admin, 5 customers, 3 sellers)
INSERT INTO Users ( RoleID, Username, Email, PasswordHash, IsActive, CreatedAt) VALUES
(1, 'admin',      'admin@shop.com',      'hash_admin_001', 1, '2026-01-05 09:00:00'),
(2, 'ali_yilmaz', 'ali.yilmaz@mail.com', 'hash_cust_002',  1, '2026-02-10 10:15:00'),
(2, 'ayse_demir', 'ayse.demir@mail.com', 'hash_cust_003',  1, '2026-02-12 14:30:00'),
(2, 'mehmet_kaya','mehmet.kaya@mail.com','hash_cust_004',  1, '2026-03-01 11:45:00'),
(2, 'zeynep_arslan','zeynep.arslan@mail.com','hash_cust_005',1,'2026-03-15 16:20:00'),
(2, 'can_ozturk',  'can.ozturk@mail.com','hash_cust_006',  1, '2026-04-02 08:50:00'),
(3, 'techworld',   'contact@techworld.com','hash_sell_007',1, '2026-01-20 12:00:00'),
(3, 'fashionhub',  'info@fashionhub.com', 'hash_sell_008', 1, '2026-01-25 13:30:00'),
(3, 'homeandbooks','hello@homeandbooks.com','hash_sell_009',1,'2026-02-01 09:40:00');

-- 3. Customers
INSERT INTO Customers ( UserID, FirstName, LastName, Phone, BirthDate) VALUES
(2, 'Ali',    'Yilmaz', '+90 532 111 1111', '1998-04-12'),
(3, 'Ayse',   'Demir',  '+90 533 222 2222', '2000-09-25'),
(4, 'Mehmet', 'Kaya',   '+90 534 333 3333', '1995-01-30'),
(5, 'Zeynep', 'Arslan', '+90 535 444 4444', '2001-06-18'),
(6, 'Can',    'Ozturk', '+90 536 555 5555', '1997-11-03');

-- 4. Sellers
INSERT INTO Sellers (UserID, StoreName, TaxNumber, Phone, IsApproved) VALUES
(7, 'TechWorld',     '1234567890', '+90 212 100 1000', 1),
(8, 'FashionHub',    '2345678901', '+90 212 200 2000', 1),
(9, 'Home & Books',  '3456789012', '+90 212 300 3000', 1);

-- 5. Categories
INSERT INTO Categories (CategoryName, Description) VALUES
('Electronics',      'Computers, accessories and gadgets'),
('Clothing',         'Shoes and apparel for all seasons'),
('Books',            'Novels, textbooks and magazines'),
('Home & Kitchen',   'Appliances and household items'),
('Sports',           'Fitness and outdoor equipment');

-- 6. Addresses
INSERT INTO Addresses (CustomerID, Title, Country, City, District, AddressLine, PostalCode, IsDefault) VALUES
(1, 'Home',   'Turkey', 'Istanbul', 'Fatih',     'Atatürk Cad. No:12 D:3',   '34080', 1),
(2, 'Home',   'Turkey', 'Ankara',   'Çankaya',   'Kızılay Sk. No:7 D:5',     '06420', 1),
(3, 'Work',   'Turkey', 'Izmir',    'Konak',     'Cumhuriyet Bul. No:45',    '35250', 1),
(4, 'Home',   'Turkey', 'Bursa',    'Nilüfer',   'Özlüce Mah. 15. Sk. No:9', '16110', 1),
(5, 'Home',   'Turkey', 'Istanbul', 'Kadıköy',   'Moda Cad. No:30 D:2',      '34710', 1),
(1, 'Office', 'Turkey', 'Istanbul', 'Şişli',     'Büyükdere Cad. No:100',    '34394', 0);

-- 7. Carts
INSERT INTO Carts (CustomerID, Status, CreatedAt) VALUES
(1, 'CheckedOut', '2026-09-30 18:00:00'),
(2, 'CheckedOut', '2026-10-04 20:10:00'),
(3, 'Active',    '2026-11-10 12:00:00'),
(4, 'Active',    '2026-11-12 09:30:00'),
(5, 'Abandoned', '2026-11-01 22:15:00');

-- 8. Products
INSERT INTO Products (SellerID, CategoryID, ProductName, Description, Price, IsActive, CreatedAt) VALUES
(1, 1, 'Wireless Mouse',       '2.4GHz ergonomic wireless mouse',      24.99, 1, '2026-02-01 10:00:00'),
(1, 1, 'Mechanical Keyboard',  'RGB mechanical keyboard, blue switch', 79.90, 1, '2026-02-01 10:05:00'),
(1, 1, 'USB-C Hub',            '7-in-1 USB-C multiport adapter',       39.50, 1, '2026-02-02 11:00:00'),
(2, 2, 'Running Shoes',        'Lightweight breathable running shoes', 89.00, 1, '2026-02-10 09:00:00'),
(2, 2, 'Cotton T-Shirt',       '100% cotton crew neck t-shirt',        14.99, 1, '2026-02-10 09:10:00'),
(2, 2, 'Denim Jacket',         'Classic fit blue denim jacket',        59.00, 1, '2026-02-11 09:20:00'),
(3, 3, 'The Silent River',     'Bestselling contemporary novel',       12.50, 1, '2026-02-15 15:00:00'),
(3, 3, 'SQL Fundamentals',     'Database textbook for beginners',      45.00, 1, '2026-02-15 15:10:00'),
(3, 4, 'Coffee Maker',         'Programmable 12-cup coffee maker',     65.00, 1, '2026-02-16 10:30:00'),
(3, 5, 'Yoga Mat',             'Non-slip 6mm yoga mat',                22.00, 1, '2026-02-16 10:40:00');

-- 9. Inventory
INSERT INTO Inventory (ProductID, QuantityInStock, LastUpdated) VALUES
(1, 150, '2026-11-01 08:00:00'),
(2,  60, '2026-11-01 08:00:00'),
(3,  95, '2026-11-01 08:00:00'),
(4,  40, '2026-11-01 08:00:00'),
(5, 300, '2026-11-01 08:00:00'),
(6,  25, '2026-11-01 08:00:00'),
(7, 120, '2026-11-01 08:00:00'),
(8,  70, '2026-11-01 08:00:00'),
(9,  35, '2026-11-01 08:00:00'),
(10, 80, '2026-11-01 08:00:00');

-- 10. Coupons
INSERT INTO Coupons (Code, DiscountType, DiscountValue, MinOrderAmount, StartDate, EndDate, UsageLimit, UsedCount, IsActive) VALUES
('WELCOME10', 'Percent', 10.00,  50.00, '2026-01-01', '2026-12-31', 1000, 1, 1),
('SAVE20',    'Fixed',      20.00, 100.00, '2026-06-01', '2026-12-31',  500, 1, 1),
('OLD5',      'Fixed',       5.00,  20.00, '2025-01-01', '2025-06-30',  100, 80, 0);

-- 11. CartItems
INSERT INTO CartItems (CartID, ProductID, Quantity) VALUES
(3, 1, 1),
(3, 2, 1),
(4, 4, 1),
(4, 5, 2),
(5, 3, 1),
(5, 8, 1),
(6, 7, 2),
(7, 10, 1);

-- 12. ShippingCompanies
INSERT INTO ShippingCompanies (CompanyName, Phone, BaseFee) VALUES
('Yurtiçi Kargo', '+90 444 99 99', 4.99),
('Aras Kargo',    '+90 444 25 52', 3.99),
('MNG Kargo',     '+90 444 06 06', 4.49);

-- 13. Orders (Total = sum of items - DiscountAmount)
INSERT INTO Orders (CustomerID, AddressID, CouponID, OrderDate, Status, DiscountAmount, TotalAmount) VALUES
(1, 1, 2,    '2026-10-01 10:00:00', 'Delivered', 10.49,  94.40),
(2, 2, NULL, '2026-10-05 13:20:00', 'Shipped',    0.00, 118.98),
(3, 3, 3,    '2026-10-10 17:45:00', 'Delivered', 20.00,  90.00),
(4, 4, NULL, '2026-10-15 09:10:00', 'Pending',    0.00,  25.00),
(5, 5, NULL, '2026-10-20 21:00:00', 'Delivered',  0.00,  81.00);

-- 14. OrderItems
INSERT INTO OrderItems (OrderID, ProductID, Quantity, UnitPrice) VALUES
(2, 1, 1, 24.99),
(2, 2, 1, 79.90),
(3, 4, 1, 89.00),
(3, 5, 2, 14.99),
(4, 9, 1, 65.00),
(4, 8, 1, 45.00),
(5, 7, 2, 12.50),
(6, 6, 1, 59.00),
(6, 10, 1, 22.00);

-- 15. Payments
INSERT INTO Payments (OrderID, Method, Amount, Status, PaidAt) VALUES
(2, 'CreditCard',      94.40, 'Completed',    '2026-10-01 10:02:00'),
(3, 'BankTransfer',          118.98, 'Completed',    '2026-10-05 13:22:00'),
(4, 'CreditCard',      90.00, 'Completed',    '2026-10-10 17:47:00'),
(5, 'CashOnDelivery', 25.00, 'Pending', NULL),
(6, 'DebitCard',       81.00, 'Completed',    '2026-10-20 21:03:00');

-- 16. Shipments (order 4 is still pending, so no shipment yet)
INSERT INTO Shipments (OrderID, ShippingCompanyID, TrackingNumber, Status, ShippedDate, DeliveredDate) VALUES
(2, 1, 'YK1000000001', 'Delivered',  '2026-10-02', '2026-10-04'),
(3, 2, 'AR2000000002', 'InTransit', '2026-10-06', NULL),
(4, 1, 'YK1000000003', 'Delivered',  '2026-10-11', '2026-10-13'),
(6, 3, 'MN3000000004', 'Delivered',  '2026-10-21', '2026-10-24');

-- 17. Reviews
INSERT INTO Reviews (ProductID, CustomerID, Rating, Comment, CreatedAt) VALUES
(1, 1, 5, 'Works perfectly, very comfortable.',        '2026-10-06 18:00:00'),
(2, 1, 4, 'Great keyboard but a bit loud.',            '2026-10-06 18:05:00'),
(9, 3, 5, 'Makes great coffee, easy to clean.',        '2026-10-15 08:30:00'),
(8, 3, 4, 'Clear explanations, good for beginners.',   '2026-10-15 08:40:00'),
(6, 5, 3, 'Nice jacket, runs slightly small.',         '2026-10-26 19:00:00');

-- 18. Returns
INSERT INTO Returns (OrderID, ProductID, Quantity, Reason, Status, RequestDate, RefundAmount) VALUES
(5, 7, 1, 'Mat too thin for my needs', 'Approved', '2026-10-27', 22.00);
