-- Test data for pagination and filtering tests
-- This file generates: ~1000 users, 500 products, 3000 orders with order_items

-- Clear existing data to avoid duplicate key errors
TRUNCATE TABLE order_products, orders, products, users CASCADE;

-- ============================================
-- USERS DATA (1000 users)
-- ============================================

INSERT INTO users (user_id, first_name, last_name, email, phone, password, role, active, created_at, updated_at, created_by, updated_by)
SELECT 
    gen_random_uuid(),
    'User' || i,
    'Last' || i,
    'user' || i || '@example.com',
    '+1-555-' || LPAD(i::TEXT, 4, '0'),
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    CASE 
        WHEN i % 10 = 0 THEN 'ADMIN'
        ELSE 'CLIENT'
    END,
    CASE WHEN i % 20 != 0 THEN true ELSE false END,
    CURRENT_TIMESTAMP - (random() * (interval '365 days')),
    CURRENT_TIMESTAMP - (random() * (interval '30 days')),
    'system',
    'system'
FROM generate_series(1, 1000) AS i;

-- ============================================
-- PRODUCTS DATA (500 products)
-- ============================================

INSERT INTO products (product_id, name, description, price, stock_quantity, active, version, created_at, updated_at, created_by, updated_by)
SELECT 
    i,
    'Product ' || i,
    'Description for product ' || i || '. High quality item with excellent features.',
    ROUND((10 + random() * 990)::NUMERIC, 2),
    (random() * 1000)::INTEGER,
    CASE WHEN i % 25 != 0 THEN true ELSE false END,
    0,
    CURRENT_TIMESTAMP - (random() * (interval '365 days')),
    CURRENT_TIMESTAMP - (random() * (interval '30 days')),
    'system',
    'system'
FROM generate_series(1, 500) AS i;

-- ============================================
-- ORDERS DATA (3000 orders)
-- ============================================

-- First, ensure we have users (this will be executed after users insert)
-- Generate orders with references to existing users using a subquery
-- order_status is stored as smallint: 0=PENDING, 1=APPROVED, 2=REJECTED
INSERT INTO orders (order_id, total_price, order_status, active, version, user_id, created_at, updated_at, created_by, updated_by)
SELECT 
    i,
    0.00,
    CASE 
        WHEN i % 10 = 0 THEN 2
        WHEN i % 5 = 0 THEN 1
        ELSE 0
    END,
    true,
    0,
    (SELECT user_id FROM users ORDER BY random() LIMIT 1),
    CURRENT_TIMESTAMP - (random() * (interval '180 days')),
    CURRENT_TIMESTAMP - (random() * (interval '30 days')),
    'system',
    'system'
FROM generate_series(1, 3000) AS i;

-- ============================================
-- ORDER PRODUCTS DATA (multiple items per order)
-- ============================================

-- Generate order items using set-based approach (1-6 items per order)
INSERT INTO order_products (order_id, product_id, quantity, unit_price)
SELECT 
    o.order_id,
    1 + (random() * 499)::INTEGER AS product_id,
    1 + (random() * 10)::INTEGER AS quantity,
    ROUND((10 + random() * 990)::NUMERIC, 2) AS unit_price
FROM 
    orders o,
    generate_series(1, 1 + (random() * 5)::INTEGER) AS items;

-- Update order totals
UPDATE orders o
SET total_price = (
    SELECT ROUND(SUM(op.quantity * op.unit_price), 2)
    FROM order_products op
    WHERE op.order_id = o.order_id
);

-- ============================================
-- ADDITIONAL INDEXES FOR BETTER PAGINATION PERFORMANCE
-- ============================================

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_active ON products(active);
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);
CREATE INDEX IF NOT EXISTS idx_products_created_at ON products(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(order_status);
CREATE INDEX IF NOT EXISTS idx_orders_active ON orders(active);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_total_price ON orders(total_price);

CREATE INDEX IF NOT EXISTS idx_order_products_order_id ON order_products(order_id);
CREATE INDEX IF NOT EXISTS idx_order_products_product_id ON order_products(product_id);

-- ============================================
-- STATISTICS
-- ============================================

ANALYZE users;
ANALYZE products;
ANALYZE orders;
ANALYZE order_products;

-- Select summary
SELECT 
    (SELECT COUNT(*) FROM users) AS total_users,
    (SELECT COUNT(*) FROM users WHERE active = true) AS active_users,
    (SELECT COUNT(*) FROM products) AS total_products,
    (SELECT COUNT(*) FROM products WHERE active = true) AS active_products,
    (SELECT COUNT(*) FROM orders) AS total_orders,
    (SELECT COUNT(*) FROM order_products) AS total_order_items;