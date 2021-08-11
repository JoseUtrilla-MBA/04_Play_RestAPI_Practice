-- !Ups
CREATE TABLE if not exists typeproduct (
    id_typeproduct BIGSERIAL NOT NULL PRIMARY KEY,
    name VARCHAR (50)
);

-- !Downs

DROP TABLE IF EXISTS typeproduct;