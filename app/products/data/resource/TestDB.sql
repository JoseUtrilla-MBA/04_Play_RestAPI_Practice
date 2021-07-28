create DATABASE playdb;

\c playdb;

create table typeproduct (
    id_typeproduct BIGSERIAL NOT NULL PRIMARY KEY,
    name VARCHAR (50)
);


create TABLE product (
    id_product BIGSERIAL NOT NULL PRIMARY KEY,
    id_typeproduct BIGINT REFERENCES typeproduct(id_typeproduct),
    name VARCHAR (50),
    gender VARCHAR (1),
    size VARCHAR (50),
    price NUMERIC (5,2) UNIQUE
);

insert into typeproduct (id_typeProduct, name) values (1,'Clothes');
insert into typeproduct (id_typeProduct, name) values (2,'Shoes');
insert into typeproduct (id_typeProduct, name) values (3,'Complements');

insert into product (id_product, id_typeProduct, name, gender, size, price)
values (1,1,'BatmanShirt','W','L',35.20);
insert into product (id_product, id_typeProduct, name, gender, size, price)
values (2,1,'BlackShirt','M','L',25.20);
insert into product (id_product, id_typeProduct, name, gender, size, price)
values (3,2,'Boots','W','39',49.50);
insert into product (id_product, id_typeProduct, name, gender, size, price)
values (4,3,'Hat','M','L',32.20);
insert into product (id_product, id_typeProduct, name, gender, size, price)
values (5,1,'skirt','W','38',29.99);
