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

