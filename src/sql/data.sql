INSERT INTO dish (id,name,dish_type) values
(1,'Salade fraiche','START'),
(2,'Poulet grille','MAIN'),
(3,'Ris aux legume','MAIN'),
(4,'Gateau au chocolat','DESSERT'),
(5,'Salade de fruits','DESSERT');


INSERT INTO ingredient (id,name,price,category,id_dish) values
(1,'Laitue', 800.00,'VEGETABLE',1),
(2,'Tomate', 600.00,'VEGETABLE',1),
(3,'Poulet', 4500.00,'ANIMAL',2),
(4,'Chocolat',3000.00,'OTHER',4),
(5,'Beurre',2500.00,'DAIRY',4);

INSERT INTO dish (id, name, dish_type, selling_price)
VALUES
    (1, 'Salade fraîche',       'START',   3500.0),
    (2, 'Poulet grillé',        'MAIN',   12000.0),
    (3, 'Riz aux légumes',      'MAIN',      NULL),
    (4, 'Gâteau au chocolat',   'DESSERT',  8000.0),
    (5, 'Salade de fruits',     'DESSERT',   NULL)
    ON CONFLICT (id) DO UPDATE
                            SET name          = EXCLUDED.name,
                            dish_type     = EXCLUDED.dish_type,
                            selling_price = EXCLUDED.selling_price;

INSERT INTO ingredient (id, name, price, category)
VALUES
    (1, 'Laitue',   800.0,  'VEGETABLE'),
    (2, 'Tomate',   600.0,  'VEGETABLE'),
    (3, 'Poulet',  4500.0,  'ANIMAL'),
    (4, 'Chocolat',3000.0,  'OTHER'),
    (5, 'Beurre',  2500.0,  'DAIRY')
    ON CONFLICT (id) DO UPDATE
                            SET name     = EXCLUDED.name,
                            price    = EXCLUDED.price,
                            category = EXCLUDED.category;

INSERT INTO stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
VALUES
    (1,  1, 5.0,  'IN',  'KG', '2024-01-05 08:00'),
    (2,  1, 0.2,  'OUT', 'KG', '2024-01-06 12:00'),
    (3,  2, 4.0,  'IN',  'KG', '2024-01-05 08:00'),
    (4,  2, 0.15, 'OUT', 'KG', '2024-01-06 12:00'),
    (5,  3, 10.0, 'IN',  'KG', '2024-01-04 09:00'),
    (6,  3, 1.0,  'OUT', 'KG', '2024-01-06 13:00'),
    (7,  4, 3.0,  'IN',  'KG', '2024-01-05 10:00'),
    (8,  4, 0.3,  'OUT', 'KG', '2024-01-06 14:00'),
    (9,  5, 2.5,  'IN',  'KG', '2024-01-05 10:00'),
    (10, 5, 0.2,  'OUT', 'KG', '2024-01-06 14:00')
    ON CONFLICT (id) DO NOTHING;

INSERT INTO dish_ingredient (id, id_dish, id_ingredient, quantity_required, unit)
VALUES

    (1, 1, 1, 0.20, 'KG'),
    (2, 1, 2, 0.15, 'KG'),

    (3, 2, 3, 1.00, 'KG'),

    (4, 4, 4, 0.30, 'KG'),
    (5, 4, 5, 0.20, 'KG')
    ON CONFLICT (id) DO UPDATE
                            SET id_dish           = EXCLUDED.id_dish,
                            id_ingredient     = EXCLUDED.id_ingredient,
                            quantity_required = EXCLUDED.quantity_required,
                            unit              = EXCLUDED.unit;

UPDATE dish_ingredient
SET quantity_required = 0.20 ,unit = 'KG'
WHERE id_dish = 1 AND id_ingredient = 1;

UPDATE dish_ingredient
set quantity_required = 0.15, unit = 'KG'
WHERE id_dish = 1 AND id_ingredient = 2;

UPDATE dish_ingredient
set quantity_required = 1.00, unit = 'KG'
where id_dish = 2 and id_ingredient = 3;

UPDATE dish_ingredient
set quantity_required = 0.30,unit = 'KG'
where id_dish = 4 and id_ingredient = 4;

UPDATE dish_ingredient
set quantity_required = 0.20 , unit = 'KG'
where id_dish = 4 AND id_ingredient = 5;

UPDATE dish
set name = 'Salade fraîche',selling_price = 3500.00
WHERE id=1;

UPDATE dish
set selling_price = 12000.00
WHERE id=2;

UPDATE dish
set selling_price = NULL
WHERE id=3;

UPDATE dish
set selling_price =8000.00
WHERE id =4;

UPDATE dish
set selling_price = NULL
WHERE id=5;


INSERT INTO stock_movement (id,id_ingredient,quantity,type,unit,creation_datetime) values
(1,1,5.0,'IN','KG','2024-01-05 08:00'),
(2,1,0.2,'OUT','KG','2024-01-06 12:00'),
(3,2,4.0,'IN','KG','2024-01-05 08:00'),
(4,2,0.15,'OUT','KG','2024-01-06 12:00'),
(5,3,10.0,'IN','KG','2024-01-04 09:00'),
(6,3,1.0,'OUT','KG','2024-01-06 13:00'),
(7,4,3.0,'IN','KG','2024-01-05 10:00'),
(8,4,0.3,'OUT','KG','2024-01-06 14:00'),
(9,5,2.5,'IN','KG','2024-01-05 10:00'),
(10,5,0.2,'OUT','KG','2024-01-06 14:00');