# Manual test checklist

Run these steps on a fresh database: execute `database/01_schema.sql`, then `database/02_seed.sql`, then start the application (see `README.md`) and open <http://localhost:8080>.

The sample data uses placeholder passwords, which are compared as plain text.

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `hash_admin_0001` |
| Seller | `techworld` | `hash_sell_0002` |
| Seller | `fashionhub` | `hash_sell_0003` |
| Customer | `ali_yilmaz` | `hash_cust_0006` |
| Customer | `ayse_demir` | `hash_cust_0007` |
| Customer | `can_ozturk` | `hash_cust_0010` |
| Customer | `elif_sahin` | `hash_cust_0011` |

Each step lists the expected result after the arrow.

## 1. Login and access control

1. Open `/admin` without logging in → you are sent to the login page.
2. Log in with a wrong password → "Invalid username or password".
3. Log in as `ali_yilmaz` → you land on the shop and the menu shows Shop, Cart, Orders and Addresses.
4. While logged in as a customer, open `/admin` and `/seller` → "You do not have permission to view this page".
5. Click Logout → "You have been logged out", and `/shop/cart` sends you to the login page again.

## 2. Customer

Log in as `ali_yilmaz`.

1. **Product list:** Choose the category Books → only The Silent River and SQL Fundamentals are listed. Search for `mouse` → only Wireless Mouse is listed.
2. **Product page:** Open Wireless Mouse → price 24.99 ₺, 150 in stock, rating 5.0 and Ali's review are shown. Change the quantity to 3 → the total under it shows 74.97 ₺ at once.
3. **Add to cart:** Click Add to cart → you stay on the product page and see "Wireless Mouse added to your cart". The stock still shows 150; stock goes down only when the order is placed.
4. **Cart:** Open Cart. Ali's sample cart has Face Moisturizer, which is no longer sold: click Remove for it. Change a quantity → the line total and the cart total change without an Update button. Enter a number higher than the stock → "Not enough stock for ..." under that row and the quantity is not saved.
5. **Checkout:** Click Proceed to checkout. Choose a shipping company → its fee is added to the summary. Type coupon `WELCOME10` → the 10% discount and the new total appear before you place the order. Choose Credit card and click Place order → "Order #N placed"; the order page shows the subtotal, the discount, the shipping fee with the company name and the total, and a Completed payment.
6. **Transaction result:** As admin, check that the product stock went down and that WELCOME10 was used one more time.
7. **Invalid coupon:** Put a product in the cart again and type coupon `SUMMER15` → "Coupon SUMMER15 is not active" under the field; placing the order shows the same message and no order is created.
8. **Cancel an order:** Open the order from step 5 (Paid, not shipped) and click Cancel order, then confirm → "Your order was cancelled", the stock comes back and the payment becomes Refunded. Order #1 (Delivered) has no Cancel order button.
9. **Order history:** Open Orders → the newest order is at the top. Open order #1 → items, payment, shipment (Yurtici Kargo) and address are shown.
10. **Addresses:** Open Addresses → Home (Default) and Office are listed; both are used by an order, so they have no Delete button. Add an address and tick "Use as my default address" → it becomes the default and is selected at checkout. Edit it and save, then delete it → "Address deleted".
    Edit Home (used by order #1) and change the street → "Your earlier orders keep the address they were sent to"; the list shows the new street, while order #1 still shows Ataturk Cad. No:12 D:3.
11. **Return request:** In order #1 (Delivered), request a return of 1 Wireless Mouse with a reason → "Your return request was sent", the request is listed as Requested and the item shows a "Return requested" label. Requesting the same item again → "You can return at most 0 of this product".
12. **Review:** Log in as `can_ozturk`, open Yoga Mat (order #5, Delivered), choose 4 and submit → the review is listed and the form disappears. Log in as `elif_sahin` and open Bluetooth Headphones (order #6, still shipping) → there is no review form, because reviews open after delivery.

## 3. Seller

Log in as `techworld`.

1. **Dashboard:** Products, units sold, revenue and the low stock list are shown.
2. **My products:** Only TechWorld products are listed.
3. **Add product:** Add a product with category Electronics, price 49.50 and stock 12 → "Product added" and the product is visible in the shop.
4. **Edit product:** Change the stock of Wireless Mouse and save → "Product updated".
5. **Hide product:** Clear "Show this product in the shop" and save → the product disappears from the shop but stays in My products as Inactive.
6. **Other seller's product:** Open `/seller/products/5/edit` (a FashionHub product) → "Page not found".
7. **Orders:** Only order lines with TechWorld products are shown.

## 4. Admin

Log in as `admin`.

1. **Dashboard:** User counts per role, total orders, revenue and the 5 latest orders are shown.
2. **Users:** Deactivate `ayse_demir` → she cannot log in ("This account is disabled"). Activate her again. Your own row shows "You" and has no button.
3. **Sellers:** All sample sellers are approved; a seller waiting for approval would have an Approve button.
4. **Categories:** Add a category named `Toys` → "Category added".
5. **Coupons:** Add coupon `NEW15`, Percent, value 15, minimum 0, valid this month, usage limit 50 → "Coupon added".
6. **Shipment:** Open order #4 (Pending, from the sample data), choose Aras Kargo, enter a tracking number and click Create shipment → the order becomes Shipped. Click Mark as delivered → the order becomes Delivered and the cash on delivery payment becomes Completed.
   For an order placed in step 2.5, the company cannot be changed: the page shows the company the customer chose at checkout.
7. **Cancel an order:** Open order #6 (Shipped) and change the status to Cancelled → its items go back into stock, the payment becomes Refunded and the shipment becomes Returned. The status cannot be changed again.
8. **Returns:** Approve the return request from step 2.11, then click Refund → the refund amount is quantity × unit price and the product stock goes up by the returned quantity.
9. **Reject a return:** For the Face Moisturizer request of order #10, click Reject, write a reason and click Reject return → the status becomes Rejected and the reason is shown. Log in as `ayse_demir` and open order #10 → the reason is shown, the item has a "Return rejected" label and there is no return form for it any more. Rejecting without a reason → "Please write why the return is rejected".

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
