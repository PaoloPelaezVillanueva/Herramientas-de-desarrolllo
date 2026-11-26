-- Run once on MySQL after confirming both queries in TESTING.md return no rows.
ALTER TABLE categories ADD CONSTRAINT uq_categories_name UNIQUE (category);
ALTER TABLE clients ADD CONSTRAINT uq_clients_dni UNIQUE (dni);
