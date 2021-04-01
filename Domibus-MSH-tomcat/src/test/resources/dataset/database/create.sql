
-- Make sure the tests are always running on a clean db
DROP ALL OBJECTS;
CREATE SCHEMA IF NOT EXISTS test;
set schema test;
