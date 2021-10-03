package ai.digital.integration.server.util

import org.gradle.api.Project
import java.sql.Connection
import java.sql.SQLException

class PostgresDbUtil {
    companion object {
        @JvmStatic
        fun resetSequences(project: Project, connection: Connection) {
            try {
                val seqStmt = connection.createStatement()
                seqStmt.closeOnCompletion()
                val rs = seqStmt.executeQuery("SELECT c.relname FROM pg_class c WHERE c.relkind = 'S';")
                while (rs.next()) {
                    val sequence = rs.getString("relname")
                    val table = sequence.replace("_ID_seq", "")
                    val updStmt = connection.createStatement()
                    updStmt.closeOnCompletion()
                    updStmt.executeQuery("SELECT SETVAL('\"${sequence}\"', (SELECT MAX(\"ID\")+1 FROM \"${table}\"));")
                }
            } catch (e: SQLException) {
                project.logger.error("Error occurred while resetting sequences.")
                e.printStackTrace()
                throw e
            }
        }
    }
}
