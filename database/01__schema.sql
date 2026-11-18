/* =====================================================================
   Online Shopping Management System - Schema (SQL Server)
   Author: Mohammed (Database Design)
   File  : database/01_schema.sql

   BUSINESS RULES
   BR1  Every user has exactly one role: Admin, Seller or Customer.
   BR2  A Customer or Seller profile belongs to exactly one User (1:1).
   BR3  A customer can have many addresses; an order ships to one of them.
   BR4  Every product belongs to one seller and one category.
   BR5  Stock can never be negative; each product has one inventory row.
   BR6  A customer has at most one ACTIVE cart;
        a product appears only once in a cart.
   BR7  Order items store the price at purchase time (UnitPrice).
   BR8  A coupon is valid only between StartDate and EndDate and until
        UsageLimit is reached.
        Percent discounts are between 1 and 100.
   BR9  An order can have one or more payments and at most one shipment.
   BR10 A customer can review a product only once, rating 1-5.
   BR11 A return refers to one specific order item.
   BR12 Prices, quantities and amounts cannot be negative.
   BR13 A seller can only change the stock of his own products.
   ===================================================================== */

--------------------------------------------------------------------
-- End of Section 1


-- Begining of Section 2
--------------------------------------------------------------------

/* ============================ Database Creation ============================ */
   CREATE DATABASE shoppingmngmntsystem;
GO

USE shoppingmngmntsystem;
GO

/* ============================ 1. Roles ============================ */
CREATE TABLE Roles (
    RoleID    INT IDENTITY(1,1),
    RoleName  VARCHAR(20) NOT NULL,

    CONSTRAINT PK_Roles PRIMARY KEY (RoleID),
    CONSTRAINT UQ_Roles_RoleName UNIQUE (RoleName),
    CONSTRAINT CK_Roles_RoleName CHECK (RoleName IN ('Admin', 'Seller', 'Customer'))
);

/* ============================ 2. Users ============================ */
CREATE TABLE Users (
    UserID        INT IDENTITY(1,1),
    RoleID        INT NOT NULL,
    Username      VARCHAR(50) NOT NULL,
    Email         VARCHAR(100) NOT NULL,
    PasswordHash  VARCHAR(255) NOT NULL,
    IsActive      BIT NOT NULL DEFAULT 1,
    CreatedAt     DATE NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_Users PRIMARY KEY (UserID),
    CONSTRAINT FK_Users_Roles FOREIGN KEY (RoleID) REFERENCES Roles(RoleID),
    CONSTRAINT UQ_Users_Username UNIQUE (Username),
    CONSTRAINT UQ_Users_Email UNIQUE (Email),
    CONSTRAINT CK_Users_Email CHECK (Email LIKE '%@%')
);

/* ========================== 3. Customers ========================== */
CREATE TABLE Customers (
    CustomerID  INT IDENTITY(1,1),
    UserID      INT NOT NULL,
    FirstName   VARCHAR(50) NOT NULL,
    LastName    VARCHAR(50) NOT NULL,
    Phone       VARCHAR(20),
    BirthDate   DATE,

    CONSTRAINT PK_Customers PRIMARY KEY (CustomerID),
    CONSTRAINT FK_Customers_Users FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT UQ_Customers_UserID UNIQUE (UserID),
    CONSTRAINT CK_Customers_BirthDate CHECK (BirthDate < GETDATE())
);

/* ========================== 4. Addresses ========================== */
CREATE TABLE Addresses (
    AddressID    INT IDENTITY(1,1),
    CustomerID   INT NOT NULL,
    Title        VARCHAR(50) NOT NULL,
    Country      VARCHAR(50) NOT NULL DEFAULT 'Turkey',
    City         VARCHAR(50) NOT NULL,
    District     VARCHAR(50) NOT NULL,
    AddressLine  VARCHAR(255) NOT NULL,
    PostalCode   VARCHAR(10),
    IsDefault    BIT NOT NULL DEFAULT 0,

    CONSTRAINT PK_Addresses PRIMARY KEY (AddressID),
    CONSTRAINT FK_Addresses_Customers FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID)
);

