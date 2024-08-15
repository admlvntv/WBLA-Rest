package com.example.database

import com.example.model.Customer
import com.example.model.CustomerLicense
import com.example.model.License
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toKotlinInstant
import java.sql.Connection
import java.sql.Statement

class DBRepoNew(private val connection: Connection) {
    companion object {

    }
}
