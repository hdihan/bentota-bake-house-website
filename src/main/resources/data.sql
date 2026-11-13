-- 1. Create Default Admin User
-- Password is 'admin123' (already BCrypt encrypted)
-- If your 'users' table has different column names, adjust them here
INSERT INTO users (username, password, role, full_name, telephone_number, home_address)
VALUES ('admin', '$2a$10$8.UnVuG9HHgffUDAlk8Kn.2NvS.C6G477/K7MvWv/C.S.H.eP2vG6', 'ADMIN', 'System Admin', '0112233445', 'Benthota');

-- 2. Create Sample Categories
INSERT INTO categories (name, description) VALUES ('Cakes', 'Delicious custom and ready-made cakes');
INSERT INTO categories (name, description) VALUES ('Bread', 'Freshly baked daily bread and buns');
INSERT INTO categories (name, description) VALUES ('Pastries', 'Sweet and savory pastries');

-- 3. Create Sample Products
-- These IDs usually start at 1
INSERT INTO products (name, description, price, stock_quantity, status, image_url)
VALUES ('Chocolate Cake', 'Rich double chocolate layers', 2500.00, 10, 'AVAILABLE', 'choc_cake.jpg');

INSERT INTO products (name, description, price, stock_quantity, status, image_url)
VALUES ('Fish Bun', 'Traditional Sri Lankan spicy fish bun', 80.00, 50, 'AVAILABLE', 'fish_bun.jpg');

UPDATE Users
SET role ='admin'
WHERE id = 1;


















