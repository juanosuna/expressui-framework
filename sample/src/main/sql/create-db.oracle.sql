-- This statement can be used to create a tablespace.
-- It is provided here as documentation and only needs to be run once
-- from SQLPlus.
-- CREATE TABLESPACE TBS_SAMPLE
--    DATAFILE 'tbs_sample.dat' SIZE 40M
--    ONLINE;
--
-- This file will never get executed.
-- If you use Oracle, you can copy and paste this into create-db.prod.sql

CREATE USER SAMPLE IDENTIFIED BY sample DEFAULT TABLESPACE TBS_SAMPLE TEMPORARY TABLESPACE TEMP;
GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE to SAMPLE;