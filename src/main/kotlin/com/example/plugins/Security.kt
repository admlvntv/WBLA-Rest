package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.util.*

fun Application.configureSecurity() {
    val digestFunction = getDigestFunction("SHA-256") { "ktor${it.length}" }
    val hashedUserTable = UserHashedTableAuth(
        table = mapOf(
            "WBLA" to digestFunction("AVerySecurePassword")
        ),
        digester = digestFunction
    )


    install(Authentication) {
        basic(name = "basic-auth") {
            realm = "WBLA-Rest"
            validate { credentials ->
                hashedUserTable.authenticate(credentials)
            }
        }
    }

}
