-- Test data for pagination and filtering tests
-- This file generates: ~1000 users, 500 products, 3000 orders with order_items

-- ============================================
-- USERS DATA (1000 users)
-- ============================================

INSERT INTO users (user_id, first_name, last_name, email, phone, password, role, active, created_at, updated_at)
SELECT 
    gen_random_uuid(),
    'User' || i,
    'Last' || i,
    'user' || i || '@example.com',
    '+1-555-' || LPAD(i::TEXT, 4, '0'),
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: password
    CASE 
        WHEN i % 10 = 0 THEN 'ADMIN'
        WHEN i % 5 = 0 THEN 'MANAGER'
        ELSE 'USER'
    END,
    CASE WHEN i % 20 != 0 THEN true ELSE false END,
    CURRENT_TIMESTAMP - (random() * (interval '365 days')),
    CURRENT_TIMESTAMP - (random() * (interval '30 days'))
FROM generate_series(1, 1000) AS i;

-- ============================================
-- PRODUCTS DATA (500 products)
-- ============================================

INSERT INTO products (product_id, name, description, price, stock_quantity, active, version, created_at, updated_at)
SELECT 
    i,
    'Product ' || i,
    'Description for product ' || i || '. High quality item with excellent features.',
    ROUND((10 + random() * 990)::NUMERIC, 2),
    (random() * 1000)::INTEGER,
    CASE WHEN i % 25 != 0 THEN true ELSE false END,
    0,
    CURRENT_TIMESTAMP - (random() * (interval '365 days')),
    CURRENT_TIMESTAMP - (random() * (interval '30 days'))
FROM generate_series(1, 500) AS i;

-- ============================================
-- ORDERS DATA (3000 orders)
-- ============================================

INSERT INTO orders (order_id, total_price, order_status, active, version, user_id, created_at, updated_at)
SELECT 
    i,
    0.00,
    CASE 
        WHEN i % 10 = 0 THEN 'CANCELLED'
        WHEN i % 8 = 0 THEN 'COMPLETED'
        WHEN i % 5 = 0 THEN 'PROCESSING'
        WHEN i % 3 = 0 THEN 'PENDING'
        ELSE 'CREATED'
    END,
    true,
    0,
    (1 + (random() * 999)::INTEGER)::UUID,
    CURRENT_TIMESTAMP - (random() * (interval '180 days')),
    CURRENT_TIMESTAMP - (random() * (interval '30 days'))
FROM generate_series(1, 3000) AS i;

-- ============================================
-- ORDER PRODUCTS DATA (multiple items per order)
-- ============================================

-- First, create a function to generate random order items
DO $$
DECLARE
    order_rec RECORD;
    num_items INTEGER;
    product_id INTEGER;
    quantity INTEGER;
    unit_price NUMERIC(10,2);
    total_order NUMERIC(10,2);
BEGIN
    FOR order_rec IN SELECT order_id FROM orders LOOP
        num_items := 1 + (random() * 5)::INTEGER;
        total_order := 0;
        
        FOR i IN 1..num_items LOOP
            product_id := 1 + (random() * 499)::INTEGER;
            quantity := 1 + (random() * 10)::INTEGER;
            unit_price := ROUND((10 + random() * 990)::NUMERIC, 2);
            
            INSERT INTO order_products (order_id, product_id, quantity, unit_price)
            VALUES (order_rec.order_id, product_id, quantity, unit_price);
            
            total_order := total_order + (quantity * unit_price);
        END LOOP;
        
        UPDATE orders SET total_price = ROUND(total_order, 2) WHERE order_id = order_rec.order_id;
    END LOOP;
END $$;

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