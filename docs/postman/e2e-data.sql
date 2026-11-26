INSERT INTO role (id, role) VALUES (1, 'ADMIN'), (2, 'USER');
INSERT INTO users (id, name, lastname, user, password, role) VALUES
  (1, 'Admin', 'Prueba', 'admin_test', '$2a$10$tB.dNuroEhI6/tuknwFPL.wFVXLg0vkItTSgajxh440d84YS0s3.y', 1),
  (2, 'Cajero', 'Prueba', 'cajero_test', '$2a$10$tB.dNuroEhI6/tuknwFPL.wFVXLg0vkItTSgajxh440d84YS0s3.y', 2);
INSERT INTO categories (id, category) VALUES (1, 'Bebidas');
INSERT INTO products (id, name, description, cost, stock, active, category) VALUES
  (1, 'Bebida prueba', 'Producto para pruebas HTTP', 2.50, 18, TRUE, 1);
INSERT INTO clients (id, name, lastname, email, address, dni) VALUES
  (1, 'Cliente', 'Prueba', 'cliente@example.test', 'Lima', '12345678');
ALTER TABLE role ALTER COLUMN id RESTART WITH 100;
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE categories ALTER COLUMN id RESTART WITH 100;
ALTER TABLE products ALTER COLUMN id RESTART WITH 100;
ALTER TABLE clients ALTER COLUMN id RESTART WITH 100;
ALTER TABLE sales ALTER COLUMN id RESTART WITH 100;
ALTER TABLE sale_detail ALTER COLUMN id RESTART WITH 100;
