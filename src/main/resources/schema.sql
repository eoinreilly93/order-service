CREATE TABLE orders
(
    id            SERIAL       NOT NULL,
    order_id      VARCHAR(255) NOT NULL,
    status        VARCHAR(50)  NOT NULL,
    price         DECIMAL      NOT NULL,
    product_ids   VARCHAR(50)  NOT NULL,
    city          VARCHAR(255) NOT NULL,
    creation_date TIMESTAMP    NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id)
);