-- *********************************************************************
-- Delete script for Oracle Domibus DB with a time interval
-- Change START_DATE and END_DATE values accordingly - please pay attention
-- that the data stored in DB is timezone agnostic.
--
-- Important: In order to keep the JMS queues synchronized with the DB data that will be
-- deleted by this script, the Domibus Administrator should remove manually the associated
-- JMS messages FROM the plugin notifications queues
-- *********************************************************************

/* For SQL*Plus client use specific definition like:
   variable START_DATE varchar2(30)
   exec :START_DATE := '2013-10-01';
*/
DEFINE START_DATE = TO_DATE('28-SEP-2021 09:00:00', 'DD-MM-YY HH24:MI:SS');
DEFINE END_DATE = TO_DATE('29-SEP-2021 10:00:00', 'DD-MM-YY HH24:MI:SS');

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

DELETE
FROM TB_D_ACTION td
WHERE td.ID_PK IN
      (SELECT tu.ACTION_ID_FK FROM TB_USER_MESSAGE tu WHERE tu.CREATION_TIME BETWEEN @START_DATE AND @END_DATE);

DELETE
FROM TB_D_AGREEMENT td
WHERE td.ID_PK IN
      (SELECT tu.AGREEMENT_ID_FK FROM TB_USER_MESSAGE tu WHERE tu.CREATION_TIME BETWEEN @START_DATE AND @END_DATE);

DELETE
FROM TB_D_MESSAGE_PROPERTY tdm
WHERE (tdm.ID_PK IN (SELECT tm.MESSAGE_PROPERTY_FK
                     FROM TB_MESSAGE_PROPERTIES tm
                     WHERE tm.USER_MESSAGE_ID_FK IN
                           (SELECT tu.ID_PK FROM TB_USER_MESSAGE tu WHERE tu.CREATION_TIME < now())));

DELETE
FROM TB_D_MESSAGE_STATUS tdm
WHERE (tdm.ID_PK IN (SELECT tm.MESSAGE_STATUS_ID_FK
                     FROM TB_USER_MESSAGE_LOG tm
                     WHERE tm.ID_PK IN (SELECT tu.ID_PK
                                        FROM TB_USER_MESSAGE tu
                                        WHERE tu.CREATION_TIME BETWEEN @START_DATE AND @END_DATE)));

DELETE
FROM TB_D_MPC td
WHERE td.ID_PK IN
      (SELECT tu.MPC_ID_FK FROM TB_USER_MESSAGE tu WHERE tu.CREATION_TIME BETWEEN @START_DATE AND @END_DATE);

DELETE
FROM TB_D_MSH_ROLE tdm
WHERE (tdm.ID_PK IN (SELECT tm.MSH_ROLE_ID_FK
                     FROM TB_USER_MESSAGE_LOG tm
                     WHERE tm.ID_PK IN (SELECT tu.ID_PK
                                        FROM TB_USER_MESSAGE tu
                                        WHERE tu.CREATION_TIME BETWEEN @START_DATE AND @END_DATE)));

DELETE
FROM TB_D_NOTIFICATION_STATUS tdn
WHERE (tdn.ID_PK IN (SELECT tm.NOTIFICATION_STATUS_ID_FK
                     FROM TB_USER_MESSAGE_LOG tm
                     WHERE tm.ID_PK IN (SELECT tu.ID_PK
                                        FROM TB_USER_MESSAGE tu
                                        WHERE tu.CREATION_TIME BETWEEN @START_DATE AND @END_DATE)));

DELETE
FROM TB_D_PART_PROPERTY td
WHERE (td.ID_PK IN (SELECT tpp.PART_INFO_PROPERTY_FK
                    FROM TB_PART_PROPERTIES tpp
                    WHERE tpp.PART_INFO_ID_FK IN (
                        SELECT tpi.ID_PK
                        FROM TB_PART_INFO tpi
                        WHERE tpi.USER_MESSAGE_ID_FK IN (
                            SELECT tu.ID_PK
                            FROM TB_USER_MESSAGE tu
                            WHERE tu.CREATION_TIME BETWEEN @START_DATE AND @END_DATE))));

DELETE
FROM TB_D_PARTY td
WHERE td.ID_PK IN
      (SELECT tu.FROM_PARTY_ID_FK FROM TB_USER_MESSAGE tu WHERE tu.CREATION_TIME BETWEEN @START_DATE AND @END_DATE);

DELETE
FROM TB_D_PARTY td
WHERE td.ID_PK IN
      (SELECT tu.TO_PARTY_ID_FK FROM TB_USER_MESSAGE tu WHERE tu.CREATION_TIME BETWEEN @START_DATE AND @END_DATE);

DELETE
FROM TB_D_ROLE td
WHERE td.ID_PK IN
      (SELECT tu.FROM_ROLE_ID_FK FROM TB_USER_MESSAGE tu WHERE tu.CREATION_TIME BETWEEN @START_DATE AND @END_DATE);

DELETE
FROM TB_D_ROLE td
WHERE td.ID_PK IN
      (SELECT tu.TO_ROLE_ID_FK FROM TB_USER_MESSAGE tu WHERE tu.CREATION_TIME BETWEEN @START_DATE AND @END_DATE);

DELETE
FROM TB_D_SERVICE td
WHERE td.ID_PK IN
      (SELECT tu.SERVICE_ID_FK FROM TB_USER_MESSAGE tu WHERE tu.CREATION_TIME BETWEEN @START_DATE AND @END_DATE);


SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
COMMIT;


