CREATE USER skullking WITH PASSWORD 'password';
CREATE DATABASE skullking WITH OWNER skullking;

\connect skullking;
CREATE SCHEMA skullking
GRANT ALL ON SCHEMA skullking TO skullking;
