DECLARE
  message_number_to_delete NUMBER;
  mpc VARCHAR2(255);
  start_date DATE;
  end_date DATE;
BEGIN
  message_number_to_delete := 5000;
  mpc:='urn:fdc:ec.europa.eu:2019:eu_ics2_t2c';
  start_date:=SYSDATE-4; -- messages will be deleted including start_date
  end_date:=SYSDATE-3; -- messages will be deleted excluding the end date

  DeleteExpiredDownloadedMessages(
    mpc=>mpc,
    start_date=>start_date,
    end_date=>end_date,
    message_number_to_delete=>message_number_to_delete
  );
END;

DECLARE
  message_number_to_delete NUMBER;
  mpc VARCHAR2(255);
  start_date DATE;
  end_date DATE;
BEGIN
  message_number_to_delete := 5000;
  mpc:='urn:fdc:ec.europa.eu:2019:eu_ics2_t2c';
  start_date:=SYSDATE-30; -- messages will be deleted including start_date
  end_date:=SYSDATE-4-1/24; -- messages will be deleted excluding the end date

  DeleteExpiredDownloadedMessages(
    mpc=>mpc,
    start_date=>start_date,
    end_date=>end_date,
    message_number_to_delete=>message_number_to_delete
  );
END;

DECLARE
  message_number_to_delete NUMBER;
  mpc VARCHAR2(255);
  start_date DATE;
  end_date DATE;
BEGIN
  message_number_to_delete := 5000;
  mpc:='urn:fdc:ec.europa.eu:2019:eu_ics2_t2c';
  start_date:=SYSDATE-4; -- messages will be deleted including start_date
  end_date:=SYSDATE-3; -- messages will be deleted excluding the end date

  DeleteExpiredSentMessages(
    mpc=>mpc,
    start_date=>start_date,
    end_date=>end_date,
    message_number_to_delete=>message_number_to_delete
  );
END;

DECLARE
  message_number_to_delete NUMBER;
  mpc VARCHAR2(255);
  start_date DATE;
  end_date DATE;
BEGIN
  message_number_to_delete := 5000;
  mpc:='urn:fdc:ec.europa.eu:2019:eu_ics2_t2c';
  start_date:=SYSDATE-30; -- messages will be deleted including start_date
  end_date:=SYSDATE-4-1/24; -- messages will be deleted excluding the end date

  DeleteExpiredSentMessages(
    mpc=>mpc,
    start_date=>start_date,
    end_date=>end_date,
    message_number_to_delete=>message_number_to_delete
  );
END;
