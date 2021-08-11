
  -- !Ups
INSERT INTO product (
        id_typeProduct,
        name,
        gender,
        size,
        price
    )
VALUES ( 1, 'Shirt', 'W', 'M', 30.5),
    ( 1, 'Skirt', 'W', 'L', 32),
    ( 2, 'Boots', 'M', '42', 45.99),
    ( 1, 'Shirt', 'M', 'XL', 35.99),
    ( 3, 'Hat', 'W', 's', 59.99),
    ( 2, 'Sneakers', 'W', '41', 37.5),
    ( 1, 'Skirt', 'W', 'L', 52),
    ( 2, 'Boots', 'M', '42', 35.99),
    ( 1, 'Shirt', 'M', 'XL', 45.99),
    ( 3, 'Hat', 'W', 's', 39.99),
    ( 2, 'Sneakers', 'W', '41', 67.5) ON CONFLICT (id_product) DO NOTHING;

  -- !Downs

  DELETE FROM product ;
