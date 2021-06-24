
-- This function migrates and old ID_PK to the new sequence id format

CREATE OR REPLACE FUNCTION migrate_seq(id_pk IN NUMBER,
                                       creation_time IN DATE)
    RETURN NUMBER IS
    seq_id NUMBER;
BEGIN
    select to_number(to_char(creation_time, 'YYMMDDHH') || to_char(id_pk, 'FM0000000000')) into seq_id from dual;
    RETURN seq_id;
END;


-- This function generates a new sequence id, to be used for manual inserts

CREATE OR REPLACE FUNCTION gen_domibus_scalable_sequence
    RETURN Number
    IS
    seq_id Number;
BEGIN
    select to_number(to_char(SYSDATE, 'YYMMDDHH') || to_char(DOMIBUS_SCALABLE_SEQUENCE.nextval, 'FM0000000000'))
    into seq_id
    from dual;
    RETURN seq_id;
END;
