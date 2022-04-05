# Domibus Ongoing Messages Database Migration

Concerns the following scripts inside this directory:
* _db-migration-ongoing-messages-4.2.9-to-4.2.9.sql_
* _db-migration-ongoing-messages-5.0-to-5.0.sql_

These scripts are meant for the migration of ongoing Domibus messages (i.e. messages having one of the _SEND_ENQUEUED_,
_WAITING_FOR_RETRY_, _READY_TO_PULL_ and _WAITING_FOR_RECEIPT_ statuses) from a source Domibus **v4.2.9** application
to a destination Domibus **v4.2.9** application or from a source Domibus **v5.0** application to a destination Domibus
**v5.0** application. All the Domibus source and destination applications are running on Weblogic 12.2/Oracle 19c.

## 1. From v4.2.9 to v4.2.9

The PL/SQL package for migrating ongoing messages between the source and destination databases is located in 
_Domibus-MSH-db/src/main/resources/db/migration/oracle/db-migration-ongoing-messages-4.2.9-to-4.2.9.sql_.

It must be compiled on the source database (v4.2.9 schema). It is intended to be only run using a few ongoing 
messages at a time (hundreds or a few thousands), something to be decided by the DBA. In case of a large result
set of ongoing messages, these need to be split into multiple batches according to the concept of time 
windows. These time windows are to be organised in such manner that they will capture the correct number of
ongoing messages to migrate. The package **DOES NOT** commit at the end of running its _migrate_ procedure, so
the user **MUST** commit or rollback depending on the result of each run.

The main entrypoint of the procedure is the _migrate_ procedure:

    PROCEDURE migrate(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS DEFAULT T_MIGRATION_DETAILS());

The _migrate_ procedure expect a database link to the destination database to be created in the source 
database (usually done by the DBA):
    
    CREATE PUBLIC DATABASE LINK DATABASE_LINK_NAME_v427
    CONNECT TO domibus IDENTIFIED BY domibus
    using
    '(DESCRIPTION=
        (SOURCE_ROUTE=ON)
        (ADDRESS=(PROTOCOL=tcp)(HOST=localhost)(PORT=1521))
        (CONNECT_DATA=
            (SERVICE_NAME=ORCLPDB1)))'
    /
    

To run this procedure, the user could use a similar anonymous block, with the startDate and endDate updated accordingly
(the user is encouraged to enable DBMS_OUTPUT before running this, for a better logging output experience):

    declare
        DB_LINK VARCHAR2(4000);
        MIGRATION MIGRATE_ONGOING_MESSAGES_429.T_MIGRATION_DETAILS;
    begin
        DB_LINK := 'DATABASE_LINK_NAME_v427';
        MIGRATION.startDate := TIMESTAMP '2021-03-11 00:00:00.01';
        MIGRATION.endDate := TIMESTAMP '2021-03-11 23:59:59.99';
    
        MIGRATE_ONGOING_MESSAGES_429.MIGRATE(
            DB_LINK => DB_LINK,
            MIGRATION => MIGRATION
        );
    end;
    /

**Note**: At the end of each execution, the user must remember to COMMIT or ROLLBACK! 

## 2. From v5.0 to v5.0

The PL/SQL package for migrating ongoing messages between the source and destination databases is located in
_Domibus-MSH-db/src/main/resources/db/migration/oracle/db-migration-ongoing-messages-5.0-to-5.0.sql_.

It must be compiled on the source database (v5.0 schema). It is intended to be only run using a few ongoing
messages at a time (hundreds or a few thousands), something to be decided by the DBA. In case of a large result
set of ongoing messages, these need to be split into multiple batches according to the concept of time
windows. These time windows are to be organised in such manner that they will capture the correct number of
ongoing messages to migrate. The package **DOES NOT** commit at the end of running its _migrate_ procedure, so
the user **MUST** commit or rollback depending on the result of each run.

The main entrypoint of the procedure is the _migrate_ procedure:

    PROCEDURE migrate(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS DEFAULT T_MIGRATION_DETAILS());

The _migrate_ procedure expect a database link to the destination database to be created in the source
database (usually done by the DBA):

    CREATE PUBLIC DATABASE LINK DATABASE_LINK_NAME_v50
    CONNECT TO domibus IDENTIFIED BY domibus
    using
    '(DESCRIPTION=
        (SOURCE_ROUTE=ON)
        (ADDRESS=(PROTOCOL=tcp)(HOST=localhost)(PORT=1521))
        (CONNECT_DATA=
            (SERVICE_NAME=ORCLPDB1)))'
    /


To run this procedure, the user could use a similar anonymous block, with the startDate and endDate updated accordingly
(the user is encouraged to enable DBMS_OUTPUT before running this, for a better logging output experience):

    declare
        DB_LINK VARCHAR2(4000);
        MIGRATION MIGRATE_ONGOING_MESSAGES_50.T_MIGRATION_DETAILS;
    begin
        DB_LINK := 'DATABASE_LINK_NAME_v50';
        MIGRATION.startDate := TIMESTAMP '2021-03-11 00:00:00.01';
        MIGRATION.endDate := TIMESTAMP '2021-03-11 23:59:59.99';
    
        MIGRATE_ONGOING_MESSAGES_50.MIGRATE(
            DB_LINK => DB_LINK,
            MIGRATION => MIGRATION
        );
    end;
    /

**Note**: At the end of each execution, the user must remember to COMMIT or ROLLBACK! 
