package ai.digital.integration.server.domain

class Database {

    Integer derbyPort

    Map<String, String> driverVersions = [
            'mssql'        : '8.4.1.jre8',
            'mysql'        : '8.0.22',
            'mysql-8'      : '8.0.22',
            'oracle-19c-se': '21.1.0.0',
            'postgres-10'  : '42.2.9',
            'postgres-12'  : '42.2.23',
    ]

    Boolean logSql = false

    String name

    Database(final String name) {
        this.name = name
    }
}
