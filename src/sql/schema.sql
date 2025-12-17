CREATE TYPE types as ENUM ('START','MAIN','DESSERT');

CREATE TABLE dish (
    id SERIAL PRIMARY KEY,
    name VARCHAR(250),
    dish_type types
);


CREATE TYPE categories as ENUM ('VEGETABLE','ANIMAL','MARINE','DAIRY','OTHER');

CREATE TABLE ingredient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(250),
    price NUMERIC(10,2),
    category categories,
    id_dish int REFERENCES ingredient(id)
    );

