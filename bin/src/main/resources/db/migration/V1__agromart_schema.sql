CREATE DATABASE IF NOT EXISTS agrowmart CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agrowmart;

CREATE TABLE roles (
  id   SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE users (
  id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  uuid                CHAR(36) NOT NULL DEFAULT (UUID()),
  name                VARCHAR(100) NOT NULL,
  email               VARCHAR(255) UNIQUE NOT NULL,
  password_hash       VARCHAR(255) NOT NULL,
  phone               VARCHAR(30)  UNIQUE NOT NULL,
  phone_verified      TINYINT(1) DEFAULT 0,
  address             TEXT,

  -- Vendor-only fields (NULL for customers)
  kisan_card_number   VARCHAR(50) NULL,
  bank_account_number VARCHAR(50) NULL,
  ifsc_code           VARCHAR(20) NULL,
  id_proof_path       VARCHAR(255) NULL,
  -- address_proof_path REMOVED

  role_id             SMALLINT UNSIGNED NOT NULL,
  created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_email ON users(email);

CREATE TABLE otp_codes (
  id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  phone      VARCHAR(30) NOT NULL,
  code       VARCHAR(10) NOT NULL,
  purpose    ENUM('PHONE_VERIFY','LOGIN','FORGOT_PASSWORD') DEFAULT 'PHONE_VERIFY',
  attempts   INT DEFAULT 0,
  is_used    TINYINT(1) DEFAULT 0,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_otp_phone (phone)
) ENGINE=InnoDB;


-- Categories
CREATE TABLE categories (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  parent_id BIGINT UNSIGNED NULL,
  name VARCHAR(200) NOT NULL,
  slug VARCHAR(255) NOT NULL UNIQUE,
  FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Products
CREATE TABLE products (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT UNSIGNED NOT NULL,
  category_id BIGINT UNSIGNED,
  product_name VARCHAR(300) NOT NULL,
  short_description VARCHAR(512),
  base_unit VARCHAR(50) DEFAULT 'kg',

  status ENUM('ACTIVE','DRAFT','ARCHIVED') DEFAULT 'ACTIVE',
  image_paths VARCHAR(1000),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (merchant_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_products_merchant ON products(merchant_id);
CREATE INDEX idx_products_category ON products(category_id);





-- PRODUCTS BASE TABLE
CREATE TABLE IF NOT EXISTS products (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  merchant_id BIGINT UNSIGNED NOT NULL,
  category_id BIGINT UNSIGNED NOT NULL,
  product_name VARCHAR(300) NOT NULL,
  status ENUM('ACTIVE','DRAFT','ARCHIVED') DEFAULT 'ACTIVE',
  image_paths VARCHAR(1000),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (merchant_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_product_merchant ON products(merchant_id);
CREATE INDEX idx_product_category ON products(category_id);

-- PRODUCT DETAIL TABLES

-- VEGETABLE DETAILS
CREATE TABLE IF NOT EXISTS vegetable_details (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT UNSIGNED NOT NULL UNIQUE,
  dietary_preference ENUM('VEG','NONVEG') DEFAULT 'VEG',
  unit VARCHAR(50),
  weight VARCHAR(50),
  disclaimer TEXT,
  shop_name VARCHAR(200),
  shop_address TEXT,
  country VARCHAR(100),
  shelf_life VARCHAR(50),
  min_price DECIMAL(10,2),
  max_price DECIMAL(10,2),
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- DAIRY DETAILS
CREATE TABLE IF NOT EXISTS dairy_details (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT UNSIGNED NOT NULL UNIQUE,
  quantity VARCHAR(50),
  price DECIMAL(10,2),
  brand VARCHAR(100),
  ingredients TEXT,
  packaging_type VARCHAR(100),
  product_information TEXT,
  usage_information TEXT,
  dietary_preference ENUM('VEG','NONVEG') DEFAULT 'VEG',
  unit VARCHAR(50),
  storage VARCHAR(100),
  shop_name VARCHAR(200),
  shop_address TEXT,
  country VARCHAR(100),
  shelf_life VARCHAR(50),
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- MEAT DETAILS
CREATE TABLE IF NOT EXISTS meat_details (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT UNSIGNED NOT NULL UNIQUE,
  quantity VARCHAR(50),
  price DECIMAL(10,2),
  brand VARCHAR(100),
  key_features TEXT,
  cut_type VARCHAR(100),
  serving_size VARCHAR(100),
  storage_instruction VARCHAR(100),
  usage TEXT,
  energy VARCHAR(100),
  dietary_preference ENUM('VEG','NONVEG') DEFAULT 'NONVEG',
  marinated TINYINT(1) DEFAULT 0,
  packaging_type VARCHAR(100),
  disclaimer TEXT,
  refund_policy TEXT,
  shop_name VARCHAR(200),
  shop_address TEXT,
  country VARCHAR(100),
  shelf_life VARCHAR(50),
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB;









-- 1. orders
CREATE TABLE orders (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT UNSIGNED NOT NULL,
    
    total_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    delivery_charge DECIMAL(10,2) DEFAULT 25,
    
    promo_code VARCHAR(50),
    
    status ENUM('PENDING','CONFIRMED','PACKED','SHIPPED','DELIVERED','CANCELLED','RETURNED','REFUNDED') DEFAULT 'PENDING',
    payment_status ENUM('PENDING','PAID','FAILED','REFUNDED') DEFAULT 'PENDING',
    payment_method ENUM('COD','RAZORPAY') DEFAULT 'COD',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 2. order_items (Supports Multiple Merchants)
CREATE TABLE order_items (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL,
    product_id BIGINT UNSIGNED NOT NULL,
    merchant_id BIGINT UNSIGNED NOT NULL,
    product_name VARCHAR(255),
    product_image VARCHAR(500),
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (merchant_id) REFERENCES users(id)
);

-- 3. payment_details (For Razorpay)
CREATE TABLE payment_details (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL,
    razorpay_order_id VARCHAR(100),
    razorpay_payment_id VARCHAR(100),
    razorpay_signature VARCHAR(200),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',
    status ENUM('CREATED','ATTEMPTED','PAID','FAILED') DEFAULT 'CREATED',
    paid_at TIMESTAMP NULL,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    UNIQUE (razorpay_order_id)
);

-- 4. delivery_address
CREATE TABLE delivery_address (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    street_address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    country VARCHAR(100) DEFAULT 'India',
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 5. order_status_history (Professional)
CREATE TABLE order_status_history (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL,
    status VARCHAR(50) NOT NULL,
    message TEXT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);