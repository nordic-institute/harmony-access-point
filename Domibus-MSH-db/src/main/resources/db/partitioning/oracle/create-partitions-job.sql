-- creates a Job that runs daily and creates partitions for the day a week from now.
BEGIN
    DBMS_SCHEDULER.CREATE_JOB (
            job_name => '"GENERATE_PARTITIONS_JOB"',
            job_type => 'PLSQL_BLOCK',
            job_action => 'BEGIN
                            PARTITIONSGEN();
                            END;',
            number_of_arguments => 0,
            start_date => SYSTIMESTAMP,
            repeat_interval => 'FREQ=DAILY',
            end_date => NULL,
            enabled => TRUE,
            auto_drop => FALSE,
            comments => 'Create partitions job');
END;