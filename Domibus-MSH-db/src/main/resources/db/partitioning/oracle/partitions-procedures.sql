CREATE OR REPLACE FUNCTION generate_partition(p_date IN DATE)
    RETURN NUMBER IS
    p_id NUMBER;
BEGIN
    DECLARE
        date_format CONSTANT STRING(10) := 'YYMMDDHH24';
    BEGIN
        SELECT to_number(to_char(p_date, date_format))
        INTO p_id
        FROM dual;
        RETURN p_id;
    END;
END;
/

-- creates partitions for the day sysdate + d
CREATE OR REPLACE PROCEDURE PARTITIONSGEN(d IN NUMBER default 7)
AS
BEGIN
    DECLARE
        number_of_partitions CONSTANT  NUMBER := 24; -- one partition every 4 hours
        p_id   NUMBER;
        p_name VARCHAR2(20);
        p_high NUMBER;
    BEGIN
        FOR p_number IN 0 .. (number_of_partitions-1)
            LOOP
                select generate_partition(TRUNC(sysdate+d) + p_number / number_of_partitions ) into p_id from dual;
                p_name := 'P' || p_id;
                p_high := p_id || '0000000000';
                EXECUTE IMMEDIATE 'ALTER TABLE TB_USER_MESSAGE ADD PARTITION ' || p_name || ' VALUES LESS THAN (' || p_high || ')';
            END LOOP;
    END;
END;
/

CREATE OR REPLACE PROCEDURE drop_partition (partition_name IN VARCHAR2) IS
   BEGIN
      execute immediate 'ALTER TABLE TB_USER_MESSAGE DROP PARTITION ' || partition_name || ' UPDATE INDEXES';
   END;
/

-- create partitions for the first 7 days
BEGIN
PARTITIONSGEN(0);
END;
/

BEGIN
PARTITIONSGEN(1);
END;
/

BEGIN
PARTITIONSGEN(2);
END;
/

BEGIN
PARTITIONSGEN(3);
END;
/

BEGIN
PARTITIONSGEN(4);
END;
/

BEGIN
PARTITIONSGEN(5);
END;
/

BEGIN
PARTITIONSGEN(6);
END;
/

BEGIN
PARTITIONSGEN(7);
END;
/