/* =========================== 5. Sellers =========================== */
CREATE TABLE Sellers (
    SellerID    INT IDENTITY(1,1),
    UserID      INT NOT NULL,
    StoreName   VARCHAR(100) NOT NULL,
    TaxNumber   VARCHAR(20) NOT NULL,
    Phone       VARCHAR(20),
    IsApproved  BIT NOT NULL DEFAULT 0,

    CONSTRAINT PK_Sellers PRIMARY KEY (SellerID),
    CONSTRAINT FK_Sellers_Users FOREIGN KEY (UserID) REFERENCES Users(UserID),
    CONSTRAINT UQ_Sellers_UserID UNIQUE (UserID),
    CONSTRAINT UQ_Sellers_StoreName UNIQUE (StoreName),
    CONSTRAINT UQ_Sellers_TaxNumber UNIQUE (TaxNumber)
);

/* ========================= 6. Categories ========================== */
CREATE TABLE Categories (
    CategoryID    INT IDENTITY(1,1),
    CategoryName  VARCHAR(100) NOT NULL,
    Description   VARCHAR(255),

    CONSTRAINT PK_Categories PRIMARY KEY (CategoryID),
    CONSTRAINT UQ_Categories_CategoryName UNIQUE (CategoryName)
);

/* ========================== 7. Products =========================== */
CREATE TABLE Products (
    ProductID    INT IDENTITY(1,1),
    SellerID     INT NOT NULL,
    CategoryID   INT NOT NULL,
    ProductName  VARCHAR(150) NOT NULL,
    Description  VARCHAR(1000),
    Price        DECIMAL(10,2) NOT NULL,
    IsActive     BIT NOT NULL DEFAULT 1,
    CreatedAt    DATE NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_Products PRIMARY KEY (ProductID),
    CONSTRAINT FK_Products_Sellers FOREIGN KEY (SellerID) REFERENCES Sellers(SellerID),
    CONSTRAINT FK_Products_Categories FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID),
    CONSTRAINT CK_Products_Price CHECK (Price >= 0)
);

/* ========================== 8. Inventory ========================== */
CREATE TABLE Inventory (
    InventoryID      INT IDENTITY(1,1),
    ProductID        INT NOT NULL,
    QuantityInStock  INT NOT NULL DEFAULT 0,
    LastUpdated      DATE NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_Inventory PRIMARY KEY (InventoryID),
    CONSTRAINT FK_Inventory_Products FOREIGN KEY (ProductID) REFERENCES Products(ProductID),
    CONSTRAINT UQ_Inventory_ProductID UNIQUE (ProductID),
    CONSTRAINT CK_Inventory_Quantity CHECK (QuantityInStock >= 0)
);

/* ============================ 9. Carts ============================ */
CREATE TABLE Carts (
    CartID      INT IDENTITY(1,1),
    CustomerID  INT NOT NULL,
    Status      VARCHAR(20) NOT NULL DEFAULT 'Active',
    CreatedAt   DATE NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_Carts PRIMARY KEY (CartID),
    CONSTRAINT FK_Carts_Customers FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID),
    CONSTRAINT CK_Carts_Status CHECK (Status IN ('Active', 'CheckedOut', 'Abandoned'))
);

/* ========================== 10. CartItems ========================= */
CREATE TABLE CartItems (
    CartItemID  INT IDENTITY(1,1),
    CartID      INT NOT NULL,
    ProductID   INT NOT NULL,
    Quantity    INT NOT NULL,

    CONSTRAINT PK_CartItems PRIMARY KEY (CartItemID),
    CONSTRAINT FK_CartItems_Carts FOREIGN KEY (CartID) REFERENCES Carts(CartID),
    CONSTRAINT FK_CartItems_Products FOREIGN KEY (ProductID) REFERENCES Products(ProductID),
    CONSTRAINT UQ_CartItems_Cart_Product UNIQUE (CartID, ProductID),
    CONSTRAINT CK_CartItems_Quantity CHECK (Quantity > 0)
);

