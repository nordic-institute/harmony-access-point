create or replace PROCEDURE DeleteExpiredDownloadedMessages (
    message_number_to_delete   IN NUMBER DEFAULT 1000,
    mpc                        VARCHAR2,
    start_date                   DATE,
    end_date                   DATE
) AS
BEGIN
    FOR user_mess_rec IN (
        SELECT
            usermessag0_.message_id AS message_id
        FROM
            tb_message_log usermessag0_
        WHERE
            usermessag0_.message_status = 'DOWNLOADED'
            AND usermessag0_.mpc = mpc
            AND ( usermessag0_.downloaded IS NOT NULL )
            AND usermessag0_.downloaded >= start_date
            AND usermessag0_.downloaded < end_date
            AND ROWNUM <= message_number_to_delete
    ) LOOP
        FOR signal_mess_rec IN (
            SELECT
                sm.id_pk,
                sm.receipt_id_pk,
                mi.message_id,
                mi.id_pk minfo_pk
            FROM
                tb_signal_message   sm,
                tb_message_info     mi
            WHERE
                sm.messageinfo_id_pk = mi.id_pk
                AND mi.ref_to_message_id = user_mess_rec.message_id
        ) LOOP
            DELETE FROM tb_messaging
            WHERE
                signal_message_id = signal_mess_rec.id_pk;

            DELETE FROM tb_error_log
            WHERE
                error_signal_message_id = signal_mess_rec.message_id;

            DELETE FROM tb_receipt_data
            WHERE
                receipt_id = signal_mess_rec.receipt_id_pk;

            DELETE FROM tb_rawenvelope_log
            WHERE
                signalmessage_id_fk = signal_mess_rec.id_pk;

            DELETE FROM tb_error
            WHERE
                signalmessage_id = signal_mess_rec.id_pk;

            DELETE FROM tb_signal_message
            WHERE
                id_pk = signal_mess_rec.id_pk;

            DELETE FROM tb_receipt
            WHERE
                id_pk = signal_mess_rec.receipt_id_pk;

            DELETE FROM tb_message_info
            WHERE
                id_pk = signal_mess_rec.minfo_pk;



           -- DBMS_OUTPUT.PUT_LINE('Receipt id:'||receipt_mess_rec .id_pk);

           -- DELETE FROM tb_message_log where message_id=signal_mess_rec.message_id;
         --   DELETE FROM tb_message_info where message_id=signal_mess_rec.message_id;
           -- DELETE FROM tb_message_ui where message_id=signal_mess_rec.message_id;
            --DBMS_OUTPUT.PUT_LINE('Signal message id:'||signal_mess_rec.message_id);

        END LOOP;

        FOR inner_user_mess_rec IN (
            SELECT
                um.id_pk,
                mi.message_id,
                mi.id_pk minfo_pk
            FROM
                tb_user_message   um,
                tb_message_info   mi
            WHERE
                um.messageinfo_id_pk = mi.id_pk
                AND mi.message_id = user_mess_rec.message_id
        ) LOOP
            DELETE FROM tb_messaging
            WHERE
                user_message_id = inner_user_mess_rec.id_pk;

            DELETE FROM tb_error_log
            WHERE
                message_in_error_id = inner_user_mess_rec.message_id;

            DELETE FROM tb_party_id
            WHERE
                from_id = inner_user_mess_rec.id_pk;

            DELETE FROM tb_party_id
            WHERE
                to_id = inner_user_mess_rec.id_pk;

            FOR part_info_rec IN (
                SELECT
                    id_pk
                FROM
                    tb_part_info
                WHERE
                    payloadinfo_id = inner_user_mess_rec.id_pk
            ) LOOP DELETE FROM tb_property
            WHERE
                partproperties_id = part_info_rec.id_pk;

            END LOOP;

            DELETE FROM tb_property
            WHERE
                messageproperties_id = inner_user_mess_rec.id_pk;

            DELETE FROM tb_part_info
            WHERE
                payloadinfo_id = inner_user_mess_rec.id_pk;

            DELETE FROM tb_rawenvelope_log
            WHERE
                usermessage_id_fk = inner_user_mess_rec.id_pk;

            DELETE FROM tb_user_message
            WHERE
                id_pk = inner_user_mess_rec.id_pk;

            DELETE FROM tb_message_info
            WHERE
                id_pk = inner_user_mess_rec.minfo_pk;

        END LOOP;

        DELETE FROM tb_message_log
        WHERE
            message_id = user_mess_rec.message_id;

        DELETE FROM tb_message_ui
        WHERE
            message_id = user_mess_rec.message_id;

    END LOOP;

    COMMIT;
END delete_received_messages;


