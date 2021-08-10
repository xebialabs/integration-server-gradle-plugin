package ai.digital.integration.server.util

import ai.digital.integration.server.IntegrationServerExtension
import com.fasterxml.jackson.core.TreeNode
import ai.digital.integration.server.domain.Database
import org.gradle.api.GradleException
import org.gradle.api.Project

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

    static randomDerbyPort = HTTPUtil.findFreePort()

    private DbUtil() {}

    static String databaseName(Project project) {
        PropertyUtil.resolveValue(project, "database", DERBY_INMEMORY)
    }

    static def dbConfigFile(Project project) {
        def dbname = databaseName(project)
        DbUtil.class.classLoader.getResourceAsStream("database-conf/deploy-repository.yaml.${dbname}")
    }

    static def isDerby(Project project) {
        def dbname = databaseName(project)
        isDerby(dbname)
    }

    static def isDerby(String name) {
        name == DERBY_NETWORK || name == DERBY_INMEMORY || name == DERBY
    }

    static def isDerbyNetwork(Project project) {
        def dbName = databaseName(project)
        dbName == DERBY_NETWORK || dbName == DERBY
    }

    static def assertNotDerby(Project project, message) {
        def dbname = databaseName(project)
        if (isDerby(dbname)) {
            throw new GradleException(message)
        }
    }

    static TreeNode dbConfig(Project project) {
        def from = dbConfigFile(project)
        TreeNode config = YamlFileUtil.readTree(from)

        if (isDerbyNetwork(project)) {
            def port = getDatabase(project).derbyPort
            config.get("xl.repository")
                    .get("database")
                    .put("db-url", "jdbc:derby://localhost:$port/xldrepo;create=true;user=admin;password=admin")
        }
        config
    }

    private static enrichDatabase(Project project, Database database) {
        def port = project.hasProperty("derbyPort") ? Integer.valueOf(project.property("derbyPort").toString()) : randomDerbyPort
        database.setDerbyPort(port)

        def logSql = project.hasProperty("logSql") ? Boolean.valueOf(project.property("logSql").toString()) : database.logSql
        database.setLogSql(logSql)

        database
    }

    static Database getDatabase(Project project) {
        def databases = project.extensions.getByType(IntegrationServerExtension).databases
        enrichDatabase(project, databases.isEmpty() ? new Database(databaseName(project)) : databases.first())
    }

    static def getResolveDbFilePath(Project project) {
        def composeFileName = dockerComposeFileName(project)
        DockerComposeUtil.getResolvedDockerPath(project, "database-compose/$composeFileName")
    }

    static def dockerComposeFileName(Project project) {
        def dbName = databaseName(project)
        "docker-compose_${dbName}.yaml"
    }

    static final DbParameters postgresParams = new DbParameters(
            'org.postgresql:postgresql',
            'org.postgresql.Driver',
            "org.dbunit.ext.postgresql.PostgresqlDataTypeFactory",
            null,
            "\"?\""
    )
    static final DbParameters mysqlParams = new DbParameters(
            'mysql:mysql-connector-java',
            "com.mysql.jdbc.Driver",
            "org.dbunit.ext.mysql.MySqlDataTypeFactory",
            "org.dbunit.ext.mysql.MySqlMetadataHandler",
            "`?`"
    )
    static final DbParameters oracle19Params = new DbParameters(
            'com.oracle.database.jdbc:ojdbc11',
            "oracle.jdbc.OracleDriver",
            "org.dbunit.ext.oracle.OracleDataTypeFactory",
            null,
            "\"?\""
    )
    static final DbParameters mssqlParams = new DbParameters(
            'com.microsoft.sqlserver:mssql-jdbc',
            "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "org.dbunit.ext.mssql.MsSqlDataTypeFactory",
            null,
            "\"?\""
    )
    static final DbParameters derbyParams = new DbParameters(
            'org.apache.derby:derby',
            null,
            null,
            null,
            "\"?\""
    )
    static final DbParameters derbyNetworkParams = new DbParameters(
            'org.apache.derby:derbyclient',
            null,
            null,
            null,
            "\"?\""
    )

    static def detectDbDependencies(db) {
        switch (db) {
            case DERBY: return derbyNetworkParams
            case DERBY_INMEMORY: return derbyParams
            case DERBY_NETWORK: return derbyNetworkParams
            case MSSQL: return mssqlParams
            case [MYSQL, MYSQL8]: return mysqlParams
            case ORACLE19: return oracle19Params
            case [POSTGRES, POSTGRES12]: return postgresParams
            default: return derbyNetworkParams
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
