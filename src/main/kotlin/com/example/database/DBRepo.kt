package com.example.database

import com.example.model.Customer
import com.example.model.CustomerLicense
import com.example.model.Device
import com.example.model.License
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toKotlinInstant
import java.sql.Connection
import java.sql.Statement

class DBRepo(private val connection: Connection) {
    companion object {
        private const val ADD_CUSTOMER = """
                INSERT INTO customer(customer_id, customer_name) VALUES (?,?) RETURNING *;
                """
        private const val UPDATE_CUSTOMER = """
                UPDATE customer SET customer_id=?, customer_name=? WHERE customer_id=? RETURNING *;
                """
        private const val DELETE_CUSTOMER = """
                DELETE FROM customer WHERE customer_id=?;
                """
        private const val RETRIEVE_CUSTOMER_LICENSES = """
                SELECT cl.customer_id, l.license_id, l.expires, l.used
                FROM license as l
                LEFT JOIN customer_licenses as cl on cl.license_id=l.license_id
                WHERE cl.customer_id=?;
                """
        private const val SET_LICENSE_TRUE = """
                UPDATE license SET used=true WHERE license_id=?;
                """
        private const val SET_LICENSE_FALSE = """
                UPDATE license SET used=false WHERE license_id=?;
                """
        private const val ADD_DEVICE = """""
                INSERT INTO device(device_name, device_id) VALUES (?,?) RETURNING *;
                """"
    }

    suspend fun addCustomer(customer: Customer): Customer = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(ADD_CUSTOMER, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, customer.customerId)
        statement.setString(2, customer.customerName)
        val generatedKeys = statement.executeQuery()
        if (generatedKeys.next()) {
            return@withContext Customer(generatedKeys.getString("customer_id"), generatedKeys.getString("customer_name"))
        } else {
            throw Exception("Could not add customer " + customer.customerId)
        }
    }

    suspend fun updateCustomer(customer: Customer): Customer = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_CUSTOMER)
        statement.setString(1, customer.customerId)
        statement.setString(2, customer.customerName)
        statement.setString(3, customer.customerId)
        val generatedKeys = statement.executeQuery()
        if (generatedKeys.next()) {
            return@withContext Customer(generatedKeys.getString("customer_id"), generatedKeys.getString("customer_name"))
        } else {
            throw Exception("Could not add customer " + customer.customerId)
        }
    }

    suspend fun deleteCustomer(customerId: String) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_CUSTOMER)
        statement.setString(1, customerId)
        statement.execute()
    }

    suspend fun retrieveLicenses(customerId: String): CustomerLicense = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(RETRIEVE_CUSTOMER_LICENSES, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, customerId)
        val generatedKeys = statement.executeQuery()
        val licenses = mutableListOf<License>()
        while (generatedKeys.next()) {
            licenses.add(License(
                generatedKeys.getString("license_id"),
                generatedKeys.getTimestamp("expires").toInstant().toKotlinInstant(),
                generatedKeys.getBoolean("used")
                ))
        }
        return@withContext CustomerLicense(customerId, licenses)
    }

    suspend fun assignLicense(licenseId: String) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SET_LICENSE_TRUE)
        statement.setString(1, licenseId)
        statement.execute()
    }

    suspend fun deleteLicense(licenseId: String) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SET_LICENSE_FALSE)
        statement.setString(1, licenseId)
        statement.execute()
    }
    suspend fun addDevice(device: Device): Device = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(ADD_DEVICE, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, device.deviceId)
        statement.setString(2, device.deviceName)
        val generatedKeys = statement.executeQuery()
        if (generatedKeys.next()) {
            return@withContext Device(generatedKeys.getString("device_id"), generatedKeys.getString("device_name"))
        } else {
            throw Exception("Could not add device " + device.deviceId)
        }
    }
}
