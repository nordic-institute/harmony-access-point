
-- Make sure the tests are always running on a clean h2 database
DROP ALL OBJECTS;
CREATE SCHEMA IF NOT EXISTS testdb;
SET SCHEMA testdb;
