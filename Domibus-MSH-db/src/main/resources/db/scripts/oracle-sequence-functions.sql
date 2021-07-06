-- This function generates a sequence id based on DOMIBUS_SCALABLE_SEQUENCE
CREATE OR REPLACE FUNCTION generate_domibus_scalable_sequence(incr IN NUMBER,
                                                              creation_time IN DATE)
    RETURN NUMBER
    IS
    seq_id Number;
BEGIN
    DECLARE
        len CONSTANT         STRING(20) := 'FM0000000000';
        date_format CONSTANT STRING(10) := 'YYMMDDHH';
    BEGIN
        SELECT to_number(to_char(creation_time, date_format) || to_char(incr, len))
        INTO seq_id
        FROM dual;
        RETURN seq_id;
    END;
END;

-- This function generates a sequence id based on DOMIBUS_SCALABLE_SEQUENCE for a new entry
CREATE OR REPLACE FUNCTION generate_id
    RETURN NUMBER IS
    seq_id NUMBER;
BEGIN
    RETURN generate_domibus_scalable_sequence(DOMIBUS_SCALABLE_SEQUENCE.nextval, SYSDATE);
END;

-- This function generates a new sequence id based on DOMIBUS_SCALABLE_SEQUENCE for an old entry based on old id_pk and old creation_time
CREATE OR REPLACE FUNCTION generate_new_id(old_id IN NUMBER,
                                           creation_time IN DATE)
    RETURN NUMBER IS
    seq_id NUMBER;
BEGIN
    RETURN generate_domibus_scalable_sequence(old_id, creation_time);
END;
