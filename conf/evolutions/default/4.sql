
  -- !Ups
INSERT INTO product (
        id_product,
        id_typeProduct,
        name,
        gender,
        size,
        price
    )
VALUES (1, 1, 'Shirt', 'W', 'M', 30.5),
    (2, 1, 'Skirt', 'W', 'L', 32),
    (3, 2, 'Boots', 'M', '42', 45.99),
    (4, 1, 'Shirt', 'M', 'XL', 35.99),
    (5, 3, 'Hat', 'W', 's', 59.99),
    (6, 2, 'Sneakers', 'W', '41', 37.5),
    (7, 1, 'Skirt', 'W', 'L', 52),
    (8, 2, 'Boots', 'M', '42', 35.99),
    (9, 1, 'Shirt', 'M', 'XL', 45.99),
    (10, 3, 'Hat', 'W', 's', 39.99),
    (11, 2, 'Sneakers', 'W', '41', 67.5) ON CONFLICT (id_product) DO NOTHING;

  -- !Downs

  DELETE FROM product WHERE id_product = 1;
  DELETE FROM product WHERE id_product = 2;
  DELETE FROM product WHERE id_product = 3;
  DELETE FROM product WHERE id_product = 4;
  DELETE FROM product WHERE id_product = 5;
  DELETE FROM product WHERE id_product = 6;
  DELETE FROM product WHERE id_product = 7;
  DELETE FROM product WHERE id_product = 8;
  DELETE FROM product WHERE id_product = 9;
  DELETE FROM product WHERE id_product = 10;
  DELETE FROM product WHERE id_product = 11;
