/*
 * COPYRIGHT © ITAMETIS - TOUS DROITS RÉSERVÉS
 * Pour plus d'information veuillez contacter : copyright@itametis.com
 */
package org.javalite.activejdbc

import org.javalite.activejdbc.kt.test_models.Address
import org.javalite.activejdbc.kt.test_models.Addresses
import org.javalite.activejdbc.kt.test_models.People
import org.javalite.activejdbc.kt.test_models.Person
import org.javalite.activejdbc.utils.KtActiveJDBCTest
import org.javalite.activejdbc.utils.KtJdbcProperties
import org.javalite.test.jspec.JSpec.a
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class CompanionModelTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun `open DB`() {
            println("KT - Open Database")
            val db = Base.open(
                KtJdbcProperties.driver, KtJdbcProperties.url, KtJdbcProperties.user, KtJdbcProperties.password
            )

            KtActiveJDBCTest.resetSchema(
                KtActiveJDBCTest.getStatements(";", "/kt_h2_schema.sql"),
                db.connection()
            )
        }

        @AfterClass
        @JvmStatic
        fun `close DB`() {
            println("KT - Close Database")
            Base.close()
        }
    }


    @Test
    fun `CompanionModel is able to insert entity without dependency`() {
        // Given
        val person = People.create(
            People.FIRST_NAME, "Bruce",
            People.NAME, "Wayne"
        )


        // When
        val saved = person.saveIt()

        // Then
        a(saved).shouldBeTrue()

        // Clean
        People.deleteAll()
    }

    @Test
    fun `CompanionModel is able to insert entity with dependency`() {

        // Given
        val person = People.createIt(
            People.FIRST_NAME, "Clark",
            People.NAME, "Kent"
        )

        val address = Address()
            .set<Address>(Addresses.CITY, "METROPOLIS")
            .set<Address>(Addresses.STREET, "Somewhere over the rainbow")

        // When
        person.add(address)
        person.saveIt()

        // Then
        a(address.getLong(Addresses.PERSON_ID)).shouldNotBeNull()

        // Clean
        Addresses.deleteAll()
        People.deleteAll()
    }

    @Test
    fun `CompanionModel is able to retrieve all elements in base`() {
        // Given
        People.createIt(
            People.FIRST_NAME, "Clark",
            People.NAME, "Kent"
        )

        People.createIt(
            People.FIRST_NAME, "Bruce",
            People.NAME, "Wayne"
        )

        // When
        val people = People.findAll()

        // Then
        a(people.size).shouldEqual(2)

        // Clean
        People.deleteAll()
    }

    @Test
    fun `CompanionModel is able to delete specific element`() {
        // Given
        People.createIt(
            People.FIRST_NAME, "Clark",
            People.NAME, "Kent"
        )

        People.createIt(
            People.FIRST_NAME, "Bruce",
            People.NAME, "Wayne"
        )

        // When
        val nbDeletions = People.delete("${People.FIRST_NAME} = ?", "Clark")

        // Then
        a(nbDeletions).shouldEqual(1)

        // Clean
        People.deleteAll()
    }

    @Test
    fun `CompanionModel is able to update specific element`() {
        // Given
        val bruce:Person = People.createIt(
            People.FIRST_NAME, "Bruce",
            People.NAME, "Wayne"
        )

        // When
        bruce.setString<Person>(People.FIRST_NAME, "bat")
            .setString<Person>(People.NAME, "man")
            .saveIt()


        // Then
        val batman = People.findById(bruce.getLong(People.ID))
        a(batman.getString(People.FIRST_NAME)).shouldEqual("bat")
        a(batman.getString(People.NAME)).shouldEqual("man")

        // Clean
        People.deleteAll()
    }
}
