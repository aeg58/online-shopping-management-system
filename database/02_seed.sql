USE shoppingmngmntsystem;
GO

/* ============================ 1. Roles ============================ */
INSERT INTO Roles (RoleName) VALUES ('Admin'),('Seller'),('Customer');

/* ============================ 2. Users ============================ */
INSERT INTO Users (RoleID, Username, Email, PasswordHash, IsActive, CreatedAt) VALUES
(1, 'admin',          'admin@shop.com',            'hash_admin_0001', 1, '2026-01-05'),
(2, 'techworld',      'contact@techworld.com',     'hash_sell_0002',  1, '2026-01-20'),
(2, 'fashionhub',     'info@fashionhub.com',       'hash_sell_0003',  1, '2026-01-25'),
(2, 'homeandbooks',   'hello@homeandbooks.com',    'hash_sell_0004',  1, '2026-02-01'),
(2, 'fitlife',        'support@fitlife.com',       'hash_sell_0005',  1, '2026-02-08'),
(3, 'ali_yilmaz',     'ali.yilmaz@mail.com',       'hash_cust_0006',  1, '2026-02-10'),
(3, 'ayse_demir',     'ayse.demir@mail.com',       'hash_cust_0007',  1, '2026-02-12'),
(3, 'mehmet_kaya',    'mehmet.kaya@mail.com',      'hash_cust_0008',  1, '2026-03-01'),
(3, 'zeynep_arslan',  'zeynep.arslan@mail.com',    'hash_cust_0009',  1, '2026-03-15'),
(3, 'can_ozturk',     'can.ozturk@mail.com',       'hash_cust_0010',  1, '2026-04-02'),
(3, 'elif_sahin',     'elif.sahin@mail.com',       'hash_cust_0011',  1, '2026-04-20'),
(3, 'burak_celik',    'burak.celik@mail.com',      'hash_cust_0012',  1, '2026-05-07'),
(3, 'deniz_aydin',    'deniz.aydin@mail.com',      'hash_cust_0013',  0, '2026-05-30');

/* ========================== 3. Customers ========================== */
INSERT INTO Customers (UserID, FirstName, LastName, Phone, BirthDate) VALUES
(6,  'Ali',    'Yilmaz', '+90 532 111 1111', '1998-04-12'),
(7,  'Ayse',   'Demir',  '+90 533 222 2222', '2000-09-25'),
(8,  'Mehmet', 'Kaya',   '+90 534 333 3333', '1995-01-30'),
(9,  'Zeynep', 'Arslan', '+90 535 444 4444', '2001-06-18'),
(10, 'Can',    'Ozturk', '+90 536 555 5555', '1997-11-03'),
(11, 'Elif',   'Sahin',  '+90 537 666 6666', '1999-02-14'),
(12, 'Burak',  'Celik',  '+90 538 777 7777', '1994-08-09'),
(13, 'Deniz',  'Aydin',  NULL,               NULL);

/* ========================== 4. Addresses ========================== */
INSERT INTO Addresses (CustomerID, Title, Country, City, District, AddressLine, PostalCode, IsDefault) VALUES
(1, 'Home',   'Turkey', 'Istanbul', 'Fatih',    'Ataturk Cad. No:12 D:3',     '34080', 1),
(2, 'Home',   'Turkey', 'Ankara',   'Cankaya',  'Kizilay Sk. No:7 D:5',       '06420', 1),
(3, 'Work',   'Turkey', 'Izmir',    'Konak',    'Cumhuriyet Bul. No:45',      '35250', 1),
(4, 'Home',   'Turkey', 'Bursa',    'Nilufer',  'Ozluce Mah. 15. Sk. No:9',   '16110', 1),
(5, 'Home',   'Turkey', 'Istanbul', 'Kadikoy',  'Moda Cad. No:30 D:2',        '34710', 1),
(6, 'Home',   'Turkey', 'Antalya',  'Muratpasa','Lara Cad. No:88 D:4',        '07230', 1),
(7, 'Home',   'Turkey', 'Konya',    'Selcuklu', 'Yazir Mah. 21. Sk. No:6',    '42070', 1),
(8, 'Home',   'Turkey', 'Eskisehir','Tepebasi', 'Hoyuk Mah. Gul Sk. No:14',   NULL,    1),
(1, 'Office', 'Turkey', 'Istanbul', 'Sisli',    'Buyukdere Cad. No:100 K:7',  '34394', 0);

