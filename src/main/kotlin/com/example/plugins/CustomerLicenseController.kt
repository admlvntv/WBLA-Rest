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
        get("/license/{customerId}") {
            val customerId: String = call.parameters["customerId"] ?: throw IllegalArgumentException("Invalid customer ID")
            println("Received customerId: $customerId")
            try {
                val license = dbRepo.retrieveLicenses(customerId)

                // Check if the license list is empty
                if (license.licenses.isNotEmpty()) {
                    call.respond(HttpStatusCode.OK, license)
                } else {
                    call.respond(HttpStatusCode.NotFound, "No licenses found for customerId: $customerId")
                }
            } catch (e: SQLException) {
                // Handle database related exceptions separately
                println("SQLException: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, "Database error occurred")
            } catch (e: IllegalArgumentException) {
                // Handle IllegalArgumentException
                println("IllegalArgumentException: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, "Invalid request")
            } catch (e: Exception) {
                // Handle other exceptions
                println("Exception: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, "An unexpected error occurred")
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
