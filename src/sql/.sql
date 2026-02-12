SELECT
    COALESCE(
            SUM(
                    CASE
                        WHEN type = 'OUT' THEN -quantity
                        ELSE quantity
                        END
            ),
            0
    ) AS stock_value
FROM stock_movement
WHERE creation_datetime <= '2026-02-12 23:59:59';

SELECT
    i.unit,
    COALESCE(
            SUM(
                    CASE
                        WHEN sm.type = 'OUT' THEN -sm.quantity
                        ELSE sm.quantity
                        END
            ),
            0
    ) AS actual_quantity
FROM stock_movement sm
         JOIN ingredient i ON sm.id_ingredient = i.id
WHERE sm.id_ingredient = ?
  AND sm.creation_datetime <= ?
GROUP BY i.unit;