/* =========================== 5. Sellers =========================== */
INSERT INTO Sellers (UserID, StoreName, TaxNumber, Phone, IsApproved) VALUES
(2, 'TechWorld',    '1234567890', '+90 212 100 1000', 1),
(3, 'FashionHub',   '2345678901', '+90 212 200 2000', 1),
(4, 'Home & Books', '3456789012', '+90 212 300 3000', 1),
(5, 'FitLife',      '4567890123', '+90 212 400 4000', 1);

/* ========================= 6. Categories ========================== */
INSERT INTO Categories (CategoryName, Description) VALUES
('Electronics',    'Computers, accessories and gadgets'),
('Clothing',       'Shoes and apparel for all seasons'),
('Books',          'Novels, textbooks and magazines'),
('Home & Kitchen', 'Appliances and household items'),
('Sports',         'Fitness and outdoor equipment'),
('Beauty',         'Skin care and personal care');

/* ========================== 7. Products =========================== */
INSERT INTO Products (SellerID, CategoryID, ProductName, Description, Price, IsActive, CreatedAt) VALUES
(1, 1, 'Wireless Mouse',        '2.4GHz ergonomic wireless mouse',         24.99, 1, '2026-02-01'),
(1, 1, 'Mechanical Keyboard',   'RGB mechanical keyboard, blue switch',    79.90, 1, '2026-02-01'),
(1, 1, 'USB-C Hub',             '7-in-1 USB-C multiport adapter',          39.50, 1, '2026-02-02'),
(1, 1, 'Bluetooth Headphones',  'Over-ear noise cancelling headphones',   129.00, 1, '2026-02-03'),
(2, 2, 'Running Shoes',         'Lightweight breathable running shoes',    89.00, 1, '2026-02-10'),
(2, 2, 'Cotton T-Shirt',        '100% cotton crew neck t-shirt',           14.99, 1, '2026-02-10'),
(2, 2, 'Denim Jacket',          'Classic fit blue denim jacket',           59.00, 1, '2026-02-11'),
(2, 2, 'Wool Scarf',            'Soft winter wool scarf',                  19.50, 1, '2026-02-12'),
(3, 3, 'The Silent River',      'Bestselling contemporary novel',          12.50, 1, '2026-02-15'),
(3, 3, 'SQL Fundamentals',      'Database textbook for beginners',         45.00, 1, '2026-02-15'),
(3, 4, 'Coffee Maker',          'Programmable 12-cup coffee maker',        65.00, 1, '2026-02-16'),
(3, 4, 'Non-stick Pan Set',     '3-piece non-stick frying pan set',        74.90, 1, '2026-02-17'),
(4, 5, 'Yoga Mat',              'Non-slip 6mm yoga mat',                   22.00, 1, '2026-03-01'),
(4, 5, 'Dumbbell Set',          'Adjustable dumbbell set, 2 x 5 kg',       49.99, 1, '2026-03-01'),
(4, 5, 'Protein Shaker',        '700ml shaker bottle with mixer ball',      9.99, 1, '2026-03-02'),
(4, 6, 'Face Moisturizer',      'Daily hydrating face cream, 50ml',        18.75, 0, '2026-03-05');

