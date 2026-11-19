# Manual test checklist

Run these steps on a fresh database: execute `database/01_schema.sql`, then `database/02_seed.sql`, then start the application (see `README.md`) and open <http://localhost:8080>.

The sample data uses placeholder passwords, which are compared as plain text.

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `hash_admin_001` |
| Seller | `techworld` | `hash_sell_007` |
| Seller | `fashionhub` | `hash_sell_008` |
| Customer | `ali_yilmaz` | `hash_cust_002` |
| Customer | `ayse_demir` | `hash_cust_003` |
| Customer | `mehmet_kaya` | `hash_cust_004` |

Each step lists the expected result after the arrow.

## 1. Login and access control

1. Open `/admin` without logging in → you are sent to the login page.
2. Log in with a wrong password → "Invalid username or password".
3. Log in as `ali_yilmaz` → you land on the shop and the menu shows Shop, Cart and Orders.
4. While logged in as a customer, open `/admin` and `/seller` → "You do not have permission to view this page".
5. Click Logout → "You have been logged out", and `/shop/cart` sends you to the login page again.

## 2. Customer

Log in as `ali_yilmaz`.

1. **Product list:** Choose the category Books → only books are listed. Search for `mouse` → only Wireless Mouse is listed.
2. **Product page:** Open Wireless Mouse → price, stock, average rating 5.0 and Ali's review are shown.
3. **Cart:** Add Mechanical Keyboard with quantity 2 → the cart shows the line total and the cart total.
4. **Stock limit:** In the cart, change the quantity to a number higher than the stock → "Not enough stock for ..." and the quantity does not change.
5. **Checkout:** Click Proceed to checkout, keep the Home address, enter coupon `WELCOME10`, choose Credit card and click Place order → "Order #N placed" and the order page shows a 10% discount and a Completed payment.
6. **Transaction result:** As admin, check that the product stock went down and that WELCOME10 was used one more time.
7. **Invalid coupon:** Put a product in the cart again and use coupon `OLD5` → "Coupon OLD5 is not active" and no order is created.
8. **Order history:** Open Orders → the new order is at the top. Open order #1 → items, payment, shipment (Yurtiçi Kargo) and address are shown.
9. **Return request:** In order #1 (Delivered), request a return of 1 Wireless Mouse with a reason → "Your return request was sent" and the request is listed as Requested. Requesting the same item again → "You can return at most 0 of this product".
10. **Review:** Log in as `ayse_demir`, open Running Shoes, choose 4 and submit → the review is listed and the form disappears.

## 3. Seller

Log in as `techworld`.

1. **Dashboard:** Products, units sold, revenue and the low stock list are shown.
2. **My products:** Only TechWorld products are listed.
3. **Add product:** Add a product with category Electronics, price 49.50 and stock 12 → "Product added" and the product is visible in the shop.
4. **Edit product:** Change the stock of Wireless Mouse and save → "Product updated".
5. **Hide product:** Clear "Show this product in the shop" and save → the product disappears from the shop but stays in My products as Inactive.
6. **Other seller's product:** Open `/seller/products/4/edit` (a FashionHub product) → "Page not found".
7. **Orders:** Only order lines with TechWorld products are shown.

## 4. Admin

Log in as `admin`.

1. **Dashboard:** User counts per role, total orders, revenue and the 5 latest orders are shown.
2. **Users:** Deactivate `ayse_demir` → she cannot log in ("This account is disabled"). Activate her again. Your own row shows "You" and has no button.
3. **Sellers:** Waiting sellers have an Approve button.
4. **Categories:** Add a category named `Toys` → "Category added".
5. **Coupons:** Add coupon `NEW15`, Percent, value 15, minimum 0, valid this month, usage limit 50 → "Coupon added".
6. **Shipment:** Open order #4 (Pending), choose Aras Kargo, enter a tracking number and click Create shipment → the order becomes Shipped. Click Mark as delivered → the order becomes Delivered and the cash on delivery payment becomes Completed.
7. **Cancel an order:** Open order #2 (Shipped) and change the status to Cancelled → its items go back into stock, the payment becomes Refunded and the shipment becomes Returned. The status cannot be changed again.
8. **Returns:** Approve the return request from step 2.9, then click Refund → the refund amount is quantity × unit price and the product stock goes up by the returned quantity.

## 5. Cases rejected by the database

These values are sent to SQL Server on purpose. The page names the constraint that rejected them.

| Where | What to enter | Expected message |
|---|---|---|
| Seller → Edit product | Price `-5` | The value breaks a database rule: CK_Products_Price |
| Seller → Add product | Stock `-1` | The value breaks a database rule: CK_Inventory_Quantity (the product is not created either) |
| Admin → Add coupon | Percent, value `150` | A percent discount cannot be more than 100 (CK_Coupons_Percent) |
| Admin → Add coupon | End date before start date | End date cannot be before start date (CK_Coupons_Dates) |
| Admin → Add category | Name `Books` | A category with this name already exists (UQ_Categories_CategoryName) |
| Admin → Order #4 → Create shipment (before step 4.6) | Tracking number `YK1000000001` | This tracking number is already used (UQ_Shipments_TrackingNumber) |
| Customer → open the same product in two tabs and submit the review form in both | Any rating | You have already reviewed this product (UQ_Reviews_Product_Customer) |
