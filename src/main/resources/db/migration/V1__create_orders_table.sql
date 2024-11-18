CREATE SEQUENCE IF NOT EXISTS order_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE orders
(
    id            BIGINT       NOT NULL,
    order_id      UUID         NOT NULL,
    price         DECIMAL      NOT NULL,
    product_ids   VARCHAR(255) NOT NULL,
    status        VARCHAR(255) NOT NULL,
    city          VARCHAR(255) NOT NULL,
    creation_date TIMESTAMP    NOT NULL,
    last_updated  TIMESTAMP    NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS order_audit_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE orders_audit
(
    id           BIGINT       NOT NULL,
    status       VARCHAR(255) NOT NULL,
    order_id     BIGINT       NOT NULL,
    last_updated TIMESTAMP    NOT NULL,
    CONSTRAINT pk_orders_audit PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES orders (id)
);

