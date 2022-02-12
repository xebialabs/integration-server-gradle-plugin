package com.xebialabs.gradle.integration.util

import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

class PostgresDbUtil {
    def static resetSequences(logger, connection) {
        try {
            Statement seqStmt = connection.createStatement()
            seqStmt.closeOnCompletion()
            ResultSet rs = seqStmt.executeQuery("SELECT c.relname FROM pg_class c WHERE c.relkind = 'S';")
            while (rs.next()) {
                String sequence = rs.getString('relname')
                String table = sequence.replace("_ID_seq", "")
                Statement updStmt = connection.createStatement()
                updStmt.closeOnCompletion()
                if (table.toUpperCase().equals(table)) {
                    updStmt.executeQuery("SELECT SETVAL('\"${sequence}\"', (SELECT MAX(\"ID\")+1 FROM \"${table}\"));")
                } else {
                    updStmt.executeQuery("SELECT SETVAL('\"${sequence}\"', (SELECT MAX(\"id\")+1 FROM \"${table}\"));")
                }
            }
        } catch (SQLException e) {
            logger.error('Error occurred while resetting sequences.')
            e.printStackTrace()
            throw e
        }
    }
}
