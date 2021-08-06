package com.xebialabs.gradle.integration.util

import org.gradle.api.GradleException
import org.gradle.api.Project

import java.nio.file.Paths

import static com.xebialabs.gradle.integration.util.PluginUtil.DIST_DESTINATION_NAME

class DbUtil {

    static def POSTGRES = 'postgres-10'
    static def POSTGRES12 = 'postgres-12'
    static def ORACLE19 = 'oracle-19c-se'
    static def MYSQL = 'mysql'
    static def MYSQL8 = 'mysql-8'
    static def MSSQL = 'mssql'
    static def DERBY = 'derby'
    static def DERBY_NETWORK = 'derby-network'
    static def DERBY_INMEMORY = 'derby-inmemory'

    private DbUtil() {}

    static String databaseName(Project project) {
        PropertyUtil.resolveValue(project, "database", DERBY_INMEMORY)
    }

    static def dbConfigFile(Project project) {
        def dbname = databaseName(project)
        return DbUtil.class.classLoader.getResourceAsStream("database-conf/deploy-repository.yaml.${dbname}")
    }

    static def isDerby(Project project) {
        def dbname = databaseName(project)
        return isDerby(dbname)
    }

    static def isDerby(String name) {
        return name == DERBY_NETWORK || name == DERBY_INMEMORY || name == DERBY
    }

    static def isDerbyNetwork(Project project) {
        def dbName = databaseName(project)
        return dbName == DERBY_NETWORK || dbName == DERBY
    }

    static def assertNotDerby(Project project, message) {
        def dbname = databaseName(project)
        if (isDerby(dbname)) {
            throw new GradleException(message)
        }
    }

    static def dbConfig(Project project) {
        def from = dbConfigFile(project)
        def config = YamlFileUtil.readTree(from)
        def port = ExtensionsUtil.getExtension(project).derbyPort

        if (isDerbyNetwork(project)) {
            config.get("xl.repository")
                    .get("database")
                    .put("db-url", "jdbc:derby://localhost:$port/xldrepo;create=true;user=admin;password=admin")
        }
        return config
    }

    static def dockerComposeFileName(Project project) {
        def dbName = databaseName(project)
        return "docker-compose_${dbName}.yaml"
    }

    static def dockerComposeFileDestination(Project project) {
        def composeFileName = dockerComposeFileName(project)
        return Paths.get(
                "${project.buildDir.toPath().resolve(DIST_DESTINATION_NAME).toAbsolutePath().toString()}/${composeFileName}")
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
    static final DbParameters oracle19Pararms = new DbParameters(
            'com.oracle.database.jdbc:ojdbc11',
            "oracle.jdbc.OracleDriver",
            "org.dbunit.ext.oracle.OracleDataTypeFactory",
            null,
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
            case DERBY: return derbyNetworkPararms
            case DERBY_INMEMORY: return derbyPararms
            case DERBY_NETWORK: return derbyNetworkPararms
            case MSSQL: return mssqlPararms
            case [MYSQL, MYSQL8]: return mysqlPararms
            case ORACLE19: return oracle19Pararms
            case [POSTGRES, POSTGRES12]: return postgresParams
            default: return derbyNetworkPararms
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
