ALTER TABLE dish
add column if not exists price DOUBLE PRECISION;

UPDATE dish SET price = 2000 WHERE name = 'Salade fraiche';
UPDATE dish SET price = 6000 WHERE name = 'Poulet grille';

UPDATE dish SET price = NULL WHERE name = 'Riz au legume';
UPDATE dish SET price = NULL WHERE name = 'Gateau au chocolat';
UPDATE dish SET price = NULL WHERE name = 'Salade de fruit';