CREATE TYPE unit_type AS ENUM ('PCS','KG','L');

CREATE TABLE dish_ingredient(
    id SERIAL PRIMARY KEY,
    id_dish INT NOT NULL REFERENCES dish(id) ON DELETE CASCADE,
    id_ingredient INT NOT NULL REFERENCES ingredient(id) ON DELETE CASCADE,
    quantity_required NUMERIC(10,3),
    unit unit_type,
    UNIQUE (id_dish,id_ingredient)
);

INSERT INTO dish_ingredient (id_dish,id_ingredient)
SELECT id_dish,id
FROM ingredient
WHERE id_dish is not NULl;

ALTER TABLE ingredient
DROP COLUMN id_dish;

ALTER TABLE dish ADD COLUMN selling_price NUMERIC(10,2);