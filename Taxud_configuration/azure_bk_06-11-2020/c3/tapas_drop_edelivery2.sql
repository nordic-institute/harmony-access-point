
DROP USER edelivery2 cascade;
CREATE USER edelivery2 IDENTIFIED BY edelivery2 DEFAULT TABLESPACE TAXUDDATATB QUOTA UNLIMITED ON TAXUDDATATB;
GRANT CREATE SESSION TO edelivery2;
GRANT CREATE TABLE TO edelivery2;
GRANT CREATE VIEW TO edelivery2;
GRANT CREATE SEQUENCE TO edelivery2;
GRANT EXECUTE ON DBMS_XA TO edelivery2;
GRANT SELECT ON PENDING_TRANS$ TO edelivery2;
GRANT SELECT ON DBA_2PC_PENDING TO edelivery2;
GRANT SELECT ON DBA_PENDING_TRANSACTIONS TO edelivery2;