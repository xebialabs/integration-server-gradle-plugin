package com.xebialabs.gradle.integration.util

import org.gradle.api.GradleException
import org.gradle.api.Project

class DbUtil {

    static def POSTGRES = 'postgres'
    static def POSTGRES12 = 'postgres-12'
    static def ORACLE = 'oracle-xe-11g'
    static def ORACLE19 = 'oracle-19c-se'
    static def ORACLE12 = 'oracle-12c'
    static def DB2 = 'db2'
    static def MYSQL = 'mysql'
    static def MYSQL8 = 'mysql-8'
    static def MSSQL = 'mssql'
    static def DERBY_NETWORK = 'derby-network'
    static def DERBY_INMEMORY = 'derby-inmemory'

    private DbUtil() {}

    static def databaseName(project) {
        project.hasProperty("database") ? project.property("database").toString() : DERBY_INMEMORY
    }

    static def dbConfigFile(project) {
        def dbname = DbUtil.databaseName(project)
        return DbUtil.class.classLoader.getResourceAsStream("database-conf/deploy-repository.yaml.${dbname}")
    }

    static def isDerby(Project project) {
        def dbname = DbUtil.databaseName(project)
        return isDerby(dbname)
    }

    static def isDerby(String name) {
        return name == DERBY_NETWORK || name == DERBY_INMEMORY
    }

    static def assertNotDerby(project, message) {
        def dbname = DbUtil.databaseName(project)
        if (DbUtil.isDerby(dbname)) {
            throw new GradleException(message)
        }
    }

    static def dbConfig(project) {
        def from = DbUtil.dbConfigFile(project)
        return YamlUtil.mapper.readTree(from)
    }

    static final DbParameters postgresParams = new DbParameters(
            'org.postgresql:postgresql',
            'org.postgresql.Driver',
            "org.dbunit.ext.postgresql.PostgresqlDataTypeFactory",
            null,
            "\"?\""
    )
    static final DbParameters postgres12Pararms = new DbParameters(
            'org.postgresql:postgresql',
            'org.postgresql.Driver',
            "org.dbunit.ext.postgresql.PostgresqlDataTypeFactory",
            null,
            "\"?\""
    )
    static final DbParameters mysqlPararms = new DbParameters(
            'mysql:mysql-connector-java',
            "com.mysql.jdbc.Driver",
            "org.dbunit.ext.mysql.MySqlDataTypeFactory",
            "org.dbunit.ext.mysql.MySqlMetadataHandler",
            "`?`"
    )
    static final DbParameters oraclePararms = new DbParameters(
            'com.oracle.database.jdbc:ojdbc6',
            "oracle.jdbc.OracleDriver",
            "org.dbunit.ext.oracle.OracleDataTypeFactory",
            null,
            "\"?\""
    )
    static final DbParameters db2Pararms = new DbParameters(
            'com.ibm.db2:jcc',
            "com.ibm.db2.jcc.DB2Driver",
            "org.dbunit.ext.db2.Db2DataTypeFactory",
            "org.dbunit.ext.db2.Db2MetadataHandler",
            "\"?\""
    )
    static final DbParameters mssqlPararms = new DbParameters(
            'com.microsoft.sqlserver:mssql-jdbc',
            "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "org.dbunit.ext.mssql.MsSqlDataTypeFactory",
            null,
            "\"?\""
    )
    static final DbParameters derbyPararms = new DbParameters(
            'org.apache.derby:derby',
            null,
            null,
            null,
            "\"?\""
    )
    static final DbParameters derbyNetworkPararms = new DbParameters(
            'org.apache.derby:derbyclient',
            null,
            null,
            null,
            "\"?\""
    )

    static def detectDbDependency(db) {
        switch (db) {
            case [POSTGRES, POSTGRES12]: return postgresParams
            case POSTGRES12: return postgres12Pararms
            case [ORACLE, ORACLE12, ORACLE19 ]: return oraclePararms
            case DB2: return db2Pararms
            case [MYSQL, MYSQL8]: return mysqlPararms
            case MSSQL: return mssqlPararms
            case DERBY_NETWORK: return derbyNetworkPararms
            case DERBY_INMEMORY: return derbyPararms
            default: return null
        }
    }
}

class DbParameters {
    String driverDependency
    String driverClass
    String dataFactory
    String metaFactory
    String escapePattern

    DbParameters(String driverDependency,
                 String driverClass,
                 String dataFactory,
                 String metaFactory,
                 String escapePattern) {
        this.driverDependency = driverDependency
        this.driverClass = driverClass
        this.dataFactory = dataFactory
        this.metaFactory = metaFactory
        this.escapePattern = escapePattern
    }
}
