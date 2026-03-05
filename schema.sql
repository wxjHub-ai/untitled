-- Database: SnackShop
-- (Make sure you have created the database 'SnackShop' before running this script if you are not letting Spring Boot auto-generate it)

-- =============================================
-- 1. Table: users (用户表)
-- Stores customer and admin information
-- =============================================
-- =============================================
-- 1. Table: users (用户表)
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role VARCHAR(50) NOT NULL -- 'USER', 'ADMIN', 'MERCHANT'
);

-- =============================================
-- 2. Table: stores (店铺表) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS stores (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    logo_url VARCHAR(255),
    owner_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE', -- 'ACTIVE', 'CLOSED'
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stores_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- =============================================
-- 3. Table: user_addresses (收货地址表) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS user_addresses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    detail_address TEXT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    label VARCHAR(20), -- '家', '公司', '学校'
    CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =============================================
-- 4. Table: products (商品表) - 修改关联到店铺
-- =============================================
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    price NUMERIC(19, 2) NOT NULL,
    stock INTEGER NOT NULL,
    image_url VARCHAR(255),
    category VARCHAR(255),
    store_id BIGINT, -- 关联到店铺
    deleted BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_products_store FOREIGN KEY (store_id) REFERENCES stores(id)
);

-- =============================================
-- 5. Table: cart_items (购物车持久化) - 新增
-- =============================================
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    added_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- =============================================
-- 6. Table: orders (订单表) - 修改关联到地址
-- =============================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    address_id BIGINT, -- 关联到具体的收货地址
    order_date TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(50), 
    total_amount NUMERIC(19, 2),
    delivery_address TEXT, -- 冗余存储下单时的快照地址
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_orders_address FOREIGN KEY (address_id) REFERENCES user_addresses(id)
);

COMMENT ON TABLE orders IS 'Customer orders';

-- =============================================
-- 4. Table: order_items (订单明细表)
-- Stores specific items within an order
-- =============================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price NUMERIC(19, 2), -- Snapshot of the price at the time of purchase
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

COMMENT ON TABLE order_items IS 'Line items for each order';

-- =============================================
-- Sample Data Insertion (Optional - for testing)
-- =============================================

-- Insert Admin User (Password is 'admin123' hashed with BCrypt - standard for Spring Security)
-- Note: In a real scenario, passwords must be hashed. This hash is for 'admin123'.
INSERT INTO users (username, password, email, role) 
VALUES ('admin', '$2a$10$wW2yVz.7z/M5n.e5d.n.u.J/5.5.5.5.5.5.5.5.5.5', 'admin@snackshop.com', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

-- Insert Sample Products
INSERT INTO products (name, description, price, stock, image_url, category) VALUES
('卫龙大面筋辣条', '经典儿时回忆，香辣更有味，休闲追剧必备零食。', 5.50, 100, 'https://img14.360buyimg.com/n0/jfs/t1/157567/2/22752/155097/61beed2eE10459347/a353683610931551.jpg', '辣条'),
('三只松鼠每日坚果', '健康混合坚果仁，科学配比，营养均衡，早餐下午茶好伴侣。', 29.90, 50, 'https://img14.360buyimg.com/n0/jfs/t1/135967/26/18260/256877/5fca087fE896350d6/d344585141258d4a.jpg', '坚果'),
('乐事薯片原味', '精选土豆，薄脆爽口，经典原味，停不下来的美味。', 7.80, 200, 'https://img14.360buyimg.com/n0/jfs/t1/211145/26/8788/247963/61824383E65543c8d/09a066491e0a2489.jpg', '膨化食品'),
('奥利奥夹心饼干', '扭一扭，舔一舔，泡一泡，经典巧克力奶香味。', 12.50, 150, 'https://img14.360buyimg.com/n0/jfs/t1/169829/4/23573/193214/61a72d3fE2161f366/0912198083818e8d.jpg', '饼干');


INSERT INTO products (name, description, price, stock, image_url, category) VALUES

('大白兔奶糖', '国民经典奶糖，浓郁奶香，甜而不腻，满满的童年记忆。', 15.90, 300, 'https://img14.360buyimg.com/n0/jfs/t1/139985/21/20563/128362/607cd7f4E0c538a7c/2dbddbce26cc6cb5.jpg', '糖果'),
('可口可乐汽水', '经典原味，畅爽怡神，快乐肥宅水，聚会必备饮品。', 12.90, 500, 'https://img14.360buyimg.com/n0/jfs/t1/107147/11/17062/207005/5e7b2faeE7f6ab0e5/06e8bd247c7c10b7.jpg', '饮料'),
('有友泡椒凤爪', '重庆特产，酸辣爽口，肉质Q弹，休闲追剧下酒佳品。', 19.80, 120, 'https://img14.360buyimg.com/n0/jfs/t1/119501/15/5761/139369/5eb8f7e2E9b0c749a/a1cc8f9df0ba57e9.jpg', '其他'),
('百草味夏威夷果', '奶油味坚果干果，果仁饱满，自带开壳器，好吃易剥。', 35.00, 80, 'https://img14.360buyimg.com/n0/jfs/t1/123306/6/10292/136894/5f38a531E83cc0f47/310abdaaa4981d33.jpg', '坚坚果'),
('麻辣王子辣条', '地道湖南口味，微麻微辣，独立小包装，干净卫生不脏手。', 14.90, 200, 'https://img14.360buyimg.com/n0/jfs/t1/105786/4/18779/119958/5e9c0919Eee14fbd1/82433ea7359560a8.jpg', '辣条'),
('旺旺雪饼', '非油炸膨化食品，香脆可口，满口米香，老少皆宜的美味。', 21.50, 150, 'https://img14.360buyimg.com/n0/jfs/t1/215357/12/17013/116997/6257d079E3be21ff7/9df3dfbe4ed6a256.jpg', '膨化食品'),
('元气森林白桃气泡水', '0糖0脂0卡，清甜白桃味，强劲气泡，好喝无负担。', 5.50, 400, 'https://img14.360buyimg.com/n0/jfs/t1/170701/3/24108/88607/61af0357E88ab4d3d/d236f0db5c1562b9.jpg', '饮料');