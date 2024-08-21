package com.example.plugins

import io.ktor.client.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import kotlinx.serialization.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*

@Serializable
data class LicenseResponse(val customerId: String, val licenses: List<License>)

@Serializable
data class License(val id: String, val expiration: Double, val used: Boolean)

fun extractLicenses(licensesJson: String): List<License> {
    val json = Json { ignoreUnknownKeys = true }
    val licenseResponse = json.decodeFromString<LicenseResponse>(licensesJson)
    return licenseResponse.licenses
}

object HttpClientSingleton {
    val client: HttpClient by lazy {
        HttpClient(CIO) {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = "userName", password = "SuperSecretPassword1234!!")
                    }
                    realm = "Access to the '/' path"
                }
            }
        }
    }
}

fun Application.configureDevicesController() {

    routing {
        // Create device endpoint
        post("/createDevice/{customerId}") {
            val headers: Headers = call.request.headers
            val customerId: String = call.parameters["customerId"] ?: throw IllegalArgumentException("Invalid customer ID")
            val deviceName: String = headers["deviceName"] ?: throw IllegalArgumentException("Invalid device name")
            val deviceId = headers["deviceId"] ?: throw IllegalArgumentException("Invalid device type")
            val locationId: String = headers["locationId"] ?: throw IllegalArgumentException("Invalid location ID")
            val licensesJson = getLicenses(customerId)
            val extractedLicenses = extractLicenses(licensesJson)
            println("Extracted Licenses: $extractedLicenses")

            val unusedLicense = extractedLicenses.find { !it.used && it.expiration > System.currentTimeMillis() / 1000 }
            if (unusedLicense != null) {
                println("Unused License: $unusedLicense")
                HttpClientSingleton.client.post("https://wbla-sandbox-rest.azurewebsites.net/license/${unusedLicense.id}/assign")
                // set the license to used in DB
                call.respond(HttpStatusCode.OK, "Device created for customer $customerId with device name $deviceName, device ID $deviceId and location ID $locationId using license ${unusedLicense.id}")
            } else {
                call.respond(HttpStatusCode.BadRequest, "All licenses are used or expired. Remove devices or purchase more licenses.")
            }
        }
        post("/deleteDevice/{customerId}") {

            val headers: Headers = call.request.headers
            // get the license id from db or headers
            val licenseId: String = headers["licenseId"] ?: throw IllegalArgumentException("Invalid license ID")
            val customerId: String = call.parameters["customerId"] ?: throw IllegalArgumentException("Invalid customer ID")
            val deviceId: String = headers["deviceId"] ?: throw IllegalArgumentException("Invalid device ID")
            HttpClientSingleton.client.post("https://wbla-sandbox-rest.azurewebsites.net/license/${licenseId}/unassign")
            call.respond(HttpStatusCode.OK, "Device $deviceId deleted for customer $customerId")
        }

        get("/getLicenses/{customerId}") {
            val customerId: String = call.parameters["customerId"] ?: throw IllegalArgumentException("Invalid customer ID")
            val licenses = extractLicenses(getLicenses(customerId))

            call.respond(HttpStatusCode.OK, "License information for $customerId:\n${licenses.joinToString(separator = "\n") { it.id + "  Used:" + it.used }}")
        }
    }
}

suspend fun getLicenses(customerId: String): String {
    // Change later to not create client every time

    val response: HttpResponse = HttpClientSingleton.client.get("https://wbla-sandbox-rest.azurewebsites.net/license/$customerId")

    return response.bodyAsText()
}
