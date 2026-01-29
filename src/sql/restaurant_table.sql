CREATE TABLE restaurant_table (
    id SERIAL PRIMARY KEY,
    table_number INT NOT NULL UNIQUE,
    CONSTRAINT check_table_number_positive CHECK (table_number > 0)
);

ALTER TABLE "order"
ADD COLUMN IF NOT EXISTS id_table INT NOT NULL REFERENCES restaurant_table(id),
ADD COLUMN IF NOT EXISTS arrival_datetime TIMESTAMP NOT NULL,
ADD COLUMN IF NOT EXISTS departure_datetime TIMESTAMP;

ALTER TABLE "order"
ADD CONSTRAINT check_departure_after_arrival
    CHECK (departure_datetime IS NULL OR departure_datetime > arrival_datetime);

CREATE INDEX IF NOT EXISTS idx_order_table_datetime
ON "order"(id_table, arrival_datetime, departure_datetime);

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