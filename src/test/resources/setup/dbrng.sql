--GPT Generated
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DO $$
    DECLARE
        i INT;
        col_type TEXT[];
        tbl_name TEXT;
    BEGIN
        col_type := ARRAY['INT', 'VARCHAR(100)', 'TEXT', 'BOOLEAN', 'DATE', 'TIMESTAMP', 'UUID', 'NUMERIC(10,2)'];

        FOR i IN 1..50 LOOP
                tbl_name := 'table_' || i;

                EXECUTE format('CREATE TABLE %I (
            id SERIAL PRIMARY KEY,
            col_a %s %s,
            col_b %s %s,
            col_c %s %s,
            col_d %s %s,
            created_at TIMESTAMP DEFAULT NOW()
        );',
                               tbl_name,
                               col_type[(i % 8) + 1], CASE WHEN i % 2 = 0 THEN 'NOT NULL' ELSE '' END,
                               col_type[(i + 1) % 8 + 1], CASE WHEN i % 3 = 0 THEN 'NOT NULL' ELSE '' END,
                               col_type[(i + 2) % 8 + 1], CASE WHEN i % 4 = 0 THEN 'NOT NULL' ELSE '' END,
                               col_type[(i + 3) % 8 + 1], CASE WHEN i % 5 = 0 THEN 'NOT NULL' ELSE '' END
                        );
            END LOOP;

        -- Add some foreign keys between random tables
        FOR i IN 2..50 LOOP
                IF i % 5 = 0 THEN
                    EXECUTE format('ALTER TABLE table_%s ADD COLUMN fk_ref INT REFERENCES table_%s(id);',
                                   i, (i - 1));
                END IF;
            END LOOP;
    END $$;
