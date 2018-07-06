/*
 * COPYRIGHT © ITAMETIS - TOUS DROITS RÉSERVÉS
 * Pour plus d'information veuillez contacter : copyright@itametis.com
 */
package org.javalite.activejdbc.utils

import java.sql.Connection
import java.sql.SQLException

object KtActiveJDBCTest {
    fun getStatements(delimiter:String, file:String):List<String> {
        println("Getting statements from file: $file")
        return javaClass.getResource(file).readText().split(delimiter)
    }

    @Throws(SQLException::class)
    fun resetSchema(statements:List<String>, connection:Connection) {
        for (statement in statements) {
            if (!statement.isEmpty()) {
                connection.createStatement().use {
                    it.executeUpdate(statement)
                }
            }
        }
    }
}
