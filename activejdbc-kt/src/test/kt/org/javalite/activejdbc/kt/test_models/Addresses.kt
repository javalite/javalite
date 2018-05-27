/*
 * COPYRIGHT © ITAMETIS - TOUS DROITS RÉSERVÉS
 * Pour plus d'information veuillez contacter : copyright@itametis.com
 */
package org.javalite.activejdbc.kt.test_models

import org.javalite.activejdbc.CompanionModel

open class Addresses {
    companion object:CompanionModel<Address>(Address::class) {
        const val ID = "ID"
        const val CITY = "CITY"
        const val STREET = "STREET"
        const val PERSON_ID = "PERSON_ID"
    }
}


