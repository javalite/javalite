/*
 * COPYRIGHT © ITAMETIS - TOUS DROITS RÉSERVÉS
 * Pour plus d'information veuillez contacter : copyright@itametis.com
 */
package org.javalite.activejdbc.utils

import java.io.IOException
import java.util.Properties

object KtJdbcProperties {

    val driver:String
    val url:String
    val user:String
    val password:String
    val db:String

    init {
        try {
            val properties = Properties()
            properties.load(KtJdbcProperties::class.java.getResourceAsStream("/jdbc.properties"))
            driver = properties.getProperty("jdbc.driver") ?: ""
            url = properties.getProperty("jdbc.url") ?: ""
            user = properties.getProperty("jdbc.user") ?: ""
            password = properties.getProperty("jdbc.password") ?: ""
            db = properties.getProperty("db") ?: ""
        }
        catch (ex:IOException) {
            throw RuntimeException(ex)
        }
    }
}
