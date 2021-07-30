-- !Ups
CREATE TABLE product (
    id_product BIGSERIAL NOT NULL PRIMARY KEY,
    id_typeproduct BIGINT REFERENCES typeproduct(id_typeproduct),
    name VARCHAR (50),
    gender VARCHAR (1),
    size VARCHAR (50),
    price NUMERIC (5, 2)
);

-- !Downs

DROP TABLE product;