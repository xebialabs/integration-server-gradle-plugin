package ai.digital.integration.server.common.util

import org.gradle.api.Project
import java.sql.Connection
import java.sql.SQLException

class PostgresDbUtil {
    companion object {
        private const val HIBERNATE_SEQUENCE = "public.hibernate_sequence"
        private const val REVINFO_TABLE = "public.revinfo"

        fun resetSequences(project: Project, connection: Connection) {
            try {
                val seqStmt = connection.createStatement()
                seqStmt.closeOnCompletion()
                val rs = seqStmt.executeQuery(
                    """
                                        WITH owned_mappings AS (
                                            SELECT
                                                seq.oid AS seq_oid,
                                                seq_ns.nspname AS seq_schema,
                                                seq.relname AS seq_name,
                                                tbl_ns.nspname AS table_schema,
                                                tbl.relname AS table_name,
                                                col.attname AS column_name
                                            FROM pg_class seq
                                            JOIN pg_namespace seq_ns ON seq.relnamespace = seq_ns.oid
                                            JOIN pg_depend dep ON dep.objid = seq.oid AND dep.deptype IN ('a', 'i')
                                            JOIN pg_class tbl ON dep.refobjid = tbl.oid
                                            JOIN pg_namespace tbl_ns ON tbl.relnamespace = tbl_ns.oid
                                            JOIN pg_attribute col ON col.attrelid = tbl.oid AND col.attnum = dep.refobjsubid
                                            WHERE seq.relkind = 'S'
                                        ),
                                        default_mappings AS (
                                            SELECT
                                                seq.oid AS seq_oid,
                                                seq_ns.nspname AS seq_schema,
                                                seq.relname AS seq_name,
                                                tbl_ns.nspname AS table_schema,
                                                tbl.relname AS table_name,
                                                col.attname AS column_name
                                            FROM pg_attrdef ad
                                            JOIN pg_class tbl ON tbl.oid = ad.adrelid
                                            JOIN pg_namespace tbl_ns ON tbl.relnamespace = tbl_ns.oid
                                            JOIN pg_attribute col ON col.attrelid = ad.adrelid AND col.attnum = ad.adnum
                                            JOIN pg_class seq ON seq.oid = to_regclass(
                                                substring(pg_get_expr(ad.adbin, ad.adrelid) FROM 'nextval\(''([^'']+)''::regclass\)')
                                            )
                                            JOIN pg_namespace seq_ns ON seq.relnamespace = seq_ns.oid
                                            WHERE seq.relkind = 'S'
                                        )
                                        SELECT DISTINCT
                                            m.seq_schema,
                                            m.seq_name,
                                            m.table_schema,
                                            m.table_name,
                                            m.column_name
                                        FROM (
                                            SELECT * FROM owned_mappings
                                            UNION ALL
                                            SELECT * FROM default_mappings
                                        ) m;
                    """.trimIndent()
                )

                fun quoteIdentifier(identifier: String): String =
                    "\"${identifier.replace("\"", "\"\"")}\""

                while (rs.next()) {
                    val seqSchema = rs.getString("seq_schema")
                    val seqName = rs.getString("seq_name")
                    val tableSchema = rs.getString("table_schema")
                    val tableName = rs.getString("table_name")
                    val columnName = rs.getString("column_name")

                    val qualifiedSequence = "${quoteIdentifier(seqSchema)}.${quoteIdentifier(seqName)}"
                    val qualifiedTable = "${quoteIdentifier(tableSchema)}.${quoteIdentifier(tableName)}"

                    val updStmt = connection.createStatement()
                    updStmt.closeOnCompletion()
                    updStmt.executeQuery(
                        "SELECT SETVAL('${qualifiedSequence}', " +
                            "(SELECT COALESCE(MAX(${quoteIdentifier(columnName)}), 0) + 1 FROM ${qualifiedTable}), false);"
                    )
                    project.logger.lifecycle(
                        "[resetSequences] Reset '${seqSchema}.${seqName}' to MAX(${tableSchema}.${tableName}.${columnName})+1"
                    )
                }

                // Envers revision ids can be generated from hibernate_sequence even when REVINFO.REV
                // has no DEFAULT nextval(...) expression. Keep it aligned with MAX(REVINFO.REV).
                alignHibernateRevisionSequence(project, connection)
            } catch (e: SQLException) {
                project.logger.error("Error occurred while resetting sequences.")
                e.printStackTrace()
                throw e
            }
        }

        private fun alignHibernateRevisionSequence(project: Project, connection: Connection) {
            val alignStmt = connection.createStatement()
            alignStmt.closeOnCompletion()
            val alignRs = alignStmt.executeQuery(
                """
                SELECT
                    EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
                            WHERE n.nspname = 'public' AND c.relname = 'revinfo' AND c.relkind = 'r') AS has_revinfo,
                    EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
                            WHERE n.nspname = 'public' AND c.relname = 'hibernate_sequence' AND c.relkind = 'S') AS has_hibernate_sequence;
                """.trimIndent()
            )

            if (!alignRs.next()) {
                return
            }

            val hasRevinfo = alignRs.getBoolean("has_revinfo")
            val hasHibernateSequence = alignRs.getBoolean("has_hibernate_sequence")

            if (!hasRevinfo || !hasHibernateSequence) {
                return
            }

            val setvalStmt = connection.createStatement()
            setvalStmt.closeOnCompletion()
            setvalStmt.executeQuery(
                "SELECT SETVAL('${HIBERNATE_SEQUENCE}', (SELECT COALESCE(MAX(rev), 0) + 1 FROM ${REVINFO_TABLE}), false);"
            )
            project.logger.lifecycle(
                "[resetSequences] Reset '${HIBERNATE_SEQUENCE}' to MAX(${REVINFO_TABLE}.rev)+1"
            )
        }
    }
}
