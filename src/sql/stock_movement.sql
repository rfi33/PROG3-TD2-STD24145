CREATE type movement_type as ENUM('IN','OUT');

CREATE TABLE stock_movement(
    id SERIAL PRIMARY KEY,
    id_ingredient int references ingredient(id),
    quantity NUMERIC(10,2),
    type movement_type,
    unit unit_type,
    creation_datetime TIMESTAMP
);