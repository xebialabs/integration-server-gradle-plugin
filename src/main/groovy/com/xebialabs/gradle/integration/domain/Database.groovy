package com.xebialabs.gradle.integration.domain

import com.xebialabs.gradle.integration.util.HTTPUtil

class Database {

    Integer derbyPort = HTTPUtil.findFreePort()

    Map<String, String> driverVersions = [
            'postgres-10'  : '42.2.9',
            'postgres-12'  : '42.2.23',
            'mysql'        : '8.0.22',
            'mysql-8'      : '8.0.22',
            'oracle-19c-se': '21.1.0.0',
            'mssql'        : '8.4.1.jre8',
    ]

    Boolean logSql = false

    String name

    Database(final String name) {
        this.name = name
    }
}
