DECLARE
  MESSAGE_NUMBER_TO_DELETE NUMBER;
    mpc VARCHAR2(255);
    end_date DATE;
BEGIN
  MESSAGE_NUMBER_TO_DELETE := 5000;
  mpc:='urn:fdc:ec.europa.eu:2019:eu_ics2_t2c';
  end_date:=SYSDATE-1;

  DeleteExpiredDownloadedMessages(
    mpc=>mpc,
    STARTDATE=>end_date,
    MAXCOUNT => MESSAGE_NUMBER_TO_DELETE
  );
END;

DECLARE
  MESSAGE_NUMBER_TO_DELETE NUMBER;
    mpc VARCHAR2(255);
    end_date DATE;
BEGIN
  MESSAGE_NUMBER_TO_DELETE := 5000;
  mpc:='urn:fdc:ec.europa.eu:2019:eu_ics2_c2t';
  end_date:=SYSDATE-1;

  DeleteExpiredSentMessages(
    mpc=>mpc,
    STARTDATE=>end_date,
    MAXCOUNT => MESSAGE_NUMBER_TO_DELETE
  );
END;