/* ========================== 8. Inventory ========================== */
INSERT INTO Inventory (ProductID, QuantityInStock, LastUpdated) VALUES
(1,  150, '2026-11-01'),
(2,   60, '2026-11-01'),
(3,   95, '2026-11-01'),
(4,   40, '2026-11-01'),
(5,   45, '2026-11-01'),
(6,  300, '2026-11-01'),
(7,   25, '2026-11-01'),
(8,   80, '2026-11-01'),
(9,  120, '2026-11-01'),
(10,  70, '2026-11-01'),
(11,  35, '2026-11-01'),
(12,  30, '2026-11-01'),
(13,  80, '2026-11-01'),
(14,  50, '2026-11-01'),
(15, 200, '2026-11-01'),
(16,   0, '2026-11-01');

/* ========================== 9. Carts ============================== */
INSERT INTO Carts (CustomerID, Status, CreatedAt) VALUES
(1, 'CheckedOut', '2026-09-30'),
(2, 'CheckedOut', '2026-10-04'),
(3, 'Active',     '2026-11-10'),
(4, 'Active',     '2026-11-12'),
(5, 'Abandoned',  '2026-11-01'),
(6, 'CheckedOut', '2026-10-24'),
(7, 'Active',     '2026-11-15'),
(1, 'Active',     '2026-11-17');

/* ========================= 10. CartItems ========================== */
INSERT INTO CartItems (CartID, ProductID, Quantity) VALUES
(1, 1,  1),
(1, 2,  1),
(2, 5,  1),
(2, 6,  2),
(3, 3,  1),
(3, 10, 1),
(4, 9,  2),
(5, 13, 1),
(5, 15, 1),
(6, 4,  1),
(6, 3,  1),
(7, 14, 1),
(8, 8,  1),
(8, 16, 2);

/* ========================== 11. Coupons =========================== */
INSERT INTO Coupons (Code, DiscountType, DiscountValue, MinOrderAmount, StartDate, EndDate, UsageLimit, UsedCount, IsActive) VALUES
('WELCOME10',     'Percent', 10.00,  50.00, '2026-01-01', '2026-12-31', 1000,  3, 1),
('SAVE20',        'Fixed',   20.00, 100.00, '2026-06-01', '2026-12-31',  500,  2, 1),
('SUMMER15',      'Percent', 15.00,  80.00, '2026-06-01', '2026-08-31',  200, 40, 0),
('BLACKFRIDAY25', 'Percent', 25.00, 150.00, '2026-11-20', '2026-11-30',  300,  0, 1);

/* ===================== 12. ShippingCompanies ====================== */
INSERT INTO ShippingCompanies (CompanyName, Phone, BaseFee) VALUES
('Yurtici Kargo', '+90 444 99 99', 4.99),
('Aras Kargo',    '+90 444 25 52', 3.99),
('MNG Kargo',     '+90 444 06 06', 4.49);

/* ============================ 13. Orders ========================== */
INSERT INTO Orders (CustomerID, AddressID, CouponID, OrderDate, Status, DiscountAmount, TotalAmount) VALUES
(1, 1, 1,    '2026-10-01', 'Delivered', 10.49,  94.40),
(2, 2, NULL, '2026-10-05', 'Delivered',  0.00, 118.98),
(3, 3, 2,    '2026-10-10', 'Delivered', 20.00,  90.00),
(4, 4, NULL, '2026-10-15', 'Pending',    0.00,  25.00),
(5, 5, NULL, '2026-10-20', 'Delivered',  0.00,  81.00),
(6, 6, 1,    '2026-10-25', 'Shipped',   16.85, 151.65),
(7, 7, NULL, '2026-11-02', 'Paid',       0.00,  69.97),
(8, 8, 2,    '2026-11-05', 'Delivered', 20.00,  93.15),
(1, 9, 1,    '2026-11-08', 'Cancelled',  5.90,  53.10),
(2, 2, NULL, '2026-11-12', 'Delivered',  0.00, 118.73);