/* =========================== 11. Coupons ========================== */
CREATE TABLE Coupons (
    CouponID        INT IDENTITY(1,1),
    Code            VARCHAR(30) NOT NULL,
    DiscountType    VARCHAR(10) NOT NULL,
    DiscountValue   DECIMAL(10,2) NOT NULL,
    MinOrderAmount  DECIMAL(10,2) NOT NULL DEFAULT 0,
    StartDate       DATE NOT NULL,
    EndDate         DATE NOT NULL,
    UsageLimit      INT NOT NULL DEFAULT 100,
    UsedCount       INT NOT NULL DEFAULT 0,
    IsActive        BIT NOT NULL DEFAULT 1,

    CONSTRAINT PK_Coupons PRIMARY KEY (CouponID),
    CONSTRAINT UQ_Coupons_Code UNIQUE (Code),
    CONSTRAINT CK_Coupons_DiscountType CHECK (DiscountType IN ('Percent', 'Fixed')),
    CONSTRAINT CK_Coupons_DiscountValue CHECK (DiscountValue > 0),
    CONSTRAINT CK_Coupons_Percent CHECK (DiscountType <> 'Percent' OR DiscountValue <= 100),
    CONSTRAINT CK_Coupons_MinOrderAmount CHECK (MinOrderAmount >= 0),
    CONSTRAINT CK_Coupons_Dates CHECK (EndDate >= StartDate),
    CONSTRAINT CK_Coupons_Usage CHECK (UsageLimit > 0 AND UsedCount >= 0 AND UsedCount <= UsageLimit)
);

/* ============================ 12. Orders ========================== */
CREATE TABLE Orders (
    OrderID         INT IDENTITY(1,1),
    CustomerID      INT NOT NULL,
    AddressID       INT NOT NULL,
    CouponID        INT,
    OrderDate       DATE NOT NULL DEFAULT GETDATE(),
    Status          VARCHAR(20) NOT NULL DEFAULT 'Pending',
    DiscountAmount  DECIMAL(10,2) NOT NULL DEFAULT 0,
    TotalAmount     DECIMAL(10,2) NOT NULL,

    CONSTRAINT PK_Orders PRIMARY KEY (OrderID),
    CONSTRAINT FK_Orders_Customers FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID),
    CONSTRAINT FK_Orders_Addresses FOREIGN KEY (AddressID) REFERENCES Addresses(AddressID),
    CONSTRAINT FK_Orders_Coupons FOREIGN KEY (CouponID) REFERENCES Coupons(CouponID),
    CONSTRAINT CK_Orders_Status CHECK (Status IN ('Pending', 'Paid', 'Shipped', 'Delivered', 'Cancelled')),
    CONSTRAINT CK_Orders_DiscountAmount CHECK (DiscountAmount >= 0),
    CONSTRAINT CK_Orders_TotalAmount CHECK (TotalAmount >= 0)
);

/* ========================== 13. OrderItems ======================== */
CREATE TABLE OrderItems (
    OrderItemID  INT IDENTITY(1,1),
    OrderID      INT NOT NULL,
    ProductID    INT NOT NULL,
    Quantity     INT NOT NULL,
    UnitPrice    DECIMAL(10,2) NOT NULL,

    CONSTRAINT PK_OrderItems PRIMARY KEY (OrderItemID),
    CONSTRAINT FK_OrderItems_Orders FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
    CONSTRAINT FK_OrderItems_Products FOREIGN KEY (ProductID) REFERENCES Products(ProductID),
    CONSTRAINT UQ_OrderItems_Order_Product UNIQUE (OrderID, ProductID),
    CONSTRAINT CK_OrderItems_Quantity CHECK (Quantity > 0),
    CONSTRAINT CK_OrderItems_UnitPrice CHECK (UnitPrice >= 0)
);

/* =========================== 14. Payments ========================= */
CREATE TABLE Payments (
    PaymentID  INT IDENTITY(1,1),
    OrderID    INT NOT NULL,
    Method     VARCHAR(20) NOT NULL,
    Amount     DECIMAL(10,2) NOT NULL,
    Status     VARCHAR(20) NOT NULL DEFAULT 'Pending',
    PaidAt     DATE,

    CONSTRAINT PK_Payments PRIMARY KEY (PaymentID),
    CONSTRAINT FK_Payments_Orders FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
    CONSTRAINT CK_Payments_Method CHECK (Method IN ('CreditCard', 'DebitCard', 'BankTransfer', 'CashOnDelivery')),
    CONSTRAINT CK_Payments_Status CHECK (Status IN ('Pending', 'Completed', 'Failed', 'Refunded')),
    CONSTRAINT CK_Payments_Amount CHECK (Amount > 0)
);

