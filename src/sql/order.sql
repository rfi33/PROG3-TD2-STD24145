CREATE TABLE "order" (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(250),
    creation_datetime TIMESTAMP
);

CREATE TABLE dish_order(
    id SERIAL PRIMARY KEY,
    id_order int references "order"(id),
    id_dish int references dish(id),
    quantity int
);

ALTER TABLE "order"
ADD COLUMN IF NOT EXISTS total_amount_ht NUMERIC(12,2),
ADD COLUMN IF NOT EXISTS total_amount_ttc NUMERIC(12,2);

ALTER TABLE "order"
ADD CONSTRAINT check_total_amount_ht_positive
    CHECK (total_amount_ht IS NULL OR total_amount_ht >= 0);

ALTER TABLE "order"
ADD CONSTRAINT check_total_amount_ttc_positive
    CHECK (total_amount_ttc IS NULL OR total_amount_ttc >= 0);

ALTER TABLE dish_order
ADD CONSTRAINT check_quantity_positive
    CHECK (quantity > 0);

CREATE INDEX IF NOT EXISTS idx_order_reference ON "order"(reference);
CREATE INDEX IF NOT EXISTS idx_order_creation_datetime ON "order"(creation_datetime);