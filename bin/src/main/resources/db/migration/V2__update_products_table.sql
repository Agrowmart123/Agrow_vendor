USE agrowmart;

ALTER TABLE products
    DROP COLUMN slug,
    DROP COLUMN description,
    DROP COLUMN attributes,
    DROP COLUMN base_unit,
    DROP COLUMN is_perishable,
    DROP COLUMN requires_cold_chain,
    DROP COLUMN created_at,
    DROP COLUMN updated_at,
    ADD COLUMN stock_quantity_kg DOUBLE,
    ADD COLUMN price DECIMAL(12, 2);