/* ====================== 15. ShippingCompanies ===================== */
CREATE TABLE ShippingCompanies (
    ShippingCompanyID  INT IDENTITY(1,1),
    CompanyName        VARCHAR(100) NOT NULL,
    Phone              VARCHAR(20),
    BaseFee            DECIMAL(10,2) NOT NULL DEFAULT 0,

    CONSTRAINT PK_ShippingCompanies PRIMARY KEY (ShippingCompanyID),
    CONSTRAINT UQ_ShippingCompanies_CompanyName UNIQUE (CompanyName),
    CONSTRAINT CK_ShippingCompanies_BaseFee CHECK (BaseFee >= 0)
);

/* =========================== 16. Shipments ======================== */
CREATE TABLE Shipments (
    ShipmentID         INT IDENTITY(1,1),
    OrderID            INT NOT NULL,
    ShippingCompanyID  INT NOT NULL,
    TrackingNumber     VARCHAR(50) NOT NULL,
    Status             VARCHAR(20) NOT NULL DEFAULT 'Preparing',
    ShippedDate        DATE,
    DeliveredDate      DATE,

    CONSTRAINT PK_Shipments PRIMARY KEY (ShipmentID),
    CONSTRAINT FK_Shipments_Orders FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
    CONSTRAINT FK_Shipments_ShippingCompanies FOREIGN KEY (ShippingCompanyID) REFERENCES ShippingCompanies(ShippingCompanyID),
    CONSTRAINT UQ_Shipments_OrderID UNIQUE (OrderID),
    CONSTRAINT UQ_Shipments_TrackingNumber UNIQUE (TrackingNumber),
    CONSTRAINT CK_Shipments_Status CHECK (Status IN ('Preparing', 'InTransit', 'Delivered', 'Returned')),
    CONSTRAINT CK_Shipments_Dates CHECK (DeliveredDate >= ShippedDate)
);

/* ============================ 17. Reviews ========================= */
CREATE TABLE Reviews (
    ReviewID    INT IDENTITY(1,1),
    ProductID   INT NOT NULL,
    CustomerID  INT NOT NULL,
    Rating      INT NOT NULL,
    Comment     VARCHAR(1000),
    CreatedAt   DATE NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_Reviews PRIMARY KEY (ReviewID),
    CONSTRAINT FK_Reviews_Products FOREIGN KEY (ProductID) REFERENCES Products(ProductID),
    CONSTRAINT FK_Reviews_Customers FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID),
    CONSTRAINT UQ_Reviews_Product_Customer UNIQUE (ProductID, CustomerID),
    CONSTRAINT CK_Reviews_Rating CHECK (Rating BETWEEN 1 AND 5)
);

/* ============================ 18. Returns ========================= */
CREATE TABLE Returns (
    ReturnID      INT IDENTITY(1,1),
    OrderID       INT NOT NULL,
    ProductID     INT NOT NULL,
    Quantity      INT NOT NULL,
    Reason        VARCHAR(255) NOT NULL,
    Status        VARCHAR(20) NOT NULL DEFAULT 'Requested',
    RequestDate   DATE NOT NULL DEFAULT GETDATE(),
    RefundAmount  DECIMAL(10,2),

    CONSTRAINT PK_Returns PRIMARY KEY (ReturnID),
    CONSTRAINT FK_Returns_Orders FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
    CONSTRAINT FK_Returns_Products FOREIGN KEY (ProductID) REFERENCES Products(ProductID),
    -- the product must really be part of that order (BR11)
    CONSTRAINT FK_Returns_OrderItems FOREIGN KEY (OrderID, ProductID) REFERENCES OrderItems(OrderID, ProductID),
    CONSTRAINT CK_Returns_Quantity CHECK (Quantity > 0),
    CONSTRAINT CK_Returns_Status CHECK (Status IN ('Requested', 'Approved', 'Rejected', 'Refunded')),
    CONSTRAINT CK_Returns_RefundAmount CHECK (RefundAmount >= 0)
);