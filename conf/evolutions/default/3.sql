
  -- !Ups
INSERT INTO typeproduct (id_typeproduct, name) VALUES (1, 'Clothes'),
    (2, 'Shoes'),
    (3, 'Complements'),
    (4, 'Undefined') ON CONFLICT (id_typeproduct) DO NOTHING;

  -- !Downs
  DELETE FROM typeproduct WHERE id_typeproduct = 1;
  DELETE FROM typeproduct WHERE id_typeproduct = 2;
  DELETE FROM typeproduct WHERE id_typeproduct = 3;