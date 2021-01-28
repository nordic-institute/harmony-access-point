create or replace PROCEDURE DeleteExpiredSentMessages (
    mpc         VARCHAR2,
    startdate   DATE,
    maxcount    IN NUMBER DEFAULT 1000
) AS
BEGIN
    INSERT INTO sent_temp_message_id
        ( SELECT
            message_id AS message_id
        FROM
            tb_message_log
        WHERE
            ( message_status = 'ACKNOWLEDGED'
              OR message_status = 'SEND_FAILURE' )
            AND mpc = mpc
            AND ( modification_time IS NOT NULL )
            AND modification_time < startdate
            AND ROWNUM <= maxcount
        );

    INSERT INTO sent_temp_signal_id
        ( SELECT
            tb_signal_message.id_pk,
            tb_signal_message.receipt_id_pk,
            tb_message_info.message_id,
            tb_message_info.id_pk minfo_pk
        FROM
            tb_signal_message,
            tb_message_info
        WHERE
            tb_signal_message.messageinfo_id_pk = tb_message_info.id_pk
            AND tb_message_info.ref_to_message_id IN (
                SELECT
                    message_id
                FROM
                    sent_temp_message_id
            )
        );

    INSERT INTO sent_temp_user_message_id
        ( SELECT
            um.id_pk,
            mi.message_id,
            mi.id_pk minfo_pk
        FROM
            tb_user_message   um,
            tb_message_info   mi
        WHERE
            um.messageinfo_id_pk = mi.id_pk
            AND mi.message_id IN (
                SELECT
                    message_id
                FROM
                    sent_temp_message_id
            )
        );

    INSERT INTO sent_temp_part_id
        ( SELECT
            id_pk
        FROM
            tb_part_info
        WHERE
            payloadinfo_id IN (
                SELECT
                    id_pk
                FROM
                    sent_temp_user_message_id
            )
        );

    DELETE FROM tb_messaging
    WHERE
        signal_message_id IN (
            SELECT
                id_pk
            FROM
                sent_temp_signal_id
        );

    DELETE FROM tb_error_log
    WHERE
        error_signal_message_id IN (
            SELECT
                message_id
            FROM
                sent_temp_signal_id
        );

    DELETE FROM tb_receipt_data
    WHERE
        receipt_id IN (
            SELECT
                receipt_id_pk
            FROM
                sent_temp_signal_id
        );

    DELETE FROM tb_rawenvelope_log
    WHERE
        signalmessage_id_fk IN (
            SELECT
                id_pk
            FROM
                sent_temp_signal_id
        );

    DELETE FROM tb_error
    WHERE
        signalmessage_id IN (
            SELECT
                id_pk
            FROM
                sent_temp_signal_id
        );

    DELETE FROM tb_signal_message
    WHERE
        id_pk IN (
            SELECT
                id_pk
            FROM
                sent_temp_signal_id
        );

    DELETE FROM tb_receipt
    WHERE
        id_pk IN (
            SELECT
                receipt_id_pk
            FROM
                sent_temp_signal_id
        );

    DELETE FROM tb_message_info
    WHERE
        id_pk IN (
            SELECT
                minfo_pk
            FROM
                sent_temp_signal_id
        );
            -- delete user message.

    DELETE FROM tb_messaging
    WHERE
        user_message_id IN (
            SELECT
                id_pk
            FROM
                sent_temp_user_message_id
        );

    DELETE FROM tb_error_log
    WHERE
        message_in_error_id IN (
            SELECT
                message_id
            FROM
                sent_temp_user_message_id
        );

    DELETE FROM tb_party_id
    WHERE
        from_id IN (
            SELECT
                id_pk
            FROM
                sent_temp_user_message_id
        );

    DELETE FROM tb_party_id
    WHERE
        to_id IN (
            SELECT
                id_pk
            FROM
                sent_temp_user_message_id
        );

    DELETE FROM tb_property
    WHERE
        partproperties_id IN (
            SELECT
                id_pk
            FROM
                sent_temp_part_id
        );

    DELETE FROM tb_property
    WHERE
        messageproperties_id IN (
            SELECT
                id_pk
            FROM
                sent_temp_user_message_id
        );

    DELETE FROM tb_part_info
    WHERE
        payloadinfo_id IN (
            SELECT
                id_pk
            FROM
                sent_temp_user_message_id
        );

    DELETE FROM tb_rawenvelope_log
    WHERE
        usermessage_id_fk IN (
            SELECT
                id_pk
            FROM
                sent_temp_user_message_id
        );

    DELETE FROM tb_user_message
    WHERE
        id_pk IN (
            SELECT
                id_pk
            FROM
                sent_temp_user_message_id
        );

    DELETE FROM tb_message_info
    WHERE
        id_pk IN (
            SELECT
                minfo_pk
            FROM
                sent_temp_user_message_id
        );

    DELETE FROM tb_message_log
    WHERE
        message_id IN (
            SELECT
                message_id
            FROM
                sent_temp_user_message_id
        );

    DELETE FROM tb_send_attempt
    WHERE
        message_id IN (
            SELECT
                message_id
            FROM
                sent_temp_user_message_id
        );

    DELETE FROM tb_message_acknw
    WHERE
        message_id IN (
            SELECT
                message_id
            FROM
                sent_temp_user_message_id
        );

    DELETE FROM tb_message_ui
    WHERE
        message_id IN (
            SELECT
                message_id
            FROM
                sent_temp_user_message_id
        );

    COMMIT;
END DeleteExpiredSentMessages;