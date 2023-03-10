CREATE OR REPLACE PROCEDURE drop_partition (partition_name IN VARCHAR2) IS
   BEGIN
      execute immediate 'ALTER TABLE TB_USER_MESSAGE DROP PARTITION ' || partition_name || ' UPDATE INDEXES';
   END;
/
