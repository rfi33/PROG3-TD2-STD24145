CREATE TABLE "order" (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(250),
    creation_datetime TIMESTAMP
)

CREATE TABLE dish_order(
    id SERIAL PRIMARY KEY,
    id_order int reference order(id),
    id_dish int reference dish(id),
    quantity int
);

