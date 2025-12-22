SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 1) member
-- =========================
CREATE TABLE member (
  member_id      BIGINT       NOT NULL AUTO_INCREMENT,
  login_id       VARCHAR(50)  NOT NULL,
  password_hash  VARCHAR(100) NOT NULL,
  member_name    VARCHAR(50)  NOT NULL,
  email          VARCHAR(100) NOT NULL,

  created_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by      VARCHAR(50) DEFAULT NULL,
  update_by      VARCHAR(50) DEFAULT NULL,

  city           VARCHAR(50)  NOT NULL,
  detail         VARCHAR(255) DEFAULT NULL,
  postcode       VARCHAR(20)  NOT NULL,
  state          VARCHAR(50)  NOT NULL,
  street         VARCHAR(255) NOT NULL,

  grade          VARCHAR(20)  NOT NULL,

  PRIMARY KEY (member_id),
  UNIQUE KEY uq_member_login_id (login_id),
  UNIQUE KEY uq_member_email (email)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 2) product
-- =========================
CREATE TABLE product (
  product_id      BIGINT       NOT NULL AUTO_INCREMENT,
  product_name    VARCHAR(100) NOT NULL,
  product_price   INT          NOT NULL,
  stock_quantity  INT          NOT NULL DEFAULT 0,

  created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by       VARCHAR(255) DEFAULT NULL,
  update_by       VARCHAR(255) DEFAULT NULL,

  PRIMARY KEY (product_id),
  INDEX idx_product_name (product_name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 3) orders
-- =========================
CREATE TABLE orders (
  order_id         BIGINT      NOT NULL AUTO_INCREMENT,
  member_id        BIGINT      NOT NULL,

  ordered_at       DATETIME    NOT NULL,
  order_status     VARCHAR(20) NOT NULL DEFAULT 'ORDERED',

  created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by        VARCHAR(255) DEFAULT NULL,
  update_by        VARCHAR(255) DEFAULT NULL,

  discount_amount  INT NOT NULL DEFAULT 0,
  final_amount     INT NOT NULL DEFAULT 0,
  original_amount  INT NOT NULL DEFAULT 0,
  is_discounted    BOOLEAN NOT NULL DEFAULT FALSE,

  PRIMARY KEY (order_id),

  CONSTRAINT fk_orders_member FOREIGN KEY (member_id)
    REFERENCES member (member_id),

  INDEX idx_order_status_ordered_at (order_status, ordered_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 4) order_item
-- =========================
CREATE TABLE order_item (
  order_item_id   BIGINT       NOT NULL AUTO_INCREMENT,
  order_id        BIGINT       NOT NULL,
  product_id      BIGINT       NOT NULL,

  product_name    VARCHAR(100) NOT NULL,
  order_price     INT          NOT NULL,
  order_quantity  INT          NOT NULL,

  created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by       VARCHAR(255) DEFAULT NULL,
  update_by       VARCHAR(255) DEFAULT NULL,

  PRIMARY KEY (order_item_id),

  UNIQUE KEY uq_order_item_order_product (order_id, product_id),

  CONSTRAINT fk_order_item_order FOREIGN KEY (order_id)
    REFERENCES orders (order_id),

  CONSTRAINT fk_order_item_product FOREIGN KEY (product_id)
    REFERENCES product (product_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 5) delivery
-- =========================
CREATE TABLE delivery (
  delivery_id      BIGINT      NOT NULL AUTO_INCREMENT,
  order_id         BIGINT      NOT NULL,

  delivery_status  VARCHAR(20) NOT NULL DEFAULT 'READY',
  tracking_no      VARCHAR(100) DEFAULT NULL,

  created_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by        VARCHAR(255) DEFAULT NULL,
  update_by        VARCHAR(255) DEFAULT NULL,

  arrived_at       DATETIME(6) DEFAULT NULL,
  delivery_fee     INT NOT NULL DEFAULT 0,

  city             VARCHAR(50)  NOT NULL,
  detail           VARCHAR(255) DEFAULT NULL,
  postcode         VARCHAR(20)  NOT NULL,
  state            VARCHAR(50)  NOT NULL,
  street           VARCHAR(255) NOT NULL,

  shipped_at       DATETIME(6) DEFAULT NULL,

  PRIMARY KEY (delivery_id),

  UNIQUE KEY uq_delivery_orders (order_id),

  CONSTRAINT fk_delivery_order_id FOREIGN KEY (order_id)
    REFERENCES orders (order_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- =========================
-- 6) pay
-- =========================
CREATE TABLE pay (
  pay_id      BIGINT      NOT NULL AUTO_INCREMENT,
  order_id    BIGINT      NOT NULL,

  pay_method  VARCHAR(50) NOT NULL,
  pay_amount  INT         NOT NULL,
  pay_status  VARCHAR(20) NOT NULL,
  paid_at     DATETIME DEFAULT NULL,

  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by   VARCHAR(255) DEFAULT NULL,
  update_by   VARCHAR(255) DEFAULT NULL,

  PRIMARY KEY (pay_id),

  UNIQUE KEY uq_pay_order_id (order_id),

  CONSTRAINT fk_pay_orders FOREIGN KEY (order_id)
    REFERENCES orders (order_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


SET FOREIGN_KEY_CHECKS = 1;