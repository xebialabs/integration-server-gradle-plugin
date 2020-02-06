package com.xebialabs.gradle.integration.util

import com.typesafe.config.ConfigFactory
import org.apache.commons.io.IOUtils
import org.gradle.api.GradleException
import org.gradle.api.Project

import java.nio.charset.StandardCharsets

class DbUtil {

    private DbUtil() {}

    static def databaseName(project) {
        project.hasProperty("database") ? project.property("database").toString() : "derby-inmemory"
    }

    static def dbConfigFile(project) {
        def dbname = DbUtil.databaseName(project)
        return DbUtil.class.classLoader.getResourceAsStream("database-conf/xl-deploy.conf.${dbname}")
    }

    static def isDerby(Project project) {
        def dbname = DbUtil.databaseName(project)
        return isDerby(dbname)
    }

    static def isDerby(String name) {
        return name == 'derby-network' || name == 'derby-inmemory'
    }

    static def assertNotDerby(project, message) {
        def dbname = DbUtil.databaseName(project)
        if (DbUtil.isDerby(dbname)) {
            throw new GradleException(message)
        }
    }

    static def dbConfig(project) {
        def from = DbUtil.dbConfigFile(project)
        def configFileStr = IOUtils.toString(from, StandardCharsets.UTF_8.name())
        return ConfigFactory.parseString(configFileStr)
    }

    static final DbParameters postgresParams = new DbParameters(
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
        'com.oracle:ojdbc6',
        "oracle.jdbc.OracleDriver",
        "org.dbunit.ext.oracle.OracleDataTypeFactory",
        null,
        "\"?\""
    )
    static final DbParameters db2Pararms = new DbParameters(
        'com.ibm:db2jcc4',
        "com.mysql.jdbc.Driver",
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
            case 'postgres': return postgresParams
            case 'oracle-xe-11g': return oraclePararms
            case 'db2': return db2Pararms
            case ['mysql', 'mysql-8']: return mysqlPararms
            case 'mssql': return mssqlPararms
            case 'derby-network': return derbyNetworkPararms
            case 'derby-inmemory': return derbyPararms
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
