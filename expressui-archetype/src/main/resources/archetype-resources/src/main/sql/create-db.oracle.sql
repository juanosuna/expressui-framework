-- This statement can be used to create a tablespace.
-- It is provided here as documentation and only needs to be run once
-- from SQLPlus.
-- CREATE TABLESPACE TBS_${artifactId}
--    DATAFILE 'tbs_${artifactId}.dat' SIZE 40M
--    ONLINE;
--
-- This file will never get executed.
-- If you use Oracle, you can copy and paste this into create-db.prod.sql

CREATE USER ${artifactId} IDENTIFIED BY ${artifactId} DEFAULT TABLESPACE TBS_${artifactId} TEMPORARY TABLESPACE TEMP;
GRANT CONNECT, RESOURCE, UNLIMITED TABLESPACE to ${artifactId};