CREATE SEQUENCE IF NOT EXISTS order_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE orders
(
    id            INTEGER                     NOT NULL,
    order_id      UUID                        NOT NULL,
    price         DECIMAL                     NOT NULL,
    product_ids   VARCHAR(255)                NOT NULL,
    status        VARCHAR(255)                NOT NULL,
    city          VARCHAR(255)                NOT NULL,
    creation_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);