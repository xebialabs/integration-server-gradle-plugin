package ai.digital.integration.server.config

class LogbackConfigs {
    companion object {
        @JvmStatic
        val toLogSql = mutableMapOf(
            "com.xebialabs.deployit.core.sql.batch" to "debug",
            "org.springframework.jdbc.core.JdbcTemplate" to "trace",
            "org.hibernate.sql" to "trace",
            "org.hibernate.type" to "all"
        )
    }
}
