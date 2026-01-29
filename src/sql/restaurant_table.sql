CREATE TABLE restaurant_table (
    id SERIAL PRIMARY KEY,
    table_number INT NOT NULL UNIQUE,
    CONSTRAINT check_table_number_positive CHECK (table_number > 0)
);

ALTER TABLE "order"
ADD COLUMN IF NOT EXISTS id_table INT,
ADD COLUMN IF NOT EXISTS arrival_datetime TIMESTAMP,
ADD COLUMN IF NOT EXISTS departure_datetime TIMESTAMP;


UPDATE "order"
SET id_table = 1,
    arrival_datetime = NOW()
WHERE id_table IS NULL
   OR arrival_datetime IS NULL;

ALTER TABLE "order"
ALTER COLUMN id_table SET NOT NULL,
ALTER COLUMN arrival_datetime SET NOT NULL;

ALTER TABLE "order"
ADD CONSTRAINT fk_order_table
FOREIGN KEY (id_table)
REFERENCES restaurant_table(id);


ALTER TABLE "order"
ADD CONSTRAINT check_departure_after_arrival
CHECK (
    departure_datetime IS NULL
    OR departure_datetime > arrival_datetime
);

INSERT INTO restaurant_table (table_number) VALUES
(1),
(2),
(3),
(4),
(5),
(6),
(7),
(8),
(9),
(10);