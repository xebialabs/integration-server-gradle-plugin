-- see https://www.ibm.com/support/knowledgecenter/en/SS4Q96_6.1.0/com.ibm.help.scc.install.doc/SCC_Create_DB2_DB_From_Scripts.html
-- create database xlr using codeset UTF8 territory us PAGESIZE 32768;
-- creating a database takes a long time
create database xldrepo using codeset UTF8 territory us PAGESIZE 8192;
connect to xldrepo;

CREATE BUFFERPOOL TMP_BP SIZE AUTOMATIC PAGESIZE 32K;
connect reset;

connect to xldrepo;
CREATE SYSTEM TEMPORARY TABLESPACE TMP_TBSP PAGESIZE 32K MANAGED BY SYSTEM USING ('/home/db2inst1/xld_tmp') BUFFERPOOL TMP_BP;
CREATE SCHEMA xldrepo AUTHORIZATION xldrepo;
connect reset;
