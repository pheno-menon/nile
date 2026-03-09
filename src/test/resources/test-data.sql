-- test-data.sql
-- Optional seed file for use with @Sql in integration tests.
-- Applied before individual test methods when explicitly referenced.
-- @Sql("/test-data.sql") on a test class or method

-- Seed users
INSERT INTO users (name, email, password, role)
VALUES ('Seed User',  'seed.user@nile.com',  '$2a$10$abcdefghijklmnopqrstuuVjnCo.rXFCt5hP8UZ7u6a0e7eVVfWIa', 'ROLE_USER'),
       ('Seed Admin', 'seed.admin@nile.com', '$2a$10$abcdefghijklmnopqrstuuVjnCo.rXFCt5hP8UZ7u6a0e7eVVfWIa', 'ROLE_ADMIN');

-- Seed products
INSERT INTO products (name, price, stock_quantity)
VALUES ('Seed Widget', 9.99,  100),
       ('Seed Gadget', 24.99, 50),
       ('Out of Stock Item', 4.99, 0);