
  -- !Ups
INSERT INTO typeproduct (name) VALUES ('Clothes'),
    ('Shoes'),
    ('Complements'),
    ('Undefined') ON CONFLICT (id_typeproduct) DO NOTHING;

  -- !Downs
  DELETE FROM typeproduct;