/* ========================= 14. OrderItems ========================= */
INSERT INTO OrderItems (OrderID, ProductID, Quantity, UnitPrice) VALUES
(1,  1,  1,  24.99),
(1,  2,  1,  79.90),
(2,  5,  1,  89.00),
(2,  6,  2,  14.99),
(3,  11, 1,  65.00),
(3,  10, 1,  45.00),
(4,  9,  2,  12.50),
(5,  7,  1,  59.00),
(5,  13, 1,  22.00),
(6,  4,  1, 129.00),
(6,  3,  1,  39.50),
(7,  14, 1,  49.99),
(7,  15, 2,   9.99),
(8,  12, 1,  74.90),
(8,  8,  1,  19.50),
(8,  16, 1,  18.75),
(9,  7,  1,  59.00),
(10, 14, 2,  49.99),
(10, 16, 1,  18.75);

/* ========================== 15. Payments ========================== */
INSERT INTO Payments (OrderID, Method, Amount, Status, PaidAt) VALUES
(1,  'CreditCard',     94.40, 'Completed', '2026-10-01'),
(2,  'DebitCard',     118.98, 'Completed', '2026-10-05'),
(3,  'CreditCard',     90.00, 'Completed', '2026-10-10'),
(4,  'CashOnDelivery', 25.00, 'Pending',   NULL),
(5,  'BankTransfer',   81.00, 'Completed', '2026-10-20'),
(6,  'CreditCard',    151.65, 'Completed', '2026-10-25'),
(7,  'CreditCard',     69.97, 'Failed',    NULL),
(7,  'DebitCard',      69.97, 'Completed', '2026-11-02'),
(8,  'CreditCard',     93.15, 'Completed', '2026-11-05'),
(9,  'CreditCard',     53.10, 'Refunded',  '2026-11-08'),
(10, 'BankTransfer',  118.73, 'Completed', '2026-11-12');

/* ========================== 16. Shipments ========================= */
INSERT INTO Shipments (OrderID, ShippingCompanyID, TrackingNumber, Status, ShippedDate, DeliveredDate) VALUES
(1,  1, '100000000001', 'Delivered', '2026-10-02', '2026-10-04'),
(2,  2, '200000000002', 'Delivered', '2026-10-06', '2026-10-08'),
(3,  1, '100000000003', 'Delivered', '2026-10-11', '2026-10-13'),
(5,  3, '300000000004', 'Delivered', '2026-10-21', '2026-10-24'),
(6,  2, '200000000005', 'InTransit', '2026-10-26', NULL),
(8,  3, '300000000006', 'Delivered', '2026-11-06', '2026-11-09'),
(10, 1, '100000000007', 'Delivered', '2026-11-13', '2026-11-15');

/* ============================ 17. Reviews ========================= */
INSERT INTO Reviews (ProductID, CustomerID, Rating, Comment, CreatedAt) VALUES
(1,  1, 5, 'Works perfectly, very comfortable.',          '2026-10-06'),
(2,  1, 4, 'Great keyboard but a bit loud.',              '2026-10-06'),
(5,  2, 5, 'Very light, perfect for daily runs.',         '2026-10-10'),
(11, 3, 5, 'Makes great coffee, easy to clean.',          '2026-10-15'),
(10, 3, 4, 'Clear explanations, good for beginners.',     '2026-10-15'),
(7,  5, 3, 'Nice jacket, runs slightly small.',           '2026-10-26'),
(12, 8, 4, 'Good quality pans for the price.',            '2026-11-12'),
(14, 2, 5, 'Solid dumbbells, great value.',               '2026-11-17');

/* ============================ 18. Returns ========================= */
INSERT INTO Returns (OrderID, ProductID, Quantity, Reason, Status, RequestDate, RefundAmount) VALUES
(5,  13, 1, 'Mat too thin for my needs',       'Approved',  '2026-10-27', 22.00),
(2,  6,  1, 'Wrong size',                      'Refunded',  '2026-10-12', 14.99),
(10, 16, 1, 'Not suitable for my skin type',   'Requested', '2026-11-16', NULL);