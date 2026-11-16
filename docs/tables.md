# Proposed tables (18)

Draft for the team to approve on 17 Nov. Mohammed finalises attributes and constraints in `database/01_schema.sql`.

| # | Table | Purpose | Main relationships |
|---|---|---|---|
| 1 | Roles | Admin, Seller, Customer | 1:N Users |
| 2 | Users | Login account for every person | N:1 Roles; 1:1 Customers or Sellers |
| 3 | Customers | Customer profile | 1:1 Users; 1:N Addresses, Orders, Reviews |
| 4 | Addresses | Delivery addresses of a customer | N:1 Customers |
| 5 | Sellers | Seller / store profile | 1:1 Users; 1:N Products |
| 6 | Categories | Product categories (can have a parent category) | 1:N Products; self-reference |
| 7 | Products | Items for sale | N:1 Sellers, Categories |
| 8 | Inventory | Stock quantity of each product | 1:1 Products |
| 9 | Carts | Open shopping cart of a customer | 1:1 Customers; 1:N CartItems |
| 10 | CartItems | Products in a cart (M:N Carts–Products) | N:1 Carts, Products |
| 11 | Orders | A placed order | N:1 Customers, Addresses, Coupons |
| 12 | OrderItems | Products in an order, with price at purchase (M:N Orders–Products) | N:1 Orders, Products |
| 13 | Payments | Payment of an order | N:1 Orders |
| 14 | ShippingCompanies | Cargo companies | 1:N Shipments |
| 15 | Shipments | Delivery of an order | N:1 Orders, ShippingCompanies |
| 16 | Reviews | Customer rating and comment on a product | N:1 Customers, Products |
| 17 | Coupons | Discount codes | 1:N Orders |
| 18 | Returns | Return request for an order item | N:1 OrderItems |

## Example business rules (to be completed in `business-rules.md`)

- A customer can review a product only if they have ordered it.
- A rating is between 1 and 5.
- Stock quantity can never be negative.
- An order must have at least one order item.
- A seller can only see and edit their own products.
- A coupon can only be used before its expiry date.
