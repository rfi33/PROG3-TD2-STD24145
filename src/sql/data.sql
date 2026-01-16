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