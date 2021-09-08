package ai.digital.integration.server.util

class LogbackConfigs {

    static def toLogSql() {
        [
                "com.xebialabs.deployit.core.sql.batch"     : "debug",
                "org.springframework.jdbc.core.JdbcTemplate": "trace",
                "org.hibernate.sql"                         : "trace",
                "org.hibernate.type"                        : "all"
        ]
    }
}
