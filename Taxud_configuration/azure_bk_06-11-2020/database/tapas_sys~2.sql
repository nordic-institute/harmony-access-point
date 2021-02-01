show parameters parallel_max_servers;
show parameters SGA_TARGET ;
show parameters SGA_MAX_SIZE ;
show parameters PGA_TARGET ;
show parameters PARALLEL_MAX_SERVERS ;
show parameters PGA_AGGREGATE_TARGET;
show parameters PROCESSES ;
 show parameter session;
 show parameter DB_CACHE_SIZE;
 show parameter JAVA_POOL_SIZE;
 show parameter LOG_BUFFER;
 
show parameters CPU_COUNT ;

alter system set processes=1000 scope=spfile;

 alter system set SESSIONS=1000 SCOPE=spfile;
 alter system set PROCESSES=1200 SCOPE=spfile;

show parameters use_large_pages;

ALTER SYSTEM SET use_large_pages=false SCOPE=SPFILE;

grant create any directory to EDELIVERY2;

SELECT directory_name, directory_path FROM dba_directories WHERE directory_name='DUMP_DIR';

start

show paramters MEMORY_TARGET;

show parameter target;
ALTER SYSTEM SET SGA_MAX_SIZE=75G SCOPE=spfile;
ALTER SYSTEM SET SGA_TARGET=65G SCOPE=both;
ALTER SYSTEM SET pga_aggregate_target=22G SCOPE=both;

ALTER SYSTEM SET CPU_COUNT=0;

select * from V$PARAMETER where name='CPU_COUNT';

alter system set CPU_COUNT = 0 scope=both;

show paramters DB_CACHE_ADVICE ;

show paramters DB_CACHE_SIZE;

select value from v$pgastat where name='maximum PGA allocated';

ALTER SYSTEM SET sga_max_size=75G SCOPE=SPFILE;

ALTER SYSTEM SET sga_target=75G SCOPE=SPFILE;

ALTER SYSTEM SET CPU_COUNT=0 SCOPE=SPFILE;

SHOW PARAMETER TARGET;

select value from v$parameter where name = 'use_large_pages';

select value/1024 from v$parameter where name = 'sga_target';
 

ALTER SYSTEM SET SGA_MAX_SIZE=75G SCOPE=spfile;
ALTER SYSTEM SET SGA_TARGET=75G SCOPE=both;
ALTER SYSTEM SET pga_aggregate_target=22G SCOPE=both;

alter system set PARALLEL_MAX_SERVERS = 64 scope=both;


SELECT name, value
  FROM v$parameter
 WHERE name = 'sessions'
 
 show parameter sessions
 
 select * from gv$resource_limit;
 
 alter system set SESSIONS=1000 SCOPE=SPFILE ;
 alter system set PROCESSES=5000 SCOPE=SPFILE  ;
 
DROP USER edelivery cascade;
CREATE USER edelivery IDENTIFIED BY edelivery DEFAULT TABLESPACE TAXUDDATATB QUOTA UNLIMITED ON TAXUDDATATB;
GRANT CREATE SESSION TO edelivery;
GRANT CREATE TABLE TO edelivery;
GRANT CREATE VIEW TO edelivery;
GRANT CREATE SEQUENCE TO edelivery;
GRANT EXECUTE ON DBMS_XA TO edelivery;
GRANT SELECT ON PENDING_TRANS$ TO edelivery;
GRANT SELECT ON DBA_2PC_PENDING TO edelivery;
GRANT SELECT ON DBA_PENDING_TRANSACTIONS TO edelivery;
grant create procedure to edelivery;

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
show parameter PRE_PAGE_SGA ;

grant create procedure to edelivery2;

SELECT SID, SERIAL#, STATUS, SERVER, USERNAME FROM V$SESSION WHERE USERNAME = 'EDELIVERY2';

select PROGRAM from v$session
   where TYPE='BACKGROUND' and USERNAME is null;



 select current_utilization, limit_value
    from v$resource_limit
    where resource_name='sessions';

SELECT * FROM DBA_SCHEDULER_JOBS order by NEXT_RUN_DATE desc;

DROP USER edelivery_jms cascade;
CREATE USER edelivery_jms IDENTIFIED BY edelivery_jms DEFAULT TABLESPACE TAXUDDATATB QUOTA UNLIMITED ON TAXUDDATATB;
GRANT CREATE SESSION TO edelivery_jms;
GRANT CREATE TABLE TO edelivery_jms;
GRANT CREATE VIEW TO edelivery_jms;
GRANT CREATE SEQUENCE TO edelivery_jms;
GRANT EXECUTE ON DBMS_XA TO edelivery_jms;
GRANT SELECT ON PENDING_TRANS$ TO edelivery_jms;
GRANT SELECT ON DBA_2PC_PENDING TO edelivery_jms;
GRANT SELECT ON DBA_PENDING_TRANSACTIONS TO edelivery_jms;

ALTER SYSTEM KILL SESSION '7987, 38402';