create or replace PROCEDURE DeleteExpiredDownloadedMessages (
    mpc         VARCHAR2,
    startdate   DATE,
    maxcount    IN NUMBER DEFAULT 1000
) AS
BEGIN
    INSERT INTO DOWNLOADED_temp_message_id
        ( SELECT
            usermessag0_.message_id AS message_id
        FROM
            tb_message_log usermessag0_
        WHERE
            usermessag0_.message_status = 'DOWNLOADED'
            AND usermessag0_.mpc = mpc
            AND ( usermessag0_.downloaded IS NOT NULL )
            AND usermessag0_.downloaded < STARTDATE
            AND ROWNUM <= MAXCOUNT
        );

    INSERT INTO DOWNLOADED_temp_signal_id
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
                    DOWNLOADED_temp_message_id
            )
        );

    INSERT INTO DOWNLOADED_temp_user_message_id
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
                    DOWNLOADED_temp_message_id
            )
        );

    INSERT INTO DOWNLOADED_temp_part_id
        ( SELECT
            id_pk
        FROM
            tb_part_info
        WHERE
            payloadinfo_id IN (
                SELECT
                    id_pk
                FROM
                    DOWNLOADED_temp_user_message_id
            )
        );

    DELETE FROM tb_messaging
    WHERE
        signal_message_id IN (
            SELECT
                id_pk
            FROM
                DOWNLOADED_temp_signal_id
        );

    DELETE FROM tb_error_log
    WHERE
        error_signal_message_id IN (
            SELECT
                message_id
            FROM
                DOWNLOADED_temp_signal_id
        );

    DELETE FROM tb_receipt_data
    WHERE
        receipt_id IN (
            SELECT
                receipt_id_pk
            FROM
                DOWNLOADED_temp_signal_id
        );

    DELETE FROM tb_rawenvelope_log
    WHERE
        signalmessage_id_fk IN (
            SELECT
                id_pk
            FROM
                DOWNLOADED_temp_signal_id
        );

    DELETE FROM tb_error
    WHERE
        signalmessage_id IN (
            SELECT
                id_pk
            FROM
                DOWNLOADED_temp_signal_id
        );

    DELETE FROM tb_signal_message
    WHERE
        id_pk IN (
            SELECT
                id_pk
            FROM
                DOWNLOADED_temp_signal_id
        );

    DELETE FROM tb_receipt
    WHERE
        id_pk IN (
            SELECT
                receipt_id_pk
            FROM
                DOWNLOADED_temp_signal_id
        );

    DELETE FROM tb_message_info
    WHERE
        id_pk IN (
            SELECT
                minfo_pk
            FROM
                DOWNLOADED_temp_signal_id
        );
            -- delete user message.

    DELETE FROM tb_messaging
    WHERE
        user_message_id IN (
            SELECT
                id_pk
            FROM
                DOWNLOADED_temp_user_message_id
        );

    DELETE FROM tb_error_log
    WHERE
        message_in_error_id IN (
            SELECT
                message_id
            FROM
                DOWNLOADED_temp_user_message_id
        );

    DELETE FROM tb_party_id
    WHERE
        from_id IN (
            SELECT
                id_pk
            FROM
                DOWNLOADED_temp_user_message_id
        );

    DELETE FROM tb_party_id
    WHERE
        to_id IN (
            SELECT
                id_pk
            FROM
                DOWNLOADED_temp_user_message_id
        );

    DELETE FROM tb_property
    WHERE
        partproperties_id IN (
            SELECT
                id_pk
            FROM
                DOWNLOADED_temp_part_id
        );

    DELETE FROM tb_property
    WHERE
        messageproperties_id IN (
            SELECT
                id_pk
            FROM
                DOWNLOADED_temp_user_message_id
        );

    DELETE FROM tb_part_info
    WHERE
        payloadinfo_id IN (
            SELECT
                id_pk
            FROM
                DOWNLOADED_temp_user_message_id
        );

    DELETE FROM tb_rawenvelope_log
    WHERE
        usermessage_id_fk IN (
            SELECT
                id_pk
            FROM
                DOWNLOADED_temp_user_message_id
        );

    DELETE FROM tb_user_message
    WHERE
        id_pk IN (
            SELECT
                id_pk
            FROM
                DOWNLOADED_temp_user_message_id
        );

    DELETE FROM tb_message_info
    WHERE
        id_pk IN (
            SELECT
                minfo_pk
            FROM
                DOWNLOADED_temp_user_message_id
        );

    DELETE FROM tb_message_log
    WHERE
        message_id IN (
            SELECT
                message_id
            FROM
                DOWNLOADED_temp_user_message_id
        );

    DELETE FROM tb_message_ui
    WHERE
        message_id IN (
            SELECT
                message_id
            FROM
                DOWNLOADED_temp_user_message_id
        );

    COMMIT;
END DeleteExpiredDownloadedMessages;