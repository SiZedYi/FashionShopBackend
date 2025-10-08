-- USERS
INSERT INTO users (email, password_hash, full_name, phone, role, is_active, created_at, updated_at)
VALUES
('leon.gigsberg@example.com', 'hash1', 'Leon Gigsberg', '1234567890', 'customer', 1, NOW(), NOW()),
('jane.smith@example.com', 'hash2', 'Jane Smith', '0987654321', 'customer', 1, NOW(), NOW()),
('admin@example.com', 'adminhash', 'Admin User', '1112223333', 'admin', 1, NOW(), NOW());

-- CATEGORIES
INSERT INTO categories (name, slug, description, is_active, created_at, updated_at)
VALUES
('Men''s Clothing', 'mens-clothing', 'Trendy and classic men''s fashion', 1, NOW(), NOW()),
('Women''s Clothing', 'womens-clothing', 'Elegant and modern women''s fashion', 1, NOW(), NOW()),
('Shoes', 'shoes', 'Footwear for all occasions', 1, NOW(), NOW()),
('Accessories', 'accessories', 'Fashion accessories for men and women', 1, NOW(), NOW()),
('Watches', 'watches', 'Stylish watches for every style', 1, NOW(), NOW());

-- PRODUCTS
INSERT INTO products (sku, name, description, price, sale_price, stock_quantity, is_active, created_at, updated_at, about_item, discount, rating, brand, color)
VALUES
('MTSHIRT001', 'Classic Men''s T-Shirt', '100% cotton, regular fit, available in multiple colors.', 19.99, 14.99, 100, 1, NOW(), NOW(), 'Soft, breathable, and perfect for everyday wear.', 25, 4.3, 'Levi''s', 'white,black,blue'),
('WDRS001', 'Elegant Women''s Dress', 'Chiffon, knee-length, floral print, summer collection.', 49.99, 39.99, 50, 1, NOW(), NOW(), 'Lightweight and flowy, ideal for summer events.', 20, 4.7, 'Zara', 'red,blue,green'),
('MSHOE001', 'Men''s Leather Sneakers', 'Genuine leather, cushioned sole, lace-up.', 59.99, 49.99, 80, 1, NOW(), NOW(), 'Durable and stylish, suitable for casual and semi-formal.', 15, 4.5, 'Nike', 'black,white,gray'),
('WBAG001', 'Women''s Tote Bag', 'Spacious, faux leather, comes with a zipper pocket.', 34.99, 29.99, 60, 1, NOW(), NOW(), 'Perfect for work or shopping, fits a laptop.', 10, 4.2, 'Michael Kors', 'brown,black,beige'),
('MWATCH001', 'Men''s Chronograph Watch', 'Stainless steel, water-resistant, date display.', 120.00, 99.99, 30, 1, NOW(), NOW(), 'Elegant design, suitable for business and casual.', 17, 4.8, 'Fossil', 'silver,black'),
('WSHOE001', 'Women''s Running Shoes', 'Lightweight mesh, shock-absorbing sole.', 69.99, 59.99, 70, 1, NOW(), NOW(), 'Designed for comfort and performance.', 14, 4.6, 'Adidas', 'pink,white,gray'),
('MJEAN001', 'Men''s Slim Fit Jeans', 'Stretch denim, mid-rise, classic 5-pocket style.', 39.99, 29.99, 90, 1, NOW(), NOW(), 'Modern look with comfortable fit.', 25, 4.4, 'Levi''s', 'blue,black'),
('WSKRT001', 'Women''s Pleated Skirt', 'Polyester, midi length, high waist.', 27.99, 22.99, 40, 1, NOW(), NOW(), 'Chic and versatile for any occasion.', 18, 4.5, 'H&M', 'black,white,navy'),
('MSHIRT002', 'Men''s Formal Shirt', 'Cotton blend, slim fit, button-down collar.', 29.99, 24.99, 60, 1, NOW(), NOW(), 'Perfect for office or formal events.', 20, 4.3, 'Uniqlo', 'white,blue,gray'),
('WJKT001', 'Women''s Denim Jacket', 'Classic fit, button closure, two chest pockets.', 54.99, 44.99, 35, 1, NOW(), NOW(), 'Timeless style for layering.', 18, 4.6, 'Levi''s', 'blue,black,white');

-- PRODUCT_CATEGORIES
INSERT INTO product_categories (product_id, category_id) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 3), (7, 1), (8, 2), (9, 1), (10, 2);

-- PRODUCT_IMAGES
INSERT INTO product_images (product_id, url, alt_text, is_primary, created_at) VALUES
(1, '/images/products/mens-tshirt-white.jpg', 'White T-Shirt', 1, NOW()),
(1, '/images/products/mens-tshirt-black.jpg', 'Black T-Shirt', 0, NOW()),
(2, '/images/products/womens-dress-floral.jpg', 'Floral Dress', 1, NOW()),
(3, '/images/products/mens-sneakers-black.jpg', 'Black Sneakers', 1, NOW()),
(4, '/images/products/womens-tote-bag-brown.jpg', 'Brown Tote Bag', 1, NOW()),
(5, '/images/products/mens-watch-silver.jpg', 'Silver Chronograph Watch', 1, NOW()),
(6, '/images/products/womens-running-shoes-pink.jpg', 'Pink Running Shoes', 1, NOW()),
(7, '/images/products/mens-jeans-blue.jpg', 'Blue Slim Fit Jeans', 1, NOW()),
(8, '/images/products/womens-skirt-black.jpg', 'Black Pleated Skirt', 1, NOW()),
(9, '/images/products/mens-formal-shirt-white.jpg', 'White Formal Shirt', 1, NOW()),
(10, '/images/products/womens-denim-jacket-blue.jpg', 'Blue Denim Jacket', 1, NOW());

-- REVIEWS
INSERT INTO reviews (product_id, content, rating, author, image, review_date) VALUES
(1, 'Great quality and fits perfectly. Will buy again!', 4.5, 'Alice Johnson', '/images/people/alice.jpg', NOW()),
(2, 'Beautiful dress, my wife loved it!', 4.8, 'Bob Smith', '/images/people/bob.jpg', NOW()),
(3, 'Comfortable sneakers, good for daily wear.', 4.2, 'Chris Evans', '/images/people/chris.jpg', NOW()),
(4, 'Stylish bag, lots of space inside.', 4.3, 'Diana Prince', '/images/people/diana.jpg', NOW()),
(5, 'Very elegant watch, looks expensive.', 4.9, 'Edward Norton', '/images/people/edward.jpg', NOW()),
(6, 'Perfect for running, very light.', 4.6, 'Fiona Gallagher', '/images/people/fiona.jpg', NOW()),
(7, 'Jeans are comfortable and look great.', 4.4, 'George Michael', '/images/people/george.jpg', NOW()),
(8, 'Skirt is cute and fits well.', 4.5, 'Hannah Lee', '/images/people/hannah.jpg', NOW()),
(9, 'Nice shirt for work, good material.', 4.3, 'Ian Curtis', '/images/people/ian.jpg', NOW()),
(10, 'Jacket is stylish and warm.', 4.7, 'Julia Roberts', '/images/people/julia.jpg', NOW());

