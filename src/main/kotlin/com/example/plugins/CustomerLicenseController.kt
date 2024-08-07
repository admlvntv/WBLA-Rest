package com.example.plugins

import com.example.database.DBRepo
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.*
import kotlinx.coroutines.*

fun Application.configureCustomerLicenseController() {
    val dbConnection: Connection = connectToPostgres()
    val dbRepo = DBRepo(dbConnection)

    install(ContentNegotiation) {
        json()
    }

    routing {
        // get /{customerId} of media type JSON
        get("/license/{customerId}") {
                val customerId: String = call.parameters["customerId"] ?: throw IllegalArgumentException("Invalid customer ID")
                try {
                    val license = dbRepo.retrieveLicenses(customerId)
                    call.respond(HttpStatusCode.OK, license)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

        post("/{licenseId}/assign") {
            val licenseId: String = call.parameters["licenseId"] ?: throw IllegalArgumentException("Invalid license ID")
            try {
                dbRepo.assignLicense(licenseId)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        post("/{licenseId}/unassign") {
            val licenseId: String = call.parameters["licenseId"] ?: throw IllegalArgumentException("Invalid license ID")
            try {
                dbRepo.deleteLicense(licenseId)
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

fun Application.connectToPostgres(): Connection {
    Class.forName("org.postgresql.Driver")
    return DriverManager.getConnection("jdbc:postgresql://wbla-restapi.postgres.database.azure.com:5432/postgres", "pgadmin", "SuperSecretPassword1234!!")
}