CREATE USER edelivery IDENTIFIED BY edelivery DEFAULT TABLESPACE TAXUDTB3 QUOTA UNLIMITED ON TAXUDTB3;

DROP TABLESPACE "TAXUDDB-SEPARATEDISK"
INCLUDING CONTENTS AND DATAFILES;


CREATE SMALLFILE TABLESPACE "TAXUDDATATB"
DATAFILE
'/taxud-data-raid/oradata/orcl/taxud/data16.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data17.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data18.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data19.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data20.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data21.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data22.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data23.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data24.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data25.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data26.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data27.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data28.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data29.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data30.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data31.dbf' SIZE 31000M
LOGGING EXTENT MANAGEMENT LOCAL SEGMENT SPACE MANAGEMENT AUTO;

	
	 	
Alter Tablespace  "TAXUDDATATB"  Add Datafile
'/taxud-data-raid/oradata/orcl/taxud/data16.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data17.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data18.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data19.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data20.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data21.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data22.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data23.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data24.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data25.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data26.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data27.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data28.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data29.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data30.dbf' SIZE 31000M,
'/taxud-data-raid/oradata/orcl/taxud/data31.dbf' SIZE 31000M;

Alter Tablespace Add Datafile
An important use of the alter tablespace command is to add datafiles.

If you would like to use alter tablespace to add a datafile, use the following syntax:

ALTER TABLESPACE
   users
ADD DATAFILE
   '/ora01/oracle/oradata/booktst_users_02.dbf'
size 100m

ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data0.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data1.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data2.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data3.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data4.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data5.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data6.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data7.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data8.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data9.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data10.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data11.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data12.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data13.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data14.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data15.dbf' AUTOEXTEND ON NEXT 30G;

ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data16.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data17.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data18.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data19.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data20.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data21.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data22.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data23.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data24.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data25.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data26.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data27.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data28.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data29.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data30.dbf' AUTOEXTEND ON NEXT 30G;
ALTER DATABASE DATAFILE '/taxud-data-raid/oradata/orcl/taxud/data31.dbf' AUTOEXTEND ON NEXT 30G;
	Execute On Multiple Databases		Return


SELECT * FROM V$LOG;




select group#,members,status,bytes/1024/1024 as mb from v$log;

 alter database drop logfile group 4;
 alter database drop logfile group 5;
 alter database drop logfile group 6;

alter system switch logfile;

alter system checkpoint;

alter database add logfile group 1 '/taxud_big_redo/redologs/new_redo01.log' size 25000M;
alter database add logfile group 2 '/taxud_big_redo/redologs/new_redo02.log' size 25000M;
alter database add logfile group 3 '/taxud_big_redo/redologs/new_redo03.log' size 25000M;

  alter database add logfile
     ('/taxud-redo/oradata/redo_g1m1.rdo')
     size 32000m;
     
     
        alter database add logfile
     ('/redo1/oradata/redo_g1m1.rdo')
     size 20000m;
     
     add logfile member '/redo0/oradata/redo_g1m0.rdo' to group 3;
     
    alter database add logfile
     ('/redo1/oradata/redo_g2m2.rdo')
     size 20000m;
     
     
        alter database add logfile
     ('/redo0/oradata/new_redo_g7m1.rdo')
     size 10000m;
     
     alter database drop logfile group 7;
     
     alter sequence SCHEMA_NAME.SEQUENCE_NAME cache 100;
     
create undo tablespace UNDOTBS2 datafile '/u01/app/oracle/oradata/emrep/UNDOTBS02.DBF' size 5000M;

create undo tablespace UNDOTBS2 datafile '/taxud-data-raid/oradata/UNDOTBS02.DBF' size 31G;


alter system set undo_tablespace = undotbs2 scope=both;

ALTER SYSTEM SET UNDO_RETENTION=300;

 show parameter undo;

select tablespace_name, status, count(*) from dba_rollback_segs group by tablespace_name, status;

Drop tablespace UNDOTBS2 including contents and datafiles;

select tablespace_name, SEGMENT_SPACE_MANAGEMENT
  from user_tablespaces;
 
 
  select
   (select username from v$session where sid=a.sid) blocker,
   a.sid,
   ' is blocking ',
   (select username from v$session where sid=b.sid) blockee,
   b.sid
from
   v$lock a,
   v$lock b
where
   a.block = 1
and
   b.request > 0
and
   a.id1 = b.id1
and
   a.id2 = b.id2;
   
   BEGIN
DBMS_STATS.GATHER_DATABASE_STATS_JOB_PROC();
END;

SELECT * FROM DBA_TABLESPACES WHERE CONTENTS = 'UNDO';

show parameter undo

alter system set undo_tablespace=UNDOTBS2 scope=both;

 select tablespace_name, status, count(*) from dba_rollback_segs group by tablespace_name, status;
 
 Drop tablespace UNDOTBS1 including contents and datafiles;
 
 select * from V$SYSTEM_EVENT order by time_waited desc;
