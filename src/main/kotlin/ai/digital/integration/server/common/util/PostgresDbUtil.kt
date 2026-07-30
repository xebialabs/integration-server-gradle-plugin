package ai.digital.integration.server.common.util

import org.gradle.api.Project
import java.sql.Connection
import java.sql.SQLException

class PostgresDbUtil {
    companion object {
        fun resetSequences(project: Project, connection: Connection) {
            try {
                val seqStmt = connection.createStatement()
                seqStmt.closeOnCompletion()
                val rs = seqStmt.executeQuery("SELECT c.relname FROM pg_class c WHERE c.relkind = 'S';")
                while (rs.next()) {
                    val sequence = rs.getString("relname")
                    val table = sequence.replace("_ID_seq", "")
                    // Check if the table exists before attempting to reset the sequence.
                    // DBUnit CLEAN_INSERT may not create tables that were empty at export time,
                    // so their sequences exist (from schema creation) but the table does not
                    // appear in the dataset — attempting MAX("ID") would throw.
                    val checkStmt = connection.createStatement()
                    checkStmt.closeOnCompletion()
                    val tableExists = checkStmt.executeQuery(
                        "SELECT 1 FROM pg_class WHERE relname = '${table}' AND relkind = 'r'")
                    if (!tableExists.next()) {
                        project.logger.lifecycle("[resetSequences] Skipping sequence '${sequence}' — table '${table}' not found")
                        continue
                    }
                    val updStmt = connection.createStatement()
                    updStmt.closeOnCompletion()
                    updStmt.executeQuery("SELECT SETVAL('\"${sequence}\"', (SELECT COALESCE(MAX(\"ID\"), 0)+1 FROM \"${table}\"));")
                    project.logger.lifecycle("[resetSequences] Reset '${sequence}' to MAX(ID)+1")
                }
            } catch (e: SQLException) {
                project.logger.error("Error occurred while resetting sequences.")
                e.printStackTrace()
                throw e
            }
        }
    }
}
