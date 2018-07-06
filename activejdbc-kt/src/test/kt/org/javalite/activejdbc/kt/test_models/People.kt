/*
 * COPYRIGHT © ITAMETIS - TOUS DROITS RÉSERVÉS
 * Pour plus d'information veuillez contacter : copyright@itametis.com
 */
package org.javalite.activejdbc.kt.test_models

import org.javalite.activejdbc.CompanionModel

open class People {
    companion object:CompanionModel<Person>(Person::class) {
        const val ID = "ID"
        const val NAME = "NAME"
        const val FIRST_NAME = "FIRST_NAME"
    }
}

