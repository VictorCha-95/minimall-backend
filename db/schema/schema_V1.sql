USE MiniMall;

CREATE TABLE member (
	member_id 		BIGINT 			AUTO_INCREMENT,
    login_id		VARCHAR(50) 	NOT NULL,
    password		VARCHAR(255) 	NOT NULL,
    member_name		VARCHAR(50)		NOT NULL,
    email			VARCHAR(100)	NOT NULL,
    addr			VARCHAR(255)	NULL,
    created_at		DATETIME		DEFAULT CURRENT_TIMESTAMP,
    updated_at		DATETIME		DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (member_id),
    UNIQUE KEY uq_member_login_id (login_id),
    UNIQUE KEY uq_member_email (email)
);


CREATE TABLE product (
	product_id		BIGINT			AUTO_INCREMENT,
    product_name	VARCHAR(100)	NOT NULL,
    product_price	INT				NOT NULL,
	stock_quantity	INT				NOT NULL DEFAULT 0,
    created_at		DATETIME		DEFAULT CURRENT_TIMESTAMP,
    updated_at		DATETIME		DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (product_id),
    INDEX idx_product_name (product_name)
);


CREATE TABLE orders (
	order_id		BIGINT			AUTO_INCREMENT,
    member_id		BIGINT			NOT NULL,
    ordered_at		DATETIME		NOT NULL,	-- 주문일(애플리케이션에서 생성)
    order_status	VARCHAR(20)		NOT NULL DEFAULT 'ORDERED',
    total_amount	INT				NOT NULL,	-- 총 주문 금액(역정규화)
    created_at		DATETIME		DEFAULT CURRENT_TIMESTAMP,
    updated_at		DATETIME		DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (order_id),
    CONSTRAINT fk_orders_member FOREIGN KEY (member_id)
		REFERENCES member (member_id),
	INDEX idx_order_status_ordered_at (order_status, ordered_at)	-- 관리자용 주문 조회 인덱스
);


CREATE TABLE order_item (
	order_item_id	BIGINT			AUTO_INCREMENT,
    order_id		BIGINT			NOT NULL,
    product_id		BIGINT			NOT NULL,
    product_name	VARCHAR(100)	NOT NULL,	-- 주문 당시 상품명(역정규화)
    order_price		INT				NOT NULL,	-- 주문 당시 가격
    order_quantity	INT				NOT NULL,
    created_at		DATETIME		DEFAULT CURRENT_TIMESTAMP,
    updated_at		DATETIME		DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (order_item_id),
    UNIQUE KEY uq_order_item_order_product (order_id, product_id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id)
		REFERENCES orders(order_id),
	CONSTRAINT fk_order_item_product FOREIGN KEY (product_id)
		REFERENCES product(product_id)
);


CREATE TABLE delivery (
	delivery_id		BIGINT			AUTO_INCREMENT,
    order_id		BIGINT			NOT NULL,
    delivery_status	VARCHAR(20)		NOT NULL DEFAULT 'READY',
    tracking_no		VARCHAR(50)		NULL,	-- 배송 시작 시 택배사에서 발급받는 번호
    ship_addr		VARCHAR(255)	NOT NULL,
    created_at		DATETIME		DEFAULT CURRENT_TIMESTAMP,
    updated_at		DATETIME		DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (delivery_id),
    UNIQUE KEY uq_delivery_orders (order_id),	-- order(주문)과 1:1 관계
    CONSTRAINT fk_delivery_order_id FOREIGN KEY (order_id)
		REFERENCES orders(order_id)
);


CREATE TABLE pay (
	pay_id			BIGINT			AUTO_INCREMENT,
    order_id		BIGINT			NOT NULL,
    pay_method		VARCHAR(50)		NOT NULL,
    pay_amount		INT				NOT NULL,
    pay_status		VARCHAR(20)		NOT NULL,
    paid_at			DATETIME		NULL,	-- 결제가 최종 완료된 시점
    created_at		DATETIME		DEFAULT CURRENT_TIMESTAMP,
    updated_at		DATETIME		DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (pay_id),
    UNIQUE KEY uq_pay_order_id (order_id),	-- order(주문)과 1:1 관계
    CONSTRAINT fk_pay_orders FOREIGN KEY (order_id)
		REFERENCES orders(order_id)
);